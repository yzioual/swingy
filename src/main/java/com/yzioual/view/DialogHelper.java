package com.yzioual.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import jakarta.validation.ConstraintViolationException;

import com.yzioual.model.artifact.Artifact;
import com.yzioual.model.hero.*;
import com.yzioual.model.villain.Villain;

import static com.yzioual.view.UIConstants.*;

public class DialogHelper {
    public DialogHelper() { }

    public static boolean askFight(Villain villain, JFrame frame) {
        if (frame == null)
            return true;

        Stack body = new Stack()
                .row(Utilities.text("“" + villain.catchphrase() + "”", 340, REG, TEXT), 0)
                .row(Utilities.text("Power " + villain.power() + "   ·   ATK " + villain.attack()
                        + "   ·   DEF " + villain.defense() + "   ·   HP " + villain.hitPoints(), 340, REG, MUTED), 10);

        return dialog(frame, "Encounter", "A " + villain.name() + " blocks your way!", body,
                new String[]{"Fight", "Run"}, 1, null) == 0;
    }

    public static void showBattleResult(JFrame frame, List<String> log,
            boolean won, boolean smash, int xp, int currentLevel, int levelsGained) {
        if (frame == null)
            return;

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
            body.row(Utilities.text("+" + xp + " XP" + (smash ? "  ·  flawless smash" : ""), 340, BOLD, ACCENT), gap);
            if (levelsGained > 0) {
                body.row(Utilities.text("Level up! You reached level " + currentLevel + "!", 340, BOLD, TEXT), 4);
            }
        } else {
            body.row(Utilities.text("You were defeated in battle...", 340, REG, MUTED), gap);
        }
        dialog(frame, won ? "Battle Victory" : "Battle Defeat", won ? "Victory" : "Defeat", body,
                new String[]{"Continue"}, 0, null);
    }

    public static boolean askEquip(JFrame frame, Artifact dropped, Artifact current) {
        if (frame == null) return false;
        Stack body = new Stack()
                .row(Utilities.text("FOUND", 340, SMALL, MUTED), 0)
                .row(Utilities.text(dropped.toString(), 340, BOLD, TEXT), 2)
                .row(Utilities.text("EQUIPPED", 340, SMALL, MUTED), 12)
                .row(Utilities.text(current != null ? current.toString() : "None", 340, REG, TEXT), 2)
                .row(Utilities.text("Do you want to keep and equip it?", 340, REG, MUTED), 14);
        return dialog(frame, "Loot Dropped", "You found something", body,
                new String[]{"Equip", "Skip"}, 1, null) == 0;
    }

    public static boolean showShore(JFrame frame, int day, Hero hero) {
        if (frame == null) return false;
        Stack body = new Stack()
                .row(Utilities.text("You reached the shore with your fish! Your family eats tonight.", 340, REG, TEXT), 0)
                .row(Utilities.text("ATK " + hero.getAttack() + "   ·   DEF " + hero.getDefense()
                        + "   ·   HP " + hero.getHitPoints(), 340, BOLD, TEXT), 12)
                .row(Utilities.text("Weapon: " +
                            Utilities.gearName(hero.getWeapon()) + "\nArmor: " + Utilities.gearName(hero.getArmor())
                        + "\nHelm: " + Utilities.gearName(hero.getHelm()), 340, REG, MUTED), 6)
                .row(Utilities.text("Ready to dive into Day " + (day + 1) + "?", 340, REG, TEXT), 14);
        return dialog(frame, "Shore Reached", "Day " + day + " complete", body,
                new String[]{"Dive in", "Not now"}, 1, null) == 0;
    }

    public static boolean showGameOver(JFrame frame) {
        if (frame == null) return false;
        Stack body = new Stack()
                .row(Utilities.text("Your family starves.", 300, REG, TEXT), 0)
                .row(Utilities.text("Would you like to try again?", 300, REG, MUTED), 6);
        return dialog(frame, "Game Over", "You drowned", body,
                new String[]{"Try again", "Quit"}, 1, null) == 0;
    }

    public static Hero askCreateHero(Runnable onSwitchToConsole) {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        List<HeroType> types = HeroType.getListOfHeroTypes();

        JTextField name = new JTextField("Waizi", 18);
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

        JLabel error = Utilities.label(" ", SMALL, ERROR);
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
                .row(Utilities.label("NAME", SMALL, MUTED), 0)
                .row(name, 4)
                .row(error, 2)
                .row(Utilities.label("CLASS", SMALL, MUTED), 8)
                .row(list, 4);

        final String[] chosen = new String[1];
        BooleanSupplier validate = () -> {
            String n = name.getText().trim();
            HeroType selectedType = list.selected();
            if (selectedType == null) {
                error.setText("Hero class is required.");
                return false;
            }
            try {
                new Hero.HeroBuilder()
                        .setName(n)
                        .setHeroType(selectedType)
                        .build();
                chosen[0] = n;
                return true;
            } catch (ConstraintViolationException e) {
                String firstError = e.getConstraintViolations().iterator().next().getMessage();
                error.setText(firstError);
                name.requestFocusInWindow();
                return false;
            }
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
            try {
                return new Hero.HeroBuilder()
                        .setName(chosen[0])
                        .setHeroType(list.selected())
                        .build();
            } catch (ConstraintViolationException e) {
                return null;
            }
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
                .row(Utilities.label("SAVED HEROES", SMALL, MUTED), 0)
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

    /********** Internal Methods **********/
    /**************************************/

    private static int dialog(Window owner, String title, String heading, JComponent body, String[] labels,
            int cancelIdx, BooleanSupplier accept) {
        return dialog(owner, title, heading, body, labels, cancelIdx, accept, null, null);
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
        root.add(Utilities.label(heading, TITLE, TEXT), BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 0, 0, 0));

        Button first = null;
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            Button b = new Button(labels[i], i == 0);
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

    private static final class OptionList extends JComponent {
        private static final int ROW = 54;
        private final List<HeroType> types;
        private int selected;

        OptionList(List<HeroType> types) {
            this.types = new ArrayList<>(types);
            setFocusable(true);
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
            Graphics2D g = Utilities.aa(gr);
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
                String passive = Utilities.pretty(t.getPassiveSpecialEffect().name());
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
            Graphics2D g = Utilities.aa(gr);
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
                String stats = "ATK " + h.getAttack() + " · DEF " + h.getDefense() + " · HP " + h.getHitPoints()
                    + " · XP " + h.getExperience() + "/" + h.getNextLevelXp();
                g.drawString(stats, 44, (float) (cy + 13));
            }
            g.dispose();
        }
    }
}
