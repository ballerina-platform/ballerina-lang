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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.CallableUnitBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StatementReference extends BallerinaElementReference {

    public StatementReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return def instanceof PackageNameNode || def instanceof VariableDefinitionNode || def instanceof ParameterNode
                || def instanceof ConstantDefinitionNode || def instanceof TypeNameNode
                || def instanceof StructDefinitionNode;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        // WARNING: If find usage shows an error message, verify that the super.resolve() returns an Identifier node.
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length != 0 ? resolveResults[0].getElement() : super.resolve();
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        List<ResolveResult> results = new ArrayList<>();
        // This is used to resolve elements which cannot be parsed properly. We assume that the element is a
        // reference to a definition.
        CallableUnitBodyNode bodyNode = PsiTreeUtil.getParentOfType(getElement(), CallableUnitBodyNode.class);
        if (bodyNode == null) {
            return results.toArray(new ResolveResult[results.size()]);
        }
        // First we get all the definitions in the callable unit body.
        Collection<VariableDefinitionNode> variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(bodyNode,
                VariableDefinitionNode.class);
        // Check and add each result.
        for (VariableDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
            PsiElement nameIdentifier = variableDefinitionNode.getNameIdentifier();
            if (nameIdentifier == null) {
                continue;
            }
            if (myElement.getText().equals(nameIdentifier.getText())) {
                results.add(new PsiElementResolveResult(variableDefinitionNode));
            }
        }

        // We need to check parameters for matches as well. So we need to first get the enclosing definition node.
        IdentifierDefSubtree definitionNode = PsiTreeUtil.getParentOfType(getElement(), FunctionNode.class,
                ResourceDefinitionNode.class, ConnectorNode.class, ActionDefinitionNode.class);
        // Get all parameter nodes.
        Collection<ParameterNode> parameterNodes = PsiTreeUtil.findChildrenOfType(definitionNode, ParameterNode.class);
        // Check and add each result.
        for (ParameterNode parameterNode : parameterNodes) {
            PsiElement nameIdentifier = parameterNode.getNameIdentifier();
            if (nameIdentifier == null) {
                continue;
            }
            if (myElement.getText().equals(nameIdentifier.getText())) {
                results.add(new PsiElementResolveResult(parameterNode));
            }
        }
        return results.toArray(new ResolveResult[results.size()]);
    }

    @Override
    public boolean isReferenceTo(PsiElement definitionElement) {
        String refName = myElement.getName();
        if (definitionElement instanceof IdentifierPSINode && isDefinitionNode(definitionElement.getParent())) {
            definitionElement = definitionElement.getParent();
        }
        if (isDefinitionNode(definitionElement)) {
            PsiElement id = ((PsiNameIdentifierOwner) definitionElement).getNameIdentifier();
            String defName = id != null ? id.getText() : null;

            PsiElement parent = definitionElement.getParent();
            PsiElement temp = myElement;

            boolean inScope = false;
            while (!(temp instanceof PsiFile)) {
                if (parent == temp) {
                    inScope = true;
                    break;
                }
                temp = temp.getParent();
            }
            if (!inScope) {
                return false;
            }
            return refName != null && defName != null && refName.equals(defName);
        }
        return false;
    }
}
