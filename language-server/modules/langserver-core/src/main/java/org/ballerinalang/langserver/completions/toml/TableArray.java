package org.ballerinalang.langserver.completions.toml;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Array of tables for Toml Snippet builder.
 *
 * @since 2.0.0
 */
public class TableArray implements TomlNode {

    private String name;
    private List<TomlNode> nodes;
    private static final int SPACING = 4;

    public TableArray(String name) {
        this.name = name;
        this.nodes = new ArrayList<>();
    }

    public TableArray(String name, List<TomlNode> nodes) {
        this.name = name;
        this.nodes = nodes;
    }

    @Override
    public String prettyPrint() {
        StringBuilder prettyString = new StringBuilder();
        prettyString.append("[[").append(name).append("]]").append(System.lineSeparator());
        for (TomlNode node : nodes) {
            prettyString.append(getIndentation()).append(node.prettyPrint()).append(System.lineSeparator());
        }
        return prettyString.toString();
    }

    private String getIndentation() {
        return " ".repeat(SPACING);
    }
}
