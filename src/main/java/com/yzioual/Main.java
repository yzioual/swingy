package com.yzioual;

import com.yzioual.model.Hero;
import com.yzioual.model.HeroClass;
import com.yzioual.model.maps.GameMap;
import com.yzioual.model.maps.Position;
import com.yzioual.model.Villain;
import com.yzioual.model.VillainType;

import java.util.Random;

public class Main {
	private static final String[] DEPTHS = {
			"The Shallow Reef", "The Kelp Forest", "The Sunken Wreck",
			"The Twilight Zone", "The Abyssal Trench", "The Black Deep"};

	private static final int[] HERO_HITS  = {14, 11, 16, 12, 15, 13, 14, 17};
	private static final int[] SHARK_HITS = {10, 13, 0, 12, 11, 9, 14};

	void main(String[] args) throws InterruptedException {
		Hero hero = Hero.builder().name("Marco").heroClass(HeroClass.FREE_DIVER).build();
		Villain shark = Villain.spawn(VillainType.REEF_SHARK, hero.getLevel());
		GameMap map = new GameMap(hero.getLevel(), new Random(3));

		String place = DEPTHS[Math.min(hero.getLevel(), DEPTHS.length) - 1];
		System.out.println("=== DAY " + hero.getLevel() + ": " + place.toUpperCase() + " ===");
		printHero(hero);
		printMap(map);
		pause();

		System.out.println("\n> w");
		System.out.println("A " + shark.name() + " blocks your path.");
		System.out.println("\"" + shark.catchphrase() + "\"");
		System.out.println("(f)ight or (r)un?  f");
		pause();

		fight(hero, shark);

		System.out.println("\nVICTORY!  +" + shark.XPRewards() + " XP");
		System.out.println("Loot: Steel Harpoon (+6 Attack).  Keep it? (y/n)  y");
		hero.gainExperience(shark.XPRewards());
		System.out.println();
		printHero(hero);
	}

	private static void fight(Hero hero, Villain shark) throws InterruptedException {
		int heroHp = hero.getHitPoints();
		int sharkHp = shark.hitPoints();
		int heroMax = heroHp;
		int sharkMax = sharkHp;

		System.out.println("\n--- BATTLE: " + hero.getName() + " vs " + shark.name() + " ---");
		for (int i = 0; sharkHp > 0 && heroHp > 0; i++) {
			System.out.println("\nRound " + (i + 1));

			int dealt = Math.min(HERO_HITS[i], sharkHp);
			sharkHp -= dealt;
			System.out.println("  You thrust your spear.  " + shark.name() + " takes " + dealt + ".");
			if (sharkHp <= 0) break;

			int taken = SHARK_HITS[i];
			if (taken == 0) {
				System.out.println("  " + shark.name() + " lunges... and misses!");
			} else {
				heroHp -= taken;
				System.out.println("  " + shark.name() + " bites.  You take " + taken + ".");
			}
			if (heroHp < heroMax / 2) {
				System.out.println("  Last Breath! Your lungs burn, but your arms are stronger.");
			}

			System.out.println("  " + hero.getName() + "  " + bar(heroHp, heroMax));
			System.out.println("  " + shark.name() + "  " + bar(sharkHp, sharkMax));
			pause();
		}
		System.out.println("\n  " + shark.name() + " sinks into the dark.");
	}

	private static void printHero(Hero h) {
		System.out.printf("%s, %s   Lv %d   XP %d/%d%n",
				h.getName(), h.getHeroClass().name(),
				h.getLevel(), h.getExperience(), h.getNextLevelXp());
		System.out.printf("ATK %d   DEF %d   HP %d   Oxygen: %d%n",
				h.getAttack(), h.getDefense(), h.getHitPoints(),
				GameMap.sizeForLevel(h.getLevel()) * 3);
	}

	private static void printMap(GameMap map) {
		System.out.println();
		for (int y = 0; y < map.getSize(); y++) {
			StringBuilder row = new StringBuilder();
			for (int x = 0; x < map.getSize(); x++) {
				Position p = new Position(x, y);
				if (p.equals(map.getHeroPosition())) row.append("@ ");
				else if (map.hasVillainAt(p))        row.append("V ");
				else                                 row.append(". ");
			}
			System.out.println(row);
		}
	}

	private static String bar(int hp, int max) {
		int filled = Math.max(0, hp) * 20 / max;
		return "[" + "#".repeat(filled) + "-".repeat(20 - filled) + "] "
				+ Math.max(0, hp) + "/" + max;
	}

	private static void pause() throws InterruptedException {
		Thread.sleep(400);
	}
}