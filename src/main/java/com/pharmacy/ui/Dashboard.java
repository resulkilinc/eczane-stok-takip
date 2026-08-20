package com.pharmacy.ui;

import com.pharmacy.App;
import com.pharmacy.db.DatabaseManager;
import com.pharmacy.logic.SoundAlert;
import com.pharmacy.model.Medicine;
import com.pharmacy.util.UiUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Ana Yönetim Paneli — JFrame (Hafta 6), JMenuBar (Hafta 9),
 * JTabbedPane + JTable (Hafta 11), ActionListener + KeyListener (Hafta 13).
 */
public class Dashboard extends JFrame {
    private DatabaseManager db;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField txtSearch;

    // İstatistik kartları
    private JLabel lblTotalValue, lblTotalCount, lblLowStockCount, lblExpiredCount;
    private JPanel cardTotal, cardLowStock, cardValue, cardExpired;

    // Sekmeler
    private StockChartPanel chartPanel;
    private StatisticsPanel statsPanel;

    private boolean isInitialAlertPlayed = false;

    // ── Renk Paleti (Catppuccin Mocha) ──
    private static final Color CLR_SURFACE   = new Color(0x2D, 0x2D, 0x44);
    private static final Color CLR_BLUE      = new Color(0x89, 0xB4, 0xFA);
    private static final Color CLR_GREEN     = new Color(0xA6, 0xE3, 0xA1);
    private static final Color CLR_RED       = new Color(0xF3, 0x8B, 0xA8);
    private static final Color CLR_ORANGE    = new Color(0xFA, 0xB3, 0x87);
    private static final Color CLR_TEXT      = new Color(0xCD, 0xD6, 0xF4);
    private static final Color CLR_SUBTEXT   = new Color(0xA6, 0xAD, 0xC8);
    private static final Color CLR_ROW_DANGER   = new Color(60, 20, 30);
    private static final Color CLR_ROW_WARNING  = new Color(60, 45, 20);
    private static final Color CLR_ROW_ALT      = new Color(0x25, 0x25, 0x3A);

    public Dashboard() {
        db = new DatabaseManager();
        initMenu();
        initUI();
        refreshData();
    }

    // ══════════════════════════════════════════
    //  Hafta 9: Menüler + Mnemonics
    // ══════════════════════════════════════════

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        // ── Dosya Menüsü ──
        JMenu menuFile = new JMenu("Dosya");
        menuFile.setMnemonic(KeyEvent.VK_D);

        JMenuItem itemAdd = new JMenuItem("Yeni İlaç Ekle");
        itemAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        itemAdd.addActionListener(e -> openAddDialog());
        menuFile.add(itemAdd);

        JMenuItem itemRefresh = new JMenuItem("Yenile");
        itemRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        itemRefresh.addActionListener(e -> refreshData());
        menuFile.add(itemRefresh);

        menuFile.addSeparator();

        JMenuItem itemExit = new JMenuItem("Çıkış");
        itemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        itemExit.addActionListener(e -> System.exit(0));
        menuFile.add(itemExit);

        // ── Görünüm Menüsü ──
        JMenu menuView = new JMenu("Görünüm");
        menuView.setMnemonic(KeyEvent.VK_G);

        JMenuItem itemDark = new JMenuItem("🌙 Koyu Tema");
        itemDark.addActionListener(e -> App.switchTheme(true));
        menuView.add(itemDark);

        JMenuItem itemLight = new JMenuItem("☀ Açık Tema");
        itemLight.addActionListener(e -> App.switchTheme(false));
        menuView.add(itemLight);

        // ── Yardım Menüsü ──
        JMenu menuHelp = new JMenu("Yardım");
        menuHelp.setMnemonic(KeyEvent.VK_Y);

        JMenuItem itemAbout = new JMenuItem("Hakkında");
        itemAbout.addActionListener(e -> {
            AboutDialog aboutDlg = new AboutDialog(this);
            aboutDlg.setVisible(true);
        });
        menuHelp.add(itemAbout);

