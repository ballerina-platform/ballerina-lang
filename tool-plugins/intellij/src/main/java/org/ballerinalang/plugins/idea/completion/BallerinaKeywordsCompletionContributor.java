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

package org.ballerinalang.plugins.idea.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.ANTLRPsiNode;
import org.ballerinalang.plugins.idea.BallerinaParserDefinition;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.CallableUnitBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.DefinitionNode;
import org.ballerinalang.plugins.idea.psi.ExpressionNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.RecordKeyValueNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.references.NameReference;
import org.ballerinalang.plugins.idea.psi.references.WorkerReference;
import org.jetbrains.annotations.NotNull;

import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.addOtherTypeAsLookup;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.addReferenceTypesAsLookups;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.addTypeNamesAsLookups;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.addValueTypesAsLookups;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.addXmlnsAsLookup;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.getAttachKeyword;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.getCommonKeywords;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.getConnectorSpecificKeywords;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.getCreateKeyword;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.getFileLevelKeywordsAsLookups;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.getFunctionSpecificKeywords;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.getLengthOfKeyword;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.getResourceSpecificKeywords;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.getServiceSpecificKeywords;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.getTypeOfKeyword;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.getValueKeywords;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.getWorkerInteractionKeywords;

/**
 * Provides keyword completion support.
 */
