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

import java.util.Optional;

/**
 * A {@code ChildNodeEntry} (key-value pair) represents the name, node pair of a
 * child node of a non-terminal node.
 *
 * @since 1.3.0
 */
public class ChildNodeEntry {
    private final String name;
    private final Node node;

    ChildNodeEntry(String name, Node node) {
        this.name = name;
        this.node = node;
    }

    public String name() {
        return this.name;
    }

    public Optional<Node> node() {
        if (isList()) {
            NonTerminalNode nodeList = (NonTerminalNode) node;
            if (nodeList.bucketCount() == 0) {
                return Optional.empty();
            } else {
                return Optional.of(nodeList.childInBucket(0));
            }
        } else {
            // TODO handle empty Nodes and Tokens
            return Optional.of(this.node);
        }
    }

    public NodeList<Node> nodeList() {
        return new NodeList<>((NonTerminalNode) node);
    }

    public boolean isList() {
        return node.kind() == SyntaxKind.LIST;
    }
}
