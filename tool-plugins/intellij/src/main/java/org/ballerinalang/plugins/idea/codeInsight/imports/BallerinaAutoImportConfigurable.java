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

package org.ballerinalang.plugins.idea.codeInsight.imports;

import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
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

public class BallerinaAutoImportConfigurable implements SearchableConfigurable {

    private JCheckBox myCbShowImportPopup;
    private JCheckBox myCbAddUnambiguousImports;

    @NotNull
    private final BallerinaCodeInsightSettings myCodeInsightSettings;
    private final boolean myIsDialog;

    public BallerinaAutoImportConfigurable(@NotNull Project project, boolean dialogMode) {
        myCodeInsightSettings = BallerinaCodeInsightSettings.getInstance();
        myIsDialog = dialogMode;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        FormBuilder builder = FormBuilder.createFormBuilder();
        myCbShowImportPopup = new JCheckBox(ApplicationBundle.message("checkbox.show.import.popup"));
        myCbAddUnambiguousImports =
                new JCheckBox(ApplicationBundle.message("checkbox.add.unambiguous.imports.on.the.fly"));
        builder.addComponent(myCbShowImportPopup);
        builder.addComponent(myCbAddUnambiguousImports);

        JPanel result = new JPanel(new BorderLayout());
        result.add(builder.getPanel(), BorderLayout.NORTH);
        if (myIsDialog) result.setPreferredSize(new Dimension(300, -1));
        return result;
    }

    @Override
    public boolean isModified() {
        return myCodeInsightSettings.isShowImportPopup() != myCbShowImportPopup.isSelected() ||
                myCodeInsightSettings.isAddUnambiguousImportsOnTheFly() != myCbAddUnambiguousImports.isSelected();
    }

    @Override
    public void apply() throws ConfigurationException {
        myCodeInsightSettings.setShowImportPopup(myCbShowImportPopup.isSelected());
        myCodeInsightSettings.setAddUnambiguousImportsOnTheFly(myCbAddUnambiguousImports.isSelected());
    }

    @Override
    public void reset() {
        myCbShowImportPopup.setSelected(myCodeInsightSettings.isShowImportPopup());
        myCbAddUnambiguousImports.setSelected(myCodeInsightSettings.isAddUnambiguousImportsOnTheFly());
    }

    @NotNull
    @Override
    public String getId() {
        return "ballerina.autoimport";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Auto Import";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public void disposeUIResources() {
        UIUtil.dispose(myCbShowImportPopup);
        UIUtil.dispose(myCbAddUnambiguousImports);
        myCbShowImportPopup = null;
        myCbAddUnambiguousImports = null;
    }
}
