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
import io.ballerina.toml.syntax.tree.KeyNode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Key in TOML AST.
 *
 * @since 2.0.0
 */
public class TomlKeyNode extends TomlNode {

    private final List<TomlKeyEntryNode> keys;

    public TomlKeyNode(KeyNode node, List<TomlKeyEntryNode> keys, @Nullable TomlNodeLocation location) {
        super(node, TomlType.KEY_VALUE, location);
        this.keys = keys;
    }

    public List<TomlKeyEntryNode> keys() {
        return keys;
    }

    public String name() {
        List<String> list = new ArrayList<>(keys.size());
        for (TomlKeyEntryNode keyEntryNode : keys) {
            if (keyEntryNode.kind() == TomlType.STRING) {
                list.add("\"" + keyEntryNode.name().toString() + "\"");
            } else {
                list.add(keyEntryNode.name().toString());
            }
        }
        return String.join(".", list);
    }

    @Override
    public String toString() {
        return "TomlKeyNode{" + "keys=" + name() + '}';
    }

    @Override
    public void accept(TomlNodeVisitor visitor) {
        visitor.visit(this);
    }
}
