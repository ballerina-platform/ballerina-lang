/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.cli.task;

import io.ballerina.projects.Project;
import io.ballerina.projects.directory.WorkspaceProject;

import java.io.PrintStream;

public class ResolveWorkspaceDependenciesTask implements Task {
    private final PrintStream outStream;

    public ResolveWorkspaceDependenciesTask(PrintStream outStream) {
        this.outStream = outStream;
    }

    @Override
    public void execute(Project project) {
        this.outStream.println("\nResolving workspace dependencies\n");
        WorkspaceProject workspaceProject = (WorkspaceProject) project;
        workspaceProject.getResolution();
    }
}
