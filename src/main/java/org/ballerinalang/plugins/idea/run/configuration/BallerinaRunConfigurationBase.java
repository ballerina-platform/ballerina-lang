///*
// *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *  http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//
//package org.ballerinalang.plugins.idea.run.configuration;
//
//import com.intellij.execution.configurations.ConfigurationFactory;
//import com.intellij.execution.configurations.LocatableConfigurationBase;
//import com.intellij.execution.configurations.RuntimeConfigurationError;
//import com.intellij.execution.configurations.RuntimeConfigurationException;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.util.InvalidDataException;
//import com.intellij.openapi.util.JDOMExternalizerUtil;
//import com.intellij.openapi.util.WriteExternalException;
//import com.intellij.openapi.util.text.StringUtil;
//import com.intellij.util.containers.ContainerUtil;
//import org.ballerinalang.plugins.idea.sdk.BallerinaSdkUtil;
//import org.jdom.Element;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.Map;
//
//public abstract class BallerinaRunConfigurationBase extends LocatableConfigurationBase {
//
//    protected static final String PARAMETERS_NAME = "parameters";
//    @NotNull
//    protected String myParams = "";
//    @NotNull
//    protected final Map<String, String> myCustomEnvironment = ContainerUtil.newHashMap();
//
//    protected BallerinaRunConfigurationBase(@NotNull Project project, @NotNull ConfigurationFactory factory, String
//            name) {
//        super(project, factory, name);
//    }
//
//    /**
//     * This will validate the selected run configuration. This is common to all run run configuration types.
//     *
//     * @throws RuntimeConfigurationException if an incorrect configuration found
//     */
//    @Override
//    public void checkConfiguration() throws RuntimeConfigurationException {
//        String ballerinaExecutablePath = BallerinaSdkUtil.getBallerinaExecutablePath(getProject());
//        if (ballerinaExecutablePath.isEmpty()) {
//            throw new RuntimeConfigurationError("Cannot find Ballerina executable. Please check Ballerina SDK path.");
//        }
//        String openedFile = BallerinaRunUtil.getOpenFilePath(getProject());
//        if (openedFile.isEmpty()) {
//            throw new RuntimeConfigurationError("No Ballerina file is opened in the editor.");
//        }
//        if (!BallerinaRunUtil.isBallerinaFileOpen(getProject())) {
//            throw new RuntimeConfigurationError("Opened file in the editor is not a Ballerina file.");
//        }
//    }
//
//    @Override
//    public void writeExternal(Element element) throws WriteExternalException {
//        super.writeExternal(element);
//        addNonEmptyElement(element, PARAMETERS_NAME, myParams);
//    }
//
//    @Override
//    public void readExternal(Element element) throws InvalidDataException {
//        super.readExternal(element);
//        myParams = StringUtil.notNullize(JDOMExternalizerUtil.getFirstChildValueAttribute(element, PARAMETERS_NAME));
//    }
//
//    protected void addNonEmptyElement(@NotNull Element element, @NotNull String attributeName, @Nullable String value) {
//        if (StringUtil.isNotEmpty(value)) {
//            JDOMExternalizerUtil.addElementWithValueAttribute(element, attributeName, value);
//        }
//    }
//
//    public void setParams(String params) {
//        this.myParams = params;
//    }
//
//    @NotNull
//    public String getParams() {
//        return myParams;
//    }
//}
