package com.pharmacy.ui;

import com.pharmacy.util.UiUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Hakkında penceresi — Özel JDialog (Hafta 7, 10).
 * Proje bilgileri, geliştirici ve kullanılan teknolojiler.
 */
public class AboutDialog extends JDialog {

    public AboutDialog(Frame owner) {
        super(owner, "Hakkında", true);
        initUI();
    }

    private void initUI() {
        setSize(420, 320);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        // ── Ana Panel ──
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setBorder(BorderFactory.createEmptyBorder(25, 30, 20, 30));

        // Uygulama ikonu / başlık
        JLabel lblIcon = new JLabel("💊", JLabel.CENTER);
        lblIcon.setFont(new Font("SansSerif", Font.PLAIN, 48));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMain.add(lblIcon);
        pnlMain.add(Box.createVerticalStrut(10));

        // Uygulama adı
        JLabel lblName = new JLabel("Eczane Stok ve İlaç Takip Sistemi");
        lblName.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMain.add(lblName);
        pnlMain.add(Box.createVerticalStrut(4));

        // Versiyon
        JLabel lblVersion = new JLabel("Sürüm 1.0 — Dijital Envanter Otomasyonu");
        lblVersion.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblVersion.setForeground(new Color(0xA6, 0xAD, 0xC8));
        lblVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMain.add(lblVersion);
        pnlMain.add(Box.createVerticalStrut(20));

        // Ayırıcı çizgi
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        pnlMain.add(sep);
        pnlMain.add(Box.createVerticalStrut(15));

        // Bilgiler
        addInfoLine(pnlMain, "Geliştirici:", "Resul Kılınç");
        addInfoLine(pnlMain, "Tür:", "Masaüstü stok otomasyonu");

        pnlMain.add(Box.createVerticalStrut(12));

        // Teknolojiler
        JLabel lblTech = new JLabel("Java 17 • Swing • SQLite • FlatLaf • Graphics2D");
        lblTech.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblTech.setForeground(new Color(0x89, 0xB4, 0xFA));
        lblTech.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMain.add(lblTech);

        add(pnlMain, BorderLayout.CENTER);

        // ── Tamam Butonu ──
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnOk = new JButton("Tamam");
        Font btnFont = UIManager.getFont("Button.font");
        btnOk.setFont(btnFont != null ? btnFont.deriveFont(Font.BOLD) : btnOk.getFont());
        btnOk.setMargin(new Insets(8, 28, 8, 28));
        btnOk.addActionListener(e -> dispose());
        pnlBottom.add(btnOk);
        add(pnlBottom, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnOk);
        SwingUtilities.invokeLater(() -> UiUtils.fixButtonTextLayout(btnOk));
    }

    private void addInfoLine(JPanel panel, String label, String value) {
        JPanel line = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        line.setOpaque(false);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.PLAIN, 12));

        line.add(lbl);
        line.add(val);
        panel.add(line);
    }
}
