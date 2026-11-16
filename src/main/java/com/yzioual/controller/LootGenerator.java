package com.yzioual.controller;

import com.yzioual.model.Artifact;
import com.yzioual.model.ArtifactType;
import com.yzioual.model.Villain;

import java.util.Optional;
import java.util.Random;

public final class LootGenerator {
    private static final int DROP_PERCENT = 40;

    // [tier] where tier 0 = Fleeting, 1 = Vivid, 2 = Lucid
    private static final String[] WEAPONS = {"Driftwood Spear", "Steel Harpoon", "Golden Trident"};
    private static final String[] ARMORS  = {"Torn Wetsuit", "Neoprene Suit", "Titanium Dive Suit"};
    private static final String[] HELMS   = {"Cracked Mask", "Dive Mask", "Deep-Sea Helmet"};

    private static final int[] WEAPON_BONUS = {3, 6, 10};
    private static final int[] ARMOR_BONUS  = {2, 4, 7};
    private static final int[] HELM_BONUS   = {10, 20, 35};

    private LootGenerator() {}

    public static Optional<Artifact> roll(Villain villain, Random random) {
        if (random.nextInt(100) >= DROP_PERCENT) {
            return Optional.empty();
        }
        int tier = villain.power() <= 1 ? 0 : villain.power() <= 3 ? 1 : 2;
        ArtifactType type = ArtifactType.values()[random.nextInt(ArtifactType.values().length)];

        String[] names = switch (type) {
            case WEAPON -> WEAPONS;
            case ARMOR  -> ARMORS;
            case HELM   -> HELMS;
        };
        int base = switch (type) {
            case WEAPON -> WEAPON_BONUS[tier];
            case ARMOR  -> ARMOR_BONUS[tier];
            case HELM   -> HELM_BONUS[tier];
        };
        int bonus = base + random.nextInt(base / 4 + 1);   // a little luck on top
        return Optional.of(new Artifact(names[tier], type, bonus));
    }
}