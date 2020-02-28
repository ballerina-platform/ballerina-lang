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

package io.ballerina.plugins.idea.settings.experimental;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Adds capability of enabling/disabling ballerina experimental feature support.
 */
public class BallerinaExperimentalFeatureConfigurable implements SearchableConfigurable {

    private JCheckBox myCbAllowExperimental;

    @NotNull
    private final BallerinaExperimentalFeatureSettings ballerinaExperimentalFeatureSettings;
    private final boolean myIsDialog;

    public BallerinaExperimentalFeatureConfigurable(boolean dialogMode) {
        ballerinaExperimentalFeatureSettings = BallerinaExperimentalFeatureSettings.getInstance();
        myIsDialog = dialogMode;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        FormBuilder builder = FormBuilder.createFormBuilder();
        myCbAllowExperimental = new JCheckBox("Allow ballerina experimental features");
        builder.addComponent(myCbAllowExperimental);
        JPanel result = new JPanel(new BorderLayout());
        result.add(builder.getPanel(), BorderLayout.NORTH);
        if (myIsDialog) {
            result.setPreferredSize(new Dimension(300, -1));
        }
        return result;
    }

    @Override
    public boolean isModified() {
        return ballerinaExperimentalFeatureSettings.getAllowExperimental() != myCbAllowExperimental.isSelected();
    }

    @Override
    public void apply() {
        ballerinaExperimentalFeatureSettings.setAllowExperimental(myCbAllowExperimental.isSelected());
        // Need to prompt a restart action to clear and re re-spawn language server instance with the changed
        // configuration.
        // Todo - Figure out a way to apply changes without restarting IDE.
        BallerinaSdkUtils.showRestartDialog(null);
    }

    @Override
    public void reset() {
        myCbAllowExperimental.setSelected(ballerinaExperimentalFeatureSettings.getAllowExperimental());
    }

    @NotNull
    @Override
    public String getId() {
        return "ballerina.experimental";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Experimental Features";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public void disposeUIResources() {
        UIUtil.dispose(myCbAllowExperimental);
        myCbAllowExperimental = null;
    }
}
