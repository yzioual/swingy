package com.yzioual.view;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import javax.swing.*;


import static com.yzioual.view.UIConstants.*;

public class Button extends JButton {
    private final boolean primary;

    Button(String text, boolean primary) {
        super(text);
        this.primary = primary;
        setFont(BOLD);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                Math.max(92,getFontMetrics(getFont()).stringWidth(getText()) + 36), 38);
    }

    @Override
    protected void paintComponent(Graphics gr) {
        Graphics2D g = Utilities.aa(gr);
        int w = getWidth(), h = getHeight();
        boolean hover = getModel().isRollover() || getModel().isArmed();
        RoundRectangle2D.Double r = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 10, 10);
        if (primary) {
            g.setColor(hover ? ACCENT_DARK : ACCENT);
            g.fill(r);
        } else {
            g.setColor(hover ? BG : SURFACE);
            g.fill(r);
            g.setColor(LINE);
            g.draw(r);
        }
        if (isFocusOwner()) {
            g.setColor(primary ? new Color(255, 255, 255, 170) : ACCENT);
            g.draw(new RoundRectangle2D.Double(3.5, 3.5, w - 7, h - 7, 7, 7));
        }
        g.setFont(getFont());
        g.setColor(primary ? Color.WHITE : TEXT);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(getText(), (w - fm.stringWidth(getText())) / 2f, (h + fm.getAscent() - fm.getDescent()) / 2f);
        g.dispose();
    }
}


