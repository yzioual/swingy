package com.yzioual;

import com.yzioual.controller.GameController;
import com.yzioual.view.ViewMode;

import java.awt.GraphicsEnvironment;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		String mode = null;
		if (args != null && args.length > 0) {
			String arg = args[0].trim().toLowerCase();
			if (arg.equals("console") || arg.equals("gui")) {
				mode = arg;
			} else {
				System.err.println("Error: Invalid mode '" + args[0] + "'. Available modes are 'console' or 'gui'.");
				System.err.println("Usage: java -jar swingy.jar [console|gui]");
				System.exit(1);
			}
		}

		if (mode == null) {
			mode = askMode();
		}

		ViewMode initialMode = "gui".equals(mode) ? ViewMode.GUI : ViewMode.CONSOLE;
		if (initialMode == ViewMode.GUI && GraphicsEnvironment.isHeadless()) {
			System.out.println("No display detected (headless environment). Falling back to console mode.");
			initialMode = ViewMode.CONSOLE;
		}

		new GameController(initialMode).play();
	}

    private static String askMode() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== CHOOSE MODE ===");
        System.out.println("1) Console");
        System.out.println("2) GUI");

        while (true) {
            System.out.print("Select mode (1 or 2) > ");
            if (!scanner.hasNextLine()) {
                return "console";
            }

            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("1") || input.equals("console") || input.equals("c")) {
                return "console";
            }

            if (input.equals("2") || input.equals("gui") || input.equals("g")) {
                return "gui";
            }
            System.out.println("Please type 1 for Console or 2 for GUI.");
        }
    }
}
