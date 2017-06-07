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

package org.ballerinalang.plugins.idea.codeInspection;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.daemon.QuickFixBundle;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaElementFactory;
import org.ballerinalang.plugins.idea.util.BallerinaUtil;
import org.jetbrains.annotations.NotNull;

public class AdjustPackageNameFix implements LocalQuickFix {
    private final String myName;

    public AdjustPackageNameFix(String targetPackage) {
        myName = targetPackage;
    }

    @Override
    @NotNull
    public String getName() {
        return QuickFixBundle.message("adjust.package.text", myName);
    }

    @Override
    @NotNull
    public String getFamilyName() {
        return QuickFixBundle.message("adjust.package.family");
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        if (element == null) {
            return;
        }
        PsiFile file = element.getContainingFile();
        if (!FileModificationService.getInstance().prepareFileForWrite(file)) {
            return;
        }

        PsiDirectory directory = file.getContainingDirectory();
        if (directory == null) {
            return;
        }

        String targetPackage = BallerinaUtil.suggestPackageNameForDirectory(directory);

        try {
            PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.findChildOfType(file,
                    PackageDeclarationNode.class);
            if (targetPackage.isEmpty()) {
                if (packageDeclarationNode != null) {
                    packageDeclarationNode.delete();
                }
            } else {
                PackageDeclarationNode newPackageDeclarationNode =
                        BallerinaElementFactory.createPackageDeclaration(project, targetPackage);
                if (packageDeclarationNode != null) {
                    packageDeclarationNode.replace(newPackageDeclarationNode);
                } else {
                    file.addAfter(newPackageDeclarationNode, null);
                }
            }
        } catch (IncorrectOperationException e) {
        }
    }
}
