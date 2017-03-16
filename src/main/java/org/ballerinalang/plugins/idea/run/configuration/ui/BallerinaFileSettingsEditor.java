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
import org.ballerinalang.plugins.idea.run.configuration.GoRunUtil;
import org.ballerinalang.plugins.idea.run.configuration.file.GoRunFileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import javax.swing.*;

public class BallerinaFileSettingsEditor extends SettingsEditor<GoRunFileConfiguration> {

    private JPanel myPanel;
    private LabeledComponent<TextFieldWithBrowseButton> myFileField;
    private LabeledComponent<RawCommandLineEditor> params;
    private LabeledComponent<JComboBox<GoRunFileConfiguration.Kind>> myRunKindComboBox;
    private Project myProject;

    public BallerinaFileSettingsEditor(Project project) {
        myProject = project;
        installRunKindComboBox();
        GoRunUtil.installGoWithMainFileChooser(project, myFileField.getComponent());
    }

    @Override
    protected void resetEditorFrom(@NotNull GoRunFileConfiguration configuration) {
        params.getComponent().setText(configuration.getParams());
        myFileField.getComponent().setText(configuration.getFilePath());
        myRunKindComboBox.getComponent().setSelectedItem(configuration.getRunKind());
    }

    @Override
    protected void applyEditorTo(@NotNull GoRunFileConfiguration configuration) throws ConfigurationException {
        configuration.setParams(params.getComponent().getText());
        configuration.setFilePath(myFileField.getComponent().getText());
        configuration.setRunKind((GoRunFileConfiguration.Kind) myRunKindComboBox.getComponent().getSelectedItem());

    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }

    private void createUIComponents() {
        myRunKindComboBox = new LabeledComponent<JComboBox<GoRunFileConfiguration.Kind>>();
        myRunKindComboBox.setComponent(new JComboBox<GoRunFileConfiguration.Kind>());

        myFileField = new LabeledComponent<TextFieldWithBrowseButton>();
        myFileField.setComponent(new TextFieldWithBrowseButton());

        params = new LabeledComponent<RawCommandLineEditor>();
        params.setComponent(new RawCommandLineEditor());
    }

    private void installRunKindComboBox() {
        myRunKindComboBox.getComponent().removeAllItems();
        myRunKindComboBox.getComponent().setRenderer(getRunKindListCellRendererWrapper());
        for (GoRunFileConfiguration.Kind kind : GoRunFileConfiguration.Kind.values()) {
            myRunKindComboBox.getComponent().addItem(kind);
        }
        //        myRunKindComboBox.getComponent().addActionListener(e -> onRunKindChanged());
    }

    @Nullable
    private static ListCellRendererWrapper<GoRunFileConfiguration.Kind> getRunKindListCellRendererWrapper() {
        return new ListCellRendererWrapper<GoRunFileConfiguration.Kind>() {
            @Override
            public void customize(JList list, @Nullable GoRunFileConfiguration.Kind kind, int index, boolean selected,
                                  boolean hasFocus) {
                if (kind != null) {
                    String kindName = StringUtil.capitalize(kind.toString().toLowerCase(Locale.US));
                    setText(kindName);
                }
            }
        };
    }
}

