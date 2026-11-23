package com.yzioual.view;

import com.yzioual.model.artifact.Artifact;
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
        this.ansi = System.console() != null;
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
                + color(TIER[4], "V") + " fears/villains   "
                + color(DIM, ":") + " shore");
        System.out.println();
        System.out.println(status.isEmpty() ? " " : status);
    }

    public enum ActionType { MOVE, SWITCH_TO_GUI, QUIT }
    public record ConsoleAction(ActionType type, Direction direction) {
        public static ConsoleAction move(Direction d) { return new ConsoleAction(ActionType.MOVE, d); }
        public static ConsoleAction switchToGui() { return new ConsoleAction(ActionType.SWITCH_TO_GUI, null); }
        public static ConsoleAction quit() { return new ConsoleAction(ActionType.QUIT, null); }
    }

    public ConsoleAction askMoveAction() {
        while (true) {
            System.out.print("Move (w/a/s/d), 'v' to switch to GUI, 'q' to quit > ");
            if (!scanner.hasNextLine()) {
                return ConsoleAction.quit();
            }

            String line = scanner.nextLine().trim().toLowerCase();
            switch (line) {
                case "w", "up" -> {
                    return ConsoleAction.move(Direction.NORTH);
                }
                case "d", "right" -> {
                    return ConsoleAction.move(Direction.EAST);
                }
                case "s", "down" -> {
                    return ConsoleAction.move(Direction.SOUTH);
                }
                case "a", "left" -> {
                    return ConsoleAction.move(Direction.WEST);
                }
                case "v", "gui", "switch", "g" -> {
                    return ConsoleAction.switchToGui();
                }
                case "q", "quit", "exit" -> {
                    return ConsoleAction.quit();
                }
                default -> System.out.println("Please type w, a, s, d (move), v (switch to GUI), or q (quit).");
            }
        }
    }

    public Optional<Direction> askMove() {
        ConsoleAction action = askMoveAction();
        return action.type() == ActionType.MOVE ? Optional.of(action.direction()) : Optional.empty();
    }

    public void showEncounter(String villainName, String catchphrase) {
        System.out.println("\nA " + villainName + " blocks your way!");
        System.out.println("\"" + catchphrase + "\"");
    }

    public void showEscapeFailed() {
        System.out.println("You couldn't escape! YOU MUST FIGHT BUDDY!");
    }

    public void showBattleLog(List<String> log) {
        if (log != null) {
            log.forEach(System.out::println);
        }
    }

    public void showShoreReached(Hero hero) {
        System.out.println("\nYou reached the shore with your fish! Your family eats tonight.");
        showHero(hero);
    }

    public boolean askRetry() {
        System.out.println("You drowned. Your family starves.");
        while (true) {
            System.out.print("Try again? (y/n) > ");
            if (!scanner.hasNextLine()) {
                return false;
            }
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) return true;
            if (line.equals("n") || line.equals("no")) return false;
            System.out.println("Please type y or n.");
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

    public boolean askEquip(Artifact dropped, Artifact current) {
        System.out.println();
        System.out.println("Loot dropped: " + dropped);
        if (current != null) {
            System.out.println("Currently equipped: " + current);
        } else {
            System.out.println("Currently equipped: None");
        }
        while (true) {
            System.out.print("Keep it? (y/n) > ");
            if (!scanner.hasNextLine()) {
                return false;
            }
            String input = scanner.nextLine().trim().toLowerCase();
            switch (input) {
                case "y", "yes" -> {
                    return true;
                }
                case "n", "no" -> {
                    return false;
                }
                default -> System.out.println("Please type y or n.");
            }
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
            } catch (NumberFormatException ignored) {/* falls through to the message below */}
            System.out.println("Please type a number from " + 0 + " to " + max + ".");
        }
    }

    public void waitForEnter() {
        System.out.print("Press Enter to continue... ");
        if (scanner.hasNextLine()) scanner.nextLine();
    }

    public void showHero(Hero hero) {
        System.out.println("\nYour stats:");
        System.out.println("    Name:      " + hero.getName());
        System.out.println("   Attack:     " + hero.getAttack());
        System.out.println("   Defense:    " + hero.getDefense());
        System.out.println("   Hit Points: " + hero.getHitPoints());
        System.out.println("   Weapon:     " + (hero.getWeapon() != null ? hero.getWeapon() : "None"));
        System.out.println("   Armor:      " + (hero.getArmor() != null ? hero.getArmor() : "None"));
        System.out.println("   Helm:       " + (hero.getHelm() != null ? hero.getHelm() : "None"));
    }

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

    public Optional<String> askHeroName() {
        System.out.print("Name your hero > ");
        if (!scanner.hasNextLine()) {
            return Optional.empty();
        }
        return Optional.of(scanner.nextLine().trim());
    }

    public void showErrors(List<String> errors) {
        errors.forEach(e -> System.out.println(color("\033[31m", "  x " + e)));
    }

    public enum StartChoice { CREATE, SELECT, SWITCH_TO_GUI, QUIT }

    public StartChoice askStartOption(boolean hasSavedHeroes) {
        System.out.println("\n=== SWINGY: OCEAN DEPTHS ===");
        System.out.println("1) Create a new hero");
        if (hasSavedHeroes) {
            System.out.println("2) Select a saved hero");
        }
        System.out.println("3) Switch to GUI mode");
        System.out.println("0) Quit");

        while (true) {
            System.out.print("Select option > ");
            if (!scanner.hasNextLine()) {
                return StartChoice.QUIT;
            }
            String input = scanner.nextLine().trim().toLowerCase();
            switch (input) {
                case "1", "create" -> { return StartChoice.CREATE; }
                case "2", "select" -> {
                    if (hasSavedHeroes) return StartChoice.SELECT;
                    System.out.println("No saved heroes available. Choose 1, 3 or 0.");
                }
                case "3", "v", "gui", "switch", "g" -> { return StartChoice.SWITCH_TO_GUI; }
                case "0", "q", "quit", "exit" -> { return StartChoice.QUIT; }
                default -> System.out.println("Please select 1, " + (hasSavedHeroes ? "2, " : "") + "3 or 0.");
            }
        }
    }

    public Optional<Hero> askSelectHero(List<Hero> heroes) {
        if (heroes == null || heroes.isEmpty()) {
            System.out.println("\nNo saved heroes found.");
            return Optional.empty();
        }
        System.out.println("\n--- Saved Heroes ---");
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            System.out.printf("%d) %s (%s) - Lv %d | XP %d/%d | ATK %d | DEF %d | HP %d%n",
                    (i + 1), h.getName(), h.getHeroType().getName(),
                    h.getLevel(), h.getExperience(), h.getNextLevelXp(),
                    h.getAttack(), h.getDefense(), h.getHitPoints());
            System.out.printf("   Weapon: %s | Armor: %s | Helm: %s%n",
                    h.getWeapon() != null ? h.getWeapon().toString() : "None",
                    h.getArmor() != null ? h.getArmor().toString() : "None",
                    h.getHelm() != null ? h.getHelm().toString() : "None");
        }
        System.out.println("0) Back / Cancel");

        OptionalInt pick = readNumber(heroes.size());
        if (pick.isEmpty() || pick.getAsInt() == 0) {
            return Optional.empty();
        }
        return Optional.of(heroes.get(pick.getAsInt() - 1));
    }
}
