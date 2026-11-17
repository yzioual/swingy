package com.yzioual.model;

public enum HeroType {
    SPEARFISHER("Spearfisher", Passive.PERFECT_AIM,
            "Perfect Aim: 15% chance of a double-damage hit",
            13, 8, 110, 3, 2, 12),

    OLD_FISHERMAN("Old Fisherman", Passive.THICK_WETSUIT,
            "Thick Wetsuit: ignores the first hit of each battle",
            10, 11, 125, 2, 3, 14),

    FREE_DIVER("Free Diver", Passive.LAST_BREATH,
            "Last Breath: +30% damage below 50% HP",
            15, 5, 90, 4, 1, 10);

    private final String name;
    private final Passive passive;
    private final String passiveDescription;
    private final int attack, defense, hitPoints;
    private final int attackGrowth, defenseGrowth, hitPointsGrowth;

    HeroType(String name, Passive passive, String passiveDescription,
             int attack, int defense, int hitPoints,
             int attackGrowth, int defenseGrowth, int hitPointsGrowth) {
        this.name = name;
        this.passive = passive;
        this.passiveDescription = passiveDescription;
        this.attack = attack;
        this.defense = defense;
        this.hitPoints = hitPoints;
        this.attackGrowth = attackGrowth;
        this.defenseGrowth = defenseGrowth;
        this.hitPointsGrowth = hitPointsGrowth;
    }

    public int attackAt(int level)    { return attack + (level - 1) * attackGrowth; }
    public int defenseAt(int level)   { return defense + (level - 1) * defenseGrowth; }
    public int hitPointsAt(int level) { return hitPoints + (level - 1) * hitPointsGrowth; }

    public String getName() {
        return name;
    }

    public Passive getPassive() {
        return passive;
    }

    public String getPassiveDescription() {
        return passiveDescription;
    }
}