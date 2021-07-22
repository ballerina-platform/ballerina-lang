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
import io.ballerina.toml.syntax.tree.DocumentMemberDeclarationNode;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.KeyValueNode;
import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.TableArrayNode;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents Table in TOML AST.
 *
 * @since 2.0.0
 */
public class TomlTableNode extends TopLevelNode {

    private final Map<String, TopLevelNode> entries;
    private final boolean generated;

    public TomlTableNode(DocumentNode documentNode, TomlKeyNode key, TomlNodeLocation location) {
        super(documentNode, key, TomlType.TABLE, location);
        this.entries = new LinkedHashMap<>();
        this.generated = false;
    }

    public TomlTableNode(DocumentMemberDeclarationNode node, TomlKeyNode key, TomlNodeLocation location) {
        super(node, key, TomlType.TABLE, location);
        this.entries = new LinkedHashMap<>();
        this.generated = false;
    }

    public TomlTableNode(DocumentMemberDeclarationNode node, TomlKeyNode key, boolean generated,
                         TomlNodeLocation location) {
        super(node, key, TomlType.TABLE, location);
        this.entries = new LinkedHashMap<>();
        this.generated = generated;
    }

    public TomlTableNode(TableNode tableNode, TomlKeyNode key, boolean generated, TomlNodeLocation location) {
        super(tableNode, key, TomlType.TABLE, location);
        this.entries = new LinkedHashMap<>();
        this.generated = generated;
    }

    public TomlTableNode(TableNode tableNode, TomlKeyNode key, boolean generated, TomlNodeLocation location,
                         Map<String, TopLevelNode> entries) {
        super(tableNode, key, TomlType.TABLE, location);
        this.entries = entries;
        this.generated = generated;
    }

    public Map<String, TopLevelNode> entries() {
        return entries;
    }

    public boolean generated() {
        return generated;
    }

    @Override
    public Set<Diagnostic> diagnostics() {
        Set<Diagnostic> tomlDiagnostics = diagnostics;
        for (Map.Entry<String, TopLevelNode> child : entries.entrySet()) {
            tomlDiagnostics.addAll(child.getValue().diagnostics());
        }
        return tomlDiagnostics;
    }

    @Override
    public void clearDiagnostics() {
        super.diagnostics.clear();
        for (Map.Entry<String, TopLevelNode> child : entries.entrySet()) {
            child.getValue().clearDiagnostics();
        }
    }

    public void replaceGeneratedTable(TomlTableNode tomlTableNode) {
        TopLevelNode childNode = entries.get(tomlTableNode.key().name());
        if (childNode.kind() == TomlType.TABLE) {
            TomlTableNode childTable = (TomlTableNode) childNode;
            if (childTable.generated()) {
                tomlTableNode.entries().putAll(childTable.entries());
                entries.put(tomlTableNode.key().name(), tomlTableNode);
            }
        }
    }

    @Override
    public String toString() {
        return "TomlTable{" +
                "identifier=" + key().name() +
                ", generated=" + generated +
                '}';
    }

    public void addSyntaxDiagnostics(Set<Diagnostic> diagnostics) {
        for (Diagnostic diagnostic : diagnostics) {
            this.addDiagnostic(diagnostic);
        }
    }

    @Override
    public void accept(TomlNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isMissingNode() {
        Node node = this.externalTreeNode();
        if (node.isMissing()) {
            return true;
        }
        if (node.kind() == SyntaxKind.MODULE_PART) {
            return false;
        }
        if (node.kind() == SyntaxKind.KEY_VALUE) {
            KeyValueNode keyValueNode = (KeyValueNode) node;
            if (keyValueNode.isMissing()) {
                return true;
            }
            if (keyValueNode.identifier().isMissing()) {
                return true;
            }
            return keyValueNode.value().isMissing();
        }
        if (node.kind() == SyntaxKind.TABLE) {
            TableNode tableNode = (TableNode) node;
            if (tableNode.isMissing()) {
                return true;
            }
            return tableNode.identifier().isMissing();
        }
        if (node.kind() == SyntaxKind.TABLE_ARRAY) {
            TableArrayNode tableArrayNode = (TableArrayNode) node;
            if (tableArrayNode.isMissing()) {
                return true;
            }
            return tableArrayNode.identifier().isMissing();
        }
        return false;
    }

    @Override
    public Map<String, Object> toNativeObject() {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, TopLevelNode> topLevelNodeEntry : this.entries().entrySet()) {
            String key = topLevelNodeEntry.getKey();
            TopLevelNode value = topLevelNodeEntry.getValue();
            map.put(key, value.toNativeObject());
        }
        return map;
    }
}
