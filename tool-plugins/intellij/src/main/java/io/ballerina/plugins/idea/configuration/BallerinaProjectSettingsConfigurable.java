/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.configuration;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

/**
 * Ballerina project-specific configurable.
 */
public class BallerinaProjectSettingsConfigurable extends WithProject implements Configurable.VariableProjectAppLevel,
        Configurable {

    private BallerinaProjectSettingsPanel settingsPanel;

    public BallerinaProjectSettingsConfigurable(Project project) {
        super(project);
        settingsPanel = new BallerinaProjectSettingsPanel(project);
    }

    @Override
    public JComponent createComponent() {
        if (settingsPanel == null) {
            settingsPanel = new BallerinaProjectSettingsPanel(project);
        }
        return settingsPanel.getPanel();
    }

    @Override
    public boolean isModified() {
        if (project == null) {
            return false;
        }
        return settingsPanel != null && settingsPanel.isModified(BallerinaProjectSettings.getStoredSettings(project));
    }

    @Override
    public void apply() {
        settingsPanel.storeSettings(BallerinaProjectSettings.getStoredSettings(project));
    }

    @Override
    public void reset() {
        settingsPanel.setData(BallerinaProjectSettings.getDefaultSettings());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Ballerina";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public void disposeUIResources() {
        if (settingsPanel == null) {
            return;
        }
        Disposer.dispose(settingsPanel);
        this.settingsPanel = null;
    }

    @Override
    public boolean isProjectLevel() {
        return true;
    }
}
