package com.yzioual.view;

import com.yzioual.model.artifact.Artifact;
import com.yzioual.model.hero.Hero;
import com.yzioual.model.hero.HeroType;
import com.yzioual.model.map.Direction;
import com.yzioual.model.map.Map;
import com.yzioual.model.map.Position;
import com.yzioual.model.villain.Villain;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class GUIView {
    private static final Color BG        = new Color(0x1B1B2F);
    private static final Color SURFACE   = new Color(0x242444);
    private static final Color LINE      = new Color(0x4A4A7A);
    private static final Color TEXT      = new Color(0xF2F0E6);
    private static final Color MUTED     = new Color(0x9A9AC0);
    private static final Color ACCENT    = new Color(0xFFC857);
    private static final Color ACCENT_DARK = new Color(0xD9A63A);
    private static final Color HOVER     = new Color(0x30305A);
    private static final Color ERROR     = new Color(0xFF6B6B);

    private static final Font TITLE = font(Font.BOLD, 20f);
    private static final Font BOLD  = font(Font.BOLD, 14f);
    private static final Font REG   = font(Font.BOLD, 13f);
    private static final Font SMALL = font(Font.BOLD, 11.5f);


    private JFrame frame;
    private JLabel nameLabel;
    private JLabel classLabel;
    private JLabel dayLabel;
    private JLabel depthLabel;
    private Stat atk;
    private Stat def;
    private Stat hp;
    private Bar xpBar;
    private JLabel xpLabel;
    private JLabel gearLabel;
    private JLabel statusLabel;
    private MapPanel mapPanel;
    private Consumer<Direction> moveListener;
    private Runnable switchListener;

    public GUIView() {
        if (!GraphicsEnvironment.isHeadless()) {
            initUI();
        }
    }

    public void setSwitchListener(Runnable switchListener) {
        this.switchListener = switchListener;
    }

    public Runnable getSwitchListener() {
        return switchListener;
    }

    private static Font font(int style, float size) {
        return new Font(Font.MONOSPACED, style, 12).deriveFont(size);
    }

    private static Graphics2D aa(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        return g2;
    }

    private static void pixelBox(Graphics2D g, int x, int y, int w, int h, Color fill, Color border) {
        g.setColor(border);
        g.fillRect(x, y, w, h);
        g.setColor(fill);
        g.fillRect(x + 2, y + 2, w - 4, h - 4);
        g.setColor(border);
        g.fillRect(x + 2, y + h - 4, w - 4, 2);
    }


    private void initUI() {
        frame = new JFrame("Swingy - Ocean Depths");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(icon());
        frame.setSize(620, 800);
        frame.setMinimumSize(new Dimension(520, 680));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(18, 22, 18, 22));
        frame.setContentPane(root);

        // header
        nameLabel = label("Hero", TITLE, TEXT);
        classLabel = label(" ", REG, MUTED);
        dayLabel = label("Day 1", TITLE, TEXT);
        dayLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        depthLabel = label("Ocean Depths", REG, MUTED);
        depthLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(new Stack().row(nameLabel, 0).row(classLabel, 2), BorderLayout.WEST);

        Btn switchBtn = new Btn("Console (Ctrl+S)", false);
        switchBtn.addActionListener(e -> fireSwitch());

        JPanel eastHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        eastHeader.setOpaque(false);
        eastHeader.add(new Stack().row(dayLabel, 0).row(depthLabel, 2));
        eastHeader.add(switchBtn);
        topRow.add(eastHeader, BorderLayout.EAST);

        atk = new Stat("ATK");
        def = new Stat("DEF");
        hp = new Stat("HP");
        JPanel stats = new JPanel(new GridLayout(1, 3, 8, 0));
        stats.setOpaque(false);
        stats.add(atk);
        stats.add(def);
        stats.add(hp);

        xpBar = new Bar();
        xpLabel = label(" ", SMALL, MUTED);
        xpLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gearLabel = label(" ", SMALL, MUTED);

        root.add(new Stack()
                .row(topRow, 0)
                .row(stats, 16)
                .row(xpBar, 8)
                .row(xpLabel, 4)
                .row(gearLabel, 6), BorderLayout.NORTH);

        // map
        mapPanel = new MapPanel();
        mapPanel.setBorder(new EmptyBorder(6, 0, 6, 0));
        root.add(mapPanel, BorderLayout.CENTER);

        // bottom: status + d-pad
        statusLabel = label(" ", BOLD, TEXT);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        PadButton north = new PadButton(Direction.NORTH, "Move north (W / Up)");
        PadButton west = new PadButton(Direction.WEST, "Move west (A / Left)");
        PadButton south = new PadButton(Direction.SOUTH, "Move south (S / Down)");
        PadButton east = new PadButton(Direction.EAST, "Move east (D / Right)");
        for (PadButton b : new PadButton[]{north, west, south, east}) {
            b.addActionListener(e -> fireMove(b.dir));
        }
        JPanel pad = new JPanel(new GridLayout(2, 3, 6, 6));
        pad.setOpaque(false);
        pad.add(spacer());
        pad.add(north);
        pad.add(spacer());
        pad.add(west);
        pad.add(south);
        pad.add(east);
        JPanel padWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        padWrap.setOpaque(false);
        padWrap.add(pad);
        JLabel hint = label("WASD / Arrows to move · Ctrl+S to switch to Console", SMALL, MUTED);
        hint.setHorizontalAlignment(SwingConstants.CENTER);

        root.add(new Stack().row(statusLabel, 0).row(padWrap, 12).row(hint, 8), BorderLayout.SOUTH);

        bind(north, KeyEvent.VK_W, KeyEvent.VK_UP);
        bind(south, KeyEvent.VK_S, KeyEvent.VK_DOWN);
        bind(west, KeyEvent.VK_A, KeyEvent.VK_LEFT);
        bind(east, KeyEvent.VK_D, KeyEvent.VK_RIGHT);

        JRootPane rp = frame.getRootPane();
        KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        KeyStroke cmdS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        AbstractAction switchAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireSwitch();
            }
        };
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlS, "switchToConsole");
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(cmdS, "switchToConsoleCmd");
        rp.getActionMap().put("switchToConsole", switchAction);
        rp.getActionMap().put("switchToConsoleCmd", switchAction);
    }

    public void fireSwitch() {
        if (switchListener != null) {
            switchListener.run();
        }
    }

    private void bind(PadButton btn, int... keys) {
        JRootPane rp = frame.getRootPane();
        String name = "move" + btn.dir;
        for (int k : keys) {
            rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(k, 0), name);
        }
        rp.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireMove(btn.dir);
            }
        });
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setMoveListener(Consumer<Direction> moveListener) {
        this.moveListener = moveListener;
    }

    private void fireMove(Direction direction) {
        if (moveListener != null) {
            moveListener.accept(direction);
        }
    }

    public void show() {
        if (frame != null) {
            frame.setVisible(true);
        }
    }

    public void hide() {
        if (frame != null) {
            frame.setVisible(false);
        }
    }

    public void dispose() {
        if (frame != null) {
            frame.dispose();
        }
    }

    public void render(String depth, Hero hero, Map map, String status) {
        if (frame == null) return;
        nameLabel.setText(hero.getName());
        classLabel.setText(hero.getHeroType().getName() + "  ·  Level " + hero.getLevel());
        dayLabel.setText("Day " + hero.getLevel());
        depthLabel.setText(depth == null ? " " : depth);
        atk.set(hero.getAttack());
        def.set(hero.getDefense());
        hp.set(hero.getHitPoints());
        xpBar.set(hero.getNextLevelXp() > 0 ? hero.getExperience() / (double) hero.getNextLevelXp() : 0);
        xpLabel.setText(hero.getExperience() + " / " + hero.getNextLevelXp() + " XP");
        String gear = "Weapon: " + gearName(hero.getWeapon())
                + "   ·   Armor: " + gearName(hero.getArmor())
                + "   ·   Helm: " + gearName(hero.getHelm());
        gearLabel.setText(gear);
        gearLabel.setToolTipText(gear);
        statusLabel.setText(status == null || status.isBlank() ? " " : status);

        int size = map.getSize();
        boolean[] fears = new boolean[size * size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                fears[y * size + x] = map.hasVillainAt(new Position(x, y));
            }
        }
        Position p = map.getHeroPosition();
        mapPanel.update(size, p.x(), p.y(), fears);
    }

    public boolean askFight(Villain villain) {
        if (frame == null) return true;
        Stack body = new Stack()
                .row(text("“" + villain.catchphrase() + "”", 340, REG, TEXT), 0)
                .row(text("Power " + villain.power() + "   ·   ATK " + villain.attack()
                        + "   ·   DEF " + villain.defense() + "   ·   HP " + villain.hitPoints(), 340, REG, MUTED), 10);
        return dialog(frame, "Encounter", "A " + villain.name() + " blocks your way!", body,
                new String[]{"Fight", "Run"}, 1, null) == 0;
    }

    public void showBattleResult(List<String> log, boolean won, boolean smash, int xp, int levelsGained) {
        if (frame == null) return;
        Stack body = new Stack();
        int gap = 0;
        if (log != null && !log.isEmpty()) {
            JTextArea area = new JTextArea(String.join("\n", log));
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(REG);
            area.setForeground(TEXT);
            area.setBackground(BG);
            area.setBorder(new EmptyBorder(8, 10, 8, 10));
            area.setCaretPosition(0);
            JScrollPane sp = new JScrollPane(area, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sp.setBorder(new LineBorder(LINE));
            sp.setPreferredSize(new Dimension(360, Math.min(180, Math.max(50, log.size() * 18 + 18))));
            body.row(sp, 0);
            gap = 12;
        }
        if (won) {
            body.row(text("+" + xp + " XP" + (smash ? "  ·  flawless smash" : ""), 340, BOLD, ACCENT), gap);
            if (levelsGained > 0) {
                body.row(text("Level up! You reached level " + levelsGained + "!", 340, BOLD, TEXT), 4);
            }
        } else {
            body.row(text("You were defeated in battle...", 340, REG, MUTED), gap);
        }
        dialog(frame, won ? "Battle Victory" : "Battle Defeat", won ? "Victory" : "Defeat", body,
                new String[]{"Continue"}, 0, null);
    }

    public boolean askEquip(Artifact dropped, Artifact current) {
        if (frame == null) return false;
        Stack body = new Stack()
                .row(text("FOUND", 340, SMALL, MUTED), 0)
                .row(text(dropped.toString(), 340, BOLD, TEXT), 2)
                .row(text("EQUIPPED", 340, SMALL, MUTED), 12)
                .row(text(current != null ? current.toString() : "None", 340, REG, TEXT), 2)
                .row(text("Do you want to keep and equip it?", 340, REG, MUTED), 14);
        return dialog(frame, "Loot Dropped", "You found something", body,
                new String[]{"Equip", "Skip"}, 1, null) == 0;
    }

    public boolean showShore(int day, Hero hero) {
        if (frame == null) return false;
        Stack body = new Stack()
                .row(text("You reached the shore with your fish! Your family eats tonight.", 340, REG, TEXT), 0)
                .row(text("ATK " + hero.getAttack() + "   ·   DEF " + hero.getDefense()
                        + "   ·   HP " + hero.getHitPoints(), 340, BOLD, TEXT), 12)
                .row(text("Weapon: " + gearName(hero.getWeapon()) + "\nArmor: " + gearName(hero.getArmor())
                        + "\nHelm: " + gearName(hero.getHelm()), 340, REG, MUTED), 6)
                .row(text("Ready to dive into Day " + (day + 1) + "?", 340, REG, TEXT), 14);
        return dialog(frame, "Shore Reached", "Day " + day + " complete", body,
                new String[]{"Dive in", "Not now"}, 1, null) == 0;
    }

    public boolean showGameOver() {
        if (frame == null) return false;
        Stack body = new Stack()
                .row(text("Your family starves.", 300, REG, TEXT), 0)
                .row(text("Would you like to try again?", 300, REG, MUTED), 6);
        return dialog(frame, "Game Over", "You drowned", body,
                new String[]{"Try again", "Quit"}, 1, null) == 0;
    }

    public static Hero askCreateHero() {
        return askCreateHero(null);
    }

    public static Hero askCreateHero(Runnable onSwitchToConsole) {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        List<HeroType> types = HeroType.getListOfHeroTypes();

        JTextField name = new JTextField("Marco", 18);
        name.setFont(font(Font.PLAIN, 15f));
        name.setForeground(TEXT);
        name.setCaretColor(ACCENT);
        name.setSelectionColor(LINE);
        name.setBackground(SURFACE);
        Runnable restyle = () -> name.setBorder(new CompoundBorder(
                new LineBorder(name.hasFocus() ? ACCENT : LINE, 2, false), new EmptyBorder(7, 9, 7, 9)));
        restyle.run();
        name.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                restyle.run();
            }

            @Override
            public void focusLost(FocusEvent e) {
                restyle.run();
            }
        });

        JLabel error = label(" ", SMALL, ERROR);
        OptionList list = new OptionList(types);
        for (int[] k : new int[][]{{KeyEvent.VK_UP, -1}, {KeyEvent.VK_DOWN, 1}}) {
            name.getInputMap().put(KeyStroke.getKeyStroke(k[0], 0), "cls" + k[0]);
            name.getActionMap().put("cls" + k[0], new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    list.move(k[1]);
                }
            });
        }

        Stack body = new Stack()
                .row(label("NAME", SMALL, MUTED), 0)
                .row(name, 4)
                .row(error, 2)
                .row(label("CLASS", SMALL, MUTED), 8)
                .row(list, 4);

        final String[] chosen = new String[1];
        BooleanSupplier validate = () -> {
            String n = name.getText().trim();
            if (n.length() < 2 || n.length() > 20) {
                error.setText("Hero name must be between 2 and 20 characters.");
                name.requestFocusInWindow();
                return false;
            }
            chosen[0] = n;
            return true;
        };
        String[] labels = onSwitchToConsole != null
                ? new String[]{"Start", "Console (Ctrl+S)", "Cancel"}
                : new String[]{"Start", "Cancel"};
        int r = dialog(null, "Create Hero - Swingy", "Create your hero", body,
                labels, labels.length - 1, validate, () -> {
                    name.requestFocusInWindow();
                    name.selectAll();
                }, onSwitchToConsole);
        if (r == 0 && list.selected() != null) {
            return new Hero.HeroBuilder()
                    .setName(chosen[0])
                    .setHeroType(list.selected())
                    .build();
        } else if (onSwitchToConsole != null && r == 1) {
            onSwitchToConsole.run();
            return null;
        }
        return null;
    }

    public static Hero askSelectOrCreateHero(List<Hero> savedHeroes, Runnable onSwitchToConsole) {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        if (savedHeroes == null || savedHeroes.isEmpty()) {
            return askCreateHero(onSwitchToConsole);
        }

        HeroSelectPanel selectPanel = new HeroSelectPanel(savedHeroes);
        JScrollPane sp = new JScrollPane(selectPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(new LineBorder(LINE));
        sp.setPreferredSize(new Dimension(420, Math.min(260, Math.max(120, savedHeroes.size() * 60 + 10))));

        Stack body = new Stack()
                .row(label("SAVED HEROES", SMALL, MUTED), 0)
                .row(sp, 6);

        int r = dialog(null, "Select Hero - Swingy", "Choose your hero", body,
                new String[]{"Play", "Create New", "Console (Ctrl+S)", "Quit"}, 3, null, null, onSwitchToConsole);
        if (r == 0) {
            return selectPanel.selected();
        } else if (r == 1) {
            return askCreateHero(onSwitchToConsole);
        } else if (r == 2) {
            if (onSwitchToConsole != null) onSwitchToConsole.run();
            return null;
        }
        return null;
    }

    private static JLabel label(String s, Font f, Color c) {
        JLabel l = new JLabel(s);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    private static JPanel spacer() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    private static String gearName(Artifact a) {
        return a != null ? a.toString() : "None";
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static JLabel text(String s, int width, Font f, Color c) {
        return label("<html><div style='width:" + width + "px'>" + esc(s).replace("\n", "<br>") + "</div></html>", f, c);
    }

    private static String pretty(String s) {
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

    private static Image icon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = aa(img.getGraphics());
        g.setColor(ACCENT);
        g.fill(new Ellipse2D.Double(2, 2, 28, 28));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(10, 10, 12, 12));
        g.dispose();
        return img;
    }

    private static int dialog(Window owner, String title, String heading, JComponent body, String[] labels,
                              int cancelIdx, BooleanSupplier accept) {
        return dialog(owner, title, heading, body, labels, cancelIdx, accept, null, null);
    }

    private static int dialog(Window owner, String title, String heading, JComponent body, String[] labels,
                              int cancelIdx, BooleanSupplier accept, Runnable onOpen) {
        return dialog(owner, title, heading, body, labels, cancelIdx, accept, onOpen, null);
    }

    private static int dialog(Window owner, String title, String heading, JComponent body, String[] labels,
                              int cancelIdx, BooleanSupplier accept, Runnable onOpen, Runnable onCtrlS) {
        JDialog d = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.setResizable(false);
        final int[] result = {-1};

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(SURFACE);
        root.setBorder(new EmptyBorder(22, 26, 18, 26));
        root.add(label(heading, TITLE, TEXT), BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 0, 0, 0));
        Btn first = null;
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            Btn b = new Btn(labels[i], i == 0);
            b.addActionListener(e -> {
                if (idx == 0 && accept != null && !accept.getAsBoolean()) return;
                result[0] = idx;
                d.dispose();
            });
            row.add(b);
            if (i == 0) first = b;
        }
        root.add(row, BorderLayout.SOUTH);
        d.setContentPane(root);
        d.getRootPane().setDefaultButton(first);

        d.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        d.getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result[0] = cancelIdx;
                d.dispose();
            }
        });

        if (onCtrlS != null) {
            KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
            KeyStroke cmdS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
            AbstractAction act = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    d.dispose();
                    onCtrlS.run();
                }
            };
            d.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlS, "dialogCtrlS");
            d.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(cmdS, "dialogCmdS");
            d.getRootPane().getActionMap().put("dialogCtrlS", act);
            d.getRootPane().getActionMap().put("dialogCmdS", act);
        }

        if (onOpen != null) {
            d.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    onOpen.run();
                }
            });
        }
        d.pack();
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
        return result[0];
    }

    private static final class Stack extends JPanel {
        private final GridBagConstraints c = new GridBagConstraints();
        private int row;

        Stack() {
            super(new GridBagLayout());
            setOpaque(false);
            c.gridx = 0;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTHWEST;
        }

        Stack row(Component comp, int gap) {
            c.gridy = row++;
            c.insets = new Insets(gap, 0, 0, 0);
            add(comp, c);
            return this;
        }
    }

    private static final class Stat extends JComponent {
        private final String label;
        private long value;

        Stat(String label) {
            this.label = label;
        }

        void set(long v) {
            if (v != value) {
                value = v;
                repaint();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(90, 52);
        }

        @Override
        protected void paintComponent(Graphics gr) {
            Graphics2D g = aa(gr);
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

    private static final class Bar extends JComponent {
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
            Graphics2D g = aa(gr);
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

    private static final class PadButton extends JButton {
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

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(64, 42);
        }

        @Override
        protected void paintComponent(Graphics gr) {
            Graphics2D g = aa(gr);
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

    private static final class Btn extends JButton {
        private final boolean primary;

        Btn(String text, boolean primary) {
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
            return new Dimension(Math.max(92, getFontMetrics(getFont()).stringWidth(getText()) + 36), 38);
        }

        @Override
        protected void paintComponent(Graphics gr) {
            Graphics2D g = aa(gr);
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

    private static final class OptionList extends JComponent {
        private static final int ROW = 54;
        private final List<HeroType> types;
        private int selected;

        OptionList(List<HeroType> types) {
            this.types = new ArrayList<>(types);
            setFocusable(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int i = e.getY() / (ROW + 6);
                    if (i >= 0 && i < OptionList.this.types.size()) {
                        selected = i;
                        requestFocusInWindow();
                        repaint();
                    }
                }
            });
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
            getActionMap().put("up", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    move(-1);
                }
            });
            getActionMap().put("down", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    move(1);
                }
            });
        }

        void move(int delta) {
            if (types.isEmpty()) return;
            selected = (selected + delta + types.size()) % types.size();
            repaint();
        }

        HeroType selected() {
            return types.isEmpty() ? null : types.get(selected);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(380, Math.max(1, types.size()) * (ROW + 6) - 6);
        }

        @Override
        protected void paintComponent(Graphics gr) {
            Graphics2D g = aa(gr);
            int w = getWidth();
            for (int i = 0; i < types.size(); i++) {
                HeroType t = types.get(i);
                int y = i * (ROW + 6);
                boolean sel = i == selected;
                RoundRectangle2D.Double r = new RoundRectangle2D.Double(0.5, y + 0.5, w - 1, ROW - 1, 12, 12);
                g.setColor(sel ? new Color(0xF1F6FA) : SURFACE);
                g.fill(r);
                g.setColor(sel ? ACCENT : LINE);
                g.setStroke(new BasicStroke(sel ? 1.6f : 1f));
                g.draw(r);
                // radio dot
                double cy = y + ROW / 2.0;
                g.setStroke(new BasicStroke(1.5f));
                g.setColor(sel ? ACCENT : MUTED);
                g.draw(new Ellipse2D.Double(16, cy - 7, 14, 14));
                if (sel) {
                    g.setColor(ACCENT);
                    g.fill(new Ellipse2D.Double(19.5, cy - 3.5, 7, 7));
                }
                g.setFont(BOLD);
                g.setColor(TEXT);
                g.drawString(t.getName(), 44, (float) (cy - 4));
                g.setFont(SMALL);
                g.setColor(MUTED);
                g.drawString("ATK " + t.getAttack() + "  ·  DEF " + t.getDefense() + "  ·  HP " + t.getHitPoints(),
                        44, (float) (cy + 13));
                String passive = pretty(t.getPassive().name());
                g.drawString(passive, w - 16 - g.getFontMetrics().stringWidth(passive), (float) (cy + 13));
            }
            g.dispose();
        }
    }

    private static final class HeroSelectPanel extends JComponent {
        private static final int ROW = 54;
        private final List<Hero> heroes;
        private int selected = 0;

        HeroSelectPanel(List<Hero> heroes) {
            this.heroes = new ArrayList<>(heroes);
            setFocusable(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int i = e.getY() / (ROW + 6);
                    if (i >= 0 && i < HeroSelectPanel.this.heroes.size()) {
                        selected = i;
                        requestFocusInWindow();
                        repaint();
                    }
                }
            });
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
            getActionMap().put("up", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    move(-1);
                }
            });
            getActionMap().put("down", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    move(1);
                }
            });
        }

        void move(int delta) {
            if (heroes.isEmpty()) return;
            selected = (selected + delta + heroes.size()) % heroes.size();
            repaint();
        }

        Hero selected() {
            return heroes.isEmpty() ? null : heroes.get(selected);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(400, Math.max(1, heroes.size()) * (ROW + 6) - 6);
        }

        @Override
        protected void paintComponent(Graphics gr) {
            Graphics2D g = aa(gr);
            int w = getWidth();
            for (int i = 0; i < heroes.size(); i++) {
                Hero h = heroes.get(i);
                int y = i * (ROW + 6);
                boolean sel = i == selected;
                RoundRectangle2D.Double r = new RoundRectangle2D.Double(0.5, y + 0.5, w - 1, ROW - 1, 12, 12);
                g.setColor(sel ? new Color(0xF1F6FA) : SURFACE);
                g.fill(r);
                g.setColor(sel ? ACCENT : LINE);
                g.setStroke(new BasicStroke(sel ? 1.6f : 1f));
                g.draw(r);

                double cy = y + ROW / 2.0;
                g.setStroke(new BasicStroke(1.5f));
                g.setColor(sel ? ACCENT : MUTED);
                g.draw(new Ellipse2D.Double(16, cy - 7, 14, 14));
                if (sel) {
                    g.setColor(ACCENT);
                    g.fill(new Ellipse2D.Double(19.5, cy - 3.5, 7, 7));
                }
                g.setFont(BOLD);
                g.setColor(TEXT);
                g.drawString(h.getName() + " (" + h.getHeroType().getName() + ") · Lv " + h.getLevel(),
                        44, (float) (cy - 4));
                g.setFont(SMALL);
                g.setColor(MUTED);
                String stats = "ATK " + h.getAttack() + " · DEF " + h.getDefense() + " · HP " + h.getHitPoints();
                g.drawString(stats, 44, (float) (cy + 13));
            }
            g.dispose();
        }
    }

    private static final class MapPanel extends JComponent {
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
}