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

package sementic.nodes;

import syntax.tree.SyntaxKind;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents Table in TOML AST.
 */
public class TomlTable extends TopLevelNode {

    Map<String, TopLevelNode> childs;
    public boolean generated;

    public TomlTable(TomlKey key) {
        super(key);
        this.childs = new LinkedHashMap<>();
    }

    public Map<String, TopLevelNode> getChilds() {
        return childs;
    }

    public void setChilds(Map<String, TopLevelNode> childs) {
        this.childs = childs;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    public void addChild(TopLevelNode topLevelNode) {
        childs.put(topLevelNode.key.name, topLevelNode);
    }

    public void replaceGeneratedTable(TomlTable tomlTable) {
        TopLevelNode childNode = childs.get(tomlTable.key.name);
        if (childNode instanceof TomlTable) {
            TomlTable childTable = (TomlTable) childNode;
            if ((childTable).isGenerated()) {
                tomlTable.addChildList(childTable.getChilds());
                childs.put(tomlTable.key.name, tomlTable);
            }
        }
    }

    public void addChildList(Map<String, TopLevelNode> topLevelNodes) {
        childs.putAll(topLevelNodes);
    }

    @Override
    public SyntaxKind getKind() {
        return SyntaxKind.TABLE;
    }

    @Override
    public String toString() {
        return "TomlTable{" +
                "identifier=" + key.name +
                ", generated=" + generated +
                '}';
    }

    @Override
    public void accept(TomlNodeVisitor visitor) {
        visitor.visit(this);
    }
}
