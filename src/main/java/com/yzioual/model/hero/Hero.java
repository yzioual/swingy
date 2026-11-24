package com.yzioual.model.hero;

import com.yzioual.model.villain.Villain;
import com.yzioual.model.artifact.Artifact;
import com.yzioual.model.artifact.ArtifactType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class Hero {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 20, message = "Name must be 2 to 20 characters")
    @Pattern(regexp = "[A-Za-z0-9 _-]*", message = "Name may only contain letters, digits, spaces, '_' and '-'")
    private final String name;
    @NotNull(message = "Hero class is required")
    private final HeroType heroType;
    @Min(value = 1, message = "Level must be at least 1")
    private int level = 1;
    @PositiveOrZero(message = "Experience cannot be negative")
    private int experience = 0;
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
    }

    public static Validator getValidator() {
        return VALIDATOR;
    }

    public Set<ConstraintViolation<Hero>> validate() {
        return VALIDATOR.validate(this);
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
    @Min(value = 0, message = "Attack cannot be negative")
    public int getAttack()     { return heroType.attackAt(level) + bonus(weapon); }
    @Min(value = 0, message = "Defense cannot be negative")
    public int getDefense()    { return heroType.defenseAt(level) + bonus(armor); }
    @Min(value = 1, message = "Hit points must be at least 1")
    public int getHitPoints()  { return heroType.hitPointsAt(level) + bonus(helm); }
    public String getName()          { return name; }
    public HeroType getHeroType()  { return heroType; }
    public int getLevel()            { return level; }
    public int getExperience()       { return experience; }
    public int getNextLevelXp()      { return xpRequiredForLevel(level); }
    public Artifact getWeapon()      { return weapon; }
    public Artifact getArmor()       { return armor; }
    public Artifact getHelm()        { return helm; }

    public Artifact getEquipped(ArtifactType type) {
        return switch (type) {
            case WEAPON -> weapon;
            case ARMOR  -> armor;
            case HELM   -> helm;
        };
    }

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

        public HeroBuilder setHeroType(HeroType heroType) {
            this.heroType = heroType;
            return this;
        }

        public HeroBuilder setLevel(int level) {
            this.level = level;
            return this;
        }

        public HeroBuilder setExperience(int experience) {
            this.experience = experience;
            return this;
        }

        public HeroBuilder setWeapon(Artifact weapon) {
            this.weapon = weapon;
            return this;
        }

        public HeroBuilder setArmor(Artifact armor) {
            this.armor = armor;
            return this;
        }

        public HeroBuilder setHelm(Artifact helm) {
            this.helm = helm;
            return this;
        }

        public Set<ConstraintViolation<Hero>> validate() {
            return VALIDATOR.validate(new Hero(this));
        }

        public Hero build() {
            Hero hero = new Hero(this);
            Set<ConstraintViolation<Hero>> violations = VALIDATOR.validate(hero);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
            return hero;
        }
    }
}
