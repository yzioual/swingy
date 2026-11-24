package com.yzioual.view;

import com.yzioual.model.artifact.Artifact;
import com.yzioual.model.hero.Hero;
import com.yzioual.model.map.Direction;
import com.yzioual.model.map.Map;
import com.yzioual.model.map.Position;
import com.yzioual.model.villain.Villain;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;
import java.util.List;

import static com.yzioual.view.UIConstants.*;
import static com.yzioual.model.map.Direction.*;

public class GUIView {
    private JFrame frame;
    private JLabel nameLabel;
    private JLabel classLabel;
    private JLabel dayLabel;
    private JLabel depthLabel;
    private StatPanel statPanel;
    private Bar XPBar;
    private JLabel XPLabel;
    private JLabel gearLabel;
    private JLabel statusLabel;
    private MapPanel mapPanel;
    private Consumer<Direction> moveListener;
    private Runnable switchListener;

    public static DialogHelper dialogHelper;

    public GUIView() {
        if (!GraphicsEnvironment.isHeadless()) { /* safe better than sorry */
            initUI();
        }
    }

    public Boolean askFight(Villain villain) {
        return DialogHelper.askFight(villain, this.frame);
    }

    public void showBattleResultJFrame(List<String> log,
            boolean won, boolean smash, int xp, int currentLevel, int levelsGained) {
        DialogHelper.showBattleResult(this.frame, log, won, smash, xp, currentLevel, levelsGained);
    }

    public Boolean askEquip(Artifact dropped, Artifact current) {
        return DialogHelper.askEquip(this.frame, dropped, current);
    }

    public Boolean showGameOver() {
        return DialogHelper.showGameOver(this.frame);
    }

    public Hero askCreateHero(Runnable onSwitchToConsole) {
        return DialogHelper.askCreateHero(onSwitchToConsole);
    }

    public Hero askSelectOrCreateHero( List<Hero> savedHeroes, Runnable onSwitchToConsole) {
        return DialogHelper.askSelectOrCreateHero(savedHeroes, onSwitchToConsole);
    }

    public boolean showShore(int day, Hero hero) {
        return DialogHelper.showShore(this.frame, day, hero);
    }

    public void showBattleResult(List<String> log,
            boolean won, boolean smash, int xp, int currentLevel, int levelsGained) {
        DialogHelper.showBattleResult(this.frame, log, won, smash, xp, currentLevel, levelsGained);
    }

    public void setSwitchListener(Runnable switchListener) {
        this.switchListener = switchListener;
    }

    private void initUI() {
        frame = new JFrame("Swingy - Ocean Depths");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(Utilities.icon());
        frame.setSize(620, 800);
        frame.setMinimumSize(new Dimension(520, 680));
        frame.setLocationRelativeTo(null);

        dialogHelper = new DialogHelper();

        statPanel = new StatPanel();
        statPanel.setOpaque(false);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(18, 22, 18, 22));
        frame.setContentPane(root);

        nameLabel = Utilities.label("Hero", TITLE, TEXT);
        classLabel = Utilities.label(" ", REG, MUTED);
        dayLabel = Utilities.label("Day 1", TITLE, TEXT);
        dayLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        depthLabel = Utilities.label("Ocean Depths", REG, MUTED);
        depthLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(new Stack().row(nameLabel, 0).row(classLabel, 2), BorderLayout.WEST);

        Button switchBtn = new Button("Console (Ctrl+S)", false);
        switchBtn.addActionListener(e -> fireSwitch());

        JPanel eastHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        eastHeader.setOpaque(false);
        eastHeader.add(new Stack().row(dayLabel, 0).row(depthLabel, 2));
        eastHeader.add(switchBtn);
        topRow.add(eastHeader, BorderLayout.EAST);

        XPBar = new Bar();
        XPLabel = Utilities.label(" ", SMALL, MUTED);
        XPLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gearLabel = Utilities.label(" ", SMALL, MUTED);

        root.add(new Stack()
                .row(topRow, 0)
                .row(statPanel, 16)
                .row(XPBar, 8)
                .row(XPLabel, 4)
                .row(gearLabel, 6), BorderLayout.NORTH);

        mapPanel = new MapPanel();
        mapPanel.setBorder(new EmptyBorder(6, 0, 6, 0));
        root.add(mapPanel, BorderLayout.CENTER);

        statusLabel = Utilities.label(" ", BOLD, TEXT);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        PadButton north = new PadButton(NORTH, "Move north (W / Up)");
        PadButton west = new PadButton(WEST, "Move west (A / Left)");
        PadButton south = new PadButton(SOUTH, "Move south (S / Down)");
        PadButton east = new PadButton(EAST, "Move east (D / Right)");

        for (PadButton b : new PadButton[]{north, west, south, east}) {
            b.addActionListener(e -> fireMove(b.getDir()));
        }

        JPanel pad = new JPanel(new GridLayout(2, 3, 6, 6));
        pad.setOpaque(false);
        pad.add(Utilities.spacer());
        pad.add(north);
        pad.add(Utilities.spacer());
        pad.add(west);
        pad.add(south);
        pad.add(east);
        JPanel padWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        padWrap.setOpaque(false);
        padWrap.add(pad);
        JLabel hint = Utilities.label("WASD / Arrows to move · Ctrl+S to switch to Console", SMALL, MUTED);
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
        String name = "move" + btn.getDir();
        for (int k : keys) {
            rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(k, 0), name);
        }
        rp.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireMove(btn.getDir());
            }
        });
    }

    private void fireMove(Direction direction) {
        if (moveListener != null) {
            moveListener.accept(direction);
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setMoveListener(Consumer<Direction> moveListener) {
        this.moveListener = moveListener;
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
        if (frame == null)
            return;

        nameLabel.setText(hero.getName());
        classLabel.setText(hero.getHeroType().getName() + "  ·  Level " + hero.getLevel());
        dayLabel.setText("Day " + hero.getLevel());
        depthLabel.setText(depth == null ? " " : depth);
        statPanel.setStats(hero.getAttack(), hero.getDefense(), hero.getHitPoints());
        XPBar.set(hero.getNextLevelXp() > 0 ? hero.getExperience() / (double) hero.getNextLevelXp() : 0);
        XPLabel.setText(hero.getExperience() + " / " + hero.getNextLevelXp() + " XP");
        String gear = "Weapon: " + Utilities.gearName(hero.getWeapon())
                + "   ·   Armor: " + Utilities.gearName(hero.getArmor())
                + "   ·   Helm: " + Utilities.gearName(hero.getHelm());
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
}
