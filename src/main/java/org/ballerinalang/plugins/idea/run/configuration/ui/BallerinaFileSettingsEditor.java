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
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.RawCommandLineEditor;
import org.ballerinalang.plugins.idea.run.configuration.BallerinaRunUtil;
import org.ballerinalang.plugins.idea.run.configuration.RunConfigurationKind;
import org.ballerinalang.plugins.idea.run.configuration.file.BallerinaRunFileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;

public class BallerinaFileSettingsEditor extends SettingsEditor<BallerinaRunFileConfiguration> {

    private JPanel myPanel;
    private LabeledComponent<TextFieldWithBrowseButton> myFileField;
    private LabeledComponent<RawCommandLineEditor> myParamsField;
    private LabeledComponent<JComboBox<RunConfigurationKind>> myRunKindComboBox;
    private Project myProject;

    public BallerinaFileSettingsEditor(Project project) {
        myProject = project;
        installRunKindComboBox();
        BallerinaRunUtil.installBallerinaWithMainFileChooser(project, myFileField.getComponent());
    }

    @Override
    protected void resetEditorFrom(@NotNull BallerinaRunFileConfiguration configuration) {
        myParamsField.getComponent().setText(configuration.getParams());
        myFileField.getComponent().setText(configuration.getFilePath());
        myRunKindComboBox.getComponent().setSelectedItem(configuration.getRunKind());
    }

    @Override
    protected void applyEditorTo(@NotNull BallerinaRunFileConfiguration configuration) throws ConfigurationException {
        configuration.setParams(myParamsField.getComponent().getText());
        configuration.setFilePath(myFileField.getComponent().getText());
        RunConfigurationKind runKind = (RunConfigurationKind) myRunKindComboBox.getComponent().getSelectedItem();
        configuration.setRunKind(runKind);

        if (runKind == RunConfigurationKind.SERVICE) {
            myParamsField.setVisible(false);
        } else {
            myParamsField.setVisible(true);
        }
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

        myParamsField = new LabeledComponent<>();
        myParamsField.setComponent(new RawCommandLineEditor());
    }

    @Nullable
    private static ListCellRendererWrapper<RunConfigurationKind> getRunKindListCellRendererWrapper() {
        return new ListCellRendererWrapper<RunConfigurationKind>() {
            @Override
            public void customize(JList list, @Nullable RunConfigurationKind kind, int index, boolean selected,
                                  boolean hasFocus) {
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
        myRunKindComboBox.getComponent().addActionListener(e -> onRunKindChanged());
    }

    private void onRunKindChanged() {
        RunConfigurationKind selectedKind = (RunConfigurationKind) myRunKindComboBox.getComponent().getSelectedItem();
        if (selectedKind == null) {
            selectedKind = RunConfigurationKind.MAIN;
        }
        boolean isMainSelected = selectedKind == RunConfigurationKind.MAIN;
        myParamsField.setVisible(isMainSelected);
    }
}
