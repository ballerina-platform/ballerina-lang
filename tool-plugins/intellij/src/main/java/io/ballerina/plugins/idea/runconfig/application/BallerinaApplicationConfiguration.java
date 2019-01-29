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
 */

package io.ballerina.plugins.idea.runconfig.application;

import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import io.ballerina.plugins.idea.runconfig.BallerinaModuleBasedConfiguration;
import io.ballerina.plugins.idea.runconfig.BallerinaRunConfigurationWithMain;
import io.ballerina.plugins.idea.runconfig.RunConfigurationKind;
import io.ballerina.plugins.idea.runconfig.ui.BallerinaApplicationSettingsEditor;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * Represents Ballerina application configuration.
 */
public class BallerinaApplicationConfiguration
        extends BallerinaRunConfigurationWithMain<BallerinaApplicationRunningState> {

    private static final String PACKAGE_ATTRIBUTE_NAME = "package";
    private static final String KIND_ATTRIBUTE_NAME = "kind";

    @NotNull
    private String myPackage = "";

    public BallerinaApplicationConfiguration(Project project, String name,
                                             @NotNull ConfigurationType configurationType) {
        super(name, new BallerinaModuleBasedConfiguration(project), configurationType.getConfigurationFactories()[0]);
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        myPackage = StringUtil.notNullize(JDOMExternalizerUtil.getFirstChildValueAttribute(element,
                PACKAGE_ATTRIBUTE_NAME));
        try {
            String kindName = JDOMExternalizerUtil.getFirstChildValueAttribute(element, KIND_ATTRIBUTE_NAME);
            myRunKind = kindName != null ? RunConfigurationKind.valueOf(kindName) : RunConfigurationKind.MAIN;
        } catch (IllegalArgumentException e) {
            myRunKind = RunConfigurationKind.MAIN;
        }
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        JDOMExternalizerUtil.addElementWithValueAttribute(element, KIND_ATTRIBUTE_NAME, myRunKind.name());
        if (!myPackage.isEmpty()) {
            JDOMExternalizerUtil.addElementWithValueAttribute(element, PACKAGE_ATTRIBUTE_NAME, myPackage);
        }
    }

    @NotNull
    @Override
    protected ModuleBasedConfiguration createInstance() {
        return new BallerinaApplicationConfiguration(getProject(), getName(),
                BallerinaApplicationRunConfigurationType.getInstance());
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new BallerinaApplicationSettingsEditor(getProject());
    }

    @NotNull
    @Override
    protected BallerinaApplicationRunningState newRunningState(@NotNull ExecutionEnvironment env,
                                                               @NotNull Module module) {
        return new BallerinaApplicationRunningState(env, module, this);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        super.checkBaseConfiguration();
        super.checkFileConfiguration();
        Module module = getConfigurationModule().getModule();
        assert module != null;

        if (StringUtil.isEmptyOrSpaces(myPackage) && StringUtil.isEmptyOrSpaces(getFilePath())) {
            throw new RuntimeConfigurationError("Both file path and package are not specified. Need to specify at " +
                    "least one.");
        }
    }

    @NotNull
    public String getPackage() {
        return myPackage;
    }

    public void setPackage(@NotNull String aPackage) {
        myPackage = aPackage;
    }
}
