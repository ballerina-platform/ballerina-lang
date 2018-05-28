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
 *
 */

package org.ballerinalang.plugins.idea.codeinsight.semanticanalyzer;

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

/**
 * Adds enabling/disabling Ballerina semantic analyzing in settings.
 */
public class BallerinaSemanticAnalyzerConfigurable implements SearchableConfigurable {

    private JCheckBox myCbUseSemanticAnalyzingSupport;

    @NotNull
    private final BallerinaSemanticAnalyzerSettings mySemanticAnalyzerSettings;
    private final boolean myIsDialog;

    public BallerinaSemanticAnalyzerConfigurable(@NotNull Project project, boolean dialogMode) {
        mySemanticAnalyzerSettings = BallerinaSemanticAnalyzerSettings.getInstance();
        myIsDialog = dialogMode;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        FormBuilder builder = FormBuilder.createFormBuilder();
        myCbUseSemanticAnalyzingSupport = new JCheckBox("Use semantic analyzing if available");
        builder.addComponent(myCbUseSemanticAnalyzingSupport);

        JPanel result = new JPanel(new BorderLayout());
        result.add(builder.getPanel(), BorderLayout.NORTH);
        if (myIsDialog) {
            result.setPreferredSize(new Dimension(300, -1));
        }
        return result;
    }

    @Override
    public boolean isModified() {
        return mySemanticAnalyzerSettings.useSemanticAnalyzer() != myCbUseSemanticAnalyzingSupport.isSelected();
    }

    @Override
    public void apply() throws ConfigurationException {
        mySemanticAnalyzerSettings.setUseSemanticAnalyzer(myCbUseSemanticAnalyzingSupport.isSelected());
    }

    @Override
    public void reset() {
        myCbUseSemanticAnalyzingSupport.setSelected(mySemanticAnalyzerSettings.useSemanticAnalyzer());
    }

    @NotNull
    @Override
    public String getId() {
        return "ballerina.semantic.analyzer";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Semantic Analyzer";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public void disposeUIResources() {
        UIUtil.dispose(myCbUseSemanticAnalyzingSupport);
        myCbUseSemanticAnalyzingSupport = null;
    }
}
