/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.SingleFileProject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.ballerinalang.debugadapter.utils.PackageUtils.computeProjectKindAndRoot;

/**
 * A cache of Ballerina project instances (against their source roots), which are loaded during the user
 * breakpoints resolving.
 *
 * @since 2.0.0
 */
public class DebugProjectCache {

    private final Map<Path, Project> loadedProjects;

    public DebugProjectCache() {
        this.loadedProjects = new ConcurrentHashMap<>();
    }

    /**
     * Returns the project instance which contains the given file path, from the project cache.
     *
     * @param filePath source root of the Ballerina project that need to be retrieved.
     * @return project instance.
     */
    public Project getProject(Path filePath) {
        Map.Entry<ProjectKind, Path> projectKindAndRoot = computeProjectKindAndRoot(filePath);
        Path projectRoot = projectKindAndRoot.getValue();
        if (!loadedProjects.containsKey(projectRoot)) {
            addProject(loadProject(filePath.toAbsolutePath().toString()));
        }
        return loadedProjects.get(projectRoot);
    }

    /**
     * Adds the given project instance into the cache.
     *
     * @param project project instance.
     */
    public void addProject(Project project) {
        Path projectSourceRoot = project.sourceRoot().toAbsolutePath();
        loadedProjects.put(projectSourceRoot, project);
    }

    /**
     * Loads the target ballerina source project instance using the Project API, from the file path of the open/active
     * editor instance in the client(plugin) side.
     *
     * @param filePath file path of the open/active editor instance in the plugin side.
     */
    private static Project loadProject(String filePath) {
        Map.Entry<ProjectKind, Path> projectKindAndProjectRootPair = computeProjectKindAndRoot(Paths.get(filePath));
        ProjectKind projectKind = projectKindAndProjectRootPair.getKey();
        Path projectRoot = projectKindAndProjectRootPair.getValue();
        BuildOptions options = BuildOptions.builder().setOffline(true).build();
        if (projectKind == ProjectKind.BUILD_PROJECT) {
            return BuildProject.load(projectRoot, options);
        } else if (projectKind == ProjectKind.SINGLE_FILE_PROJECT) {
            return SingleFileProject.load(projectRoot, options);
        } else {
            return ProjectLoader.loadProject(projectRoot, options);
        }
    }
}
