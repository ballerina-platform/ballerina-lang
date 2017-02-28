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

package org.ballerinalang.plugins.idea.run.configuration;

import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;

public abstract class BallerinaRunningState extends CommandLineState {

    private Project project;
    private String params;

    protected BallerinaRunningState(Project project, String params, ExecutionEnvironment environment) {
        super(environment);
        this.project = project;
        this.params = params;
        addConsoleFilters(new BallerinaConsoleFilter(project));
    }

    public abstract String getCommand();

    public Project getProject() {
        return project;
    }

    public String getParams() {
        return params;
    }
}
