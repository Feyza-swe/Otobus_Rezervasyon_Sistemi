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
        RezervasyonSistemi sistem = new RezervasyonSistemi();
        sistem.ornekVerileriYukle(); // Örnek veriler: 5 sefer, 10 yolcu, rezervasyonlar + fişler
        sistem.cliCalistir();         // CLI ile etkileşim
    }
}

/* -----------------------------
    Alan Sınıfları (OOP)
    ----------------------------- */

/** Tek bir koltuğu temsil eder */
class Koltuk {
    private final int koltukNumarasi; // 1..N
    private boolean rezerveEdildi;
    private String yolcuAdi;
    private String yolcuTelefonu;
    private LocalDateTime rezervasyonZamani;
    private String rezervasyonKimlik; // UUID ile benzersiz kimlik

    public Koltuk(int koltukNumarasi) {
        this.koltukNumarasi = koltukNumarasi;
        this.rezerveEdildi = false;
    }

    public int getKoltukNumarasi() { return koltukNumarasi; }
    public boolean isRezerveEdildi() { return rezerveEdildi; }
    public String getYolcuAdi() { return yolcuAdi; }
    public String getYolcuTelefonu() { return yolcuTelefonu; }
    public LocalDateTime getRezervasyonZamani() { return rezervasyonZamani; }
    public String getRezervasyonKimlik() { return rezervasyonKimlik; }

    /**
     * Rezervasyon yapar. rezervasyonKimlik otomatik UUID olarak atanır.
     */
    public void rezerveEt(String yolcuAdi, String yolcuTelefonu) {
        if (this.rezerveEdildi) return;
        this.rezerveEdildi = true;
        this.yolcuAdi = yolcuAdi;
        this.yolcuTelefonu = yolcuTelefonu;
        this.rezervasyonZamani = LocalDateTime.now();
        this.rezervasyonKimlik = UUID.randomUUID().toString();
    }

    /**
     * Rezervasyon iptal eder.
     */
    public void iptalEt() {
        this.rezerveEdildi = false;
        this.yolcuAdi = null;
        this.yolcuTelefonu = null;
        this.rezervasyonZamani = null;
        this.rezervasyonKimlik = null;
    }
}

/** Bir sefer temsil eder */
class Sefer {
    private final String seferKimlik; // benzersiz sefer kimliği
    private final String kalkisYeri;
    private final String varisYeri;
    private final LocalDateTime kalkisZamani;
    private final int kapasite; // toplam koltuk sayısı
    private final Map<Integer, Koltuk> koltuklar; // koltukNumarasi -> Koltuk

    // Her sefer için farklı bilet fiyatı
    private final int biletFiyati;

    public Sefer(String seferKimlik, String kalkisYeri, String varisYeri, LocalDateTime kalkisZamani, int kapasite, int biletFiyati) {
        if (kapasite <= 0) throw new IllegalArgumentException("Kapasite pozitif olmalı.");
        if (biletFiyati <= 0) throw new IllegalArgumentException("Bilet fiyatı pozitif olmalı.");
        this.seferKimlik = seferKimlik;
        this.kalkisYeri = kalkisYeri;
        this.varisYeri = varisYeri;
        this.kalkisZamani = kalkisZamani;
        this.kapasite = kapasite;
        this.biletFiyati = biletFiyati;
        this.koltuklar = new LinkedHashMap<>();
        for (int i = 1; i <= kapasite; i++) {
            koltuklar.put(i, new Koltuk(i));
        }
    }

    public String getSeferKimlik() { return seferKimlik; }
    public String getKalkisYeri() { return kalkisYeri; }
    public String getVarisYeri() { return varisYeri; }
    public LocalDateTime getKalkisZamani() { return kalkisZamani; }
    public int getKapasite() { return kapasite; }
    public int getBiletFiyati() { return biletFiyati; }

    public Optional<Koltuk> getKoltuk(int koltukNumarasi) {
        return Optional.ofNullable(koltuklar.get(koltukNumarasi));
    }

    public List<Koltuk> getTumKoltuklar() {
        return new ArrayList<>(koltuklar.values());
    }

    public List<Koltuk> getRezerveKoltuklar() {
        List<Koltuk> rezerveListesi = new ArrayList<>();
        for (Koltuk k : koltuklar.values()) if (k.isRezerveEdildi()) rezerveListesi.add(k);
        return rezerveListesi;
    }

