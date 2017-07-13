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
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.psi.AssignmentStatementNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceListNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StructKeyReference extends BallerinaElementReference {

    public StructKeyReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();

        VariableDefinitionNode variableDefinitionNode = PsiTreeUtil.getParentOfType(identifier,
                VariableDefinitionNode.class);
        if (variableDefinitionNode == null) {
            // Todo - resolve variable to definition
            AssignmentStatementNode assignmentStatementNode = PsiTreeUtil.getParentOfType(identifier,
                    AssignmentStatementNode.class);
            if (assignmentStatementNode == null) {

            } else {
                VariableReferenceListNode variableReferenceListNode =
                        PsiTreeUtil.getChildOfType(assignmentStatementNode, VariableReferenceListNode.class);
                if (variableReferenceListNode == null) {
                    return null;
                }
                PsiElement variableReferenceNode = variableReferenceListNode.getFirstChild();
                if (variableReferenceNode == null) {
                    return null;
                }
                PsiReference reference = variableReferenceNode.findReferenceAt(0);
                if (reference == null) {
                    return null;
                }
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement == null) {
                    return null;
                }
                PsiElement parent = resolvedElement.getParent();
                if (!(parent instanceof VariableDefinitionNode)) {
                    return null;
                }
                return resolveDefinition(((VariableDefinitionNode) parent));
            }
        } else {
            return resolveDefinition(variableDefinitionNode);
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();

        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            results.addAll(getVariantsFromCurrentPackage());
        } else {
            results.addAll(getVariantsFromPackage(packageNameNode));
        }
        return results.toArray(new LookupElement[results.size()]);
    }

    @NotNull
    private List<LookupElement> getVariantsFromCurrentPackage() {
        List<LookupElement> results = new LinkedList<>();

        IdentifierPSINode identifier = getElement();

        VariableDefinitionNode variableDefinitionNode = PsiTreeUtil.getParentOfType(identifier,
                VariableDefinitionNode.class);
        if (variableDefinitionNode == null) {
            // Todo - resolve variable to definition
        } else {
            TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(variableDefinitionNode, TypeNameNode.class);
            if (typeNameNode == null) {
                return results;
            } else {
                PsiReference reference = typeNameNode.findReferenceAt(typeNameNode.getTextLength());
                if (reference == null) {
                    return results;
                }
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement == null) {
                    return results;
                }
                PsiElement resolvedElementParent = resolvedElement.getParent();
                if (resolvedElementParent instanceof StructDefinitionNode) {
                    Collection<FieldDefinitionNode> fieldDefinitionNodes =
                            PsiTreeUtil.findChildrenOfType(resolvedElementParent, FieldDefinitionNode.class);
                    results = BallerinaCompletionUtils.createFieldLookupElements(fieldDefinitionNodes,
                            (IdentifierPSINode) resolvedElement,
                            PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
                }
            }
        }
        return results;
    }

    @NotNull
    private List<LookupElement> getVariantsFromPackage(@NotNull PackageNameNode packageNameNode) {
        return new LinkedList<>();
    }

    @Nullable
    private PsiElement resolveDefinition(@NotNull VariableDefinitionNode variableDefinitionNode) {
        IdentifierPSINode identifier = getElement();
        TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(variableDefinitionNode, TypeNameNode.class);
        if (typeNameNode == null) {
            return null;
        } else {
            PsiReference reference = typeNameNode.findReferenceAt(typeNameNode.getTextLength());
            if (reference == null) {
                return null;
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return null;
            }
            PsiElement resolvedElementParent = resolvedElement.getParent();
            if (resolvedElementParent instanceof StructDefinitionNode) {
                // Todo - use an util method
                Collection<FieldDefinitionNode> fieldDefinitionNodes =
                        PsiTreeUtil.findChildrenOfType(resolvedElementParent, FieldDefinitionNode.class);
                for (FieldDefinitionNode fieldDefinitionNode : fieldDefinitionNodes) {
                    IdentifierPSINode fieldName = PsiTreeUtil.getChildOfType(fieldDefinitionNode,
                            IdentifierPSINode.class);
                    if (fieldName != null && identifier.getText().equals(fieldName.getText())) {
                        return fieldName;
                    }
                }
            }
        }
        return null;
    }
}
