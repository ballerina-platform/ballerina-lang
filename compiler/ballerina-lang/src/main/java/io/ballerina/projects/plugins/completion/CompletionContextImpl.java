package io.ballerina.projects.plugins.completion;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.projects.Document;
import io.ballerina.projects.plugins.codeaction.PositionedActionContextImpl;
import io.ballerina.tools.text.LinePosition;

import java.nio.file.Path;

/**
 * Implementation of completion plugin context.
 *
 * @since 2201.6.0
 */
public class CompletionContextImpl extends PositionedActionContextImpl implements CompletionContext {

    private final Node nodeAtCursor;
    private final int cursorPosInTree;

    protected CompletionContextImpl(String fileUri,
                                    Path filePath,
                                    Node nodeAtCursor,
                                    int cursorPosInTree,
                                    LinePosition cursorPosition,
                                    Document document,
                                    SemanticModel semanticModel) {
        super(fileUri, filePath, cursorPosition, document, semanticModel);
        this.nodeAtCursor = nodeAtCursor;
        this.cursorPosInTree = cursorPosInTree;
    }

    @Override
    public Node nodeAtCursor() {
        return nodeAtCursor;
    }

    @Override
    public int cursorPosInTree() {
        return this.cursorPosInTree;
    }

    public static CompletionContextImpl from(String fileUri,
                                             Path filePath,
                                             LinePosition cursorPosition,
                                             int cursorPosInTree,
                                             Node nodeAtCursor,
                                             Document document,
                                             SemanticModel semanticModel) {
        return new CompletionContextImpl(fileUri, filePath, nodeAtCursor, cursorPosInTree,
                cursorPosition, document, semanticModel);
    }
}
