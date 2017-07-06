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
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConnectorReferenceNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ActionInvocationReference extends BallerinaElementReference {

    public ActionInvocationReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();
        ConnectorReferenceNode connectorReferenceNode = PsiTreeUtil.getChildOfType(parent,
                ConnectorReferenceNode.class);
        if (connectorReferenceNode == null) {
            return null;
        }
        PsiReference reference = connectorReferenceNode.findReferenceAt(connectorReferenceNode.getTextLength());
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
}
