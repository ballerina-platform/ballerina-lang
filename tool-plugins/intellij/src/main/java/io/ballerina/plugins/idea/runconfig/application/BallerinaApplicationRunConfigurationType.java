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

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import io.ballerina.plugins.idea.BallerinaConstants;
import icons.BallerinaIcons;
import io.ballerina.plugins.idea.runconfig.BallerinaConfigurationFactoryBase;
import org.jetbrains.annotations.NotNull;

/**
 * Represents Ballerina run configuration type. This is used to create Ballerina application configurations.
 */
public class BallerinaApplicationRunConfigurationType extends ConfigurationTypeBase {

    public BallerinaApplicationRunConfigurationType() {
        super("BallerinaApplicationRunConfiguration", "Ballerina",
                "Ballerina Application Run Configuration", BallerinaIcons.APPLICATION_RUN);

        addFactory(new BallerinaConfigurationFactoryBase(this) {

            @Override
            @NotNull
            public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
                return new BallerinaApplicationConfiguration(project, BallerinaConstants.BALLERINA, getInstance());
            }
        });
    }

    @NotNull
    public static BallerinaApplicationRunConfigurationType getInstance() {
        return Extensions.findExtension(CONFIGURATION_TYPE_EP, BallerinaApplicationRunConfigurationType.class);
    }
}
