# Otobüs Rezervasyon Sistemi 

## 1. Projenin Amacı
Otobüs rezervasyon sistemi; kullanıcıların otobüs seferleri için koltuk rezervasyonu yapmasını, rezervasyonları görüntülemesini ve yönetmesini sağlar. Sistem, nesneye yönelik programlama ilkelerine uygun şekilde tasarlanmıştır.

## 2. Temel Sınıflar ve Özellikleri

### Bus (Otobüs)
- Plaka (licensePlate)
- Koltuk sayısı (seatCount)
- Koltukların durumu (seats: dolu/boş)
- Seferler (trips)

### Trip (Sefer)
- Sefer numarası (id)
- Kalkış noktası (origin)
- Varış noktası (destination)
- Tarih/saat (date, time)
- Otobüs (bus)
- Rezervasyonlar (reservations)

### User (Kullanıcı)
- Kullanıcı ID (user_id)
- Ad Soyad (name)
- Telefon (phone)
- E-posta (email)
- Kullanıcının rezervasyonları (reservations)

### Reservation (Rezervasyon)
- Rezervasyon ID (reservation_id)
- Kullanıcı (user)
- Sefer (trip)
- Koltuk numarası (seatNumber)
- Rezervasyon tarihi (reservationDate)

## 3. Fonksiyonlar (Metotlar)

- **Bus**
  - boşKoltuklarıListele()
  - seferEkle(trip)

- **Trip**
  - rezervasyonYap(user, seatNumber)
  - rezervasyonlarıListele()

- **User**
  - rezervasyonYap(trip, seatNumber)
  - rezervasyonlarıGörüntüle()

- **Reservation**
  - rezervasyonDetaylarınıGörüntüle()

## 4. Senaryo Akışı
1. Kullanıcı sisteme kaydolur.
2. Admin otobüs ve seferleri ekler.
3. Kullanıcı seferleri ve boş koltukları görüntüler.
4. Kullanıcı koltuk seçip rezervasyon yapar.
5. Kullanıcı rezervasyonlarını görüntüler.
