package com.pharmacy.db;

import com.pharmacy.model.Medicine;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Veritabanı yöneticisi — SQLite JDBC bağlantısı ve CRUD işlemleri (Hafta 15).
 * PreparedStatement ile SQL Injection koruması sağlanır.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public DatabaseManager() {
        initializeDatabase();
    }

    private Connection openConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            // SQLite'da kısa süreli kilitlenmelerde bekleyip tekrar dene
            stmt.execute("PRAGMA busy_timeout = 5000");
            // Eşzamanlı okuma/yazma için daha stabil (desteklenmiyorsa hata vermez)
            stmt.execute("PRAGMA journal_mode = WAL");
        }
        return conn;
    }

    /**
     * Veritabanını ve tabloları oluşturur, boşsa örnek veri yükler.
     */
    private void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS medicines (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "category TEXT," +
                "stock_count INTEGER DEFAULT 0," +
                "unit_price REAL DEFAULT 0.0," +
                "critical_level INTEGER DEFAULT 5," +
                "expiry_date TEXT" +
                ");";

        try (Connection conn = openConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

            // Tablo boşsa seed data yükle
            int count = 0;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM medicines")) {
                if (rs.next()) count = rs.getInt(1);
            }
            if (count == 0) {
                seedInitialData(conn);
                System.out.println("[✓] Veritabanı oluşturuldu ve 8 örnek ilaç yüklendi.");
            }
        } catch (SQLException e) {
            System.err.println("[✗] Veritabanı başlatma hatası: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════
    //  CRUD İşlemleri (Hafta 15: JDBC)
    // ══════════════════════════════════════════

    /**
     * Tüm ilaçları listeler.
     */
    public List<Medicine> getAllMedicines() {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT * FROM medicines ORDER BY name";

        try (Connection conn = openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return medicines;
    }

    /**
     * Yeni ilaç ekler (CREATE).
     */
    public void addMedicine(Medicine medicine) {
        String sql = "INSERT INTO medicines(name, category, stock_count, unit_price, critical_level, expiry_date) VALUES(?,?,?,?,?,?)";

        try (Connection conn = openConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, medicine.getName());
            pstmt.setString(2, medicine.getCategory());
            pstmt.setInt(3, medicine.getStockCount());
            pstmt.setDouble(4, medicine.getUnitPrice());
            pstmt.setInt(5, medicine.getCriticalLevel());
            pstmt.setString(6, medicine.getExpiryDate());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mevcut ilacı günceller (UPDATE).
     */
    public void updateMedicine(Medicine medicine) {
        String sql = "UPDATE medicines SET name=?, category=?, stock_count=?, unit_price=?, critical_level=?, expiry_date=? WHERE id=?";

        try (Connection conn = openConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, medicine.getName());
            pstmt.setString(2, medicine.getCategory());
            pstmt.setInt(3, medicine.getStockCount());
            pstmt.setDouble(4, medicine.getUnitPrice());
            pstmt.setInt(5, medicine.getCriticalLevel());
            pstmt.setString(6, medicine.getExpiryDate());
            pstmt.setInt(7, medicine.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * İlacı siler (DELETE).
     */
    public void deleteMedicine(int id) {
        String sql = "DELETE FROM medicines WHERE id=?";

        try (Connection conn = openConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════
    //  Gelişmiş Sorgular (PreparedStatement)
    // ══════════════════════════════════════════

    /**
     * İlaç adı veya kategoriye göre SQL LIKE araması.
     * Java'da filtreleme yerine doğrudan veritabanında aranır (performans).
     */
    public List<Medicine> searchMedicines(String keyword) {
        List<Medicine> results = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE LOWER(name) LIKE ? OR LOWER(category) LIKE ? ORDER BY name";

        try (Connection conn = openConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String param = "%" + keyword.toLowerCase() + "%";
            pstmt.setString(1, param);
            pstmt.setString(2, param);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(mapResultSetToMedicine(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Veritabanındaki benzersiz kategorileri döndürür (JComboBox için).
     */
    public List<String> getDistinctCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM medicines ORDER BY category";

        try (Connection conn = openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String cat = rs.getString("category");
                if (cat != null && !cat.isEmpty()) {
                    categories.add(cat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    /**
     * Toplam envanter değerini hesaplar (SUM sorgusu).
     */
    public double getTotalStockValue() {
        String sql = "SELECT SUM(stock_count * unit_price) AS total FROM medicines";

        try (Connection conn = openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Toplam stok adedini döndürür.
     */
    public int getTotalStockCount() {
        String sql = "SELECT SUM(stock_count) AS total FROM medicines";

        try (Connection conn = openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ══════════════════════════════════════════
    //  Seed Data — 8 farklı ilaç, 4 kategori
    // ══════════════════════════════════════════

    private void seedInitialData(Connection conn) throws SQLException {
        List<Medicine> meds = List.of(
                new Medicine(0, "Parol 500mg",       "Ağrı Kesici",   50,  45.50,  10, "01.12.2026"),
                new Medicine(0, "Aspirin Plus",      "Ağrı Kesici",    5,  32.00,  15, "15.06.2026"),
                new Medicine(0, "Voltaren Jel",      "Ağrı Kesici",   12,  67.50,   5, "20.03.2027"),
                new Medicine(0, "Augmentin 1000mg",  "Antibiyotik",    3, 120.00,  10, "20.08.2025"),
                new Medicine(0, "Amoksisilin 500mg", "Antibiyotik",   35,  85.00,   8, "15.09.2026"),
                new Medicine(0, "Redoxon C",         "Takviye",       30,  95.00,  10, "10.01.2027"),
                new Medicine(0, "D-Vitamin 1000IU",  "Takviye",       45,  55.00,  10, "30.06.2027"),
                new Medicine(0, "Nexium 40mg",       "Mide/Sindirim", 25, 345.50,   8, "01.11.2026"),
                new Medicine(0, "Rennie Çiğneme",    "Mide/Sindirim", 20,  45.00,  10, "15.08.2026"),
                new Medicine(0, "Zyrtec 10mg",       "Alerji",         2,  90.00,   5, "10.05.2026"),
                new Medicine(0, "Bepanthol Onarıcı", "Cilt",          18, 180.00,   5, "15.03.2027"),
                new Medicine(0, "Fucidin %2 Krem",   "Cilt",          14,  95.00,   5, "18.04.2027"),
                new Medicine(0, "Pedifen Şurup",     "Ateş Düşürücü", 42,  65.00,   5, "10.04.2022"),
                new Medicine(0, "Systane Damla",     "Göz / Kulak",   30, 110.00,  10, "01.09.2025"),
                new Medicine(0, "Tylolhot Poşet",    "Soğuk Algınlığı",40, 150.00,  15, "20.12.2026")
        );

        String sql = "INSERT INTO medicines(name, category, stock_count, unit_price, critical_level, expiry_date) VALUES(?,?,?,?,?,?)";
        boolean oldAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Medicine m : meds) {
                pstmt.setString(1, m.getName());
                pstmt.setString(2, m.getCategory());
                pstmt.setInt(3, m.getStockCount());
                pstmt.setDouble(4, m.getUnitPrice());
                pstmt.setInt(5, m.getCriticalLevel());
                pstmt.setString(6, m.getExpiryDate());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    /**
     * ResultSet → Medicine nesne dönüşümü.
     */
    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        return new Medicine(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getInt("stock_count"),
                rs.getDouble("unit_price"),
                rs.getInt("critical_level"),
                rs.getString("expiry_date")
        );
    }
}
