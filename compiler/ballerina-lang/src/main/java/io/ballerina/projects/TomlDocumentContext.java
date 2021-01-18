package io.ballerina.projects;

/**
 * Toml document context used in ProjectAPI Inner Tree.
 *
 * @since 2.0.0
 */
public class TomlDocumentContext {
    private TomlDocument tomlDocument;

    private TomlDocumentContext(TomlDocument tomlDocument) {
        this.tomlDocument = tomlDocument;
    }

    static TomlDocumentContext from(TomlDocument tomlDocument) {
        return new TomlDocumentContext(tomlDocument);
    }

    static TomlDocumentContext from(DocumentConfig documentConfig) {
        return new TomlDocumentContext(new TomlDocument(documentConfig.name(), documentConfig.content()));
    }

    public TomlDocument tomlDocument() {
        return tomlDocument;
    }
}
