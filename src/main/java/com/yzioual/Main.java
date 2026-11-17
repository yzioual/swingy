package com.yzioual;

import com.yzioual.controller.BattleSimulator;

public class Main {
	void main() {
		System.out.println("[DEBUG] Starting Swingy application...");
		try {
			System.out.println("[DEBUG] Instantiating BattleSimulator...");
			BattleSimulator simulator = new BattleSimulator();
			System.out.println("[DEBUG] Running simulation...");
			simulator.simulate();
			System.out.println("[DEBUG] Simulation completed successfully.");
		} catch (InterruptedException ie) {
			System.err.println("[DEBUG] InterruptedException caught: caused by pause()");
			ie.printStackTrace();
		} catch (Exception exception) {
			System.err.println("[DEBUG] Something went wrong!");
			System.err.println("[DEBUG] Exception: " + exception.getClass().getName());
			System.err.println("[DEBUG] Message: " + exception.getMessage());
			System.err.println("[DEBUG] Stack trace:");
			exception.printStackTrace();
		}
	}
}