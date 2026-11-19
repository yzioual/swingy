package com.yzioual.model.artifact;

public record Artifact(String name, ArtifactType type, int bonus) {
    public String toString() {
        return name + " (+" + bonus + " " + type.getStatLabel() + ")";
    }
}