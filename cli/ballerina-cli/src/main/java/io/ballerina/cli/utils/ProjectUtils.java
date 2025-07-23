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
package io.ballerina.cli.utils;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectLoadResult;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.projects.util.ProjectPaths;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Optional;

public class ProjectUtils {

    private ProjectUtils() {
        // Prevent instantiation
    }

    public static ProjectLoadResult loadProject(Path projectPath, BuildOptions buildOptions, Path absProjectPath,
                                                PrintStream outStream) {
        ProjectLoadResult loadResult;
        if (ProjectPaths.isWorkspaceProjectRoot(projectPath)) {
            loadResult = WorkspaceProject.from(projectPath, buildOptions);
        } else {
            Optional<Path> workspaceRoot = ProjectPaths.workspaceRoot(absProjectPath);
            if (workspaceRoot.isPresent()) {
                loadResult = WorkspaceProject.from(workspaceRoot.get(), buildOptions);
            } else {
                loadResult = BuildProject.from(projectPath, buildOptions);
            }
        }
        loadResult.diagnostics().diagnostics().forEach(diagnostic -> {
            outStream.println(diagnostic.toString());
        });
        return loadResult;
    }
}
