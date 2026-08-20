# Eczane Stok ve İlaç Takip Sistemi

Java Swing masaüstü uygulaması — eczane ilaç stoklarını yönetmek, kritik stok / SKT uyarıları üretmek ve stok istatistiklerini görselleştirmek için.

KSÜ Bilgisayar Mühendisliği · Görsel Programlama dersi projesi  
Geliştirici: **Resul Kılınç**

## Özellikler

- İlaç ekleme, düzenleme, silme (CRUD)
- Satış / tedarik ile stok güncelleme
- Kritik stok ve son kullanma tarihi uyarıları (sesli alert)
- Anlık arama ve tablo sıralama
- Graphics2D ile stok grafikleri
- Detaylı istatistik paneli
- FlatLaf koyu / açık tema

## Teknolojiler

| Teknoloji | Kullanım |
| --- | --- |
| Java 17 | Uygulama dili |
| Swing + FlatLaf | Modern masaüstü UI |
| SQLite + JDBC | Yerel veritabanı |
| Maven | Derleme ve bağımlılıklar |
| Graphics2D / Java Sound | Grafik ve uyarı sesi |

## Proje yapısı

```
src/main/java/com/pharmacy/
├── App.java                 # Giriş noktası, tema
├── model/Medicine.java      # İlaç modeli
├── db/DatabaseManager.java  # SQLite CRUD
├── logic/SoundAlert.java    # Sesli uyarı
├── util/UiUtils.java        # UI yardımcıları
└── ui/
    ├── Dashboard.java       # Ana pencere
    ├── MedicineDialog.java  # İlaç formu
    ├── StockChartPanel.java # Stok grafikleri
    ├── StatisticsPanel.java # İstatistikler
    └── AboutDialog.java     # Hakkında
```

## Çalıştırma

```bash
# Bağımlılıkları indir + çalıştır
mvn exec:java

# veya fat JAR üret
mvn package
java -jar target/pharmacy-inventory-system-1.0-SNAPSHOT.jar
```

Gereksinim: **JDK 17+** ve **Maven 3.x**

İlk çalıştırmada `pharmacy.db` yoksa veya boşsa örnek ilaç verisi otomatik yüklenir.

## Rapor

Ders proje raporu: [`rapor.html`](rapor.html) / [`rapor.pdf`](rapor.pdf)

## Lisans

Eğitim / portföy amaçlı. Ticari kullanım için izin gerekir.
