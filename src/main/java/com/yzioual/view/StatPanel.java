package com.yzioual.view;

import java.awt.*;
import javax.swing.*;

import static com.yzioual.view.UIConstants.*;

public class StatPanel extends JPanel {
    private Stat attack;
    private Stat defense;
    private Stat hitPoints;

    public StatPanel() {
        setOpaque(false);
        setLayout(new GridLayout(1, 3, 8, 0));
        attack = new Stat("ATK");
        defense = new Stat("DEF");
        hitPoints = new Stat("HP");
        setPreferredSize(new Dimension(300, 52));
    }

    public void setStats(int attack, int defense, int hitPoints) {
        this.attack.set((int) attack);
        this.defense.set((int) defense);
        this.hitPoints.set((int) hitPoints);
    }

    private static final class Stat extends JComponent {
        private final String label;
        private long value;

        Stat(String label) {
            this.label = label;
        }

        public void set(long v) {
            if (v != value) {
                value = v;
                repaint();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(90, 52);
        }

        private static void pixelBox(Graphics2D g, int x, int y, int w, int h, Color fill, Color border) {
            g.setColor(border);
            g.fillRect(x, y, w, h);
            g.setColor(fill);
            g.fillRect(x + 2, y + 2, w - 4, h - 4);
            g.setColor(border);
            g.fillRect(x + 2, y + h - 4, w - 4, 2);
        }

        @Override
        protected void paintComponent(Graphics gr) {
            Graphics2D g = Utilities.aa(gr);
            pixelBox(g, 0, 0, getWidth(), getHeight(), SURFACE, LINE);
            g.setFont(SMALL);
            g.setColor(MUTED);
            g.drawString(label, 12, 20);
            g.setFont(font(Font.BOLD, 20f));
            g.setColor(TEXT);
            g.drawString(String.valueOf(value), 12, 43);
            g.dispose();
        }
    }
}
