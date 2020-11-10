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
package io.ballerina.toml.internal.parser.tree;

import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.Token;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a terminal node in the internal syntax tree.
 * <p>
 * Always Minutiae is associated with a terminal node.
 *
 * @since 2.0.0
 */
public class STToken extends STNode {
    protected final STNode leadingMinutiae;
    protected final STNode trailingMinutiae;

    // Number of preceding tokens whose lookahead reached lexeme
    protected final int lookback = 1; // TODO These is a default number
    // Number of characters read beyond lexeme
    protected final int lookahead = 1; // TODO These is a default number

    STToken(SyntaxKind kind, STNode leadingMinutiae, STNode trailingMinutiae) {
        this(kind, kind.stringValue().length(), leadingMinutiae, trailingMinutiae);
    }

    STToken(SyntaxKind kind, int width, STNode leadingMinutiae, STNode trailingMinutiae) {
        this(kind, width, leadingMinutiae, trailingMinutiae, Collections.emptyList());
    }

    STToken(SyntaxKind kind,
            int width,
            STNode leadingMinutiae,
            STNode trailingMinutiae,
            Collection<STNodeDiagnostic> diagnostics) {
        super(kind, diagnostics);
        this.leadingMinutiae = leadingMinutiae;
        this.trailingMinutiae = trailingMinutiae;

        this.width = width;
        this.widthWithLeadingMinutiae = this.width + leadingMinutiae.width;
        this.widthWithTrailingMinutiae = this.width + trailingMinutiae.width;
        this.widthWithMinutiae = this.width + leadingMinutiae.width + trailingMinutiae.width;
    }

    public String text() {
        return kind.stringValue();
    }

    public STNode leadingMinutiae() {
        return leadingMinutiae;
    }

    public STNode trailingMinutiae() {
        return trailingMinutiae;
    }

    public int lookbackTokenCount() {
        return lookback;
    }

    public STToken firstToken() {
        return this;
    }

    public STToken lastToken() {
        return this;
    }

    public STToken modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STToken(this.kind, this.width, this.leadingMinutiae, this.trailingMinutiae, diagnostics);
    }

    public STToken modifyWith(STNode leadingMinutiae, STNode trailingMinutiae) {
        return new STToken(this.kind, this.width, leadingMinutiae, trailingMinutiae, this.diagnostics);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new Token(this, position, parent);
    }

    @Override
    public void accept(STNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(STNodeTransformer<T> transformer) {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return leadingMinutiae + kind.stringValue() + trailingMinutiae;
    }

    @Override
    public void writeTo(StringBuilder builder) {
        leadingMinutiae.writeTo(builder);
        builder.append(kind.stringValue());
        trailingMinutiae.writeTo(builder);
    }
}
