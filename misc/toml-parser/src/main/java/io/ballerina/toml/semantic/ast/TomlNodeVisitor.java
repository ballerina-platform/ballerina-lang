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
public interface TomlNodeVisitor {

    default void visit(TomlTableNode tomlTableNode) {}

    default void visit(TomlTableArrayNode tomlTableArrayNode) {}

    default void visit(TomlKeyValueNode keyValue) {}

    default void visit(TomlKeyNode tomlKeyNode) {}

    default void visit(TomlValueNode tomlValue) {}

    default void visit(TomlArrayValueNode tomlArrayValueNode) {}

    default void visit(TomlKeyEntryNode tomlKeyEntryNode) {}

    default void visit(TomlStringValueNode tomlStringValueNode) {}

    default void visit(TomlDoubleValueNodeNode tomlDoubleValueNodeNode) {}

    default void visit(TomlLongValueNode tomlLongValueNode){}

    default void visit(TomlBooleanValueNode tomlBooleanValueNode) {}
}
