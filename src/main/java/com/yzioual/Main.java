package com.yzioual;

import com.yzioual.controller.GameController;
import com.yzioual.model.hero.Hero;
import com.yzioual.model.hero.HeroType;
import com.yzioual.model.map.Map;
import com.yzioual.view.ConsoleView;

import java.util.Optional;

public class Main {
	void main() {
		ConsoleView consoleView = new ConsoleView();
		Optional<HeroType> type = consoleView.askForHero(HeroType.getListOfHeroTypes());
		if (type.isEmpty()) {
			System.out.println("You didn't pick a hero!");
			return;
		}

		HeroType newType = type.get();
		Hero hero = new Hero.HeroBuilder()
				.setName("ALI")
				.setHeroType(newType)
				.setLevel(1)
				.build();

		GameController gameController = new GameController(consoleView, hero);
		gameController.play();
    }
}
