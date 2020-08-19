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
package io.ballerina.projects.directory;

import io.ballerina.projects.Project;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;

/**
 * Contains a set of utility methods to create a project.
 *
 * @since 2.0.0
 */
public class ProjectLoader {

    public static Project loadProject(Path projectPath) throws Exception {
        if (RepoUtils.isBallerinaProject(projectPath)) {
            return BuildProject.loadProject(projectPath);
        } else if (isFileInDefaultModule(projectPath)) {
            return BuildProject.loadProject(projectPath.getParent());
        } else if (isFileInOtherModules(projectPath)) {
            return BuildProject.loadProject(projectPath.getParent().getParent().getParent());
        } else if (RepoUtils.isBallerinaStandaloneFile(projectPath)) {
            return SingleFileProject.loadProject(projectPath);
        } else {
            throw new Exception("invalid project path: " + projectPath);
        }
    }

    private static boolean isFileInDefaultModule(Path filePath) {
        return RepoUtils.isBallerinaProject(filePath.getParent());
    }
    private static boolean isFileInOtherModules(Path filePath) {
        if (filePath.getParent().getParent().getFileName() != null) {
            return ProjectDirConstants.MODULES_ROOT.equals(filePath.getParent().getParent().getFileName().toString());
        }
        return false;
    }
}
