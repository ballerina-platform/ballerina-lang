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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Table Array in TOML AST.
 *
 * @since 2.0.0
 */
public class TomlTableArrayNode extends TopLevelNode {
    private final List<TomlTableNode> children;
    public TomlTableArrayNode(TomlKeyNode key, TomlNodeLocation location) {
        super(key, TomlType.TABLE_ARRAY, location);
        this.children = new ArrayList<>();
    }
    public TomlTableArrayNode(TomlKeyNode key, TomlNodeLocation location, List<TomlTableNode> children) {
        super(key, TomlType.TABLE_ARRAY, location);
        this.children = children;
    }

    public List<TomlTableNode> children() {
        return children;
    }

    public void addChild(TomlTableNode topLevelNode) {
        children.add(topLevelNode);
    }

    @Override
    public String toString() {
        return "TomlTableArray{" +
                "identifier=" + key().name() +
                '}';
    }

    @Override
    public void accept(TomlNodeVisitor visitor) {
        visitor.visit(this);
    }
}
