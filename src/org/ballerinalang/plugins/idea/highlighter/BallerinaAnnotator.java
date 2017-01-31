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

package org.ballerinalang.plugins.idea.highlighter;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.ballerinalang.plugins.idea.psi.BallerinaFunctionName;
import org.ballerinalang.plugins.idea.psi.BallerinaImportDeclaration;
import org.ballerinalang.plugins.idea.psi.BallerinaPackageDeclaration;
import org.ballerinalang.plugins.idea.psi.BallerinaPackageName;
import org.ballerinalang.plugins.idea.quickfix.RemovePackageQuickFix;
import org.ballerinalang.plugins.idea.util.BallerinaUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class BallerinaAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {

        Project project = element.getProject();
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();

        if (element instanceof BallerinaPackageDeclaration) {
            BallerinaPackageName packageName = ((BallerinaPackageDeclaration) element).getPackageName();

            if (packageName != null) {

                String relativePath = FileUtil.getRelativePath(project.getBasePath(), virtualFile.getPath(),
                        File.separatorChar);
                int len = relativePath.length() - virtualFile.getName().length();

                // Remove separator at the end if the file is not situated at the project root.
                if (len > 0) {
                    len--;
                }
                relativePath = relativePath.substring(0, len).replaceAll(File.separator, ".");

                if (relativePath.isEmpty()) {
                    holder.createErrorAnnotation(packageName, "Incorrect package").registerFix(
                            new RemovePackageQuickFix());
                    return;
                }
                if (!Objects.equals(relativePath, packageName.getText())) {
                    holder.createErrorAnnotation(packageName, "Incorrect package");
                    return;
                }
            }
        }

        if (element instanceof BallerinaImportDeclaration) {
            BallerinaPackageName packageName = ((BallerinaImportDeclaration) element).getPackageName();
            List<String> allPackageDeclarations =
                    BallerinaUtil.findAllFullPackageDeclarations(project);

            if (!allPackageDeclarations.contains(packageName.getText())) {
                holder.createErrorAnnotation(packageName, "Invalid package name");
                return;
            }
        }

        if (element instanceof BallerinaFunctionName) {
            BallerinaPackageName packageName = ((BallerinaFunctionName) element).getPackageName();
            List<String> allPackageDeclarations =
                    BallerinaUtil.findAllImportedFunctions(project, element.getContainingFile());

            if (!allPackageDeclarations.contains(packageName.getText())) {
                holder.createErrorAnnotation(packageName, "Invalid package name");
                return;
            }
        }

    }
}
