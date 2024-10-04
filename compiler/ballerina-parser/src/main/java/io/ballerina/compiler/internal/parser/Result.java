/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.internal.parser;

import java.util.ArrayDeque;
import java.util.Optional;

/**
 * Represent a result of a token-sequence-search in a sub-tree. The result will contain the fixes required to
 * traverse in that sub-tree, and the number of matching tokens it found, without the fixed tokens.
 *
 * @since 2.0.0
 */
public class Result {

    /**
     * Number of tokens with successful matches.
     */
    protected int matches;

    /**
     * Number of remove fixes in the result.
     */
    protected int removeFixes;

    /**
     * List of solutions to recover from the error.
     */
    private final ArrayDeque<AbstractParserErrorHandler.Solution> fixes;

    /**
     * Represent the end solution to be applied to the next immediate token, to recover from the error.
     * If the solution is to insert/remove next immediate token, then this is equivalent to the
     * <code>fixes.peek()</code>. Else, if the solution is to insert/remove a token that is not the
     * immediate next token, then this will have a solution with {@link AbstractParserErrorHandler.Action#KEEP}
     * as the action.
     */
    protected AbstractParserErrorHandler.Solution solution;

    public Result(ArrayDeque<AbstractParserErrorHandler.Solution> fixes, int matches) {
        this.fixes = fixes;
        this.matches = matches;
    }

    protected Optional<AbstractParserErrorHandler.Solution> peekFix() {
        return Optional.ofNullable(this.fixes.peek());
    }

    protected AbstractParserErrorHandler.Solution popFix() {
        AbstractParserErrorHandler.Solution sol = this.fixes.pop();
        if (sol.action == AbstractParserErrorHandler.Action.REMOVE) {
            removeFixes--;
        }
        return sol;
    }

    protected void pushFix(AbstractParserErrorHandler.Solution sol) {
        if (sol.action == AbstractParserErrorHandler.Action.REMOVE) {
            removeFixes++;
        }
        this.fixes.push(sol);
    }

    protected int fixesSize() {
        return this.fixes.size();
    }
}
