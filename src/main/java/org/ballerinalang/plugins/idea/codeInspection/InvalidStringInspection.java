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
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.QuotedLiteralString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class InvalidStringInspection extends LocalInspectionTool {

    @Override
    @Nullable
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager,
                                         boolean isOnTheFly) {
        List<ProblemDescriptor> problemDescriptors = new LinkedList<>();
        List<LocalQuickFix> availableFixes = new ArrayList<>();
        Collection<QuotedLiteralString> strings = PsiTreeUtil.findChildrenOfType(file,
                QuotedLiteralString.class);
        for (QuotedLiteralString string : strings) {
            ProgressManager.checkCanceled();
            if (!string.getText().matches(".*(?<!\\\\)(\\\\\\\\)*(\\\\\")?\"")) {
                ProblemDescriptor problemDescriptor = getProblemDescriptor(manager, isOnTheFly, string, availableFixes);
                problemDescriptors.add(problemDescriptor);
            }
        }
        return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
    }

    @NotNull
    private ProblemDescriptor getProblemDescriptor(@NotNull InspectionManager manager, boolean isOnTheFly,
                                                   @NotNull PsiElement element,
                                                   @NotNull List<LocalQuickFix> availableFixes) {
        LocalQuickFix[] fixes = availableFixes.toArray(new LocalQuickFix[availableFixes.size()]);
        return manager.createProblemDescriptor(element, "Unclosed string literal", isOnTheFly, fixes,
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }
}
