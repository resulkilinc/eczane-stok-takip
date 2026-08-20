package com.pharmacy.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * İlaç veri modeli — OOP Encapsulation prensibi (Hafta 1-3).
 * Comparable interface'i ile doğal sıralama desteklenir.
 */
public class Medicine implements Comparable<Medicine> {
    private int id;
    private String name;
    private String category;
    private int stockCount;
    private double unitPrice;
    private int criticalLevel;
    private String expiryDate; // Format: dd.MM.yyyy

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Medicine() {}

    public Medicine(int id, String name, String category, int stockCount, double unitPrice, int criticalLevel, String expiryDate) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.stockCount = stockCount;
        this.unitPrice = unitPrice;
        this.criticalLevel = criticalLevel;
        this.expiryDate = expiryDate;
    }

    // ── Getters and Setters ──
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getStockCount() { return stockCount; }
    public void setStockCount(int stockCount) { this.stockCount = stockCount; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public int getCriticalLevel() { return criticalLevel; }
    public void setCriticalLevel(int criticalLevel) { this.criticalLevel = criticalLevel; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    // ── İş Mantığı Metotları ──

    /**
     * Stok, kritik seviyenin altında veya eşit mi?
     */
    public boolean isLowStock() {
        return stockCount <= criticalLevel;
    }

    /**
     * Son kullanma tarihi geçmiş mi? (SKT kontrolü)
     */
    public boolean isExpired() {
        try {
            LocalDate expiry = LocalDate.parse(expiryDate, DATE_FORMATTER);
            return LocalDate.now().isAfter(expiry);
        } catch (DateTimeParseException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Son kullanma tarihi 30 gün içinde dolacak mı?
     */
    public boolean isExpiringSoon() {
        try {
            LocalDate expiry = LocalDate.parse(expiryDate, DATE_FORMATTER);
            return LocalDate.now().plusDays(30).isAfter(expiry) && !isExpired();
        } catch (DateTimeParseException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Fiyatı formatlı olarak döndür: ₺45,50
     */
    public String getFormattedPrice() {
        return String.format("₺%.2f", unitPrice);
    }

    /**
     * Toplam stok değeri (stok * birim fiyat)
     */
    public double getTotalValue() {
        return stockCount * unitPrice;
    }

    /**
     * Comparable implementasyonu — İsme göre doğal sıralama (Hafta 1-3: OOP)
     */
    @Override
    public int compareTo(Medicine other) {
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        return name + " (" + category + ") — Stok: " + stockCount;
    }
}
