package com.github.gtache.lsp.settings.gui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.util.List;

public class ComboCheckboxDialogContentPanel {
    private final ComboBoxModel<String> serverDefModel;
    private final ComboBoxModel<String> serverWrapModel;
    private JCheckBox showDetailsCheck;
    private JPanel rootPane;
    private JComboBox<String> serverBox;

    public ComboCheckboxDialogContentPanel(final List<String> serverDefinitions, final List<String> serverWrappers) {
        this.serverDefModel = new DefaultComboBoxModel<>(serverDefinitions.toArray(new String[serverDefinitions.size()]));
        this.serverWrapModel = new DefaultComboBoxModel<>(serverWrappers.toArray(new String[serverWrappers.size()]));
        setupUI();
    }


    public JPanel getRootPane() {
        return rootPane;
    }

    public int getComboBoxIndex() {
        /*if (showDetailsCheck.isSelected()) {
            final var serverDefSize = serverDefModel.getSize();
            return serverDefSize + serverBox.getSelectedIndex();
        } else */
        return serverBox.getSelectedIndex();
    }


    private void setupUI() {
        rootPane = new JPanel();
        rootPane.setLayout(new GridLayoutManager(4, 3, JBUI.emptyInsets(), -1, -1));
        /*showDetailsCheck = new JCheckBox();
        showDetailsCheck.setText("Show servers instances");
        rootPane.add(showDetailsCheck, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showDetailsCheck.addItemListener(e -> {
            final var checkBox = (JCheckBox) e.getItem();
            if (checkBox.isSelected()) {
                serverBox.setModel(serverWrapModel);
            } else {
                serverBox.setModel(serverDefModel);
            }
        });*/
        serverBox = new ComboBox<>(this.serverDefModel);

        rootPane.add(serverBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        rootPane.add(spacer1, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        rootPane.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        rootPane.add(spacer3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        rootPane.add(spacer4, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        rootPane.add(spacer5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

}
