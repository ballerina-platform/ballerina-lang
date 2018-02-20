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

package org.ballerinalang.plugins.idea.psi.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a type reference.
 */
public class TypeReference extends BallerinaElementReference {

    private PsiElement typeNameNode;

    public TypeReference(@NotNull IdentifierPSINode element, PsiElement typeNameNode) {
        super(element);
        this.typeNameNode = typeNameNode;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return resolve(typeNameNode, getElement());
    }

    @Nullable
    public static PsiElement resolve(PsiElement typeNameNode, IdentifierPSINode identifier) {
        String valueTypeName = typeNameNode.getText();
        Project project = typeNameNode.getProject();
        PsiFile psiFile = BallerinaPsiImplUtil.findPsiFileInSDK(project, typeNameNode,
                "/ballerina/builtin/" + valueTypeName + "lib.bal");
        if (psiFile == null) {
            return null;
        }
        List<IdentifierPSINode> functionDefinitions =
                BallerinaPsiImplUtil.getMatchingElementsFromAFile(psiFile, FunctionDefinitionNode.class, false);
        for (IdentifierPSINode functionDefinition : functionDefinitions) {
            if (functionDefinition != null && identifier.getText().equals(functionDefinition.getText())) {
                return functionDefinition;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return getVariants(typeNameNode);
    }

    @NotNull
    public static Object[] getVariants(PsiElement typeNameNode) {
        String valueTypeName = typeNameNode.getText();
        Project project = typeNameNode.getProject();
        PsiFile psiFile = BallerinaPsiImplUtil.findPsiFileInSDK(project, typeNameNode,
                "/ballerina/builtin/" + valueTypeName + "lib.bal");
        if (psiFile == null) {
            return new LookupElement[0];
        }
        List<LookupElement> results = new LinkedList<>();
        List<IdentifierPSINode> functionDefinitions =
                BallerinaPsiImplUtil.getMatchingElementsFromAFile(psiFile, FunctionDefinitionNode.class, false);
        results.addAll(BallerinaCompletionUtils.createFunctionLookupElements(functionDefinitions));
        return results.toArray(new LookupElement[results.size()]);
    }
}
