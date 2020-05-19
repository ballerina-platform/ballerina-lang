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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.Token;

/**
 * Represents a terminal node in the internal syntax tree.
 * <p>
 * Always Minutiae is associated with a terminal node.
 *
 * @since 2.0.0
 */
public class STToken extends STNode {

    public final STNode leadingTrivia;
    public final STNode trailingTrivia;

    // Number of preceding tokens whose lookahead reached lexeme
    public final int lookback = 1; // TODO These is a default number
    // Number of characters read beyond lexeme
    public final int lookahead = 1; // TODO These is a default number

    STToken(SyntaxKind kind, STNode leadingTrivia, STNode trailingTrivia) {
        this(kind, kind.stringValue().length(), leadingTrivia, trailingTrivia);
    }

    STToken(SyntaxKind kind, int width, STNode leadingTrivia, STNode trailingTrivia) {
        super(kind);
        this.leadingTrivia = leadingTrivia;
        this.trailingTrivia = trailingTrivia;

        this.width = width;
        this.widthWithLeadingMinutiae = this.width + leadingTrivia.width;
        this.widthWithTrailingMinutiae = this.width + trailingTrivia.width;
        this.widthWithMinutiae = this.width + leadingTrivia.width + trailingTrivia.width;

    }

    public String text() {
        return kind.stringValue();
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new Token(this, position, parent);
    }

    @Override
    public String toString() {
        return leadingTrivia + kind.stringValue() + trailingTrivia;
    }
}