    public List<Koltuk> getBosKoltuklar() {
        List<Koltuk> boslar = new ArrayList<>();
        for (Koltuk k : koltuklar.values()) if (!k.isRezerveEdildi()) boslar.add(k);
        return boslar;
    }

    public double getDolulukOrani() {
        int rezerve = getRezerveKoltuklar().size();
        return (rezerve / (double) kapasite) * 100.0;
    }

    public Optional<Koltuk> rezervasyonKimlikIleKoltukBul(String rezervasyonKimlik) {
        for (Koltuk k : koltuklar.values()) {
            if (k.isRezerveEdildi() && rezervasyonKimlik.equals(k.getRezervasyonKimlik())) return Optional.of(k);
        }
        return Optional.empty();
    }

    /**
     * Rezervasyon yapar ve rezervasyon yapılan Koltuk'u döner (null ise başarısız).
     */
    public Koltuk koltukRezerveEt(int koltukNumarasi, String yolcuAdi, String yolcuTelefonu) {
        Optional<Koltuk> belki = getKoltuk(koltukNumarasi);
        if (!belki.isPresent()) return null;
        Koltuk k = belki.get();
        if (k.isRezerveEdildi()) return null;
        k.rezerveEt(yolcuAdi, yolcuTelefonu);
        return k;
    }
}

/* -----------------------------
    Rezervasyon Sistemi (Kontrolcü)
    ----------------------------- */

class RezervasyonSistemi {
    private final Map<String, Sefer> seferler; // seferKimlik -> Sefer
    private final Scanner tarayici;
    private final DateTimeFormatter tarihFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DateTimeFormatter guzelTarihFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public RezervasyonSistemi() {
        this.seferler = new LinkedHashMap<>();
        this.tarayici = new Scanner(System.in);
    }

    // CLI ana döngüsü
    public void cliCalistir() {
        boolean calisiyor = true;
        while (calisiyor) {
            anaMenuyuYazdir();
            String secim = tarayici.nextLine().trim();
            switch (secim) {
                case "1": seferOlusturCLI(); break;
                case "2": seferleriListeleCLI(); break;
                case "3": seferDetaylariGosterCLI(); break;
                case "4": koltukRezerveEtCLI(); break;
                case "5": rezervasyonIptalEtCLI(); break;
                case "6": dolulukGosterCLI(); break;
                case "7": tumRezervasyonlariListeleCLI(); break;
                case "8": ozetGosterCLI(); break;
                case "0":
                    System.out.println("Çıkılıyor. İyi günler! 👋");
                    calisiyor = false;
                    break;
                default:
                    System.out.println("Geçersiz seçim. Tekrar deneyin.");
            }
            System.out.println();
        }
    }

    private void anaMenuyuYazdir() {
        System.out.println("=== Otobüs Rezervasyon Simülasyonu ===");
        System.out.println("1) Yeni sefer oluştur");
        System.out.println("2) Seferleri listele");
        System.out.println("3) Sefer detaylarını göster");
        System.out.println("4) Koltuk rezervasyonu yap");
        System.out.println("5) Rezervasyon iptal et (Rezervasyon ID ile)");
        System.out.println("6) Doluluk durumunu göster");
        System.out.println("7) Tüm rezervasyonları listele");
        System.out.println("8) Rapor: Toplam sefer sayısı ve gelir");
        System.out.println("0) Çıkış");
        System.out.print("Seçiminiz: ");
    }

    /* ---------- CLI İşlevleri ---------- */

