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

package org.ballerinalang.plugins.idea.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.antlr.jetbrains.adaptor.lexer.RuleIElementType;
import org.antlr.jetbrains.adaptor.psi.ANTLRPsiLeafNode;
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.antlr.jetbrains.adaptor.psi.Trees;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.psi.references.ActionInvocationReference;
import org.ballerinalang.plugins.idea.psi.references.AnnotationAttributeReference;
import org.ballerinalang.plugins.idea.psi.references.AnnotationAttributeValueReference;
import org.ballerinalang.plugins.idea.psi.references.AnnotationReference;
import org.ballerinalang.plugins.idea.psi.references.AttachmentPointReference;
import org.ballerinalang.plugins.idea.psi.references.ConnectorReference;
import org.ballerinalang.plugins.idea.psi.references.EnumFieldReference;
import org.ballerinalang.plugins.idea.psi.references.FieldReference;
import org.ballerinalang.plugins.idea.psi.references.FunctionReference;
import org.ballerinalang.plugins.idea.psi.references.InvocationReference;
import org.ballerinalang.plugins.idea.psi.references.NameReference;
import org.ballerinalang.plugins.idea.psi.references.NameSpaceReference;
import org.ballerinalang.plugins.idea.psi.references.PackageNameReference;
import org.ballerinalang.plugins.idea.psi.references.RecordKeyReference;
import org.ballerinalang.plugins.idea.psi.references.RecordValueReference;
import org.ballerinalang.plugins.idea.psi.references.StatementReference;
import org.ballerinalang.plugins.idea.psi.references.StructReference;
import org.ballerinalang.plugins.idea.psi.references.TransformerReference;
import org.ballerinalang.plugins.idea.psi.references.TypeReference;
import org.ballerinalang.plugins.idea.psi.references.WorkerReference;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_annotationAttribute;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_annotationReference;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_attachmentPoint;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_connectorReference;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_field;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_functionReference;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_invocation;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_nameReference;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_packageName;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_recordKey;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_statement;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_structReference;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_transformerReference;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_workerReference;

public class IdentifierPSINode extends ANTLRPsiLeafNode implements PsiNamedElement, PsiNameIdentifierOwner {

    public IdentifierPSINode(IElementType type, CharSequence text) {
        super(type, text);
    }

    @Override
    public String getName() {
        return getText();
    }

