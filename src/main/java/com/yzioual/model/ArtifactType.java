package com.yzioual.model;

public enum ArtifactType {
    WEAPON("Weapon", "Attack"),
    ARMOR("Armor", "Defense"),
    HELM("Helm", "Hit Points");

    private final String name;
    private final String statLabel;

    ArtifactType(String name, String statLabel) {
        this.name = name;
        this.statLabel = statLabel;
    }

    public String getName() {
        return this.name;
    }

    public String getStatLabel() {
        return this.statLabel;
    }
}