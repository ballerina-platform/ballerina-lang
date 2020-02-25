/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.plugins.idea.settings.autodetect;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Adds enabling/disabling Ballerina home auto detection in settings.
 */
public class BallerinaAutoDetectionConfigurable implements SearchableConfigurable {

    private JCheckBox myAutoDetectionCb;
    private final BallerinaAutoDetectionSettings myBalHomeAutoDetectionSettings;
    private final boolean myIsDialog;

    public BallerinaAutoDetectionConfigurable(@NotNull Project project, boolean dialogMode) {
        myBalHomeAutoDetectionSettings = BallerinaAutoDetectionSettings.getInstance(project);
        myIsDialog = dialogMode;
    }

    @Nullable
    @Override
    public JComponent createComponent() {

        FormBuilder builder = FormBuilder.createFormBuilder();
        myAutoDetectionCb = new JCheckBox("Auto-detect ballerina SDK location");
        builder.addComponent(myAutoDetectionCb);
        JPanel result = new JPanel(new BorderLayout());
        result.add(builder.getPanel(), BorderLayout.NORTH);
        if (myIsDialog) {
            result.setPreferredSize(new Dimension(300, -1));
        }
        return result;
    }

    @Override
    public boolean isModified() {
        return myBalHomeAutoDetectionSettings.getIsAutoDetectionEnabled() != myAutoDetectionCb.isSelected();
    }

    @Override
    public void apply() {
        myBalHomeAutoDetectionSettings.setIsAutoDetectionEnabled(myAutoDetectionCb.isSelected());
        BallerinaSdkUtils.showRestartDialog(null);
    }

    @Override
    public void reset() {
        myAutoDetectionCb.setSelected(myBalHomeAutoDetectionSettings.getIsAutoDetectionEnabled());
    }

    @NotNull
    @Override
    public String getId() {
        return "ballerina.autodetect";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Ballerina Home Auto Detection";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public void disposeUIResources() {
        UIUtil.dispose(myAutoDetectionCb);
        myAutoDetectionCb = null;
    }
}
