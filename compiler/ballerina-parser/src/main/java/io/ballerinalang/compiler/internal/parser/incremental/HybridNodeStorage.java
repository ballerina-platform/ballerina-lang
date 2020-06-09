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

import io.ballerinalang.compiler.internal.parser.BallerinaLexer;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.utils.PersistentStack;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.text.TextDocumentChange;
import io.ballerinalang.compiler.text.TextEdit;
import io.ballerinalang.compiler.text.TextRange;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintain the list of {@code HybridNode}s retrieved from
 * the old {@code SyntaxTree} and the new source code.
 *
 * @since 1.3.0
 */
public class HybridNodeStorage {

    // TODO improve this implementation
    private final List<HybridNode> hybridNodeList;

    private int consumedNodeIndex = 0;
    private int peekedNodeIndex = 0;

    private HybridNode currentToken = null;
    private HybridNode currentNode = null;

    // TODO Explain why we are adding the initial node
    public HybridNodeStorage(SyntaxTree oldTree,
                             BallerinaLexer lexer,
                             TextDocumentChange textDocumentChange) {
        this.hybridNodeList = new ArrayList<>(20);
        this.hybridNodeList.add(createInitialNode(oldTree.modulePart(), lexer, textDocumentChange));
        consumedNodeIndex++;
        peekedNodeIndex++;
    }

    HybridNode getCurrentToken() {
        return currentToken;
    }

    HybridNode consumeSubtree() {
        HybridNode hybridNode = currentNode;
        this.hybridNodeList.add(consumedNodeIndex++, hybridNode);
        peekedNodeIndex = consumedNodeIndex;
        currentNode = null;
        currentToken = null;
        return hybridNode;
    }

    HybridNode peekSubtree() {
        if (currentNode != null) {
            return currentNode;
        }

        this.currentNode = nextSubtree();
        return currentNode;
    }

    HybridNode consumeToken() {
        if (currentToken == null) {
            currentToken = peekToken();
        }
        HybridNode hybridNode = currentToken;
        currentToken = null;
        consumedNodeIndex++;
        return hybridNode;
    }

    HybridNode peekToken() {
        if (this.currentToken != null) {
            return currentToken;
        }

        if (consumedNodeIndex >= peekedNodeIndex) {
            storeNextToken();
        }

        currentToken = peek(0);
        return currentToken;
    }

    HybridNode peekToken(int k) {
        while (consumedNodeIndex + k >= peekedNodeIndex) {
            storeNextToken();
        }

        return peek(k);
    }

    private HybridNode peek(int k) {
        return this.hybridNodeList.get(consumedNodeIndex + k);
    }

    private HybridNode lastConsumed() {
        return this.hybridNodeList.get(consumedNodeIndex - 1);
    }

    private HybridNode lastStored() {
        return this.hybridNodeList.get(peekedNodeIndex - 1);
    }

    private HybridNode nextSubtree() {
        HybridNode prevHybridNode = lastConsumed();
        return HybridNodes.nextNode(prevHybridNode, HybridNode.Kind.SUBTREE);
    }

    private void storeNextToken() {
        // Previous node's parser state
        HybridNode prevHybridNode = lastStored();
        HybridNode hybridNode = HybridNodes.nextNode(prevHybridNode, HybridNode.Kind.TOKEN);
        storeToken(hybridNode);
    }

    private void storeToken(HybridNode hybridNode) {
        this.hybridNodeList.add(peekedNodeIndex++, hybridNode);
    }

    private HybridNode createInitialNode(ModulePartNode oldTree,
                                         BallerinaLexer lexer,
                                         TextDocumentChange textDocumentChange) {
        NodePointer oldTreePtr = new NodePointer(oldTree);
        PersistentStack<TextEditRange> textEditRanges = markAffectedRanges(oldTree, textDocumentChange);
        HybridNode.State state = new HybridNode.State(0, 0, lexer, oldTreePtr.nextChild(), textEditRanges);
        return new HybridNode(null, state);
    }

    private PersistentStack<TextEditRange> markAffectedRanges(ModulePartNode oldTree,
                                                              TextDocumentChange textDocumentChange) {
        int textEditCount = textDocumentChange.getTextEditCount();
        PersistentStack<TextEditRange> markedTextEdits = PersistentStack.getEmpty();
        for (int index = textEditCount - 1; index >= 0; index--) {
            TextEdit textEdit = textDocumentChange.getTextEdit(index);
            markedTextEdits = markedTextEdits.push(markAffectedRange(oldTree, textEdit));
        }
        return markedTextEdits;
    }

    private TextEditRange markAffectedRange(ModulePartNode oldTree, TextEdit textEdit) {
        TextRange textRange = textEdit.range();
        // Find the affected token in the oldTree
        Token affectedToken = oldTree.findToken(textRange.startOffset());
        // If this is the first toke, then return
        if (affectedToken.textRangeWithMinutiae().startOffset() == 0) {
            return new TextEditRange(textRange.startOffset(), textRange.endOffset(), textEdit.text().length());
        }

        STToken internalToken = (STToken) affectedToken.internalNode();
        for (int lbIndex = 0; lbIndex < internalToken.lookbackTokenCount(); lbIndex++) {
            // Since the common lookback = 1, this loop runs only once
            affectedToken = oldTree.findToken(affectedToken.textRangeWithMinutiae().startOffset() - 1);
        }

        int affectedTokenStartOffset = affectedToken.textRangeWithMinutiae().startOffset();
        int newTextLength = textEdit.text().length() +
                (textRange.startOffset() - affectedTokenStartOffset);
        return new TextEditRange(affectedTokenStartOffset, textRange.endOffset(), newTextLength);
    }
}
