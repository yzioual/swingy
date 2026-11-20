package com.yzioual.controller;

import com.yzioual.database.DatabaseManager;
import com.yzioual.model.artifact.Artifact;
import com.yzioual.model.hero.Hero;
import com.yzioual.model.hero.HeroType;
import com.yzioual.model.hero.Passive;
import com.yzioual.model.map.Direction;
import com.yzioual.model.map.Map;
import com.yzioual.model.map.Position;
import com.yzioual.model.villain.Villain;
import com.yzioual.model.villain.VillainType;
import com.yzioual.view.ConsoleView;
import com.yzioual.view.GUIView;
import com.yzioual.view.ViewMode;

import javax.swing.JOptionPane;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

public class GameController {
    private static final int CHASE_RADIUS = 3;
    public static final int seed = 3;
    public static final int VILLAIN_PERCENT = 12;
    private static final String[] DEPTHS = {
            "The Shallow Reef",
            "The Kelp Forest",
            "The Sunken Wreck",
            "The Twilight Zone",
            "The Abyssal Trench",
            "The Black Deep"
    };
    private static final int LUCK = 15;
    private static final int ROUNDS_TO_WIN = 2;
    private static final int AIM_CHANCE = 15;
    private static final int AIM_BONUS = 8;

    private final DatabaseManager db;
    private final ConsoleView consoleView;
    private final GUIView guiView;
    private final Random random = new Random(seed);
    private final Object lock = new Object();

    private ViewMode currentMode;
    private Hero hero;
    private Map map;
    private String status = "";
    private Villain villain;
    private int day = 1;
    private boolean running = true;
    private boolean gameOver = false;

    public enum MoveResult { ERR, FIGHT, WIN, RUN, MOVED }
    public enum BattleOutcome { SMASH, WIN, LOST, ONGOING }
    public record BattleResult(BattleOutcome outcome, List<String> log) {
        public boolean didHeroWin() { return outcome == BattleOutcome.WIN || outcome == BattleOutcome.SMASH; }
        public boolean didHeroSmash() { return outcome == BattleOutcome.SMASH; }
    }

    public GameController() {
        this(ViewMode.CONSOLE, null, null);
    }

    public GameController(ViewMode initialMode) {
        this(initialMode, null, null);
    }

    public GameController(ConsoleView consoleView, Hero hero) {
        this(ViewMode.CONSOLE, consoleView, hero);
    }

