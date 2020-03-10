/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.psi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import io.ballerina.plugins.idea.psi.BallerinaAlias;
import io.ballerina.plugins.idea.psi.BallerinaFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaFunctionNameReference;
import io.ballerina.plugins.idea.psi.BallerinaOrgName;
import io.ballerina.plugins.idea.psi.BallerinaPackageName;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_SRC_DIR_NAME;

/**
 * Util class which contains methods related to PSI manipulation.
 */
public class BallerinaPsiImplUtil {

    private static final List<String> BUILTIN_DIRECTORIES = new LinkedList<>();

    private static final List<String> BUILTIN_VARIABLE_TYPES = new LinkedList<>();

    static {
        BUILTIN_DIRECTORIES.add(File.separator + "builtin");

        BUILTIN_VARIABLE_TYPES.add("future"); //async
        BUILTIN_VARIABLE_TYPES.add("blob");
        BUILTIN_VARIABLE_TYPES.add("json");
        BUILTIN_VARIABLE_TYPES.add("map");
        BUILTIN_VARIABLE_TYPES.add("stream");
        BUILTIN_VARIABLE_TYPES.add("string");
        BUILTIN_VARIABLE_TYPES.add("table");
        BUILTIN_VARIABLE_TYPES.add("xml");
    }

    // Since instances of "com.intellij.openapi.project.Project" returns system-independant paths for project directory
    // File.seperator should not be used
    private static final String FILE_SEPARATOR = "/";

    @Nullable
    public static String getName(@NotNull BallerinaPackageName ballerinaPackageName) {
        PsiElement identifier = ballerinaPackageName.getIdentifier();
        return identifier.getText();
    }

    @Nullable
    public static String getName(@NotNull BallerinaOrgName ballerinaOrgName) {
        PsiElement identifier = ballerinaOrgName.getIdentifier();
        return identifier.getText();
    }

    @Nullable
    public static String getName(@NotNull BallerinaAlias ballerinaAlias) {
        PsiElement identifier = ballerinaAlias.getIdentifier();
        return identifier != null ? identifier.getText() : null;
    }

    @Nullable
    public static PsiElement getIdentifier(BallerinaFunctionDefinition ballerinaFunctionDefinition) {
        //todo: fix
//        BallerinaCallableUnitSignature callableUnitSignature = ballerinaFunctionDefinition.getCallableUnitSignature();
//        return callableUnitSignature != null ? callableUnitSignature.getAnyIdentifierName().getIdentifier() : null;
        return null;
    }

    @Nullable
    public static String getName(BallerinaFunctionDefinition ballerinaFunctionDefinition) {
//        BallerinaCallableUnitSignature callableUnitSignature = ballerinaFunctionDefinition.getCallableUnitSignature();
//        return callableUnitSignature != null && callableUnitSignature.getAnyIdentifierName().getIdentifier() != null ?
//                callableUnitSignature.getAnyIdentifierName().getIdentifier().getText() :
//                "";
        return null;
    }

    public static boolean isInLocalPackage(@NotNull BallerinaFunctionNameReference nameReference) {
        return nameReference.getPackageReference() == null;
    }

    /**
     * Finds a file in the project SDK.
     *
     * @param project current project
     * @param path    relative file path in the SDK
     * @return {@code null} if the file cannot be found, otherwise returns the corresponding {@link VirtualFile}.
     */
    @Nullable
    public static VirtualFile findFileInProjectSDK(@NotNull Project project, @NotNull String path) {
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk == null) {
            return null;
        }
        VirtualFile[] roots = projectSdk.getSdkModificator().getRoots(OrderRootType.SOURCES);
        VirtualFile file;
        for (VirtualFile root : roots) {
            file = VfsUtilCore.findRelativeFile(path, root);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    /**
     * Find the specified file in the project and returns the corresponding {@link PsiFile}.
     *
     * @param project a project
     * @param path    file path
     * @return corresponding psi file
     */
    @Nullable
    public static PsiFile findFileInProject(@NotNull Project project, @NotNull String path) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
        if (virtualFile == null) {
            return null;
        }
        return PsiManager.getInstance(project).findFile(virtualFile);
    }

    @NotNull
    public static String getPackage(@NotNull PsiFile file) {
        Project project = file.getProject();
        String balProjectRoot = BallerinaSdkUtils.searchForBallerinaProjectRoot(file.getVirtualFile().getPath(),
                project.getBasePath());
        if (balProjectRoot.isEmpty()) {
            return balProjectRoot;
        }

        String filePath = file.getVirtualFile().getPath();
        filePath = filePath.replace(String.format("%s%s%s%s", balProjectRoot, FILE_SEPARATOR, BALLERINA_SRC_DIR_NAME,
                FILE_SEPARATOR), "");
        if (!filePath.contains(FILE_SEPARATOR)) {
            return "";
        }
        int index = filePath.indexOf(FILE_SEPARATOR);
        return filePath.substring(0, index);
    }

    @NotNull
    public static String getPackage(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String modulePath = project.getBasePath() + FILE_SEPARATOR;
        String filePath = virtualFile.getPath();
        filePath = filePath.replace(modulePath, "");
        if (!filePath.contains(FILE_SEPARATOR)) {
            return "";
        }
        int index = filePath.indexOf(FILE_SEPARATOR);
        return filePath.substring(0, index);
    }

    @NotNull
    public static String getFilePathInPackage(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String projectPath = project.getBasePath() + FILE_SEPARATOR;
        String filePath = virtualFile.getPath();
        filePath = filePath.replace(projectPath, "");
        if (!filePath.contains(FILE_SEPARATOR)) {
            return "";
        }
        int index = filePath.indexOf(FILE_SEPARATOR);
        return filePath.substring(index + 1);
    }
}
