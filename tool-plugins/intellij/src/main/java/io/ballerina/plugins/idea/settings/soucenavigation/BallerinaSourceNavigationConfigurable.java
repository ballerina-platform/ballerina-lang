/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.settings.soucenavigation;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import io.ballerina.plugins.idea.preloading.LSPUtils;
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
 * Adds capability of enabling/disabling source navigation support.
 */
public class BallerinaSourceNavigationConfigurable implements SearchableConfigurable {

    private final BallerinaSourceNavigationSettings ballerinaSourceNavigationSettings;
    private Project project;
    private JCheckBox myCbEnableStdlibGotoDef;
    private final boolean myIsDialog;

    public BallerinaSourceNavigationConfigurable(Project project, boolean dialogMode) {
        ballerinaSourceNavigationSettings = BallerinaSourceNavigationSettings.getInstance(project);
        this.project = project;
        this.myIsDialog = dialogMode;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        FormBuilder builder = FormBuilder.createFormBuilder();
        myCbEnableStdlibGotoDef = new JCheckBox("Enable goto definition support for ballerina standard libraries");
        builder.addComponent(myCbEnableStdlibGotoDef);
        JPanel result = new JPanel(new BorderLayout());
        result.add(builder.getPanel(), BorderLayout.NORTH);
        if (myIsDialog) {
            result.setPreferredSize(new Dimension(300, -1));
        }
        return result;
    }

    @Override
    public boolean isModified() {
        return ballerinaSourceNavigationSettings.isEnableStdlibGotoDef() != myCbEnableStdlibGotoDef.isSelected();
    }

    @Override
    public void apply() {
        ballerinaSourceNavigationSettings.setEnableStdlibGotoDef(myCbEnableStdlibGotoDef.isSelected());
        // Tries to notify the setting changes to the language server and if failed, requests to reload the project.
        boolean success = LSPUtils.notifyConfigChanges(project);
        if (!success) {
            BallerinaSdkUtils.showRestartDialog(project);
        }
    }

    @Override
    public void reset() {
        myCbEnableStdlibGotoDef.setSelected(ballerinaSourceNavigationSettings.isEnableStdlibGotoDef());
    }

    @NotNull
    @Override
    public String getId() {
        return "ballerina.sourcenavigation";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Source Navigation";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public void disposeUIResources() {
        UIUtil.dispose(myCbEnableStdlibGotoDef);
        myCbEnableStdlibGotoDef = null;
    }
}
