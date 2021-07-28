/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.toml.completions.visitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Toml Table node in {@link org.ballerinalang.langserver.toml.completions.visitor.TomlSchemaVisitor}.
 *
 * @since 2.0.0
 */
public class Table implements TomlNode {

    private String name;
    private List<TomlNode> childNodes;
    private int slot = 1;
    private static final int SPACING = 4;

    public Table(String name) {
        this.name = name;
        this.childNodes = new ArrayList<>();
    }

    public void addKeyValuePair(String key, String defaultValue, ValueType type) {
        childNodes.add(new KeyValuePair(key, defaultValue, slot, type));
        this.slot++;
    }

    public void addTable(Table table) {
        childNodes.add(table);
    }

    public void addTableArray(TableArray tableArray) {
        childNodes.add(tableArray);
    }

    @Override
    public String getTomlSyntax() {
        StringBuilder prettyString = new StringBuilder();
        prettyString.append("[").append(name).append("]").append(System.lineSeparator());
        for (TomlNode node : childNodes) {
            prettyString.append(getIndentation()).append(node.getTomlSyntax()).append(System.lineSeparator());
        }
        return prettyString.toString();
    }

    @Override
    public String getKey() {
        return this.name;
    }

    @Override
    public TomlNodeType type() {
        return TomlNodeType.TABLE;
    }

    private String getIndentation() {
        return " ".repeat(SPACING);
    }

    @Override
    public String toString() {
        return this.getKey();
    }
}
