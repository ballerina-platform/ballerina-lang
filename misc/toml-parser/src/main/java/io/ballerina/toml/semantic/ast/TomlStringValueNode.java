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

package io.ballerina.toml.semantic.ast;

import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.toml.syntax.tree.LiteralStringLiteralNode;
import io.ballerina.toml.syntax.tree.StringLiteralNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;

/**
 * Represents A String Value in Toml AST.
 *
 * @since 2.0.0
 */
public class TomlStringValueNode extends TomlBasicValueNode<String> {
    public TomlStringValueNode(StringLiteralNode stringLiteralNode, String value, TomlNodeLocation location) {
        super(stringLiteralNode, value, TomlType.STRING, location);
    }

    public TomlStringValueNode(LiteralStringLiteralNode literalStringNode, String value, TomlNodeLocation location) {
        super(literalStringNode, value, TomlType.STRING, location);
    }

    @Override
    public void accept(TomlNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isMissingNode() {
        if (this.externalTreeNode().kind() == SyntaxKind.STRING_LITERAL) {
            StringLiteralNode stringLiteralNode = (StringLiteralNode) this.externalTreeNode();
            if (stringLiteralNode.isMissing()) {
                return true;
            }
            return stringLiteralNode.startDoubleQuote().isMissing() || stringLiteralNode.endDoubleQuote().isMissing();
        }
        if (this.externalTreeNode().kind() == SyntaxKind.LITERAL_STRING) {
            LiteralStringLiteralNode literalStringNode = (LiteralStringLiteralNode) this.externalTreeNode();
            if (literalStringNode.isMissing()) {
                return true;
            }
            return literalStringNode.startSingleQuote().isMissing() || literalStringNode.startSingleQuote().isMissing();
        }
        return false;
    }
}
