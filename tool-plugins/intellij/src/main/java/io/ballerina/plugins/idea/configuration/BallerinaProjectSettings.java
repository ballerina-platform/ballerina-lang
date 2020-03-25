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

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Ballerina project-level settings.
 */
public class BallerinaProjectSettings implements Serializable {

    private boolean allowExperimental = false;
    private boolean autodetect = true;
    private boolean lsDebugLogs = false;
    private boolean lsTraceLogs = false;
    private boolean stdlibGotoDef = true;

    public static BallerinaProjectSettings getDefaultSettings() {
        return new BallerinaProjectSettings();
    }

    public static BallerinaProjectSettings getStoredSettings(@NotNull Project project) {
        BallerinaProjectSettingsComponent component = project.getComponent(BallerinaProjectSettingsComponent.class);
        if (component == null) {
            //in the default settings there is no project available and thus no project components.
            return new BallerinaProjectSettings();
        }
        return component.getState();
    }

    public boolean isAllowExperimental() {
        return allowExperimental;
    }

    public void setAllowExperimental(boolean allowExperimental) {
        this.allowExperimental = allowExperimental;
    }

    public boolean isAutodetect() {
        return autodetect;
    }

    public void setAutodetect(boolean autodetect) {
        this.autodetect = autodetect;
    }

    public boolean isLsDebugLogs() {
        return lsDebugLogs;
    }

    public void setLsDebugLogs(boolean lsDebugLogs) {
        this.lsDebugLogs = lsDebugLogs;
    }

    public boolean isLsTraceLogs() {
        return lsTraceLogs;
    }

    public void setLsTraceLogs(boolean lsTraceLogs) {
        this.lsTraceLogs = lsTraceLogs;
    }

    public boolean isStdlibGotoDef() {
        return stdlibGotoDef;
    }

    public void setStdlibGotoDef(boolean stdlibGotoDef) {
        this.stdlibGotoDef = stdlibGotoDef;
    }
}

