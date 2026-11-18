package com.yzioual.model.villain;

public record Villain(String name,
                      int attack,
                      int defense,
                      int hitPoints,
                      int power,
                      int XPRewards,
                      String catchphrase
) {
    public static Villain spawn(VillainType type, int heroLevel) {
        int scale = heroLevel - 1;

        return new Villain(
                type.getName(), // name
                type.getAttack() + scale * 2, // attack
                type.getDefense() + scale, // defense
                type.getHitPoints() + scale * 10, //  hit points
                type.getPower(), // power
                type.getPower() * 200 + heroLevel * 100, // xp rewards
                type.getCatchphrase() // catch phrase
        );
    }
}
