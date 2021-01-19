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
package org.ballerinalang.langserver.codeaction.toml;

import io.ballerina.projects.Project;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for maintaining service related details for each project.
 *
 * @since 2.0.0
 */
public class ProjectServiceInfoHolder {

    private static final Map<Path, ProjectServiceInfo> projectServiceInfoDir = new HashMap<>();

    private ProjectServiceInfoHolder() {
    }

    public static ProjectServiceInfo getInstance(Project project) {
        Path path = project.sourceRoot();
        ProjectServiceInfo projectServiceInfo = ProjectServiceInfoHolder.projectServiceInfoDir.get(path);
        if (projectServiceInfo == null) {
            projectServiceInfo = new ProjectServiceInfo(project);
            projectServiceInfoDir.put(path, projectServiceInfo);
        }
        return projectServiceInfo;
    }
}
