package com.yzioual.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;

import com.yzioual.model.map.*;
import static com.yzioual.view.UIConstants.*;

public class PadButton extends JButton {
    private final Direction dir;

    PadButton(Direction dir, String tip) {
        this.dir = dir;
        setToolTipText(tip);
        getAccessibleContext().setAccessibleName(tip);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public Direction getDir() { return this.dir; }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(64, 42);
    }

    @Override
    protected void paintComponent(Graphics gr) {
        Graphics2D g = Utilities.aa(gr);
        int w = getWidth(), h = getHeight();
        boolean down = getModel().isArmed() && getModel().isPressed();
        boolean hover = getModel().isRollover();
        RoundRectangle2D.Double r = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 12, 12);
        g.setColor(down ? ACCENT : hover ? new Color(0xEFEFEA) : SURFACE);
        g.fill(r);
        g.setColor(down ? ACCENT : LINE);
        g.draw(r);
        Path2D.Double tri = new Path2D.Double();
        tri.moveTo(0, -5.5);
        tri.lineTo(6, 4.5);
        tri.lineTo(-6, 4.5);
        tri.closePath();
        int quarter = switch (dir) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            default -> 3;
        };
        AffineTransform at = AffineTransform.getTranslateInstance(w / 2.0, h / 2.0);
        at.rotate(Math.toRadians(90 * quarter));
        g.setColor(down ? Color.WHITE : TEXT);
        g.fill(at.createTransformedShape(tri));
        g.dispose();
    }
}
