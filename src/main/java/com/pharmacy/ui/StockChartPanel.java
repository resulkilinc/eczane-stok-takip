package com.pharmacy.ui;

import com.pharmacy.model.Medicine;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Grafik Çizim Paneli — Graphics2D ile sıfırdan çizim (Hafta 12).
 * Hiçbir dış grafik kütüphanesi KULLANILMAMIŞTIR.
 * Sütun grafiği + Pasta grafik + Legend + Grid çizgileri.
 */
public class StockChartPanel extends JPanel {
    private List<Medicine> medicines;

    // Renk paleti
    private static final Color CLR_BG        = new Color(0x1E, 0x1E, 0x2E);
    private static final Color CLR_GRID      = new Color(0x40, 0x40, 0x58);
    private static final Color CLR_TEXT       = new Color(0xCD, 0xD6, 0xF4);
    private static final Color CLR_SUBTEXT   = new Color(0xA6, 0xAD, 0xC8);
    private static final Color CLR_BLUE_TOP  = new Color(0x89, 0xB4, 0xFA);
    private static final Color CLR_BLUE_BOT  = new Color(0x45, 0x6C, 0xB5);
    private static final Color CLR_RED_TOP   = new Color(0xF3, 0x8B, 0xA8);
    private static final Color CLR_RED_BOT   = new Color(0xA0, 0x3A, 0x55);
    private static final Color CLR_GREEN     = new Color(0xA6, 0xE3, 0xA1);
    private static final Color CLR_ORANGE    = new Color(0xFA, 0xB3, 0x87);
    private static final Color CLR_YELLOW    = new Color(0xF9, 0xE2, 0xAF);
    private static final Color CLR_MAUVE     = new Color(0xCB, 0xA6, 0xF7);
    private static final Color CLR_TEAL      = new Color(0x94, 0xE2, 0xD5);
    private static final Color CLR_PINK      = new Color(0xF5, 0xC2, 0xE7);

    // Pasta grafik renkleri
    private static final Color[] PIE_COLORS = {
            CLR_BLUE_TOP, CLR_RED_TOP, CLR_GREEN, CLR_ORANGE,
            CLR_YELLOW, CLR_MAUVE, CLR_TEAL, CLR_PINK
    };

    public void updateData(List<Medicine> medicines) {
        this.medicines = medicines;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // LCD AA bazı macOS + Retina yapılandırmalarında özel çizim metninde boşluklu/kayıp glif üretebiliyor.
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (medicines == null || medicines.isEmpty()) {
            g2d.setColor(CLR_SUBTEXT);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2d.drawString("Gösterilecek veri yok.", width / 2 - 80, height / 2);
            return;
        }

        // Üst yarı: Sütun Grafiği, Alt yarı: Pasta Grafik
        int barChartHeight = (int)(height * 0.55);
        drawBarChart(g2d, width, barChartHeight);
        drawPieChart(g2d, width, barChartHeight, height);
    }

    // ══════════════════════════════════════════
    //  SÜTUN GRAFİĞİ (Bar Chart)
    // ══════════════════════════════════════════

