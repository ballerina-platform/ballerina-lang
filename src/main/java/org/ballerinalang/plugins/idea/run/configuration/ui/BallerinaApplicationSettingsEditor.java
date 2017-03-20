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
import org.ballerinalang.plugins.idea.run.configuration.BallerinaRunUtil;
import org.ballerinalang.plugins.idea.run.configuration.application.BallerinaApplicationConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import javax.swing.*;

public class BallerinaApplicationSettingsEditor extends SettingsEditor<BallerinaApplicationConfiguration> {

    private JPanel myPanel;
    private LabeledComponent<TextFieldWithBrowseButton> myFileField;
    private LabeledComponent<RawCommandLineEditor> myParamsField;
    private LabeledComponent<JComboBox<BallerinaApplicationConfiguration.Kind>> myRunKindComboBox;
    private LabeledComponent<EditorTextField> myPackageField;
    private LabeledComponent<TextFieldWithBrowseButton> myOutputFilePathField;
    private LabeledComponent<ModulesComboBox> myModulesComboBox;
    private LabeledComponent<TextFieldWithBrowseButton> myWorkingDirectoryField;
    private Project myProject;

    public BallerinaApplicationSettingsEditor(Project project) {
        myProject = project;
        installRunKindComboBox();

        BallerinaRunUtil.installBallerinaWithMainFileChooser(project, myFileField.getComponent());
        BallerinaRunUtil.installFileChooser(myProject, myOutputFilePathField.getComponent(), true, true);
    }

    @Override
    protected void resetEditorFrom(@NotNull BallerinaApplicationConfiguration configuration) {

        myFileField.getComponent().setText(configuration.getFilePath());
        myPackageField.getComponent().setText(configuration.getPackage());
        myRunKindComboBox.getComponent().setSelectedItem(configuration.getKind());
        myOutputFilePathField.getComponent().setText(StringUtil.notNullize(configuration.getOutputFilePath()));

        myModulesComboBox.getComponent().setModules(configuration.getValidModules());
        myModulesComboBox.getComponent().setSelectedModule(configuration.getConfigurationModule().getModule());

        myParamsField.getComponent().setText(configuration.getParams());
        myWorkingDirectoryField.getComponent().setText(configuration.getWorkingDirectory());
    }

    @Override
    protected void applyEditorTo(@NotNull BallerinaApplicationConfiguration configuration)
            throws ConfigurationException {
        configuration.setFilePath(myFileField.getComponent().getText());
        configuration.setPackage(myPackageField.getComponent().getText());
        configuration.setKind((BallerinaApplicationConfiguration.Kind) myRunKindComboBox.getComponent()
                .getSelectedItem());
        configuration.setFileOutputPath(StringUtil.nullize(myOutputFilePathField.getComponent().getText()));

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
        myRunKindComboBox = new LabeledComponent<>();
        myRunKindComboBox.setComponent(new JComboBox<>());

        myPackageField = new LabeledComponent<>();
        myPackageField.setComponent(new BallerinaPackageFieldCompletionProvider(
                () -> myModulesComboBox.getComponent().getSelectedModule()).createEditor(myProject));

        myFileField = new LabeledComponent<>();
        myFileField.setComponent(new TextFieldWithBrowseButton());

        myOutputFilePathField = new LabeledComponent<>();
        myOutputFilePathField.setComponent(new TextFieldWithBrowseButton());

        myWorkingDirectoryField = new LabeledComponent<>();
        myWorkingDirectoryField.setComponent(new TextFieldWithBrowseButton());

        myParamsField = new LabeledComponent<>();
        myParamsField.setComponent(new RawCommandLineEditor());

        myModulesComboBox = new LabeledComponent<>();
        myModulesComboBox.setComponent(new ModulesComboBox());
    }

    @Nullable
    private static ListCellRendererWrapper<BallerinaApplicationConfiguration.Kind> getRunKindListCellRendererWrapper() {
        return new ListCellRendererWrapper<BallerinaApplicationConfiguration.Kind>() {
            @Override
            public void customize(JList list, @Nullable BallerinaApplicationConfiguration.Kind kind, int index,
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
        for (BallerinaApplicationConfiguration.Kind kind : BallerinaApplicationConfiguration.Kind.values()) {
            myRunKindComboBox.getComponent().addItem(kind);
        }
        //        myRunKindComboBox.getComponent().addActionListener(e -> onRunKindChanged());
    }
}
