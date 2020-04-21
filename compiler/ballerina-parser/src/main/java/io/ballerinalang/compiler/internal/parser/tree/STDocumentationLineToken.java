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

import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

/**
 * Represents documentation line in the internal syntax tree.
 * <p>
 * TODO: This is a temporary node
 *
 * @since 1.3.0
 */
public class STDocumentationLineToken extends STToken {

    public final String text;

    STDocumentationLineToken(String text, STNode leadingTrivia, STNode trailingTrivia) {
        super(SyntaxKind.DOCUMENTATION_LINE, text.length(), leadingTrivia, trailingTrivia);
        this.text = text;
    }

    public String text() {
        return text;
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new IdentifierToken(this, position, parent);
    }

    @Override
    public String toString() {
        return leadingTrivia + text + trailingTrivia;
    }
}
