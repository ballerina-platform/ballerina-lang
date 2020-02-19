/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.completions.util;

import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.model.Whitespace;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;


/**
 * Utility methods for Completion Tree Visiting.
 *
 * @since 0.985.0
 */
public class CompletionVisitorUtil {

    private CompletionVisitorUtil() {
    }

    /**
     * Check whether the cursor is located within the given node's block scope.
     * <p>
     * Note: This method should only be used to check and terminate the visitor when the content within the block
     * is empty.
     *
     * @param nodePosition Position of the current node
     * @param symbolEnv    Symbol Environment
     * @param lsContext    Language Server Operation Context
     * @param treeVisitor  Completion tree visitor instance
     * @return {@link Boolean}  Whether the cursor within the block scope
     */
    public static boolean isCursorWithinBlock(DiagnosticPos nodePosition, @Nonnull SymbolEnv symbolEnv,
                                              LSContext lsContext, TreeVisitor treeVisitor) {
        DiagnosticPos zeroBasedPosition = CommonUtil.toZeroBasedPosition(nodePosition);
        int line = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int nodeSLine = zeroBasedPosition.sLine;
        int nodeELine = zeroBasedPosition.eLine;

        if ((nodeSLine <= line && nodeELine >= line)) {
            Map<Name, List<Scope.ScopeEntry>> visibleSymbolEntries = new HashMap<>();
            if (symbolEnv.scope != null) {
                visibleSymbolEntries.putAll(treeVisitor.resolveAllVisibleSymbols(symbolEnv));
            }
            treeVisitor.populateSymbols(visibleSymbolEntries, symbolEnv);
            treeVisitor.forceTerminateVisitor();
            return true;
        }

        return false;
    }

    /**
     * Check whether the cursor is within the service expression list.
     *
     * @param node BLangService node to evaluate
     * @param symbolEnv Current symbol environment
     * @param lsContext Language server completion context
     * @param treeVisitor Tree Visitor
     * @return {@link Boolean} whether the cursor is within the service expression list
     */
    public static boolean cursorWithinServiceExpressionList(BLangService node, @Nonnull SymbolEnv symbolEnv,
                                                            LSContext lsContext, TreeVisitor treeVisitor) {
        Position cursorPos = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int line = cursorPos.getLine();
        int col = cursorPos.getCharacter();
        List<BLangExpression> attachedExprs = node.attachedExprs;
        if (attachedExprs.isEmpty()) {
            return false;
        }
        /*
        If the cursor within the attached expressions we return true
         */
        BLangExpression firstExpr = attachedExprs.get(0);
        BLangExpression lastExpr = CommonUtil.getLastItem(attachedExprs);
        DiagnosticPos firstExprPos = CommonUtil.toZeroBasedPosition(firstExpr.pos);
        int fSLine = firstExprPos.sLine;
        int fSCol = firstExprPos.sCol;
        DiagnosticPos lastExprPos = CommonUtil.toZeroBasedPosition(lastExpr.pos);
        int lSLine = lastExprPos.sLine;
        int lECol = lastExprPos.eCol;

        if (fSLine <= line && lSLine >= line && (fSCol <= col && lECol >= col)) {
            Map<Name, List<Scope.ScopeEntry>> visibleSymbolEntries = new HashMap<>();
            if (symbolEnv.scope != null) {
                visibleSymbolEntries.putAll(treeVisitor.resolveAllVisibleSymbols(symbolEnv));
            }
            treeVisitor.populateSymbols(visibleSymbolEntries, symbolEnv);
            treeVisitor.forceTerminateVisitor();
            return true;
        }
        
        return false;
    }

    /**
     * Check whether the cursor is within the worker return context.
     *
     * @param env Symbol Environment
     * @param lsContext Language server completion Context
     * @param treeVisitor Tree Visitor
     * @param funcNode BLangFunction node
     * @return {@link Boolean} whether the cursor is located within the worker return context
     */
    public static boolean isWithinWorkerReturnContext(SymbolEnv env, LSContext lsContext, TreeVisitor treeVisitor,
                                                      BLangFunction funcNode) {
        Position cursorPosition = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        DiagnosticPos position = CommonUtil.toZeroBasedPosition(funcNode.getPosition());
        ArrayList<Whitespace> whitespaces = new ArrayList<>(funcNode.getWS());
        int cursorLine = cursorPosition.getLine();
        int cursorCol = cursorPosition.getCharacter();
        int sLine = position.sLine;
        int sCol = position.sCol;
        int openBraceStart = cursorCol;
        int counter = 1;
        
        while (true) {
            // start the loop from the first element, where skipping the "worker" token
            if (counter > whitespaces.size() - 1) {
                break;
            }
            Whitespace whitespace = whitespaces.get(counter);
            if (whitespace.getPrevious().equals(CommonKeys.OPEN_BRACE_KEY)) {
                openBraceStart += whitespace.getWs().length();
                break;
            }
            openBraceStart += whitespace.getPrevious().length() + whitespace.getWs().length();
            counter++;
        }
        
        if (cursorLine == sLine && cursorCol > sCol && cursorCol < openBraceStart) {
            lsContext.put(CompletionKeys.IN_WORKER_RETURN_CONTEXT_KEY, true);
            treeVisitor.populateSymbols(treeVisitor.resolveAllVisibleSymbols(env), env);
            treeVisitor.forceTerminateVisitor();
            return true;
        }

        return false;
    }

