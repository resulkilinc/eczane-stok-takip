package com.pharmacy.ui;

import com.pharmacy.db.DatabaseManager;
import com.pharmacy.model.Medicine;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * İstatistik Paneli — JTabbedPane'in 3. sekmesi.
 * Envanter özeti, kategori dağılımı ve en pahalı ilaçlar.
 * JPanel + JLabel kompozisyonu ile oluşturulmuştur.
 */
public class StatisticsPanel extends JPanel {

    private static final Color CLR_SURFACE = new Color(0x2D, 0x2D, 0x44);
    private static final Color CLR_TEXT    = new Color(0xCD, 0xD6, 0xF4);
    private static final Color CLR_SUBTEXT = new Color(0xA6, 0xAD, 0xC8);
    private static final Color CLR_BLUE    = new Color(0x89, 0xB4, 0xFA);
    private static final Color CLR_GREEN   = new Color(0xA6, 0xE3, 0xA1);
    private static final Color CLR_RED     = new Color(0xF3, 0x8B, 0xA8);
    private static final Color CLR_ORANGE  = new Color(0xFA, 0xB3, 0x87);
    private static final Color CLR_BAR_BG  = new Color(0x40, 0x40, 0x58);

    private JPanel contentPanel;

    /**
     * macOS + bazı OpenJDK sürümlerinde JLabel metni "harfler ayrık" görünebiliyor.
     * Bunun etrafından dolaşmak için metni doğrudan GlyphVector ile çiziyoruz.
     */
    private static class StableTextLabel extends JLabel {
        StableTextLabel(String text) {
            super(text);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            // Arka plan/border gerekiyorsa yine JLabel çizsin
            if (isOpaque() || getBorder() != null) {
                super.paintComponent(g);
            } else {
                // background çizmeden sadece child çizimi için alan temizliği yapma
                // (JLabel zaten varsayılan olarak arka plan çizmez)
            }

            String text = getText();
            if (text == null || text.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setFont(getFont());
                g2.setColor(getForeground());
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

                Insets in = getInsets();
                int w = getWidth() - in.left - in.right;
                int h = getHeight() - in.top - in.bottom;

                FontRenderContext frc = g2.getFontRenderContext();
                GlyphVector gv = getFont().createGlyphVector(frc, text);
                Rectangle2D vb = gv.getVisualBounds();

                int ascent = g2.getFontMetrics().getAscent();
                int descent = g2.getFontMetrics().getDescent();
                int baseline = in.top + (h - (ascent + descent)) / 2 + ascent;

                double textW = vb.getWidth();
                int ha = getHorizontalAlignment();
                double x;
                if (ha == CENTER) {
                    x = in.left + (w - textW) / 2.0 - vb.getX();
                } else if (ha == RIGHT || ha == TRAILING) {
                    x = in.left + (w - textW) - vb.getX();
                } else {
                    x = in.left - vb.getX();
                }

                g2.drawGlyphVector(gv, (float) x, (float) baseline);
            } finally {
                g2.dispose();
            }
        }
    }

