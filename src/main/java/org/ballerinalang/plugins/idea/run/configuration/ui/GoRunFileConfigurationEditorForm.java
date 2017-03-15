/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.plugins.idea.run.configuration.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.ballerinalang.plugins.idea.run.configuration.GoRunUtil;
import org.ballerinalang.plugins.idea.run.configuration.file.GoRunFileConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GoRunFileConfigurationEditorForm extends SettingsEditor<GoRunFileConfiguration> {
    private JPanel myComponent;
    private TextFieldWithBrowseButton myFileField;
    private GoCommonSettingsPanel myCommonSettingsPanel;

    public GoRunFileConfigurationEditorForm(@NotNull Project project) {
        myCommonSettingsPanel.init(project);
        GoRunUtil.installGoWithMainFileChooser(project, myFileField);
    }

    @Override
    protected void resetEditorFrom(GoRunFileConfiguration configuration) {
        myFileField.setText(configuration.getFilePath());
        myCommonSettingsPanel.resetEditorFrom(configuration);
    }

    @Override
    protected void applyEditorTo(GoRunFileConfiguration configuration) throws ConfigurationException {
        configuration.setFilePath(myFileField.getText());
        myCommonSettingsPanel.applyEditorTo(configuration);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myComponent;
    }
}
