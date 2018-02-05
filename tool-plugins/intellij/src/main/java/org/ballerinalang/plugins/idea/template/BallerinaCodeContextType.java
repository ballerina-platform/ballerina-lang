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
import org.ballerinalang.plugins.idea.highlighter.BallerinaSyntaxHighlighter;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.CallableUnitBodyNode;
import org.ballerinalang.plugins.idea.psi.CompilationUnitNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ForkJoinStatementNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
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
            return !(element instanceof PsiWhiteSpace) && element != null && isInContext(element);
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
            PsiFile originalFile = element.getContainingFile().getOriginalFile();
            PsiElement originalElement = originalFile.findElementAt(element.getTextOffset());

            ServiceBodyNode serviceBodyNode;
            if (originalElement instanceof PsiWhiteSpace) {
                if (originalElement.getPrevSibling() instanceof ServiceBodyNode) {
                    serviceBodyNode = (ServiceBodyNode) originalElement.getPrevSibling();
                } else if (originalElement.getNextSibling() instanceof ServiceBodyNode) {
                    serviceBodyNode = (ServiceBodyNode) originalElement.getNextSibling();
                } else {
                    serviceBodyNode = PsiTreeUtil.getParentOfType(originalElement, ServiceBodyNode.class);
                }
            } else {
                serviceBodyNode = PsiTreeUtil.getParentOfType(originalElement, ServiceBodyNode.class);
            }

            ResourceDefinitionNode resourceDefinitionNode = PsiTreeUtil.getParentOfType(originalElement,
                    ResourceDefinitionNode.class);
            if (serviceBodyNode != null && resourceDefinitionNode == null) {
                return true;
            }

            if (element.getParent() instanceof PsiErrorElement || element.getParent() instanceof NameReferenceNode) {
                PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(element);
                if (prevVisibleLeaf instanceof LeafPsiElement) {
                    IElementType elementType = ((LeafPsiElement) prevVisibleLeaf).getElementType();
                    if (elementType == BallerinaTypes.LBRACE &&
                            prevVisibleLeaf.getParent() instanceof ServiceDefinitionNode) {
                        return true;
                    }
                }
                resourceDefinitionNode = PsiTreeUtil.getParentOfType(prevVisibleLeaf, ResourceDefinitionNode.class);
                if (resourceDefinitionNode != null) {
                    return false;
                }
                PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(element);
                if (nextVisibleLeaf == null || !(nextVisibleLeaf instanceof LeafPsiElement)) {
                    return false;
                }
                IElementType elementType = ((LeafPsiElement) nextVisibleLeaf).getElementType();
                if (elementType == BallerinaTypes.RESOURCE) {
                    return true;
                } else if (elementType == BallerinaTypes.RBRACE) {
                    ServiceDefinitionNode serviceDefinitionNode = PsiTreeUtil.getParentOfType(originalElement,
                            ServiceDefinitionNode.class);
                    if (serviceDefinitionNode != null) {
                        return true;
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
            PsiFile originalFile = element.getContainingFile().getOriginalFile();
            PsiElement originalElement = originalFile.findElementAt(element.getTextOffset());
            CallableUnitBodyNode callableUnitBodyNode;
            if (originalElement instanceof PsiWhiteSpace) {
                if (originalElement.getPrevSibling() instanceof CallableUnitBodyNode) {
                    callableUnitBodyNode = (CallableUnitBodyNode) originalElement.getPrevSibling();
                } else if (originalElement.getNextSibling() instanceof CallableUnitBodyNode) {
                    callableUnitBodyNode = (CallableUnitBodyNode) originalElement.getNextSibling();
                } else {
                    callableUnitBodyNode = PsiTreeUtil.getParentOfType(originalElement, CallableUnitBodyNode.class);
                }
            } else {
                callableUnitBodyNode = PsiTreeUtil.getParentOfType(originalElement, CallableUnitBodyNode.class);
            }

            ResourceDefinitionNode resourceDefinitionNode = PsiTreeUtil.getParentOfType(originalElement,
                    ResourceDefinitionNode.class);
            if (callableUnitBodyNode != null && resourceDefinitionNode != null) {
                return true;
            }

            if (element.getParent() instanceof PsiErrorElement || element.getParent() instanceof NameReferenceNode) {
                PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(element);
                if (prevVisibleLeaf != null && prevVisibleLeaf.getParent() instanceof ResourceDefinitionNode) {
                    return true;
                }
                PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(element);
                if (nextVisibleLeaf != null && nextVisibleLeaf instanceof LeafPsiElement) {
                    IElementType elementType = ((LeafPsiElement) nextVisibleLeaf).getElementType();
                    if (elementType == BallerinaTypes.RBRACE) {
                        resourceDefinitionNode = PsiTreeUtil.getParentOfType(originalElement,
                                ResourceDefinitionNode.class);
                        if (resourceDefinitionNode != null) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    public static class Connector extends BallerinaCodeContextType {

        protected Connector() {
            super("BALLERINA_CONNECTOR", "Connector", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            PsiFile originalFile = element.getContainingFile().getOriginalFile();
            PsiElement originalElement = originalFile.findElementAt(element.getTextOffset());

            ConnectorBodyNode connectorBodyNode;
            if (originalElement instanceof PsiWhiteSpace) {
                if (originalElement.getPrevSibling() instanceof ConnectorBodyNode) {
                    connectorBodyNode = (ConnectorBodyNode) originalElement.getPrevSibling();
                } else if (originalElement.getNextSibling() instanceof ConnectorBodyNode) {
                    connectorBodyNode = (ConnectorBodyNode) originalElement.getNextSibling();
                } else {
                    connectorBodyNode = PsiTreeUtil.getParentOfType(originalElement, ConnectorBodyNode.class);
                }
            } else {
                connectorBodyNode = PsiTreeUtil.getParentOfType(originalElement, ConnectorBodyNode.class);
            }

            ActionDefinitionNode actionDefinitionNode = PsiTreeUtil.getParentOfType(originalElement,
                    ActionDefinitionNode.class);
            if (connectorBodyNode != null && actionDefinitionNode == null) {
                return true;
            }

            if (element.getParent() instanceof PsiErrorElement || element.getParent() instanceof NameReferenceNode) {
                PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(element);
                if (prevVisibleLeaf != null && prevVisibleLeaf.getParent() instanceof ActionDefinitionNode) {
                    return true;
                }
                PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(element);
                if (nextVisibleLeaf != null && nextVisibleLeaf instanceof LeafPsiElement) {
                    IElementType elementType = ((LeafPsiElement) nextVisibleLeaf).getElementType();
                    if (elementType == BallerinaTypes.ACTION) {
                        return true;
                    } else if (elementType == BallerinaTypes.RBRACE) {
                        ConnectorDefinitionNode connectorDefinitionNode = PsiTreeUtil.getParentOfType(originalElement,
                                ConnectorDefinitionNode.class);
                        if (connectorDefinitionNode != null) {
                            return true;
                        }
                    }
                }
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
            PsiFile originalFile = element.getContainingFile().getOriginalFile();
            PsiElement originalElement = originalFile.findElementAt(element.getTextOffset());
            CallableUnitBodyNode callableUnitBodyNode;
            if (originalElement instanceof PsiWhiteSpace) {
                if (originalElement.getPrevSibling() instanceof CallableUnitBodyNode) {
                    callableUnitBodyNode = (CallableUnitBodyNode) originalElement.getPrevSibling();
                } else if (originalElement.getNextSibling() instanceof CallableUnitBodyNode) {
                    callableUnitBodyNode = (CallableUnitBodyNode) originalElement.getNextSibling();
                } else {
                    callableUnitBodyNode = PsiTreeUtil.getParentOfType(originalElement,
                            CallableUnitBodyNode.class);
                }
            } else {
                callableUnitBodyNode = PsiTreeUtil.getParentOfType(originalElement,
                        CallableUnitBodyNode.class);
            }

            ActionDefinitionNode actionDefinitionNode = PsiTreeUtil.getParentOfType(originalElement,
                    ActionDefinitionNode.class);
            if (callableUnitBodyNode != null && actionDefinitionNode != null) {
                return true;
            }

            if (element.getParent() instanceof PsiErrorElement || element.getParent() instanceof NameReferenceNode) {
                PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(element);
                if (prevVisibleLeaf != null && prevVisibleLeaf.getParent() instanceof ActionDefinitionNode) {
                    return true;
                }
                PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(element);
                if (nextVisibleLeaf != null && nextVisibleLeaf instanceof LeafPsiElement) {
                    IElementType elementType = ((LeafPsiElement) nextVisibleLeaf).getElementType();
                    if (elementType == BallerinaTypes.RBRACE) {
                        actionDefinitionNode = PsiTreeUtil.getParentOfType(originalElement,
                                ActionDefinitionNode.class);
                        if (actionDefinitionNode != null) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    public static class Function extends BallerinaCodeContextType {

        protected Function() {
            super("BALLERINA_FUNCTION", "Function", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            PsiFile originalFile = element.getContainingFile().getOriginalFile();
            PsiElement originalElement = originalFile.findElementAt(element.getTextOffset());
            CallableUnitBodyNode callableUnitBodyNode;
            if (originalElement instanceof PsiWhiteSpace) {
                if (originalElement.getPrevSibling() instanceof CallableUnitBodyNode) {
                    callableUnitBodyNode = (CallableUnitBodyNode) originalElement.getPrevSibling();
                } else if (originalElement.getNextSibling() instanceof CallableUnitBodyNode) {
                    callableUnitBodyNode = (CallableUnitBodyNode) originalElement.getNextSibling();
                } else {
                    callableUnitBodyNode = PsiTreeUtil.getParentOfType(originalElement, CallableUnitBodyNode.class);
                }
            } else {
                callableUnitBodyNode = PsiTreeUtil.getParentOfType(originalElement, CallableUnitBodyNode.class);
            }

            FunctionDefinitionNode functionDefinitionNode = PsiTreeUtil.getParentOfType(originalElement,
                    FunctionDefinitionNode.class);
            if (callableUnitBodyNode != null && functionDefinitionNode != null) {
                return true;
            }

            if (element.getParent() instanceof PsiErrorElement || element.getParent() instanceof NameReferenceNode) {
                PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(element);
                functionDefinitionNode = PsiTreeUtil.getParentOfType(prevVisibleLeaf, FunctionDefinitionNode.class);
                if (functionDefinitionNode != null) {
                    return true;
                }
                PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(element);
                if (nextVisibleLeaf == null || !(nextVisibleLeaf instanceof LeafPsiElement)) {
                    return false;
                }
                IElementType elementType = ((LeafPsiElement) nextVisibleLeaf).getElementType();
                if (elementType != BallerinaTypes.RBRACE) {
                    return false;
                }
                functionDefinitionNode = PsiTreeUtil.getParentOfType(originalElement,
                        FunctionDefinitionNode.class);
                if (functionDefinitionNode != null) {
                    return true;
                }
            }
            return false;
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
            return forkJoinStatementNode != null;
        }
    }
}
