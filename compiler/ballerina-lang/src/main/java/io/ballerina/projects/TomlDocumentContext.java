package io.ballerina.projects;

import io.ballerina.toml.semantic.ast.TomlTransformer;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

public class TomlDocumentContext {
    private TextDocument textDocument;
    private DocumentId documentId;
    private String name;
    private String content;
    private SyntaxTree syntaxTree;
    private TomlTableNode tomlAstNode;

    private TomlDocumentContext(DocumentId documentId, String name, String content) {
        this.documentId = documentId;
        this.name = name;
        this.content = content;
    }

    static TomlDocumentContext from(DocumentConfig documentConfig) {
        return new TomlDocumentContext(documentConfig.documentId(), documentConfig.name(), documentConfig.content());
    }

    DocumentId documentId() {
        return this.documentId;
    }

    String name() {
        return this.name;
    }

    TextDocument textDocument() {
        if (this.textDocument == null) {
            this.textDocument = TextDocuments.from(this.content);
        }
        return this.textDocument;
    }

    public SyntaxTree syntaxTree() {
        if (syntaxTree != null) {
            return syntaxTree;
        }

        parse();
        return syntaxTree;
    }

    public TomlTableNode tomlAstNode() {
        if (tomlAstNode != null) {
            return tomlAstNode;
        }

        parse();
        return tomlAstNode;
    }

    private void parse() {
        syntaxTree = SyntaxTree.from(textDocument, name);
        TomlTransformer nodeTransformer = new TomlTransformer();
        tomlAstNode = (TomlTableNode) nodeTransformer.transform((DocumentNode) syntaxTree.rootNode());
    }
}
