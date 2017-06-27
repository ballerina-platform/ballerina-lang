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
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.ActionInvocationNode;
import org.ballerinalang.plugins.idea.psi.ConnectorInitExpressionNode;
import org.ballerinalang.plugins.idea.psi.ExpressionListNode;
import org.ballerinalang.plugins.idea.psi.ExpressionNode;
import org.ballerinalang.plugins.idea.psi.FunctionInvocationStatementNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.ParameterListNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.references.ActionInvocationReference;
import org.ballerinalang.plugins.idea.psi.references.NameReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
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
        // Todo - Add util methods to get these elements
        // Get the element at offset.
        PsiElement element = context.getFile().findElementAt(context.getOffset());
        // If there is no element, return null.
        if (element == null) {
            return null;
        }
        // If the element at offset is ), that means we are at the end of a parameter list. Ex:- setData(name,|)
        // So in that case, we get the element at the "offset - 1". Ex:- setData(name|,)
        // This will allow us to identify the correct ExpressionListNode element.
        if (")".equals(element.getText())) {
            PsiElement parent = element.getParent();
            // If the parent is an ActionInvocationNode, Connector will be resolved instead of the action if we get
            // the NameReferenceNode here. We need to get the NameReferenceNode because otherwise the nested function
            // invocations will not show proper parameters.
            if (parent != null && !(parent instanceof ActionInvocationNode)) {
                NameReferenceNode nameReferenceNode = PsiTreeUtil.getChildOfType(parent, NameReferenceNode.class);
                if (nameReferenceNode != null) {
                    return nameReferenceNode;
                }
            }
            // If a NameReferenceNode is not found, get the element at previous offset. This will most probably
            // contain "(".
            element = context.getFile().findElementAt(context.getOffset() - 1);
        }
        PsiElement node = PsiTreeUtil.getParentOfType(element, ExpressionListNode.class);
        // ExpressionListNode can be null if there are no arguments provided.
        if (node != null) {
            return node;
        }
        // So we check return the FunctionInvocationStatementNode in that case.
        node = PsiTreeUtil.getParentOfType(element, FunctionInvocationStatementNode.class);
        if (node != null) {
            return node;
        }
        // If FunctionInvocationStatementNode is null, we check return the ActionInvocationNode in that case.
        node = PsiTreeUtil.getParentOfType(element, ActionInvocationNode.class);
        if (node != null) {
            return node;
        }
        // If ActionInvocationNode is null, we check return the ConnectorInitExpressionNode in that case.
        node = PsiTreeUtil.getParentOfType(element, ConnectorInitExpressionNode.class);
        if (node != null) {
            return node;
        }
        // If the node is still null and the current element is (, that means we are at a empty (). So we check for
        // any expression node parent.
        if (element != null && "(".equals(element.getText())) {
            node = PsiTreeUtil.getParentOfType(element, ExpressionNode.class);
        }
        if (node != null) {
            return node;
        }
        return null;
    }

    @Override
    public void showParameterInfo(@NotNull Object element, @NotNull CreateParameterInfoContext context) {
        // This method will be called with the return object of the findElementForParameterInfo(). If it is null,
        // this method will not be called.
        // Since we know the type, we check and cast the object.
        if (element instanceof ExpressionListNode) {
            ExpressionListNode expressionListNode = (ExpressionListNode) element;
            // We need to get the ExpressionListNode parent of current ExpressionListNode.
            // Current ExpressionListNode - "WSO2"
            // Parent ExpressionListNode - setName("WSO2")
            // By doing this, we get the function name because setName("WSO2") is also a ExpressionNode.
            PsiElement parent = PsiTreeUtil.getParentOfType(expressionListNode, ExpressionNode.class);
            // If the parent is null, that means there is no parent ExpressionListNode. That can happen if the parent
            // node is a FunctionInvocationStatementNode.
            if (parent == null) {
                // So if the parent is null, we consider the FunctionInvocationStatementNode as the parent node.
                parent = PsiTreeUtil.getParentOfType(expressionListNode, FunctionInvocationStatementNode.class);
            }
            if (parent == null) {
                // So if the parent is null, we consider the ActionInvocationNode as the parent node.
                parent = PsiTreeUtil.getParentOfType(expressionListNode, ActionInvocationNode.class);
            }
            if (parent == null) {
                // So if the parent is null, we consider the ActionInvocationNode as the parent node.
                parent = PsiTreeUtil.getParentOfType(expressionListNode, ConnectorInitExpressionNode.class);
            }
            if (parent == null) {
                // So if the parent is null, we consider the ExpressionListNode as the parent node.
                parent = PsiTreeUtil.getParentOfType(expressionListNode, ExpressionListNode.class);
            }

            if (parent == null) {
                parent = expressionListNode;
            }
            setItemsToShow(expressionListNode, parent, context);
        } else if (element instanceof FunctionInvocationStatementNode) {
            FunctionInvocationStatementNode functionInvocationStatementNode = (FunctionInvocationStatementNode) element;
            setItemsToShow(functionInvocationStatementNode, functionInvocationStatementNode, context);
        } else if (element instanceof ActionInvocationNode) {
            ActionInvocationNode actionInvocationNode = (ActionInvocationNode) element;
            setItemsToShow(actionInvocationNode, actionInvocationNode, context);
        } else if (element instanceof ConnectorInitExpressionNode) {
            ConnectorInitExpressionNode connectorInitExpressionNode = (ConnectorInitExpressionNode) element;
            setItemsToShow(connectorInitExpressionNode, connectorInitExpressionNode, context);
        } else if (element instanceof ExpressionNode) {
            ExpressionNode expressionNode = (ExpressionNode) element;
            setItemsToShow(expressionNode, expressionNode, context);
        } else if (element instanceof NameReferenceNode) {
            NameReferenceNode nameReferenceNode = (NameReferenceNode) element;
            setItemsToShow(nameReferenceNode, nameReferenceNode, context);
        }
    }

    private void setItemsToShow(PsiElement element, PsiElement parent, CreateParameterInfoContext context) {
        // This method can be a overloaded method. So there can be multiple signatures. To store all of these, we
        // create a list.
        List<ParameterListNode> list = new ArrayList<>();
        // Function name will be at NameReferenceNode. So we search for this child node.
        PsiElement namedIdentifierDefNode = null;

        if (parent instanceof ExpressionListNode || parent instanceof FunctionInvocationStatementNode) {
            namedIdentifierDefNode = PsiTreeUtil.findChildOfType(parent, NameReferenceNode.class);
        } else if (parent instanceof ActionInvocationNode) {
            namedIdentifierDefNode = parent;
        } else if (parent instanceof ConnectorInitExpressionNode) {
            namedIdentifierDefNode = PsiTreeUtil.findChildOfType(parent, NameReferenceNode.class);
        } else if (parent instanceof ExpressionNode) {
            namedIdentifierDefNode = PsiTreeUtil.findChildOfType(parent, NameReferenceNode.class);
        } else if (parent instanceof NameReferenceNode) {
            namedIdentifierDefNode = parent;
        }

        if (namedIdentifierDefNode != null) {
            // Sometimes we might not be able to resolve elements. In that case, we should not show "No parameters"
            // message. To identify this situation, we use this variable.
            boolean isResolved = false;
            // Get the identifier of this node.
            PsiElement nameIdentifier = ((IdentifierDefSubtree) namedIdentifierDefNode).getNameIdentifier();
            if (nameIdentifier != null) {
                // Get the reference for this node. This can be used to resolve functions defined in the same
                // package. Otherwise, we have to use the getReferences() as below.
                PsiReference reference = nameIdentifier.getReference();
                if (reference != null) {
                    // Resolve the reference
                    PsiElement resolvedElement = reference.resolve();
                    if (resolvedElement != null) {
                        isResolved = true;
                        // Resolved element will be the identifier of the function node. So we get the parent
                        // node (FunctionDefinitionNode).
                        PsiElement functionNode = resolvedElement.getParent();
                        // Since we need the ParameterListNode, search for ParameterListNode child node.
                        ParameterListNode parameterListNode =
                                PsiTreeUtil.findChildOfType(functionNode, ParameterListNode.class);
                        // Add to the list if the result is not null.
                        if (parameterListNode != null) {
                            list.add(parameterListNode);
                        }
                    }
                }

                if (!isResolved) {
                    // getReference() will only resolve function definitions within the same package. If the function
                    // definition is not in the same package, we need to use the getReferences() method as below.
                    PsiReference[] references = nameIdentifier.getReferences();
                    // Iterate through all references. In most cases, there will be only one item.
                    for (PsiReference psiReference : references) {
                        if (psiReference instanceof NameReference) {
                            // Multi resolve the reference.
                            ResolveResult[] resolveResults = ((NameReference) psiReference).multiResolve(false);
                            //If it cannot be resolved, continue with the next item.
                            if (resolveResults.length == 0) {
                                continue;
                            }
                            // If it can be resolved, iterate through all resolve results.
                            for (ResolveResult resolveResult : resolveResults) {
                                // Get the resolved element. This will be a identifier node.
                                PsiElement resolvedElement = resolveResult.getElement();
                                if (resolvedElement == null) {
                                    continue;
                                }
                                isResolved = true;
                                // Get the parent node (FunctionDefinitionNode).
                                PsiElement parentElement = resolvedElement.getParent();
                                // Get the ParameterListNode.
                                ParameterListNode parameterListNode =
                                        PsiTreeUtil.findChildOfType(parentElement, ParameterListNode.class);
                                // Add to the list if it is not null;
                                if (parameterListNode != null) {
                                    list.add(parameterListNode);
                                }
                            }
                        } else if (psiReference instanceof ActionInvocationReference) {
                            // Multi resolve the reference.
                            ResolveResult[] resolveResults = ((ActionInvocationReference) psiReference).multiResolve
                                    (false);
                            //If it cannot be resolved, continue with the next item.
                            if (resolveResults.length == 0) {
                                continue;
                            }
                            // If it can be resolved, iterate through all resolve results.
                            for (ResolveResult resolveResult : resolveResults) {
                                // Get the resolved element. This will be a identifier node.
                                PsiElement resolvedElement = resolveResult.getElement();
                                if (resolvedElement == null) {
                                    continue;
                                }
                                isResolved = true;
                                // Get the parent node (FunctionDefinitionNode).
                                PsiElement parentElement = resolvedElement.getParent();
                                // Get the ParameterListNode.
                                ParameterListNode parameterListNode =
                                        PsiTreeUtil.findChildOfType(parentElement, ParameterListNode.class);
                                // Add to the list if it is not null;
                                if (parameterListNode != null) {
                                    list.add(parameterListNode);
                                }
                            }
                        }
                    }
                }
            }
            // If there are no items to show, set a custom object. Otherwise set the list as an array.
            if (list.isEmpty() && isResolved) {
                // Todo - change how to identify no parameter situation
                context.setItemsToShow(new Object[]{"Empty"});
            } else {
                context.setItemsToShow(list.toArray(new ParameterListNode[list.size()]));
            }
            context.showHint(element, element.getTextRange().getStartOffset(), this);
        }
    }

    @Nullable
    @Override
    public Object findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        // Todo - Add util methods to get these elements
        // Get the element at offset.
        PsiElement element = context.getFile().findElementAt(context.getOffset());
        // If there is no element, return null.
        if (element == null) {
            return null;
        }
        // If the element at offset is ), that means we are at the end of a parameter list. Ex:- setData(name,|)
        // So in that case, we get the element at the "offset - 1". Ex:- setData(name|,)
        // This will allow us to identify the correct ExpressionListNode element.
        if (")".equals(element.getText())) {
            PsiElement parent = element.getParent();
            // If the parent is an ActionInvocationNode, Connector will be resolved instead of the action if we get
            // the NameReferenceNode here. We need to get the NameReferenceNode because otherwise the nested function
            // invocations will not show proper parameters.
            if (parent != null && !(parent instanceof ActionInvocationNode)) {
                NameReferenceNode nameReferenceNode = PsiTreeUtil.getChildOfType(parent, NameReferenceNode.class);
                if (nameReferenceNode != null) {
                    return nameReferenceNode;
                }
            }
            // If a NameReferenceNode is not found, get the element at previous offset. This will most probably
            // contain "(".
            element = context.getFile().findElementAt(context.getOffset() - 1);
        }
        PsiElement node = PsiTreeUtil.getParentOfType(element, ExpressionListNode.class);
        // ExpressionListNode can be null if there are no arguments provided.
        if (node != null) {
            return node;
        }
        // So we check return the FunctionInvocationStatementNode in that case.
        node = PsiTreeUtil.getParentOfType(element, FunctionInvocationStatementNode.class);
        if (node != null) {
            return node;
        }
        // If FunctionInvocationStatementNode is null, we check return the ActionInvocationNode in that case.
        node = PsiTreeUtil.getParentOfType(element, ActionInvocationNode.class);
        if (node != null) {
            return node;
        }
        // If ActionInvocationNode is null, we check return the ConnectorInitExpressionNode in that case.
        node = PsiTreeUtil.getParentOfType(element, ConnectorInitExpressionNode.class);
        if (node != null) {
            return node;
        }
        // If the node is still null and the current element is (, that means we are at a empty (). So we check for
        // any expression node parent.
        if (element != null && "(".equals(element.getText())) {
            node = PsiTreeUtil.getParentOfType(element, ExpressionNode.class);
        }
        if (node != null) {
            return node;
        }
        return null;
    }

    @Override
    public void updateParameterInfo(@NotNull Object o, @NotNull UpdateParameterInfoContext context) {
        // This method updates parameter index node. This will be used to highlight the current parameter in the
        // parameter popup.
        PsiElement element;
        // If the object is a type of ExpressionListNode, cast the object.
        if (o instanceof ExpressionListNode) {
            element = ((ExpressionListNode) o);
        } else if (o instanceof NameReferenceNode) {
            PsiElement parent = ((NameReferenceNode) o).getParent();
            ExpressionListNode expressionListNode = PsiTreeUtil.getChildOfType(parent, ExpressionListNode.class);
            if (expressionListNode == null) {
                context.setCurrentParameter(0);
                return;
            }
            PsiElement[] children = expressionListNode.getChildren();
            int index = children.length / 2;
            context.setCurrentParameter(index);
            return;
        } else {
            context.setCurrentParameter(0);
            return;
        }

        // Get the element at offset.
        PsiElement psiElement = context.getFile().findElementAt(context.getOffset());
        if (psiElement == null) {
            return;
        }
        // Get the child nodes of element.
        PsiElement[] children = element.getChildren();
        // If there are no child nodes, set current parameter to 0 and return.
        if (children.length == 0) {
            context.setCurrentParameter(0);
            return;
        }

        // If the number of children are not 0, we need to calculate the correct index.
        int index = 0;
        // Iterate through all children.
        for (PsiElement child : children) {
            // Get the offset of the child.
            int childTextOffset = child.getTextOffset();
            // Ex:- setName(|"WSO2")
            // In the above example, if we consider the caret position as X, child text offset is X+1. Then the
            // following condition is true. So we set the current parameter and return.
            if (psiElement.getTextOffset() <= childTextOffset) {
                context.setCurrentParameter(index);
                return;
            }
            // If the child is a LeafPsiElement, increment the index.
            if (child instanceof LeafPsiElement) {
                index++;
            }
        }
        // Ex:- setName("WSO2"|)
        // In the above example, "psiElement.getTextOffset() <= childTextOffset" condition is false. The child node
        // ("WSO2") is not a LeafPsiElement. So the index does not get incremented in the for loop. At this point,
        // the index is 0 which is correct. This can be other than 0 depending on the number of parameters. So we set
        // the calculated current parameter.
        context.setCurrentParameter(index);
    }

    @Nullable
    @Override
    public String getParameterCloseChars() {
        return "(,";
    }

    @Override
    public boolean tracksParameterIndex() {
        return true;
    }

    @Override
    public void updateUI(Object p, @NotNull ParameterInfoUIContext context) {
        updatePresentation(p, context);
    }

    public static String updatePresentation(Object p, @NotNull ParameterInfoUIContext context) {
        // This method contains the logic which we use to show the parameters in the popup.
        // The object should be of type ParameterListNode. This method will be called for each element we set using the
        // context.setItemsToShow() in showParameterInfo method.
        if (p instanceof ParameterListNode) {
            // Caste the object.
            ParameterListNode element = (ParameterListNode) p;
            // Get the parameter presentations.
            List<String> parameterPresentations = getParameterPresentations(element);
            // These will be used to identify which parameter is selected. This will be highlighted in the popup.
            int start = 0;
            int end = 0;
            StringBuilder builder = new StringBuilder();
            // If there are no parameter nodes, set "no parameters" message.
            if (parameterPresentations.isEmpty()) {
                builder.append(CodeInsightBundle.message("parameter.info.no.parameters"));
            } else {
                // Get the current parameter index.
                int selected = context.getCurrentParameterIndex();
                // Iterate through each parameter presentation.
                for (int i = 0; i < parameterPresentations.size(); i++) {
                    // If i != 0, we need to add the , between parameters.
                    if (i != 0) {
                        builder.append(", ");
                    }
                    // If the current parameter is the selected parameter, get the start index.
                    if (i == selected) {
                        start = builder.length();
                    }
                    // Append the parameter.
                    builder.append(parameterPresentations.get(i));
                    // If the current parameter is the selected parameter, get the end index.
                    if (i == selected) {
                        end = builder.length();
                    }
                }
            }
            // Call setupUIComponentPresentation with necessary arguments.
            return context.setupUIComponentPresentation(builder.toString(), start, end, false, false, false,
                    context.getDefaultParameterColor());
        } else if (p.equals("Empty")) {
            // This will be called if there are no arguments in the method.
            // Todo - change how to identify no parameter situation
            // We set the "no.parameters" message to the popup.
            return context.setupUIComponentPresentation(CodeInsightBundle.message("parameter.info.no.parameters"), 0, 0,
                    false, false, false,
                    context.getDefaultParameterColor());
        } else {
            // Disable the popup for other types.
            context.setUIComponentEnabled(false);
            return null;
        }
    }

    /**
     * Creates a list of parameter presentations.
     *
     * @param node parameterListNode which contains the parameters
     * @return list of parameter presentations
     */
    public static List<String> getParameterPresentations(ParameterListNode node) {
        List<String> params = new LinkedList<>();
        if (node == null) {
            return params;
        }
        // Get parameter nodes.
        Collection<ParameterNode> parameterNodes = PsiTreeUtil.findChildrenOfType(node, ParameterNode.class);
        for (ParameterNode parameterNode : parameterNodes) {
            params.add(parameterNode.getText());
        }
        return params;
    }
}
