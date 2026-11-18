package com.yzioual.model.map;

import com.yzioual.model.hero.Hero;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Map {
    private final int size;
    private final Set<Position> villainCells = new HashSet<>();
    private Position currentHeroPosition;
    private Position previousHeroPosition;

    public Map(Hero hero) {
        this.size = sizeForLevel(hero.getLevel());
        Position center = new Position(size / 2, size / 2);
        currentHeroPosition = center;
        previousHeroPosition = center;
    }

    public static int sizeForLevel(int level) {
        return (level - 1) * 5 + 10 - (level % 2);
    }
    public Position getHeroPosition() { return this.currentHeroPosition; }
    public void retreat() { currentHeroPosition = previousHeroPosition; }
    public int getSize() { return size; }
    public Set<Position> getVillainCells() { return Collections.unmodifiableSet(villainCells); }
    public void clearVillainsCells() { villainCells.clear(); }
    public void replaceVillainsCells(Set<Position> newVillainsCells) {
        villainCells.addAll(newVillainsCells);
    }
    public boolean hasVillainAt(Position position) { return villainCells.contains(position); }
    public void addVillainCells(Position position) { villainCells.add(position); }

    public void setCurrentPosition(Position position) { currentHeroPosition = position; }
    public void setPreviousHeroPosition(Position position) { previousHeroPosition = position; }
}