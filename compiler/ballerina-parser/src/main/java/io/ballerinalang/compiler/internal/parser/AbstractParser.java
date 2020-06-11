/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.diagnostics.DiagnosticCode;
import io.ballerinalang.compiler.internal.parser.AbstractParserErrorHandler.Action;
import io.ballerinalang.compiler.internal.parser.AbstractParserErrorHandler.Solution;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeList;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.syntax.NodeListUtils;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

/**
 * @since 2.0.0
 */
public abstract class AbstractParser {

    protected final AbstractParserErrorHandler errorHandler;
    protected final AbstractTokenReader tokenReader;

    public AbstractParser(AbstractTokenReader tokenReader, AbstractParserErrorHandler errorHandler) {
        this.tokenReader = tokenReader;
        this.errorHandler = errorHandler;
    }

    public abstract STNode parse();

    public abstract STNode resumeParsing(ParserRuleContext context, Object... args);

    protected STToken peek() {
        return this.tokenReader.peek();
    }

    protected STToken peek(int k) {
        return this.tokenReader.peek(k);
    }

    protected STToken consume() {
        return this.tokenReader.read();
    }

    protected Solution recover(STToken token, ParserRuleContext currentCtx, Object... args) {
        Solution sol = this.errorHandler.recover(currentCtx, token, args);
        // If the action is to remove, then re-parse the same rule.
        if (sol.action == Action.REMOVE) {
            sol.recoveredNode = resumeParsing(currentCtx, args);
        }
        return sol;
    }

    protected void startContext(ParserRuleContext context) {
        this.errorHandler.startContext(context);
    }

    protected void endContext() {
        this.errorHandler.endContext();
    }

    /**
     * Switch the current context to the provided one. This will replace the
     * existing context.
     *
     * @param context Context to switch to.
     */
    protected void switchContext(ParserRuleContext context) {
        this.errorHandler.switchContext(context);
    }

    protected STToken getNextNextToken(SyntaxKind tokenKind) {
        STToken nextToken = peek(1);
        return nextToken.kind == tokenKind ? peek(2) : nextToken;
    }

    /**
     * Returns 'true' if the list is empty.
     * <p>
     * First check whether this node is an instance of STNodeList.
     *
     * @param node the nodelist instance
     * @return returns 'true' if the list is empty
     */
    protected boolean isNodeListEmpty(STNode node) {
        if (!NodeListUtils.isSTNodeList(node)) {
            throw new IllegalArgumentException("The 'node' should be an instance of STNodeList");
        }

        STNodeList nodeList = (STNodeList) node;
        return nodeList.isEmpty();
    }

    /**
     * Returns a clone of the given STNode with the given diagnostic if the nodeList is empty,
     * otherwise returns the original STNode.
     *
     * @param nodeList the node list instance
     * @param target the STNode instance
     * @param diagnosticCode the DiagnosticCode to be added to the node
     * @return a clone of the given STNode
     */
    protected STNode cloneWithDiagnosticIfListEmpty(STNode nodeList, STNode target, DiagnosticCode diagnosticCode) {
        if (isNodeListEmpty(nodeList)) {
            return errorHandler.addDiagnostics(target, diagnosticCode);
        }
        return target;
    }
}
