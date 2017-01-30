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
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.RawCommandLineEditor;
import org.ballerinalang.plugins.idea.run.configuration.BallerinaRunConfigurationBase;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class BallerinaApplicationSettingsEditor extends SettingsEditor<BallerinaRunConfigurationBase> {

    private JPanel myPanel;
    private LabeledComponent<RawCommandLineEditor> params;

    @Override
    protected void resetEditorFrom(@NotNull BallerinaRunConfigurationBase ballerinaRunConfigurationBase) {
        params.getComponent().setText(ballerinaRunConfigurationBase.getParams());
    }

    @Override
    protected void applyEditorTo(@NotNull BallerinaRunConfigurationBase ballerinaRunConfigurationBase)
            throws ConfigurationException {
        ballerinaRunConfigurationBase.setParams(params.getComponent().getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }

    private void createUIComponents() {
        params = new LabeledComponent<RawCommandLineEditor>();
        params.setComponent(new RawCommandLineEditor());
    }
}
