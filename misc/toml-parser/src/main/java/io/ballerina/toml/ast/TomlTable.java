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
 */
public class TomlTable extends TopLevelNode {

    Map<String, TopLevelNode> children;
    public boolean generated;

    public TomlTable(TomlKey key) {
        super(key, SyntaxKind.TABLE);
        this.children = new LinkedHashMap<>();
    }

    public Map<String, TopLevelNode> getChildren() {
        return children;
    }

    public void setChildren(Map<String, TopLevelNode> children) {
        this.children = children;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    public void addChild(TopLevelNode topLevelNode) {
        children.put(topLevelNode.key.name, topLevelNode);
    }

    public void replaceGeneratedTable(TomlTable tomlTable) {
        TopLevelNode childNode = children.get(tomlTable.key.name);
        if (childNode instanceof TomlTable) {
            TomlTable childTable = (TomlTable) childNode;
            if ((childTable).isGenerated()) {
                tomlTable.addChildList(childTable.getChildren());
                children.put(tomlTable.key.name, tomlTable);
            }
        }
    }

    public void addChildList(Map<String, TopLevelNode> topLevelNodes) {
        children.putAll(topLevelNodes);
    }

    @Override
    public String toString() {
        return "TomlTable{" +
                "identifier=" + key.name +
                ", generated=" + generated +
                '}';
    }

    public void setSyntacticalDiagnostics(List<TomlDiagnostic> syntaxDiags) {
        this.getDiagnostics().addAll(syntaxDiags);
    }

    public List<TomlDiagnostic> collectSemanticDiagnostics() {
        List<TomlDiagnostic> tomlDiagnostics = new ArrayList<>();
        for (Map.Entry<String, TopLevelNode> child : children.entrySet()) {
            tomlDiagnostics.addAll(child.getValue().getDiagnostics());
        }
        this.getDiagnostics().addAll(tomlDiagnostics);
        return tomlDiagnostics;
    }

    @Override
    public <T> T apply(TomlNodeTransformer<T> transformer) {
        return transformer.transform(this);
    }

    @Override
    public void accept(TomlNodeVisitor visitor) {
        visitor.visit(this);
    }
}
