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

/**
 * Visitor for TOML AST.
 *
 * @since 2.0.0
 */
public abstract class TomlNodeVisitor {

    public void visit(TomlTableNode tomlTableNode) {
        throw new AssertionError();
    }

    public void visit(TomlTableArrayNode tomlTableArrayNode) {
        throw new AssertionError();
    }

    public void visit(TomlKeyValueNode keyValue) {
        throw new AssertionError();
    }

    public void visit(TomlKeyNode tomlKeyNode) {
        throw new AssertionError();
    }

    public void visit(TomlValueNode tomlValue) {
        throw new AssertionError();
    }

    public void visit(TomlArrayValueNode tomlArrayValueNode) {
        throw new AssertionError();
    }

    public void visit(TomlKeyEntryNode tomlKeyEntryNode) {
        throw new AssertionError();
    }

    public void visit(TomlStringValueNode tomlStringValueNode) {
        throw new AssertionError();
    }

    public void visit(TomlDoubleValueNodeNode tomlDoubleValueNodeNode) {
        throw new AssertionError();
    }

    public void visit(TomlLongValueNode tomlLongValueNode) {
        throw new AssertionError();
    }

    public void visit(TomlBooleanValueNode tomlBooleanValueNode) {
        throw new AssertionError();
    }
}
