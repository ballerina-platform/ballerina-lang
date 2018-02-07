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

package org.ballerinalang.plugins.idea.codeinspection;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.DefinitionNode;
import org.ballerinalang.plugins.idea.psi.FullyQualifiedPackageNameNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.sdk.BallerinaSdkService;
import org.ballerinalang.plugins.idea.util.BallerinaUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class WrongPackageStatementInspection extends LocalInspectionTool {

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
        Module module = ModuleUtil.findModuleForFile(file.getVirtualFile(), file.getProject());
        boolean isBallerinaModule = BallerinaSdkService.getInstance(file.getProject()).isBallerinaModule(module);
        if (!isBallerinaModule) {
            return new ProblemDescriptor[0];
        }
        BallerinaFile ballerinaFile = (BallerinaFile) file;
        PsiDirectory directory = ballerinaFile.getContainingDirectory();
        if (directory == null) {
            return new ProblemDescriptor[0];
        }

        List<ProblemDescriptor> problemDescriptors = new LinkedList<>();

        String packageName = BallerinaUtil.suggestPackageNameForDirectory(directory);
        PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.findChildOfType(file, PackageDeclarationNode.class);
        Collection<DefinitionNode> definitionNodes = PsiTreeUtil.findChildrenOfType(file, DefinitionNode.class);
        for (DefinitionNode definitionNode : definitionNodes) {
            PsiElement firstChild = definitionNode.getFirstChild();
            if (firstChild == null || !(firstChild instanceof IdentifierDefSubtree)) {
                return new ProblemDescriptor[0];
            }

            PsiElement nameIdentifier = ((IdentifierDefSubtree) firstChild).getNameIdentifier();
            if (nameIdentifier == null) {
                return new ProblemDescriptor[0];
            }
            if (!Comparing.strEqual(packageName, "", true) && packageDeclarationNode == null) {
                String description = "Missing package statement: '" + packageName + "'";
                ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(nameIdentifier, description,
                        new AdjustPackageNameFix(nameIdentifier, packageName),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly);
                problemDescriptors.add(problemDescriptor);
            }
        }
        if (!problemDescriptors.isEmpty()) {
            return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
        }

        if (packageDeclarationNode == null) {
            return new ProblemDescriptor[0];
        }
        FullyQualifiedPackageNameNode fullyQualifiedPackageNameNode = PsiTreeUtil.findChildOfType
                (packageDeclarationNode, FullyQualifiedPackageNameNode.class);
        if (fullyQualifiedPackageNameNode == null || fullyQualifiedPackageNameNode.getText().isEmpty()) {
            return new ProblemDescriptor[0];
        }

        Collection<PackageNameNode> packageNames = PsiTreeUtil.findChildrenOfType(packageDeclarationNode,
                PackageNameNode.class);
        if (packageNames.isEmpty()) {
            return new ProblemDescriptor[0];
        }

        LinkedList<PackageNameNode> packageNameNodes = new LinkedList<>();
        packageNameNodes.addAll(packageNames);
        PackageNameNode lastElement = packageNameNodes.getLast();
        if (lastElement == null) {
            return new ProblemDescriptor[0];
        }
        List<LocalQuickFix> availableFixes = new ArrayList<>();
        availableFixes.add(new AdjustPackageNameFix(fullyQualifiedPackageNameNode, packageName));
        PsiElement packageNameIdentifier = lastElement.getNameIdentifier();
        if (packageNameIdentifier == null) {
            return getProblemDescriptors(manager, isOnTheFly, packageName, availableFixes,
                    fullyQualifiedPackageNameNode, lastElement);
        }
        PsiReference reference = packageNameIdentifier.getReference();
        if (reference == null) {
            return getProblemDescriptors(manager, isOnTheFly, packageName, availableFixes,
                    fullyQualifiedPackageNameNode, lastElement);
        }
        PsiElement resolvedElement = reference.resolve();
        if (!(resolvedElement instanceof PsiDirectory)) {
            return getProblemDescriptors(manager, isOnTheFly, packageName, availableFixes,
                    fullyQualifiedPackageNameNode, lastElement);
        }
        String containingDirectoryPackageName =
                BallerinaUtil.suggestPackageNameForDirectory(((PsiDirectory) resolvedElement));
        if (!Comparing.equal(packageName, containingDirectoryPackageName, true)) {
            if (!availableFixes.isEmpty()) {
                return getProblemDescriptors(manager, isOnTheFly, packageName, availableFixes,
                        fullyQualifiedPackageNameNode,
                        lastElement);
            }
        }
        return new ProblemDescriptor[0];
    }

    @NotNull
    private ProblemDescriptor[] getProblemDescriptors(@NotNull InspectionManager manager, boolean isOnTheFly,
                                                      String packageName, List<LocalQuickFix> availableFixes,
                                                      FullyQualifiedPackageNameNode fullyQualifiedPackageNameNode,
                                                      PackageNameNode lastElement) {
        String description = "Package name '" + lastElement.getText() + "' does not correspond to the file path '" +
                packageName + "'";
        LocalQuickFix[] fixes = availableFixes.toArray(new LocalQuickFix[availableFixes.size()]);
        ProblemDescriptor descriptor = manager.createProblemDescriptor(fullyQualifiedPackageNameNode, description,
                isOnTheFly, fixes, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        return new ProblemDescriptor[]{descriptor};
    }
}
