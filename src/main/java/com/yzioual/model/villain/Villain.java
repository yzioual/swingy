package com.yzioual.model.villain;

public record Villain(String name, int attack, int defense, int hitPoints, int power, int XPRewards, String catchphrase
) {
    public static Villain spawn(VillainType type, int heroLevel) {
        int scale = heroLevel - 1;

        return new Villain(
                type.getName(),
                type.getAttack() + scale * 2,
                type.getDefense() + scale,
                type.getHitPoints() + scale * 10,
                type.getPower(),
                type.getPower() * 200 + heroLevel * 100,
                type.getCatchphrase()
        );
    }
}