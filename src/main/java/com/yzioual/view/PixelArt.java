package com.yzioual.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

final class PixelArt {
    static final int TILE = 8;
    private static final Color WATER_A   = new Color(0x2B5F8A);
    private static final Color WATER_B   = new Color(0x2F6896);
    private static final Color WAVE      = new Color(0x6FA8D6);
    private static final Color SAND      = new Color(0xE3C98A);
    private static final Color SAND_DARK = new Color(0xB8975A);

    private static final String[] HERO = {
            "..hhhh..",
            ".hvvvvh.",
            ".hvvvvh.",
            "..hhhh..",
            ".bbbbbb.",
            "bbbbbbbb",
            ".bb..bb.",
            ".kk..kk."
    };

    private static final String[] FEAR = {
            "..rrrr..",
            ".rrrrrr.",
            "rwkrrwkr",
            "rrrrrrrr",
            "rrrrrrrr",
            "rrrrrrrr",
            "rr.rr.rr",
            "r..rr..r"
    };

    private PixelArt() {}

    static BufferedImage renderMap(int size, int heroX, int heroY, boolean[] fears) {
        BufferedImage img = new BufferedImage(size * TILE, size * TILE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean shore = x == 0 || y == 0 || x == size - 1 || y == size - 1;
                if (shore) {
                    sand(g, x, y);
                } else {
                    water(g, x, y);
                }
            }
        }
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (fears[y * size + x] && !(x == heroX && y == heroY)) {
                    sprite(g, FEAR, x * TILE, y * TILE);
                }
            }
        }
        sprite(g, HERO, heroX * TILE, heroY * TILE);

        g.dispose();
        return img;
    }

    private static void water(Graphics2D g, int x, int y) {
        int ox = x * TILE, oy = y * TILE;
        g.setColor(((x + y) & 1) == 0 ? WATER_A : WATER_B);
        g.fillRect(ox, oy, TILE, TILE);
        if ((x * 7 + y * 13) % 5 == 0) {          // a few wave marks, in fixed places
            g.setColor(WAVE);
            g.fillRect(ox + 2, oy + 3, 2, 1);
            g.fillRect(ox + 5, oy + 5, 2, 1);
        }
    }

    private static void sand(Graphics2D g, int x, int y) {
        int ox = x * TILE, oy = y * TILE;
        g.setColor(SAND);
        g.fillRect(ox, oy, TILE, TILE);
        if ((x * 5 + y * 11) % 4 == 0) {
            g.setColor(SAND_DARK);
            g.fillRect(ox + 3, oy + 4, 1, 1);
            g.fillRect(ox + 6, oy + 2, 1, 1);
        }
    }

    private static void sprite(Graphics2D g, String[] rows, int ox, int oy) {
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length(); c++) {
                Color color = colorOf(rows[r].charAt(c));
                if (color != null) {
                    g.setColor(color);
                    g.fillRect(ox + c, oy + r, 1, 1);
                }
            }
        }
    }

    private static Color colorOf(char ch) {
        return switch (ch) {
            case 'h' -> new Color(0xC9D1D9);   // helmet
            case 'v' -> new Color(0x7FE7FF);   // visor
            case 'b' -> new Color(0x2D6CDF);   // wetsuit
            case 'k' -> new Color(0x1B1B2F);   // dark details
            case 'r' -> new Color(0xD64545);   // fear
            case 'w' -> Color.WHITE;           // eyes
            default  -> null;
        };
    }
}