        menuBar.add(menuFile);
        menuBar.add(menuView);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);
    }

    // ══════════════════════════════════════════
    //  Arayüz Kurulumu
    // ══════════════════════════════════════════

    private void initUI() {
        setTitle("Eczane Stok ve İlaç Takip Sistemi");
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        // ── ÜST PANEL: Başlık + Kartlar + Arama ──
        JPanel pnlTop = new JPanel();
        pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
        pnlTop.setBorder(BorderFactory.createEmptyBorder(12, 15, 8, 15));

        // Başlık satırı
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel title = new JLabel("💊 Eczane Yönetim Paneli");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(CLR_BLUE);
        pnlTitle.add(title, BorderLayout.WEST);

        // Arama (Hafta 13: KeyListener + DocumentListener)
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlSearch.setOpaque(false);
        JLabel lblSearch = new JLabel("🔍");
        lblSearch.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtSearch = new JTextField(20);
        txtSearch.setToolTipText("İlaç adı veya kategori yazın...");
        JButton btnSearch = new JButton("Ara");
        JButton btnClear = new JButton("Temizle");
        Font headerBtnFont = UIManager.getFont("Button.font");
        if (headerBtnFont != null) {
            btnSearch.setFont(headerBtnFont);
            btnClear.setFont(headerBtnFont);
        }
        UiUtils.fixButtonTextLayout(btnSearch);
        UiUtils.fixButtonTextLayout(btnClear);

        pnlSearch.add(lblSearch);
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);
        pnlSearch.add(btnClear);
        pnlTitle.add(pnlSearch, BorderLayout.EAST);
        pnlTop.add(pnlTitle);
        pnlTop.add(Box.createVerticalStrut(10));

        // ── İSTATİSTİK KARTLARI ──
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlCards.setOpaque(false);
        pnlCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        lblTotalCount  = new JLabel("0", JLabel.CENTER);
        lblLowStockCount = new JLabel("0", JLabel.CENTER);
        lblTotalValue  = new JLabel("₺0", JLabel.CENTER);
        lblExpiredCount = new JLabel("0", JLabel.CENTER);

        cardTotal    = createStatCard("📦 Toplam İlaç",   lblTotalCount,   CLR_BLUE);
        cardLowStock = createStatCard("⚠ Kritik Stok",    lblLowStockCount, CLR_RED);
        cardValue    = createStatCard("💰 Envanter Değeri", lblTotalValue,   CLR_GREEN);
        cardExpired  = createStatCard("📅 SKT Geçen",      lblExpiredCount,  CLR_ORANGE);

        pnlCards.add(cardTotal);
        pnlCards.add(cardLowStock);
        pnlCards.add(cardValue);
        pnlCards.add(cardExpired);
        pnlTop.add(pnlCards);

        add(pnlTop, BorderLayout.NORTH);

        // ══ MERKEZ: JTabbedPane (Hafta 11) ══
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        // ─── Sekme 1: Stok Yönetimi ───
        JPanel pnlStock = new JPanel(new BorderLayout());
        String[] columns = {"ID", "İlaç Adı", "Kategori", "Stok", "Fiyat (₺)", "Kritik Seviye", "Son Kullanma"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0 || column == 3 || column == 5) return Integer.class;
                if (column == 4) return Double.class;
                return String.class;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        // TableRowSorter — sütun başlığına tıklayarak sıralama (Hafta 11)
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        // Son Kullanma (dd.MM.yyyy) sıralamasını gerçek tarihe göre yap
        // Aksi halde String sıralaması gün'e göre (lexicographic) hatalı sonuç verir.
        rowSorter.setComparator(6, (a, b) -> {
            LocalDate da = parseExpiryDateSafe(a);
            LocalDate db = parseExpiryDateSafe(b);
            return da.compareTo(db);
        });

        // Sütun genişlikleri
        table.getColumnModel().getColumn(0).setPreferredWidth(40);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(180);  // İsim
        table.getColumnModel().getColumn(2).setPreferredWidth(110);  // Kategori
        table.getColumnModel().getColumn(3).setPreferredWidth(60);   // Stok
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // Fiyat
        table.getColumnModel().getColumn(5).setPreferredWidth(90);   // Kritik
        table.getColumnModel().getColumn(6).setPreferredWidth(110);  // SKT

        // Custom Renderer — Kritik stok ve SKT renklendirmesi + Zebra striping
        table.setDefaultRenderer(Object.class, createTableRenderer());
        table.setDefaultRenderer(Integer.class, createTableRenderer());
        table.setDefaultRenderer(Double.class, createTableRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        pnlStock.add(scrollPane, BorderLayout.CENTER);

        // Aksiyon butonları (Alt Panel)
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnSell   = createStyledButton("➖ Satış Yap (-)", CLR_ORANGE);
        JButton btnSupply = createStyledButton("🚚 Tedarik Ekle (+)", new Color(0x94, 0xE2, 0xD5)); // Teal

        // Ayırıcı için boşluk
        pnlActions.add(btnSell);
        pnlActions.add(btnSupply);
        pnlActions.add(Box.createHorizontalStrut(20)); // Şov butonları ile klasikler arası boşluk

        JButton btnAdd    = createStyledButton("➕ İlaç Ekle",  CLR_GREEN);
        JButton btnEdit   = createStyledButton("✏ Düzenle",     CLR_BLUE);
        JButton btnDelete = createStyledButton("🗑 Sil",         CLR_RED);
        JButton btnRefresh = createStyledButton("🔄 Yenile",     CLR_SUBTEXT);

        pnlActions.add(btnAdd);
        pnlActions.add(btnEdit);
        pnlActions.add(btnDelete);
        pnlActions.add(btnRefresh);
        pnlStock.add(pnlActions, BorderLayout.SOUTH);

        // ─── Sekme 2: Grafiksel Raporlar (Hafta 12) ───
        chartPanel = new StockChartPanel();

        // ─── Sekme 3: İstatistikler ───
        statsPanel = new StatisticsPanel();

        tabbedPane.addTab("📋 Stok Yönetimi", pnlStock);
        tabbedPane.addTab("📊 Grafiksel Raporlar", chartPanel);
        tabbedPane.addTab("📈 İstatistikler", statsPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // ══ EVENT LISTENERS (Hafta 13-14) ══

        btnAdd.addActionListener(e -> openAddDialog());

        btnEdit.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                int id = (int) tableModel.getValueAt(modelRow, 0);
                Medicine med = db.getAllMedicines().stream()
                        .filter(m -> m.getId() == id).findFirst().orElse(null);
                if (med != null) {
                    MedicineDialog dialog = new MedicineDialog(this, med);
                    dialog.setVisible(true);
                    refreshData();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Lütfen düzenlemek istediğiniz ilacı tablodan seçin.",
                        "Uyarı", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnDelete.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                String name = (String) tableModel.getValueAt(modelRow, 1);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "\"" + name + "\" ilacını silmek istediğinize emin misiniz?",
                        "Silme Onayı", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    db.deleteMedicine((int) tableModel.getValueAt(modelRow, 0));
                    refreshData();
                    JOptionPane.showMessageDialog(this,
                            "\"" + name + "\" başarıyla silindi.",
                            "Bilgi", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Lütfen silmek istediğiniz ilacı tablodan seçin.",
                        "Uyarı", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnRefresh.addActionListener(e -> refreshData());

        // Satış Butonu (Toplu / Çoklu)
        btnSell.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                int currentStock = (int) tableModel.getValueAt(modelRow, 3);
                String name = (String) tableModel.getValueAt(modelRow, 1);

                if (currentStock <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "\"" + name + "\" ürününden stokta hiç kalmadı! Satış yapılamaz.",
                        "Stok Tükendi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String input = JOptionPane.showInputDialog(this,
                    "\"" + name + "\" ürününden kaç kutu satıldı?\n(Mevcut Stok: " + currentStock + ")",
                    "Satış İşlemi", JOptionPane.QUESTION_MESSAGE);

                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int amount = Integer.parseInt(input.trim());
                        if (amount <= 0) throw new NumberFormatException();
                        
                        if (amount > currentStock) {
                            JOptionPane.showMessageDialog(this,
                                "Hata: Stokta olandan (" + currentStock + " kutu) daha fazla satış yapamazsınız!",
                                "Yetersiz Stok", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        updateStockAndRefresh(modelRow, currentStock - amount);
                        
                        JOptionPane.showMessageDialog(this,
                            amount + " kutu başarıyla satıldı.\nKalan Stok: " + (currentStock - amount),
                            "Satış Başarılı", JOptionPane.INFORMATION_MESSAGE);

                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this,
                            "Lütfen satılacak miktarı pozitif bir tamsayı olarak giriniz.",
                            "Giriş Hatası", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lütfen satış yapmak için bir ilaç seçin.", "Satış İşlemi", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Toplu Tedarik Girişi (Kamyon Geldiğinde)
        btnSupply.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                String name = (String) tableModel.getValueAt(modelRow, 1);
                int currentStock = (int) tableModel.getValueAt(modelRow, 3);

                String input = JOptionPane.showInputDialog(this,
                    "\"" + name + "\" için depodan kaç kutu geldi?\n(Mevcut Stok: " + currentStock + ")",
                    "Yeni Teslimat Ekle", JOptionPane.QUESTION_MESSAGE);

                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int amount = Integer.parseInt(input.trim());
                        if (amount <= 0) throw new NumberFormatException();

                        updateStockAndRefresh(modelRow, currentStock + amount);

                        JOptionPane.showMessageDialog(this,
                            amount + " kutu stoğa başarıyla eklendi.\nYeni Stok: " + (currentStock + amount),
                            "Tedarik Başarılı", JOptionPane.INFORMATION_MESSAGE);

                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this,
                            "Lütfen eklenecek miktarı pozitif bir tamsayı olarak giriniz.",
                            "Giriş Hatası", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lütfen teslimat / stok girişi yapmak için bir ilaç seçin.", "Tedarik Girişi", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnSearch.addActionListener(e -> performSearch());
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            refreshData();
        });

        // Enter ile arama (Hafta 13: KeyListener)
        txtSearch.addActionListener(e -> performSearch());

        // Gerçek zamanlı filtreleme (DocumentListener)
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { performSearch(); }
            public void removeUpdate(DocumentEvent e) { performSearch(); }
            public void changedUpdate(DocumentEvent e) { performSearch(); }
        });
    }

    // ══════════════════════════════════════════
    //  Yardımcı Metotlar
    // ══════════════════════════════════════════

    private void openAddDialog() {
        MedicineDialog dialog = new MedicineDialog(this, null);
        dialog.setVisible(true);
        refreshData();
    }

    /**
     * İstatistik kartı oluşturur — Rounded border + renk kodlu.
     */
    private JPanel createStatCard(String label, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CLR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 2, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(CLR_SUBTEXT);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());

        return card;
    }

    /**
     * Stilize edilmiş buton oluşturur.
     */
    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        Font bf = UIManager.getFont("Button.font");
        btn.setFont(bf != null ? bf.deriveFont(Font.BOLD, 12f) : btn.getFont());
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        UiUtils.fixButtonTextLayout(btn);
        return btn;
    }

    /**
     * Tablo hücre renderer.
     *
     * Kurallar:
     * - Kritik stok: kırmızı
     * - SKT geçmiş: sarı/turuncu
     * - İkisi birden: özel koyu vurgu
     * - Sorun yoksa: arka plan rengi (zebra yok)
     */
    private DefaultTableCellRenderer createTableRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    int modelRow = table.convertRowIndexToModel(row);
                    int stock = (int) tableModel.getValueAt(modelRow, 3);
                    int crit = (int) tableModel.getValueAt(modelRow, 5);
                    String skt = (String) tableModel.getValueAt(modelRow, 6);

                    boolean isExpired = false;
                    try {
                        isExpired = LocalDate.now().isAfter(LocalDate.parse(skt, Medicine.DATE_FORMATTER));
                    } catch (Exception ignored) {}

                    boolean isLowStock = stock <= crit;

                    // Öncelik: ikisi birlikte -> düşük stok -> SKT geçmiş -> normal
                    if (isLowStock && isExpired) {
                        c.setBackground(CLR_ROW_ALT);
                        c.setForeground(CLR_TEXT);
                    } else if (isLowStock) {
                        c.setBackground(CLR_ROW_DANGER);
                        c.setForeground(CLR_RED);
                    } else if (isExpired) {
                        c.setBackground(CLR_ROW_WARNING);
                        c.setForeground(CLR_ORANGE);
                    } else {
                        // Sorun yoksa: zebra yok, tablonun arka planı
                        c.setBackground(table.getBackground());
                        c.setForeground(CLR_TEXT);
                    }
                }

                // Ortalama hizalama (sayısal sütunlar)
                if (column == 3 || column == 4 || column == 5) {
                    setHorizontalAlignment(JLabel.CENTER);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }

                return c;
            }
        };
    }

    private static LocalDate parseExpiryDateSafe(Object value) {
        if (value == null) return LocalDate.MAX; // boş/bozuk tarihler en sona gitsin
        String s = String.valueOf(value).trim();
        if (s.isEmpty()) return LocalDate.MAX;
        try {
            return LocalDate.parse(s, Medicine.DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            return LocalDate.MAX;
        }
    }

    // ══════════════════════════════════════════
    //  Veri Yönetimi
    // ══════════════════════════════════════════

    /**
     * Hızlı stok güncellemesi yapar (-satış / +tedarik).
     */
    private void updateStockAndRefresh(int modelRow, int newStock) {
        int id = (int) tableModel.getValueAt(modelRow, 0);
        Medicine med = db.getAllMedicines().stream()
                .filter(m -> m.getId() == id).findFirst().orElse(null);
        if (med != null) {
            med.setStockCount(newStock);
            db.updateMedicine(med);
            refreshData(); // Her şeyi (grafikler, tablo, istatistik, alarmlar) baştan hesaplar
        }
    }

    /**
     * Tüm verileri veritabanından çekip arayüzü günceller.
     */
    private void refreshData() {
        List<Medicine> medicines = db.getAllMedicines();
        updateTableData(medicines);
        chartPanel.updateData(medicines);
        statsPanel.updateData(medicines, db);
        updateStatCards(medicines);

        // Multimedya alarm kontrolü (Hafta 14)
        long lowCount = medicines.stream().filter(Medicine::isLowStock).count();
        if (lowCount > 0 && !isInitialAlertPlayed) {
            SoundAlert.playAlertSound();
            isInitialAlertPlayed = true;
        } else if (lowCount == 0) {
            isInitialAlertPlayed = false;
        }
    }

    /**
     * Arama kutusundaki metne göre veritabanından filtreleme.
     */
    private void performSearch() {
        String query = txtSearch.getText().trim();
        List<Medicine> medicines;
        if (query.isEmpty()) {
            medicines = db.getAllMedicines();
        } else {
            medicines = db.searchMedicines(query);
        }
        updateTableData(medicines);
        chartPanel.updateData(medicines);
    }

    /**
     * Tablo verilerini günceller.
     */
    private void updateTableData(List<Medicine> medicines) {
        tableModel.setRowCount(0);
        for (Medicine m : medicines) {
            tableModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getCategory(), m.getStockCount(),
                    m.getUnitPrice(), m.getCriticalLevel(), m.getExpiryDate()
            });
        }
    }

    /**
     * Üst kısımdaki istatistik kartlarını günceller.
     */
    private void updateStatCards(List<Medicine> medicines) {
        int totalCount = medicines.size();
        long lowCount = medicines.stream().filter(Medicine::isLowStock).count();
        long expiredCount = medicines.stream().filter(Medicine::isExpired).count();
        double totalValue = medicines.stream().mapToDouble(Medicine::getTotalValue).sum();

        lblTotalCount.setText(String.valueOf(totalCount));
        lblLowStockCount.setText(String.valueOf(lowCount));
        lblExpiredCount.setText(String.valueOf(expiredCount));
        lblTotalValue.setText(String.format("₺%,.0f", totalValue));
    }
}
