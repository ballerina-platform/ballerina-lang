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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.CodeBlockParameterNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConnectorReferenceNode;
import org.ballerinalang.plugins.idea.psi.EndpointDeclarationNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ActionInvocationReference extends BallerinaElementReference {

    public ActionInvocationReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();

        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(parent);
        PsiReference variableReference = null;
        if (prevVisibleLeaf != null && ".".equals(prevVisibleLeaf.getText())) {
            PsiElement connectorVariable = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
            if (connectorVariable != null) {
                variableReference = connectorVariable.findReferenceAt(connectorVariable.getTextLength());
            }
        } else {
            PsiElement prevSibling = parent.getPrevSibling();
            variableReference = prevSibling.findReferenceAt(prevSibling.getTextLength());
        }

        if (variableReference == null) {
            return null;
        }
        PsiElement variableDefinition = variableReference.resolve();
        if (variableDefinition == null) {
            return null;
        }
        PsiElement variableDefinitionParent = variableDefinition.getParent();
        ConnectorDefinitionNode connectorDefinitionNode;
        if (variableDefinitionParent instanceof EndpointDeclarationNode) {
            connectorDefinitionNode = BallerinaPsiImplUtil.getConnectorDefinition(((EndpointDeclarationNode)
                    variableDefinitionParent));
        } else {
            connectorDefinitionNode =
                    BallerinaPsiImplUtil.resolveConnectorFromVariableDefinitionNode(variableDefinitionParent);
        }
        if (connectorDefinitionNode == null) {
            return null;
        }
        Collection<ActionDefinitionNode> actionDefinitionNodes = PsiTreeUtil.findChildrenOfType
                (connectorDefinitionNode, ActionDefinitionNode.class);
        for (ActionDefinitionNode actionDefinitionNode : actionDefinitionNodes) {
            IdentifierPSINode actionIdentifier = PsiTreeUtil.getChildOfType(actionDefinitionNode,
                    IdentifierPSINode.class);
            if (actionIdentifier == null) {
                continue;
            }
            if (actionIdentifier.getText().equals(identifier.getText())) {
                return actionIdentifier;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();
        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(identifier);
        PsiElement resolvedElement = null;

        if (prevVisibleLeaf != null && ".".equals(prevVisibleLeaf.getText())) {
            PsiElement prevSibling = prevVisibleLeaf.getPrevSibling();
            if (prevSibling != null) {
                PsiReference reference = prevSibling.findReferenceAt(prevSibling.getTextLength());
                if (reference == null) {
                    return new Object[0];
                }
                resolvedElement = reference.resolve();
            } else {
                PsiElement variableReference = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
                if (variableReference != null) {
                    PsiReference reference = variableReference.findReferenceAt(variableReference.getTextLength());
                    if (reference == null) {
                        return new Object[0];
                    }
                    resolvedElement = reference.resolve();
                }
            }
        } else {
            ConnectorReferenceNode connectorReferenceNode = PsiTreeUtil.getChildOfType(parent,
                    ConnectorReferenceNode.class);
            if (connectorReferenceNode == null) {
                return new Object[0];
            }
            PsiReference reference = connectorReferenceNode.findReferenceAt(connectorReferenceNode.getTextLength());
            if (reference == null) {
                return new Object[0];
            }
            resolvedElement = reference.resolve();
        }
        if (resolvedElement == null) {
            return new Object[0];
        }
        PsiElement connectorNode = resolvedElement.getParent();
        if (connectorNode instanceof VariableDefinitionNode || connectorNode instanceof CodeBlockParameterNode) {
            connectorNode = BallerinaPsiImplUtil.resolveConnectorFromVariableDefinitionNode(connectorNode);
        } else if (connectorNode instanceof EndpointDeclarationNode) {
            connectorNode = BallerinaPsiImplUtil.getConnectorDefinition(((EndpointDeclarationNode) connectorNode));
        }

        if (connectorNode == null) {
            return new Object[0];
        }

        List<IdentifierPSINode> actions = BallerinaPsiImplUtil.getAllActionsFromAConnector(connectorNode);
        List<LookupElement> results = BallerinaCompletionUtils.createActionLookupElements(actions);

        return results.toArray(new LookupElement[results.size()]);
    }
}
