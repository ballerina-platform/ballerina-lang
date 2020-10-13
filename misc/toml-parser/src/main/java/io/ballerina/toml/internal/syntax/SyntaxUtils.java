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
package io.ballerina.toml.internal.syntax;

import io.ballerina.toml.internal.parser.tree.STNode;
import io.ballerina.toml.internal.parser.tree.STToken;
import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.Token;

/**
 * Contains utility methods works with both internal and external syntax trees.
 *
 * @since 2.0.0
 */
public class SyntaxUtils {
    private SyntaxUtils() {
    }

    public static boolean isToken(Node blNode) {
        // TODO find a syntaxKind based approach to check
        return blNode instanceof Token;
    }

    public static boolean isNonTerminalNode(Node node) {
        // TODO find a syntaxKind based approach to check
        return !(node instanceof Token);
    }

    public static boolean isToken(STNode stNode) {
        // TODO find a syntaxKind based approach to check
        return stNode instanceof STToken;
    }

    public static boolean isNonTerminalNode(STNode node) {
        // TODO find a syntaxKind based approach to check
        return !isToken(node);
    }

    public static boolean isSTNodePresent(STNode node) {
        return node != null;
    }
}
