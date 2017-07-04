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
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FieldReference extends BallerinaElementReference {

    public FieldReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return false;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        // Get the current element.
        IdentifierPSINode identifier = getElement();
        // Get the parent element.
        PsiElement parent = identifier.getParent();
        // Get the previous element.
        PsiElement prevSibling = parent.getPrevSibling();
        // If the previous element is not null and is an instance of VariableReferenceNode, that means we have
        // encountered the following situation.
        // user.id - In here, current node is the 'id' node. previous node is the 'user' node.
        if (prevSibling != null && prevSibling instanceof VariableReferenceNode) {
            // We get the reference at end. This is because struct field access can be multiple levels deep.
            // Eg: user.name.first - If the current node is 'first', then the previous node will be 'user.name'. If
            // we get the reference at the beginning, we we get the reference for 'user'. But we want to resolve the
            // 'name' field first. That is why we get the reference at the end.
            PsiReference variableReference = prevSibling.findReferenceAt(prevSibling.getTextLength());
            if (variableReference == null) {
                return null;
            }
            // Resolve the reference. The resolved element can be an identifier of either a struct of a field
            // depending on the current node.
            // Eg: user.name.first - If the current node is 'name', resolved element will be a struct definition. if
            // the current element is 'first', then the resolved element will be a field definition.
            PsiElement resolvedElement = variableReference.resolve();
            if (resolvedElement == null) {
                return null;
            }

            // Get the parent of the resolved element.
            PsiElement resolvedElementParent = resolvedElement.getParent();
            StructDefinitionNode structDefinitionNode = null;
            // Resolve the corresponding resolvedElementParent to get the struct definition.
            if (resolvedElementParent instanceof VariableDefinitionNode) {
                structDefinitionNode = BallerinaPsiImplUtil.resolveStructFromDefinitionNode
                        (((VariableDefinitionNode) resolvedElementParent));
            } else if (resolvedElementParent instanceof FieldDefinitionNode) {
                structDefinitionNode =
                        BallerinaPsiImplUtil.resolveField(((FieldDefinitionNode) resolvedElementParent));
            }
            if (structDefinitionNode == null) {
                return null;
            }
            // Resolve the field and return the resolved element.
            return structDefinitionNode.resolve(identifier);
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        // Todo - Implement
        return new Object[0];
    }
}
