package com.yzioual;

import com.yzioual.controller.GameController;
import com.yzioual.view.ViewMode;

import java.awt.GraphicsEnvironment;

public class Main {
	private static final String GUI = "gui";
    private static final String CONSOLE = "console";
    private static final String HEADLESS_ENV = "No display detected (headless environment)."
        + "Falling back to console mode.";

    public static void main(String[] args) {
		String mode = null;
        try {
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

            if (mode == null) {
                System.err.println("Usage: java -jar swingy.jar [console|gui]");
                return;
            }

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

        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Usage: java -jar swingy.jar [console|gui]");
            return;
        }
	}

    private static boolean hasGUIEnvWell(ViewMode initialMode) {
        return initialMode == ViewMode.GUI && GraphicsEnvironment.isHeadless();
    }
}
