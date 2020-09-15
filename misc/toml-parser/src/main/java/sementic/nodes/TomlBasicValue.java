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

package sementic.nodes;

import syntax.tree.SyntaxKind;

/**
 * Represents Basic Value in TOML AST.
 */
public class TomlBasicValue extends TomlValue {

    private Object value;
    private SyntaxKind kind;

    public TomlBasicValue(Object value, SyntaxKind kind) {
        this.value = value;
        this.kind = kind;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "TomlBasicValue{" +
                "value=" + value +
                ", kind=" + kind +
                '}';
    }

    @Override
    public void accept(TomlNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public SyntaxKind getKind() {
        return kind;
    }
}
