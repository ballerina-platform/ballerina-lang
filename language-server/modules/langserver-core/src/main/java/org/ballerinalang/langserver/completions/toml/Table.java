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
        prettyString.append(System.lineSeparator());
        return prettyString.toString();
    }

    private String getIndentation() {
        return " ".repeat(SPACING);
    }
}
