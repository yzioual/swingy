package com.yzioual.model;

public record Artifact(String name, ArtifactType type, int bonus) {
    public String toString() {
        return name + " (+" + bonus + " " + type.toString() + ")";
    }
}