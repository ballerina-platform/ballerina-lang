package org.ballerinalang.langserver.extensions.ballerina.document;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.Optional;

/**
 * This is the BallerinaSyntaxTreeByRange class for related utils in retrieving the ST for a given selection.
 */

public class BallerinaSyntaxTreeByRangeUtil {
    public static NonTerminalNode getNode(Range range, SyntaxTree syntaxTree) {
        TextDocument textDocument = syntaxTree.textDocument();
        Position rangeStart = range.getStart();
        Position rangeEnd = range.getEnd();
        int start = textDocument.textPositionFrom(LinePosition.from(rangeStart.getLine(), rangeStart.getCharacter()));
        int end = textDocument.textPositionFrom(LinePosition.from(rangeEnd.getLine(), rangeEnd.getCharacter()));
        return findNode(syntaxTree.rootNode(), TextRange.from(start, end - start));
    }

    private static NonTerminalNode findNode(ModulePartNode node, TextRange textRange) {
        TextRange textRangeWithMinutiae = node.textRangeWithMinutiae();
        if (textRangeWithMinutiae.startOffset() > textRange.startOffset() ||
                textRangeWithMinutiae.endOffset() < textRange.endOffset()) {
            throw new IllegalStateException("Invalid Text Range for: " + textRange.toString());
        }

        NonTerminalNode foundNode = null;
        Optional<Node> temp = Optional.of(node);
        while (temp.isPresent() && !(temp.get() instanceof Token)) {
            foundNode = (NonTerminalNode) temp.get();
            temp = findChildNode((NonTerminalNode) temp.get(), textRange);
        }

        return foundNode;
    }

    private static Optional<Node> findChildNode(NonTerminalNode node, TextRange textRange) {
        int offset = node.textRangeWithMinutiae().startOffset();

        for (Node internalChildNode: node.children()) {
            if (internalChildNode == null) {
                continue;
            }

            int offsetWithMinutiae = offset + internalChildNode.textRangeWithMinutiae().length();
            if (textRange.startOffset() >= offset && textRange.endOffset() <= offsetWithMinutiae) {
                return Optional.ofNullable(internalChildNode);
            } else if (textRange.startOffset() <= offset && textRange.endOffset() >= offsetWithMinutiae) {
                return Optional.empty();
            }

            offset += internalChildNode.textRangeWithMinutiae().length();
        }

        return Optional.empty();
    }
}
