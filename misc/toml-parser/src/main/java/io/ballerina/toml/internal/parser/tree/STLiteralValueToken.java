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

import io.ballerina.toml.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a literal value in the Ballerina internal syntax tree.
 *
 * @since 2.0.0
 */
public class STLiteralValueToken extends STToken {
    private final String text;

    STLiteralValueToken(SyntaxKind kind, String text, STNode leadingTrivia, STNode trailingTrivia) {
        this(kind, text, leadingTrivia, trailingTrivia, Collections.emptyList());
    }

    STLiteralValueToken(SyntaxKind kind,
                        String text,
                        STNode leadingTrivia,
                        STNode trailingTrivia,
                        Collection<STNodeDiagnostic> diagnostics) {
        super(kind, text.length(), leadingTrivia, trailingTrivia, diagnostics);
        this.text = text;
    }

    public String text() {
        return text;
    }

    public STToken modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STLiteralValueToken(this.kind, this.text, this.leadingMinutiae, this.trailingMinutiae, diagnostics);
    }

    public STToken modifyWith(STNode leadingMinutiae, STNode trailingMinutiae) {
        return new STLiteralValueToken(this.kind, this.text, leadingMinutiae, trailingMinutiae, this.diagnostics);
    }

    @Override
    public <T> T apply(STNodeTransformer<T> transformer) {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return leadingMinutiae + text + trailingMinutiae;
    }

    @Override
    public void writeTo(StringBuilder builder) {
        leadingMinutiae.writeTo(builder);
        builder.append(text);
        trailingMinutiae.writeTo(builder);
    }
}
