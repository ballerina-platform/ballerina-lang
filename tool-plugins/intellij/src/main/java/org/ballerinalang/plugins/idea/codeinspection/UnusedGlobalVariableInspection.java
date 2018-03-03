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
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Checks for unused global variables in the code.
 */
public class UnusedGlobalVariableInspection extends LocalInspectionTool {

    @Override
    @Nullable
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager,
                                         boolean isOnTheFly) {
        List<ProblemDescriptor> problemDescriptors = new LinkedList<>();
        List<LocalQuickFix> availableFixes = new ArrayList<>();

        Collection<GlobalVariableDefinitionNode> globalVariables = PsiTreeUtil.findChildrenOfType(file,
                GlobalVariableDefinitionNode.class);
        for (GlobalVariableDefinitionNode variable : globalVariables) {
            ProgressManager.checkCanceled();
            PsiElement identifier = variable.getNameIdentifier();
            if (identifier == null) {
                continue;
            }
            Query<PsiReference> psiReferences = ReferencesSearch.search(identifier);
            PsiReference firstReference = psiReferences.findFirst();
            if (firstReference == null) {
                ProblemDescriptor problemDescriptor = getProblemDescriptor(manager, isOnTheFly, identifier,
                        availableFixes);
                problemDescriptors.add(problemDescriptor);
            }
        }

        return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
    }

    @NotNull
    private ProblemDescriptor getProblemDescriptor(@NotNull InspectionManager manager, boolean isOnTheFly,
                                                   @NotNull PsiElement identifier,
                                                   @NotNull List<LocalQuickFix> availableFixes) {
        LocalQuickFix[] fixes = availableFixes.toArray(new LocalQuickFix[availableFixes.size()]);
        return manager.createProblemDescriptor(identifier, "Unused global variable <code>#ref</code>" +
                " #loc", isOnTheFly, fixes, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
    }
}
