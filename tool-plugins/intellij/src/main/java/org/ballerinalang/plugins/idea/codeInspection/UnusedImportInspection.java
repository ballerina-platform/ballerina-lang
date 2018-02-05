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
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.FullyQualifiedPackageNameNode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.XmlAttribNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class UnusedImportInspection extends LocalInspectionTool {

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

        // This is used to track all packages used in the file.
        List<String> usedPackages = new LinkedList<>();

        LocalQuickFix[] availableFixes = new LocalQuickFix[0];

        List<ProblemDescriptor> problemDescriptors = new LinkedList<>();
        Collection<PackageNameNode> packageNameNodes = PsiTreeUtil.findChildrenOfType(file, PackageNameNode.class);
        for (PackageNameNode packageNameNode : packageNameNodes) {
            ProgressManager.checkCanceled();
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
            usedPackages.add(nameIdentifier.getText());
        }

        // This is used to keep track of fully qualified imported packages. This will be used to identify redeclared
        // import statements.
        List<String> fullyQualifiedImportedPackages = new LinkedList<>();
        // This is used to keep track of last package in import declaration. This will be used to identify importing
        // multiple packages which ends with same name.
        List<String> importedPackages = new LinkedList<>();

        Collection<ImportDeclarationNode> importDeclarationNodes = PsiTreeUtil.findChildrenOfType(file,
                ImportDeclarationNode.class);
        for (ImportDeclarationNode importDeclarationNode : importDeclarationNodes) {
            ProgressManager.checkCanceled();
            if (importDeclarationNode == null) {
                continue;
            }
            // Check unused imports. No need to check for fully qualified path since we cant import packages of same
            // name.
            List<PackageNameNode> packageNames = new ArrayList<>(PsiTreeUtil.findChildrenOfType(importDeclarationNode,
                    PackageNameNode.class));
            PackageNameNode lastPackage = ContainerUtil.getLastItem(packageNames);
            if (lastPackage == null) {
                continue;
            }
            String lastPackageName = lastPackage.getText();
            if (!usedPackages.contains(lastPackageName)) {
                problemDescriptors.add(createProblemDescriptor(manager, "Unused import", isOnTheFly,
                        importDeclarationNode, availableFixes, ProblemHighlightType.LIKE_UNUSED_SYMBOL));
            }

            // Check conflicting imports (which ends with same package name).
            if (importedPackages.contains(lastPackageName)) {
                problemDescriptors.add(createProblemDescriptor(manager, "Conflicting import", isOnTheFly,
                        importDeclarationNode, availableFixes, ProblemHighlightType.GENERIC_ERROR));
            }
            importedPackages.add(lastPackageName);

            // Check redeclared imports.
            FullyQualifiedPackageNameNode fullyQualifiedPackageName = PsiTreeUtil.getChildOfType(importDeclarationNode,
                    FullyQualifiedPackageNameNode.class);
            if (fullyQualifiedPackageName == null) {
                continue;
            }
            if (fullyQualifiedImportedPackages.contains(fullyQualifiedPackageName.getText())) {
                problemDescriptors.add(createProblemDescriptor(manager, "Redeclared import", isOnTheFly,
                        importDeclarationNode, availableFixes, ProblemHighlightType.GENERIC_ERROR));
            }

            fullyQualifiedImportedPackages.add(fullyQualifiedPackageName.getText());
        }

        return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
    }

    @NotNull
    private ProblemDescriptor createProblemDescriptor(@NotNull InspectionManager manager, @NotNull String description,
                                                      boolean isOnTheFly, @NotNull PsiElement element,
                                                      @NotNull LocalQuickFix[] availableFixes,
                                                      ProblemHighlightType problemHighlightType) {
        return manager.createProblemDescriptor(element, description, isOnTheFly, availableFixes, problemHighlightType);
    }
}
