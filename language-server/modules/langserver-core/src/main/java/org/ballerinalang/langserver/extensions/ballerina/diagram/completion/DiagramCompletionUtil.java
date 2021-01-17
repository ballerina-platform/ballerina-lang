package org.ballerinalang.langserver.extensions.ballerina.diagram.completion;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.util.TokensUtil;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.eclipse.lsp4j.Position;

import java.util.Optional;

public class DiagramCompletionUtil {
    /**
     * Find the token at cursor.
     */
    public static void fillTokenInfoAtCursor(DiagramCompletionContext context) throws TokenOrSymbolNotFoundException {
        context.setTokenAtCursor(TokensUtil.findTokenAtPosition(context, context.getCursorPosition()));
        Optional<Document> document = context.workspace().document(context.filePath());
        if (document.isEmpty()) {
            throw new RuntimeException("Could not find a valid document");
        }
        TextDocument textDocument = document.get().textDocument();

        Position position = context.getCursorPosition();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        // TODO: Try to delegate the set option to the context
        context.setCursorPositionInTree(txtPos);
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nonTerminalNode = ((ModulePartNode) document.get().syntaxTree().rootNode()).findNode(range);

        while (true) {
            /*
            ModulePartNode's parent is null
             */
            if (nonTerminalNode.parent() != null && !withinTextRange(txtPos, nonTerminalNode)) {
                nonTerminalNode = nonTerminalNode.parent();
                continue;
            }
            break;
        }

        context.setNodeAtCursor(nonTerminalNode);
    }

    private static boolean withinTextRange(int position, NonTerminalNode node) {
        TextRange rangeWithMinutiae = node.textRangeWithMinutiae();
        TextRange textRange = node.textRange();
        TextRange leadingMinutiaeRange = TextRange.from(rangeWithMinutiae.startOffset(),
                textRange.startOffset() - rangeWithMinutiae.startOffset());
        return leadingMinutiaeRange.endOffset() <= position;
    }
}
