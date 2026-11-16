package com.yzioual.model;

import com.yzioual.model.Artifact;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class Hero {
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 20, message = "Name must be 2 to 20 characters")
    @Pattern(regexp = "[A-Za-z0-9 _-]*",
            message = "Name may only contain letters, digits, spaces, '_' and '-'")
    private final String name;

    @NotNull(message = "Hero class is required")
    private final HeroClass heroClass;

    @Min(value = 1, message = "Level must be at least 1")
    private int level;

    @PositiveOrZero(message = "Experience cannot be negative")
    private int experience;

    private Artifact weapon;
    private Artifact armor;
    private Artifact helm;

    private Hero(Builder b) {
        this.name = b.name;
        this.heroClass = b.heroClass;
        this.level = b.level;
        this.experience = b.experience;
        this.weapon = b.weapon;
        this.armor = b.armor;
        this.helm = b.helm;
    }

    public static int xpRequiredForLevel(int level) {
        return level * 1000 + (level - 1) * (level - 1) * 450;
    }

    public int gainExperience(int amount) {
        experience += amount;
        int gained = 0;
        while (experience >= xpRequiredForLevel(level)) {
            level++;
            gained++;
        }
        return gained;
    }

    public void equip(Artifact artifact) {
        switch (artifact.type()) {
            case WEAPON -> weapon = artifact;
            case ARMOR  -> armor = artifact;
            case HELM   -> helm = artifact;
        }
    }

    private static int bonus(Artifact a) { return a == null ? 0 : a.bonus(); }

    public int getAttack()     { return heroClass.attackAt(level) + bonus(weapon); }
    public int getDefense()    { return heroClass.defenseAt(level) + bonus(armor); }
    public int getHitPoints()  { return heroClass.hitPointsAt(level) + bonus(helm); }

    public String getName()          { return name; }
    public HeroClass getHeroClass()  { return heroClass; }
    public int getLevel()            { return level; }
    public int getExperience()       { return experience; }
    public int getNextLevelXp()      { return xpRequiredForLevel(level); }
    public Artifact getWeapon()      { return weapon; }
    public Artifact getArmor()       { return armor; }
    public Artifact getHelm()        { return helm; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private HeroClass heroClass;
        private int level = 1;
        private int experience = 0;
        private Artifact weapon, armor, helm;

        public Builder name(String name)              { this.name = name; return this; }
        public Builder heroClass(HeroClass heroClass) { this.heroClass = heroClass; return this; }
        public Builder level(int level)               { this.level = level; return this; }
        public Builder experience(int experience)     { this.experience = experience; return this; }
        public Builder weapon(Artifact weapon)        { this.weapon = weapon; return this; }
        public Builder armor(Artifact armor)          { this.armor = armor; return this; }
        public Builder helm(Artifact helm)            { this.helm = helm; return this; }

        public Hero build() { return new Hero(this); }
    }
}