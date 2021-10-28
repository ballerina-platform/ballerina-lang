package io.ballerina.shell.utils;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;


public class CommonUtil {

    public static NonTerminalNode findNode(Symbol symbol, SyntaxTree syntaxTree) {
        if (symbol.getLocation().isEmpty()) {
            return null;
        }

        TextDocument textDocument = syntaxTree.textDocument();
        LineRange symbolRange = symbol.getLocation().get().lineRange();
        int start = textDocument.textPositionFrom(symbolRange.startLine());
        int end = textDocument.textPositionFrom(symbolRange.endLine());
        return ((ModulePartNode) syntaxTree.rootNode()).
                findNode(TextRange.from(start, end - start), true);
    }
}
