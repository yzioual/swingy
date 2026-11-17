package com.yzioual.controller;

import com.yzioual.model.Hero;
import com.yzioual.model.HeroType;
import com.yzioual.model.Villain;
import com.yzioual.model.VillainType;
import com.yzioual.model.maps.Map;
import com.yzioual.model.maps.Position;

import java.util.Random;
import java.util.Set;

public class BattleSimulator {
    private final Hero hero;
    private Map map;
    private Villain villain;

    public static  final int seed = 3;
    public static final int VILLAIN_PERCENT = 12;
    public static final int[] HERO_HITS  = {14, 11, 16, 12, 15, 13, 14, 17};
    public static final int[] SHARK_HITS = {10, 13, 0, 12, 11, 9, 14};
    private static final String[] DEPTHS = {
            "The Shallow Reef",
            "The Kelp Forest",
            "The Sunken Wreck",
            "The Twilight Zone",
            "The Abyssal Trench",
            "The Black Deep"
    };

    public BattleSimulator() {
        System.out.println("[DEBUG] Initializing BattleSimulator...");
        hero = new Hero.HeroBuilder()
                .setName("Ali")
                .setHeroClass(HeroType.SPEARFISHER)
                .build();
        System.out.println("[DEBUG] Hero initialized: " + hero.getName() + " (" + hero.getHeroClass() + ")");
        villain = Villain.spawn(VillainType.THIN_AIR, hero.getLevel());
        System.out.println("[DEBUG] Villain spawned: " + villain.name() + " [HP=" + villain.hitPoints() + ", ATK=" + villain.attack() + ", DEF=" + villain.defense() + "]");
        map = new Map(hero.getLevel());
        System.out.println("[DEBUG] Map created with size " + map.getSize() + " for hero level " + hero.getLevel());
        System.out.println("[DEBUG] Placing villains on map...");
        placeVillains();
        System.out.println("[DEBUG] Finished placing villains. Total villains: " + map.getVillainCells().size());
    }

    private void placeVillains() {
        Random random = new Random(seed);
        Position center =  new Position(map.getSize() / 2, map.getSize() / 2);
        int count = Math.max(1, map.getSize() * 2 * VILLAIN_PERCENT / 100);
        System.out.println("[DEBUG] placeVillains: Target count=" + count + ", Center=" + center + ", Current villain count=" + map.getVillainCells().size());

        while (map.getVillainCells().size() < count) {
            Position p = new Position(
                    random.nextInt(map.getSize()),
                    random.nextInt(map.getSize())
            );
            System.out.println("[DEBUG] placeVillains: Generated candidate position " + p);

            if (!p.equals(center)) {
                System.out.println("[DEBUG] placeVillains: Attempting to add villain at " + p + " via map.getVillainCells().add()...");
                map.addVillainCells(p);
                System.out.println("[DEBUG] placeVillains: Villain added at " + p);
            } else {
                System.out.println("[DEBUG] placeVillains: Position " + p + " is center; skipped.");
            }
        }
    }

    public static void pause() throws InterruptedException {
        Thread.sleep(400);
    }

    public static String bar(int hp, int max) {
        int filled = Math.max(0, hp) * 20 / max;
        return "[" + "#".repeat(filled) + "-".repeat(20 - filled) + "] " + Math.max(0, hp) + "/" + max;
    }

    public void simulate() throws InterruptedException {
        String place = DEPTHS[Math.min(hero.getLevel(), DEPTHS.length) - 1];
        System.out.println("[DEBUG] simulate: Starting simulation at location: " + place);
        hero.print();
        map.print();

        pause();

        System.out.println("\n> w");
        System.out.println("A " + villain.name() + " blocks your path.");
        System.out.println("\"" + villain.catchphrase() + "\"");
        System.out.println("(f)ight or (r)un?  f");

        pause();

        System.out.println("[DEBUG] simulate: Starting battle against " + villain.name() + "...");
        hero.fight(villain);
        System.out.println("[DEBUG] simulate: Battle finished.");

        System.out.println("\nVICTORY!  +" + villain.XPRewards() + " XP");
        System.out.println("Loot: Steel Harpoon (+6 Attack).  Keep it? (y/n)  y");
        System.out.println("[DEBUG] simulate: Adding " + villain.XPRewards() + " XP to hero...");
        hero.gainExperience(villain.XPRewards());
        System.out.println();
        hero.print();
        System.out.println("[DEBUG] simulate: Simulation complete.");
    }
}
