package io.ballerina.quoter.parser;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

/**
 * Parser that parses as a module.
 */
public class ModuleParser extends QuoterParser {
    public ModuleParser(long timeoutMs) {
        super(timeoutMs);
    }

    @Override
    public Node parse(String source) {
        TextDocument document = TextDocuments.from(source);
        return getSyntaxTree(document).rootNode();
    }
}
