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
import io.ballerina.projects.utils.ProjectConstants;
import io.ballerina.projects.utils.RepoUtils;
import org.ballerinalang.toml.exceptions.TomlException;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Contains a set of utility methods to create a project.
 *
 * @since 2.0.0
 */
public class ProjectLoader {

    public static Project loadProject(Path path) throws TomlException {

        Path absProjectPath = path.toAbsolutePath();
        if (!path.toFile().exists()) {
            throw new RuntimeException("project path does not exist:" + path);
        }

        if (absProjectPath.toFile().isDirectory()) {
            return BuildProject.loadProject(absProjectPath);
        } else if (isFileInDefaultModule(absProjectPath)) {
            return BuildProject.loadProject(Optional.of(absProjectPath.getParent()).get());
        } else if (isFileInOtherModules(absProjectPath)) {
            Path projectRoot = Optional.of(Optional.of(absProjectPath.getParent()).get().getParent()).get();
            return BuildProject.loadProject(Optional.of(projectRoot.getParent()).get());
        } else {
            return SingleFileProject.loadProject(absProjectPath);
        }
    }

    private static boolean isFileInDefaultModule(Path filePath) {
        return RepoUtils.isBallerinaProject(Optional.of(filePath.getParent()).get());
    }
    private static boolean isFileInOtherModules(Path filePath) {
        Path projectRoot = Optional.of(Optional.of(filePath.getParent()).get().getParent()).get();
        return ProjectConstants.MODULES_ROOT.equals(projectRoot.toFile().getName());
    }
}
