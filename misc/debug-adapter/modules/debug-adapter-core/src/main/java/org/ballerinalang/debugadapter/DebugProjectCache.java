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

import io.ballerina.projects.Project;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.ballerinalang.debugadapter.utils.PackageUtils.loadProject;

/**
 * A cache of Ballerina project instances, which are loaded when processing the user debug points.
 *
 * @since 2.0.0
 */
public class DebugProjectCache {

    private final Map<Path, Project> projects;

    public DebugProjectCache() {
        this.projects = new ConcurrentHashMap<>();
    }

    public Project getProjectWithPath(Path projectRoot) {
        if (!projects.containsKey(projectRoot)) {
            addProject(loadProject(projectRoot.toAbsolutePath().toString()));
        }
        return projects.get(projectRoot);
    }

    public void addProject(Project project) {
        Path projectSourceRoot = project.sourceRoot().toAbsolutePath();
        projects.put(projectSourceRoot, project);
    }
}
