package com.yzioual.view;

import com.yzioual.model.map.Direction;
import static com.yzioual.view.ActionType.*;

public record ConsoleAction(ActionType type, Direction direction) {
    public static ConsoleAction move(Direction d) {
        return new ConsoleAction(MOVE, d);
    }

    public static ConsoleAction switchToGui() {
        return new ConsoleAction(SWITCH_TO_GUI, null);
    }

    public static ConsoleAction quit() {
        return new ConsoleAction(QUIT, null);
    }
}
