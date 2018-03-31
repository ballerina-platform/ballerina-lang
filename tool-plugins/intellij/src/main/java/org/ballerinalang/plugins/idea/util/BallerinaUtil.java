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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import org.ballerinalang.plugins.idea.sdk.BallerinaSdkService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Util class for imports.
 */
public class BallerinaUtil {

    private BallerinaUtil() {

    }

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

    /**
     * Suggest the package name for a directory.
     *
     * @param directory directory which needs to be processed
     * @return suggested package name or empty string if package name cannot be determined.
     */
    @NotNull
    public static String suggestPackageNameForDirectory(@Nullable PsiDirectory directory) {
        // If the directory is not null, get the package name
        if (directory != null) {
            VirtualFile currentDirectory = directory.getVirtualFile();
            Module module = ModuleUtilCore.findModuleForPsiElement(directory);
            // Check directories in module content roots.
            if (module != null) {
                VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
                for (VirtualFile contentRoot : contentRoots) {
                    if (!directory.getVirtualFile().getPath().startsWith(contentRoot.getPath())) {
                        continue;
                    }
                    return getImportPath(currentDirectory, contentRoot);
                }
            }

            Project project = directory.getProject();
            // Check directories in project content roots.
            VirtualFile[] contentRoots = ProjectRootManager.getInstance(project).getContentRoots();
            for (VirtualFile contentRoot : contentRoots) {
                if (!directory.getVirtualFile().getPath().startsWith(contentRoot.getPath())) {
                    continue;
                }
                return getImportPath(currentDirectory, contentRoot);
            }

            // Then we check the sources of module sdk.
            if (module != null) {
                Sdk moduleSdk = ModuleRootManager.getInstance(module).getSdk();
                String root = getImportPath(directory, currentDirectory, moduleSdk);
                if (root != null) {
                    return root;
                }

                // Suggest package name for packages in BALLERINA_REPOSITORY.
                // Todo - This can be used to get package names for packages in SDK, BALLERINA_REPO, etc.
                OrderEntry[] entries = ModuleRootManager.getInstance(module).getOrderEntries();
                for (OrderEntry entry : entries) {
                    for (VirtualFile file : entry.getFiles(OrderRootType.SOURCES)) {
                        if (currentDirectory.getPath().startsWith(file.getPath())) {
                            return getImportPath(currentDirectory, file);
                        }
                    }
                }
            }

            // Then we check the sources of project sdk.
            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
            String root = getImportPath(directory, currentDirectory, projectSdk);
            if (root != null) {
                return root;
            }

            String sdkHomePath = BallerinaSdkService.getInstance(project).getSdkHomePath(null);
            if (sdkHomePath == null) {
                return "";
            }
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(sdkHomePath + "/src");
            if (virtualFile == null) {
                return "";
            }

            return getImportPath(currentDirectory, virtualFile);
        }
        // If the directory is null, return empty string
        return "";
    }

    @Nullable
    private static String getImportPath(@NotNull PsiDirectory directory, VirtualFile virtualFile, Sdk sdk) {
        if (sdk != null) {
            VirtualFile[] roots = sdk.getSdkModificator().getRoots(OrderRootType.SOURCES);
            for (VirtualFile root : roots) {
                if (!directory.getVirtualFile().getPath().startsWith(root.getPath())) {
                    continue;
                }
                return getImportPath(virtualFile, root);
            }
        }
        return null;
    }

    /**
     * Returns the import path.
     *
     * @param virtualFile file which we are checking
     * @param root        root directory which contains the file
     * @return import path of the file
     */
    private static String getImportPath(VirtualFile virtualFile, VirtualFile root) {
        // Get the relative path of the file in the project
        String trimmedPath = virtualFile.getPath().replace(root.getPath(), "");
        // Node: In virtual file paths, separators will always be "/" regardless of the OS.
        // Remove the separator at the beginning of the string
        trimmedPath = trimmedPath.replaceFirst("/", "");
        // Replace all other separators with . to get the package path
        trimmedPath = trimmedPath.replaceAll("/", ".");
        return trimmedPath;
    }

    public static String suggestPackageNameForFile(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        VirtualFile parent = virtualFile.getParent();
        PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(parent);
        return suggestPackageNameForDirectory(psiDirectory);
    }
}
