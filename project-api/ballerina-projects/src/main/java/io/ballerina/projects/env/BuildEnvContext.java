/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects.env;

import io.ballerina.build.Project;
import io.ballerina.build.environment.EnvironmentContext;
import io.ballerina.build.environment.ProjectEnvironmentContext;

/**
 * Represents the {@code EnvironmentContext} of the build project.
 *
 * @since 2.0.0
 */
public class BuildEnvContext extends EnvironmentContext {
    private static final BuildEnvContext instance = new BuildEnvContext();

    public static BuildEnvContext getInstance() {
        return instance;
    }

    private BuildEnvContext() {
    }

    public ProjectEnvironmentContext projectEnvironmentContext(Project project) {
        return BuildProjectEnvContext.from(project);
    }
}
