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
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.FileTypeUtils;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.ActionInvocationNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttributeNode;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.ConnectorReferenceNode;
import org.ballerinalang.plugins.idea.psi.FunctionReferenceNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
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
            ProgressManager.checkCanceled();
            if (packageNameNode == null) {
                continue;
            }
            PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.getParentOfType(packageNameNode,
                    PackageDeclarationNode.class);
            if (packageDeclarationNode != null) {
                continue;
            }
            XmlAttribNode xmlAttribNode = PsiTreeUtil.getParentOfType(packageNameNode, XmlAttribNode.class);
            if (xmlAttribNode != null) {
                continue;
            }

            LocalQuickFix[] availableFixes;

            ImportDeclarationNode importDeclarationNode = PsiTreeUtil.getParentOfType(packageNameNode,
                    ImportDeclarationNode.class);
            if (importDeclarationNode != null) {
                availableFixes = new LocalQuickFix[0];
            } else {
                BallerinaImportPackageQuickFix quickFix = new BallerinaImportPackageQuickFix(packageNameNode);
                availableFixes = new LocalQuickFix[]{quickFix};
            }

            PsiElement nameIdentifier = packageNameNode.getNameIdentifier();
            if (nameIdentifier == null) {
                continue;
            }
            PsiReference reference = nameIdentifier.getReference();
            if (reference == null || reference.resolve() == null) {
                problemDescriptors.add(createProblemDescriptor(manager, packageNameNode, isOnTheFly, availableFixes));
            }
        }

        // Todo - Add new quick fixes.
        LocalQuickFix[] availableFixes = new LocalQuickFix[0];

        Collection<NameReferenceNode> nameReferenceNodes = PsiTreeUtil.findChildrenOfType(file,
                NameReferenceNode.class);
        problemDescriptors.addAll(getUnresolvedReferenceDescriptors(manager, isOnTheFly, availableFixes,
                nameReferenceNodes));

        Collection<AnnotationAttributeNode> annotationAttributeNodes = PsiTreeUtil.findChildrenOfType(file,
                AnnotationAttributeNode.class);
        problemDescriptors.addAll(getUnresolvedReferenceDescriptors(manager, isOnTheFly, availableFixes,
                annotationAttributeNodes));

        Collection<ConnectorReferenceNode> connectorReferenceNodes = PsiTreeUtil.findChildrenOfType(file,
                ConnectorReferenceNode.class);
        problemDescriptors.addAll(getUnresolvedReferenceDescriptors(manager, isOnTheFly, availableFixes,
                connectorReferenceNodes));

        Collection<ActionInvocationNode> actionInvocationNodes = PsiTreeUtil.findChildrenOfType(file,
                ActionInvocationNode.class);
        problemDescriptors.addAll(getUnresolvedReferenceDescriptors(manager, isOnTheFly, availableFixes,
                actionInvocationNodes));

        Collection<FunctionReferenceNode> functionReferenceNodes = PsiTreeUtil.findChildrenOfType(file,
                FunctionReferenceNode.class);
        problemDescriptors.addAll(getUnresolvedReferenceDescriptors(manager, isOnTheFly, availableFixes,
                functionReferenceNodes));

        return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
    }

    private <T extends PsiElement> List<ProblemDescriptor> getUnresolvedReferenceDescriptors(
            @NotNull InspectionManager manager, boolean isOnTheFly, @NotNull LocalQuickFix[] availableFixes,
            @NotNull Collection<T> nodes) {
        List<ProblemDescriptor> problemDescriptors = new LinkedList<>();
        for (T annotationAttributeNode : nodes) {
            ProgressManager.checkCanceled();
            if (annotationAttributeNode == null) {
                continue;
            }
            IdentifierPSINode identifier = PsiTreeUtil.getChildOfType(annotationAttributeNode, IdentifierPSINode.class);
            if (identifier == null) {
                continue;
            }
            PsiReference reference = identifier.getReference();
            if (reference == null || reference.resolve() == null) {
                problemDescriptors.add(createProblemDescriptor(manager, identifier, isOnTheFly, availableFixes));
            }
        }
        return problemDescriptors;
    }

    @NotNull
    private ProblemDescriptor createProblemDescriptor(@NotNull InspectionManager manager, @NotNull PsiElement element,
                                                      boolean isOnTheFly, @NotNull LocalQuickFix[] availableFixes) {
        String description = "Unresolved reference <code>#ref</code> #loc";
        return createProblemDescriptor(manager, description, isOnTheFly, element, availableFixes);
    }

    @NotNull
    private ProblemDescriptor createProblemDescriptor(@NotNull InspectionManager manager, @NotNull String description,
                                                      boolean isOnTheFly, @NotNull PsiElement element,
                                                      @NotNull LocalQuickFix[] availableFixes) {
        return manager.createProblemDescriptor(element, description, isOnTheFly, availableFixes,
                ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
    }
}
