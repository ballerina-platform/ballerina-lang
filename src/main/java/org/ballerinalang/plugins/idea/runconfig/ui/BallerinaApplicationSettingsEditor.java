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

package org.ballerinalang.plugins.idea.runconfig.ui;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.RawCommandLineEditor;
import org.ballerinalang.plugins.idea.runconfig.BallerinaRunUtil;
import org.ballerinalang.plugins.idea.runconfig.RunConfigurationKind;
import org.ballerinalang.plugins.idea.runconfig.application.BallerinaApplicationConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;

public class BallerinaApplicationSettingsEditor extends SettingsEditor<BallerinaApplicationConfiguration> {

    private JPanel myPanel;
    private LabeledComponent<JComboBox<RunConfigurationKind>> myRunKindComboBox;
    private LabeledComponent<TextFieldWithBrowseButton> myFileField;
    private LabeledComponent<EditorTextField> myPackageField;
    private LabeledComponent<RawCommandLineEditor> myParamsField;
    private LabeledComponent<RawCommandLineEditor> myBallerinaParamsField;
    private LabeledComponent<TextFieldWithBrowseButton> myWorkingDirectoryField;
    private LabeledComponent<ModulesComboBox> myModulesComboBox;
    private Project myProject;

    public BallerinaApplicationSettingsEditor(Project project) {
        myProject = project;
        installRunKindComboBox();
        BallerinaRunUtil.installBallerinaWithMainFileChooser(project, myFileField.getComponent());
        BallerinaRunUtil.installBallerinaWithWorkingDirectoryChooser(project, myWorkingDirectoryField.getComponent());
    }

    @Override
    protected void resetEditorFrom(@NotNull BallerinaApplicationConfiguration configuration) {
        myFileField.getComponent().setText(configuration.getFilePath());

        myPackageField.getComponent().setText(configuration.getPackage());

        myRunKindComboBox.getComponent().setSelectedItem(configuration.getRunKind());

        myModulesComboBox.getComponent().setModules(configuration.getValidModules());
        myModulesComboBox.getComponent().setSelectedModule(configuration.getConfigurationModule().getModule());

        myParamsField.getComponent().setText(configuration.getParams());
        myBallerinaParamsField.getComponent().setText(configuration.getBallerinaToolParams());

        myWorkingDirectoryField.getComponent().setText(configuration.getWorkingDirectory());
    }

    @Override
    protected void applyEditorTo(@NotNull BallerinaApplicationConfiguration configuration)
            throws ConfigurationException {
        configuration.setPackage(myPackageField.getComponent().getText());
        RunConfigurationKind runKind = (RunConfigurationKind) myRunKindComboBox.getComponent().getSelectedItem();
        configuration.setRunKind(runKind);
        configuration.setFilePath(myFileField.getComponent().getText());
        configuration.setModule(myModulesComboBox.getComponent().getSelectedModule());
        configuration.setParams(myParamsField.getComponent().getText());
        configuration.setBallerinaParams(myBallerinaParamsField.getComponent().getText());
        configuration.setWorkingDirectory(myWorkingDirectoryField.getComponent().getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }

    private void createUIComponents() {
        myRunKindComboBox = new LabeledComponent<>();
        myRunKindComboBox.setComponent(new JComboBox<>());

        myFileField = new LabeledComponent<>();
        myFileField.setComponent(new TextFieldWithBrowseButton());

        myPackageField = new LabeledComponent<>();
        myPackageField.setComponent(new BallerinaPackageFieldCompletionProvider(
                () -> myModulesComboBox.getComponent().getSelectedModule()).createEditor(myProject));

        myWorkingDirectoryField = new LabeledComponent<>();
        myWorkingDirectoryField.setComponent(new TextFieldWithBrowseButton());

        myParamsField = new LabeledComponent<>();
        myParamsField.setComponent(new RawCommandLineEditor());

        myBallerinaParamsField = new LabeledComponent<>();
        myBallerinaParamsField.setComponent(new RawCommandLineEditor());

        myModulesComboBox = new LabeledComponent<>();
        myModulesComboBox.setComponent(new ModulesComboBox());
    }

    private static ListCellRendererWrapper<RunConfigurationKind> getRunKindListCellRendererWrapper() {
        return new ListCellRendererWrapper<RunConfigurationKind>() {
            @Override
            public void customize(JList list, @Nullable RunConfigurationKind kind, int index,
                                  boolean selected, boolean hasFocus) {
                if (kind != null) {
                    String kindName = StringUtil.capitalize(kind.toString().toLowerCase(Locale.US));
                    setText(kindName);
                }
            }
        };
    }

    private void installRunKindComboBox() {
        myRunKindComboBox.getComponent().removeAllItems();
        myRunKindComboBox.getComponent().setRenderer(getRunKindListCellRendererWrapper());
        for (RunConfigurationKind kind : RunConfigurationKind.values()) {
            myRunKindComboBox.getComponent().addItem(kind);
        }
    }
}
