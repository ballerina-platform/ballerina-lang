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

package org.ballerinalang.plugins.idea.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class BallerinaUtil {

    /**
     * Returns the first library root found in the project.
     *
     * @param project project to find the library root
     * @return first library root
     */
    public static String getLibraryRoot(Project project) {
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk != null) {
            VirtualFile[] roots = projectSdk.getSdkModificator().getRoots(OrderRootType.SOURCES);
            for (VirtualFile root : roots) {
                return root.getPath();
            }
        }
        return "";
    }

    /**
     * Returns whether the given file is a library file or not.
     *
     * @param project     project to get the library roots
     * @param virtualFile file to find in the library
     * @return {@code true} if the given file is in the libraries. {@code false} otherwise.
     */
    public static boolean isLibraryFile(Project project, VirtualFile virtualFile) {
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        String path = virtualFile.getPath();
        if (projectSdk != null) {
            VirtualFile[] roots = projectSdk.getSdkModificator().getRoots(OrderRootType.SOURCES);
            for (VirtualFile root : roots) {
                if (path.startsWith(root.getPath())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether the given file is a workspace file or not.
     *
     * @param project     project to find the file
     * @param virtualFile file to find in the project
     * @return {@code true} if the given file is in the project. {@code false} otherwise.
     */
    public static boolean isWorkspaceFile(Project project, VirtualFile virtualFile) {
        String filePath = virtualFile.getPath();
        if (filePath.startsWith(project.getBasePath())) {
            return true;
        }
        return false;
    }

    @NotNull
    public static String suggestPackageNameForDirectory(@Nullable PsiDirectory directory) {
        // If the directory is not null, get the package name
        if (directory != null) {
            VirtualFile virtualFile = directory.getVirtualFile();
            Project project = ProjectUtil.guessProjectForContentFile(virtualFile);
            if (project != null && project.getBasePath() != null) {
                // Get the relative path of the file in the project
                String trimmedPath = virtualFile.getPath().replace(project.getBasePath(), "");
                // Remove the separator at the beginning of the string
                trimmedPath = trimmedPath.replaceFirst(File.separator, "");
                // Replace all other separators with . to get the package path
                trimmedPath = trimmedPath.replaceAll(File.separator, ".");
                return trimmedPath;
            }
            // If the package name cannot be constructed, return empty string
            return "";
        }
        // If the directory is null, return empty string
        return "";
    }

    public static String suggestPackageNameForFile(Project project, VirtualFile virtualFile) {
        String basePath = project.getBasePath();
        if (isLibraryFile(project, virtualFile)) {
            basePath = getLibraryRoot(project);
        }
        String relativePath = FileUtil.getRelativePath(basePath, virtualFile.getPath(),
                File.separatorChar);
        int len = relativePath.length() - virtualFile.getName().length();

        if (len <= 0) {
            return "";
        }
        // Remove separator at the end if the file is not situated at the project root and get the path.
        return relativePath.substring(0, len - 1).replaceAll(File.separator, ".");
    }
}
