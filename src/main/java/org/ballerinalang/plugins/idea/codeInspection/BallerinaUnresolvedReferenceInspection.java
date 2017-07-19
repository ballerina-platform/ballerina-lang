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

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.FileTypeUtils;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.XmlAttribNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class BallerinaUnresolvedReferenceInspection extends LocalInspectionTool {

    @Override
    @Nullable
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager,
                                         boolean isOnTheFly) {
        // does not work in tests since CodeInsightTestCase copies file into temporary location
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return new ProblemDescriptor[0];
        }
        if (!(file instanceof BallerinaFile)) {
            return new ProblemDescriptor[0];
        }
        if (FileTypeUtils.isInServerPageFile(file)) {
            return new ProblemDescriptor[0];
        }

        List<ProblemDescriptor> problemDescriptors = new LinkedList<>();
        Collection<PackageNameNode> packageNameNodes = PsiTreeUtil.findChildrenOfType(file, PackageNameNode.class);
        for (PackageNameNode packageNameNode : packageNameNodes) {
            if (packageNameNode == null) {
                continue;
            }
            PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.getParentOfType(packageNameNode,
                    PackageDeclarationNode.class);
            if (packageDeclarationNode != null) {
                continue;
            }
            ImportDeclarationNode importDeclarationNode = PsiTreeUtil.getParentOfType(packageNameNode,
                    ImportDeclarationNode.class);
            if (importDeclarationNode != null) {
                continue;
            }
            XmlAttribNode xmlAttribNode = PsiTreeUtil.getParentOfType(packageNameNode, XmlAttribNode.class);
            if (xmlAttribNode != null) {
                continue;
            }
            PsiElement nameIdentifier = packageNameNode.getNameIdentifier();
            if (nameIdentifier == null) {
                continue;
            }
            PsiReference reference = nameIdentifier.getReference();
            if (reference == null || reference.resolve() == null) {
                LocalQuickFix[] availableFixes = createImportPackageFix(packageNameNode);
                String description = "Unresolved reference " + "'" + packageNameNode.getText() + "'";
                ProblemDescriptor problemDescriptor = createProblemDescriptor(manager, isOnTheFly, description,
                        packageNameNode, availableFixes);
                problemDescriptors.add(problemDescriptor);
            }
        }
        return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
    }

    @NotNull
    private static LocalQuickFix[] createImportPackageFix(@NotNull PackageNameNode packageNameNode) {
        return new LocalQuickFix[]{new BallerinaImportPackageQuickFix(packageNameNode)};
    }

    @NotNull
    private ProblemDescriptor createProblemDescriptor(@NotNull InspectionManager manager, boolean isOnTheFly,
                                                      String description, PackageNameNode packageNameNode,
                                                      LocalQuickFix[] availableFixes) {
        return manager.createProblemDescriptor(packageNameNode, description, isOnTheFly, availableFixes,
                ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
    }
}
