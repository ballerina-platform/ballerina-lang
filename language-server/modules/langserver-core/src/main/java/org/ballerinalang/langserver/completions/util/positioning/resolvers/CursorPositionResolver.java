/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util.positioning.resolvers;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Cursor position resolver interface.
 */
public abstract class CursorPositionResolver {

    /**
     * Check whether the cursor is positioned before the given node start.
     * @param nodePosition          Position of the node
     * @param treeVisitor           {@link TreeVisitor} current tree visitor instance
     * @param completionContext     Completion operation context
     * @param node                  Node
     * @param bSymbol               Node's Symbol
     * @return {@link Boolean}      Whether the cursor is before the node start or not
     */
    public abstract boolean isCursorBeforeNode(DiagnosticPos nodePosition, TreeVisitor treeVisitor,
                                               LSContext completionContext, BLangNode node, BSymbol bSymbol);

    /**
     * Check whether the cursor is within the invocation arguments.
     *
     * @param node BLang node to evaluate
     * @param cursorLine Cursor line
     * @param cursorCol Cursor column
     * @param lsContext Language server operation context
     * @return {@link Boolean} Within the args or not
     */
    boolean withinInvocationArguments(BLangNode node, int cursorLine, int cursorCol, LSContext lsContext) {
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
        List<BLangInvocation> invocationArgs = getInvocationArgumentExpressions(node);

        for (BLangInvocation invocation : invocationArgs) {
            DiagnosticPos diagnosticPos = CommonUtil.toZeroBasedPosition(invocation.getPosition());
            int sLine = diagnosticPos.sLine;
            int sCol = diagnosticPos.sCol;
            int eCol = diagnosticPos.eCol;
            if (cursorLine > sLine || (cursorLine == sLine && sCol < cursorCol && eCol > cursorCol)) {
                return false;
            }
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
     * Get the arguments which are invocations.
     *
     * @param node BLangNode to evaluate. This node always an invocation or and invocation expression
     * @return {@link List} List of invocation args
     */
    private List<BLangInvocation> getInvocationArgumentExpressions(BLangNode node) {
        BLangInvocation invocation;
        if (node instanceof BLangInvocation) {
            invocation = (BLangInvocation) node;
        } else {
            invocation = ((BLangInvocation) ((BLangExpressionStmt) node).expr);
        }

        return invocation.argExprs.stream()
                .filter(bLangExpression -> bLangExpression instanceof BLangInvocation)
                .map(bLangExpression -> (BLangInvocation) bLangExpression)
                .collect(Collectors.toList());
    }
}
