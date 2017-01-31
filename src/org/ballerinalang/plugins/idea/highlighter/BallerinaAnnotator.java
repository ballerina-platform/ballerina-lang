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

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.ballerinalang.plugins.idea.psi.BallerinaFunctionName;
import org.ballerinalang.plugins.idea.psi.BallerinaImportDeclaration;
import org.ballerinalang.plugins.idea.psi.BallerinaPackageDeclaration;
import org.ballerinalang.plugins.idea.psi.BallerinaPackageName;
import org.ballerinalang.plugins.idea.psi.BallerinaTypes;
import org.ballerinalang.plugins.idea.quickfix.ChangePackageQuickFix;
import org.ballerinalang.plugins.idea.quickfix.InsertPackageQuickFix;
import org.ballerinalang.plugins.idea.quickfix.RemovePackageQuickFix;
import org.ballerinalang.plugins.idea.util.BallerinaUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class BallerinaAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {

        Project project = element.getProject();
        PsiFile psiFile = element.getContainingFile();
        VirtualFile virtualFile = psiFile.getVirtualFile();

        if (element == psiFile) {
            ASTNode packageNode = psiFile.getNode().findChildByType(BallerinaTypes.PACKAGE_DECLARATION);
            if (packageNode == null) {
                String packageName = BallerinaUtil.suggestPackageNameForFile(project, virtualFile);
                if (!packageName.isEmpty()) {
                    holder.createErrorAnnotation(psiFile.getNode().getFirstChildNode(), "Missing package declaration")
                            .registerFix(new InsertPackageQuickFix(packageName));
                }
            }
        }

        if (element instanceof BallerinaPackageDeclaration) {
            BallerinaPackageName packageName = ((BallerinaPackageDeclaration) element).getPackageName();
            if (packageName != null) {
                String suggestedPackageName = BallerinaUtil.suggestPackageNameForFile(project, virtualFile);
                if (suggestedPackageName.isEmpty()) {
                    holder.createErrorAnnotation(packageName, "Incorrect package").registerFix(
                            new RemovePackageQuickFix());
                } else if (!Objects.equals(suggestedPackageName, packageName.getText())) {
                    holder.createErrorAnnotation(packageName, "Incorrect package").registerFix(
                            new ChangePackageQuickFix(suggestedPackageName));
                }
            }
        }

        if (element instanceof BallerinaImportDeclaration) {
            BallerinaPackageName packageName = ((BallerinaImportDeclaration) element).getPackageName();
            List<String> allPackageDeclarations =
                    BallerinaUtil.findAllFullPackageDeclarations(project);

            if (packageName != null && !allPackageDeclarations.contains(packageName.getText())) {
                holder.createErrorAnnotation(packageName, "Invalid package name");

            }
        }

        if (element instanceof BallerinaFunctionName) {
            BallerinaPackageName packageName = ((BallerinaFunctionName) element).getPackageName();
            List<String> allPackageDeclarations =
                    BallerinaUtil.findAllImportedFunctions(project, psiFile);

            if (packageName != null && !allPackageDeclarations.contains(packageName.getText())) {
                holder.createErrorAnnotation(packageName, "Invalid package name");

            }
        }
    }
}
