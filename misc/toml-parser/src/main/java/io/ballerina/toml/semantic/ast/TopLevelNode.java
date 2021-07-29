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
import io.ballerina.toml.syntax.tree.Node;

/**
 * Represents a Top Level Node in TOML.
 *
 * @since 2.0.0
 */
public abstract class TopLevelNode extends TomlNode {

    private final TomlKeyNode key;

    public TopLevelNode(Node node, TomlKeyNode key, TomlType type, TomlNodeLocation location) {
        super(node, type, location);
        this.key = key;
    }

    public TomlKeyNode key() {
        return key;
    }
    
    public abstract Object toNativeObject();
}
