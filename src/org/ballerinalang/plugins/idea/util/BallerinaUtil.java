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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.ballerinalang.plugins.idea.BallerinaFileType;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.BallerinaFunctionDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaPackageDeclaration;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BallerinaUtil {

    @NotNull
    public static String suggestPackageForDirectory(@Nullable PsiDirectory directory) {
        // If the directory is not null, get the package name
        if (directory != null) {
            return BallerinaPsiImplUtil.getLocalPackageName(directory.getVirtualFile());
        }
        // If the directory is null, return empty string
        return "";
    }

    public static List<String> findAllFullPackageDeclarations(Project project) {
        List<String> result = new ArrayList<String>();
        Collection<VirtualFile> virtualFiles =
                FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, BallerinaFileType.INSTANCE,
                        GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            BallerinaFile ballerinaFile = (BallerinaFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (ballerinaFile != null) {
                BallerinaPackageDeclaration[] properties = PsiTreeUtil.getChildrenOfType(ballerinaFile,
                        BallerinaPackageDeclaration.class);
                if (properties != null) {
                    for (BallerinaPackageDeclaration property : properties) {
                        result.add(property.getPackageName().getText());
                    }
                }
            }
        }
        return result;
    }


    public static List<String> findAllImportedFunctions(Project project, PsiFile psiFile) {
        List<String> result = new ArrayList<String>();
        Collection<VirtualFile> virtualFiles =
                FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, BallerinaFileType.INSTANCE,
                        GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            BallerinaFile ballerinaFile = (BallerinaFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (ballerinaFile != null) {
                BallerinaPackageDeclaration[] properties = PsiTreeUtil.getChildrenOfType(ballerinaFile,
                        BallerinaPackageDeclaration.class);
                if (properties != null) {
                    for (BallerinaPackageDeclaration packageDeclaration : properties) {
                        result.add(packageDeclaration.getRealPackageName());
                    }
                }
            }
        }
        return result;
    }

    public static List<String> findAllRealPackageDeclarations(Project project) {
        List<String> result = new ArrayList<String>();
        Collection<VirtualFile> virtualFiles =
                FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, BallerinaFileType.INSTANCE,
                        GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            BallerinaFile ballerinaFile = (BallerinaFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (ballerinaFile != null) {
                BallerinaPackageDeclaration[] properties = PsiTreeUtil.getChildrenOfType(ballerinaFile,
                        BallerinaPackageDeclaration.class);
                if (properties != null) {
                    for (BallerinaPackageDeclaration packageDeclaration : properties) {
                        result.add(packageDeclaration.getRealPackageName());
                    }
                }
            }
        }
        return result;
    }

    public static List<String> findAllFunctionDeclarations(Project project) {
        List<String> result = new ArrayList<String>();
        Collection<VirtualFile> virtualFiles =
                FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, BallerinaFileType.INSTANCE,
                        GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            BallerinaFile ballerinaFile = (BallerinaFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (ballerinaFile != null) {
                BallerinaFunctionDefinition[] functions = PsiTreeUtil.getChildrenOfType(ballerinaFile,
                        BallerinaFunctionDefinition.class);
                if (functions != null) {
                    for (BallerinaFunctionDefinition function : functions) {
                        result.add(function.getFunctionName());
                    }
                }
            }
        }
        return result;
    }
}
