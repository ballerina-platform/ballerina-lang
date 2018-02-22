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
import org.ballerinalang.plugins.idea.psi.AssignmentStatementNode;
import org.ballerinalang.plugins.idea.psi.CodeBlockParameterNode;
import org.ballerinalang.plugins.idea.psi.EnumDefinitionNode;
import org.ballerinalang.plugins.idea.psi.EnumFieldNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a field reference.
 */
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
        // If the current statement is not completed properly, the parent will be a StatementNode. This is used to
        // resolve multi level structs. Eg: user.name.<caret>
        if (parent instanceof StatementNode) {
            // Get the previous element.
            PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(parent);
            if (prevVisibleLeaf == null) {
                return null;
            }
            // Previous leaf element should be "." if the references are correctly defined.
            if (!".".equals(prevVisibleLeaf.getText())) {
                return null;
            }
            // Get the prevSibling. This is used to resolve the current field.
            prevSibling = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
            if (prevSibling == null) {
                return null;
            }
        } else if (parent instanceof NameReferenceNode) {
            prevSibling = PsiTreeUtil.prevVisibleLeaf(parent);
            if (prevSibling != null && ".".equals(prevSibling.getText())) {
                prevSibling = PsiTreeUtil.prevVisibleLeaf(prevSibling);
            }
        } else {
            PsiElement parentPrevSibling = parent.getPrevSibling();
            if (parentPrevSibling != null) {
                if (parentPrevSibling instanceof VariableReferenceNode) {
                    PsiElement[] children = parentPrevSibling.getChildren();
                    if (children.length <= 0) {
                        return null;
                    }
                    PsiElement firstChild = children[0].getFirstChild();
                    if (firstChild == null) {
                        return null;
                    }
                    prevSibling = firstChild;
                } else {
                    return null;
                }
            } else {
                // If the current statement is correctly resolved, that means all the fields are identified properly.
                // Get the prevSibling. This is used to resolve the current field.
                prevSibling = PsiTreeUtil.prevVisibleLeaf(parent);
            }
        }
        // If the prevSibling is null, we return from this method because we cannot resolve the element.
        if (prevSibling == null) {
            return null;
        }

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
        // Eg: user.name.firstName - If the current node is 'name', resolved element will be a struct definition. if
        // the current element is 'firstName', then the resolved element will be a field definition.
        PsiElement resolvedElement = variableReference.resolve();
        if (resolvedElement == null || !(resolvedElement instanceof IdentifierPSINode)) {
            return null;
        }

        // Get the parent of the resolved element.
        PsiElement resolvedElementParent = resolvedElement.getParent();
        StructDefinitionNode structDefinitionNode = null;
        // Resolve the corresponding resolvedElementParent to get the struct definition.
        if (resolvedElementParent instanceof VariableDefinitionNode
                || resolvedElementParent instanceof CodeBlockParameterNode
                || resolvedElementParent instanceof ParameterNode) {
            // Resolve the Type of the VariableDefinitionNode to get the corresponding struct.
            // Eg: User user = {}
            //     In here, "User" is resolved and struct identifier is returned.
            structDefinitionNode = BallerinaPsiImplUtil.resolveStructFromDefinitionNode(resolvedElementParent);
        } else if (resolvedElementParent instanceof FieldDefinitionNode) {
            // If the resolvedElementParent is of type FieldDefinitionNode, that means we need to resolve the type of
            // the field to get the struct definition.
            // Eg: user.name.firstName - In here, if we want to resolve the 'firstName' we will get the 'Name name;'
            // field. So we need to resolve the type of the field which is 'Name'. Then we will get the Name struct.
            // Then we need to get the 'firstName' field from that.
            structDefinitionNode =
                    BallerinaPsiImplUtil.resolveTypeNodeStruct((resolvedElementParent));
        } else if (resolvedElementParent instanceof NameReferenceNode) {
            structDefinitionNode = BallerinaPsiImplUtil.findStructDefinition((IdentifierPSINode) resolvedElement);
        } else if (resolvedElementParent instanceof EnumDefinitionNode) {
            return ((EnumDefinitionNode) resolvedElementParent).resolve(identifier);
        }
        if (structDefinitionNode == null) {
            return null;
        }
        // Resolve the field and return the resolved element.
        return structDefinitionNode.resolve(identifier);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        IdentifierPSINode identifier = getElement();
        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(identifier);
        // Todo - remove hard coded "."
        if (prevVisibleLeaf == null || !".".equals(prevVisibleLeaf.getText())) {
            return new LookupElement[0];
        }
        PsiElement previousField = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
        if (previousField == null) {
            return new LookupElement[0];
        }
        PsiReference reference = previousField.findReferenceAt(0);
        if (reference == null) {
            PsiElement prevSibling = identifier.getParent().getPrevSibling();
            if (prevSibling == null) {
                return new LookupElement[0];
            }
            if (prevSibling instanceof VariableReferenceNode) {
                PsiElement[] children = prevSibling.getChildren();
                if (children.length <= 0) {
                    return new LookupElement[0];
                }
                PsiElement firstChild = children[0].getFirstChild();
                if (firstChild == null) {
                    return new LookupElement[0];
                }
                PsiReference functionReference = firstChild.findReferenceAt(firstChild.getTextLength());
                if (functionReference == null) {
                    return new LookupElement[0];
                }
                PsiElement resolvedElement = functionReference.resolve();
                if (resolvedElement == null) {
                    return new LookupElement[0];
                }
                PsiElement parent = resolvedElement.getParent();
                if (parent instanceof FunctionDefinitionNode) {
                    List<TypeNameNode> returnTypes =
                            BallerinaPsiImplUtil.getReturnTypes(((FunctionDefinitionNode) parent));
                    if (returnTypes.size() == 1) {
                        TypeNameNode typeNameNode = returnTypes.get(0);
                        List<LookupElement> functions =
                                Arrays.asList((LookupElement[]) TypeReference.getVariants(typeNameNode));
                        return functions.toArray(new LookupElement[functions.size()]);
                    }
                }
            }
        }
        if (reference == null) {
            return new LookupElement[0];
        }

        // Todo - use util method
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null || !(resolvedElement instanceof IdentifierPSINode)) {
            return new LookupElement[0];
        }
        PsiElement resolvedElementParent = resolvedElement.getParent();
        StructDefinitionNode structDefinitionNode = null;
        List<LookupElement> results = new LinkedList<>();
        // Resolve the corresponding resolvedElementParent to get the struct definition.
        if (resolvedElementParent instanceof VariableDefinitionNode
                || resolvedElementParent instanceof CodeBlockParameterNode
                || resolvedElementParent instanceof ParameterNode) {
            structDefinitionNode = BallerinaPsiImplUtil.resolveStructFromDefinitionNode(resolvedElementParent);
        } else if (resolvedElementParent instanceof FieldDefinitionNode) {
            structDefinitionNode =
                    BallerinaPsiImplUtil.resolveTypeNodeStruct((resolvedElementParent));
        } else if (resolvedElementParent instanceof NameReferenceNode) {
            AssignmentStatementNode assignmentStatementNode = PsiTreeUtil.getParentOfType(resolvedElement,
                    AssignmentStatementNode.class);
            if (assignmentStatementNode != null) {
                structDefinitionNode = BallerinaPsiImplUtil.getStructDefinition(assignmentStatementNode,
                        ((IdentifierPSINode) resolvedElement));
            } else {
                structDefinitionNode = BallerinaPsiImplUtil.findStructDefinition((IdentifierPSINode) resolvedElement);
            }
            if (structDefinitionNode != null) {
                IdentifierPSINode structName = PsiTreeUtil.findChildOfType(structDefinitionNode,
                        IdentifierPSINode.class);
                if (structName != null) {
                    resolvedElement = structName;
                }
            }
        } else if (resolvedElementParent instanceof EnumDefinitionNode) {
            Collection<EnumFieldNode> fieldDefinitionNodes =
                    PsiTreeUtil.findChildrenOfType(resolvedElementParent, EnumFieldNode.class);
            results.addAll(BallerinaCompletionUtils.createEnumFieldLookupElements(fieldDefinitionNodes,
                    (IdentifierPSINode) resolvedElement));
            return results.toArray(new LookupElement[results.size()]);
        } else if (resolvedElementParent instanceof StructDefinitionNode) {
            structDefinitionNode = ((StructDefinitionNode) resolvedElementParent);
        }
        if (structDefinitionNode == null) {
            return new LookupElement[0];
        }
        Collection<FieldDefinitionNode> fieldDefinitionNodes =
                PsiTreeUtil.findChildrenOfType(structDefinitionNode, FieldDefinitionNode.class);
        results.addAll(BallerinaCompletionUtils.createFieldLookupElements(fieldDefinitionNodes,
                (IdentifierPSINode) resolvedElement, null));

        List<IdentifierPSINode> attachedFunctions =
                BallerinaPsiImplUtil.getAttachedFunctions(structDefinitionNode);
        results.addAll(BallerinaCompletionUtils.createAttachedFunctionsLookupElements(attachedFunctions));

        return results.toArray(new LookupElement[results.size()]);
    }
}
