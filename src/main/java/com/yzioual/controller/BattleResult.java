package com.yzioual.controller;

import static com.yzioual.controller.BattleOutcome.SMASH;
import static com.yzioual.controller.BattleOutcome.WIN;

import java.util.List;

public record BattleResult(BattleOutcome outcome, List<String> log) {
    public boolean didHeroWin() {
        return outcome == WIN || outcome == SMASH;
    }

    public boolean didHeroSmash() {
        return outcome == SMASH;
    }
}
