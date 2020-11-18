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

import java.util.Collection;

/**
 * Represents whitespaces, comments, newline characters attached to a {@code STToken}.
 *
 * @since 2.0.0
 */
public class STMinutiae extends STNode {

    private final String text;

    STMinutiae(SyntaxKind kind, String text) {
        this(kind, text, text.length());
    }

    STMinutiae(SyntaxKind kind, String text, int width) {
        super(kind);
        this.text = text;
        this.width = width;
        this.widthWithLeadingMinutiae = width;
        this.widthWithTrailingMinutiae = width;
        this.widthWithMinutiae = width;
    }

    @Override
    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void accept(STNodeVisitor visitor) {
        // TODO visiting minutiae is not yet supported
        throw new IllegalStateException();
    }

    @Override
    public <T> T apply(STNodeTransformer<T> transformer) {
        // TODO transforming minutiae is not yet supported
        throw new IllegalStateException();
    }

    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public void writeTo(StringBuilder builder) {
        builder.append(text);
    }
}
