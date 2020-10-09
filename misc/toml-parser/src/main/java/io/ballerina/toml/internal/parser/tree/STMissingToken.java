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

import io.ballerina.toml.syntax.tree.IdentifierToken;
import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.Token;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STMissingToken extends STToken {

    STMissingToken(SyntaxKind kind) {
        super(kind, 0, new STNodeList(new ArrayList<>(0)), new STNodeList(new ArrayList<>(0)));
    }

    STMissingToken(SyntaxKind kind, Collection<STNodeDiagnostic> diagnostics) {
        super(kind, 0, new STNodeList(new ArrayList<>(0)), new STNodeList(new ArrayList<>(0)), diagnostics);
    }

    STMissingToken(SyntaxKind kind,
                   STNode leadingMinutiae,
                   STNode trailingMinutiae,
                   Collection<STNodeDiagnostic> diagnostics) {
        super(kind, 0,  leadingMinutiae, trailingMinutiae, diagnostics);
    }

    public STToken modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STMissingToken(this.kind, this.leadingMinutiae, this.trailingMinutiae, diagnostics);
    }

    public STToken modifyWith(STNode leadingMinutiae, STNode trailingMinutiae) {
        return new STMissingToken(this.kind, leadingMinutiae, trailingMinutiae, this.diagnostics);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        switch (kind) {
            case IDENTIFIER_LITERAL:
                return new IdentifierToken(this, position, parent);
            default:
                return new Token(this, position, parent);
        }
    }

    @Override
    public <T> T apply(STNodeTransformer<T> transformer) {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        // TODO for testing purpose only
        return " MISSING[" + kind.stringValue() + "]";
    }

    @Override
    public void writeTo(StringBuilder builder) {
        leadingMinutiae.writeTo(builder);
        trailingMinutiae.writeTo(builder);
    }
}
