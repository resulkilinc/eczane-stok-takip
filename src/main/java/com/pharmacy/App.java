package com.pharmacy;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.pharmacy.ui.Dashboard;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * Eczane Stok ve İlaç Takip Sistemi
 * Giriş noktası — Tema yönetimi ve uygulama başlatma.
 * 
 * @author Resul Kılınç — 22110131066
 * KSÜ Görsel Programlama Dersi Projesi
 */
public class App {

    public static void main(String[] args) {
        // macOS + Retina: LCD subpixel AA koyu temada JButton gliflerini “kaybedebiliyor”.
        // Gri tonlamalı AA genelde daha stabil.
        // "gasp" (grayscale AA) macOS'ta bazı glif/harf bozulmalarını engeller.
        System.setProperty("awt.useSystemAAFontSettings", "gasp");
        System.setProperty("swing.aatext", "true");

        // Konsol Banner — Profesyonel başlangıç
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  Eczane Stok ve İlaç Takip Sistemi v1.0 ║");
        System.out.println("║  KSÜ Görsel Programlama Projesi         ║");
        System.out.println("║  Geliştirici: Resul Kılınç              ║");
        System.out.println("╚══════════════════════════════════════════╝");

        // Dark tema varsayılan olarak ayarla (Hafta 1-5: Swing Temelleri)
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // Düz Font("SansSerif") global default; FlatLaf türev fontlarını bozup buton ölçümünü şaşırtabiliyor.
            applyFlatLafFriendlyDefaultFont(14f);

            System.out.println("[✓] FlatLaf Dark tema başarıyla yüklendi.");
        } catch (Exception ex) {
            System.err.println("[✗] Tema yüklenemedi, varsayılan kullanılıyor: " + ex.getMessage());
        }

        // Event Dispatch Thread üzerinde çalıştır (Swing güvenlik kuralı)
        SwingUtilities.invokeLater(() -> {
            Dashboard dashboard = new Dashboard();
            dashboard.setVisible(true);
            System.out.println("[✓] Dashboard başarıyla açıldı.");
        });
    }

    /**
     * Çalışma zamanında tema değiştirme (Menüden çağrılacak).
     * @param useDark true ise koyu tema, false ise açık tema
     */
    public static void switchTheme(boolean useDark) {
        try {
            if (useDark) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
            applyFlatLafFriendlyDefaultFont(14f);
            // Tüm açık pencereleri güncelle
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
            System.out.println("[✓] Tema değiştirildi: " + (useDark ? "Koyu" : "Açık"));
        } catch (Exception ex) {
            System.err.println("[✗] Tema değiştirme hatası: " + ex.getMessage());
        }
    }

    private static void applyFlatLafFriendlyDefaultFont(float size) {
        // macOS'ta OpenJDK'nin sistem fontu (San Francisco / .AppleSystemUIFont) ile
        // harf aralığı (tracking) bug'ı var (bkz. JDK-8338429). Güvenilir workaround:
        // Font'u negatif TRACKING ile derive etmek veya farklı bir font kullanmak.
        int sz = Math.round(size);
        Font stable = buildStableUIFont(sz);
        FontUIResource uiFont = new FontUIResource(stable);
        UIManager.put("defaultFont", uiFont);

        // Kritik bileşenlerin fontlarını da sabitle (FlatLaf çoğunu defaultFont'tan alır,
        // ama bazıları override edebiliyor).
        UIManager.put("Label.font", uiFont);
        UIManager.put("Button.font", uiFont);
        UIManager.put("Menu.font", uiFont);
        UIManager.put("MenuItem.font", uiFont);
        UIManager.put("Table.font", uiFont);
        UIManager.put("TableHeader.font", uiFont);
        UIManager.put("TextField.font", uiFont);
        UIManager.put("ComboBox.font", uiFont);
    }

    private static Font buildStableUIFont(int size) {
        boolean isMac = System.getProperty("os.name", "").toLowerCase().contains("mac");
        if (!isMac) {
            return new Font("Dialog", Font.PLAIN, size);
        }

        // macOS'ta sistem fontu (.AppleSystemUIFont / San Francisco) OpenJDK'de
        // bazı ortamlarda "harfler ayrık" (tracking) bug'ı üretebiliyor.
        // En stabil çözüm: sistem fontunu UI'dan çıkarıp Helvetica/Arial kullanmak
        // ve typographic özellikleri (kerning/ligatures/tracking) kapatmak.
        Font base = new Font("Helvetica Neue", Font.PLAIN, size);
        if (!base.getFamily().toLowerCase().contains("helvetica")) {
            base = new Font("Arial", Font.PLAIN, size);
        }

        Map<TextAttribute, Object> attrs = new HashMap<>();
        // Harf aralığı / kerning / ligature gibi özellikleri kapat.
        attrs.put(TextAttribute.TRACKING, 0f);
        attrs.put(TextAttribute.KERNING, 0);
        attrs.put(TextAttribute.LIGATURES, 0);
        return base.deriveFont(attrs);
    }
}
