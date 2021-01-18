package io.ballerina.projects;

import io.ballerina.toml.semantic.ast.TomlTransformer;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

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

}
