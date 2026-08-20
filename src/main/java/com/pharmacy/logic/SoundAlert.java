package com.pharmacy.logic;

import javax.sound.sampled.*;

/**
 * Ses Alarm Sistemi — Java Audio API ile sinüs dalgası sentezi (Hafta 14: Multimedya).
 * Hiçbir harici .mp3 veya .wav dosyası KULLANILMAMAKTADIR.
 * Alarm, çalışma zamanında matematiksel olarak üretilir.
 */
public class SoundAlert {

    /**
     * 3 kısa bip çalar — artan frekans deseni ile dikkat çekici alarm.
     * Frekanslar: A5 (880Hz) → C6 (1046Hz) → D6 (1175Hz)
     */
    public static void playAlertSound() {
        new Thread(() -> {
            try {
                double[] frequencies = {880.0, 1046.5, 1174.7}; // A5 → C6 → D6
                int[] durations      = {200, 200, 350};          // ms
                int pauseMs          = 80;

                for (int i = 0; i < frequencies.length; i++) {
                    synthesizeBeep(frequencies[i], durations[i], 0.6);
                    if (i < frequencies.length - 1) {
                        Thread.sleep(pauseMs);
                    }
                }

                System.out.println("[🔔] Kritik stok alarmı çalındı!");

            } catch (Exception e) {
                System.err.println("[✗] Ses çalınırken hata: " + e.getMessage());
                // Fallback: Sistem bip sesi
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        }, "SoundAlert-Thread").start();
    }

    /**
     * Belirli frekansta ve sürede tek bir bip sentezler.
     *
     * @param frequency Frekans (Hz) — örn: 880.0 = A5 notası
     * @param durationMs Süre (milisaniye)
     * @param volume Ses seviyesi (0.0 - 1.0)
     */
    private static void synthesizeBeep(double frequency, int durationMs, double volume) throws Exception {
        int sampleRate = 16000;
        int totalSamples = (int) (sampleRate * durationMs / 1000.0);
        byte[] buffer = new byte[totalSamples];

        // Fade-in ve fade-out için kenar yumuşatma (click önleme)
        int fadeSamples = Math.min(totalSamples / 8, 400);

        for (int i = 0; i < totalSamples; i++) {
            double angle = 2.0 * Math.PI * frequency * i / sampleRate;
            double sample = Math.sin(angle) * volume;

            // Fade-in
            if (i < fadeSamples) {
                sample *= (double) i / fadeSamples;
            }
            // Fade-out
            if (i > totalSamples - fadeSamples) {
                sample *= (double) (totalSamples - i) / fadeSamples;
            }

            buffer[i] = (byte) (sample * 127.0);
        }

        AudioFormat format = new AudioFormat((float) sampleRate, 8, 1, true, false);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
        }
    }
}
