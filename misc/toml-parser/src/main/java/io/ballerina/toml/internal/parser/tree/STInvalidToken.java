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

/**
 * Represents an invalid token produced by the lexer.
 *
 * @since 2.0.0
 */
public class STInvalidToken extends STToken {
    private String tokenText;

    STInvalidToken(String tokenText) {
        super(SyntaxKind.INVALID_TOKEN, STNodeFactory.createEmptyNodeList(), STNodeFactory.createEmptyNodeList());
        this.tokenText = tokenText;
    }

    STInvalidToken(String tokenText,
                   STNode leadingTrivia,
                   STNode trailingTrivia,
                   Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.INVALID_TOKEN, tokenText.length(), leadingTrivia, trailingTrivia, diagnostics);
        this.tokenText = tokenText;
    }

    public String text() {
        return tokenText;
    }

    public STToken modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STInvalidToken(this.tokenText, this.leadingMinutiae, this.trailingMinutiae, diagnostics);
    }

    public STToken modifyWith(STNode leadingMinutiae, STNode trailingMinutiae) {
        return new STInvalidToken(this.tokenText, leadingMinutiae, trailingMinutiae, this.diagnostics);
    }

    @Override
    public void writeTo(StringBuilder builder) {
        leadingMinutiae.writeTo(builder);
        builder.append(tokenText);
        trailingMinutiae.writeTo(builder);
    }
}
