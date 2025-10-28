import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Otobüs Rezervasyon Simülasyonu
 * Nesneye Yönelik Programlama (OOP) örneği
 *
 * Çalıştırma:
 * javac Main.java
 * java Main
 */
public class Main {
    public static void main(String[] args) {
        ReservationSystem system = new ReservationSystem();
        system.seedSampleData(); // Örnek veriler: 5 sefer, 10 yolcu, rezervasyonlar + fişler
        system.runCLI();         // CLI ile etkileşim
    }
}

/* -----------------------------
    Domain Sınıfları (OOP)
    ----------------------------- */

/** Tek bir koltuğu temsil eder */
class Seat {
    private final int seatNumber; // 1..N
    private boolean reserved;
    private String passengerName;
    private String passengerPhone;
    private LocalDateTime reservationTime;
    private String reservationId; // UUID ile benzersiz id

    public Seat(int seatNumber) {
        this.seatNumber = seatNumber;
        this.reserved = false;
    }

    public int getSeatNumber() { return seatNumber; }
    public boolean isReserved() { return reserved; }
    public String getPassengerName() { return passengerName; }
    public String getPassengerPhone() { return passengerPhone; }
    public LocalDateTime getReservationTime() { return reservationTime; }
    public String getReservationId() { return reservationId; }

    /**
     * Rezervasyon yapar. reservationId otomatik UUID olarak atanır.
     */
    public void reserve(String passengerName, String passengerPhone) {
        if (this.reserved) return;
        this.reserved = true;
        this.passengerName = passengerName;
        this.passengerPhone = passengerPhone;
        this.reservationTime = LocalDateTime.now();
        this.reservationId = UUID.randomUUID().toString();
    }

    /**
     * Rezervasyon iptal eder.
     */
    public void cancel() {
        this.reserved = false;
        this.passengerName = null;
        this.passengerPhone = null;
        this.reservationTime = null;
        this.reservationId = null;
    }
}

/** Bir sefer (trip) temsil eder */
class Trip {
    private final String tripId; // benzersiz sefer id
    private final String origin;
    private final String destination;
    private final LocalDateTime departTime;
    private final int capacity; // toplam koltuk sayısı
    private final Map<Integer, Seat> seats; // seatNumber -> Seat

    // Her sefer için farklı bilet fiyatı
    private final int ticketPrice;

    // CONSTRUCTOR GÜNCELLENDİ: ticketPrice parametresi eklendi
    public Trip(String tripId, String origin, String destination, LocalDateTime departTime, int capacity, int ticketPrice) {
        if (capacity <= 0) throw new IllegalArgumentException("Kapasite pozitif olmalı.");
        if (ticketPrice <= 0) throw new IllegalArgumentException("Bilet fiyatı pozitif olmalı.");
        this.tripId = tripId;
        this.origin = origin;
        this.destination = destination;
        this.departTime = departTime;
        this.capacity = capacity;
        this.ticketPrice = ticketPrice; // Artık farklı fiyatlar atanabilir
        this.seats = new LinkedHashMap<>();
        for (int i = 1; i <= capacity; i++) {
            seats.put(i, new Seat(i));
        }
    }

