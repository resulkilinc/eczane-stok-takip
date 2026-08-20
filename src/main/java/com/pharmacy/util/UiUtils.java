package com.pharmacy.util;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

/**
 * macOS + FlatLaf ortamında JButton metinlerinin eksik/boşluklu çizilmesine karşı
 * genişlik ve istemci özellikleri ayarı.
 */
public final class UiUtils {

    private UiUtils() {
    }

    /**
     * FlatLaf varsayılan minimum genişlik + hatalı tercih edilen boyut birleşiminde etiket kırpılabiliyor.
     * Metin genişliğini JLabel ile ölçüp butona açıkça yansıtır.
     */
    public static void fixButtonTextLayout(AbstractButton b) {
        if (b == null) {
            return;
        }
        b.putClientProperty(FlatClientProperties.MINIMUM_WIDTH, 0);
        String t = b.getText();
        if (t == null || t.isEmpty()) {
            return;
        }

        JLabel probe = new JLabel(t);
        probe.setFont(b.getFont());
        Dimension textDim = probe.getPreferredSize();

        Insets m = b.getMargin();
        int mx = m != null ? m.left + m.right : 0;
        int my = m != null ? m.top + m.bottom : 0;

        int flatPad = 36;
        int w = textDim.width + mx + flatPad;
        int h = Math.max(textDim.height + my + 18, b.getPreferredSize().height);

        Dimension d = new Dimension(w, h);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
    }
}
