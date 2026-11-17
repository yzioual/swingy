package com.yzioual.model.hero;

import com.yzioual.controller.BattleSimulator;
import com.yzioual.model.villain.Villain;
import com.yzioual.model.artifact.Artifact;
import com.yzioual.model.map.Map;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class Hero {
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 20, message = "Name must be 2 to 20 characters")
    @Pattern(regexp = "[A-Za-z0-9 _-]*", message = "Name may only contain letters, digits, spaces, '_' and '-'")
    private final String name;
    @NotNull(message = "Hero class is required")
    private final HeroType heroType;
    @Min(value = 1, message = "Level must be at least 1")
    private int level;
    @PositiveOrZero(message = "Experience cannot be negative")
    private int experience;
    private Artifact weapon;
    private Artifact armor;
    private Artifact helm;

    private Hero(HeroBuilder heroBuilder) {
        this.name = heroBuilder.name;
        this.heroType = heroBuilder.heroType;
        this.level = heroBuilder.level;
        this.experience = heroBuilder.experience;
        this.weapon = heroBuilder.weapon;
        this.armor = heroBuilder.armor;
        this.helm = heroBuilder.helm;
        System.out.println("[DEBUG] Hero built: " + name + " [Type=" + heroType + ", Level=" + level + ", Exp=" + experience + ", HP=" + getHitPoints() + ", ATK=" + getAttack() + ", DEF=" + getDefense() + "]");
    }

    public static int xpRequiredForLevel(int level) {
        return level * 1000 + (level - 1) * (level - 1) * 450;
    }

    public int gainExperience(int amount) {
        System.out.println("[DEBUG] Hero.gainExperience: " + name + " gaining " + amount + " XP (current XP: " + experience + ", level: " + level + ")");
        experience += amount;
        int gained = 0;
        while (experience >= xpRequiredForLevel(level)) {
            level++;
            gained++;
            System.out.println("[DEBUG] Hero.gainExperience: Level up! New level: " + level + ", Next level XP threshold: " + xpRequiredForLevel(level));
        }
        return gained;
    }

    public void equip(Artifact artifact) {
        System.out.println("[DEBUG] Hero.equip: Equipping " + artifact);
        switch (artifact.type()) {
            case WEAPON -> weapon = artifact;
            case ARMOR  -> armor = artifact;
            case HELM   -> helm = artifact;
        }
    }

    public void print() {
        System.out.printf("%s, %s   Lv %d   XP %d/%d%n",
                name,
                heroType.getName(),
                level,
                experience,
                getNextLevelXp()
        );
        System.out.printf("ATK %d   DEF %d   HP %d   Oxygen: %d%n",
                getAttack(),
                getDefense(),
                getHitPoints(),
                Map.sizeForLevel(getLevel()) * 3
        );
    }

    public void fight(Villain shark) throws InterruptedException {
        int heroHp = getHitPoints();
        int sharkHp = shark.hitPoints();
        int heroMax = heroHp;
        int sharkMax = sharkHp;

        System.out.println("[DEBUG] Hero.fight: Starting battle - " + getName() + " (HP: " + heroHp + ") vs " + shark.name() + " (HP: " + sharkHp + ")");
        System.out.println("\n--- BATTLE: " + getName() + " vs " + shark.name() + " ---");
        for (int i = 0; sharkHp > 0 && heroHp > 0; i++) {
            System.out.println("\nRound " + (i + 1));
            int dealt = Math.min(BattleSimulator.HERO_HITS[i], sharkHp);
            sharkHp -= dealt;
            System.out.println("[DEBUG] Round " + (i + 1) + ": Hero dealt " + dealt + " damage -> " + shark.name() + " HP remaining: " + sharkHp);
            BattleSimulator.pause();
            System.out.println("  You thrust your spear.  " + shark.name() + " takes " + dealt + ".");

            if (sharkHp <= 0) {
                System.out.println("[DEBUG] Hero.fight: " + shark.name() + " defeated.");
                break;
            }

            int taken = BattleSimulator.SHARK_HITS[i];
            if (taken == 0) {
                System.out.println("  " + shark.name() + " lunges... and misses!");
            } else {
                heroHp -= taken;
                System.out.println("  " + shark.name() + " bites.  You take " + taken + ".");
            }
            System.out.println("[DEBUG] Round " + (i + 1) + ": " + shark.name() + " dealt " + taken + " damage -> Hero HP remaining: " + heroHp);
            if (heroHp < heroMax / 2) {
                System.out.println("  Last Breath! Your lungs burn, but your arms are stronger.");
            }
            System.out.println("  " + getName() + "  " + BattleSimulator.bar(heroHp, heroMax));
            System.out.println("  " + shark.name() + "  " + BattleSimulator.bar(sharkHp, sharkMax));
            BattleSimulator.pause();
        }
        System.out.println("\n  " + shark.name() + " sinks into the dark.");
        System.out.println("[DEBUG] Hero.fight: Battle ended. Final Hero HP=" + heroHp + ", Final Villain HP=" + sharkHp);
    }

    private static int bonus(Artifact a) { return a == null ? 0 : a.bonus(); }
    public int getAttack()     { return heroType.attackAt(level) + bonus(weapon); }
    public int getDefense()    { return heroType.defenseAt(level) + bonus(armor); }
    public int getHitPoints()  { return heroType.hitPointsAt(level) + bonus(helm); }
    public String getName()          { return name; }
    public HeroType getHeroClass()  { return heroType; }
    public int getLevel()            { return level; }
    public int getExperience()       { return experience; }
    public int getNextLevelXp()      { return xpRequiredForLevel(level); }
    public Artifact getWeapon()      { return weapon; }
    public Artifact getArmor()       { return armor; }
    public Artifact getHelm()        { return helm; }

    public static class HeroBuilder {
        private String name;
        private HeroType heroType;
        private int level = 1;
        private int experience = 0;
        private Artifact weapon, armor, helm;

        public HeroBuilder() {}

        public HeroBuilder setName(String name)              {
            this.name = name;
            return this;
        }

        public HeroBuilder setHeroClass(HeroType heroType) {
            this.heroType = heroType;
            return this;
        }

        public HeroBuilder setLevel(int level)               { this.level = level; return this; }
        public HeroBuilder setExperience(int experience)     { this.experience = experience; return this; }
        public HeroBuilder setWeapon(Artifact weapon)        { this.weapon = weapon; return this; }
        public HeroBuilder setArmor(Artifact armor)          { this.armor = armor; return this; }
        public HeroBuilder setHelm(Artifact helm)            { this.helm = helm; return this; }

        public Hero build() {
            return new Hero(this);
        }
    }
}