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
package io.ballerinalang.compiler.internal.parser.incremental;

import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.utils.PersistentStack;
import io.ballerinalang.compiler.internal.syntax.SyntaxUtils;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.text.TextRange;

/**
 * Contains utility methods to retrieve {@code HybridNode}s from
 * the old syntax tree as well from the new source code.
 *
 * @since 1.3.0
 */
class HybridNodes {

    /**
     * Returns the next node based on the previous node's state and the given kind.
     *
     * @param prevNode previous {@code HybridNode}
     * @param kind     token or a subtree
     * @return the next available node
     */
    static HybridNode nextNode(HybridNode prevNode, HybridNode.Kind kind) {
        HybridNode.State state = prevNode.state().cloneState();
        switch (kind) {
            case TOKEN:
                return nextToken(state);
            case SUBTREE:
                return nextSubtree(state);
            default:
                throw new UnsupportedOperationException("Unsupported HybridNode.Kind: " + kind);
        }
    }

    private static HybridNode nextSubtree(HybridNode.State state) {
        if (state.oldTreePtr.isAtEOF() || state.oldTextOffset != state.newTextOffset) {
            return null;
        }

        Node oldTreeNode = state.oldTreePtr.currentNode();
        while (SyntaxUtils.isNonTerminalNode(oldTreeNode)) {
            if (isNodeReusable(oldTreeNode, state)) {
                // Adjust offsets accordingly
                int width = oldTreeNode.textRangeWithMinutiae().length();
                state.oldTextOffset += width;
                state.newTextOffset += width;
                // Move to the next sibling
                state.oldTreePtr = state.oldTreePtr.nextSibling();
                return new HybridNode(oldTreeNode.internalNode(), state);
            }

            // If the node cannot be reused, then move to the next child of the current node.
            state.oldTreePtr = state.oldTreePtr.nextChild();
            oldTreeNode = state.oldTreePtr.currentNode();
        }
        return null;
    }

    private static HybridNode nextToken(HybridNode.State state) {
        HybridNode hybridNode = null;
        do {
            if (state.oldTreePtr.isAtEOF() || state.oldTextOffset > state.newTextOffset) {
                // There are two situations where we need to lex a token from the new text
                // 1) When we are at the end of the old token stream
                // // TODO 2) When we ...
                // Lex tokens from the new text stream
                hybridNode = tokenFromNewText(state);
            } else if (state.oldTextOffset < state.newTextOffset) {
                // state.oldTextOffset < state.newTextOffset
                // We need to skip a token from the old tree and see weather both offsets are aligned
                syncOldWithNewTextOffset(getTokenFromOldTree(state), state);
            } else {
                // Both oldTextOffset and the newTextOffset are in sync.
                // Let's try to reuse a token from the old tree
                // If we cannot get a token from the old tree. We would have to lex a token from the new text.
                hybridNode = getReusableTokenFromOldTree(state);
            }
        } while (hybridNode == null);
        return hybridNode;
    }

    private static void syncOldWithNewTextOffset(Token token, HybridNode.State state) {
        state.oldTextOffset += token.textRangeWithMinutiae().length();
        state.oldTreePtr = state.oldTreePtr.nextSibling();
        removeInvalidTextEdits(token, state);
    }

    private static HybridNode getReusableTokenFromOldTree(HybridNode.State state) {
        Token token = getTokenFromOldTree(state);
        if (isNodeReusable(token, state)) {
            // Adjust offsets accordingly
            int width = token.textRangeWithMinutiae().length();
            state.oldTextOffset += width;
            state.newTextOffset += width;
            // Move to the next sibling
            state.oldTreePtr = state.oldTreePtr.nextSibling();
            return new HybridNode(token.internalNode(), state);
        } else {
            syncOldWithNewTextOffset(token, state);
            return null;
        }
    }

    private static Token getTokenFromOldTree(HybridNode.State state) {
        if (SyntaxUtils.isToken(state.oldTreePtr.currentNode())) {
            return state.oldTreePtr.currentToken();
        }

        // state.nodePointer points to a NonTerminalNode.
        // Move to the next token in the tree.
        state.oldTreePtr = state.oldTreePtr.nextToken();
        return state.oldTreePtr.currentToken();
    }

    private static void removeInvalidTextEdits(Token oldToken, HybridNode.State state) {
        if (state.textEditRanges.isEmpty()) {
            return;
        }
        int nextOldTokenStartOffset = oldToken.textRangeWithMinutiae().endOffset();
        TextEditRange textEditRange = state.textEditRanges.peek();
        if (nextOldTokenStartOffset < textEditRange.oldEndOffset) {
            return;
        }

        state.textEditRanges = state.textEditRanges.pop();
        state.oldTextOffset += textEditRange.newTextLength - textEditRange.oldLength;
    }

    private static HybridNode tokenFromNewText(HybridNode.State state) {
        state.lexer.reset(state.newTextOffset);
        STToken token = state.lexer.nextToken();
        state.newTextOffset += token.widthWithMinutiae();
        return new HybridNode(token, state);
    }

    private static boolean isNodeReusable(Node node, HybridNode.State state) {
        if (node.internalNode().widthWithMinutiae() == 0) {
            return false;
        }

        return noOverlapWithCurrentTextEdit(node, state.textEditRanges);
    }

    private static boolean noOverlapWithCurrentTextEdit(Node oldNode,
                                                        PersistentStack<TextEditRange> textEditRanges) {
        if (textEditRanges.isEmpty()) {
            return true;
        }

        TextEditRange textEditRange = textEditRanges.peek();
        TextRange oldTokenRange = oldNode.textRangeWithMinutiae();

        // Does not overlap if => editStart < editEnd < oldTokenStart || oldTokenEnd < editStart < editEnd
        // Can be simplified to => editEnd < oldTokenStart || oldTokenEnd < editStart
        return textEditRange.oldEndOffset < oldTokenRange.startOffset() ||
                oldTokenRange.endOffset() < textEditRange.oldStartOffset;
    }
}