    public String getTripId() { return tripId; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDepartTime() { return departTime; }
    public int getCapacity() { return capacity; }
    public int getTicketPrice() { return ticketPrice; }

    public Optional<Seat> getSeat(int seatNumber) {
        return Optional.ofNullable(seats.get(seatNumber));
    }

    public List<Seat> getAllSeats() {
        return new ArrayList<>(seats.values());
    }

    public List<Seat> getReservedSeats() {
        List<Seat> r = new ArrayList<>();
        for (Seat s : seats.values()) if (s.isReserved()) r.add(s);
        return r;
    }

    public List<Seat> getAvailableSeats() {
        List<Seat> a = new ArrayList<>();
        for (Seat s : seats.values()) if (!s.isReserved()) a.add(s);
        return a;
    }

    public double getOccupancyRate() {
        int reserved = getReservedSeats().size();
        return (reserved / (double) capacity) * 100.0;
    }

    public Optional<Seat> findSeatByReservationId(String reservationId) {
        for (Seat s : seats.values()) {
            if (s.isReserved() && reservationId.equals(s.getReservationId())) return Optional.of(s);
        }
        return Optional.empty();
    }

    /**
     * Rezervasyon yapar ve rezervasyon yapılan Seat'i döner (null ise başarısız).
     * Burada basit mantık: seatNumber geçerliyse ve boşsa rezervasyon yapılır.
     */
    public Seat reserveSeatDirect(int seatNumber, String passengerName, String passengerPhone) {
        Optional<Seat> maybe = getSeat(seatNumber);
        if (!maybe.isPresent()) return null;
        Seat s = maybe.get();
        if (s.isReserved()) return null;
        s.reserve(passengerName, passengerPhone);
        return s;
    }
}

/* -----------------------------
    Reservation System (Controller)
    ----------------------------- */

class ReservationSystem {
    private final Map<String, Trip> trips; // tripId -> Trip
    private final Scanner scanner;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DateTimeFormatter prettyDtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ReservationSystem() {
        this.trips = new LinkedHashMap<>();
        this.scanner = new Scanner(System.in);
    }

