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

package org.ballerinalang.plugins.idea.editor;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandlerWithTabActionSupport;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.ExpressionListNode;
import org.ballerinalang.plugins.idea.psi.FunctionInvocationStatementNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ParameterListNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.SimpleTypeNode;
import org.ballerinalang.plugins.idea.psi.references.SimpleTypeReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BallerinaParameterInfoHandler implements ParameterInfoHandlerWithTabActionSupport {

    @NotNull
    @Override
    public PsiElement[] getActualParameters(@NotNull PsiElement o) {
        return new PsiElement[0];
    }

    @NotNull
    @Override
    public IElementType getActualParameterDelimiterType() {
        return BallerinaTypes.COMMA;
    }

    @NotNull
    @Override
    public IElementType getActualParametersRBraceType() {
        return BallerinaTypes.RPAREN;
    }

    @NotNull
    @Override
    public Set<Class> getArgumentListAllowedParentClasses() {
        return ContainerUtil.newHashSet();
    }

    @NotNull
    @Override
    public Set<? extends Class> getArgListStopSearchClasses() {
        return ContainerUtil.newHashSet();
    }

    @NotNull
    @Override
    public Class getArgumentListClass() {
        return ExpressionListNode.class;
    }

    @Override
    public boolean couldShowInLookup() {
        return true;
    }

    @Nullable
    @Override
    public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context) {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Nullable
    @Override
    public Object[] getParametersForDocumentation(Object p, ParameterInfoContext context) {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Nullable
    @Override
    public Object findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        PsiElement psiElement = context.getFile().findElementAt(context.getOffset());
        return PsiTreeUtil.getParentOfType(psiElement, ExpressionListNode.class);
    }

    @Override
    public void showParameterInfo(@NotNull Object element, @NotNull CreateParameterInfoContext context) {
        if (element instanceof ExpressionListNode) {
            PsiElement psiElement = (ExpressionListNode) element;

            FunctionInvocationStatementNode parent = PsiTreeUtil.getParentOfType(psiElement,
                    FunctionInvocationStatementNode.class);

            // Todo - Check support for connectors and actions after finalizing the Ballerina SDK
            if (parent != null) {
                List<ParameterListNode> list = new ArrayList<>();

                SimpleTypeNode function = PsiTreeUtil.findChildOfType(parent, SimpleTypeNode.class);
                IdentifierPSINode identifier = PsiTreeUtil.findChildOfType(function, IdentifierPSINode.class);
                if (identifier != null) {
                    PsiReference reference = identifier.getReference();
                    if (reference != null) {
                        PsiElement resolvedElement = reference.resolve();
                        if (resolvedElement != null) {
                            ParameterListNode parameterListNode =
                                    PsiTreeUtil.findChildOfType(resolvedElement.getParent(), ParameterListNode.class);
                            if (parameterListNode == null) {
                                // Todo - change how to identify no parameter situation
                                context.setItemsToShow(new Object[]{"Empty"});
                            } else {
                                context.setItemsToShow(new Object[]{parameterListNode});
                            }
                            context.showHint(psiElement, (psiElement).getTextRange().getStartOffset(), this);
                            return;
                        }
                    }

                    PsiReference[] references = identifier.getReferences();
                    for (PsiReference psiReference : references) {
                        ResolveResult[] resolveResults = ((SimpleTypeReference) psiReference).multiResolve(false);
                        if (resolveResults.length == 0) {
                            continue;
                        }
                        for (ResolveResult resolveResult : resolveResults) {
                            PsiElement parentElement = resolveResult.getElement().getParent();
                            ParameterListNode parameterListNode = PsiTreeUtil.findChildOfType(parentElement,
                                    ParameterListNode.class);
                            if (parameterListNode != null) {
                                list.add(parameterListNode);
                            }
                        }
                    }
                }

                if (list.isEmpty()) {
                    // Todo - change how to identify no parameter situation
                    context.setItemsToShow(new Object[]{"Empty"});
                } else {
                    context.setItemsToShow(list.toArray(new ParameterListNode[list.size()]));
                }
                context.showHint(psiElement, (psiElement).getTextRange().getStartOffset(), this);
            }
        }
    }

    @Nullable
    @Override
    public Object findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        PsiElement psiElement = context.getFile().findElementAt(context.getOffset());
        return PsiTreeUtil.getParentOfType(psiElement, ExpressionListNode.class);
    }

    @Override
    public void updateParameterInfo(@NotNull Object o, @NotNull UpdateParameterInfoContext context) {
        if (o instanceof ExpressionListNode) {

            // Todo - Update logic
            ExpressionListNode argumentListNode = ((ExpressionListNode) o);
            ExpressionListNode expressionListNodes =
                    PsiTreeUtil.getChildOfType(argumentListNode, ExpressionListNode.class);

            if (expressionListNodes == null) {
                context.setCurrentParameter(0);
                return;
            }

            PsiElement psiElement = context.getFile().findElementAt(context.getOffset());

            PsiElement[] children = expressionListNodes.getChildren();
            if (children.length == 0) {
                context.setCurrentParameter(0);
                return;
            }

            int index = 0;
            for (PsiElement child : children) {
                int childTextOffset = child.getTextOffset();
                if (psiElement.getTextOffset() <= childTextOffset) {
                    context.setCurrentParameter(index);
                    return;
                }
                if (child instanceof LeafPsiElement) {
                    index++;
                }
            }
            context.setCurrentParameter(index);
        } else {
            context.setCurrentParameter(0);
        }
    }

    @Nullable
    @Override
    public String getParameterCloseChars() {
        return ",";
    }

    @Override
    public boolean tracksParameterIndex() {
        return true;
    }

    @Override
    public void updateUI(Object p, @NotNull ParameterInfoUIContext context) {
        if (p instanceof ParameterListNode) {
            PsiElement element = (ParameterListNode) p;
            Collection<ParameterNode> childrenOfType = PsiTreeUtil.findChildrenOfType(element, ParameterNode.class);

            int start = 0;
            int end = 0;
            StringBuilder builder = new StringBuilder();

            if (childrenOfType.isEmpty()) {
                builder.append(CodeInsightBundle.message("parameter.info.no.parameters"));
            } else {

                int selected = context.getCurrentParameterIndex();

                Iterator<ParameterNode> iterator = childrenOfType.iterator();
                for (int i = 0; i < childrenOfType.size(); i++) {
                    ParameterNode next = iterator.next();
                    if (i != 0) {
                        builder.append(", ");
                    }
                    if (i == selected) {
                        start = builder.length();
                    }
                    builder.append(next.getText());

                    if (i == selected) {
                        end = builder.length();
                    }
                }
            }
            context.setupUIComponentPresentation(builder.toString(), start, end, false, false, false,
                    context.getDefaultParameterColor());
        } else if (p.equals("Empty")) {
            // Todo - change how to identify no parameter situation
            StringBuilder builder = new StringBuilder();
            builder.append(CodeInsightBundle.message("parameter.info.no.parameters"));
            context.setupUIComponentPresentation(builder.toString(), 0, 0, false, false, false,
                    context.getDefaultParameterColor());
        } else {
            context.setUIComponentEnabled(false);
        }
    }
}
