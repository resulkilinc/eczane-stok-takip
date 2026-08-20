package com.pharmacy.ui;

import com.pharmacy.db.DatabaseManager;
import com.pharmacy.model.Medicine;
import com.pharmacy.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * İlaç ekleme/düzenleme formu — JDialog (Hafta 7), GridBagLayout (Hafta 5),
 * InputVerifier validasyon (Hafta 10), JComboBox (Hafta 11).
 */
public class MedicineDialog extends JDialog {
    private JTextField txtName, txtStock, txtPrice, txtCrit, txtExpiry;
    private JComboBox<String> cmbCategory;
    private DatabaseManager db;
    private Medicine existingMedicine;
    private boolean isEdit;

    public MedicineDialog(Frame owner, Medicine medicine) {
        super(owner, medicine != null ? "İlaç Düzenle" : "Yeni İlaç Ekle", true);
        this.db = new DatabaseManager();
        this.existingMedicine = medicine;
        this.isEdit = (medicine != null);

        initUI();
        if (isEdit) populateFields();
    }

    private void initUI() {
        setSize(450, 480);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        // ── Başlık ──
        JLabel lblTitle = new JLabel(isEdit ? "✏ İlaç Bilgilerini Güncelle" : "➕ Yeni İlaç Kaydı", JLabel.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ── Form Alanları (GridBagLayout — Hafta 5) ──
        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // İlaç Adı
        txtName = addTextField(pnlCenter, "İlaç Adı:", 0, gbc);

        // Kategori — JComboBox (editable) (Hafta 11: İleri Bileşenler)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        pnlCenter.add(new JLabel("Kategori:"), gbc);

        String[] defaultCategories = {"Ağrı Kesici", "Antibiyotik", "Takviye", "Mide/Sindirim", "Alerji", "Cilt", "Diğer"};
        cmbCategory = new JComboBox<>(defaultCategories);
        cmbCategory.setEditable(true); // Kullanıcı yeni kategori de yazabilir
        cmbCategory.setFont(new Font("SansSerif", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlCenter.add(cmbCategory, gbc);

        // Stok Miktarı
        txtStock = addTextField(pnlCenter, "Mevcut Stok:", 2, gbc);

        // Birim Fiyat
        txtPrice = addTextField(pnlCenter, "Birim Fiyat (₺):", 3, gbc);

        // Kritik Seviye
        txtCrit = addTextField(pnlCenter, "Kritik Seviye:", 4, gbc);

        // Son Kullanma Tarihi + Validasyon
        txtExpiry = addTextField(pnlCenter, "Son Kullanma (gg.aa.yyyy):", 5, gbc);

        // Tarih validasyonu — InputVerifier (Hafta 10)
        txtExpiry.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = ((JTextField) input).getText().trim();
                if (text.isEmpty()) return true;
                try {
                    LocalDate.parse(text, Medicine.DATE_FORMATTER);
                    input.setBackground(UIManager.getColor("TextField.background"));
                    return true;
                } catch (DateTimeParseException e) {
                    input.setBackground(new Color(80, 30, 30));
                    return false;
                }
            }
        });

        add(pnlCenter, BorderLayout.CENTER);

        // ── Butonlar ──
        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        // Emoji + JButton bazı LnF kombinasyonlarında metni parçalı/kırpık gösteriyor; düz metin kullan.
        JButton btnSave = new JButton(isEdit ? "Güncelle" : "Kaydet");
        Font bf = UIManager.getFont("Button.font");
        btnSave.setFont(bf != null ? bf.deriveFont(Font.BOLD) : btnSave.getFont());
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setMargin(new Insets(8, 16, 8, 16));

        JButton btnCancel = new JButton("İptal");
        btnCancel.setFont(bf != null ? bf : btnCancel.getFont());
        btnCancel.setMargin(new Insets(8, 16, 8, 16));

        pnlSouth.add(btnSave);
        pnlSouth.add(btnCancel);
        add(pnlSouth, BorderLayout.SOUTH);

        // Event Listeners (Hafta 13)
        btnSave.addActionListener(e -> save());
        btnCancel.addActionListener(e -> dispose());

        // Enter ile kaydet
        getRootPane().setDefaultButton(btnSave);

        SwingUtilities.invokeLater(() -> {
            UiUtils.fixButtonTextLayout(btnSave);
            UiUtils.fixButtonTextLayout(btnCancel);
        });
    }

    private JTextField addTextField(JPanel pnl, String label, int row, GridBagConstraints gbc) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pnl.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField txt = new JTextField();
        txt.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pnl.add(txt, gbc);
        return txt;
    }

    private void populateFields() {
        txtName.setText(existingMedicine.getName());
        cmbCategory.setSelectedItem(existingMedicine.getCategory());
        txtStock.setText(String.valueOf(existingMedicine.getStockCount()));
        txtPrice.setText(String.valueOf(existingMedicine.getUnitPrice()));
        txtCrit.setText(String.valueOf(existingMedicine.getCriticalLevel()));
        txtExpiry.setText(existingMedicine.getExpiryDate());
    }

    private void save() {
        try {
            String name = txtName.getText().trim();
            String cat = (cmbCategory.getSelectedItem() != null)
                    ? cmbCategory.getSelectedItem().toString().trim() : "";
            String stockStr = txtStock.getText().trim();
            String priceStr = txtPrice.getText().trim();
            String critStr = txtCrit.getText().trim();
            String expiry = txtExpiry.getText().trim();

            // Validasyonlar (Hafta 10: JOptionPane)
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "İlaç adı boş olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
                txtName.requestFocus();
                return;
            }

            int stock = Integer.parseInt(stockStr);
            double price = Double.parseDouble(priceStr);
            int crit = Integer.parseInt(critStr);

            if (stock < 0 || price < 0 || crit < 0) {
                JOptionPane.showMessageDialog(this,
                        "Stok, fiyat ve kritik seviye negatif olamaz!",
                        "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Tarih formatı kontrolü
            if (!expiry.isEmpty()) {
                try {
                    LocalDate.parse(expiry, Medicine.DATE_FORMATTER);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Tarih formatı hatalı! Doğru format: gg.aa.yyyy (Örn: 31.12.2026)",
                            "Hata", JOptionPane.ERROR_MESSAGE);
                    txtExpiry.requestFocus();
                    return;
                }
            }

            Medicine m = new Medicine(
                    isEdit ? existingMedicine.getId() : 0,
                    name, cat, stock, price, crit, expiry
            );

            if (isEdit) {
                db.updateMedicine(m);
            } else {
                db.addMedicine(m);
            }

            JOptionPane.showMessageDialog(this,
                    isEdit ? "İlaç bilgileri güncellendi." : "Yeni ilaç başarıyla kaydedildi.",
                    "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen sayısal alanları doğru doldurunuz.\n(Stok: tamsayı, Fiyat: ondalıklı, Kritik: tamsayı)",
                    "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}
