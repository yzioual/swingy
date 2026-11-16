package com.yzioual.model.maps;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GameMap {
        public static final int VILLAIN_PERCENT = 12;

        private final int size;
        private final Set<Position> villainCells = new HashSet<>();
        private Position heroPos;
        private Position prevPos;

    public GameMap(int heroLevel) {
        this(heroLevel, new Random());
    }

    public GameMap(int heroLevel, Random random) {
        this.size = sizeForLevel(heroLevel);
        Position center = new Position(size / 2, size / 2);
        this.heroPos = center;
        this.prevPos = center;
        placeVillains(center, random);
    }

    private void placeVillains(Position center, Random random) {
        int count = Math.max(1, size * size * VILLAIN_PERCENT / 100);

        while (villainCells.size() < count) {
            Position p = new Position(random.nextInt(size),
                    random.nextInt(size));

            if (!p.equals(center)) {
                villainCells.add(p);
            }
        }
    }

    public static int sizeForLevel(int level) {
        return (level - 1) * 5 + 10 - (level % 2);
    }

    public MoveResult move(Direction direction) {
        Position next = heroPos.move(direction);
        if (!isInside(next)) {
            throw new IllegalArgumentException("Cannot move outside the map");
        }
        prevPos = heroPos;
        heroPos = next;

        if (villainCells.contains(next)) {
            return MoveResult.ENCOUNTER;
        }
        return isOnBorder(next) ? MoveResult.REACHED_BORDER : MoveResult.MOVED;
    }

    public void retreat() {
        heroPos = prevPos;
    }

    public void removeVillain(Position position) {
        villainCells.remove(position);
    }

    public boolean isHeroOnBorder()           { return isOnBorder(heroPos); }
    public boolean hasVillainAt(Position p)   { return villainCells.contains(p); }
    public int getSize()                      { return size; }
    public Position getHeroPosition()         { return heroPos; }
    public Position getPreviousPosition()     { return prevPos; }
    public Set<Position> getVillainCells()    { return Collections.unmodifiableSet(villainCells); }

    private boolean isInside(Position p) {
        return p.x() >= 0 && p.y() >= 0 && p.x() < size && p.y() < size;
    }

    private boolean isOnBorder(Position p) {
        return p.x() == 0 || p.y() == 0 || p.x() == size - 1 || p.y() == size - 1;
    }
}