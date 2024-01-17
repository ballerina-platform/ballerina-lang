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
import io.ballerina.toml.syntax.tree.IdentifierLiteralNode;
import io.ballerina.toml.syntax.tree.InlineTableNode;
import io.ballerina.toml.syntax.tree.NodeFactory;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents toml inline table value in AST.
 *
 * @since 2.0.0
 */
public class TomlInlineTableValueNode extends TomlValueNode {

    private final List<TopLevelNode> elements;

    public TomlInlineTableValueNode(InlineTableNode table, List<TopLevelNode> elements, TomlNodeLocation location) {
        super(table, TomlType.INLINE_TABLE, location);
        this.elements = elements;
    }

    /**
     * Get a single element of the array based on the Index.
     *
     * @param index index of the element
     * @param <T>   Type of the element
     * @return Element object
     */
    public <T extends TopLevelNode> T get(int index) {
        TopLevelNode tomlValue = elements.get(index);
        return (T) tomlValue;
    }

    public List<TopLevelNode> elements() {
        return elements;
    }

    @Override
    public TomlType kind() {
        return super.kind();
    }

    @Override
    public void accept(TomlNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Set<Diagnostic> diagnostics() {
        Set<Diagnostic> tomlDiagnostics = diagnostics;
        for (TopLevelNode child : elements) {
            tomlDiagnostics.addAll(child.diagnostics());
        }
        return tomlDiagnostics;
    }

    @Override
    public void clearDiagnostics() {
        super.diagnostics.clear();
        for (TopLevelNode child : elements) {
            child.clearDiagnostics();
        }
    }

    @Override
    public boolean isMissingNode() {
        InlineTableNode inlineNode = (InlineTableNode) externalTreeNode();
        if (inlineNode.isMissing()) {
            return true;
        }
        return inlineNode.openBrace().isMissing() || inlineNode.closeBrace().isMissing();
    }

    @Override
    public List<Object> toNativeValue() {
        List<Object> list = new ArrayList<>();
        for (TopLevelNode element : elements) {
            list.add(element.toNativeObject());
        }
        return list;
    }

    public TomlTableNode toTable() {
        Map<String, TopLevelNode> table = new LinkedHashMap<>();
        for (TopLevelNode node : elements) {
            table.put(node.key().name(), node);
        }
        return new TomlTableNode((InlineTableNode) this.externalTreeNode(), generateKey(), false,
                this.location(), table);
    }

    private TomlKeyNode generateKey() {
        String tableKey = "__inline_value";
        IdentifierLiteralNode key =
                NodeFactory.createIdentifierLiteralNode(NodeFactory.createIdentifierToken(tableKey));
        TomlKeyEntryNode root = new TomlKeyEntryNode(key, new TomlUnquotedKeyNode(key, tableKey, this.location()));
        List<TomlKeyEntryNode> tomlKeyEntryNodes = Collections.singletonList(root);
        return new TomlKeyNode(NodeFactory.createKeyNode(NodeFactory.createSeparatedNodeList(key)),
                tomlKeyEntryNodes, this.location());
    }
}
