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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.BallerinaParserDefinition;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResourceDefinitionNode extends IdentifierDefSubtree implements ScopeNode {

    public ResourceDefinitionNode(@NotNull ASTNode node) {
        super(node, BallerinaParserDefinition.ID);
    }

    @Nullable
    @Override
    public PsiElement resolve(PsiNamedElement element) {
        if (element.getParent() instanceof VariableReferenceNode) {
            // WARNING: SymtabUtils.resolve() will return the element node instead of the Identifier node. This might
            // cause issues when using find usage, etc. So use BallerinaPsiImplUtil.resolveElement() instead.
            return BallerinaPsiImplUtil.resolveElement(this, element, "//parameter/Identifier");
        } else if (element.getParent() instanceof NameReferenceNode) {
            PsiElement definition = BallerinaPsiImplUtil.resolveElement(this, element,
                    "//functionDefinition/Identifier", "//connectorDefinition/Identifier",
                    "//structDefinition/Identifier");
            if (definition != null) {
                return definition;
            }
            VariableReferenceNode variableReferenceNode = PsiTreeUtil.getParentOfType(element,
                    VariableReferenceNode.class);
            if (variableReferenceNode == null) {
                PsiElement prevToken = BallerinaCompletionUtils.getPreviousNonEmptyElement(element
                        .getContainingFile(), element.getTextOffset());
                if (prevToken instanceof LeafPsiElement) {
                    IElementType elementType = ((LeafPsiElement) prevToken).getElementType();
                    if (elementType == BallerinaTypes.DOT) {
                        PsiElement prevSibling = BallerinaCompletionUtils.getPreviousNonEmptyElement(element
                                .getContainingFile(), prevToken.getTextOffset());
                        return BallerinaPsiImplUtil.resolveField(element, prevSibling);
                    }
                }
                return null;
            }
            PsiElement prevSibling = variableReferenceNode.getPrevSibling();
            if (prevSibling != null) {
                if (prevSibling instanceof LeafPsiElement) {
                    IElementType elementType = ((LeafPsiElement) prevSibling).getElementType();
                    if (elementType == BallerinaTypes.DOT) {
                        PsiElement prevPrevSibling = BallerinaCompletionUtils.getPreviousNonEmptyElement(element
                                .getContainingFile(), prevSibling.getTextOffset());
                        return BallerinaPsiImplUtil.resolveField(element, prevPrevSibling);
                    }
                }
            }

            PsiElement nextSibling = variableReferenceNode.getNextSibling();
            if (nextSibling != null) {
                if (nextSibling instanceof LeafPsiElement) {
                    IElementType elementType = ((LeafPsiElement) nextSibling).getElementType();
                    if (elementType == BallerinaTypes.DOT) {
                        PsiElement parent = variableReferenceNode.getParent();
                        if (parent instanceof VariableReferenceNode) {
                            prevSibling = parent.getPrevSibling();
                            if (prevSibling instanceof LeafPsiElement) {
                                elementType = ((LeafPsiElement) nextSibling).getElementType();
                                if (elementType == BallerinaTypes.DOT) {
                                    PsiElement prevPrevSibling = BallerinaCompletionUtils.getPreviousNonEmptyElement
                                            (element
                                                    .getContainingFile(), prevSibling.getTextOffset());
                                    return BallerinaPsiImplUtil.resolveField(element, prevPrevSibling);
                                }
                            }
                        }
                    }
                }
            }

            VariableDefinitionNode variableDefinitionNode =
                    PsiTreeUtil.getParentOfType(element, VariableDefinitionNode.class);
            if (variableDefinitionNode != null) {

                PsiElement resolvedStruct = BallerinaPsiImplUtil.resolveStruct(variableDefinitionNode);

                if (resolvedStruct != null && resolvedStruct.getParent() instanceof StructDefinitionNode) {
                    return ((StructDefinitionNode) resolvedStruct.getParent()).resolve(element);
                }
            }

            ExpressionNode expressionNode = PsiTreeUtil.getParentOfType(element, ExpressionNode.class);
            if (expressionNode != null) {
                if (expressionNode.getParent() instanceof MapStructKeyValueNode) {
                    return null;
                }
            }

            return BallerinaPsiImplUtil.resolveElement(this, element, "//variableDefinitionStatement/Identifier");
        }
        return null;
    }
}
