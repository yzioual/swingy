package com.yzioual.view;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

public class Stack extends JPanel {
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

