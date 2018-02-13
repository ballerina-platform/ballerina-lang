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

package org.ballerinalang.plugins.idea.configuration;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.ui.UIUtil;
import org.ballerinalang.plugins.idea.project.BallerinaModuleSettings;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class BallerinaModuleSettingsUI implements ConfigurableUi<BallerinaModuleSettings>, Disposable {

    private JPanel myPanel;

    public BallerinaModuleSettingsUI(@NotNull Module module, boolean dialogMode) {
        myPanel.setPreferredSize(new Dimension(400, -1));
    }

    @Override
    public void reset(@NotNull BallerinaModuleSettings settings) {

    }

    @Override
    public boolean isModified(@NotNull BallerinaModuleSettings settings) {
        return false;
    }

    @Override
    public void apply(@NotNull BallerinaModuleSettings settings) throws ConfigurationException {

    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return myPanel;
    }

    private void createUIComponents() {

    }

    @Override
    public void dispose() {
        UIUtil.dispose(myPanel);
    }
}