    // CLI ana döngüsü
    public void runCLI() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": createTripCLI(); break;
                case "2": listTripsCLI(); break;
                case "3": showTripDetailsCLI(); break;
                case "4": reserveSeatCLI(); break;
                case "5": cancelReservationCLI(); break;
                case "6": showOccupancyCLI(); break;
                case "7": listAllReservationsCLI(); break;
                case "8": showSummaryCLI(); break;
                case "0":
                    System.out.println("Çıkılıyor. İyi günler! 👋");
                    running = false;
                    break;
                default:
                    System.out.println("Geçersiz seçim. Tekrar deneyin.");
            }
            System.out.println();
        }
    }

    private void printMainMenu() {
        System.out.println("=== Otobüs Rezervasyon Simülasyonu ===");
        System.out.println("1) Yeni sefer oluştur");
        System.out.println("2) Seferleri listele");
        System.out.println("3) Sefer detaylarını göster");
        System.out.println("4) Koltuk rezervasyonu yap");
        System.out.println("5) Rezervasyon iptal et (Rezervation ID ile)");
        System.out.println("6) Doluluk durumunu göster");
        System.out.println("7) Tüm rezervasyonları listele");
        System.out.println("8) Rapor: Toplam sefer sayısı ve gelir");
        System.out.println("0) Çıkış");
        System.out.print("Seçiminiz: ");
    }

    /* ---------- CLI İşlevleri ---------- */

    private void createTripCLI() {
        System.out.println("--- Yeni Sefer Oluştur ---");
        System.out.print("Sefer ID (örnek: SFR1001): ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty() || trips.containsKey(id)) {
            System.out.println("Geçersiz veya mevcut ID.");
            return;
        }
        System.out.print("Kalkış yeri: ");
        String origin = scanner.nextLine().trim();
        System.out.print("Varış yeri: ");
        String dest = scanner.nextLine().trim();
        System.out.print("Kalkış tarihi-saat (YYYY-MM-DD HH:mm): ");
        String dtStr = scanner.nextLine().trim();
        LocalDateTime dt;
        try {
            dt = LocalDateTime.parse(dtStr, dtf);
        } catch (Exception e) {
            System.out.println("Tarih formatı hatalı. Örnek: 2025-11-01 13:30");
            return;
        }

        // Kapasite 10 olarak sabitlendi.
        int cap = 10;
        System.out.println("Kapasite: " + cap + " (Otobüs kapasitesi 10 olarak sabitlenmiştir.)");

        System.out.print("Bilet Fiyatı (TL): ");
        int price;
        try {
            price = Integer.parseInt(scanner.nextLine().trim());
            if (price <= 0) {
                System.out.println("Bilet fiyatı pozitif olmalı.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Hatalı bilet fiyatı.");
            return;
        }

        // Fiyat parametresi Trip constructor'ına eklendi
        Trip t = new Trip(id, origin, dest, dt, cap, price);
        trips.put(id, t);
        System.out.println("✅ Sefer oluşturuldu: " + id);
    }

    private void listTripsCLI() {
        if (trips.isEmpty()) {
            System.out.println("Henüz sefer yok.");
            return;
        }
        System.out.println("--- Seferler ---");
        for (Trip t : trips.values()) {
            System.out.printf("%s | %s -> %s | Kalkış: %s | Kap: %d | Dolu: %d | Doluluk: %4.1f%% | Fiyat: %d TL\n",
                    t.getTripId(), t.getOrigin(), t.getDestination(),
                    t.getDepartTime().format(prettyDtf),
                    t.getCapacity(),
                    t.getReservedSeats().size(),
                    t.getOccupancyRate(),
                    t.getTicketPrice()); // Farklı fiyatlar burada görünüyor
        }
    }

    private void showTripDetailsCLI() {
        System.out.print("Sefer ID: ");
        String id = scanner.nextLine().trim();
        Trip t = trips.get(id);
        if (t == null) {
            System.out.println("Sefer bulunamadı.");
            return;
        }
        System.out.printf("Sefer %s (%s → %s) - Bilet: %d TL\n", t.getTripId(), t.getOrigin(), t.getDestination(), t.getTicketPrice());
        System.out.printf("Kalkış: %s | Kapasite: %d | Doluluk: %d (%%%.2f)\n",
                t.getDepartTime().format(prettyDtf),
                t.getCapacity(),
                t.getReservedSeats().size(),
                t.getOccupancyRate());
        System.out.println("Koltuk listesi (No : Durum [RezID kısa] - Yolcu):");
        for (Seat s : t.getAllSeats()) {
            String status = s.isReserved() ? ("DOLU [" + shortRid(s.getReservationId()) + "] - " + s.getPassengerName()) : "BOŞ";
            System.out.printf("%02d : %s\n", s.getSeatNumber(), status);
        }
    }

    private void reserveSeatCLI() {
        System.out.print("Sefer ID: ");
        String id = scanner.nextLine().trim();
        Trip t = trips.get(id);
        if (t == null) {
            System.out.println("Sefer bulunamadı.");
            return;
        }
        System.out.printf("Sefer: %s (%s → %s) | Bilet Fiyatı: %d TL%n", t.getTripId(), t.getOrigin(), t.getDestination(), t.getTicketPrice());
        System.out.print("Koltuk numarası (1.." + t.getCapacity() + "): ");
        int seatNo;
        try {
            seatNo = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Geçersiz koltuk numarası.");
            return;
        }
        System.out.print("Yolcu adı: ");
        String name = scanner.nextLine().trim();
        System.out.print("Telefon: ");
        String phone = scanner.nextLine().trim();

        Seat seat = t.reserveSeatDirect(seatNo, name, phone);
        if (seat == null) {
            System.out.println("Rezervasyon başarısız (koltuk dolu veya numara hatalı).");
            return;
        }
        System.out.println("✅ Rezervasyon tamamlandı! RezID: " + seat.getReservationId());
        // Fiş (ticket) yazdır
        printTicket(t, seat);
    }

    private void cancelReservationCLI() {
        System.out.print("Rezervasyon ID girin: ");
        String rid = scanner.nextLine().trim();
        boolean found = false;
        for (Trip t : trips.values()) {
            Optional<Seat> seatOpt = t.findSeatByReservationId(rid);
            if (seatOpt.isPresent()) {
                Seat s = seatOpt.get();
                System.out.printf("İptal ediliyor: Sefer %s | Koltuk %d | Yolcu %s%n",
                        t.getTripId(), s.getSeatNumber(), s.getPassengerName());
                s.cancel();
                System.out.println("✅ Rezervasyon iptal edildi.");
                found = true;
                break;
            }
        }
        if (!found) System.out.println("❌ Rezervasyon ID bulunamadı.");
    }

    private void showOccupancyCLI() {
        System.out.print("Sefer ID (tüm seferler için boş bırak): ");
        String id = scanner.nextLine().trim();
        System.out.println("--- Doluluk Durumu ve Gelir ---");
        if (id.isEmpty()) {
            for (Trip t : trips.values()) {
                System.out.printf("%s | %s → %s | Fiyat: %d TL | Doluluk: %d/%d (%%%.2f) | Tahmini Gelir: %d TL%n",
                        t.getTripId(), t.getOrigin(), t.getDestination(), t.getTicketPrice(),
                        t.getReservedSeats().size(), t.getCapacity(),
                        t.getOccupancyRate(),
                        t.getReservedSeats().size() * t.getTicketPrice());
            }
            return;
        }
        Trip t = trips.get(id);
        if (t == null) {
            System.out.println("Sefer bulunamadı.");
            return;
        }
        System.out.printf("Sefer %s | Fiyat: %d TL | Doluluk: %d/%d (%%%.2f) | Tahmini Gelir: %d TL%n",
                t.getTripId(), t.getTicketPrice(),
                t.getReservedSeats().size(),
                t.getCapacity(),
                t.getOccupancyRate(),
                t.getReservedSeats().size() * t.getTicketPrice());
        System.out.println("Boş koltuklar: " + formatSeatList(t.getAvailableSeats()));
        System.out.println("Dolu koltuklar: " + formatSeatList(t.getReservedSeats()));
    }

    private void listAllReservationsCLI() {
        boolean any = false;
        System.out.println("--- Tüm Rezervasyonlar ---");
        for (Trip t : trips.values()) {
            for (Seat s : t.getReservedSeats()) {
                any = true;
                System.out.printf("Sefer %s | Koltuk %02d | Fiyat: %d TL | RezID: %s | Yolcu: %s | Tel: %s | Zaman: %s%n",
                        t.getTripId(), s.getSeatNumber(), t.getTicketPrice(),
                        s.getReservationId(),
                        s.getPassengerName(),
                        s.getPassengerPhone() == null ? "(bilgi yok)" : s.getPassengerPhone(),
                        s.getReservationTime() == null ? "(bilgi yok)" : s.getReservationTime().format(prettyDtf));
            }
        }
        if (!any) System.out.println("Hiç rezervasyon yok.");
    }

    private void showSummaryCLI() {
        int tripCount = trips.size();
        int totalRevenue = calculateTotalRevenue();
        System.out.println("=== RAPOR ===");
        System.out.println("Toplam Sefer Sayısı: " + tripCount);
        System.out.println("Toplam Gelir: " + totalRevenue + " TL");
        System.out.println("Sefer Bazlı Detaylar:");
        for (Trip t : trips.values()) {
            System.out.printf("%s | %s → %s | Fiyat: %d TL | Rezerve: %d | Kap: %d | Doluluk: %4.1f%% | Gelir: %d TL%n",
                    t.getTripId(), t.getOrigin(), t.getDestination(), t.getTicketPrice(),
                    t.getReservedSeats().size(), t.getCapacity(),
                    t.getOccupancyRate(),
                    t.getReservedSeats().size() * t.getTicketPrice());
        }
    }

    /* ---------- Yardımcı metotlar ---------- */

    private String shortRid(String rid) {
        if (rid == null) return "(yok)";
        return rid.length() <= 8 ? rid : rid.substring(0, 8);
    }

    private String formatSeatList(List<Seat> seats) {
        if (seats.isEmpty()) return "(yok)";
        StringBuilder sb = new StringBuilder();
        for (Seat s : seats) {
            sb.append(s.getSeatNumber()).append(", ");
        }
        if (sb.length() >= 2) sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    /**
     * Konsola bilet fişi yazar (zengin, okunaklı format).
     */
    private void printTicket(Trip t, Seat s) {
        System.out.println("\n=====================================");
        System.out.println("           BİLET FİŞİ / TICKET       ");
        System.out.println("=====================================");
        System.out.printf("Rezervasyon ID : %s%n", s.getReservationId());
        System.out.printf("Yolcu          : %s%n", s.getPassengerName());
        System.out.printf("Telefon        : %s%n", s.getPassengerPhone() == null ? "(yok)" : s.getPassengerPhone());
        System.out.printf("Sefer ID       : %s%n", t.getTripId());
        System.out.printf("Güzergah       : %s → %s%n", t.getOrigin(), t.getDestination());
        System.out.printf("Kalkış         : %s%n", t.getDepartTime().format(prettyDtf));
        System.out.printf("Koltuk No      : %02d%n", s.getSeatNumber());
        System.out.printf("Bilet Fiyatı   : %d TL%n", t.getTicketPrice()); // Farklı fiyatı gösterir
        System.out.printf("Rezervasyon Zamanı: %s%n", s.getReservationTime() == null ? "(bilgi yok)" : s.getReservationTime().format(prettyDtf));
        System.out.println("-------------------------------------");
        System.out.println("NOT: Rezervasyon ID'nizi saklayınız. İptal için bu ID gereklidir.");
        System.out.println("=====================================\n");
    }

    /**
     * Toplam geliri hesapla (tüm seferler)
     */
    private int calculateTotalRevenue() {
        int total = 0;
        for (Trip t : trips.values()) {
            total += t.getReservedSeats().size() * t.getTicketPrice();
        }
        return total;
    }

    /**
     * Toplam rezerve koltuk sayısını hesaplar.
     */
    private int calculateTotalReservations() {
        int total = 0;
        for (Trip t : trips.values()) {
            total += t.getReservedSeats().size();
        }
        return total;
    }

    /* ---------- Örnek/seed verisi oluşturma ---------- */

    // örnek seferler ve rezervasyonlar ekle: 5 sefer, 10 yolcu, her biri için rezervasyon + fiş
    public void seedSampleData() {
        // Otobüs kapasitesi 10 olarak sabitlendi.
        final int CAPACITY = 10;

        // 1) Seferleri oluştur (kapasite 10, her birinin fiyatı farklı)
        Trip s1 = new Trip("SFR1001", "İstanbul", "Ankara",
                LocalDateTime.now().plusDays(2).withHour(9).withMinute(0), CAPACITY, 550); // Fiyat: 550 TL

        Trip s2 = new Trip("SFR1002", "İzmir", "Bursa",
                LocalDateTime.now().plusDays(1).withHour(14).withMinute(0), CAPACITY, 450); // Fiyat: 450 TL

        Trip s3 = new Trip("SFR1003", "Antalya", "Konya",
                LocalDateTime.now().plusDays(3).withHour(10).withMinute(30), CAPACITY, 380); // Fiyat: 380 TL

        Trip s4 = new Trip("SFR1004", "Kırklareli", "İstanbul",
                LocalDateTime.now().plusDays(1).withHour(8).withMinute(15), CAPACITY, 290); // Fiyat: 290 TL

        Trip s5 = new Trip("SFR1005", "Trabzon", "Samsun",
                LocalDateTime.now().plusDays(2).withHour(7).withMinute(45), CAPACITY, 420); // Fiyat: 420 TL

        // Map'e ekle
        trips.put(s1.getTripId(), s1);
        trips.put(s2.getTripId(), s2);
        trips.put(s3.getTripId(), s3);
        trips.put(s4.getTripId(), s4);
        trips.put(s5.getTripId(), s5);

        // 2) Yolcular (10 kişi)
        String[] passengers = {
                "Ali Yılmaz", "Zeynep Demir", "Ahmet Kaya", "Ece Yalçın", "Mehmet Aksoy",
                "Furkan Çelik", "Selin Öztürk", "Caner Yücel", "Deniz Şahin", "Gizem Kılıç"
        };
        String[] phones = {
                "05330001111", "05330002222", "05330003333", "05330004444", "05330005555",
                "05330006666", "05330007777", "05330008888", "05330009999", "05330000000"
        };

        // 3) Rezervasyonlar (seed): Toplam 10 rezervasyon (her yolcuya bir koltuk)
        // Sefer 1 (İstanbul-Ankara): 3 kişi rezerve
        Seat r1_1 = s1.reserveSeatDirect(1, passengers[0], phones[0]); if (r1_1 != null) printTicket(s1, r1_1);
        Seat r1_2 = s1.reserveSeatDirect(2, passengers[1], phones[1]); if (r1_2 != null) printTicket(s1, r1_2);
        Seat r1_3 = s1.reserveSeatDirect(3, passengers[2], phones[2]); if (r1_3 != null) printTicket(s1, r1_3);

        // Sefer 2 (İzmir-Bursa): 2 kişi rezerve
        Seat r2_1 = s2.reserveSeatDirect(5, passengers[3], phones[3]); if (r2_1 != null) printTicket(s2, r2_1);
        Seat r2_2 = s2.reserveSeatDirect(6, passengers[4], phones[4]); if (r2_2 != null) printTicket(s2, r2_2);

        // Sefer 3 (Antalya-Konya): 2 kişi rezerve
        Seat r3_1 = s3.reserveSeatDirect(1, passengers[5], phones[5]); if (r3_1 != null) printTicket(s3, r3_1);
        Seat r3_2 = s3.reserveSeatDirect(2, passengers[6], phones[6]); if (r3_2 != null) printTicket(s3, r3_2);

        // Sefer 4 (Kırklareli-İstanbul): 2 kişi rezerve
        Seat r4_1 = s4.reserveSeatDirect(3, passengers[7], phones[7]); if (r4_1 != null) printTicket(s4, r4_1);
        Seat r4_2 = s4.reserveSeatDirect(4, passengers[8], phones[8]); if (r4_2 != null) printTicket(s4, r4_2);

        // Sefer 5 (Trabzon-Samsun): 1 kişi rezerve
        Seat r5_1 = s5.reserveSeatDirect(10, passengers[9], phones[9]); if (r5_1 != null) printTicket(s5, r5_1);


        // 4) Başlangıç raporu: toplam sefer sayısı, sefer bazlı doluluk ve gelir
        System.out.println("\n----------------- Başlangıç (Seed) Raporu -----------------");
        System.out.println("Toplam sefer sayısı: " + trips.size());

        // HATA VEREN SATIR DÜZELTİLDİ: calculateTotalReservations metodu ile güvenli toplama yapılıyor.
        System.out.println("Toplam yolcu sayısı (seed): " + calculateTotalReservations());

        int totalRevenue = calculateTotalRevenue();
        System.out.println("Toplam gelir (seed): " + totalRevenue + " TL");
        System.out.println("Sefer detayları:");
        for (Trip t : trips.values()) {
            System.out.printf("%s | %s → %s | Fiyat: %d TL | Rezerve: %d | Kap: %d | Doluluk: %4.1f%% | Gelir: %d TL%n",
                    t.getTripId(), t.getOrigin(), t.getDestination(), t.getTicketPrice(),
                    t.getReservedSeats().size(), t.getCapacity(),
                    t.getOccupancyRate(),
                    t.getReservedSeats().size() * t.getTicketPrice());
        }
        System.out.println("-----------------------------------------------------------\n");
    }
}