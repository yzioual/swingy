package com.yzioual.controller;

import com.yzioual.model.hero.Hero;
import com.yzioual.model.hero.HeroType;
import com.yzioual.model.map.Direction;
import com.yzioual.model.villain.Villain;
import com.yzioual.model.villain.VillainType;
import com.yzioual.model.map.Map;
import com.yzioual.model.map.Position;
import com.yzioual.view.ConsoleView;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class BattleSimulator {
    private final GameController gameController;
    private final Hero hero;
    private final Map map;
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

    public BattleSimulator(GameController gameController,
                           ConsoleView consoleView,
                           Hero hero,
                           Map map
    ) {
        this.consoleView = consoleView;
        this.hero = hero;
        this.map = map;
        this.gameController = gameController;
    }



    public static void pause() throws InterruptedException {
        Thread.sleep(400);
    }

    public static String bar(int hp, int max) {
        int filled = Math.max(0, hp) * 20 / max;
        return "[" + "#".repeat(filled) + "-".repeat(20 - filled) + "] " + Math.max(0, hp) + "/" + max;
    }

    public void simulate() throws InterruptedException {
        String place = DEPTHS[0];
        gameController.placeVillains(map);

        System.out.println("villain pos : " + map.getVillainCells());

        while (true) {
            consoleView.render(place, hero, map, "ready");
            Optional<Direction> doMove = consoleView.askMove();
            if (doMove.isEmpty()) {
                System.out.println("\nSee you soon! Byte Byte!!");
                break;
            }
            GameController.MoveResult result = gameController.moveHero(map, doMove.get());
            switch (result) {
                case ERR, RUN -> {
                }
                case FIGHT -> {
                    System.out.println("are we running or fighting boy");
                }
                case WIN -> {
                    System.out.println("Congrats you won");
                    return;
                }
            }

            boolean contact = gameController.advanceVillains(map);
            if (contact) {
                System.out.println("are we running or fighting boy");
            }
        }
    }
}
