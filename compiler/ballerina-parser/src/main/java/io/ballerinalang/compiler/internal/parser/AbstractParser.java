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

import io.ballerinalang.compiler.internal.parser.AbstractParserErrorHandler.Action;
import io.ballerinalang.compiler.internal.parser.AbstractParserErrorHandler.Solution;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
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
        return peek(1).kind == tokenKind ? peek(2) : peek(1);
    }
}
