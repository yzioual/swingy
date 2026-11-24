package com.yzioual.view;


import java.awt.*;

public class UIConstants {
    public static final Color BG        = new Color(0x1B1B2F);
    public static final Color SURFACE   = new Color(0x242444);
    public static final Color LINE      = new Color(0x4A4A7A);
    public static final Color TEXT      = new Color(0xF2F0E6);
    public static final Color MUTED     = new Color(0x9A9AC0);
    public static final Color ACCENT    = new Color(0xFFC857);
    public static final Color ACCENT_DARK = new Color(0xD9A63A);
    public static final Color ERROR     = new Color(0xFF6B6B);

    public static final Font TITLE = font(Font.BOLD, 20f);
    public static final Font BOLD  = font(Font.BOLD, 14f);
    public static final Font REG   = font(Font.BOLD, 13f);
    public static final Font SMALL = font(Font.BOLD, 11.5f);

    private UIConstants() {
    }

    public static Font font(int style, float size) {
        return new Font(Font.MONOSPACED, style, 12)
            .deriveFont(size);
    }
}
