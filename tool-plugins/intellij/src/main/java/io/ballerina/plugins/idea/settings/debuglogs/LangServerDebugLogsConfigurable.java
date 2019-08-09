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

package io.ballerina.plugins.idea.settings.debuglogs;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Adds enabling/disabling language server debug logs in settings.
 */
public class LangServerDebugLogsConfigurable implements SearchableConfigurable {

    private JCheckBox myEnableDebugLogsCb;
    private final LangServerDebugLogsSettings myLangServerDebugLogsSettings;
    private final boolean myIsDialog;

    public LangServerDebugLogsConfigurable(boolean dialogMode) {
        myLangServerDebugLogsSettings = LangServerDebugLogsSettings.getInstance();
        myIsDialog = dialogMode;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        FormBuilder builder = FormBuilder.createFormBuilder();
        myEnableDebugLogsCb = new JCheckBox("Enable language server debug logs");
        builder.addComponent(myEnableDebugLogsCb);
        JPanel result = new JPanel(new BorderLayout());
        result.add(builder.getPanel(), BorderLayout.NORTH);
        if (myIsDialog) {
            result.setPreferredSize(new Dimension(300, -1));
        }
        return result;
    }

    @Override
    public boolean isModified() {
        return myLangServerDebugLogsSettings.getIsLangServerDebugLogsEnabled() != myEnableDebugLogsCb.isSelected();
    }

    @Override
    public void apply() {
        myLangServerDebugLogsSettings.setIsLangServerDebugLogsEnabled(myEnableDebugLogsCb.isSelected());
    }

    @Override
    public void reset() {
        myEnableDebugLogsCb.setSelected(myLangServerDebugLogsSettings.getIsLangServerDebugLogsEnabled());
    }

    @NotNull
    @Override
    public String getId() {
        return "langserver.debuglogs";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Language server debug logs";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public void disposeUIResources() {
        UIUtil.dispose(myEnableDebugLogsCb);
        myEnableDebugLogsCb = null;
    }
}
