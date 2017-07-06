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
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FieldReference extends BallerinaElementReference {

    public FieldReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        // Get the current element.
        IdentifierPSINode identifier = getElement();
        // Get the parent element.
        PsiElement parent = identifier.getParent();

        PsiElement prevSibling;
        if (parent instanceof StatementNode) {
            // Get the previous element.
            PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(parent);
            if (prevVisibleLeaf == null) {
                return null;
            }
            if (!".".equals(prevVisibleLeaf.getText())) {
                return null;
            }
            prevSibling = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
            if (prevSibling == null) {
                return null;
            }
        } else {
            prevSibling = parent.getPrevSibling();
        }

        if (prevSibling == null) {
            return null;
        }
        // If the previous element is not null and is an instance of VariableReferenceNode, that means we have
        // encountered the following situation.
        // user.id - In here, current node is the 'id' node. previous node is the 'user' node.
        //        if (prevSibling instanceof VariableReferenceNode || ) {


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
        //        }
        //        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {

        IdentifierPSINode identifier = getElement();

        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(identifier);
        if (prevVisibleLeaf == null || !".".equals(prevVisibleLeaf.getText())) {
            return new LookupElement[0];
        }

        List<LookupElement> results = new LinkedList<>();

        PsiElement previousField = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
        if (previousField != null) {
            PsiReference reference = previousField.findReferenceAt(0);
            if (reference == null) {
                return new Object[0];
            }

            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement != null) {
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
                    return results.toArray(new LookupElement[results.size()]);
                }

                Collection<FieldDefinitionNode> fieldDefinitionNodes =
                        PsiTreeUtil.findChildrenOfType(structDefinitionNode, FieldDefinitionNode.class);
                for (FieldDefinitionNode fieldDefinitionNode : fieldDefinitionNodes) {
                    IdentifierPSINode fieldName = PsiTreeUtil.getChildOfType(fieldDefinitionNode,
                            IdentifierPSINode.class);
                    if (fieldName == null) {
                        continue;
                    }
                    TypeNameNode fieldType = PsiTreeUtil.getChildOfType(fieldDefinitionNode,
                            TypeNameNode.class);
                    if (fieldType == null) {
                        continue;
                    }
                    LookupElement lookupElement =
                            BallerinaCompletionUtils.createFieldLookupElement(fieldName, fieldType,
                                    (IdentifierPSINode) resolvedElement);
                    results.add(lookupElement);
                }
            }
        }

        return results.toArray(new LookupElement[results.size()]);
    }
}
