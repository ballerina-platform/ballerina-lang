/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.plugins.idea.codeinspection;

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
import com.intellij.psi.util.PsiTreeUtil;
import io.ballerina.plugins.idea.psi.BallerinaFile;
import io.ballerina.plugins.idea.psi.BallerinaIdentifier;
import io.ballerina.plugins.idea.psi.BallerinaOrgName;
import io.ballerina.plugins.idea.psi.BallerinaPackageName;
import io.ballerina.plugins.idea.psi.BallerinaPackageReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Checks for unresolved references in the code.
 */
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

        List<ProblemDescriptor> problemDescriptors = new LinkedList<>();
        LocalQuickFix[] availableFixes = new LocalQuickFix[0];
        Collection<BallerinaIdentifier> nameReferenceNodes = PsiTreeUtil.findChildrenOfType(file,
                BallerinaIdentifier.class);
        problemDescriptors.addAll(getUnresolvedNameReferenceDescriptors(manager, isOnTheFly, availableFixes,
                nameReferenceNodes));
        return problemDescriptors.toArray(new ProblemDescriptor[0]);
    }

    private List<ProblemDescriptor> getUnresolvedNameReferenceDescriptors(@NotNull InspectionManager manager,
                                                                          boolean isOnTheFly,
                                                                          @NotNull LocalQuickFix[] availableFixes,
                                                                          @NotNull Collection<BallerinaIdentifier>
                                                                                  identifiers) {
        List<ProblemDescriptor> problemDescriptors = new LinkedList<>();
        for (BallerinaIdentifier identifier : identifiers) {
            ProgressManager.checkCanceled();
            if (identifier == null || "_".equals(identifier.getText())) {
                continue;
            }
            BallerinaOrgName orgName = PsiTreeUtil.getParentOfType(identifier, BallerinaOrgName.class);
            if (orgName != null) {
                continue;
            }
            BallerinaPackageName packageNameNode = PsiTreeUtil.getParentOfType(identifier, BallerinaPackageName.class);
            if (packageNameNode != null) {
                continue;
            }

            PsiElement parent = identifier.getParent();
            if (parent instanceof BallerinaPackageReference) {
                PsiReference reference = parent.getReference();
                if (reference == null || reference.resolve() == null) {
                    availableFixes = new LocalQuickFix[]{new BallerinaImportPackageQuickFix(identifier)};
                    problemDescriptors.add(createProblemDescriptor(manager, identifier, isOnTheFly, availableFixes));
                }
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