    /**
     * Alter this node to have text specified by the argument. Do this by
     * creating a new node through parsing of an ID and then doing a
     * replace.
     */
    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        if (getParent() == null) {
            return this;
        }
        PsiElement newID = Trees.createLeafFromText(getProject(), BallerinaLanguage.INSTANCE, getContext(), name,
                BallerinaTypes.IDENTIFIER);
        if (newID != null) {
            // use replace on leaves but replaceChild on ID nodes that are part of defs/decls.
            return this.replace(newID);
        }
        return this;
    }

    /**
     * Create and return a PsiReference object associated with this ID
     * node. The reference object will be asked to resolve this ref
     * by using the text of this node to identify the appropriate definition
     * site. The definition site is typically a subtree for a function
     * or variable definition whereas this reference is just to this ID
     * leaf node.
     * <p>
     * As the AST factory has no context and cannot create different kinds
     * of PsiNamedElement nodes according to context, every ID node
     * in the tree will be of this type. So, we distinguish references
     * from definitions or other uses by looking at context in this method
     * as we have parent (context) information.
     */
    @Override
    public PsiReference getReference() {
        PsiElement parent = getParent();
        IElementType elType = parent.getNode().getElementType();

        PsiElement prevVisibleLeaf;

        // do not return a reference for the ID nodes in a definition
        if (elType instanceof RuleIElementType) {
            XmlAttribNode xmlAttribNode = PsiTreeUtil.getParentOfType(parent, XmlAttribNode.class);
            switch (((RuleIElementType) elType).getRuleIndex()) {
                case RULE_packageName:
                    if (xmlAttribNode != null) {
                        return new NameSpaceReference(this);
                    }
                    AliasNode aliasNode = PsiTreeUtil.getParentOfType(parent, AliasNode.class);
                    if (aliasNode != null) {
                        return null;
                    }
                    return new PackageNameReference(this);
                case RULE_nameReference:
                    // If we are currently resolving a var variable, we don't need to resolve it since it is the
                    // definition.
                    PsiElement element = getPsi();
                    if (element instanceof IdentifierPSINode) {
                        AssignmentStatementNode assignmentStatementNode = PsiTreeUtil.getParentOfType(element,
                                AssignmentStatementNode.class);
                        if (assignmentStatementNode != null
                                && BallerinaPsiImplUtil.isVarAssignmentStatement(assignmentStatementNode)) {
                            IdentifierPSINode identifier = (IdentifierPSINode) element;
                            if (BallerinaPsiImplUtil.isRedeclaredVar(identifier)) {
                                return new NameReference(this);
                            }
                            if (BallerinaPsiImplUtil.isValidVarVariable(assignmentStatementNode, identifier)) {
                                return null;
                            }
                        }
                    }

                    if (xmlAttribNode != null) {
                        return new NameSpaceReference(this);
                    }
                    RecordKeyNode recordKeyNode = PsiTreeUtil.getParentOfType(parent, RecordKeyNode.class);
                    if (recordKeyNode != null) {
                        return new RecordKeyReference(this);
                    }
                    RecordValueNode recordValueNode = PsiTreeUtil.getParentOfType(parent,
                            RecordValueNode.class);
                    if (recordValueNode != null) {
                        return new RecordValueReference(this);
                    }

                    prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(parent);
                    if (prevVisibleLeaf != null) {
                        if (".".equals(prevVisibleLeaf.getText())) {
                            // Todo - update logic
                            PsiReference reference = checkAndSuggestReferenceAfterDot();
                            if (reference != null) {
                                return reference;
                            }
                            return new FieldReference(this);
                        }
                        // Todo - remove ","
                        if ((prevVisibleLeaf instanceof IdentifierPSINode)
                                && prevVisibleLeaf.getParent() instanceof AnnotationDefinitionNode) {
                            return null;
                        } else if (("attach".equals(prevVisibleLeaf.getText()) || ",".equals(prevVisibleLeaf.getText()))
                                && prevVisibleLeaf.getParent() instanceof AnnotationDefinitionNode) {
                            return new AttachmentPointReference(this);
                        }
                    }

                    return new NameReference(this);
                case RULE_field:
                    prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(parent);
                    PsiReference variableReference = null;
                    if (prevVisibleLeaf != null && ".".equals(prevVisibleLeaf.getText())) {
                        PsiElement connectorVariable = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
                        if (connectorVariable != null) {
                            variableReference = connectorVariable.findReferenceAt(connectorVariable.getTextLength());
                        }
                    } else {
                        PsiElement prevSibling = parent.getPrevSibling();
                        if (prevSibling != null) {
                            PsiReference reference = checkAndSuggestReferenceAfterDot();
                            if (reference != null) {
                                return reference;
                            }
                            return new FieldReference(this);
                        }
                    }
                    if (variableReference == null) {
                        return null;
                    }
                    PsiElement variableDefinition = variableReference.resolve();
                    if (variableDefinition == null) {
                        return new FieldReference(this);
                    }
                    ConnectorDefinitionNode connectorDefinitionNode =
                            BallerinaPsiImplUtil.resolveConnectorFromVariableDefinitionNode(variableDefinition
                                    .getParent());
                    if (connectorDefinitionNode != null) {
                        return new ActionInvocationReference(this);
                    }
                    return new FieldReference(this);
                case RULE_functionReference:
                    return new FunctionReference(this);
                case RULE_annotationReference:
                    return new AnnotationReference(this);
                case RULE_connectorReference:
                    return new ConnectorReference(this);
                case RULE_workerReference:
                    return new WorkerReference(this);
                case RULE_annotationAttribute:
                    return new AnnotationAttributeReference(this);
                case RULE_attachmentPoint:
                    return new AttachmentPointReference(this);
                case RULE_statement:
                    // Todo - update logic
                    PsiReference reference = checkAndSuggestReferenceAfterDot();
                    if (reference != null) {
                        return reference;
                    }
                    prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(getParent());
                    if (prevVisibleLeaf != null) {
                        if (".".equals(prevVisibleLeaf.getText())) {
                            PsiElement psiElement = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
                            if (psiElement == null) {
                                return new FieldReference(this);
                            }
                            reference = psiElement.findReferenceAt(psiElement.getTextLength());
                            if (reference == null) {
                                return new FieldReference(this);
                            }
                            PsiElement resolvedElement = reference.resolve();
                            if (resolvedElement == null) {
                                return new FieldReference(this);
                            }
                            PsiElement resolvedElementParent = resolvedElement.getParent();
                            if (resolvedElementParent instanceof ConnectorDefinitionNode) {
                                return new ActionInvocationReference(this);
                            }
                            if (resolvedElementParent instanceof VariableDefinitionNode) {
                                connectorDefinitionNode = BallerinaPsiImplUtil
                                        .resolveConnectorFromVariableDefinitionNode((resolvedElementParent));
                                if (connectorDefinitionNode != null) {
                                    return new ActionInvocationReference(this);
                                }
                            }
                            return new FieldReference(this);
                        } else if (prevVisibleLeaf.getText().matches("[,]")) {
                            return new NameReference(this);
                        } else if (prevVisibleLeaf.getText().matches("[{]")) {
                            return new RecordKeyReference(this);
                        }
                    }
                    PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(getPsi());
                    if (nextVisibleLeaf != null && ":".equals(nextVisibleLeaf.getText())) {
                        return new PackageNameReference(this);
                    }
                    return new StatementReference(this);
                case RULE_invocation:
                    return suggestReferenceTypeForInvocation();
                case RULE_structReference:
                    return new StructReference(this);
                case RULE_transformerReference:
                    return new TransformerReference(this);
                case RULE_recordKey:
                    return new RecordKeyReference(this);
                default:
                    return null;
            }
        }
        if (parent instanceof PsiErrorElement) {
            return suggestReferenceType(parent);
        }
        return null;
    }

    @Nullable
    private PsiReference suggestReferenceTypeForInvocation() {
        PsiElement parent = getParent();
        PsiElement prevSibling = parent.getPrevSibling();
        if (prevSibling == null) {
            return null;
        }
        PsiReference reference = null;
        if (prevSibling instanceof VariableReferenceNode) {
            InvocationNode invocationNode = PsiTreeUtil.getChildOfType(prevSibling, InvocationNode.class);
            if (invocationNode != null) {
                IdentifierPSINode identifier = PsiTreeUtil.getChildOfType(invocationNode, IdentifierPSINode.class);
                if (identifier == null) {
                    return null;
                }
                reference = identifier.findReferenceAt(identifier.getTextLength());
            }
            FieldNode fieldNode = PsiTreeUtil.getChildOfType(prevSibling, FieldNode.class);
            if (fieldNode != null) {
                IdentifierPSINode identifier = PsiTreeUtil.getChildOfType(fieldNode, IdentifierPSINode.class);
                if (identifier == null) {
                    return null;
                }
                reference = identifier.findReferenceAt(identifier.getTextLength());
            }
            FunctionInvocationNode functionInvocationNode = PsiTreeUtil.getChildOfType(prevSibling,
                    FunctionInvocationNode.class);
            if (functionInvocationNode != null) {
                FunctionReferenceNode functionReferenceNode = PsiTreeUtil.getChildOfType(functionInvocationNode,
                        FunctionReferenceNode.class);
                if (functionReferenceNode != null) {
                    return new NameReference(this);
                }
            }
            if (reference == null) {
                reference = prevSibling.findReferenceAt(prevSibling.getTextLength());
            }
        } else {
            reference = prevSibling.findReferenceAt(prevSibling.getTextLength());
        }
        if (reference == null) {
            return null;
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return null;
        }
        PsiElement definitionNode = resolvedElement.getParent();
        if (definitionNode instanceof VariableDefinitionNode) {
            ConnectorDefinitionNode connectorDefinitionNode =
                    BallerinaPsiImplUtil.resolveConnectorFromVariableDefinitionNode((definitionNode));
            if (connectorDefinitionNode != null) {
                return new ActionInvocationReference(this);
            }
        } else if (definitionNode instanceof EndpointDeclarationNode) {
            ConnectorReferenceNode connectorReferenceNode = PsiTreeUtil.getChildOfType(definitionNode,
                    ConnectorReferenceNode.class);
            if (connectorReferenceNode != null) {
                return new ActionInvocationReference(this);
            }
        } else if (definitionNode instanceof FunctionDefinitionNode) {
            return new NameReference(this);
        }
        reference = checkAndSuggestReferenceAfterDot();
        if (reference != null) {
            return reference;
        }

        return new InvocationReference(this);
    }

    @Nullable
    private PsiReference checkAndSuggestReferenceAfterDot() {
        PsiReference reference = null;
        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(getParent());
        if (prevVisibleLeaf != null) {
            if (".".equals(prevVisibleLeaf.getText())) {
                PsiElement prevSibling = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
                if (prevSibling != null) {
                    reference = prevSibling.findReferenceAt(prevSibling.getTextLength());
                }
            } else {
                reference = prevVisibleLeaf.findReferenceAt(prevVisibleLeaf.getTextLength());
            }
        }
        if (reference == null) {
            PsiElement prevSibling = getParent().getPrevSibling();
            if (prevSibling == null) {
                return null;
            }
            InvocationNode invocationNode = PsiTreeUtil.getChildOfType(prevSibling, InvocationNode.class);
            if (invocationNode == null) {
                return null;
            }
            IdentifierPSINode identifier = PsiTreeUtil.getChildOfType(invocationNode, IdentifierPSINode.class);
            if (identifier == null) {
                return null;
            }
            reference = identifier.findReferenceAt(identifier.getTextLength());
        }
        if (reference == null) {
            return null;
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return null;
        }
        // Get the return type of the function definition.
        PsiElement parent = resolvedElement.getParent();
        if (parent instanceof FunctionDefinitionNode) {
            List<TypeNameNode> returnTypes = BallerinaPsiImplUtil.getReturnTypes(((FunctionDefinitionNode) parent));
            if (returnTypes.size() == 1) {
                return new TypeReference(this, returnTypes.get(0));
            }
        }

        if (resolvedElement.getParent() instanceof EndpointDeclarationNode) {
            return new ActionInvocationReference(this);
        }
        if (resolvedElement.getParent() instanceof EnumDefinitionNode) {
            return new EnumFieldReference(this);
        }
        PsiElement type = BallerinaPsiImplUtil.getType(((IdentifierPSINode) resolvedElement));
        if (type == null || (!(type instanceof BuiltInReferenceTypeNameNode) && !(type instanceof ReferenceTypeNameNode)
                && !(type instanceof ValueTypeNameNode))) {
            return null;
        }
        return new TypeReference(this, type);
    }

    @Nullable
    private PsiReference suggestReferenceType(@NotNull PsiElement parent) {
        PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(getParent());
        if (nextVisibleLeaf != null) {
            if (":".equals(nextVisibleLeaf.getText())) {
                return new PackageNameReference(this);
            } else if (".".equals(nextVisibleLeaf.getText())) {
                return new NameReference(this);
            }
        }

        if (parent.getParent() instanceof AttachmentPointNode) {
            return new AttachmentPointReference(this);
        }

        if (parent.getParent() instanceof AnnotationAttachmentNode) {
            return new AnnotationAttributeValueReference(this);
        }

        return new NameReference(this);
    }

    @Override
    public ItemPresentation getPresentation() {
        PsiElement parent = getParent();
        if (parent instanceof IdentifierDefSubtree) {
            return ((IdentifierDefSubtree) parent).getPresentation();
        }
        return super.getPresentation();
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        // If the parent is a ParameterNode, we return this identifier object. Otherwise when we get parents, it
        // might return excluded nodes below and can cause issues.
        if (getParent() instanceof ParameterNode || getParent() instanceof VariableDefinitionNode) {
            return this;
        }

        // We should return the name identifier node (which is "this" object) for every identifier which can be used
        // to find usage. But for some nodes like ServiceDefinitionNode, etc, we don't need to find
        // usages because they will not be used in other places.
        PsiElement parent = PsiTreeUtil.getParentOfType(this, ResourceDefinitionNode.class);
        if (parent != null) {
            return null;
        }
        parent = PsiTreeUtil.getParentOfType(this, ServiceDefinitionNode.class);
        if (parent != null) {
            return null;
        }
        return this;
    }
}
