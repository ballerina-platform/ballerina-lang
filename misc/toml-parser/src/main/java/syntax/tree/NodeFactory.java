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
package syntax.tree;

import internal.parser.tree.STNode;
import internal.parser.tree.STNodeFactory;

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

    public static TableNode createTableNode(
            Token openBracket,
            IdentifierToken identifier,
            Token closeBracket,
            SeparatedNodeList<Node> fields) {
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
            Token openBracket,
            IdentifierToken identifier,
            Token closeBracket) {
        Objects.requireNonNull(openBracket, "openBracket must not be null");
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(closeBracket, "closeBracket must not be null");

        STNode stTableArrayNode = STNodeFactory.createTableArrayNode(
                openBracket.internalNode(),
                identifier.internalNode(),
                closeBracket.internalNode());
        return stTableArrayNode.createUnlinkedFacade();
    }

    public static KeyValue createKeyValue(
            IdentifierToken identifier,
            Token assign,
            Node value) {
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(assign, "assign must not be null");
        Objects.requireNonNull(value, "value must not be null");

        STNode stKeyValue = STNodeFactory.createKeyValue(
                identifier.internalNode(),
                assign.internalNode(),
                value.internalNode());
        return stKeyValue.createUnlinkedFacade();
    }
}

