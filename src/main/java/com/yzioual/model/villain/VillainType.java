package com.yzioual.model.villain;

public enum VillainType {
    THIN_AIR("Thin Air", 6, 3, 40, 1, "Your chest tightens."),
    LEAKING_HULL("Leaking Hull", 10, 5, 70, 2, "The planks groan beneath you."),
    REEF_SHARK("Reef Shark", 15, 9, 110, 3, "A shadow circles below."),
    GREAT_WHITE("Great White", 22, 13, 160, 4, "It smelled the fish.");

    private final String name;
    private final int attack;
    private final int defense;
    private final int hitPoints;
    private final int power;
    private final String catchphrase;

    VillainType(String name, int attack, int defense, int hitPoints, int power,
            String catchphrase) {
        this.name = name;
        this.attack = attack;
        this.defense = defense;
        this.hitPoints = hitPoints;
        this.power = power;
        this.catchphrase = catchphrase;
    }

    public String getName() { return this.name; }
    public int getAttack() { return this.attack; }
    public String getCatchphrase() { return this.catchphrase; }
    public int getDefense() { return this.defense; }
    public int getHitPoints() { return this.hitPoints; }
    public int getPower() { return this.power; }
}
