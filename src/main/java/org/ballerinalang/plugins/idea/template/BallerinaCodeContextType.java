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

package org.ballerinalang.plugins.idea.template;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.highlighter.BallerinaSyntaxHighlighter;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.CallableUnitBodyNode;
import org.ballerinalang.plugins.idea.psi.CompilationUnitNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ForkJoinStatementNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BallerinaCodeContextType extends TemplateContextType {

    BallerinaCodeContextType(@NotNull @NonNls String id, @NotNull String presentableName,
                             @Nullable Class<? extends TemplateContextType> baseContextType) {
        super(id, presentableName, baseContextType);
    }

    @Override
    public boolean isInContext(@NotNull PsiFile file, int offset) {
        if (PsiUtilCore.getLanguageAtOffset(file, offset).isKindOf(BallerinaLanguage.INSTANCE)) {
            PsiElement element = file.findElementAt(offset);
            if (element instanceof PsiWhiteSpace) {
                return false;
            }
            return element != null && isInContext(element);
        }
        return false;
    }

    @NotNull
    @Override
    public SyntaxHighlighter createHighlighter() {
        return new BallerinaSyntaxHighlighter();
    }

    protected abstract boolean isInContext(@NotNull PsiElement element);

    public static class File extends BallerinaCodeContextType {

        protected File() {
            super("BALLERINA_FILE", "File", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            if (element.getParent() instanceof PsiErrorElement) {
                if (element.getParent().getParent() instanceof CompilationUnitNode) {
                    return true;
                }
            } else if (element instanceof CompilationUnitNode) {
                return true;
            } else if (element.getParent() instanceof NameReferenceNode) {
                GlobalVariableDefinitionNode node = PsiTreeUtil.getParentOfType(element,
                        GlobalVariableDefinitionNode.class);
                if (node != null) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class Service extends BallerinaCodeContextType {

        protected Service() {
            super("BALLERINA_SERVICE", "Service", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            if (element.getParent() instanceof PsiErrorElement) {
                ServiceBodyNode serviceBodyNode = PsiTreeUtil.getParentOfType(element, ServiceBodyNode.class);
                if (serviceBodyNode != null) {
                    return true;
                }
                PsiElement previousNonEmptyElement = BallerinaCompletionUtils.getPreviousNonEmptyElement(element
                        .getContainingFile(), element.getTextOffset());
                if (previousNonEmptyElement.getParent() instanceof ResourceDefinitionNode) {
                    return true;
                }
            } else if (element instanceof ServiceBodyNode) {
                return true;
            } else if (element.getParent() instanceof NameReferenceNode) {
                PsiElement previousNonEmptyElement = BallerinaCompletionUtils.getPreviousNonEmptyElement(element
                        .getContainingFile(), element.getTextOffset());
                if (previousNonEmptyElement.getParent() instanceof ResourceDefinitionNode) {
                    return true;
                }
                ServiceBodyNode serviceBodyNode = PsiTreeUtil.getParentOfType(element, ServiceBodyNode.class);
                StatementNode statementNode = PsiTreeUtil.getParentOfType(element, StatementNode.class);
                if (serviceBodyNode != null && statementNode == null) {
                    return true;
                }
            } else {
                PsiElement parent = element.getParent();
                while (parent != null && !(parent instanceof PsiFile)) {
                    if (parent instanceof ServiceBodyNode) {
                        return true;
                    }
                    parent = parent.getParent();
                    if (parent instanceof ResourceDefinitionNode) {
                        return false;
                    }
                }
            }
            return false;
        }
    }

    public static class Resource extends BallerinaCodeContextType {

        protected Resource() {
            super("BALLERINA_RESOURCE", "Resource", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            ResourceDefinitionNode resourceDefinitionNode = PsiTreeUtil.getParentOfType(element,
                    ResourceDefinitionNode.class);
            CallableUnitBodyNode callableUnitBodyNode = PsiTreeUtil.getParentOfType(element,
                    CallableUnitBodyNode.class);
            if (resourceDefinitionNode == null || callableUnitBodyNode == null) {
                return false;
            }
            return true;
        }
    }

    public static class Connector extends BallerinaCodeContextType {

        protected Connector() {
            super("BALLERINA_CONNECTOR", "Connector", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            if (element instanceof LeafPsiElement) {
                IElementType elementType = ((LeafPsiElement) element).getElementType();
                if (elementType == BallerinaTypes.QUOTED_STRING) {
                    return false;
                }
            }
            if (element.getParent() instanceof PsiErrorElement || element.getParent() instanceof NameReferenceNode) {
                PsiElement previousNonEmptyElement = BallerinaCompletionUtils.getPreviousNonEmptyElement(element
                        .getContainingFile(), element.getTextOffset());
                if (previousNonEmptyElement.getParent() instanceof ActionDefinitionNode) {
                    return true;
                }
            }
            ActionDefinitionNode actionDefinitionNode = PsiTreeUtil.getParentOfType(element,
                    ActionDefinitionNode.class);
            if (actionDefinitionNode != null) {
                return false;
            }
            ConnectorBodyNode connectorBodyNode = PsiTreeUtil.getParentOfType(element, ConnectorBodyNode.class);
            if (connectorBodyNode != null) {
                return true;
            }

            return false;
        }
    }

    public static class Action extends BallerinaCodeContextType {

        protected Action() {
            super("BALLERINA_ACTION", "Action", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            if (element instanceof LeafPsiElement) {
                IElementType elementType = ((LeafPsiElement) element).getElementType();
                if (elementType == BallerinaTypes.QUOTED_STRING) {
                    return false;
                }
            }
            ActionDefinitionNode actionDefinitionNode = PsiTreeUtil.getParentOfType(element,
                    ActionDefinitionNode.class);
            CallableUnitBodyNode callableUnitBodyNode = PsiTreeUtil.getParentOfType(element,
                    CallableUnitBodyNode.class);
            if (actionDefinitionNode == null || callableUnitBodyNode == null) {
                return false;
            }
            return true;
        }
    }

    public static class Function extends BallerinaCodeContextType {

        protected Function() {
            super("BALLERINA_FUNCTION", "Function", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            if (element instanceof LeafPsiElement) {
                IElementType elementType = ((LeafPsiElement) element).getElementType();
                if (elementType == BallerinaTypes.QUOTED_STRING) {
                    return false;
                }
            }
            FunctionDefinitionNode functionDefinitionNode = PsiTreeUtil.getParentOfType(element,
                    FunctionDefinitionNode.class);
            CallableUnitBodyNode callableUnitBodyNode = PsiTreeUtil.getParentOfType(element,
                    CallableUnitBodyNode.class);
            if (functionDefinitionNode == null || callableUnitBodyNode == null) {
                return false;
            }
            return true;
        }
    }

    public static class ForkJoin extends BallerinaCodeContextType {

        protected ForkJoin() {
            super("BALLERINA_FORK_JOIN", "Fork Join", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            ForkJoinStatementNode forkJoinStatementNode = PsiTreeUtil.getParentOfType(element,
                    ForkJoinStatementNode.class);
            if (forkJoinStatementNode == null) {
                return false;
            }
            return true;
        }
    }
}
