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
package io.ballerina.toml.syntax.tree;

import io.ballerina.toml.internal.parser.tree.STNode;
import io.ballerina.toml.internal.parser.tree.STNodeFactory;

import java.util.Objects;

/**
 * A factory for creating nodes in the syntax tree.
 *
 * This is a generated class.
 *
 * @since 2.0.0
 */
public abstract class NodeFactory extends AbstractNodeFactory {

    private NodeFactory() {
    }

    public static ModulePartNode createModulePartNode(
            NodeList<ModuleMemberDeclarationNode> members,
            Token eofToken) {
        Objects.requireNonNull(members, "members must not be null");
        Objects.requireNonNull(eofToken, "eofToken must not be null");

        STNode stModulePartNode = STNodeFactory.createModulePartNode(
                members.underlyingListNode().internalNode(),
                eofToken.internalNode());
        return stModulePartNode.createUnlinkedFacade();
    }

    public static BasicValueNode createBasicValueNode(
            SyntaxKind kind,
            Token value) {
        Objects.requireNonNull(value, "value must not be null");

        STNode stBasicValueNode = STNodeFactory.createBasicValueNode(
                kind,
                value.internalNode());
        return stBasicValueNode.createUnlinkedFacade();
    }

    public static TableNode createTableNode(
            Token openBracket,
            IdentifierToken identifier,
            Token closeBracket,
            NodeList<Node> fields) {
        Objects.requireNonNull(openBracket, "openBracket must not be null");
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(closeBracket, "closeBracket must not be null");
        Objects.requireNonNull(fields, "fields must not be null");

        STNode stTableNode = STNodeFactory.createTableNode(
                openBracket.internalNode(),
                identifier.internalNode(),
                closeBracket.internalNode(),
                fields.underlyingListNode().internalNode());
        return stTableNode.createUnlinkedFacade();
    }

    public static TableArrayNode createTableArrayNode(
            Token firstOpenBracket,
            Token secondOpenBracket,
            IdentifierToken identifier,
            Token firstCloseBracket,
            Token secondCloseBracket,
            NodeList<Node> fields) {
        Objects.requireNonNull(firstOpenBracket, "firstOpenBracket must not be null");
        Objects.requireNonNull(secondOpenBracket, "secondOpenBracket must not be null");
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(firstCloseBracket, "firstCloseBracket must not be null");
        Objects.requireNonNull(secondCloseBracket, "secondCloseBracket must not be null");
        Objects.requireNonNull(fields, "fields must not be null");

        STNode stTableArrayNode = STNodeFactory.createTableArrayNode(
                firstOpenBracket.internalNode(),
                secondOpenBracket.internalNode(),
                identifier.internalNode(),
                firstCloseBracket.internalNode(),
                secondCloseBracket.internalNode(),
                fields.underlyingListNode().internalNode());
        return stTableArrayNode.createUnlinkedFacade();
    }

    public static KeyValue createKeyValue(
            Token identifier,
            Token assign,
            ValueNode value) {
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(assign, "assign must not be null");
        Objects.requireNonNull(value, "value must not be null");

        STNode stKeyValue = STNodeFactory.createKeyValue(
                identifier.internalNode(),
                assign.internalNode(),
                value.internalNode());
        return stKeyValue.createUnlinkedFacade();
    }

    public static Array createArray(
            Token openBracket,
            SeparatedNodeList<ValueNode> values,
            Token closeBracket) {
        Objects.requireNonNull(openBracket, "openBracket must not be null");
        Objects.requireNonNull(values, "values must not be null");
        Objects.requireNonNull(closeBracket, "closeBracket must not be null");

        STNode stArray = STNodeFactory.createArray(
                openBracket.internalNode(),
                values.underlyingListNode().internalNode(),
                closeBracket.internalNode());
        return stArray.createUnlinkedFacade();
    }
}

