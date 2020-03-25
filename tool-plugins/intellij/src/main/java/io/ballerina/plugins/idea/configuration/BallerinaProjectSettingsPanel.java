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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Plugin settings UI panel.
 */
public class BallerinaProjectSettingsPanel extends WithProject implements Disposable {
    private JPanel settingsPane;
    private JCheckBox allowExperimental;
    private JCheckBox autodetect;
    private JCheckBox lsDebugLogs;
    private JCheckBox lsTraceLogs;
    private JCheckBox stdlibGotoDef;

    public BallerinaProjectSettingsPanel(Project project) {
        super(project);
    }

    public void setData(BallerinaProjectSettings settings) {
        autodetect.setSelected(settings.isAutodetect());
        lsTraceLogs.setSelected(settings.isLsTraceLogs());
        lsDebugLogs.setSelected(settings.isLsDebugLogs());
        stdlibGotoDef.setSelected(settings.isStdlibGotoDef());
        allowExperimental.setSelected(settings.isAllowExperimental());
    }

    public void storeSettings(BallerinaProjectSettings settings) {
        settings.setAutodetect(autodetect.isSelected());
        settings.setLsTraceLogs(lsTraceLogs.isSelected());
        settings.setLsDebugLogs(lsDebugLogs.isSelected());
        settings.setStdlibGotoDef(stdlibGotoDef.isSelected());
        settings.setAllowExperimental(allowExperimental.isSelected());
    }

    public boolean isModified(BallerinaProjectSettings settings) {
        return autodetect.isSelected() != settings.isAutodetect()
                || lsDebugLogs.isSelected() != settings.isLsDebugLogs()
                || lsTraceLogs.isSelected() != settings.isLsTraceLogs()
                || stdlibGotoDef.isSelected() != settings.isStdlibGotoDef()
                || allowExperimental.isSelected() != settings.isAllowExperimental();
    }

    public JPanel getPanel() {
        return settingsPane;
    }

    public boolean isModified() {
        return false;
    }

    @SuppressWarnings("BoundFieldAssignment")
    public void dispose() {
        this.settingsPane = null;
        this.autodetect = null;
        this.lsDebugLogs = null;
        this.lsTraceLogs = null;
        this.stdlibGotoDef = null;
    }
}
