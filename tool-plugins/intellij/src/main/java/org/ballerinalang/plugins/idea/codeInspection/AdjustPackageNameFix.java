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
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.openapi.editor.Editor;
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
import org.jetbrains.annotations.Nullable;

public class AdjustPackageNameFix extends LocalQuickFixAndIntentionActionOnPsiElement {

    private final String myName;

    AdjustPackageNameFix(@Nullable PsiElement element, @NotNull String targetPackage) {
        super(element);
        myName = targetPackage;
    }

    @Override
    @NotNull
    public String getFamilyName() {
        return "Adjust package name";
    }

    @NotNull
    @Override
    public String getText() {
        if (myName.isEmpty()) {
            return "Remove package declaration";
        }
        return "Set package name to '" + myName + "'";
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file, @Nullable Editor editor,
                       @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
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
