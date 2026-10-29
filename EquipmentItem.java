import java.awt.Color;

public class EquipmentItem {

    public enum SlotType {
        HELMET("Kask"),
        CHESTPLATE("Göğüslük"),
        LEGGINGS("Pantolon"),
        BOOTS("Çizme"),
        WEAPON("Silah");

        public final String displayName;

        SlotType(String displayName) {
            this.displayName = displayName;
        }
    }

    public enum WeaponType {
        NONE("Ortak Zırh"),
        SWORD("Kılıç (Savaşçı)"),
        DAGGER("Hançer (Suikastçı)"),
        AXE("Balta (Viking)"),
        BOW("Yay (Okçu)"),
        STAFF("Asa (Büyücü)");

        public final String displayName;

        WeaponType(String displayName) {
            this.displayName = displayName;
        }
    }

    public enum Rarity {
        COMMON("Sıradan", new Color(165, 175, 185), 1.0, 80),
        RARE("Nadir", new Color(65, 155, 255), 1.3, 160),
        EPIC("Destansı", new Color(185, 80, 255), 1.65, 320),
        LEGENDARY("Efsanevi", new Color(255, 195, 35), 2.1, 600);

        public final String displayName;
        public final Color color;
        public final double multiplier;
        public final int shopBasePrice;

        Rarity(String displayName, Color color, double multiplier, int shopBasePrice) {
            this.displayName = displayName;
            this.color = color;
            this.multiplier = multiplier;
            this.shopBasePrice = shopBasePrice;
        }
    }

    public final int id;
    public final String name;
    public final SlotType slot;
    public final WeaponType weaponType;
    public final Rarity rarity;
    public final int bonusHp;
    public final int bonusDamage;
    public final int bonusArmor;
    public final double bonusSpeed;
    public final double bonusCrit;

