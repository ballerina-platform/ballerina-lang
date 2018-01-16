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
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.ExpressionListNode;
import org.ballerinalang.plugins.idea.psi.ExpressionNode;
import org.ballerinalang.plugins.idea.psi.FunctionInvocationNode;
import org.ballerinalang.plugins.idea.psi.FunctionReferenceNode;
import org.ballerinalang.plugins.idea.psi.ParameterListNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FunctionInvocationInspection extends LocalInspectionTool {

    @Override
    @Nullable
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager,
                                         boolean isOnTheFly) {
        List<ProblemDescriptor> problemDescriptors = new LinkedList<>();
        LocalQuickFix[] availableFixes = new LocalQuickFix[0];
        Collection<FunctionInvocationNode> functionInvocations = PsiTreeUtil.findChildrenOfType(file,
                FunctionInvocationNode.class);
        for (FunctionInvocationNode functionInvocation : functionInvocations) {
            ProgressManager.checkCanceled();
            // We first need to resolve the reference to the function definition.
            FunctionReferenceNode functionReferenceNode = PsiTreeUtil.getChildOfType(functionInvocation,
                    FunctionReferenceNode.class);
            if (functionReferenceNode == null) {
                continue;
            }
            PsiReference reference = functionReferenceNode.findReferenceAt(functionReferenceNode.getTextLength());
            if (reference == null) {
                continue;
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                continue;
            }
            PsiElement definitionNode = resolvedElement.getParent();
            ParameterListNode parameterListNode = PsiTreeUtil.getChildOfType(definitionNode, ParameterListNode.class);
            if (parameterListNode == null) {
                continue;
            }
            // Todo - move to util method.
            // Get the parameters required for the function.
            Collection<ParameterNode> parameterNodes = PsiTreeUtil.getChildrenOfTypeAsList(parameterListNode,
                    ParameterNode.class);

            // Todo - move to util method.
            // Then we need to find current arguments provided for the function invocation.
            ExpressionListNode expressionListNode = PsiTreeUtil.getChildOfType(functionInvocation,
                    ExpressionListNode.class);
            if (expressionListNode == null) {
                if (!parameterNodes.isEmpty()) {
                    String description = String.format("Missing arguments for function '%s'. Required: %d, Found: %d.",
                            reference.getElement().getText(), parameterNodes.size(), 0);
                    ProblemDescriptor problemDescriptor = createProblemDescriptor(manager, description,
                            isOnTheFly, functionInvocation, availableFixes);
                    problemDescriptors.add(problemDescriptor);
                }
                continue;
            }
            // Get the expression nodes. Each of these are an argument to the function invocation.
            ExpressionNode[] expressionNodes = PsiTreeUtil.getChildrenOfType(expressionListNode, ExpressionNode.class);
            if (expressionNodes == null) {
                continue;
            }
            String message;
            if (parameterNodes.size() > expressionNodes.length) {
                message = String.format("Missing arguments for function '%s'.", reference.getElement().getText());
            } else if (parameterNodes.size() < expressionNodes.length) {
                message = String.format("Too many arguments for function '%s'.", reference.getElement().getText());
            } else {
                // If the number of arguments and parameters are equal, we don't need to add a problem descriptor.
                // Todo - Check for types.
                continue;
            }
            String description = String.format("%s Required: %d, Found: %d.", message, parameterNodes.size(),
                    expressionNodes.length);
            ProblemDescriptor problemDescriptor = createProblemDescriptor(manager, description, isOnTheFly,
                    functionInvocation, availableFixes);
            problemDescriptors.add(problemDescriptor);
        }

        Collection<VariableReferenceNode> variableReferenceNodes = PsiTreeUtil.findChildrenOfType(file,
                VariableReferenceNode.class);

        for (VariableReferenceNode variableReferenceNode : variableReferenceNodes) {
            ProgressManager.checkCanceled();
            // Todo - move to util method.
            if (!BallerinaPsiImplUtil.isFunctionInvocation(variableReferenceNode)) {
                continue;
            }
            PsiReference reference = variableReferenceNode.findReferenceAt(variableReferenceNode.getTextLength());
            if (reference == null) {
                continue;
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                continue;
            }
            PsiElement definitionNode = resolvedElement.getParent();
            ParameterListNode parameterListNode = PsiTreeUtil.getChildOfType(definitionNode, ParameterListNode.class);
            if (parameterListNode == null) {
                continue;
            }
            Collection<ParameterNode> parameterNodes = PsiTreeUtil.findChildrenOfType(parameterListNode,
                    ParameterNode.class);
            // Todo - move to util method.
            PsiElement parent = variableReferenceNode.getParent();
            ExpressionListNode expressionListNode = PsiTreeUtil.getChildOfType(parent, ExpressionListNode.class);
            if (expressionListNode == null) {
                if (!parameterNodes.isEmpty()) {
                    String description = String.format("Missing arguments for function '%s'. Required: %d, Found: %d.",
                            variableReferenceNode.getText(), parameterNodes.size(), 0);
                    ProblemDescriptor problemDescriptor = createProblemDescriptor(manager, description, isOnTheFly,
                            parent, availableFixes);
                    problemDescriptors.add(problemDescriptor);
                }
                continue;
            }
            ExpressionNode[] expressionNodes = PsiTreeUtil.getChildrenOfType(expressionListNode, ExpressionNode.class);
            if (expressionNodes == null) {
                continue;
            }
            String message;
            if (parameterNodes.size() > expressionNodes.length) {
                message = String.format("Missing arguments for function '%s'.", variableReferenceNode.getText());
            } else if (parameterNodes.size() < expressionNodes.length) {
                message = String.format("Too many arguments for function '%s'.", variableReferenceNode.getText());
            } else {
                // If the number of arguments and parameters are equal, we don't need to add a problem descriptor.
                // Todo - Check for types.
                continue;
            }
            String description = String.format("%s Required: %d, Found: %d.", message, parameterNodes.size(),
                    expressionNodes.length);
            ProblemDescriptor problemDescriptor = createProblemDescriptor(manager, description, isOnTheFly, parent,
                    availableFixes);
            problemDescriptors.add(problemDescriptor);
        }
        return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
    }

    @NotNull
    private ProblemDescriptor createProblemDescriptor(@NotNull InspectionManager manager, @NotNull String description,
                                                      boolean isOnTheFly, @NotNull PsiElement element,
                                                      @NotNull LocalQuickFix[] availableFixes) {
        return manager.createProblemDescriptor(element, description, isOnTheFly, availableFixes,
                ProblemHighlightType.GENERIC_ERROR);
    }
}
