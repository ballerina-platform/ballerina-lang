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

package io.ballerina.toml.ast;

import io.ballerina.toml.syntax.tree.SyntaxKind;

/***
 * Represents a Basic Value in Toml AST.
 * @param <T>Type of the Basic Node
 * @since 0.1.0
 */
public abstract class TomlBasicValueNode<T> extends TomlValueNode {
    private final T value;

    public TomlBasicValueNode(T value, SyntaxKind kind, TomlNodeLocation location) {
        super(kind, location);
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
