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

package io.ballerina.plugins.idea.codeinsight.autodetect;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import javax.swing.*;

/**
 * Adds enabling/disabling Ballerina language server auto detection in settings.
 */
public class BallerinaAutoDetectionConfigurable implements SearchableConfigurable {

    private JCheckBox myCbUseAutoDetectedBallerinaHome;

    @NotNull
    private final BallerinaAutoDetectionSettings myLangServerAutoDetectionSettings;
    private final boolean myIsDialog;

    public BallerinaAutoDetectionConfigurable(@NotNull Project project, boolean dialogMode) {
        myLangServerAutoDetectionSettings = BallerinaAutoDetectionSettings.getInstance();
        myIsDialog = dialogMode;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        FormBuilder builder = FormBuilder.createFormBuilder();
        myCbUseAutoDetectedBallerinaHome = new JCheckBox("Enable language server auto detection");
        builder.addComponent(myCbUseAutoDetectedBallerinaHome);
        JPanel result = new JPanel(new BorderLayout());
        result.add(builder.getPanel(), BorderLayout.NORTH);
        if (myIsDialog) {
            result.setPreferredSize(new Dimension(300, -1));
        }
        return result;
    }

    @Override
    public boolean isModified() {
        return myLangServerAutoDetectionSettings.autoDetectBalHome() != myCbUseAutoDetectedBallerinaHome.isSelected();
    }

    @Override
    public void apply() {
        myLangServerAutoDetectionSettings.setAutoDetectBalHome(myCbUseAutoDetectedBallerinaHome.isSelected());
    }

    @Override
    public void reset() {
        myCbUseAutoDetectedBallerinaHome.setSelected(myLangServerAutoDetectionSettings.autoDetectBalHome());
    }

    @NotNull
    @Override
    public String getId() {
        return "ballerina.langserver.autodetect";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Language Server Auto Detection";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public void disposeUIResources() {
        UIUtil.dispose(myCbUseAutoDetectedBallerinaHome);
        myCbUseAutoDetectedBallerinaHome = null;
    }
}