    public EquipmentItem(int id, String name, SlotType slot, WeaponType weaponType, Rarity rarity,
                         int bonusHp, int bonusDamage, int bonusArmor, double bonusSpeed, double bonusCrit) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.weaponType = weaponType;
        this.rarity = rarity;
        this.bonusHp = bonusHp;
        this.bonusDamage = bonusDamage;
        this.bonusArmor = bonusArmor;
        this.bonusSpeed = bonusSpeed;
        this.bonusCrit = bonusCrit;
    }

    public int getHpAtLevel(int level) {
        if (level <= 0) return bonusHp;
        return (int) Math.round(bonusHp * (1.0 + (level - 1) * 0.18));
    }

    public int getDamageAtLevel(int level) {
        if (level <= 0) return bonusDamage;
        return (int) Math.round(bonusDamage * (1.0 + (level - 1) * 0.18));
    }

    public int getArmorAtLevel(int level) {
        if (level <= 0) return bonusArmor;
        return bonusArmor + (bonusArmor > 0 ? (level - 1) : 0);
    }

    public double getSpeedAtLevel(int level) {
        if (level <= 0) return bonusSpeed;
        return bonusSpeed + (bonusSpeed > 0 ? (level - 1) * 0.06 : 0);
    }

    public double getCritAtLevel(int level) {
        if (level <= 0) return bonusCrit;
        return bonusCrit + (bonusCrit > 0 ? (level - 1) * 0.015 : 0);
    }

    // Master Catalog of all collectible equipment & class weapons
    public static final EquipmentItem[] CATALOG = new EquipmentItem[] {
        // --- HELMETS (0..2) ---
        new EquipmentItem(0, "Deri Başlık", SlotType.HELMET, WeaponType.NONE, Rarity.COMMON, 18, 0, 1, 0.0, 0.0),
        new EquipmentItem(1, "Şövalye Miğferi", SlotType.HELMET, WeaponType.NONE, Rarity.RARE, 35, 3, 2, 0.0, 0.02),
        new EquipmentItem(2, "Kraliyet Tacı", SlotType.HELMET, WeaponType.NONE, Rarity.LEGENDARY, 65, 8, 4, 0.15, 0.05),

        // --- CHESTPLATES (3..5) ---
        new EquipmentItem(3, "Zincir Zırh", SlotType.CHESTPLATE, WeaponType.NONE, Rarity.COMMON, 30, 0, 2, 0.0, 0.0),
        new EquipmentItem(4, "Gümüş Göğüslük", SlotType.CHESTPLATE, WeaponType.NONE, Rarity.EPIC, 60, 5, 4, 0.0, 0.0),
        new EquipmentItem(5, "Ejderha Zırhı", SlotType.CHESTPLATE, WeaponType.NONE, Rarity.LEGENDARY, 100, 12, 7, 0.0, 0.04),

        // --- LEGGINGS (6..8) ---
        new EquipmentItem(6, "Kumaş Pantolon", SlotType.LEGGINGS, WeaponType.NONE, Rarity.COMMON, 15, 0, 1, 0.10, 0.0),
        new EquipmentItem(7, "Muhafız Dizliği", SlotType.LEGGINGS, WeaponType.NONE, Rarity.RARE, 32, 2, 3, 0.15, 0.0),
        new EquipmentItem(8, "Gölge Pantolonu", SlotType.LEGGINGS, WeaponType.NONE, Rarity.EPIC, 48, 6, 3, 0.25, 0.04),

        // --- BOOTS (9..11) ---
        new EquipmentItem(9, "Gezgin Çizmesi", SlotType.BOOTS, WeaponType.NONE, Rarity.COMMON, 10, 0, 0, 0.30, 0.0),
        new EquipmentItem(10, "Kanatlı Çizme", SlotType.BOOTS, WeaponType.NONE, Rarity.RARE, 22, 2, 1, 0.50, 0.03),
        new EquipmentItem(11, "Fırtına Adımı", SlotType.BOOTS, WeaponType.NONE, Rarity.LEGENDARY, 45, 6, 2, 0.80, 0.06),

        // --- WARRIOR SWORDS (12..14) ---
        new EquipmentItem(12, "Paslı Kılıç", SlotType.WEAPON, WeaponType.SWORD, Rarity.COMMON, 0, 8, 0, 0.0, 0.02),
        new EquipmentItem(13, "Şövalye Kılıcı", SlotType.WEAPON, WeaponType.SWORD, Rarity.EPIC, 20, 18, 2, 0.0, 0.05),
        new EquipmentItem(14, "Güneşkesen Kılıç", SlotType.WEAPON, WeaponType.SWORD, Rarity.LEGENDARY, 35, 30, 3, 0.20, 0.10),

        // --- ASSASSIN DAGGERS (15..17) ---
        new EquipmentItem(15, "Avcı Hançeri", SlotType.WEAPON, WeaponType.DAGGER, Rarity.COMMON, 0, 6, 0, 0.15, 0.05),
        new EquipmentItem(16, "Zehirli Diş", SlotType.WEAPON, WeaponType.DAGGER, Rarity.RARE, 0, 13, 0, 0.25, 0.10),
        new EquipmentItem(17, "Gece Gölgesi", SlotType.WEAPON, WeaponType.DAGGER, Rarity.LEGENDARY, 15, 24, 0, 0.45, 0.18),

        // --- VIKING AXES (18..20) ---
        new EquipmentItem(18, "Oduncu Baltası", SlotType.WEAPON, WeaponType.AXE, Rarity.COMMON, 10, 11, 0, 0.0, 0.0),
        new EquipmentItem(19, "Kuzey Baltası", SlotType.WEAPON, WeaponType.AXE, Rarity.EPIC, 30, 23, 2, 0.0, 0.05),
        new EquipmentItem(20, "Ragnarok Baltası", SlotType.WEAPON, WeaponType.AXE, Rarity.LEGENDARY, 55, 38, 4, 0.10, 0.10),

        // --- ARCHER BOWS (21..23) ---
        new EquipmentItem(21, "Avcı Yayı", SlotType.WEAPON, WeaponType.BOW, Rarity.COMMON, 0, 7, 0, 0.10, 0.04),
        new EquipmentItem(22, "Elf Yayı", SlotType.WEAPON, WeaponType.BOW, Rarity.RARE, 10, 15, 0, 0.25, 0.08),
        new EquipmentItem(23, "Yıldızoku Yayı", SlotType.WEAPON, WeaponType.BOW, Rarity.LEGENDARY, 25, 27, 1, 0.40, 0.15),

        // --- MAGE STAFFS (24..26) ---
        new EquipmentItem(24, "Çırak Asası", SlotType.WEAPON, WeaponType.STAFF, Rarity.COMMON, 0, 10, 0, 0.0, 0.02),
        new EquipmentItem(25, "Kristal Asa", SlotType.WEAPON, WeaponType.STAFF, Rarity.EPIC, 15, 22, 1, 0.10, 0.06),
        new EquipmentItem(26, "Kaos Hükümdarı", SlotType.WEAPON, WeaponType.STAFF, Rarity.LEGENDARY, 30, 36, 2, 0.20, 0.12)
    };
}

