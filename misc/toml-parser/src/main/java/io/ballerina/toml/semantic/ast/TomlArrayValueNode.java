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

import java.util.List;

/**
 * Represents Toml Array in AST. Used to support nested and mixed arrays.
 *
 * @since 2.0.0
 */
public class TomlArrayValueNode extends TomlValueNode {

    private final List<TomlValueNode> elements;

    public TomlArrayValueNode(List<TomlValueNode> elements, TomlNodeLocation location) {
        super(TomlType.ARRAY, location);
        this.elements = elements;
    }

    /**
     * Get a single element of the array based on the Index.
     * @param index index of the element
     * @param <T> Type of the element
     * @return Element object
     */
    public <T extends TomlValueNode> T get(int index) {
        TomlValueNode tomlValue = elements.get(index);
        return (T) tomlValue;
    }

    public List<TomlValueNode> elements() {
        return elements;
    }

    @Override
    public TomlType kind() {
        return super.kind();
    }

    @Override
    public void accept(TomlNodeVisitor visitor) {
        visitor.visit(this);
    }
}
