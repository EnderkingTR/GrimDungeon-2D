import java.awt.Color;

public enum HeroClass {
    WARRIOR(
        "Savaşçı",
        "Dengeli yakın dövüşçü. Geniş kılıç darbeleri ve yüksek savunma.",
        135, 28, 4.5, 22, 0.12, 4,
        EquipmentItem.WeaponType.SWORD,
        "Kalkan Darbesi",
        "Etrafındaki düşmanlara şok dalgası vurur, geri iter ve sersemletir.",
        210,
        new Color(65, 145, 255)
    ),
    ASSASSIN(
        "Suikastçı",
        "Aşırı hızlı hareket ve saldırı. Düşük can, ölümcül kritik vuruşlar.",
        82, 21, 5.7, 11, 0.36, 1,
        EquipmentItem.WeaponType.DAGGER,
        "Gölge Adımı",
        "Fare yönüne hızla atılır, içinden geçtiği düşmanları biçer ve 0.6sn hasar almaz.",
        150,
        new Color(185, 70, 255)
    ),
    VIKING(
        "Viking",
        "Yüksek canlı ağır savaşçı. Yavaş ama yıkıcı balta darbeleri.",
        195, 42, 3.8, 32, 0.16, 7,
        EquipmentItem.WeaponType.AXE,
        "Savaş Narası",
        "Yeri sarsarak geniş alanda ağır hasar verir ve 25 Can yeniler.",
        260,
        new Color(235, 95, 45)
    ),
    ARCHER(
        "Okçu",
        "Çevik nişancı. Uzak mesafeden hızlı ve yüksek kritik şanslı oklar atar.",
        95, 25, 5.0, 16, 0.25, 2,
        EquipmentItem.WeaponType.BOW,
        "Yaylım Ateşi",
        "Hedef yöne aynı anda 5 adet delici ok fırlatır.",
        180,
        new Color(60, 215, 110)
    ),
    MAGE(
        "Büyücü",
        "Yıkıcı büyü ustası. Düşük cana karşılık çok yüksek menzilli büyü hasarı.",
        85, 38, 4.2, 26, 0.15, 1,
        EquipmentItem.WeaponType.STAFF,
        "Buz Novası",
        "8 yöne dondurucu büyü küreleri saçar ve yakındaki düşmanları yavaşlatır.",
        220,
        new Color(0, 215, 255)
    );

    public final String displayName;
    public final String description;
    public final int baseHp;
    public final int baseDamage;
    public final double baseSpeed;
    public final int baseAttackCooldown; // Lower = faster attack speed
    public final double baseCritChance;
    public final int baseArmor;
    public final EquipmentItem.WeaponType weaponType;
    public final String skillName;
    public final String skillDescription;
    public final int skillCooldownTicks;
    public final Color themeColor;

    HeroClass(String displayName, String description, int baseHp, int baseDamage, double baseSpeed,
              int baseAttackCooldown, double baseCritChance, int baseArmor,
              EquipmentItem.WeaponType weaponType, String skillName, String skillDescription,
              int skillCooldownTicks, Color themeColor) {
        this.displayName = displayName;
        this.description = description;
        this.baseHp = baseHp;
        this.baseDamage = baseDamage;
        this.baseSpeed = baseSpeed;
        this.baseAttackCooldown = baseAttackCooldown;
        this.baseCritChance = baseCritChance;
        this.baseArmor = baseArmor;
        this.weaponType = weaponType;
        this.skillName = skillName;
        this.skillDescription = skillDescription;
        this.skillCooldownTicks = skillCooldownTicks;
        this.themeColor = themeColor;
    }

    public boolean isMelee() {
        return this == WARRIOR || this == ASSASSIN || this == VIKING;
    }
}

