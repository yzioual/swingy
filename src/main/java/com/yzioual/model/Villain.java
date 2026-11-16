package com.yzioual.model.villain;

public enum Villaintype {

}

public record Villain(
        String name,
        int attack,
        int defense,
        int hitPoints,
        int power,
        int XPReward,
        String catchphrase) {

    public static Villain spawn() {
        return new Villain();
    }
}
