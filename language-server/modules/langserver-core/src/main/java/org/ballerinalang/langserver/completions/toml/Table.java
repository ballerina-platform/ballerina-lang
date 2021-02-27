/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.completions.toml;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Toml Table for snippet builder.
 *
 * @since 2.0.0
 */
public class Table implements TomlNode {
    private String name;
    private List<TomlNode> nodes;
    private int slot = 1;
    private static final int SPACING = 4;

    public Table(String name) {
        this.name = name;
        this.nodes = new ArrayList<>();
    }

    public void addKeyValuePair(String key, String defaultValue, ValueType type) {
        nodes.add(new KeyValuePair(key, defaultValue, slot, type));
        this.slot++;
    }

    public void addTable(Table table) {
        nodes.add(table);
    }

    public void addTableArray(TableArray tableArray) {
        nodes.add(tableArray);
    }

    @Override
    public String prettyPrint() {
        StringBuilder prettyString = new StringBuilder();
        prettyString.append("[").append(name).append("]").append(System.lineSeparator());
        for (TomlNode node : nodes) {
            prettyString.append(getIndentation()).append(node.prettyPrint()).append(System.lineSeparator());
        }
        return prettyString.toString();
    }

    private String getIndentation() {
        return " ".repeat(SPACING);
    }
}
