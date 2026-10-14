# ⚔️ GrimDungeon 2D

> Sıfırdan Java ile geliştirilmiş, prosedürel zindan üretimine ve zeki yapay zekaya sahip 2D Rogue-like Aksiyon & Strateji Oyun Motoru.

Bu proje, **Gemini Argon 4** modelinin mimari ve oyun motoru tasarım yeteneklerini test etmek ve sıfırdan harici kütüphane kullanmadan saf Java (AWT & Swing) ile çalışan modüler bir oyun motoru inşa etmek amacıyla geliştirilmiştir.

---

## 🎮 Oynanış ve Kontroller

| Tuş | Eylem |
| :--- | :--- |
| **W, A, S, D** / Yön Tuşları | Karakter Hareketi (LERP Kameralı Yumuşak Takip) |
| **Space** | Yakın Dövüş: Geniş Yaylı Kılıç Saldırısı (Geri Savurma / Knockback) |
| **Sol Tık** | Menzilli Saldırı: Fare Yönüne Büyü / Mermi Ateşleme (Projectile) |
| **E** | Etkileşim: Zindan Sandıklarını Açma (+Altın & +25 Can) |
| **F3** | Hata Ayıklama (Debug Paneli, Düşman AI Rotaları & Tam Harita) |
| **F5** | Hızlı Kayıt (`savegame.dat` dosyasına durum serileştirme) |
| **F9** | Hızlı Yükleme (Kayıtlı oyunu diskten geri yükleme) |
| **R** | Prosedürel Zindanı Yeniden Üret (Restart) |

---

## 🛠️ Motorun Mimari Özellikleri

1. **Çekirdek Motor (Core Engine):**
   - Sabit 60 UPS (Updates Per Second) Game Loop ve Delta Time yönetimi.
   - Ekranda yırtılma (tearing) olmasını engelleyen Triple-Buffering.
   - Ekran Sarsıntısı (Screen Shake) ve LERP takip kameralı Viewport.

2. **Dünya & Harita Sistemi:**
   - Rastgele oda ve koridorlar bağlayan **Prosedürel Zindan Üreticisi** (Procedural Dungeon Generator).
   - Karo tabanlı (Tilemap) matris mimarisi ve zemin/duvar ayrımı.
   - Eksen Hizalı Sınırlayıcı Kutu (**AABB Collision**) ile kusursuz fizik ve çarpışma kontrolü.

3. **Strateji, Yapay Zeka & Savaş Sisi (AI & Fog of War):**
   - **A\* (A-Star) Algoritması:** Düşmanlar engellerin etrafından en kısa yolla dolaşarak oyuncuyu takip eder.
   - **Sonlu Durum Makinesi (FSM):** Düşmanlar `PATROL` (Devriye), `CHASE` (Kovalama) ve `FLEE` (Düşük canda kaçış) durumları arasında dinamik geçiş yapar.
   - **Bresenham Işın İzleme (Line of Sight):** Oyuncunun görmediği odalar ve sis altındaki düşmanlar dinamik savaş sisi ile gizlenir.

4. **Savaş, Efektler & Arayüz:**
   - 360 derece partikül sistemi ve seken kıvılcımlar.
   - Gerçek zamanlı karo minimap'i ve keşfedilen alan takibi.
   - Binary serileştirme ile dosya tabanlı kayıt (Save/Load) motoru.

---

## 🚀 Projeyi Çalıştırma

Bilgisayarınızda **Java (JDK 17 veya üzeri)** kurulu olması yeterlidir.

### Windows (Tek Tıkla):
Klasör içerisindeki `run.bat` dosyasına çift tıklayarak doğrudan derleyip başlatabilirsiniz.

### Terminalden:
```bash
# Kodları derle
javac -encoding UTF-8 -d bin *.java

# Oyunu başlat
java -cp bin Main
```

---

*Geliştirme: Gemini Argon 4 & Antigravity IDE*
