# Otobüs Rezervasyon Simülasyonu 🚌

Nesneye Yönelik Programlama (OOP) prensipleriyle geliştirilmiş, komut satırı tabanlı bir otobüs rezervasyon yönetim sistemi.

## 📋 İçindekiler

- [Özellikler](#özellikler)
- [Kurulum](#kurulum)
- [Kullanım](#kullanım)
- [Sistem Mimarisi](#sistem-mimarisi)
- [Örnek Kullanım Senaryoları](#örnek-kullanım-senaryoları)

## Özellikler

### Sefer Yönetimi
- ✅ Yeni sefer oluşturma (kalkış-varış, tarih-saat, kapasite, fiyat)
- ✅ Sefer listeleme ve detaylı görüntüleme
- ✅ Dinamik bilet fiyatlandırma (sefer bazlı)
- ✅ Gerçek zamanlı doluluk oranı takibi

### Rezervasyon İşlemleri
- ✅ Koltuk bazlı rezervasyon yapma
- ✅ UUID tabanlı benzersiz rezervasyon ID'si
- ✅ Rezervasyon iptal etme
- ✅ Detaylı bilet fişi yazdırma

### Raporlama ve Analiz
- ✅ Tüm rezervasyonları listeleme
- ✅ Sefer bazlı doluluk durumu
- ✅ Gelir hesaplama ve raporlama
- ✅ Boş/dolu koltuk görüntüleme

### Otomatik Test Verisi
- 5 farklı sefer (farklı güzergahlar ve fiyatlar)
- 10 örnek yolcu rezervasyonu
- Otomatik bilet fişi oluşturma
- Başlangıç raporu

## Kurulum

### Gereksinimler

- Java JDK 8 veya üzeri
- Komut satırı erişimi

### Adımlar

1. Projeyi klonlayın:

```bash
git clone https://github.com/kullaniciadi/otobus-rezervasyon.git
cd otobus-rezervasyon
```

2. Java dosyasını derleyin:

```bash
javac Main.java
```

3. Programı çalıştırın:

```bash
java Main
```

## Kullanım

Program başlatıldığında otomatik olarak örnek verilerle doldurulur ve ana menü görüntülenir:

```
=== Otobüs Rezervasyon Simülasyonu ===
1) Yeni sefer oluştur
2) Seferleri listele
3) Sefer detaylarını göster
4) Koltuk rezervasyonu yap
5) Rezervasyon iptal et (Rezervation ID ile)
6) Doluluk durumunu göster
7) Tüm rezervasyonları listele
8) Rapor: Toplam sefer sayısı ve gelir
0) Çıkış
```

### Örnek İşlem Akışı

#### 1. Sefer Listeleme

```
Seçiminiz: 2

SFR1001 | İstanbul -> Ankara | Kalkış: 30/10/2025 09:00 | Kap: 10 | Dolu: 3 | Doluluk: 30.0% | Fiyat: 550 TL
SFR1002 | İzmir -> Bursa | Kalkış: 29/10/2025 14:00 | Kap: 10 | Dolu: 2 | Doluluk: 20.0% | Fiyat: 450 TL
...
```

#### 2. Rezervasyon Yapma

```
Seçiminiz: 4
Sefer ID: SFR1001
Sefer: SFR1001 (İstanbul → Ankara) | Bilet Fiyatı: 550 TL
Koltuk numarası (1..10): 5
Yolcu adı: Ayşe Yılmaz
Telefon: 05551234567

✅ Rezervasyon tamamlandı! RezID: a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

#### 3. Bilet Fişi

```
=====================================
           BİLET FİŞİ / TICKET       
=====================================
Rezervasyon ID : a1b2c3d4-e5f6-7890-abcd-ef1234567890
Yolcu          : Ayşe Yılmaz
Telefon        : 05551234567
Sefer ID       : SFR1001
Güzergah       : İstanbul → Ankara
Kalkış         : 30/10/2025 09:00
Koltuk No      : 05
Bilet Fiyatı   : 550 TL
Rezervasyon Zamanı: 28/10/2025 15:30
-------------------------------------
NOT: Rezervasyon ID'nizi saklayınız.
=====================================
```

## Sistem Mimarisi

### Sınıf Yapısı

```
Main
  └── ReservationSystem (Controller)
        ├── Trip (Domain Model)
        │     └── Seat (Domain Model)
        └── CLI Interface
```

### Temel Sınıflar

#### `Seat` (Koltuk)

- Koltuk numarası, durum (boş/dolu)
- Yolcu bilgileri (ad, telefon)
- Rezervasyon zamanı ve benzersiz ID
- Rezervasyon yapma/iptal metotları

#### `Trip` (Sefer)

- Sefer bilgileri (ID, güzergah, tarih-saat)
- Kapasite ve fiyat yönetimi
- Koltuk koleksiyonu (Map yapısı)
- Doluluk oranı hesaplama
- Rezervasyon ID ile koltuk bulma

#### `ReservationSystem` (Ana Kontrol)

- Sefer yönetimi (CRUD işlemleri)
- CLI menü sistemi
- Rezervasyon işlemleri
- Raporlama ve analiz
- Örnek veri oluşturma

## Örnek Kullanım Senaryoları

### Senaryo 1: Yeni Sefer Ekleme

```java
// Sistem otomatik olarak seed data ile başlar
// Ancak yeni sefer eklemek için:
// Menüden "1" seçeneği -> Sefer bilgilerini girin
```

### Senaryo 2: Doluluk Raporu Alma

```java
// Tüm seferler için: Menü -> 6 -> Enter
// Belirli sefer için: Menü -> 6 -> Sefer ID girin
```

### Senaryo 3: Rezervasyon İptali

```java
// Menü -> 5 -> Rezervasyon ID'sini girin
// Örnek: a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

## Örnek Veri Yapısı

Sistem başlatıldığında aşağıdaki örnek veriler yüklenir:

| Sefer ID | Güzergah | Fiyat | Kapasite | Rezerve |
|----------|----------|-------|----------|---------|
| SFR1001 | İstanbul → Ankara | 550 TL | 10 | 3 |
| SFR1002 | İzmir → Bursa | 450 TL | 10 | 2 |
| SFR1003 | Antalya → Konya | 380 TL | 10 | 2 |
| SFR1004 | Kırklareli → İstanbul | 290 TL | 10 | 2 |
| SFR1005 | Trabzon → Samsun | 420 TL | 10 | 1 |

**Toplam Gelir (Seed):** 4.550 TL

## Geliştirme Notları

### OOP Prensipleri

- **Encapsulation:** Private field'lar ve public getter/setter metotları
- **Single Responsibility:** Her sınıf tek bir sorumluluğa sahip
- **Immutability:** Final field'lar değişmez veri yapıları için kullanılmış
- **Optional:** Null kontrolü yerine `Optional<T>` kullanımı

### Veri Yapıları

- `LinkedHashMap<>` - Ekleme sırasını koruyan sefer/koltuk yönetimi
- `UUID` - Benzersiz rezervasyon ID'leri
- `LocalDateTime` - Modern tarih-saat yönetimi

### Güvenlik

- Input validation (kapasite, fiyat, tarih format kontrolü)
- Duplicate ID kontrolü
- Rezervasyon durumu kontrolü
