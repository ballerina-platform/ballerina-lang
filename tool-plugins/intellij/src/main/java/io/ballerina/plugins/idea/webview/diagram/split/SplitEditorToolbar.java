/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.ballerina.plugins.idea.webview.diagram.split;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Tool bar implementation of the ballerina diagram editor.
 */
public class SplitEditorToolbar extends JPanel implements Disposable {
    private static final String RIGHT_TOOLBAR_GROUP_ID = "Diagram.Toolbar.Right";

    private final MySpacingPanel mySpacingPanel;

    private final ActionToolbar myToolbar;

    private final List<EditorGutterComponentEx> myGutters = new ArrayList<>();

    private final ComponentAdapter myAdjustToGutterListener = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            adjustSpacing();
        }

        @Override
        public void componentShown(ComponentEvent e) {
            adjustSpacing();
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            adjustSpacing();
        }
    };

    SplitEditorToolbar(@NotNull final JComponent targetComponentForActions) {
        super(new GridBagLayout());

        myToolbar = createToolbarFromGroupId(RIGHT_TOOLBAR_GROUP_ID);
        myToolbar.setTargetComponent(targetComponentForActions);

        mySpacingPanel = new MySpacingPanel((int) myToolbar.getComponent().getPreferredSize().getHeight());
        final JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JLabel("View: ", SwingConstants.RIGHT), BorderLayout.EAST);
        add(mySpacingPanel);
        add(centerPanel,
                new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        JBUI.emptyInsets(), 0, 0));
        add(myToolbar.getComponent());

        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtil.CONTRAST_BORDER_COLOR));

        addComponentListener(myAdjustToGutterListener);
    }

    void addGutterToTrack(@NotNull EditorGutterComponentEx gutterComponentEx) {
        myGutters.add(gutterComponentEx);

        gutterComponentEx.addComponentListener(myAdjustToGutterListener);
    }

    public void refresh() {
        adjustSpacing();
        myToolbar.updateActionsImmediately();
    }

    private void adjustSpacing() {
        EditorGutterComponentEx leftMostGutter = null;
        for (EditorGutterComponentEx gutter : myGutters) {
            if (!gutter.isShowing()) {
                continue;
            }
            if (leftMostGutter == null || leftMostGutter.getX() > gutter.getX()) {
                leftMostGutter = gutter;
            }
        }

        final int spacing;
        if (leftMostGutter == null) {
            spacing = 0;
        } else {
            spacing = leftMostGutter.getWhitespaceSeparatorOffset();
        }
        mySpacingPanel.setSpacing(spacing);

        revalidate();
        repaint();
    }

    @Override
    public void dispose() {
        removeComponentListener(myAdjustToGutterListener);
        for (EditorGutterComponentEx gutter : myGutters) {
            gutter.removeComponentListener(myAdjustToGutterListener);
        }
    }

    @NotNull
    private static ActionToolbar createToolbarFromGroupId(@NotNull String groupId) {
        final ActionManager actionManager = ActionManager.getInstance();

        if (!actionManager.isGroup(groupId)) {
            throw new IllegalStateException(groupId + " should have been a group");
        }
        final ActionGroup group = ((ActionGroup) actionManager.getAction(groupId));
        final ActionToolbarImpl editorToolbar = ((ActionToolbarImpl) actionManager
                .createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, group, true));
        editorToolbar.setOpaque(false);
        editorToolbar.setBorder(new JBEmptyBorder(0, 2, 0, 2));

        return editorToolbar;
    }

    private static class MySpacingPanel extends JPanel {
        private final int myHeight;

        private int mySpacing;

        MySpacingPanel(int height) {
            myHeight = height;
            mySpacing = 0;
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(mySpacing, myHeight);
        }

        public void setSpacing(int spacing) {
            mySpacing = spacing;
        }
    }
}
