package com.yzioual.controller;

import com.yzioual.model.hero.Hero;
import com.yzioual.model.map.Direction;
import com.yzioual.model.map.Map;
import com.yzioual.model.map.Position;
import com.yzioual.model.villain.Villain;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GameController {
    private static final int CHASE_RADIUS = 3;

    // return a fight result which we will use to either move on to the next round or end the game
    public static void fight(Hero hero, Villain villain) {
    }

    private boolean isInside(Map map, Position p) { return p.x() >= 0 && p.y() >= 0 && p.x() < map.getSize() && p.y() < map.getSize(); }
    private boolean isOnBorder(Map map, Position p) { return p.x() == 0 || p.y() == 0 || p.x() == map.getSize() - 1 || p.y() == map.getSize() - 1;}

    public enum MoveResult { ERR, FIGHT, WIN, RUN }

    public MoveResult moveHero(Map map, Direction direction) {
        Position next = map.getHeroPosition().move(direction);
        if (isOnBorder(map, next)) {
            System.out.println("You reached shore and survived! Well done ");
            return MoveResult.WIN;
        }
        if (!isInside(map, next)) {
            return MoveResult.ERR;
        }
        map.setPreviousHeroPosition(map.getHeroPosition());
        map.setCurrentPosition(next);
        return MoveResult.RUN;
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

    public void placeVillains(Map map) {
        int seed = 3;
        int VILLAIN_PERCENT = 3;
        Random random = new Random(seed);
        Position center =  new Position(map.getSize() / 2, map.getSize() / 2);
        int count = Math.max(1, map.getSize() * map.getSize() * VILLAIN_PERCENT / 100);

        while (map.getVillainCells().size() < count) {
            Position p = new Position(random.nextInt(map.getSize()), random.nextInt(map.getSize()) );

            if (!p.equals(center)) {
                map.addVillainCells(p);
            }
        }
    }
}