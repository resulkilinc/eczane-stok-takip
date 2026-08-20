# Eczane Stok ve İlaç Takip Sistemi

Java Swing desktop app for pharmacy inventory: CRUD, critical-stock / expiry alerts, charts, and statistics.

Author: **Resul Kılınç**

## Features

- Add / edit / delete medicines (CRUD)
- Stock updates via sale and supply flows
- Critical stock and expiry-date alerts (with sound)
- Live search and sortable table
- Stock charts (Graphics2D)
- Statistics panel
- FlatLaf dark / light themes

## Stack

| Tech | Role |
| --- | --- |
| Java 17 | Language |
| Swing + FlatLaf | Desktop UI |
| SQLite + JDBC | Local database |
| Maven | Build & dependencies |

## Layout

```
src/main/java/com/pharmacy/
├── App.java
├── model/Medicine.java
├── db/DatabaseManager.java
├── logic/SoundAlert.java
├── util/UiUtils.java
└── ui/
    ├── Dashboard.java
    ├── MedicineDialog.java
    ├── StockChartPanel.java
    ├── StatisticsPanel.java
    └── AboutDialog.java
```

## Run

Requirements: **JDK 17+**, **Maven 3.x**

```bash
mvn exec:java
```

Or build a runnable JAR:

```bash
mvn package
java -jar target/pharmacy-inventory-system-1.0-SNAPSHOT.jar
```

On first run the app creates `pharmacy.db` locally and seeds sample medicines if the table is empty. The database file is not committed.

## License

Educational / portfolio use. Commercial use requires permission.
