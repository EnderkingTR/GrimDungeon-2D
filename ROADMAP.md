# GrimDungeon 2D - Geliştirme Yol Haritası

## Faz 1: Çekirdek Motor (Core Architecture) - [MEVCUT DURUM]
- [x] **Game Loop**: Sabit 60 UPS ve Delta Time / Tick hesabı
- [x] **Rendering**: Triple Buffering ve 2D Kamera (Viewport + LERP yumuşak takip)
- [x] **Input**: Klavye dinleyicisi (W-A-S-D ve F3 debug toggle)
- **Çıktı (Milestone)**: Ekranda yırtılma olmadan akıcı hareket eden kamera ve kontrol edilebilir temel mavi nesne.

---

## Faz 2: Dünya, Harita ve Fizik (World & Physics)
- [x] **Tilemap Sistemi**: 2B matrislerle harita katmanları
- [x] **Prosedürel Üretim**: Cellular Automata veya BSP ile rastgele zindan üretimi
- [x] **Çarpışma**: AABB (Axis-Aligned Bounding Box) çarpışma testi
- **Çıktı (Milestone)**: Her çalıştırıldığında farklı oluşan, duvarlarından geçilemeyen etkileşimli bir harita.

---

## Faz 3: Varlık Yönetimi (Entity Architecture)
- [x] **Mimari Tasarım**: Kalıtım yerine Bileşen Tabanlı Mimari (ECS Lite)
- [x] **Durum Yönetimi**: Karakter statları, envanter ve aksiyon-macera mekanikleri
- [x] **Sprite & Animasyon**: Kare (Frame) bazlı durum animasyonları
- **Çıktı (Milestone)**: Oyuncu, düşman, tuzak ve sandık gibi objelerin modüler olarak dünyaya eklenmesi.

---

## Faz 4: Strateji ve Yapay Zeka (AI & Pathfinding) - [MEVCUT DURUM]
- [x] **Yol Bulma**: Engellerin etrafından dolaşan A* (A-Star) algoritması
- [x] **Karar Mekanizması**: Finite State Machine (FSM) (Devriye, Takip, Saldırı, Kaçış)
- [x] **Görüş Alanı**: Raycasting veya Bresenham algoritması ile Fog of War (Savaş Sisi)
- **Çıktı (Milestone)**: Oyuncuyu gördüğünde stratejik olarak en kısa yoldan kuşatan, görmediğinde devriye gezen zeki düşmanlar.

---

## Faz 5: Savaş, Arayüz ve Cila (Combat & UI) - [TAMAMLANDI]
- [x] **Savaş Sistemi**: Menzil, kılıç savurma (Hitbox), büyü mermileri (Projectile), partikül ve ekran sarsıntısı (Screen Shake)
- [x] **Hata Ayıklama & Arayüz**: HUD, gerçek zamanlı minimap, FPS/UPS/Partikül sayacı ve durum bildirimleri
- [x] **Veri Kaydı**: Binary serileştirme ile oyun kaydetme ve yükleme (savegame.dat - [F5] Kaydet / [F9] Yükle)
- **Çıktı (Milestone)**: Oynanabilir, stratejik derinliği olan, zindan atmosferine ve motor içi analiz araçlarına sahip tam sürüm prototip.