public class BallerinaKeywordsCompletionContributor extends CompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();

        if (element instanceof LeafPsiElement) {
            IElementType elementType = ((LeafPsiElement) element).getElementType();
            if (elementType == BallerinaTypes.FLOATING_POINT) {
                return;
            }
        }

        if (parent instanceof NameReferenceNode) {
            PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(element);
            if (prevVisibleLeaf != null && "public".equals(prevVisibleLeaf.getText())) {
                result.addAllElements(getFileLevelKeywordsAsLookups(false, true, true));
            }
            if (prevVisibleLeaf instanceof IdentifierPSINode) {
                result.addElement(getAttachKeyword());
                return;
            }

            ANTLRPsiNode definitionParent = PsiTreeUtil.getParentOfType(parent, CallableUnitBodyNode.class,
                    ServiceBodyNode.class, ConnectorBodyNode.class);
            if (definitionParent != null && prevVisibleLeaf != null && "=".equals(prevVisibleLeaf.getText())) {
                result.addElement(getCreateKeyword());
                result.addElement(getTypeOfKeyword());
                result.addElement(getLengthOfKeyword());
                result.addAllElements(getValueKeywords());
            }

            ExpressionNode expressionNode = PsiTreeUtil.getParentOfType(parent, ExpressionNode.class);
            if (expressionNode != null && expressionNode.getChildren().length == 1) {
                PsiReference referenceAt = parent.findReferenceAt(0);
                if (referenceAt == null || referenceAt instanceof NameReference) {
                    result.addAllElements(getValueKeywords());
                }
                PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(element);
                if ((prevVisibleLeaf != null && "(".equals(prevVisibleLeaf.getText())) ||
                        (nextVisibleLeaf != null && ")".equals(nextVisibleLeaf.getText())
                                && !":".equals(prevVisibleLeaf.getText()))) {
                    addOtherTypeAsLookup(result);
                    addValueTypesAsLookups(result);
                    addReferenceTypesAsLookups(result);
                }
            }

            AnnotationAttachmentNode attachmentNode = PsiTreeUtil.getParentOfType(parent,
                    AnnotationAttachmentNode.class);
            if (attachmentNode != null) {
                result.addAllElements(getValueKeywords());
            }

            TypeNameNode typeNameNode = PsiTreeUtil.getParentOfType(parent, TypeNameNode.class);
            if (typeNameNode != null && prevVisibleLeaf != null && !prevVisibleLeaf.getText().matches("[:.=]")) {
                AnnotationDefinitionNode annotationDefinitionNode = PsiTreeUtil.getParentOfType(typeNameNode,
                        AnnotationDefinitionNode.class);
                if (annotationDefinitionNode == null) {
                    addOtherTypeAsLookup(result);
                    addXmlnsAsLookup(result);
                    addValueTypesAsLookups(result);
                    addReferenceTypesAsLookups(result);
                }
            }
        }

        if (parent instanceof StatementNode) {
            PsiElement prevVisibleSibling = PsiTreeUtil.prevVisibleLeaf(element);
            if (prevVisibleSibling != null && "=".equals(prevVisibleSibling.getText())) {
                result.addElement(getCreateKeyword());
                result.addElement(getTypeOfKeyword());
                result.addElement(getLengthOfKeyword());
            }
        }

        if (parent instanceof ConstantDefinitionNode || parent instanceof PsiErrorElement) {
            PsiElement prevVisibleSibling = PsiTreeUtil.prevVisibleLeaf(element);
            if (prevVisibleSibling != null && "const".equals(prevVisibleSibling.getText())) {
                addValueTypesAsLookups(result);
                return;
            }
        }

        if (parent instanceof PsiErrorElement) {
            PsiElement prevVisibleSibling = PsiTreeUtil.prevVisibleLeaf(element);
            PsiElement definitionNode = PsiTreeUtil.getParentOfType(element, FunctionDefinitionNode.class,
                    ServiceDefinitionNode.class, ConnectorDefinitionNode.class, ResourceDefinitionNode.class);
            if (definitionNode != null) {

                if (prevVisibleSibling != null && "=".equals(prevVisibleSibling.getText())) {
                    result.addElement(getCreateKeyword());
                    result.addAllElements(getValueKeywords());
                    result.addElement(getTypeOfKeyword());
                    result.addElement(getLengthOfKeyword());
                }

                if (prevVisibleSibling != null && prevVisibleSibling.getText().matches("[;{}]")
                        && !(prevVisibleSibling.getParent() instanceof AnnotationAttachmentNode)) {
                    // Todo - change method
                    addOtherTypeAsLookup(result);
                    addXmlnsAsLookup(result);
                    addValueTypesAsLookups(result);
                    addReferenceTypesAsLookups(result);

                    if (definitionNode instanceof FunctionDefinitionNode) {
                        result.addAllElements(getFunctionSpecificKeywords());
                    }
                    if (definitionNode instanceof ResourceDefinitionNode) {
                        result.addAllElements(getResourceSpecificKeywords());
                    }
                    if (definitionNode instanceof ServiceDefinitionNode) {
                        result.addAllElements(getServiceSpecificKeywords());
                    }
                    if (definitionNode instanceof ConnectorDefinitionNode) {
                        result.addAllElements(getConnectorSpecificKeywords());
                    }
                    if (!(definitionNode instanceof ServiceDefinitionNode
                            || definitionNode instanceof ConnectorDefinitionNode)) {
                        result.addAllElements(getCommonKeywords());
                    }
                }
                if (prevVisibleSibling != null && !prevVisibleSibling.getText().matches("[{}]")
                        /*|| !(prevVisibleSibling.getParent() instanceof AnnotationAttachmentNode)*/) {
                    result.addAllElements(getValueKeywords());
                }
            }

            ConnectorBodyNode connectorBodyNode = PsiTreeUtil.getParentOfType(element, ConnectorBodyNode.class);
            if (connectorBodyNode != null) {
                result.addAllElements(getConnectorSpecificKeywords());
            }

            ConnectorDefinitionNode connectorDefinitionNode = PsiTreeUtil.getParentOfType(element,
                    ConnectorDefinitionNode.class);
            if (connectorDefinitionNode != null) {
                result.addAllElements(getConnectorSpecificKeywords());
            }
            return;
        }

        if (parent instanceof NameReferenceNode) {

            RecordKeyValueNode recordKeyValueNode = PsiTreeUtil.getParentOfType(parent,
                    RecordKeyValueNode.class);

            if (recordKeyValueNode == null) {
                PsiElement prevVisibleSibling = PsiTreeUtil.prevVisibleLeaf(element);
                if (prevVisibleSibling != null && "{".equals(prevVisibleSibling.getText())) {
                    FunctionDefinitionNode functionDefinitionNode = PsiTreeUtil.getParentOfType(element,
                            FunctionDefinitionNode.class);

                    if (functionDefinitionNode != null) {

                        // Todo - change method
                        addOtherTypeAsLookup(result);
                        addXmlnsAsLookup(result);
                        addValueTypesAsLookups(result);
                        addReferenceTypesAsLookups(result);

                        result.addAllElements(getFunctionSpecificKeywords());
                        result.addAllElements(getCommonKeywords());
                        result.addAllElements(getValueKeywords());
                    }

                    ServiceBodyNode serviceBodyNode = PsiTreeUtil.getParentOfType(element, ServiceBodyNode.class);
                    if (serviceBodyNode != null) {
                        result.addAllElements(getServiceSpecificKeywords());
                    }

                    ConnectorBodyNode connectorBodyNode = PsiTreeUtil.getParentOfType(element, ConnectorBodyNode.class);
                    if (connectorBodyNode != null) {
                        result.addAllElements(getConnectorSpecificKeywords());
                    }
                } else if (prevVisibleSibling != null && "}".equals(prevVisibleSibling.getText())) {
                    result.addAllElements(getFileLevelKeywordsAsLookups(true, false, false));
                }
            }
        }

        if (parent instanceof ResourceDefinitionNode) {
            result.addAllElements(getServiceSpecificKeywords());
        }

        if (parent.getPrevSibling() == null) {

            GlobalVariableDefinitionNode globalVariableDefinitionNode = PsiTreeUtil.getParentOfType(element,
                    GlobalVariableDefinitionNode.class);
            if (globalVariableDefinitionNode != null) {
                PsiElement prevVisibleSibling = PsiTreeUtil.prevVisibleLeaf(element);
                if (prevVisibleSibling != null && !(";".equals(prevVisibleSibling.getText()))) {
                    if (!(prevVisibleSibling.getText().matches("[:=]") || prevVisibleSibling instanceof
                            IdentifierPSINode || "create".equals(prevVisibleSibling.getText()))) {
                        if (prevVisibleSibling instanceof LeafPsiElement) {
                            IElementType elementType = ((LeafPsiElement) prevVisibleSibling).getElementType();
                            if (BallerinaParserDefinition.KEYWORDS.contains(elementType)) {
                                return;
                            }
                        }
                        result.addAllElements(getCommonKeywords());
                    }
                    return;
                }

                PsiElement definitionNode = globalVariableDefinitionNode.getParent();

                PackageDeclarationNode prevPackageDeclarationNode = PsiTreeUtil.getPrevSiblingOfType(definitionNode,
                        PackageDeclarationNode.class);

                ImportDeclarationNode prevImportDeclarationNode = PsiTreeUtil.getPrevSiblingOfType(definitionNode,
                        ImportDeclarationNode.class);

                ConstantDefinitionNode prevConstantDefinitionNode = PsiTreeUtil.getPrevSiblingOfType(definitionNode,
                        ConstantDefinitionNode.class);

                DefinitionNode prevDefinitionNode =
                        PsiTreeUtil.getPrevSiblingOfType(definitionNode, DefinitionNode.class);

                GlobalVariableDefinitionNode prevGlobalVariableDefinition =
                        PsiTreeUtil.findChildOfType(prevDefinitionNode, GlobalVariableDefinitionNode.class);

                if (prevPackageDeclarationNode == null && prevImportDeclarationNode == null
                        && prevConstantDefinitionNode == null && prevGlobalVariableDefinition == null) {
                    result.addAllElements(getFileLevelKeywordsAsLookups(true, true, true));
                } else if ((prevPackageDeclarationNode != null || prevImportDeclarationNode != null)
                        && prevConstantDefinitionNode == null && prevGlobalVariableDefinition == null) {
                    result.addAllElements(getFileLevelKeywordsAsLookups(true, false, true));
                } else {
                    result.addAllElements(getFileLevelKeywordsAsLookups(true, false, false));
                }

                addTypeNamesAsLookups(result);
            }
        }

        if (element instanceof IdentifierPSINode) {
            PsiReference reference = element.findReferenceAt(element.getTextLength());
            if (reference instanceof WorkerReference) {
                result.addAllElements(getWorkerInteractionKeywords());
            }
        }
    }
}
