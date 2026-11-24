package com.yzioual;

import com.yzioual.controller.GameController;
import com.yzioual.view.ViewMode;

import java.awt.GraphicsEnvironment;
import java.util.Scanner;

public class Main {
	private static final String GUI = "gui";
    private static final String CONSOLE = "console";
    private static final String HEADLESS_ENV = "No display detected (headless environment)."
        + "Falling back to console mode.";

    void main(String[] args) {
		String mode = null;
		if (args != null && args.length > 0) {
			String arg = args[0].trim().toLowerCase();
			if (arg.equals(CONSOLE) || arg.equals(GUI)) {
				mode = arg;
			} else {
				System.err.println("Error: Invalid mode '" + args[0] + "'. Available modes are 'console' or 'gui'.");
				System.err.println("Usage: java -jar swingy.jar [console|gui]");
				System.exit(1);
			}
		}

		if (mode == null) mode = askMode();

        ViewMode initialMode = null;
        if (GUI.equals(mode)) {
		    initialMode = ViewMode.GUI;
        } else {
            initialMode = ViewMode.CONSOLE;
        }

		if (hasGUIEnvWell(initialMode)) {
			System.out.println(HEADLESS_ENV);
			initialMode = ViewMode.CONSOLE;
		}

		new GameController(initialMode).play();
	}

    private boolean hasGUIEnvWell(ViewMode initialMode) {
        return initialMode == ViewMode.GUI && GraphicsEnvironment.isHeadless();
    }

    private static boolean isGUI(String input) {
        return input.equals("2") || input.equals(GUI) || input.equals("g");
    }

    private static boolean isConsole(String input) {
        return input.equals("1") || input.equals(CONSOLE) || input.equals("c");
    }

    @SuppressWarnings("resource")
    private static String askMode() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== CHOOSE MODE ===");
        System.out.println("1) Console");
        System.out.println("2) GUI");

        while (true) {
            System.out.print("Select mode (1 or 2) > ");
            if (!scanner.hasNextLine()) return CONSOLE;

            String input = scanner.nextLine().trim().toLowerCase();
            if (isConsole(input)) {
                return CONSOLE;
            }

            if (isGUI(input)) {
                return GUI;
            }
            System.out.println("Please type 1 for Console or 2 for GUI.");
        }
    }
}
