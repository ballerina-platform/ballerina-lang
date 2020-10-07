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

import io.ballerina.toml.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.toml.syntax.tree.SyntaxKind;

import java.util.Map;

/**
 * A Validator for TOML.
 */
public class TomlValidator extends TomlNodeVisitor {

    private DiagnosticLog dlog;

    public TomlValidator() {
        this.dlog = DiagnosticLog.getInstance();
    }

    @Override
    public void visit(TomlTable tomlTable) {
        Map<String, TopLevelNode> children = tomlTable.getChildren();
        for (Map.Entry<String, TopLevelNode> child : children.entrySet()) {
            TopLevelNode value = child.getValue();
            if (value.getKind() == SyntaxKind.TABLE) {
                String childTableFullName = value.getKey().name;
                String[] split = childTableFullName.split("\\.");
                if (split.length > 1) {
                    String childTableName = split[split.length - 1];
                    if (children.containsKey(childTableName)) {
                        TomlDiagnostic nodeExists = dlog.error(value.location, DiagnosticErrorCode.ERROR_EXISTING_NODE);
                        tomlTable.addDiagnostic(nodeExists);
                    }
                }
                value.accept(this);
            }
        }
    }

    @Override
    public void visit(TomlKeyValue keyValue) {

    }

    @Override
    public void visit(TomlKey tomlKey) {

    }

    @Override
    public void visit(TomlValue tomlValue) {

    }

    @Override
    public void visit(TomlArray tomlArray) {

    }

    @Override
    public void visit(TomlBasicValue tomlBasicValue) {

    }

    @Override
    public void visit(TomlTableArray tomlTableArray) {

    }
}
