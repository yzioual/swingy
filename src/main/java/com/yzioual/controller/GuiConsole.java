package com.yzioual.controller;

import com.yzioual.model.hero.Hero;
import com.yzioual.view.GUIView;

public class GuiConsole extends GuiController {
    public GuiConsole() {
        super();
    }

    public GuiConsole(Hero hero) {
        super(hero);
    }

    public GuiConsole(GUIView guiView, Hero hero) {
        super(guiView, hero);
    }
}