    /**
     * Check whether the cursor is located within the condition context.
     * Eg: if (_cursor_)
     *     while (_cursor_)
     *
     * @param env Symbol Environment
     * @param lsContext Language server completion Context
     * @param treeVisitor Tree Visitor
     * @param conditionNode Conditional node - if or while node
     * @return {@link Boolean} whether the cursor is located within the condition context
     */
    public static boolean isWithinConditionContext(SymbolEnv env, LSContext lsContext, TreeVisitor treeVisitor,
                                                   BLangNode conditionNode) {
        Position cursorPosition = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        BLangNode expressionNode = null;
        if (conditionNode instanceof BLangIf) {
            expressionNode = ((BLangIf) conditionNode).expr;
        } else if (conditionNode instanceof BLangWhile) {
            expressionNode = ((BLangWhile) conditionNode).expr;
        }
        
        if (expressionNode == null) {
            return false;
        }
        DiagnosticPos exprPosition = CommonUtil.toZeroBasedPosition(expressionNode.pos);
        
        int cursorLine = cursorPosition.getLine();
        int cursorCol = cursorPosition.getCharacter();
        int sLine = exprPosition.sLine;
        int eLine = exprPosition.eLine;
        int sCol = exprPosition.sCol;
        int eCol = exprPosition.eCol;
        
        if ((cursorLine > sLine && cursorLine < eLine)
                || ((cursorLine == sLine || cursorLine == eLine) && cursorCol >= sCol && cursorCol <= eCol)) {
            lsContext.put(CompletionKeys.IN_CONDITION_CONTEXT_KEY, true);
            treeVisitor.populateSymbols(treeVisitor.resolveAllVisibleSymbols(env), env);
            treeVisitor.forceTerminateVisitor();
            return true;
        }
        
        return false;
    }

    /**
     * Get the object's items ordered according to the position.
     *
     * @param objectTypeNode Object type node
     * @return {@link List} List of ordered item nodes
     */
    public static List<BLangNode> getObjectItemsOrdered(BLangObjectTypeNode objectTypeNode) {
        List<BLangNode> nodes = new ArrayList<>();
        if (objectTypeNode == null) {
            return nodes;
        }

        nodes.addAll(objectTypeNode.getFields().stream()
                .map(field -> (BLangNode) field)
                .collect(Collectors.toList()));

        nodes.addAll(objectTypeNode.getFunctions().stream()
                .map(function -> (BLangNode) function)
                .collect(Collectors.toList()));

        if (objectTypeNode.initFunction != null) {
            nodes.add(objectTypeNode.initFunction);
        }

        nodes.sort(Comparator.comparing(node -> node.getPosition().getStartLine()));

        return nodes;
    }

    /**
     * Get the function parameters ordered according to the position.
     *
     * @param function BLangFUnction node
     * @return {@link List} List of ordered item nodes
     */
    public static List<BLangNode> getFunctionParamsOrdered(BLangFunction function) {
        List<BLangNode> nodes = new ArrayList<>();
//        nodes.addAll(function.defaultableParams);
        nodes.addAll(function.requiredParams);
        if (function.restParam != null) {
            nodes.add(function.restParam);
        }
        nodes.sort(Comparator.comparing(node -> node.getPosition().getStartLine()));

        return nodes;
    }

    /**
     * Check whether the cursor is within the invocation arguments.
     *
     * @param node BLang node to evaluate
     * @param lsContext Language server operation context
     * @return {@link Boolean} within the argument
     */
    public static boolean withinInvocationArguments(BLangNode node, LSContext lsContext) {
        Position position = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int cursorLine = position.getLine();
        int cursorCol = position.getCharacter();
        /*
        When the expression statement is a function invocation or invocation is as an argument for a function invocation
        this gets hit and hence the OR condition has been added.
        Eg: exampleFunction(a<cursor>)
            exampleFunction(a, func2(a<cursor>))
         */
        if (!(((node instanceof BLangExpressionStmt) && ((BLangExpressionStmt) node).expr instanceof BLangInvocation)
                || node instanceof BLangInvocation)) {
            return false;
        }

        DiagnosticPos nodePos = CommonUtil.toZeroBasedPosition(node.pos);
        int sLine = nodePos.sLine;
        int eLine = nodePos.eLine;
        int sCol = nodePos.sCol;
        int eCol = nodePos.eCol;

        if ((cursorLine > sLine && cursorLine < eLine)
                || (cursorLine == sLine && sCol < cursorCol && eCol > cursorCol)) {
            lsContext.put(CompletionKeys.IN_INVOCATION_PARAM_CONTEXT_KEY, true);
            return true;
        }
        
        return false;
    }

    /**
     * Check whether the cursor is before the invocation node.
     * Here we consider the completion for complete source as well
     * 
     * @param node BLangNode to process
     * @param context Language server Context
     * @return {@link Boolean} whether the cursor before node
     */
    public static boolean cursorBeforeInvocationNode(BLangNode node, LSContext context) {
        Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int cursorLine = position.getLine();
        int cursorCol = position.getCharacter();
        DiagnosticPos nodePos = CommonUtil.toZeroBasedPosition(node.getPosition());
        int endLine = nodePos.eLine;
        int endCol = nodePos.eCol;
        
        return cursorLine < endLine || (cursorLine == endLine && cursorCol < endCol);
    }
}
