/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.cli.utils;

import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Project;

/**
 * Utility functions related to project used by Commands.
 *
 * @since 2201.2.0
 */
public class ProjectUtils {

    private ProjectUtils() {
    }

    /**
     * Checks if a given project does not contain ballerina source files or test files.
     *
     * @param project project for checking for emptiness
     * @return true if the project is empty
     */
    public static boolean isProjectEmpty(Project project) {
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            if (!module.documentIds().isEmpty() || !module.testDocumentIds().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
