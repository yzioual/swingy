package com.yzioual.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import static com.yzioual.view.UIConstants.*;

public class Bar extends JComponent {
    private double frac;

    void set(double f) {
        f = Math.max(0, Math.min(1, f));
        if (f != frac) {
            frac = f;
            repaint();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 6);
    }

    @Override
    protected void paintComponent(Graphics gr) {
        Graphics2D g = Utilities.aa(gr);
        int segments = 20, gap = 2;
        int segWidth = Math.max(2, (getWidth() - gap * (segments - 1)) / segments);
        int filled = (int) Math.round(frac * segments);
        for (int i = 0; i < segments; i++) {
            g.setColor(i < filled ? ACCENT : LINE);
            g.fillRect(i * (segWidth + gap), 0, segWidth, getHeight());
        }
        g.dispose();
    }
}

