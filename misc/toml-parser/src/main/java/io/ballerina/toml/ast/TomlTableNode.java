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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents Table in TOML AST.
 *
 * @since 0.1.0
 */
public class TomlTableNode extends TopLevelNode {

    private final Map<String, TopLevelNode> children;
    private final boolean generated;

    public TomlTableNode(TomlKeyNode key, TomlNodeLocation location) {
        super(key, SyntaxKind.TABLE, location);
        this.children = new LinkedHashMap<>();
        this.generated = false;
    }

    public TomlTableNode(TomlKeyNode key, boolean generated, TomlNodeLocation location) {
        super(key, SyntaxKind.TABLE, location);
        this.children = new LinkedHashMap<>();
        this.generated = generated;
    }

    public TomlTableNode(TomlKeyNode key, boolean generated, TomlNodeLocation location,
                         Map<String, TopLevelNode> children) {
        super(key, SyntaxKind.TABLE, location);
        this.children = children;
        this.generated = generated;
    }

    public Map<String, TopLevelNode> children() {
        return children;
    }

    public boolean generated() {
        return generated;
    }

    public void addChild(TopLevelNode topLevelNode) {
        children.put(topLevelNode.key().name(), topLevelNode);
    }

    public void replaceGeneratedTable(TomlTableNode tomlTableNode) {
        TopLevelNode childNode = children.get(tomlTableNode.key().name());
        if (childNode instanceof TomlTableNode) {
            TomlTableNode childTable = (TomlTableNode) childNode;
            if ((childTable).generated()) {
                tomlTableNode.addChildList(childTable.children());
                children.put(tomlTableNode.key().name(), tomlTableNode);
            }
        }
    }

    public void addChildList(Map<String, TopLevelNode> topLevelNodes) {
        children.putAll(topLevelNodes);
    }

    @Override
    public String toString() {
        return "TomlTable{" +
                "identifier=" + key().name() +
                ", generated=" + generated +
                '}';
    }

    public void setSyntacticalDiagnostics(List<TomlDiagnostic> syntaxDiags) {
        this.diagnostics().addAll(syntaxDiags);
    }

    public List<TomlDiagnostic> collectSemanticDiagnostics() {
        List<TomlDiagnostic> tomlDiagnostics = new ArrayList<>();
        for (Map.Entry<String, TopLevelNode> child : children.entrySet()) {
            tomlDiagnostics.addAll(child.getValue().diagnostics());
        }
        this.diagnostics().addAll(tomlDiagnostics);
        return tomlDiagnostics;
    }

    @Override
    public void accept(TomlNodeVisitor visitor) {
        visitor.visit(this);
    }
}
