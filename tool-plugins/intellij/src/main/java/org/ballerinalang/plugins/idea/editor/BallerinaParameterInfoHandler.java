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
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.ActionInvocationNode;
import org.ballerinalang.plugins.idea.psi.AnyIdentifierNameNode;
import org.ballerinalang.plugins.idea.psi.ConnectorInitNode;
import org.ballerinalang.plugins.idea.psi.ConnectorReferenceNode;
import org.ballerinalang.plugins.idea.psi.ExpressionListNode;
import org.ballerinalang.plugins.idea.psi.ExpressionNode;
import org.ballerinalang.plugins.idea.psi.FunctionInvocationNode;
import org.ballerinalang.plugins.idea.psi.FunctionReferenceNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.InvocationNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.ParameterListNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.ParameterTypeName;
import org.ballerinalang.plugins.idea.psi.ParameterTypeNameList;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Provides parameter info support.
 */
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
        return findElement(context);
    }

    @Nullable
    private Object findElement(@NotNull ParameterInfoContext context) {
        PsiElement element = context.getFile().findElementAt(context.getOffset());
        if (element == null) {
            return null;
        }
        PsiElement prevElement = context.getFile().findElementAt(context.getOffset() - 1);
        return findElement(element, prevElement);
    }

    @Nullable
    public static Object findElement(@NotNull PsiElement element, @Nullable PsiElement prevElement) {
        // If the element at offset is ), that means we are at the end of a parameter list. Ex:- setData(name,|)
        // So in that case, we get the element at the "offset - 1". Ex:- setData(name|,)
        // This will allow us to identify the correct ExpressionListNode element.
        if (")".equals(element.getText())) {
            PsiElement parent = element.getParent();
            // If the parent is an ActionInvocationNode, Connector will be resolved instead of the action if we get
            // the FunctionReferenceNode here. We need to get the FunctionReferenceNode because otherwise the nested
            // function
            // invocations will not show proper parameters.
            if (parent != null && !(parent instanceof ActionInvocationNode)) {
                FunctionReferenceNode functionReferenceNode = PsiTreeUtil.getChildOfType(parent,
                        FunctionReferenceNode.class);
                if (functionReferenceNode != null) {
                    return functionReferenceNode;
                }
                ConnectorReferenceNode connectorReferenceNode = PsiTreeUtil.getChildOfType(parent,
                        ConnectorReferenceNode.class);
                if (connectorReferenceNode != null) {
                    return connectorReferenceNode;
                }
                if (parent instanceof StatementNode) {
                    PsiElement firstChild = parent.getFirstChild();
                    PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(firstChild);
                    if (prevVisibleLeaf == null || !"(".equals(prevVisibleLeaf.getText())) {
                        if (firstChild instanceof PsiErrorElement) {
                            return PsiTreeUtil.findChildOfType(firstChild, IdentifierPSINode.class);
                        }
                        return prevVisibleLeaf;
                    } else if ("(".equals(prevVisibleLeaf.getText())) {
                        return PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
                    } else {
                        return prevVisibleLeaf.getParent().getFirstChild();
                    }
                }
            }
            // If a FunctionReferenceNode is not found, get the element at previous offset. This will most probably
            // contain "(".
            if (prevElement != null) {
                element = prevElement;
            }
        }
        PsiElement node = PsiTreeUtil.getParentOfType(element, ExpressionListNode.class);
        // ExpressionListNode can be null if there are no arguments provided.
        if (node != null) {
            return node;
        }
        // So we check return the FunctionInvocationNode in that case.
        node = PsiTreeUtil.getParentOfType(element, FunctionInvocationNode.class);
        if (node != null) {
            return node;
        }
        // If FunctionInvocationNode is null, we check return the ActionInvocationNode in that case.
        node = PsiTreeUtil.getParentOfType(element, ActionInvocationNode.class);
        if (node != null) {
            return node;
        }
        // If ActionInvocationNode is null, we check return the ConnectorInitNode in that case.
        node = PsiTreeUtil.getParentOfType(element, InvocationNode.class);
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
        if (prevElement != null && prevElement.getText().matches("[{,]") || prevElement instanceof LeafPsiElement) {
            return resolve(prevElement);
        }
        return null;
    }

    @Nullable
    private static PsiElement resolve(@NotNull PsiElement element) {
        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(element);
        if (prevVisibleLeaf != null) {
            if ("(".equals(prevVisibleLeaf.getText())) {
                PsiElement functionName = prevVisibleLeaf.getPrevSibling();
                if (functionName != null) {
                    return functionName;
                }
                return PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
            }
            return resolve(prevVisibleLeaf);
        }
        PsiElement prevSibling = element.getPrevSibling();
        if (prevSibling == null) {
            return null;
        }
        if ("(".equals(prevSibling.getText())) {
            PsiElement functionName = prevSibling.getPrevSibling();
            if (functionName != null) {
                return functionName;
            }
            return PsiTreeUtil.prevVisibleLeaf(prevSibling);
        }
        return resolve(prevSibling);
    }

    @Override
    public void showParameterInfo(@NotNull Object element, @NotNull CreateParameterInfoContext context) {
        // This method will be called with the return object of the findElementForParameterInfo(). If it is null,
        // this method will not be called.
        // Since we know the type, we check and cast the object.
        PsiElement currentElement = null;
        PsiElement parentElement = null;

        List<PsiElement> list = getParameters(element);

        if (element instanceof ExpressionListNode) {
            ExpressionListNode expressionListNode = (ExpressionListNode) element;
            // We need to get the ExpressionListNode parent of current ExpressionListNode.
            // Current ExpressionListNode - "WSO2"
            // Parent ExpressionListNode - setName("WSO2")
            // By doing this, we get the function name because setName("WSO2") is also a ExpressionNode.
            PsiElement parent = PsiTreeUtil.getParentOfType(expressionListNode, FunctionInvocationNode.class);
            // If the parent is null, that means there is no parent ExpressionListNode. That can happen if the parent
            // node is a FunctionInvocationNode.
            if (parent == null) {
                // So if the parent is null, we consider the FunctionInvocationNode as the parent node.
                parent = PsiTreeUtil.getParentOfType(expressionListNode, ExpressionNode.class);
            }
            if (parent == null) {
                parent = PsiTreeUtil.getParentOfType(expressionListNode, InvocationNode.class);
            }
            if (parent == null) {
                // So if the parent is null, we consider the ActionInvocationNode as the parent node.
                parent = PsiTreeUtil.getParentOfType(expressionListNode, ActionInvocationNode.class);
            }
            if (parent == null) {
                // So if the parent is null, we consider the ActionInvocationNode as the parent node.
                parent = PsiTreeUtil.getParentOfType(expressionListNode, ConnectorInitNode.class);
            }
            if (parent == null) {
                // So if the parent is null, we consider the ExpressionListNode as the parent node.
                parent = PsiTreeUtil.getParentOfType(expressionListNode, ExpressionListNode.class);
            }
            if (parent == null) {
                parent = expressionListNode;
            }
            currentElement = expressionListNode;
            parentElement = parent;
        } else if (element instanceof FunctionInvocationNode) {
            FunctionInvocationNode functionInvocationNode = (FunctionInvocationNode) element;
            currentElement = functionInvocationNode;
            parentElement = functionInvocationNode;
        } else if (element instanceof ActionInvocationNode) {
            ActionInvocationNode actionInvocationNode = (ActionInvocationNode) element;
            currentElement = actionInvocationNode;
            parentElement = actionInvocationNode;
        } else if (element instanceof ConnectorInitNode) {
            ConnectorInitNode connectorInitNode = (ConnectorInitNode) element;
            currentElement = connectorInitNode;
            parentElement = connectorInitNode;
        } else if (element instanceof ExpressionNode) {
            ExpressionNode expressionNode = (ExpressionNode) element;
            currentElement = expressionNode;
            parentElement = expressionNode;
        } else if (element instanceof NameReferenceNode) {
            NameReferenceNode nameReferenceNode = (NameReferenceNode) element;
            currentElement = nameReferenceNode;
            parentElement = nameReferenceNode;
        } else if (element instanceof FunctionReferenceNode) {
            FunctionReferenceNode functionReferenceNode = (FunctionReferenceNode) element;
            currentElement = functionReferenceNode;
            parentElement = functionReferenceNode;
        } else if (element instanceof ConnectorReferenceNode) {
            ConnectorReferenceNode connectorReferenceNode = (ConnectorReferenceNode) element;
            currentElement = connectorReferenceNode;
            parentElement = connectorReferenceNode;
        } else if (element instanceof IdentifierPSINode) {
            IdentifierPSINode identifier = (IdentifierPSINode) element;
            currentElement = identifier;
            parentElement = identifier;
        } else if (element instanceof InvocationNode) {
            InvocationNode invocationNode = (InvocationNode) element;
            currentElement = invocationNode;
            parentElement = invocationNode;
        }

        PsiElement namedIdentifierDefNode = getNameIdentifierDefinitionNode(parentElement);
        PsiElement nameIdentifier = getNameIdentifier(currentElement, namedIdentifierDefNode);

        if (currentElement == null || nameIdentifier == null || !(nameIdentifier instanceof IdentifierPSINode)) {
            return;
        }

        if (list == null) {
            list = new LinkedList<>();
        }

        // Sometimes we might not be able to resolve elements. In that case, we should not show "No parameters"
        // message. To identify this situation, we use this variable.
        // If there are no items to show, set a custom object. Otherwise set the list as an array.
        if (list.isEmpty() && canResolve(nameIdentifier)) {
            // Todo - change how to identify no parameter situation
            context.setItemsToShow(new Object[]{"Empty"});
        } else {
            context.setItemsToShow(list.toArray(new PsiElement[list.size()]));
        }
        context.showHint(currentElement, currentElement.getTextRange().getStartOffset(), this);
    }

    /**
     * Returns the parameter list for the given element.
     *
     * @param element
     * @return
     */
    @NotNull
    public static List<PsiElement> getParameters(@NotNull Object element) {
        List<PsiElement> list = new LinkedList<>();
        if (element instanceof ExpressionListNode) {
            ExpressionListNode expressionListNode = (ExpressionListNode) element;
            // We need to get the ExpressionListNode parent of current ExpressionListNode.
            // Current ExpressionListNode - "WSO2"
            // Parent ExpressionListNode - setName("WSO2")

            PsiElement parent = PsiTreeUtil.getParentOfType(expressionListNode, FunctionInvocationNode.class);
            if (parent == null) {
                // So if the parent is null, we consider the ActionInvocationNode as the parent node.
                parent = PsiTreeUtil.getParentOfType(expressionListNode, ActionInvocationNode.class);
            }
            if (parent == null) {
                parent = PsiTreeUtil.getParentOfType(expressionListNode, InvocationNode.class);
            }

            if (parent == null) {
                // So if the parent is null, we consider the ActionInvocationNode as the parent node.
                parent = PsiTreeUtil.getParentOfType(expressionListNode, ConnectorInitNode.class);
            }

            if (parent == null) {
                // So if the parent is null, we consider the FunctionInvocationNode as the parent node.
                parent = PsiTreeUtil.getParentOfType(expressionListNode, ExpressionNode.class);
            }
            // If the parent is null, that means there is no parent ExpressionListNode. That can happen if the parent
            // node is a FunctionInvocationNode.

            if (parent == null) {
                // So if the parent is null, we consider the ExpressionListNode as the parent node.
                parent = PsiTreeUtil.getParentOfType(expressionListNode, ExpressionListNode.class);
            }

            if (parent == null) {
                parent = expressionListNode;
            }
            list = getItemsToShow(expressionListNode, parent);
        } else if (element instanceof FunctionInvocationNode) {
            FunctionInvocationNode functionInvocationNode = (FunctionInvocationNode) element;
            list = getItemsToShow(functionInvocationNode, functionInvocationNode);
        } else if (element instanceof ActionInvocationNode) {
            ActionInvocationNode actionInvocationNode = (ActionInvocationNode) element;
            list = getItemsToShow(actionInvocationNode, actionInvocationNode);
        } else if (element instanceof ConnectorInitNode) {
            ConnectorInitNode connectorInitNode = (ConnectorInitNode) element;
            list = getItemsToShow(connectorInitNode, connectorInitNode);
        } else if (element instanceof ExpressionNode) {
            ExpressionNode expressionNode = (ExpressionNode) element;
            list = getItemsToShow(expressionNode, expressionNode);
        } else if (element instanceof NameReferenceNode) {
            NameReferenceNode nameReferenceNode = (NameReferenceNode) element;
            list = getItemsToShow(nameReferenceNode, nameReferenceNode);
        } else if (element instanceof FunctionReferenceNode) {
            FunctionReferenceNode functionReferenceNode = (FunctionReferenceNode) element;
            list = getItemsToShow(functionReferenceNode, functionReferenceNode);
        } else if (element instanceof ConnectorReferenceNode) {
            ConnectorReferenceNode connectorReferenceNode = (ConnectorReferenceNode) element;
            list = getItemsToShow(connectorReferenceNode, connectorReferenceNode);
        } else if (element instanceof IdentifierPSINode) {
            IdentifierPSINode identifier = (IdentifierPSINode) element;
            list = getItemsToShow(identifier, identifier);
        } else if (element instanceof InvocationNode) {
            InvocationNode invocationNode = (InvocationNode) element;
            list = getItemsToShow(invocationNode, invocationNode);
        }
        return list;
    }

    /**
     * Returns an list of {@link ParameterListNode} which corresponds to the given element and parent element.
     *
     * @param element
     * @param parent
     * @return
     */
    private static List<PsiElement> getItemsToShow(PsiElement element, PsiElement parent) {
        // This method can be a overloaded method. So there can be multiple signatures. To store all of these, we
        // create a list.
        List<PsiElement> list = new ArrayList<>();
        // Function name will be at NameReferenceNode. So we search for this child node.
        PsiElement namedIdentifierDefNode = getNameIdentifierDefinitionNode(parent);
        PsiElement nameIdentifier = getNameIdentifier(element, namedIdentifierDefNode);
        if (nameIdentifier == null || !(nameIdentifier instanceof IdentifierPSINode)) {
            return list;
        }

        PsiReference reference = nameIdentifier.findReferenceAt(nameIdentifier.getTextLength());
        if (reference != null) {
            // Resolve the reference
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement != null) {
                // Resolved element will be the identifier of the function node. So we get the parent
                // node (FunctionDefinitionNode).
                PsiElement definitionNode = resolvedElement.getParent();

                if (definitionNode instanceof ParameterNode) {
                    boolean isLambda = BallerinaPsiImplUtil.isALambdaFunction(resolvedElement);
                    if (isLambda) {
                        ParameterTypeNameList parameterTypeNameList = PsiTreeUtil.findChildOfType(definitionNode,
                                ParameterTypeNameList.class);
                        if (parameterTypeNameList != null) {
                            list.add(parameterTypeNameList);
                        }
                    }
                } else {
                    // Since we need the ParameterListNode, search for ParameterListNode child node.
                    ParameterListNode parameterListNode =
                            PsiTreeUtil.findChildOfType(definitionNode, ParameterListNode.class);
                    // Add to the list if the result is not null.
                    if (parameterListNode != null) {
                        list.add(parameterListNode);
                    }
                }
            }
        }
        return list;
    }

    private static PsiElement getNameIdentifierDefinitionNode(PsiElement parent) {
        PsiElement namedIdentifierDefNode = null;
        if (parent instanceof ExpressionListNode || parent instanceof ExpressionNode) {
            namedIdentifierDefNode = PsiTreeUtil.findChildOfType(parent, NameReferenceNode.class);
        } else if (parent instanceof FunctionInvocationNode) {
            namedIdentifierDefNode = PsiTreeUtil.findChildOfType(parent, FunctionReferenceNode.class);
        } else if (parent instanceof NameReferenceNode || parent instanceof FunctionReferenceNode
                || parent instanceof ConnectorReferenceNode || parent instanceof ActionInvocationNode
                || parent instanceof IdentifierPSINode) {
            namedIdentifierDefNode = parent;
        } else if (parent instanceof ConnectorInitNode) {
            namedIdentifierDefNode = PsiTreeUtil.findChildOfType(parent, ConnectorReferenceNode.class);
        } else if (parent instanceof InvocationNode) {
            AnyIdentifierNameNode anyIdentifierNameNode = PsiTreeUtil.getChildOfType(parent,
                    AnyIdentifierNameNode.class);
            if (anyIdentifierNameNode != null) {
                namedIdentifierDefNode = PsiTreeUtil.findChildOfType(anyIdentifierNameNode, IdentifierPSINode.class);
            }
        }
        return namedIdentifierDefNode;
    }

    @Nullable
    private static PsiElement getNameIdentifier(PsiElement element, PsiElement namedIdentifierDefNode) {
        PsiElement nameIdentifier = null;
        if (element instanceof IdentifierPSINode) {
            nameIdentifier = element;
        }
        if (namedIdentifierDefNode != null && namedIdentifierDefNode instanceof IdentifierDefSubtree) {
            // Get the identifier of this node.
            nameIdentifier = ((IdentifierDefSubtree) namedIdentifierDefNode).getNameIdentifier();
        }
        if (namedIdentifierDefNode instanceof InvocationNode) {
            AnyIdentifierNameNode anyIdentifierNameNode = PsiTreeUtil.getChildOfType(namedIdentifierDefNode,
                    AnyIdentifierNameNode.class);
            if (anyIdentifierNameNode != null) {
                nameIdentifier = PsiTreeUtil.getChildOfType(anyIdentifierNameNode, IdentifierPSINode.class);
            }
        }
        if (namedIdentifierDefNode instanceof IdentifierPSINode) {
            nameIdentifier = namedIdentifierDefNode;
        }
        return nameIdentifier;
    }

    private boolean canResolve(@NotNull PsiElement identifier) {
        PsiReference reference = identifier.findReferenceAt(identifier.getTextLength());
        if (reference == null) {
            return false;
        }
        PsiElement resolvedElement = reference.resolve();
        return resolvedElement != null;
    }

    @Nullable
    @Override
    public Object findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        return findElement(context);
    }

    @Override
    public void updateParameterInfo(@NotNull Object o, @NotNull UpdateParameterInfoContext context) {
        int index = getCurrentParameterIndex(o, context.getOffset());
        if (index != -1) {
            context.setCurrentParameter(index);
        }
    }

    public static int getCurrentParameterIndex(@NotNull Object o, int offset) {
        // This method updates parameter index node. This will be used to highlight the current parameter in the
        // parameter popup.
        PsiElement element;
        // If the object is a type of ExpressionListNode, cast the object.
        if (o instanceof ExpressionListNode) {
            element = ((ExpressionListNode) o);
        } else if (o instanceof FunctionReferenceNode) {
            PsiElement parent = ((FunctionReferenceNode) o).getParent();
            ExpressionListNode expressionListNode = PsiTreeUtil.getChildOfType(parent, ExpressionListNode.class);
            if (expressionListNode == null) {
                return 0;
            }
            PsiElement[] children = expressionListNode.getChildren();
            return children.length / 2;
        } else if (o instanceof ConnectorReferenceNode) {
            PsiElement parent = ((ConnectorReferenceNode) o).getParent();
            ExpressionListNode expressionListNode = PsiTreeUtil.getChildOfType(parent, ExpressionListNode.class);
            if (expressionListNode == null) {
                return 0;
            }
            PsiElement[] children = expressionListNode.getChildren();
            return children.length / 2;
        } else if (o instanceof IdentifierPSINode) {
            StatementNode statementNode = PsiTreeUtil.getParentOfType((IdentifierPSINode) o, StatementNode.class);
            if (statementNode == null) {
                return 0;
            }
            PsiFile containingFile = statementNode.getContainingFile();
            PsiElement elementAtOffset = containingFile.findElementAt(offset);
            if (elementAtOffset == null) {
                return 0;
            }
            int count = 0;
            int commas = 0;
            PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(elementAtOffset);
            do {
                if (prevVisibleLeaf != null) {
                    if (prevVisibleLeaf.getText().matches("[,]")) {
                        count++;
                        commas++;
                    } else if (prevVisibleLeaf.getText().matches("[}]")) {
                        count++;
                    } else if (prevVisibleLeaf.getText().matches("[{]")) {
                        count = count - commas;
                    } else if ("(".equals(prevVisibleLeaf.getText())) {
                        break;
                    }
                    prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
                }
            } while (prevVisibleLeaf != null);
            return count;
        } else {
            return 0;
        }

        if (!(o instanceof PsiElement)) {
            return -1;
        }
        // Get the element at offset.
        PsiElement psiElement = ((PsiElement) o).getContainingFile().findElementAt(offset);
        if (psiElement == null) {
            return -1;
        }
        // Get the child nodes of element.
        PsiElement[] children = element.getChildren();
        // If there are no child nodes, set current parameter to 0 and return.
        if (children.length == 0) {
            return 0;
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
                return index;
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
        return index;
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
        // context.getItemsToShow() in showParameterInfo method.
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
        } else if (p instanceof ParameterTypeNameList) {
            // Caste the object.
            ParameterTypeNameList element = (ParameterTypeNameList) p;
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
        Collection<ParameterNode> parameterNodes = PsiTreeUtil.getChildrenOfTypeAsList(node, ParameterNode.class);
        for (ParameterNode parameterNode : parameterNodes) {
            // Parameters might have spaces in between. So we need to remove them as well.
            params.add(formatParameter(parameterNode.getText()));
        }
        return params;
    }

    public static List<String> getParameterPresentations(ParameterTypeNameList node) {
        List<String> params = new LinkedList<>();
        if (node == null) {
            return params;
        }
        // Get type name nodes.
        Collection<ParameterTypeName> parameterTypeNames = PsiTreeUtil.findChildrenOfType(node,
                ParameterTypeName.class);
        for (ParameterTypeName parameterTypeName : parameterTypeNames) {
            TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(parameterTypeName, TypeNameNode.class);
            if (typeNameNode != null) {
                params.add(parameterTypeName.getText());
            }
        }
        return params;
    }

    /**
     * Removes excess spaces in the provided string. This is used to format parameters and types.
     *
     * @param text text to be formatted
     * @return formatted string.
     */
    public static String formatParameter(String text) {
        return text.trim().replaceAll("\\s+", " ").replaceAll("( )?\\[ ]", "[]");
    }
}
