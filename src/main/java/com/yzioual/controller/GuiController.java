package com.yzioual.controller;

import com.yzioual.model.hero.Hero;
import com.yzioual.view.GUIView;
import com.yzioual.view.ViewMode;

public class GuiController extends GameController {
    public GuiController() {
        super(ViewMode.GUI);
    }

    public GuiController(Hero hero) {
        super(ViewMode.GUI, null, hero);
    }

    public GuiController(GUIView guiView, Hero hero) {
        super(ViewMode.GUI, null, hero);
    }
}