    private void drawBarChart(Graphics2D g2d, int width, int chartHeight) {
        int padding = 60;
        int topPad = 45;

        // ── Başlık ──
        g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2d.setColor(CLR_TEXT);
        String title = "📊 İlaç Stok Dağılım Grafiği";
        FontMetrics fmTitle = g2d.getFontMetrics();
        g2d.drawString(title, (width - fmTitle.stringWidth(title)) / 2, 25);

        // Tarih bilgisi
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2d.setColor(CLR_SUBTEXT);
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        g2d.drawString("Tarih: " + dateStr, width - 130, 20);

        int chartW = width - 2 * padding;
        int chartH = chartHeight - topPad - 30;

        // Limit bar count
        int maxBars = 12;
        int displayCount = Math.min(medicines.size(), maxBars);
        int barWidth = Math.max(20, chartW / displayCount - 16);

        // Max stok
        int maxStock = medicines.stream()
                .limit(displayCount)
                .mapToInt(Medicine::getStockCount)
                .max().orElse(1);
        if (maxStock == 0) maxStock = 1;

        // ── Grid Çizgileri (kesikli) ──
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{4, 4}, 0));
        g2d.setColor(CLR_GRID);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int gridY = topPad + chartH - (i * chartH / gridLines);
            g2d.drawLine(padding, gridY, width - padding, gridY);
            int gridVal = maxStock * i / gridLines;
            g2d.setColor(CLR_SUBTEXT);
            g2d.drawString(String.valueOf(gridVal), padding - 30, gridY + 4);
            g2d.setColor(CLR_GRID);
        }
        g2d.setStroke(new BasicStroke(1));

        // ── Eksenler ──
        g2d.setColor(CLR_SUBTEXT);
        g2d.drawLine(padding, topPad, padding, topPad + chartH);
        g2d.drawLine(padding, topPad + chartH, width - padding, topPad + chartH);

        // Y ekseni etiketi
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2d.setColor(CLR_SUBTEXT);

        // ── Bar'ları Çiz ──
        for (int i = 0; i < displayCount; i++) {
            Medicine m = medicines.get(i);
            int x = padding + i * (barWidth + 14) + 10;
            int barHeight = (int) (((double) m.getStockCount() / maxStock) * chartH);
            if (barHeight < 2 && m.getStockCount() > 0) barHeight = 2;
            int y = topPad + chartH - barHeight;

            // Gradient fill (RoundRectangle)
            Color topColor, botColor;
            if (m.isLowStock()) {
                topColor = CLR_RED_TOP;
                botColor = CLR_RED_BOT;
            } else {
                topColor = CLR_BLUE_TOP;
                botColor = CLR_BLUE_BOT;
            }
            GradientPaint gp = new GradientPaint(x, y, topColor, x, y + barHeight, botColor);
            g2d.setPaint(gp);
            g2d.fill(new RoundRectangle2D.Float(x, y, barWidth, barHeight, 6, 6));

            // Bar outline
            g2d.setColor(topColor.darker());
            g2d.draw(new RoundRectangle2D.Float(x, y, barWidth, barHeight, 6, 6));

            // Stok değeri (bar üstü)
            g2d.setColor(CLR_TEXT);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics fm = g2d.getFontMetrics();
            String valStr = String.valueOf(m.getStockCount());
            g2d.drawString(valStr, x + (barWidth - fm.stringWidth(valStr)) / 2, y - 5);

            // İlaç adı (bar altı, döndürme yok, kısaltılmış)
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 9));
            fm = g2d.getFontMetrics();
            String name = m.getName();
            if (name.length() > 10) name = name.substring(0, 9) + "…";
            int nameW = fm.stringWidth(name);
            g2d.setColor(CLR_SUBTEXT);
            g2d.drawString(name, x + (barWidth - nameW) / 2, topPad + chartH + 15);
        }

        // ── Legend ──
        int legendX = width - 170;
        int legendY = topPad + 5;
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));

        // Normal
        g2d.setColor(CLR_BLUE_TOP);
        g2d.fillRoundRect(legendX, legendY, 12, 12, 3, 3);
        g2d.setColor(CLR_TEXT);
        g2d.drawString("Normal Stok", legendX + 18, legendY + 11);

        // Kritik
        g2d.setColor(CLR_RED_TOP);
        g2d.fillRoundRect(legendX, legendY + 20, 12, 12, 3, 3);
        g2d.setColor(CLR_TEXT);
        g2d.drawString("Kritik Stok", legendX + 18, legendY + 31);
    }

    // ══════════════════════════════════════════
    //  PASTA GRAFİĞİ (Pie Chart) — Kategori Dağılımı
    // ══════════════════════════════════════════

    private void drawPieChart(Graphics2D g2d, int width, int startY, int totalHeight) {
        int sectionHeight = totalHeight - startY;
        if (sectionHeight < 100) return;

        // Başlık
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.setColor(CLR_TEXT);
        String pieTitle = "🥧 Kategori Dağılımı";
        FontMetrics fmT = g2d.getFontMetrics();
        g2d.drawString(pieTitle, (width / 2 - fmT.stringWidth(pieTitle)) / 2 + 60, startY + 25);

        // Kategori verisi toplama
        Map<String, Integer> categoryMap = new LinkedHashMap<>();
        for (Medicine m : medicines) {
            String cat = m.getCategory() != null ? m.getCategory() : "Diğer";
            categoryMap.merge(cat, m.getStockCount(), Integer::sum);
        }

        int totalStock = categoryMap.values().stream().mapToInt(Integer::intValue).sum();
        if (totalStock == 0) return;

        // Pasta çizimi
        int diameter = Math.min(sectionHeight - 50, 180);
        int pieX = width / 4 - diameter / 2;
        int pieY = startY + 35;

        double startAngle = 90;
        int colorIdx = 0;
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(categoryMap.entrySet());

        for (Map.Entry<String, Integer> entry : entries) {
            double arcAngle = (entry.getValue() * 360.0) / totalStock;

            g2d.setColor(PIE_COLORS[colorIdx % PIE_COLORS.length]);
            g2d.fill(new Arc2D.Double(pieX, pieY, diameter, diameter, startAngle, arcAngle, Arc2D.PIE));

            // Dilim kenarlığı
            g2d.setColor(new Color(0x1E, 0x1E, 0x2E));
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(new Arc2D.Double(pieX, pieY, diameter, diameter, startAngle, arcAngle, Arc2D.PIE));
            g2d.setStroke(new BasicStroke(1));

            startAngle += arcAngle;
            colorIdx++;
        }

        // Pasta Legend (sağ tarafta)
        int legendX = width / 2 + 30;
        int legendY = startY + 50;
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        colorIdx = 0;

        for (Map.Entry<String, Integer> entry : entries) {
            double pct = (entry.getValue() * 100.0) / totalStock;

            g2d.setColor(PIE_COLORS[colorIdx % PIE_COLORS.length]);
            g2d.fillRoundRect(legendX, legendY, 14, 14, 3, 3);

            g2d.setColor(CLR_TEXT);
            String legendText = String.format("%s — %d adet (%%%.1f)", entry.getKey(), entry.getValue(), pct);
            g2d.drawString(legendText, legendX + 22, legendY + 12);

            legendY += 24;
            colorIdx++;
        }
    }
}
