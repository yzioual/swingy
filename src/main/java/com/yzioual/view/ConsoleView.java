package com.yzioual.view;

import com.yzioual.model.hero.Hero;
import com.yzioual.model.hero.HeroType;
import com.yzioual.model.map.Direction;
import com.yzioual.model.map.Map;
import com.yzioual.model.map.Position;

import java.util.*;

public class ConsoleView {
    private static final String RESET = "\033[0m";
    private static final String DIM = "\033[2m";
    private static final String HERO = "\033[1;36m";
    private static final String[] TIER = {"", "\033[32m", "\033[33m", "\033[38;5;208m", "\033[31m"};
    private final Scanner scanner = new Scanner(System.in);
    private final boolean ansi;

    public ConsoleView() {
        // isTerminal() is false in IDE consoles and when output is piped
        this.ansi = System.console() != null && System.console().isTerminal();
    }

    public void render(String depth, Hero hero, Map map, String status) {
        clear();
        System.out.println("=== DAY " + hero.getLevel() + ": " + depth.toUpperCase() + " ===");
        System.out.printf("%s, %s   Lv %d   XP %d/%d%n",
                hero.getName(), hero.getHeroType().getName(),
                hero.getLevel(), hero.getExperience(), hero.getNextLevelXp());
        System.out.printf("ATK %d   DEF %d   HP %d%n%n",
                hero.getAttack(), hero.getDefense(), hero.getHitPoints());

        int size = map.getSize();
        for (int y = 0; y < size; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < size; x++) {
                row.append(cell(map, new Position(x, y), size));
            }
            System.out.println(row);
        }

        System.out.println();
        System.out.println(color(HERO, "@") + " you   "
                + color(TIER[1], "1") + color(TIER[2], "2")
                + color(TIER[3], "3") + color(TIER[4], "4") + " fears (by power)   "
                + color(DIM, ":") + " shore");
        System.out.println();
        System.out.println(status.isEmpty() ? " " : status);
    }

    public Optional<Direction> askMove() {
        while (true) {
            System.out.print("Move (w/a/s/d), q to quit > ");
            if (!scanner.hasNextLine()) {
                return Optional.empty();
            }

            switch (scanner.nextLine().trim().toLowerCase()) {
                case "w" -> {
                    return Optional.of(Direction.NORTH);
                }
                case "d" -> {
                    return Optional.of(Direction.EAST);
                }
                case "s" -> {
                    return Optional.of(Direction.SOUTH);
                }
                case "a" -> {
                    return Optional.of(Direction.WEST);
                }
                case "q" -> {
                    return Optional.empty();
                }
                default -> System.out.println("Please type w, a, s, d or q.");
            }
        }
    }

    public boolean askFight(String message) {
        System.out.println();
        System.out.println(message);
        while (true) {
            System.out.print("(f)ight or (r)un? > ");
            if (!scanner.hasNextLine()) {
                return true;
            }
            switch (scanner.nextLine().trim().toLowerCase()) {
                case "f" -> {
                    return true;
                }
                case "r" -> {
                    return false;
                }
                default -> System.out.println("Please type f or r.");
            }
        }
    }

    public void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String cell(Map map, Position p, int size) {
        if (p.equals(map.getHeroPosition())) {
            return color(HERO, "@") + " ";
        }
        if (map.hasVillainAt(p)) {
            return color(TIER[4], "V") + " ";
        }
        boolean shore = p.x() == 0 || p.y() == 0 || p.x() == size - 1 || p.y() == size - 1;
        return shore ? color(DIM, ":") + " " : ". ";
    }



    private String color(String code, String text) {
        return ansi ? code + text + RESET : text;
    }

    private void clear() {
        if (ansi) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        } else {
            System.out.println("\n".repeat(3) + "-".repeat(40));
        }
    }


    /**
     * Re-asks until the input is a whole number in [min, max]. Empty means input closed.
     * */
    private OptionalInt readNumber(int max) {
        while (true) {
            System.out.print("Type > ");
            if (!scanner.hasNextLine()) {
                return OptionalInt.empty();
            }
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= 0 && value <= max) {
                    return OptionalInt.of(value);
                }
            } catch (NumberFormatException ignored) {
                // falls through to the message below
            }
            System.out.println("Please type a number from " + 0 + " to " + max + ".");
        }
    }

    /**
     * Shows every hero type and returns the one the player picks, or empty if they go back.
     * */
    public Optional<HeroType> askForHero(List<HeroType> listOfHeroes) {
        System.out.println();
        System.out.println("--- Choose a hero type ---");
        for (int i = 0; i < listOfHeroes.size(); i++) {
            HeroType t = listOfHeroes.get(i);
            System.out.println();
            System.out.println((i + 1) + ") " + t.getName());
            System.out.println("   Attack:     " + t.getAttack());
            System.out.println("   Defense:    " + t.getDefense());
            System.out.println("   Hit Points: " + t.getHitPoints());
        }
        System.out.println();
        System.out.println("0) Back");

        OptionalInt pick = readNumber(listOfHeroes.size());
        if (pick.isEmpty() || pick.getAsInt() == 0) {
            return Optional.empty();
        }
        return Optional.of(listOfHeroes.get(pick.getAsInt() - 1));
    }
}
