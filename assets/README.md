# 🎨🎵 GrimDungeon 2D — Asset Rehberi (Görsel & Ses Dosyaları)

Oyun motoru **Hibrit (Prosedürel + Harici Dosya)** mimarisiyle çalışır:
- Aşağıdaki klasörlere kendi indirdiğiniz `.png` veya `.wav` dosyalarını koyduğunuz anda motor bunları **otomatik olarak algılar ve kullanır**.
- Herhangi bir dosya eksikse motor çökmez; kendi dahili prosedürel çizimini ve 16-bit ses sentezleyicisini kullanmaya devam eder.

---

## 🖼️ 1. Görsel Sprite Dosyaları (`assets/sprites/`)
İndirdiğiniz (Örn: *Kenney.nl*, *itch.io*, *OpenGameArt*) `.png` görsellerini şu isimlerle `assets/sprites/` klasörüne atabilirsiniz:

| Dosya Adı | Açıklama | Önerilen Boyut |
| :--- | :--- | :--- |
| `warrior.png` | Savaşçı karakter görseli | 32x32 veya 48x48 |
| `assassin.png` | Suikastçı karakter görseli | 32x32 veya 48x48 |
| `viking.png` | Viking karakter görseli | 32x32 veya 48x48 |
| `archer.png` | Okçu karakter görseli | 32x32 veya 48x48 |
| `mage.png` | Büyücü karakter görseli | 32x32 veya 48x48 |
| `enemy_melee.png` | Yakın dövüşçü düşman | 32x32 |
| `enemy_ranged.png` | Menzilli büyücü/okçu düşman | 32x32 |
| `enemy_swarm.png` | Sürü (Swarm) minik düşman | 18x18 veya 24x24 |
| `turret.png` | Kristal Taret kulesi | 34x34 |
| `chest.png` | Kapalı hazine sandığı | 32x26 |
| `chest_open.png` | Açılmış hazine sandığı | 32x26 |
| `floor.png` | Zindan zemin karosu | 48x48 |
| `wall.png` | Zindan duvar karosu | 48x48 |

---

## 🔊 2. Ses Efektleri (`assets/audio/sfx/`) ve Müzik (`assets/audio/music/`)
Standart **16-bit PCM `.wav`** formatındaki ses dosyalarını şu isimlerle yerleştirebilirsiniz:

### Ses Efektleri (`assets/audio/sfx/`):
- `swing.wav` — Kılıç / Balta / Hançer savurma sesi
- `shoot.wav` — Ok / Büyü ateşleme sesi
- `hit.wav` — Düşmana isabet sesi
- `crit.wav` — Kritik vuruş sesi
- `hurt.wav` — Karakterin hasar alma sesi
- `kill.wav` — Düşman ölüm sesi
- `coin.wav` — Altın / Ödül kazanma sesi
- `skill.wav` — Sınıf özel yetenek (`[Sağ Tık / Q]`) sesi
- `chest.wav` — Sandık açma sesi
- `victory.wav` — Bölüm geçme (Görev Başarılı) müziği/efekti
- `gameover.wav` — Kaybetme sesi
- `click.wav` — Menü buton tıklama sesi

### Arka Plan Müziği (`assets/audio/music/`):
- `bgm.wav` — Oyun boyunca döngüde (loop) çalacak zindan müziği.

