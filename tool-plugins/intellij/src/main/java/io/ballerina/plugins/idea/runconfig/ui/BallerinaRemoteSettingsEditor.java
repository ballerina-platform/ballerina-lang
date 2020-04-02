/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.runconfig.ui;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.RawCommandLineEditor;
import io.ballerina.plugins.idea.runconfig.remote.BallerinaRemoteConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Provides the UI for remote settings editor.
 */
public class BallerinaRemoteSettingsEditor extends SettingsEditor<BallerinaRemoteConfiguration> {

    private JPanel myPanel;
    private LabeledComponent<RawCommandLineEditor> myParamsField;
    private LabeledComponent<TextFieldWithBrowseButton> myWorkingDirectoryField;
    private LabeledComponent<ModulesComboBox> myModulesComboBox;
    private JPanel myRemoteDebuggingPanel;
    private LabeledComponent<EditorTextField> myHost;
    private LabeledComponent<EditorTextField> myPort;
    private Project myProject;
    private EditorTextField myHostField;
    private EditorTextField myPortField;

    public BallerinaRemoteSettingsEditor(Project project) {
        myProject = project;
    }

    @Override
    protected void resetEditorFrom(@NotNull BallerinaRemoteConfiguration configuration) {
        myModulesComboBox.getComponent().setModules(configuration.getValidModules());
        myModulesComboBox.getComponent().setSelectedModule(configuration.getConfigurationModule().getModule());

        myHost.getComponent().setText(configuration.getRemoteDebugHost());
        myPort.getComponent().setText(configuration.getRemoteDebugPort());

        myParamsField.getComponent().setText(configuration.getParams());
        myWorkingDirectoryField.getComponent().setText(configuration.getWorkingDirectory());
    }

    @Override
    protected void applyEditorTo(@NotNull BallerinaRemoteConfiguration configuration)
            throws ConfigurationException {
        configuration.setModule(myModulesComboBox.getComponent().getSelectedModule());
        configuration.setParams(myParamsField.getComponent().getText());
        configuration.setWorkingDirectory(myWorkingDirectoryField.getComponent().getText());

        configuration.setRemoteDebugHost(myHost.getComponent().getText().trim());
        configuration.setRemoteDebugPort(myPort.getComponent().getText().trim());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }

    private void createUIComponents() {
        myWorkingDirectoryField = new LabeledComponent<>();
        myWorkingDirectoryField.setComponent(new TextFieldWithBrowseButton());

        myParamsField = new LabeledComponent<>();
        myParamsField.setComponent(new RawCommandLineEditor());

        myModulesComboBox = new LabeledComponent<>();
        myModulesComboBox.setComponent(new ModulesComboBox());

        myHost = new LabeledComponent<>();
        myHostField = new EditorTextField();
        myHostField.setPreferredWidth(300);
        myHost.setComponent(myHostField);

        myPort = new LabeledComponent<>();
        myPortField = new EditorTextField();
        myPortField.setPreferredWidth(100);
        myPort.setComponent(myPortField);
    }

}
