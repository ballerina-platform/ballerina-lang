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

package org.ballerinalang.plugins.idea.runconfig.remote;

import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.ballerinalang.plugins.idea.runconfig.BallerinaModuleBasedConfiguration;
import org.ballerinalang.plugins.idea.runconfig.BallerinaRunConfigurationWithMain;
import org.ballerinalang.plugins.idea.runconfig.ui.BallerinaRemoteSettingsEditor;
import org.jetbrains.annotations.NotNull;

public class BallerinaRemoteConfiguration extends BallerinaRunConfigurationWithMain<BallerinaRemoteRunningState> {

    public BallerinaRemoteConfiguration(Project project, String name, @NotNull ConfigurationType configurationType) {
        super(name, new BallerinaModuleBasedConfiguration(project), configurationType.getConfigurationFactories()[0]);
    }

    @NotNull
    @Override
    protected ModuleBasedConfiguration createInstance() {
        return new BallerinaRemoteConfiguration(getProject(), getName(),
                BallerinaRemoteRunConfigurationType.getInstance());
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new BallerinaRemoteSettingsEditor(getProject());
    }

    @NotNull
    @Override
    protected BallerinaRemoteRunningState newRunningState(@NotNull ExecutionEnvironment env, @NotNull Module module) {
        return new BallerinaRemoteRunningState(env, module, this);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        super.checkBaseConfiguration();
        // Todo - validate remote address
    }
}
