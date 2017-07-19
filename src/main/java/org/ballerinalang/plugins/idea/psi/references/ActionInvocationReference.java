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
import org.ballerinalang.plugins.idea.psi.ConnectorReferenceNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
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
        ConnectorReferenceNode connectorReferenceNode = PsiTreeUtil.getChildOfType(parent,
                ConnectorReferenceNode.class);
        PsiReference reference;
        if (connectorReferenceNode != null) {
            reference = connectorReferenceNode.findReferenceAt(connectorReferenceNode.getTextLength());
        } else {
            PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(identifier);
            if (prevVisibleLeaf == null || !".".equals(prevVisibleLeaf.getText())) {
                return null;
            }

            PsiElement connectorName = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
            if (connectorName == null || !(connectorName instanceof IdentifierPSINode)) {
                return null;
            }
            reference = connectorName.findReferenceAt(connectorName.getTextLength());
        }
        if (reference == null) {
            return null;
        }

        PsiElement connectorIdentifier = reference.resolve();
        if (connectorIdentifier == null) {
            return null;
        }
        PsiElement connectorNode = connectorIdentifier.getParent();
        if (connectorNode == null) {
            return null;
        }
        Collection<ActionDefinitionNode> actionDefinitionNodes = PsiTreeUtil.findChildrenOfType(connectorNode,
                ActionDefinitionNode.class);
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

        List<PsiElement> actions = BallerinaPsiImplUtil.getAllActionsFromAConnector(resolvedElement.getParent());
        List<LookupElement> results = BallerinaCompletionUtils.createActionLookupElements(actions);

        return results.toArray(new LookupElement[results.size()]);
    }
}
