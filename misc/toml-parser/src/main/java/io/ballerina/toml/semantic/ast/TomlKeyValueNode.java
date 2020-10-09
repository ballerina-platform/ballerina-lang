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

/**
 * Represents Key Value Pair in TOML AST.
 *
 * @since 2.0.0
 */
public class TomlKeyValueNode extends TopLevelNode {

    private final TomlValueNode value;

    public TomlKeyValueNode(TomlKeyNode key, TomlValueNode value, TomlNodeLocation location) {
        super(key, TomlType.KEY_VALUE, location);
        this.value = value;
    }

    public TomlValueNode value() {
        return value;
    }

    @Override
    public String toString() {
        return "TomlKeyValue{" +
                "key=" + key().name() +
                ", value=" + value +
                '}';
    }

    @Override
    public void accept(TomlNodeVisitor visitor) {
        visitor.visit(this);
    }
}
