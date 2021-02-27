/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser.incremental;

import io.ballerina.compiler.internal.parser.BallerinaLexer;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.internal.parser.utils.PersistentStack;
import io.ballerina.compiler.internal.syntax.SyntaxUtils;

/**
 * Represents a {@code STNode} retrieved either from the old syntax tree or from the new text document.
 *
 * @since 1.3.0
 */
class HybridNode {
    static final HybridNode EMPTY = new HybridNode(null, null);
    private final State state;
    private final STNode node;

    HybridNode(STNode node, State state) {
        this.node = node;
        this.state = state;
    }

    State state() {
        return state;
    }

    /**
     * Returns a {@code STToken} value if the underlying node is a token, otherwise null.
     *
     * @return {@code STToken} value if the underlying node is a token, otherwise null
     */
    STToken token() {
        return SyntaxUtils.isToken(node) ? (STToken) node : null;
    }

    STNode subtree() {
        return SyntaxUtils.isNonTerminalNode(node) ? node : null;
    }

    /**
     * Marks this {@code HybridNode} as a subtree or a token.
     *
     * @since 1.3.0
     */
    enum Kind {
        /**
         * Represents subtree retrieved from the old syntax tree.
         */
        SUBTREE,
        /**
         * Represents a token retrived either from the old syntax tree
         * or from the new text document.
         */
        TOKEN;
    }

    /**
     * Captures the source from which the {@code STNode} is retrieved.
     * <p>
     * Used primarily in debugging and debug logs.
     *
     * @since 1.3.0
     */
    enum Source {
        OLD_SYNTAX_TREE,
        NEW_TEXT_DOCUMENT;
    }

    /**
     * Captures the state that this {@code HybridNode} was at when it was created.
     *
     * @since 1.3.0
     */
    static class State {
        int oldTextOffset;
        int newTextOffset;
        BallerinaLexer lexer;
        NodePointer oldTreePtr;
        PersistentStack<TextEditRange> textEditRanges;

        State(int oldTextOffset,
              int newTextOffset,
              BallerinaLexer lexer,
              NodePointer oldTreePtr,
              PersistentStack<TextEditRange> textEditRanges) {
            this.oldTextOffset = oldTextOffset;
            this.newTextOffset = newTextOffset;
            this.lexer = lexer;
            this.oldTreePtr = oldTreePtr;
            this.textEditRanges = textEditRanges;
        }

        State cloneState() {
            return new State(oldTextOffset, newTextOffset, lexer,
                    oldTreePtr.clonePointer(), textEditRanges);
        }
    }
}
