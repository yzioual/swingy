package com.yzioual.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import com.yzioual.model.artifact.Artifact;
import static com.yzioual.view.UIConstants.*;

public class Utilities {
    public Utilities() { }

    public static Graphics2D aa(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        return g2;
    }

    public static String pretty(String s) {
        if (s == null || s.isBlank()) return "";
        String t = s.replace('_', ' ').trim();
        if (!t.equals(t.toUpperCase())) return t;
        StringBuilder sb = new StringBuilder();
        boolean cap = true;
        for (char ch : t.toCharArray()) {
            sb.append(cap ? Character.toUpperCase(ch) : Character.toLowerCase(ch));
            cap = ch == ' ';
        }
        return sb.toString();
    }

    public static JLabel label(String s, Font f, Color c) {
        JLabel l = new JLabel(s);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    public static JPanel spacer() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    public static String gearName(Artifact a) {
        return a != null ? a.toString() : "None";
    }

    public static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static JLabel text(String s, int width, Font f, Color c) {
        return Utilities.label("<html><div style='width:" + width + "px'>"
                + esc(s).replace("\n", "<br>") + "</div></html>", f, c);
    }

    public static Image icon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = aa(img.getGraphics());
        g.setColor(ACCENT);
        g.fill(new Ellipse2D.Double(2, 2, 28, 28));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(10, 10, 12, 12));
        g.dispose();
        return img;
    }
}
