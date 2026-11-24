package com.yzioual.view;

import javax.swing.JComponent;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MapPanel extends JComponent {
    private int size;
    private int heroX;
    private int heroY;
    private boolean[] fears = new boolean[0];

    void update(int size, int heroX, int heroY, boolean[] fears) {
        this.size = size;
        this.heroX = heroX;
        this.heroY = heroY;
        this.fears = fears;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics gr) {
        if (size <= 0) return;
        BufferedImage img = PixelArt.renderMap(size, heroX, heroY, fears);

        Insets in = getInsets();
        int aw = getWidth() - in.left - in.right;
        int ah = getHeight() - in.top - in.bottom;

        double scale = Math.min(aw, ah) / (double) img.getWidth();
        if (scale >= 2) scale = Math.floor(scale);   // whole-number scale keeps pixels even
        int w = (int) (img.getWidth() * scale);
        int h = (int) (img.getHeight() * scale);

        Graphics2D g = (Graphics2D) gr.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(img, in.left + (aw - w) / 2, in.top + (ah - h) / 2, w, h, null);
        g.dispose();
    }
}
