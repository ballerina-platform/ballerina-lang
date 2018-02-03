package org.ballerinalang.langserver.definition;

import org.eclipse.lsp4j.Position;

/**
 * Holds the Meta data to generate the Definition Item.
 */
public class DefinitionDataHolder {

    private String compilationUnitName;

    private Position position;

    public DefinitionDataHolder(String compilationUnitName, Position position) {
        this.compilationUnitName = compilationUnitName;
        this.position = position;
    }

    public String getCompilationUnitName() {
        return compilationUnitName;
    }

    public Position getPosition() {
        return position;
    }
}
