package com.yzioual.model.maps;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Map {
    private final int size;
    private final Set<Position> villainCells = new HashSet<>();
    private Position currentPosition;
    private Position previousPosition;

    public Map(int heroLevel) {
        this.size = sizeForLevel(heroLevel);
        Position center = new Position(size / 2, size / 2);
        this.currentPosition = center;
        this.previousPosition = center;
        System.out.println("[DEBUG] Map initialized: heroLevel=" + heroLevel + ", size=" + size + "x" + size + ", center=" + center);
    }



    public static int sizeForLevel(int level) {
        /* the formula is determined by the subject */
        return (level - 1) * 5 + 10 - (level % 2);
    }

    public MoveResult move(Direction direction) {
        Position next = currentPosition.move(direction);
        System.out.println("[DEBUG] Map.move: current=" + currentPosition + ", direction=" + direction + ", next=" + next);
        if (!isInside(next)) {
            System.err.println("[DEBUG] Map.move: Invalid move! Position " + next + " is out of bounds (map size: " + size + ")");
            throw new IllegalArgumentException("Cannot move outside the map");
        }
        previousPosition = currentPosition;
        currentPosition = next;

        if (villainCells.contains(next)) {
            System.out.println("[DEBUG] Map.move: Encountered villain at " + next);
            return MoveResult.ENCOUNTER;
        }
        MoveResult result = isOnBorder(next) ? MoveResult.REACHED_BORDER : MoveResult.MOVED;
        System.out.println("[DEBUG] Map.move: Result=" + result);
        return result;
    }

    public void retreat() {
        System.out.println("[DEBUG] Map.retreat: retreating from " + currentPosition + " back to " + previousPosition);
        currentPosition = previousPosition;
    }

    public void removeVillain(Position position) {
        boolean removed = villainCells.remove(position);
        System.out.println("[DEBUG] Map.removeVillain: position=" + position + ", removed=" + removed + ", remaining=" + villainCells.size());
    }

    public boolean isHeroOnBorder()           { return isOnBorder(currentPosition); }
    public boolean hasVillainAt(Position p)   { return villainCells.contains(p); }
    public int getSize()                      { return size; }
    public Position getHeroPosition()         { return currentPosition; }
    public Position getPreviousPosition()     { return previousPosition; }

    public Set<Position> getVillainCells()    {
        return Collections.unmodifiableSet(villainCells);
    }

    public boolean addVillainCells(Position position) {
        return villainCells.add(position);
    }

    private boolean isInside(Position p) {
        return p.x() >= 0 && p.y() >= 0 && p.x() < size && p.y() < size;
    }

    private boolean isOnBorder(Position p) {
        return p.x() == 0 || p.y() == 0 || p.x() == size - 1 || p.y() == size - 1;
    }

    public void print() {
        System.out.println();
        for (int y = 0; y < size; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < size; x++) {
                Position p = new Position(x, y);

                if (p.equals(currentPosition)) {
                    row.append("@ ");
                } else if (hasVillainAt(p)) {
                    row.append("V ");
                } else {
                    row.append(". ");
                }
            }
            System.out.println(row);
        }
    }
}