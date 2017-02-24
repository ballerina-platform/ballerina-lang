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
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionBodyNode;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.NamedParameterNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeMapperBodyNode;
import org.ballerinalang.plugins.idea.psi.TypeMapperInputNode;
import org.ballerinalang.plugins.idea.psi.TypeMapperNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.jetbrains.annotations.NotNull;

public class VariableReference extends BallerinaElementReference {

    public VariableReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return def instanceof VariableDefinitionNode || def instanceof VariableReferenceNode
                || def instanceof ParameterNode || def instanceof NamedParameterNode
                || def instanceof TypeMapperInputNode || def instanceof ConstantDefinitionNode;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return super.multiResolve(incompleteCode);
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

            if (definitionElement instanceof ParameterNode) {
                if (!(myElement.getParent() instanceof VariableReferenceNode)) {
                    return false;
                }
                // If the common context is file, that means the myElement is not in the scope where the
                // definitionElement is defined in.
                PsiElement commonContext = PsiTreeUtil.findCommonContext(definitionElement, myElement);
                if (!(commonContext instanceof FunctionNode || commonContext instanceof ResourceDefinitionNode
                        || commonContext instanceof ConnectorDefinitionNode
                        || commonContext instanceof ActionDefinitionNode)) {
                    return false;
                }
            } else if (definitionElement instanceof VariableDefinitionNode) {
                // If the common context is file, that means the myElement is not in the scope where the
                // definitionElement is defined in.
                PsiElement commonContext = PsiTreeUtil.findCommonContext(definitionElement, myElement);
                if (!(commonContext instanceof FunctionBodyNode || commonContext instanceof ConnectorBodyNode
                        || commonContext instanceof TypeMapperBodyNode)) {
                    return false;
                }
            } else if (definitionElement instanceof NamedParameterNode) {
                // The parent of myElement must be a VariableReferenceNode. If this is not checked, The named
                // parameter definition will also be added as a usage when we use Find Usages.
                if (!(myElement.getParent() instanceof VariableReferenceNode)) {
                    return false;
                }
                PsiElement nameIdentifier = ((NamedParameterNode) definitionElement).getNameIdentifier();
                if (nameIdentifier == null) {
                    return false;
                }
                return refName.equals(nameIdentifier.getText());
            } else if (definitionElement instanceof TypeMapperInputNode) {
                PsiElement commonContext = PsiTreeUtil.findCommonContext(definitionElement, myElement);
                if (!(commonContext instanceof TypeMapperNode)) {
                    return false;
                }
            }
            return refName != null && defName != null && refName.equals(defName);
        }
        return false;
    }
}
