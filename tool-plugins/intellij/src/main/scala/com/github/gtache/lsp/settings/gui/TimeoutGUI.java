package com.github.gtache.lsp.settings.gui;

import com.github.gtache.lsp.requests.Timeout;
import com.github.gtache.lsp.requests.Timeouts;
import com.github.gtache.lsp.settings.LSPState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GUI for the Timeouts settings
 */
public final class TimeoutGUI implements LSPGUI {
    private static final String FIELD_TOOLTIP = "Time in milliseconds";
    private static final Logger LOG = Logger.getInstance(TimeoutGUI.class);
    private final Map<Timeouts, JTextField> rows;
    private final LSPState state = state();
    private final JPanel rootPanel;

    public TimeoutGUI() {
        rows = Timeout.getTimeoutsJava().entrySet().stream().map(e -> {
            NumberFormat format = NumberFormat.getInstance();
            NumberFormatter formatter = new NumberFormatter(format);
            format.setGroupingUsed(false);
            formatter.setValueClass(Integer.class);
            formatter.setMinimum(0);
            formatter.setAllowsInvalid(true);
            formatter.setMaximum(Integer.MAX_VALUE);
            final JFormattedTextField field = new JFormattedTextField(formatter);
            field.setToolTipText(FIELD_TOOLTIP);
            field.setText(e.getValue().toString());
            return new AbstractMap.SimpleEntry<>(e.getKey(), field);
        }).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        rootPanel = createRootPanel();
    }

    private static GridConstraints createGridConstraints(final int rowIdx, final int colIdx) {
        return createGridConstraints(rowIdx, colIdx, null);
    }

    private static GridConstraints createGridConstraints(final int rowIdx, final int colIdx, final Dimension preferredSize) {
        return new GridConstraints(rowIdx, colIdx, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, preferredSize, null, 0, false);
    }

    private static GridConstraints createSpacerGridConstraints(final int rowIdx, final int colIdx) {
        return new GridConstraints(rowIdx, colIdx, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false);
    }

    @Override
    public JPanel getRootPanel() {
        return rootPanel;
    }

    private JPanel createRootPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayoutManager(rows.size(), 6, JBUI.emptyInsets(), -1, -1));
        int idx = 0;
        final Iterator<Map.Entry<Timeouts, JTextField>> iterator = rows.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Timeouts, JTextField> entry = iterator.next();
            Timeouts timeout = entry.getKey();
            JTextField textField = entry.getValue();
            String name = timeout.name();
            panel.add(new JLabel(name.substring(0, 1) + name.substring(1).toLowerCase()), createGridConstraints(idx, 0));
            panel.add(textField, createGridConstraints(idx, 1, new Dimension(100, 10)));
            panel.add(new Spacer(), createSpacerGridConstraints(idx, 2));
            if (iterator.hasNext()) {
                entry = iterator.next();
                timeout = entry.getKey();
                textField = entry.getValue();
                name = timeout.name();
                panel.add(new JLabel(name.substring(0, 1) + name.substring(1).toLowerCase()), createGridConstraints(idx, 3));
                panel.add(textField, createGridConstraints(idx, 4, new Dimension(100, 10)));
                panel.add(new Spacer(), createSpacerGridConstraints(idx++, 5));
            }
        }
        return panel;
    }

    @Override
    public void apply() {
        final Map<Timeouts, Integer> newTimeouts = rows.entrySet().stream().map(e ->
                new AbstractMap.SimpleEntry<>(e.getKey(), Integer.parseInt(e.getValue().getText()))).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        state.setTimeouts(newTimeouts);
        Timeout.setTimeouts(newTimeouts);
    }

    @Override
    public void reset() {
        final Map<Timeouts, Integer> currentTimeouts = Timeout.getTimeoutsJava();
        rows.forEach((timeout, textField) -> textField.setText(currentTimeouts.get(timeout).toString()));
    }

    @Override
    public boolean isModified() {
        final Map<Timeouts, Integer> currentTimeouts = Timeout.getTimeoutsJava();
        try { //Don't allow apply if the value is not valid
            return Arrays.stream(Timeouts.values()).anyMatch(t -> {
                if (rows.containsKey(t)) {
                    final int newValue = Integer.parseInt(rows.get(t).getText());
                    return currentTimeouts.get(t) != newValue && newValue >= 0;
                } else {
                    return false;
                }
            });
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

}
