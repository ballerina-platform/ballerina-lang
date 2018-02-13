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
import org.ballerinalang.plugins.idea.psi.AnonStructTypeNameNode;
import org.ballerinalang.plugins.idea.psi.AssignmentStatementNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.InvocationNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.StructBodyNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceListNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a record key reference.
 */
public class RecordKeyReference extends BallerinaElementReference {

    public RecordKeyReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        VariableDefinitionNode variableDefinitionNode = PsiTreeUtil.getParentOfType(identifier,
                VariableDefinitionNode.class);
        if (variableDefinitionNode != null) {
            return resolve(variableDefinitionNode);
        }

        GlobalVariableDefinitionNode globalVariableDefinitionNode = PsiTreeUtil.getParentOfType(identifier,
                GlobalVariableDefinitionNode.class);
        if (globalVariableDefinitionNode != null) {
            return resolve(globalVariableDefinitionNode);
        }

        AssignmentStatementNode assignmentStatementNode = PsiTreeUtil.getParentOfType(identifier,
                AssignmentStatementNode.class);
        if (assignmentStatementNode != null) {
            PsiElement resolvedElement = resolve(assignmentStatementNode);
            if (resolvedElement != null) {
                return resolvedElement;
            }
        }

        // Try to resolve to fields in anonymous struct.
        PsiElement definitionNode = BallerinaPsiImplUtil.resolveAnonymousStruct(identifier);
        if (definitionNode == null) {
            return null;
        }
        if (definitionNode instanceof AnonStructTypeNameNode) {
            StructBodyNode structBodyNode = PsiTreeUtil.findChildOfType(definitionNode, StructBodyNode.class);
            if (structBodyNode == null) {
                return null;
            }
            List<FieldDefinitionNode> fieldDefinitionNodes = PsiTreeUtil.getChildrenOfTypeAsList(structBodyNode,
                    FieldDefinitionNode.class);
            for (FieldDefinitionNode fieldDefinitionNode : fieldDefinitionNodes) {
                if (fieldDefinitionNode == null) {
                    continue;
                }
                IdentifierPSINode fieldName = PsiTreeUtil.getChildOfType(fieldDefinitionNode, IdentifierPSINode.class);
                if (fieldName == null) {
                    continue;
                }
                if (fieldName.getText().equals(identifier.getText())) {
                    return fieldName;
                }
            }
        }
        if (!(definitionNode instanceof StructDefinitionNode)) {
            return null;
        }
        StructDefinitionNode structDefinitionNode = ((StructDefinitionNode) definitionNode);
        IdentifierPSINode structNameNode = PsiTreeUtil.getChildOfType(structDefinitionNode,
                IdentifierPSINode.class);
        if (structNameNode == null) {
            return null;
        }
        Collection<FieldDefinitionNode> fieldDefinitionNodes =
                PsiTreeUtil.findChildrenOfType(structDefinitionNode, FieldDefinitionNode.class);
        for (FieldDefinitionNode fieldDefinitionNode : fieldDefinitionNodes) {
            IdentifierPSINode fieldName = PsiTreeUtil.getChildOfType(fieldDefinitionNode,
                    IdentifierPSINode.class);
            if (fieldName != null && identifier.getText().equals(fieldName.getText())) {
                return fieldName;
            }
        }
        return null;
    }

    @Nullable
    private PsiElement resolve(@NotNull PsiElement definitionNode) {
        IdentifierPSINode identifier = getElement();
        TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(definitionNode, TypeNameNode.class);
        if (typeNameNode == null) {
            return null;
        } else {
            // Todo - Add getType util method
            PsiReference reference = typeNameNode.findReferenceAt(typeNameNode.getTextLength());
            if (reference == null) {
                TypeNameNode actualType = PsiTreeUtil.getChildOfType(typeNameNode, TypeNameNode.class);
                if (actualType == null) {
                    return null;
                }
                reference = actualType.findReferenceAt(actualType.getTextLength());
                if (reference == null) {
                    return null;
                }
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

    private PsiElement resolve(@NotNull AssignmentStatementNode assignmentStatementNode) {
        IdentifierPSINode identifier = getElement();
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
        if (parent instanceof VariableDefinitionNode || parent instanceof ParameterNode) {
            return resolve(parent);
        } else if (parent instanceof NameReferenceNode) {
            StructDefinitionNode structDefinitionNode = resolveStructDefinition(identifier);
            if (structDefinitionNode == null) {
                return null;
            }
            Collection<FieldDefinitionNode> fieldDefinitionNodes =
                    PsiTreeUtil.findChildrenOfType(structDefinitionNode, FieldDefinitionNode.class);
            for (FieldDefinitionNode fieldDefinitionNode : fieldDefinitionNodes) {
                IdentifierPSINode fieldName = PsiTreeUtil.getChildOfType(fieldDefinitionNode,
                        IdentifierPSINode.class);
                if (fieldName != null && identifier.getText().equals(fieldName.getText())) {
                    return fieldName;
                }
            }
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
        InvocationNode invocationNode = PsiTreeUtil.getParentOfType(identifier, InvocationNode.class);
        if (variableDefinitionNode == null || invocationNode != null) {
            StructDefinitionNode structDefinitionNode = resolveStructDefinition(identifier);
            if (structDefinitionNode == null) {
                // Todo - Check for enclosing {} since the parse errors might cause issues when identifying
                // RecordLiteralNode element

                //                RecordLiteralNode mapStructLiteralNode = PsiTreeUtil.getParentOfType(identifier,
                //                        RecordLiteralNode.class);
                //                if (mapStructLiteralNode == null) {
                //                    return results;
                //                }

                // Try to get fields from an anonymous struct.
                PsiElement definitionNode = BallerinaPsiImplUtil.resolveAnonymousStruct(identifier);
                if (definitionNode == null) {
                    return results;
                }

                if (definitionNode instanceof AnonStructTypeNameNode) {
                    StructBodyNode structBodyNode = PsiTreeUtil.findChildOfType(definitionNode, StructBodyNode.class);
                    if (structBodyNode == null) {
                        return null;
                    }
                    List<FieldDefinitionNode> fieldDefinitionNodes = PsiTreeUtil.getChildrenOfTypeAsList(structBodyNode,
                            FieldDefinitionNode.class);
                    results = BallerinaCompletionUtils.createFieldLookupElements(fieldDefinitionNodes,
                            PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
                    return results;
                }

                structDefinitionNode = ((StructDefinitionNode) definitionNode);
                IdentifierPSINode structNameNode = PsiTreeUtil.getChildOfType(structDefinitionNode,
                        IdentifierPSINode.class);
                if (structNameNode == null) {
                    return results;
                }
                Collection<FieldDefinitionNode> fieldDefinitionNodes =
                        PsiTreeUtil.findChildrenOfType(structDefinitionNode, FieldDefinitionNode.class);
                results = BallerinaCompletionUtils.createFieldLookupElements(fieldDefinitionNodes,
                        structNameNode, PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
                return results;
            }
            IdentifierPSINode structNameNode = PsiTreeUtil.getChildOfType(structDefinitionNode,
                    IdentifierPSINode.class);
            if (structNameNode == null) {
                return results;
            }
            Collection<FieldDefinitionNode> fieldDefinitionNodes =
                    PsiTreeUtil.findChildrenOfType(structDefinitionNode, FieldDefinitionNode.class);
            results = BallerinaCompletionUtils.createFieldLookupElements(fieldDefinitionNodes,
                    structNameNode, PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
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
    private StructDefinitionNode resolveStructDefinition(@NotNull IdentifierPSINode identifier) {
        AssignmentStatementNode assignmentStatementNode = PsiTreeUtil.getParentOfType(identifier,
                AssignmentStatementNode.class);
        if (assignmentStatementNode != null &&
                !BallerinaPsiImplUtil.isVarAssignmentStatement(assignmentStatementNode)) {
            VariableReferenceListNode variableReferenceListNode = PsiTreeUtil.getChildOfType
                    (assignmentStatementNode,
                            VariableReferenceListNode.class);
            if (variableReferenceListNode != null) {
                VariableReferenceNode variableReferenceNode = PsiTreeUtil.getChildOfType(variableReferenceListNode,
                        VariableReferenceNode.class);

                if (variableReferenceNode != null) {

                    PsiReference reference = variableReferenceNode.findReferenceAt(variableReferenceNode
                            .getTextLength());
                    if (reference != null) {
                        PsiElement resolvedElement = reference.resolve();
                        if (resolvedElement != null && resolvedElement instanceof IdentifierPSINode) {
                            return BallerinaPsiImplUtil.findStructDefinition((IdentifierPSINode) resolvedElement);
                        }
                    }
                }
            }
        }
        return null;
    }
}
