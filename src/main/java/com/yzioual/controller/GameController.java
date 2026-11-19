package com.yzioual.controller;

import com.yzioual.model.artifact.Artifact;
import com.yzioual.model.hero.Hero;
import com.yzioual.model.hero.Passive;
import com.yzioual.model.map.Direction;
import com.yzioual.model.map.Map;
import com.yzioual.model.map.Position;
import com.yzioual.model.villain.Villain;
import com.yzioual.model.villain.VillainType;
import com.yzioual.view.ConsoleView;

import java.util.*;

public class GameController {
    private static final int CHASE_RADIUS = 3;
    private final Hero hero;
    private Map map;
    private String status;
    private Villain villain;
    private final ConsoleView consoleView;
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
    private static final int LUCK = 15;            // random 0..15 added to each score
    private static final int ROUNDS_TO_WIN = 2;
    private static final int AIM_CHANCE = 15;      // percent
    private static final int AIM_BONUS = 8;
    private final Random random = new Random(seed);


    private enum MissionResult { SHORE, DIED, QUIT };

    public GameController(ConsoleView consoleView,
                          Hero hero) {
        this.consoleView = consoleView;
        this.hero = hero;
        this.status = "";
    }

    public void play() {
        int day = 1;

        while (true) {
            this.map = new Map(hero);
            this.placeVillains(this.map);
            this.status = "Day " + day + ": dive in!";
            MissionResult result = runMission();

            if (result == MissionResult.QUIT) {
                System.out.println("\nSee you soon!");
                return;
            }
            if (result == MissionResult.DIED) {
                System.out.println("You drowned. Your family starves.");
                return;
            }

            // SHORE: the hero survived, so show his progress and dive again
            System.out.println("\nYou reached the shore with your fish! Your family eats tonight.");
            consoleView.showHero(hero);
            consoleView.waitForEnter();
            day++;
        }
    }

    public static String bar(int hp, int max) {
        int filled = Math.max(0, hp) * 20 / max;
        return "[" + "#".repeat(filled) + "-".repeat(20 - filled) + "] " + Math.max(0, hp) + "/" + max;
    }

    public static void pause() throws InterruptedException {
        Thread.sleep(400);
    }

    private boolean isInside(Map map, Position p) {
        return p.x() >= 0 && p.y() >= 0 && p.x() < map.getSize() && p.y() < map.getSize();
    }

    private boolean isOnBorder() {
        Position p = map.getHeroPosition();
        return p.x() == 0 || p.y() == 0 || p.x() == map.getSize() - 1 || p.y() == map.getSize() - 1;
    }

    public enum MoveResult { ERR, FIGHT, WIN, RUN, MOVED }
    private enum BattleOutcome { SMASH, WIN, LOST, ONGOING};

    // win = 2 - 0 or 2 - 1
    // smash = 3 - 0
    public record BattleResult(BattleOutcome outcome, List<String> log) {
        public boolean didHeroWin() {
            return outcome == BattleOutcome.WIN || outcome == BattleOutcome.SMASH;
        }
        public boolean didHeroSmash() {return outcome == BattleOutcome.SMASH;}
    }

    public MoveResult moveHero(Map map, Direction direction) {
        Position next = map.getHeroPosition().move(direction);
        if (!isInside(map, next)) return MoveResult.ERR;

        map.setPreviousHeroPosition(map.getHeroPosition());
        map.setCurrentPosition(next);

        if (map.hasVillainAt(next)) return MoveResult.FIGHT;
        if (isOnBorder()) return  MoveResult.WIN;
        return MoveResult.MOVED;
    }

    private Position stepToward(Map map, Position from) {
        int dx = map.getHeroPosition().x() - from.x();
        int dy = map.getHeroPosition().y() - from.y();
        int dist = Math.max(Math.abs(dx),  Math.abs(dy));

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
        VillainType[] types = VillainType.values();          // enum order = tier order
        int tier = Math.min(types.length, hero.getLevel());
        if (tier > 1 && random.nextInt(100) >= 70) tier--;   // 30% the tier below
        return Villain.spawn(types[tier - 1], hero.getLevel());
    }

