package com.yzioual;

import com.yzioual.controller.GameController;
import com.yzioual.model.hero.Hero;
import com.yzioual.model.hero.HeroType;
//import com.yzioual.validation.ValidationService;
import com.yzioual.view.ConsoleView;

import java.util.List;
import java.util.Optional;

public class Main {

	void main() {
		ConsoleView consoleView = new ConsoleView();
		Optional<HeroType> type = consoleView.askForHero(HeroType.getListOfHeroTypes());
		if (type.isEmpty()) {
			System.out.println("You didn't pick a hero! Try again!");
			return;
		}
		Hero hero = createHero(consoleView, type.get());
		if (hero == null) return;
		new GameController(consoleView, hero).play();
	}

	/** Keeps asking until the name passes validation. Returns null if input closes. */
	private static Hero createHero(ConsoleView view, HeroType type) {
		while (true) {
			Optional<String> name = view.askHeroName();
			if (name.isEmpty()) {
				return null;
			}

			return new Hero.HeroBuilder()
					.setName(name.get())
					.setHeroType(type)
					.build();                         // level 1, XP 0 by default

			/*
			List<String> errors = ValidationService.validate(hero);
			if (errors.isEmpty()) {
				return hero;
			}
			view.showErrors(errors);
			 */
		}
	}
}