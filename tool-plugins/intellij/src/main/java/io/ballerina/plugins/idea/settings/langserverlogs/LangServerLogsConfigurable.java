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

package io.ballerina.plugins.idea.settings.langserverlogs;

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
 * Adds enabling/disabling language server logs in settings.
 */
public class LangServerLogsConfigurable implements SearchableConfigurable {

    private Project project;
    private JCheckBox myEnableDebugLogsCb;
    private JCheckBox myEnableTraceLogsCb;
    private final LangServerLogsSettings myLangServerLogsSettings;
    private final boolean myIsDialog;

    public LangServerLogsConfigurable(Project project, boolean dialogMode) {
        myLangServerLogsSettings = LangServerLogsSettings.getInstance(project);
        this.project = project;
        this.myIsDialog = dialogMode;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        FormBuilder builder = FormBuilder.createFormBuilder();
        myEnableDebugLogsCb = new JCheckBox("Enable debug logs");
        myEnableTraceLogsCb = new JCheckBox("Enable trace logs");
        builder.addComponent(myEnableDebugLogsCb);
        builder.addComponent(myEnableTraceLogsCb);
        JPanel result = new JPanel(new BorderLayout());
        result.add(builder.getPanel(), BorderLayout.NORTH);
        if (myIsDialog) {
            result.setPreferredSize(new Dimension(300, -1));
        }
        return result;
    }

    @Override
    public boolean isModified() {
        return myLangServerLogsSettings.isLangServerDebugLogsEnabled() != myEnableDebugLogsCb.isSelected()
                || myLangServerLogsSettings.isLangServerTraceLogsEnabled() != myEnableTraceLogsCb.isSelected();

    }

    @Override
    public void apply() {
        myLangServerLogsSettings.setIsLangServerDebugLogsEnabled(myEnableDebugLogsCb.isSelected());
        myLangServerLogsSettings.setIsLangServerTraceLogsEnabled(myEnableTraceLogsCb.isSelected());
        // Tries to notify the setting changes to the language server and if failed, requests to reload the project.
        boolean success = LSPUtils.notifyConfigChanges(project);
        if (!success) {
            BallerinaSdkUtils.showRestartDialog(project);
        }
    }

    @Override
    public void reset() {
        myEnableDebugLogsCb.setSelected(myLangServerLogsSettings.isLangServerDebugLogsEnabled());
        myEnableTraceLogsCb.setSelected(myLangServerLogsSettings.isLangServerTraceLogsEnabled());
    }

    @NotNull
    @Override
    public String getId() {
        return "ballerina.langserver.logs";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Language Server Logs";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public void disposeUIResources() {
        UIUtil.dispose(myEnableDebugLogsCb);
        UIUtil.dispose(myEnableTraceLogsCb);
        myEnableDebugLogsCb = null;
        myEnableTraceLogsCb = null;
    }
}
