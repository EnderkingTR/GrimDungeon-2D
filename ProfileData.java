import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProfileData {
    private static final int SAVE_MAGIC = 0x4752494D; // "GRIM"
    private static final int SAVE_VERSION = 2;

    public static final int MAX_STAGES = 15;

    public int gold = 250;
    public int victoryChests = 1; // Start with 1 welcome chest so the player can test opening right away!
    public int unlockedStage = 1;
    public int selectedStage = 1;

    public HeroClass selectedHero = HeroClass.WARRIOR;

    public final int[] heroLevels = new int[HeroClass.values().length];
    public final int[] heroCards = new int[HeroClass.values().length];
    public final int[] skillLevels = new int[HeroClass.values().length];

    public final int[] itemLevels = new int[EquipmentItem.CATALOG.length];
    public final int[] itemCards = new int[EquipmentItem.CATALOG.length];

    // Equipped armor IDs (-1 = none): [0]=HELMET, [1]=CHESTPLATE, [2]=LEGGINGS, [3]=BOOTS
    public final int[] equippedArmor = new int[] { 0, 3, 6, 9 };
    // Equipped weapon ID per hero class
    public final int[] equippedWeaponByHero = new int[] { 12, 15, 18, 21, 24 };

    public static class ComputedStats {
        public int maxHp;
        public int damage;
        public double speed;
        public int attackCooldown;
        public double critChance;
        public int armor;
        public int skillCooldown;
        public double skillPowerMultiplier;
    }

    public static class CardReward {
        public final boolean isHeroCard;
        public final HeroClass hero;
        public final EquipmentItem item;
        public final int count;
        public final String title;
        public final String subtitle;
        public final Color rarityColor;

        public CardReward(HeroClass hero, int count) {
            this.isHeroCard = true;
            this.hero = hero;
            this.item = null;
            this.count = count;
            this.title = hero.displayName + " Kartı";
            this.subtitle = "Karakter Gelişim Kartı (x" + count + ")";
            this.rarityColor = hero.themeColor;
        }

        public CardReward(EquipmentItem item, int count) {
            this.isHeroCard = false;
            this.hero = null;
            this.item = item;
            this.count = count;
            this.title = item.name;
            this.subtitle = item.rarity.displayName + " " + item.slot.displayName + " (x" + count + ")";
            this.rarityColor = item.rarity.color;
        }
    }

    public static class ChestOpenResult {
        public final int goldReward;
        public final List<CardReward> cards = new ArrayList<>();

        public ChestOpenResult(int goldReward) {
            this.goldReward = goldReward;
        }
    }

    public ProfileData() {
        resetDefaults();
    }

    public void resetDefaults() {
        gold = 250;
        victoryChests = 1;
        unlockedStage = 1;
        selectedStage = 1;
        selectedHero = HeroClass.WARRIOR;

        for (int i = 0; i < HeroClass.values().length; i++) {
            heroLevels[i] = 1;
            heroCards[i] = 2;
            skillLevels[i] = 1;
        }

        for (int i = 0; i < EquipmentItem.CATALOG.length; i++) {
            EquipmentItem item = EquipmentItem.CATALOG[i];
            if (item.rarity == EquipmentItem.Rarity.COMMON) {
                itemLevels[i] = 1;
                itemCards[i] = 1;
            } else {
                itemLevels[i] = 0;
                itemCards[i] = 0;
            }
        }

        equippedArmor[0] = 0; // Deri Başlık
        equippedArmor[1] = 3; // Zincir Zırh
        equippedArmor[2] = 6; // Kumaş Pantolon
        equippedArmor[3] = 9; // Gezgin Çizmesi

        equippedWeaponByHero[HeroClass.WARRIOR.ordinal()] = 12;
        equippedWeaponByHero[HeroClass.ASSASSIN.ordinal()] = 15;
        equippedWeaponByHero[HeroClass.VIKING.ordinal()] = 18;
        equippedWeaponByHero[HeroClass.ARCHER.ordinal()] = 21;
        equippedWeaponByHero[HeroClass.MAGE.ordinal()] = 24;
    }

    public ComputedStats computeStats(HeroClass hero) {
        ComputedStats s = new ComputedStats();
        int hIdx = hero.ordinal();
        int hLvl = Math.max(1, heroLevels[hIdx]);
        int skLvl = Math.max(1, skillLevels[hIdx]);

        // Hero level scaling (+12% HP and Damage per hero level)
        double heroMult = 1.0 + (hLvl - 1) * 0.12;
        s.maxHp = (int) Math.round(hero.baseHp * heroMult);
        s.damage = (int) Math.round(hero.baseDamage * heroMult);
        s.speed = hero.baseSpeed + (hLvl - 1) * 0.05;
        s.attackCooldown = Math.max(6, hero.baseAttackCooldown - (hLvl / 3));
        s.critChance = hero.baseCritChance + (hLvl - 1) * 0.01;
        s.armor = hero.baseArmor + (hLvl - 1);

        // Add Common Armor bonuses
        for (int slot = 0; slot < 4; slot++) {
            int itemId = equippedArmor[slot];
            if (itemId >= 0 && itemId < EquipmentItem.CATALOG.length) {
                EquipmentItem item = EquipmentItem.CATALOG[itemId];
                int lvl = Math.max(1, itemLevels[itemId]);
                s.maxHp += item.getHpAtLevel(lvl);
                s.damage += item.getDamageAtLevel(lvl);
                s.armor += item.getArmorAtLevel(lvl);
                s.speed += item.getSpeedAtLevel(lvl);
                s.critChance += item.getCritAtLevel(lvl);
            }
        }

        // Add Class Weapon bonuses
        int weaponId = equippedWeaponByHero[hIdx];
        if (weaponId >= 0 && weaponId < EquipmentItem.CATALOG.length) {
            EquipmentItem w = EquipmentItem.CATALOG[weaponId];
            int lvl = Math.max(1, itemLevels[weaponId]);
            s.maxHp += w.getHpAtLevel(lvl);
            s.damage += w.getDamageAtLevel(lvl);
            s.armor += w.getArmorAtLevel(lvl);
            s.speed += w.getSpeedAtLevel(lvl);
            s.critChance += w.getCritAtLevel(lvl);
        }

        // Skill scaling
        s.skillCooldown = Math.max(60, hero.skillCooldownTicks - (skLvl - 1) * 12);
        s.skillPowerMultiplier = 1.0 + (skLvl - 1) * 0.20;
        s.critChance = Math.min(0.85, s.critChance);
        s.speed = Math.min(7.8, s.speed);

        return s;
    }

    public int getHeroUpgradeCardCost(HeroClass hero) {
        int lvl = heroLevels[hero.ordinal()];
        return 3 + (lvl - 1) * 2;
    }

    public int getHeroUpgradeGoldCost(HeroClass hero) {
        int lvl = heroLevels[hero.ordinal()];
        return 100 * lvl;
    }

    public boolean canUpgradeHero(HeroClass hero) {
        int idx = hero.ordinal();
        return heroCards[idx] >= getHeroUpgradeCardCost(hero) && gold >= getHeroUpgradeGoldCost(hero);
    }

    public boolean upgradeHero(HeroClass hero) {
        if (!canUpgradeHero(hero)) return false;
        int idx = hero.ordinal();
        gold -= getHeroUpgradeGoldCost(hero);
        heroCards[idx] -= getHeroUpgradeCardCost(hero);
        heroLevels[idx]++;
        save();
        return true;
    }

    public int getItemUpgradeCardCost(int itemId) {
        int lvl = itemLevels[itemId];
        return lvl == 0 ? 1 : (2 + lvl);
    }

    public int getItemUpgradeGoldCost(int itemId) {
        int lvl = Math.max(1, itemLevels[itemId]);
        EquipmentItem item = EquipmentItem.CATALOG[itemId];
        return (int) (60 * lvl * item.rarity.multiplier);
    }

    public boolean canUpgradeItem(int itemId) {
        if (itemLevels[itemId] == 0) {
            return itemCards[itemId] >= 1;
        }
        return itemCards[itemId] >= getItemUpgradeCardCost(itemId) && gold >= getItemUpgradeGoldCost(itemId);
    }

    public boolean upgradeItem(int itemId) {
        if (itemLevels[itemId] == 0) {
            if (itemCards[itemId] >= 1) {
                itemCards[itemId] -= 1;
                itemLevels[itemId] = 1;
                save();
                return true;
            }
            return false;
        }
        if (!canUpgradeItem(itemId)) return false;
        gold -= getItemUpgradeGoldCost(itemId);
        itemCards[itemId] -= getItemUpgradeCardCost(itemId);
        itemLevels[itemId]++;
        save();
        return true;
    }

    public int getSkillUpgradeGoldCost(HeroClass hero) {
        int lvl = skillLevels[hero.ordinal()];
        return 120 * lvl;
    }

    public boolean upgradeSkill(HeroClass hero) {
        int cost = getSkillUpgradeGoldCost(hero);
        if (gold < cost) return false;
        gold -= cost;
        skillLevels[hero.ordinal()]++;
        save();
        return true;
    }

    public void equipItem(int itemId) {
        if (itemId < 0 || itemId >= EquipmentItem.CATALOG.length) return;
        if (itemLevels[itemId] <= 0) return; // Must be unlocked
        EquipmentItem item = EquipmentItem.CATALOG[itemId];
        switch (item.slot) {
            case HELMET -> equippedArmor[0] = itemId;
            case CHESTPLATE -> equippedArmor[1] = itemId;
            case LEGGINGS -> equippedArmor[2] = itemId;
            case BOOTS -> equippedArmor[3] = itemId;
            case WEAPON -> {
                for (HeroClass hc : HeroClass.values()) {
                    if (hc.weaponType == item.weaponType) {
                        equippedWeaponByHero[hc.ordinal()] = itemId;
                    }
                }
            }
        }
        save();
    }

    public boolean isEquipped(int itemId) {
        for (int a : equippedArmor) {
            if (a == itemId) return true;
        }
        int wId = equippedWeaponByHero[selectedHero.ordinal()];
        return wId == itemId;
    }

    public ChestOpenResult openVictoryChest(Random random) {
        if (victoryChests <= 0) return null;
        victoryChests--;

        int goldGain = 65 + random.nextInt(60) + (unlockedStage * 12);
        this.gold += goldGain;

        ChestOpenResult result = new ChestOpenResult(goldGain);

        // 1st Card: Always Hero Card (1 to 3 copies)
        HeroClass randomHero = HeroClass.values()[random.nextInt(HeroClass.values().length)];
        int heroCardCount = 1 + random.nextInt(3);
        heroCards[randomHero.ordinal()] += heroCardCount;
        result.cards.add(new CardReward(randomHero, heroCardCount));

        // 2nd & 3rd Cards: Equipment or Weapon Cards weighted by Rarity
        for (int i = 0; i < 2; i++) {
            EquipmentItem picked = rollEquipmentByRarity(random);
            int count = 1 + (picked.rarity == EquipmentItem.Rarity.COMMON ? random.nextInt(3) : random.nextInt(2));
            itemCards[picked.id] += count;
            if (itemLevels[picked.id] == 0) {
                itemLevels[picked.id] = 1; // Unlock item immediately when first found!
            }
            result.cards.add(new CardReward(picked, count));
        }

        save();
        return result;
    }

    private EquipmentItem rollEquipmentByRarity(Random random) {
        double roll = random.nextDouble();
        EquipmentItem.Rarity targetRarity;
        if (roll < 0.50) {
            targetRarity = EquipmentItem.Rarity.COMMON;
        } else if (roll < 0.80) {
            targetRarity = EquipmentItem.Rarity.RARE;
        } else if (roll < 0.94) {
            targetRarity = EquipmentItem.Rarity.EPIC;
        } else {
            targetRarity = EquipmentItem.Rarity.LEGENDARY;
        }

        List<EquipmentItem> pool = new ArrayList<>();
        for (EquipmentItem item : EquipmentItem.CATALOG) {
            if (item.rarity == targetRarity) {
                pool.add(item);
            }
        }
        if (pool.isEmpty()) {
            return EquipmentItem.CATALOG[random.nextInt(EquipmentItem.CATALOG.length)];
        }
        return pool.get(random.nextInt(pool.size()));
    }

    public void save() {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream("savegame.dat"))) {
            out.writeInt(SAVE_MAGIC);
            out.writeInt(SAVE_VERSION);
            out.writeInt(gold);
            out.writeInt(victoryChests);
            out.writeInt(unlockedStage);
            out.writeInt(selectedStage);
            out.writeInt(selectedHero.ordinal());

            for (int i = 0; i < HeroClass.values().length; i++) {
                out.writeInt(heroLevels[i]);
                out.writeInt(heroCards[i]);
                out.writeInt(skillLevels[i]);
                out.writeInt(equippedWeaponByHero[i]);
            }

            for (int i = 0; i < 4; i++) {
                out.writeInt(equippedArmor[i]);
            }

            for (int i = 0; i < EquipmentItem.CATALOG.length; i++) {
                out.writeInt(itemLevels[i]);
                out.writeInt(itemCards[i]);
            }
        } catch (IOException ignored) {
        }
    }

    public boolean load() {
        File file = new File("savegame.dat");
        if (!file.exists()) return false;
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            int magic = in.readInt();
            if (magic != SAVE_MAGIC) {
                return false; // Legacy save format, keep fresh profile defaults
            }
            int version = in.readInt();
            if (version != SAVE_VERSION) {
                return false;
            }
            gold = in.readInt();
            victoryChests = in.readInt();
            unlockedStage = Math.max(1, Math.min(MAX_STAGES, in.readInt()));
            selectedStage = Math.max(1, Math.min(unlockedStage, in.readInt()));
            int heroOrd = in.readInt();
            if (heroOrd >= 0 && heroOrd < HeroClass.values().length) {
                selectedHero = HeroClass.values()[heroOrd];
            }

            for (int i = 0; i < HeroClass.values().length; i++) {
                heroLevels[i] = in.readInt();
                heroCards[i] = in.readInt();
                skillLevels[i] = in.readInt();
                equippedWeaponByHero[i] = in.readInt();
            }

            for (int i = 0; i < 4; i++) {
                equippedArmor[i] = in.readInt();
            }

            for (int i = 0; i < EquipmentItem.CATALOG.length; i++) {
                itemLevels[i] = in.readInt();
                itemCards[i] = in.readInt();
            }
            return true;
        } catch (IOException e) {
            resetDefaults();
            return false;
        }
    }
}