    public void placeVillains(Map map) {
        Position center =  new Position(map.getSize() / 2, map.getSize() / 2);
        int count = Math.max(1, map.getSize() * map.getSize() * VILLAIN_PERCENT / 100);

        while (map.getVillainCells().size() < count) {
            Position p = new Position(random.nextInt(map.getSize()), random.nextInt(map.getSize()) );

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
        Random random = new Random();

        while (heroWins < ROUNDS_TO_WIN && villainWins < ROUNDS_TO_WIN) {
            round++;

            double heroScore = hero.getAttack() + hero.getDefense() / 2.0 + hero.getHitPoints()  / 10.0 + random.nextInt(LUCK+1);
            double villainScore = villain.attack() + villain.defense() / 2.0
                    + villain.hitPoints() / 10.0 + random.nextInt(LUCK + 1);

            if (passive == Passive.PERFECT_AIM && random.nextInt(100) < AIM_CHANCE) {
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

    private BattleOutcome handleEncounter() {
        this.villain = pickVillain();
        System.out.println("\nA " + villain.name() + " blocks your way!");
        System.out.println("\"" + villain.catchphrase() + "\"");

        if (!consoleView.askFight("")) {
            boolean cellFree = !map.hasVillainAt(map.getHeroPreviousPosition());
            if (cellFree && random.nextBoolean()) {
                map.retreat();
                this.status = "You slipped back to your preivous cell";
                return BattleOutcome.ONGOING; // back to playing
            }
            System.out.println("You couldn't escape! YOU MUST FIGHT BUDDY!");
        }

        BattleResult result = this.fight();
        result.log().forEach(System.out::println);
        consoleView.waitForEnter(); // lets the player read the fight

        if (!result.didHeroWin()) {
            return BattleOutcome.LOST;
        }
        map.removeVillainAt(map.getHeroPosition());

        int xp = result.outcome() == BattleOutcome.SMASH
                ? villain.XPRewards() * 3 / 2 : villain.XPRewards();
        int levelsGained = hero.gainExperience(xp);
        status = "Victory! +" + xp + " XP" + (result.didHeroSmash() ? " (flawless)" : "");

        if (levelsGained > 0) {
            status += "  LEVEL UP! You are now level " + hero.getLevel() + ".";
        }

        Optional<Artifact> drop = LootGenerator.roll(villain, random);
        if (drop.isPresent()) {
            Artifact artifact = drop.get();
            Artifact current = hero.getEquipped(artifact.type());
            if (consoleView.askEquip(artifact, current)) {
                hero.equip(artifact);
                status += "  Equipped " + artifact.name() + "!";
            } else {
                status += "  Left " + artifact.name() + ".";
            }
        }
        if (isOnBorder()) {
            return BattleOutcome.WIN;
        }
        return BattleOutcome.ONGOING;
    }

    private MissionResult runMission() {
        String place = DEPTHS[Math.min(hero.getLevel(), DEPTHS.length) - 1];
        while (true) {
            boolean contact = false;
            consoleView.render(place, this.hero, this.map, status);
            Optional<Direction> move = consoleView.askMove();
            if (move.isEmpty()) {
                System.out.println("\nSee you soon!");
                break;
            }

            MoveResult moveResult = this.moveHero(this.map, move.get());
            switch (moveResult) {
                case ERR   -> { status = "You can't go that way."; continue; }
                case WIN   -> { System.out.println("Congrats you won"); return MissionResult.SHORE; }
                case FIGHT -> contact = true;
                default -> contact = this.advanceVillains(this.map);
            }

            if (contact) {
                switch (handleEncounter()) {
                    case LOST -> { return MissionResult.DIED; }
                    case WIN -> { return MissionResult.SHORE; }
                    default -> {}
                }
            }
        }
        return null;
    }
}