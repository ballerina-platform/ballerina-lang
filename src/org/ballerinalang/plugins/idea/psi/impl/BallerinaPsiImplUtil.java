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

package org.ballerinalang.plugins.idea.psi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class BallerinaPsiImplUtil {

    public static String getLocalPackageName(@NotNull VirtualFile directory) {

        Project project = ProjectUtil.guessProjectForContentFile(directory);
        if (project != null && project.getBasePath() != null) {
            // Get the relative path of the file in the project
            String trimmedPath = directory.getPath().replace(project.getBasePath(), "");
            // Remove the separator at the beginning of the string
            trimmedPath = trimmedPath.replaceFirst(File.separator, "");
            // Replace all other separators with . to get the package path
            trimmedPath = trimmedPath.replaceAll(File.separator, ".");
            // If the path is not empty, return the path
            if (!trimmedPath.isEmpty()) {
                return trimmedPath;
            }
            // If the path is empty, return the project base directory name
            return project.getBaseDir().getName().replaceAll(" ", "_");
        }

        // If the package name cannot be constructed, return empty string
        return "";
    }
}
