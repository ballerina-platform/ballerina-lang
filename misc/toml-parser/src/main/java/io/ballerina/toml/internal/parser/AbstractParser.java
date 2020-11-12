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
package io.ballerina.toml.internal.parser;


import io.ballerina.toml.internal.diagnostics.DiagnosticCode;
import io.ballerina.toml.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.toml.internal.parser.AbstractParserErrorHandler.Action;
import io.ballerina.toml.internal.parser.AbstractParserErrorHandler.Solution;
import io.ballerina.toml.internal.parser.tree.STNode;
import io.ballerina.toml.internal.parser.tree.STNodeList;
import io.ballerina.toml.internal.parser.tree.STToken;
import io.ballerina.toml.internal.syntax.NodeListUtils;
import io.ballerina.toml.syntax.tree.SyntaxKind;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * @since 2.0.0
 */
public abstract class AbstractParser {

    protected final AbstractParserErrorHandler errorHandler;
    protected final AbstractTokenReader tokenReader;
    private final Deque<InvalidNodeInfo> invalidNodeInfoStack = new ArrayDeque<>(5);
    protected STToken insertedToken = null;

    public AbstractParser(AbstractTokenReader tokenReader, AbstractParserErrorHandler errorHandler) {
        this.tokenReader = tokenReader;
        this.errorHandler = errorHandler;
    }

    public AbstractParser(AbstractTokenReader tokenReader) {
        this.tokenReader = tokenReader;
        this.errorHandler = null;
    }

    public abstract STNode parse();

    protected STToken peek() {
        if (this.insertedToken != null) {
            return this.insertedToken;
        }

        return this.tokenReader.peek();
    }

    protected STToken peek(int k) {
        if (this.insertedToken == null) {
            return this.tokenReader.peek(k);
        }

        if (k == 1) {
            return this.insertedToken;
        }

        if (k > 0) {
            k = k - 1;
        }

        return this.tokenReader.peek(k);
    }

    protected STToken consume() {
        if (this.insertedToken != null) {
            STToken nextToken = this.insertedToken;
            this.insertedToken = null;
            return nextToken;
        }

        if (invalidNodeInfoStack.isEmpty()) {
            return this.tokenReader.read();
        }

        return consumeWithInvalidNodes();
    }

    private STToken consumeWithInvalidNodes() {
        // TODO can we improve this logic by cloning only once with all the invalid tokens?
        STToken token = this.tokenReader.read();
        while (!invalidNodeInfoStack.isEmpty()) {
            InvalidNodeInfo invalidNodeInfo = invalidNodeInfoStack.pop();
            token = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(token, invalidNodeInfo.node,
                    invalidNodeInfo.diagnosticCode, invalidNodeInfo.args);
        }
        return token;
    }

    protected Solution recover(STToken token, ParserRuleContext currentCtx, Object... args) {
        Solution sol = this.errorHandler.recover(currentCtx, token, args);
        // If the action is to remove, then re-parse the same rule.
        if (sol.action == Action.REMOVE) {
            addInvalidTokenToNextToken(sol.removedToken);
        } else if (sol.action == Action.INSERT) {
            this.insertedToken = (STToken) sol.recoveredNode;
        }

        return sol;
    }

    protected void insertToken(SyntaxKind kind) {
        this.insertedToken = SyntaxErrors.createMissingTokenWithDiagnostics(kind);
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
        return peek(2);
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
            return SyntaxErrors.addDiagnostic(target, diagnosticCode);
        }
        return target;
    }

    /**
     * Clones the last node in list with the invalid node as minutiae and update the list.
     *
     * @param nodeList       node list to be updated
     * @param invalidParam   the invalid node to be attached to the last node in list as minutiae
     * @param diagnosticCode diagnostic code related to the invalid node
     */
    protected void updateLastNodeInListWithInvalidNode(List<STNode> nodeList,
                                                       STNode invalidParam,
                                                       DiagnosticCode diagnosticCode) {
        int lastIndex = nodeList.size() - 1;
        STNode prevNode = nodeList.remove(lastIndex);
        STNode newNode = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(prevNode, invalidParam);
        if (diagnosticCode != null) {
            newNode = SyntaxErrors.addDiagnostic(newNode, diagnosticCode);
        }
        nodeList.add(newNode);
    }

    /**
     * Adds the invalid node as minutiae to the next consumed token.
     * <p>
     * This method pushes this invalid node into a stack and attach it
     * as invalid node minutiae to the next token when it is consumed.
     *
     * @param invalidNode    invalid node to added as {@code STInvalidNodeMinutiae}
     * @param diagnosticCode the {@code DiagnosticCode} to be added
     * @param args           additional arguments required to format the diagnostic message
     */
    protected void addInvalidNodeToNextToken(STNode invalidNode, DiagnosticCode diagnosticCode, Object... args) {
        invalidNodeInfoStack.push(new InvalidNodeInfo(invalidNode, diagnosticCode, args));
    }

    /**
     * Adds the invalid node as minutiae to the next consumed token.
     * <p>
     * This method pushes this invalid node into a stack and attach it
     * as invalid node minutiae to the next token when it is consumed.
     *
     * @param invalidNode invalid node to added as {@code STInvalidNodeMinutiae}
     */
    protected void addInvalidTokenToNextToken(STToken invalidNode) {
        invalidNodeInfoStack.push(new InvalidNodeInfo(invalidNode,
                DiagnosticErrorCode.ERROR_INVALID_TOKEN, invalidNode.text()));
    }

    /**
     * Holds invalid node diagnostic information until the next token is consumed.
     *
     * @since 2.0.0
     */
    private static class InvalidNodeInfo {
        final STNode node;
        final DiagnosticCode diagnosticCode;
        final Object[] args;

        public InvalidNodeInfo(STNode invalidNode, DiagnosticCode diagnosticCode, Object... args) {
            this.node = invalidNode;
            this.diagnosticCode = diagnosticCode;
            this.args = args;
        }
    }
}