    private void seferOlusturCLI() {
        System.out.println("--- Yeni Sefer Oluştur ---");
        System.out.print("Sefer ID (örnek: SFR1001): ");
        String kimlik = tarayici.nextLine().trim();
        if (kimlik.isEmpty() || seferler.containsKey(kimlik)) {
            System.out.println("Geçersiz veya mevcut ID.");
            return;
        }
        System.out.print("Kalkış yeri: ");
        String kalkis = tarayici.nextLine().trim();
        System.out.print("Varış yeri: ");
        String varis = tarayici.nextLine().trim();
        System.out.print("Kalkış tarihi-saat (YYYY-MM-DD HH:mm): ");
        String tarihStr = tarayici.nextLine().trim();
        LocalDateTime tarih;
        try {
            tarih = LocalDateTime.parse(tarihStr, tarihFormat);
        } catch (Exception e) {
            System.out.println("Tarih formatı hatalı. Örnek: 2025-11-01 13:30");
            return;
        }

        // Kapasite 10 olarak sabitlendi.
        int kap = 10;
        System.out.println("Kapasite: " + kap + " (Otobüs kapasitesi 10 olarak sabitlenmiştir.)");

        System.out.print("Bilet Fiyatı (TL): ");
        int fiyat;
        try {
            fiyat = Integer.parseInt(tarayici.nextLine().trim());
            if (fiyat <= 0) {
                System.out.println("Bilet fiyatı pozitif olmalı.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Hatalı bilet fiyatı.");
            return;
        }

        Sefer s = new Sefer(kimlik, kalkis, varis, tarih, kap, fiyat);
        seferler.put(kimlik, s);
        System.out.println("✅ Sefer oluşturuldu: " + kimlik);
    }

    private void seferleriListeleCLI() {
        if (seferler.isEmpty()) {
            System.out.println("Henüz sefer yok.");
            return;
        }
        System.out.println("--- Seferler ---");
        for (Sefer s : seferler.values()) {
            System.out.printf("%s | %s → %s | Kalkış: %s | Kap: %d | Dolu: %d | Doluluk: %4.1f%% | Fiyat: %d TL\n",
                    s.getSeferKimlik(), s.getKalkisYeri(), s.getVarisYeri(),
                    s.getKalkisZamani().format(guzelTarihFormat),
                    s.getKapasite(),
                    s.getRezerveKoltuklar().size(),
                    s.getDolulukOrani(),
                    s.getBiletFiyati());
        }
    }

    private void seferDetaylariGosterCLI() {
        System.out.print("Sefer ID: ");
        String kimlik = tarayici.nextLine().trim();
        Sefer s = seferler.get(kimlik);
        if (s == null) {
            System.out.println("Sefer bulunamadı.");
            return;
        }
        System.out.printf("Sefer %s (%s → %s) - Bilet: %d TL\n", s.getSeferKimlik(), s.getKalkisYeri(), s.getVarisYeri(), s.getBiletFiyati());
        System.out.printf("Kalkış: %s | Kapasite: %d | Doluluk: %d (%%%.2f)\n",
                s.getKalkisZamani().format(guzelTarihFormat),
                s.getKapasite(),
                s.getRezerveKoltuklar().size(),
                s.getDolulukOrani());
        System.out.println("Koltuk listesi (No : Durum [RezID kısa] - Yolcu):");
        for (Koltuk k : s.getTumKoltuklar()) {
            String durum = k.isRezerveEdildi() ? ("DOLU [" + kisaRezKimlik(k.getRezervasyonKimlik()) + "] - " + k.getYolcuAdi()) : "BOŞ";
            System.out.printf("%02d : %s\n", k.getKoltukNumarasi(), durum);
        }
    }

    private void koltukRezerveEtCLI() {
        System.out.print("Sefer ID: ");
        String kimlik = tarayici.nextLine().trim();
        Sefer s = seferler.get(kimlik);
        if (s == null) {
            System.out.println("Sefer bulunamadı.");
            return;
        }
        System.out.printf("Sefer: %s (%s → %s) | Bilet Fiyatı: %d TL%n", s.getSeferKimlik(), s.getKalkisYeri(), s.getVarisYeri(), s.getBiletFiyati());
        System.out.print("Koltuk numarası (1.." + s.getKapasite() + "): ");
        int koltukNo;
        try {
            koltukNo = Integer.parseInt(tarayici.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Geçersiz koltuk numarası.");
            return;
        }
        System.out.print("Yolcu adı: ");
        String ad = tarayici.nextLine().trim();
        System.out.print("Telefon: ");
        String telefon = tarayici.nextLine().trim();

        Koltuk koltuk = s.koltukRezerveEt(koltukNo, ad, telefon);
        if (koltuk == null) {
            System.out.println("Rezervasyon başarısız (koltuk dolu veya numara hatalı).");
            return;
        }
        System.out.println("✅ Rezervasyon tamamlandı! RezID: " + koltuk.getRezervasyonKimlik());
        // Fiş yazdır
        fisYazdir(s, koltuk);
    }

    private void rezervasyonIptalEtCLI() {
        System.out.print("Rezervasyon ID girin: ");
        String rezKimlik = tarayici.nextLine().trim();
        boolean bulundu = false;
        for (Sefer s : seferler.values()) {
            Optional<Koltuk> koltukOpt = s.rezervasyonKimlikIleKoltukBul(rezKimlik);
            if (koltukOpt.isPresent()) {
                Koltuk k = koltukOpt.get();
                System.out.printf("İptal ediliyor: Sefer %s | Koltuk %d | Yolcu %s%n",
                        s.getSeferKimlik(), k.getKoltukNumarasi(), k.getYolcuAdi());
                k.iptalEt();
                System.out.println("✅ Rezervasyon iptal edildi.");
                bulundu = true;
                break;
            }
        }
        if (!bulundu) System.out.println("❌ Rezervasyon ID bulunamadı.");
    }

    private void dolulukGosterCLI() {
        System.out.print("Sefer ID (tüm seferler için boş bırak): ");
        String kimlik = tarayici.nextLine().trim();
        System.out.println("--- Doluluk Durumu ve Gelir ---");
        if (kimlik.isEmpty()) {
            for (Sefer s : seferler.values()) {
                System.out.printf("%s | %s → %s | Fiyat: %d TL | Doluluk: %d/%d (%%%.2f) | Tahmini Gelir: %d TL%n",
                        s.getSeferKimlik(), s.getKalkisYeri(), s.getVarisYeri(), s.getBiletFiyati(),
                        s.getRezerveKoltuklar().size(), s.getKapasite(),
                        s.getDolulukOrani(),
                        s.getRezerveKoltuklar().size() * s.getBiletFiyati());
            }
            return;
        }
        Sefer s = seferler.get(kimlik);
        if (s == null) {
            System.out.println("Sefer bulunamadı.");
            return;
        }
        System.out.printf("Sefer %s | Fiyat: %d TL | Doluluk: %d/%d (%%%.2f) | Tahmini Gelir: %d TL%n",
                s.getSeferKimlik(), s.getBiletFiyati(),
                s.getRezerveKoltuklar().size(),
                s.getKapasite(),
                s.getDolulukOrani(),
                s.getRezerveKoltuklar().size() * s.getBiletFiyati());
        System.out.println("Boş koltuklar: " + koltukListesiFormatla(s.getBosKoltuklar()));
        System.out.println("Dolu koltuklar: " + koltukListesiFormatla(s.getRezerveKoltuklar()));
    }

    private void tumRezervasyonlariListeleCLI() {
        boolean varMi = false;
        System.out.println("--- Tüm Rezervasyonlar ---");
        for (Sefer s : seferler.values()) {
            for (Koltuk k : s.getRezerveKoltuklar()) {
                varMi = true;
                System.out.printf("Sefer %s | Koltuk %02d | Fiyat: %d TL | RezID: %s | Yolcu: %s | Tel: %s | Zaman: %s%n",
                        s.getSeferKimlik(), k.getKoltukNumarasi(), s.getBiletFiyati(),
                        k.getRezervasyonKimlik(),
                        k.getYolcuAdi(),
                        k.getYolcuTelefonu() == null ? "(bilgi yok)" : k.getYolcuTelefonu(),
                        k.getRezervasyonZamani() == null ? "(bilgi yok)" : k.getRezervasyonZamani().format(guzelTarihFormat));
            }
        }
        if (!varMi) System.out.println("Hiç rezervasyon yok.");
    }

    private void ozetGosterCLI() {
        int seferSayisi = seferler.size();
        int toplamGelir = toplamGeliriHesapla();
        System.out.println("=== RAPOR ===");
        System.out.println("Toplam Sefer Sayısı: " + seferSayisi);
        System.out.println("Toplam Gelir: " + toplamGelir + " TL");
        System.out.println("Sefer Bazlı Detaylar:");
        for (Sefer s : seferler.values()) {
            System.out.printf("%s | %s → %s | Fiyat: %d TL | Rezerve: %d | Kap: %d | Doluluk: %4.1f%% | Gelir: %d TL%n",
                    s.getSeferKimlik(), s.getKalkisYeri(), s.getVarisYeri(), s.getBiletFiyati(),
                    s.getRezerveKoltuklar().size(), s.getKapasite(),
                    s.getDolulukOrani(),
                    s.getRezerveKoltuklar().size() * s.getBiletFiyati());
        }
    }

    /* ---------- Yardımcı metotlar ---------- */

    private String kisaRezKimlik(String rezKimlik) {
        if (rezKimlik == null) return "(yok)";
        return rezKimlik.length() <= 8 ? rezKimlik : rezKimlik.substring(0, 8);
    }

    private String koltukListesiFormatla(List<Koltuk> koltuklar) {
        if (koltuklar.isEmpty()) return "(yok)";
        StringBuilder sb = new StringBuilder();
        for (Koltuk k : koltuklar) {
            sb.append(k.getKoltukNumarasi()).append(", ");
        }
        if (sb.length() >= 2) sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    /**
     * Konsola bilet fişi yazar (zengin, okunaklı format).
     */
    private void fisYazdir(Sefer s, Koltuk k) {
        System.out.println("\n=====================================");
        System.out.println("           BİLET FİŞİ / TICKET       ");
        System.out.println("=====================================");
        System.out.printf("Rezervasyon ID : %s%n", k.getRezervasyonKimlik());
        System.out.printf("Yolcu          : %s%n", k.getYolcuAdi());
        System.out.printf("Telefon        : %s%n", k.getYolcuTelefonu() == null ? "(yok)" : k.getYolcuTelefonu());
        System.out.printf("Sefer ID       : %s%n", s.getSeferKimlik());
        System.out.printf("Güzergah       : %s → %s%n", s.getKalkisYeri(), s.getVarisYeri());
        System.out.printf("Kalkış         : %s%n", s.getKalkisZamani().format(guzelTarihFormat));
        System.out.printf("Koltuk No      : %02d%n", k.getKoltukNumarasi());
        System.out.printf("Bilet Fiyatı   : %d TL%n", s.getBiletFiyati());
        System.out.printf("Rezervasyon Zamanı: %s%n", k.getRezervasyonZamani() == null ? "(bilgi yok)" : k.getRezervasyonZamani().format(guzelTarihFormat));
        System.out.println("-------------------------------------");
        System.out.println("NOT: Rezervasyon ID'nizi saklayınız. İptal için bu ID gereklidir.");
        System.out.println("=====================================\n");
    }

    /**
     * Toplam geliri hesapla (tüm seferler)
     */
    private int toplamGeliriHesapla() {
        int toplam = 0;
        for (Sefer s : seferler.values()) {
            toplam += s.getRezerveKoltuklar().size() * s.getBiletFiyati();
        }
        return toplam;
    }

    /**
     * Toplam rezerve koltuk sayısını hesaplar.
     */
    private int toplamRezervasyonlariHesapla() {
        int toplam = 0;
        for (Sefer s : seferler.values()) {
            toplam += s.getRezerveKoltuklar().size();
        }
        return toplam;
    }

    /* ---------- Örnek/seed verisi oluşturma ---------- */

    public void ornekVerileriYukle() {
        // Otobüs kapasitesi 10 olarak sabitlendi.
        final int KAPASITE = 10;

        // 1) Seferleri oluştur (kapasite 10, her birinin fiyatı farklı)
        Sefer s1 = new Sefer("SFR1001", "İstanbul", "Ankara",
                LocalDateTime.now().plusDays(2).withHour(9).withMinute(0), KAPASITE, 550);

        Sefer s2 = new Sefer("SFR1002", "İzmir", "Bursa",
                LocalDateTime.now().plusDays(1).withHour(14).withMinute(0), KAPASITE, 450);

        Sefer s3 = new Sefer("SFR1003", "Antalya", "Konya",
                LocalDateTime.now().plusDays(3).withHour(10).withMinute(30), KAPASITE, 380);

        Sefer s4 = new Sefer("SFR1004", "Kırklareli", "İstanbul",
                LocalDateTime.now().plusDays(1).withHour(8).withMinute(15), KAPASITE, 290);

        Sefer s5 = new Sefer("SFR1005", "Trabzon", "Samsun",
                LocalDateTime.now().plusDays(2).withHour(7).withMinute(45), KAPASITE, 420);

        // Map'e ekle
        seferler.put(s1.getSeferKimlik(), s1);
        seferler.put(s2.getSeferKimlik(), s2);
        seferler.put(s3.getSeferKimlik(), s3);
        seferler.put(s4.getSeferKimlik(), s4);
        seferler.put(s5.getSeferKimlik(), s5);

        // 2) Yolcular (10 kişi)
        String[] yolcular = {
                "Ali Yılmaz", "Zeynep Demir", "Ahmet Kaya", "Ece Yalçın", "Mehmet Aksoy",
                "Furkan Çelik", "Selin Öztürk", "Caner Yücel", "Deniz Şahin", "Gizem Kılıç"
        };
        String[] telefonlar = {
                "05330001111", "05330002222", "05330003333", "05330004444", "05330005555",
                "05330006666", "05330007777", "05330008888", "05330009999", "05330000000"
        };

        // 3) Rezervasyonlar: Toplam 10 rezervasyon (her yolcuya bir koltuk)
        // Sefer 1 (İstanbul-Ankara): 3 kişi rezerve
        Koltuk r1_1 = s1.koltukRezerveEt(1, yolcular[0], telefonlar[0]); if (r1_1 != null) fisYazdir(s1, r1_1);
        Koltuk r1_2 = s1.koltukRezerveEt(2, yolcular[1], telefonlar[1]); if (r1_2 != null) fisYazdir(s1, r1_2);
        Koltuk r1_3 = s1.koltukRezerveEt(3, yolcular[2], telefonlar[2]); if (r1_3 != null) fisYazdir(s1, r1_3);

        // Sefer 2 (İzmir-Bursa): 2 kişi rezerve
        Koltuk r2_1 = s2.koltukRezerveEt(5, yolcular[3], telefonlar[3]); if (r2_1 != null) fisYazdir(s2, r2_1);
        Koltuk r2_2 = s2.koltukRezerveEt(6, yolcular[4], telefonlar[4]); if (r2_2 != null) fisYazdir(s2, r2_2);

        // Sefer 3 (Antalya-Konya): 2 kişi rezerve
        Koltuk r3_1 = s3.koltukRezerveEt(1, yolcular[5], telefonlar[5]); if (r3_1 != null) fisYazdir(s3, r3_1);
        Koltuk r3_2 = s3.koltukRezerveEt(2, yolcular[6], telefonlar[6]); if (r3_2 != null) fisYazdir(s3, r3_2);

        // Sefer 4 (Kırklareli-İstanbul): 2 kişi rezerve
        Koltuk r4_1 = s4.koltukRezerveEt(3, yolcular[7], telefonlar[7]); if (r4_1 != null) fisYazdir(s4, r4_1);
        Koltuk r4_2 = s4.koltukRezerveEt(4, yolcular[8], telefonlar[8]); if (r4_2 != null) fisYazdir(s4, r4_2);

        // Sefer 5 (Trabzon-Samsun): 1 kişi rezerve
        Koltuk r5_1 = s5.koltukRezerveEt(10, yolcular[9], telefonlar[9]); if (r5_1 != null) fisYazdir(s5, r5_1);

        // 4) Başlangıç raporu: toplam sefer sayısı, sefer bazlı doluluk ve gelir
        System.out.println("\n----------------- Başlangıç (Örnek Veri) Raporu -----------------");
        System.out.println("Toplam sefer sayısı: " + seferler.size());
        System.out.println("Toplam yolcu sayısı (örnek veri): " + toplamRezervasyonlariHesapla());

        int toplamGelir = toplamGeliriHesapla();
        System.out.println("Toplam gelir (örnek veri): " + toplamGelir + " TL");
        System.out.println("Sefer detayları:");
        for (Sefer s : seferler.values()) {
            System.out.printf("%s | %s → %s | Fiyat: %d TL | Rezerve: %d | Kap: %d | Doluluk: %4.1f%% | Gelir: %d TL%n",
                    s.getSeferKimlik(), s.getKalkisYeri(), s.getVarisYeri(), s.getBiletFiyati(),
                    s.getRezerveKoltuklar().size(), s.getKapasite(),
                    s.getDolulukOrani(),
                    s.getRezerveKoltuklar().size() * s.getBiletFiyati());
        }
        System.out.println("-------------------------------------------------------------------\n");
    }
}
