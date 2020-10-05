/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * This transform each node in the abstract syntax tree to
 * another object of type T.
 * <p>
 * This class separates tree nodes from various unrelated operations that needs
 * to be performed on the syntax tree nodes.
 * <p>
 * This class allows you to transform the syntax tree into something else without
 * mutating instance variables.
 * <p>
 *
 * @param <T> the type of class that is returned by visit methods
 * @since 0.1.0
 */
public abstract class TomlNodeTransformer<T> {

    public T transform(TomlTable tomlTable) {
        return transformSyntaxNode(tomlTable);
    }

    public T transform(TomlTableArray tomlTableArray) {
        return transformSyntaxNode(tomlTableArray);
    }

    public T transform(TomlKeyValue keyValue) {
        return transformSyntaxNode(keyValue);
    }

    public T transform(TomlKey tomlKey) {
        return transformSyntaxNode(tomlKey);
    }

    public T transform(TomlValue tomlValue) {
        return transformSyntaxNode(tomlValue);
    }

    public T transform(TomlArray tomlArray) {
        return transformSyntaxNode(tomlArray);
    }

    public T transform(TomlBasicValue tomlBasicValue) {
        return transformSyntaxNode(tomlBasicValue);
    }

    protected abstract T transformSyntaxNode(TomlNode node);
}
