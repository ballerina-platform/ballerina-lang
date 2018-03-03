/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.plugins.idea.runconfig.ui;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.RawCommandLineEditor;
import org.ballerinalang.plugins.idea.runconfig.BallerinaRunUtil;
import org.ballerinalang.plugins.idea.runconfig.test.BallerinaTestConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
/**
 * Provides the UI for test settings editor.
 */
public class BallerinaTestSettingsEditor extends SettingsEditor<BallerinaTestConfiguration> {

    private JPanel myPanel;
    private LabeledComponent<TextFieldWithBrowseButton> myFileField;
    private LabeledComponent<EditorTextField> myPackageField;
    private LabeledComponent<RawCommandLineEditor> myParamsField;
    private LabeledComponent<TextFieldWithBrowseButton> myWorkingDirectoryField;
    private LabeledComponent<ModulesComboBox> myModulesComboBox;
    private Project myProject;

    public BallerinaTestSettingsEditor(Project project) {
        myProject = project;
        BallerinaRunUtil.installBallerinTestFileChooser(project, myFileField.getComponent());
        BallerinaRunUtil.installBallerinaWithWorkingDirectoryChooser(project, myWorkingDirectoryField.getComponent());
    }

    @Override
    protected void resetEditorFrom(@NotNull BallerinaTestConfiguration configuration) {
        myFileField.getComponent().setText(configuration.getFilePath());

        myPackageField.getComponent().setText(configuration.getPackage());

        myModulesComboBox.getComponent().setModules(configuration.getValidModules());
        myModulesComboBox.getComponent().setSelectedModule(configuration.getConfigurationModule().getModule());

        myParamsField.getComponent().setText(configuration.getParams());
        myWorkingDirectoryField.getComponent().setText(configuration.getWorkingDirectory());
    }

    @Override
    protected void applyEditorTo(@NotNull BallerinaTestConfiguration configuration) throws ConfigurationException {
        configuration.setPackage(myPackageField.getComponent().getText());
        configuration.setFilePath(myFileField.getComponent().getText());
        configuration.setModule(myModulesComboBox.getComponent().getSelectedModule());
        configuration.setParams(myParamsField.getComponent().getText());
        configuration.setWorkingDirectory(myWorkingDirectoryField.getComponent().getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }

    private void createUIComponents() {
        myFileField = new LabeledComponent<>();
        myFileField.setComponent(new TextFieldWithBrowseButton());

        myPackageField = new LabeledComponent<>();
        myPackageField.setComponent(new BallerinaPackageFieldCompletionProvider(
                () -> myModulesComboBox.getComponent().getSelectedModule()).createEditor(myProject));

        myWorkingDirectoryField = new LabeledComponent<>();
        myWorkingDirectoryField.setComponent(new TextFieldWithBrowseButton());

        myParamsField = new LabeledComponent<>();
        myParamsField.setComponent(new RawCommandLineEditor());

        myModulesComboBox = new LabeledComponent<>();
        myModulesComboBox.setComponent(new ModulesComboBox());
    }
}