    public StatisticsPanel() {
        setLayout(new BorderLayout());
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * İstatistik verilerini günceller.
     */
    public void updateData(List<Medicine> medicines, DatabaseManager db) {
        contentPanel.removeAll();

        if (medicines == null || medicines.isEmpty()) {
            JLabel lbl = new JLabel("Gösterilecek veri yok.", JLabel.CENTER);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
            lbl.setForeground(CLR_SUBTEXT);
            contentPanel.add(lbl);
            revalidate();
            repaint();
            return;
        }

        // ── Başlık ──
        addTitle("Envanter İstatistikleri");
        contentPanel.add(Box.createVerticalStrut(15));

        // ══ GENEL ÖZET ══
        addSectionHeader("Genel Özet");

        int totalTypes = medicines.size();
        int totalStock = medicines.stream().mapToInt(Medicine::getStockCount).sum();
        double totalValue = medicines.stream().mapToDouble(Medicine::getTotalValue).sum();
        long criticalCount = medicines.stream().filter(Medicine::isLowStock).count();
        long expiredCount = medicines.stream().filter(Medicine::isExpired).count();
        long expiringSoon = medicines.stream().filter(Medicine::isExpiringSoon).count();

        addStatRow("Toplam İlaç Çeşidi", String.valueOf(totalTypes), CLR_BLUE);
        addStatRow("Toplam Stok Adedi", String.valueOf(totalStock), CLR_BLUE);
        addStatRow("Toplam Envanter Değeri", String.format("₺%,.2f", totalValue), CLR_GREEN);
        addStatRow("Kritik Stoklu İlaç", String.valueOf(criticalCount), criticalCount > 0 ? CLR_RED : CLR_GREEN);
        addStatRow("SKT Geçen İlaç", String.valueOf(expiredCount), expiredCount > 0 ? CLR_RED : CLR_GREEN);
        addStatRow("30 Gün İçinde SKT Dolacak", String.valueOf(expiringSoon), expiringSoon > 0 ? CLR_ORANGE : CLR_GREEN);

        contentPanel.add(Box.createVerticalStrut(20));

        // ══ KATEGORİ DAĞILIMI ══
        addSectionHeader("Kategori Dağılımı");

        Map<String, Integer> categoryCount = new LinkedHashMap<>();
        Map<String, Integer> categoryStock = new LinkedHashMap<>();
        for (Medicine m : medicines) {
            String cat = m.getCategory() != null ? m.getCategory() : "Diğer";
            categoryCount.merge(cat, 1, Integer::sum);
            categoryStock.merge(cat, m.getStockCount(), Integer::sum);
        }

        int maxCatStock = categoryStock.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        Color[] catColors = {CLR_BLUE, CLR_RED, CLR_GREEN, CLR_ORANGE, new Color(0xCB, 0xA6, 0xF7), new Color(0x94, 0xE2, 0xD5)};
        int ci = 0;

        for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
            String cat = entry.getKey();
            int count = entry.getValue();
            int stock = categoryStock.getOrDefault(cat, 0);
            double pct = (count * 100.0) / totalTypes;

            addCategoryBar(cat, count, stock, pct, maxCatStock, catColors[ci % catColors.length]);
            ci++;
        }

        contentPanel.add(Box.createVerticalStrut(20));

        // ══ EN PAHALI İLAÇLAR ══
        addSectionHeader("Fiyatı En Yüksek 3 İlaç");

        medicines.stream()
                .sorted((a, b) -> Double.compare(b.getUnitPrice(), a.getUnitPrice()))
                .limit(3)
                .forEach(m -> addStatRow(m.getName(), m.getFormattedPrice(), CLR_ORANGE));

        contentPanel.add(Box.createVerticalStrut(15));

        // ══ KRİTİK STOK LİSTESİ ══
        if (criticalCount > 0) {
            addSectionHeader("Kritik Stoklu İlaçlar");
            medicines.stream()
                    .filter(Medicine::isLowStock)
                    .forEach(m -> addStatRow(
                            m.getName(),
                            "Stok: " + m.getStockCount() + " / Kritik: " + m.getCriticalLevel(),
                            CLR_RED));
        }

        contentPanel.add(Box.createVerticalStrut(15));

        // ══ SKT GEÇEN İLAÇLAR ══
        if (expiredCount > 0) {
            addSectionHeader("Son Kullanma Tarihi Geçen İlaçlar");
            medicines.stream()
                    .filter(Medicine::isExpired)
                    .forEach(m -> addStatRow(
                            m.getName(),
                            "SKT: " + m.getExpiryDate(),
                            CLR_RED));
        }

        revalidate();
        repaint();
    }

    // ── UI Yardımcı Metotlar ──

    private void addTitle(String text) {
        JLabel lbl = new StableTextLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        lbl.setForeground(CLR_BLUE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lbl);
    }

    private void addSectionHeader(String text) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new StableTextLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(CLR_SUBTEXT);

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(CLR_BAR_BG);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        header.add(lbl);
        header.add(Box.createHorizontalStrut(10));
        header.add(sep);

        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(header);
        contentPanel.add(Box.createVerticalStrut(6));
    }

    private void addStatRow(String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        JLabel lblName = new StableTextLabel(label);
        lblName.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblName.setForeground(CLR_TEXT);

        JLabel lblValue = new StableTextLabel(value);
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        lblValue.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblValue.setForeground(valueColor);

        row.add(lblName, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.EAST);
        contentPanel.add(row);
    }

    private void addCategoryBar(String category, int count, int stock, double pct, int maxStock, Color color) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        // Üst satır: Kategori adı ve yüzde
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel lblCat = new StableTextLabel(category);
        lblCat.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblCat.setForeground(CLR_TEXT);
        JLabel lblPct = new StableTextLabel(String.format("%d çeşit • %d adet (%%%.1f)", count, stock, pct));
        lblPct.setHorizontalAlignment(SwingConstants.RIGHT);
        lblPct.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblPct.setForeground(CLR_SUBTEXT);
        topRow.add(lblCat, BorderLayout.WEST);
        topRow.add(lblPct, BorderLayout.EAST);
        row.add(topRow);

        // Progress bar (özel çizim)
        JPanel barPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Arka plan
                g2.setColor(CLR_BAR_BG);
                g2.fillRoundRect(0, 0, w, h, 6, 6);

                // Dolu kısım
                int fillW = (int) ((double) stock / maxStock * w);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, fillW, h, 6, 6);
            }
        };
        barPanel.setPreferredSize(new Dimension(0, 10));
        barPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        row.add(barPanel);

        contentPanel.add(row);
    }
}
