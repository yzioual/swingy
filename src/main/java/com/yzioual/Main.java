package com.yzioual;

import com.yzioual.controller.BattleSimulator;
import com.yzioual.controller.GameController;
import com.yzioual.model.hero.Hero;
import com.yzioual.model.hero.HeroType;
import com.yzioual.model.map.Map;
import com.yzioual.model.villain.Villain;
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

		Map map = new Map(hero);
		GameController gameController = new GameController();
		BattleSimulator simulator = new BattleSimulator(gameController, consoleView, hero, map);

        try {
            simulator.simulate();
        } catch (InterruptedException e) {
			System.out.println("something went wrong");
        }
    }
}