    public GameController(ViewMode initialMode, ConsoleView consoleView, Hero hero) {
        this.currentMode = initialMode != null ? initialMode : ViewMode.CONSOLE;
        this.consoleView = consoleView != null ? consoleView : new ConsoleView();
        this.hero = hero;
        this.db = new DatabaseManager();

        if (!GraphicsEnvironment.isHeadless()) {
            this.guiView = new GUIView();
            this.guiView.setMoveListener(dir -> applyMove(dir, true));
            this.guiView.setSwitchListener(this::switchToConsole);
            if (this.guiView.getFrame() != null) {
                this.guiView.getFrame().addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        if (GameController.this.hero != null) {
                            db.saveHero(GameController.this.hero);
                        }
                        running = false;
                        guiView.dispose();
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                });
            }
        } else {
            this.guiView = null;
            if (this.currentMode == ViewMode.GUI) {
                System.out.println("Headless environment detected. Falling back to console mode.");
                this.currentMode = ViewMode.CONSOLE;
            }
        }
    }

    public void play() {
        initHero();
        if (hero == null || !running) {
            return;
        }

        startDay();

        while (running && !gameOver) {
            if (currentMode == ViewMode.CONSOLE) {
                runConsoleTurn();
            } else {
                runGuiLoop();
            }
        }

        if (guiView != null) {
            guiView.dispose();
        }
    }

    private void initHero() {
        while (hero == null && running) {
            if (currentMode == ViewMode.CONSOLE) {
                initHeroConsole();
            } else {
                initHeroGui();
            }
        }
    }

    private void initHeroConsole() {
        List<Hero> savedHeroes = db.loadAllHeroes();
        ConsoleView.StartChoice choice = consoleView.askStartOption(!savedHeroes.isEmpty());
        switch (choice) {
            case CREATE -> {
                Optional<HeroType> type = consoleView.askForHero(HeroType.getListOfHeroTypes());
                if (type.isEmpty()) return;
                Optional<String> name = consoleView.askHeroName();
                if (name.isEmpty()) return;
                hero = new Hero.HeroBuilder()
                        .setName(name.get())
                        .setHeroType(type.get())
                        .build();
                db.saveHero(hero);
            }
            case SELECT -> {
                Optional<Hero> picked = consoleView.askSelectHero(savedHeroes);
                picked.ifPresent(h -> hero = h);
            }
            case SWITCH_TO_GUI -> {
                if (GraphicsEnvironment.isHeadless()) {
                    System.out.println("Cannot switch to GUI: No graphical display detected.");
                } else {
                    System.out.println("Switching to GUI mode... (Press Ctrl+S in GUI to switch back)");
                    currentMode = ViewMode.GUI;
                }
            }
            case QUIT -> {
                running = false;
            }
        }
    }

    private void initHeroGui() {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("No display detected. Falling back to console mode.");
            currentMode = ViewMode.CONSOLE;
            return;
        }
        List<Hero> savedHeroes = db.loadAllHeroes();
        Hero created = GUIView.askSelectOrCreateHero(savedHeroes, () -> {
            currentMode = ViewMode.CONSOLE;
        });
        if (created != null) {
            hero = created;
            db.saveHero(hero);
        } else if (currentMode == ViewMode.GUI) {
            running = false;
        }
    }

    private void startDay() {
        this.map = new Map(hero);
        this.placeVillains(this.map);
        this.status = "Day " + day + ": dive in!";
        this.gameOver = false;
        if (hero != null) {
            db.saveHero(hero);
        }
    }

    public void switchToConsole() {
        if (hero != null) {
            db.saveHero(hero);
        }
        if (guiView != null) {
            guiView.hide();
        }
        currentMode = ViewMode.CONSOLE;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void switchToGui() {
        if (GraphicsEnvironment.isHeadless()) {
            status = "Cannot switch to GUI: No display detected.";
            return;
        }
        if (hero != null) {
            db.saveHero(hero);
        }
        System.out.println("Switching to GUI mode... (Press Ctrl+S in GUI to switch back)");
        currentMode = ViewMode.GUI;
    }

    private void runConsoleTurn() {
        String place = DEPTHS[Math.min(hero.getLevel(), DEPTHS.length) - 1];
        consoleView.render(place, hero, map, status);

        ConsoleView.ConsoleAction action = consoleView.askMoveAction();
        switch (action.type()) {
            case MOVE -> applyMove(action.direction(), false);
            case SWITCH_TO_GUI -> switchToGui();
            case QUIT -> {
                if (hero != null) {
                    db.saveHero(hero);
                }
                System.out.println("\nSee you soon! Hero progress saved to database.");
                running = false;
            }
        }
    }

    private void runGuiLoop() {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("No display detected. Falling back to console mode.");
            currentMode = ViewMode.CONSOLE;
            return;
        }

        renderGui();
        guiView.show();

        synchronized (lock) {
            while (currentMode == ViewMode.GUI && running && !gameOver) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        if (currentMode == ViewMode.CONSOLE && running && !gameOver) {
            System.out.println("\n[Switched to Console mode]");
        }
    }

    private void renderGui() {
        if (guiView == null || hero == null || map == null) return;
        String place = DEPTHS[Math.min(hero.getLevel(), DEPTHS.length) - 1];
        guiView.render(place, hero, map, status);
    }

    public void applyMove(Direction direction, boolean isGui) {
        if (gameOver) return;

        Position next = map.getHeroPosition().move(direction);
        if (!isInside(map, next)) {
            status = "You can't go that way.";
            if (isGui) renderGui();
            return;
        }

        map.setPreviousHeroPosition(map.getHeroPosition());
        map.setCurrentPosition(next);

        boolean contact = false;
        if (map.hasVillainAt(next)) {
            contact = true;
        } else if (isOnBorder(map)) {
            handleShoreReached(isGui);
            return;
        } else {
            contact = advanceVillains(map);
        }

        if (contact) {
            BattleOutcome outcome = handleEncounter(isGui);
            if (outcome == BattleOutcome.LOST) {
                handleDied(isGui);
                return;
            } else if (outcome == BattleOutcome.WIN) {
                handleShoreReached(isGui);
                return;
            }
        }

        if (isGui) {
            renderGui();
        }
    }

    private BattleOutcome handleEncounter(boolean isGui) {
        this.villain = pickVillain();

        boolean wantsFight;
        if (isGui) {
            wantsFight = guiView.askFight(villain);
        } else {
            System.out.println("\nA " + villain.name() + " blocks your way!");
            System.out.println("\"" + villain.catchphrase() + "\"");
            wantsFight = consoleView.askFight("");
        }

        if (!wantsFight) {
            boolean cellFree = !map.hasVillainAt(map.getHeroPreviousPosition());
            if (cellFree && random.nextBoolean()) {
                map.retreat();
                this.status = "You slipped back to your previous cell";
                return BattleOutcome.ONGOING;
            }
            if (isGui && guiView.getFrame() != null) {
                JOptionPane.showMessageDialog(guiView.getFrame(),
                        "You couldn't escape! YOU MUST FIGHT BUDDY!",
                        "Escape Failed", JOptionPane.WARNING_MESSAGE);
            } else {
                System.out.println("You couldn't escape! YOU MUST FIGHT BUDDY!");
            }
        }

        BattleResult result = this.fight();
        if (!result.didHeroWin()) {
            if (isGui) {
                guiView.showBattleResult(result.log(), false, false, 0, 0);
            } else {
                result.log().forEach(System.out::println);
                consoleView.waitForEnter();
            }
            return BattleOutcome.LOST;
        }

        map.removeVillainAt(map.getHeroPosition());
        int xp = result.outcome() == BattleOutcome.SMASH
                ? villain.XPRewards() * 3 / 2 : villain.XPRewards();
        int levelsGained = hero.gainExperience(xp);
        db.saveHero(hero);

        if (isGui) {
            guiView.showBattleResult(result.log(), true, result.didHeroSmash(), xp, levelsGained);
        } else {
            result.log().forEach(System.out::println);
            consoleView.waitForEnter();
        }

        status = "Victory! +" + xp + " XP" + (result.didHeroSmash() ? " (flawless)" : "");
        if (levelsGained > 0) {
            status += "  LEVEL UP! You are now level " + hero.getLevel() + ".";
        }

        Optional<Artifact> drop = LootGenerator.roll(villain, random);
        if (drop.isPresent()) {
            Artifact artifact = drop.get();
            Artifact current = hero.getEquipped(artifact.type());
            boolean keep = isGui ? guiView.askEquip(artifact, current) : consoleView.askEquip(artifact, current);
            if (keep) {
                hero.equip(artifact);
                db.saveHero(hero);
                status += "  Equipped " + artifact.name() + "!";
            } else {
                status += "  Left " + artifact.name() + ".";
            }
        }

        if (isOnBorder(map)) {
            return BattleOutcome.WIN;
        }
        return BattleOutcome.ONGOING;
    }

    private void handleShoreReached(boolean isGui) {
        db.saveHero(hero);
        if (isGui) {
            boolean continueDiving = guiView.showShore(day, hero);
            if (continueDiving) {
                day++;
                startDay();
                renderGui();
            } else {
                running = false;
                guiView.dispose();
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        } else {
            System.out.println("\nYou reached the shore with your fish! Your family eats tonight.");
            consoleView.showHero(hero);
            consoleView.waitForEnter();
            day++;
            startDay();
        }
    }

    private void handleDied(boolean isGui) {
        gameOver = true;
        db.saveHero(hero);
        if (isGui) {
            boolean retry = guiView.showGameOver();
            if (retry) {
                gameOver = false;
                day = 1;
                hero = null;
                initHero();
                if (hero != null) {
                    startDay();
                    renderGui();
                }
            } else {
                running = false;
                guiView.dispose();
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        } else {
            System.out.println("You drowned. Your family starves.");
            System.out.print("Try again? (y/n) > ");
            Scanner sc = new Scanner(System.in);
            if (sc.hasNextLine() && sc.nextLine().trim().equalsIgnoreCase("y")) {
                gameOver = false;
                day = 1;
                hero = null;
                initHero();
                if (hero != null) {
                    startDay();
                }
            } else {
                running = false;
            }
        }
    }

    private boolean isInside(Map map, Position p) {
        return p.x() >= 0 && p.y() >= 0 && p.x() < map.getSize() && p.y() < map.getSize();
    }

    private boolean isOnBorder(Map map) {
        if (map == null) return false;
        Position p = map.getHeroPosition();
        return p.x() == 0 || p.y() == 0 || p.x() == map.getSize() - 1 || p.y() == map.getSize() - 1;
    }

    private Position stepToward(Map map, Position from) {
        int dx = map.getHeroPosition().x() - from.x();
        int dy = map.getHeroPosition().y() - from.y();
        int dist = Math.max(Math.abs(dx), Math.abs(dy));

        if (dist == 0 || dist > CHASE_RADIUS) {
            return from;
        }

        return Math.abs(dx) >= Math.abs(dy)
                ? new Position(from.x() + Integer.signum(dx), from.y())
                : new Position(from.x(), from.y() + Integer.signum(dy));
    }

    public boolean advanceVillains(Map map) {
        Set<Position> next = new HashSet<>();
        boolean contact = false;
        Set<Position> villainCells = map.getVillainCells();

        for (Position from : villainCells) {
            Position to = stepToward(map, from);
            boolean blocked = !to.equals(from) && (next.contains(to)
                    || (!to.equals(map.getHeroPosition()) && villainCells.contains(to)));
            Position finalPos = blocked ? from : to;
            next.add(finalPos);

            if (finalPos.equals(map.getHeroPosition()))
                contact = true;
        }
        map.clearVillainsCells();
        map.replaceVillainsCells(next);
        return contact;
    }

    private Villain pickVillain() {
        VillainType[] types = VillainType.values();
        int tier = Math.min(types.length, hero.getLevel());
        if (tier > 1 && random.nextInt(100) >= 70) tier--;
        return Villain.spawn(types[tier - 1], hero.getLevel());
    }

    public void placeVillains(Map map) {
        Position center = new Position(map.getSize() / 2, map.getSize() / 2);
        int count = Math.max(1, map.getSize() * map.getSize() * VILLAIN_PERCENT / 100);

        while (map.getVillainCells().size() < count) {
            Position p = new Position(random.nextInt(map.getSize()), random.nextInt(map.getSize()));
            if (!p.equals(center)) {
                map.addVillainCells(p);
            }
        }
    }

    private BattleResult fight() {
        Passive passive = hero.getHeroType().getPassive();
        List<String> log = new ArrayList<>();
        int heroWins = 0, villainWins = 0, round = 0;
        boolean wetSuitUsed = false;
        Random r = new Random();

        while (heroWins < ROUNDS_TO_WIN && villainWins < ROUNDS_TO_WIN) {
            round++;

            double heroScore = hero.getAttack() + hero.getDefense() / 2.0 + hero.getHitPoints() / 10.0 + r.nextInt(LUCK + 1);
            double villainScore = villain.attack() + villain.defense() / 2.0
                    + villain.hitPoints() / 10.0 + r.nextInt(LUCK + 1);

            if (passive == Passive.PERFECT_AIM && r.nextInt(100) < AIM_CHANCE) {
                heroScore += AIM_BONUS;
                log.add("  Perfect Aim! Your spear finds its mark.");
            }
            if (passive == Passive.LAST_BREATH && villainWins >= 1) {
                heroScore += hero.getAttack() * 0.3;
                log.add("  Last Breath! Your arms surge with desperate strength.");
            }

            if (heroScore >= villainScore) {
                heroWins++;
                log.add("Round " + round + ": you win the round.");
            } else if (passive == Passive.THICK_WETSUIT && !wetSuitUsed) {
                wetSuitUsed = true;
                log.add("Round " + round + ": the blow hits... your wetsuit absorbs it!");
            } else {
                villainWins++;
                log.add("Round " + round + ": " + villain.name() + " wins the round.");
            }
        }
        if (heroWins < ROUNDS_TO_WIN) {
            return new BattleResult(BattleOutcome.LOST, log);
        }
        return new BattleResult(villainWins == 0 ? BattleOutcome.SMASH : BattleOutcome.WIN, log);
    }

    public Hero getHero() { return hero; }
    public Map getMap() { return map; }
    public int getDay() { return day; }
    public GUIView getGuiView() { return guiView; }
    public ConsoleView getConsoleView() { return consoleView; }
}
