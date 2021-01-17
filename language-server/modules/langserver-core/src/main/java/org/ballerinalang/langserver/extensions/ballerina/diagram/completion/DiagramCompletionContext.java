package org.ballerinalang.langserver.extensions.ballerina.diagram.completion;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.PositionedOperationContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.contexts.AbstractDocumentServiceContext;
import org.eclipse.lsp4j.Position;

public class DiagramCompletionContext extends AbstractDocumentServiceContext implements PositionedOperationContext {
    private int offset = -1;
    private final Position position;
    private Token tokenAtCursor;
    private NonTerminalNode nodeAtCursor;

    public DiagramCompletionContext(LSOperation operation,
                             String uri,
                             WorkspaceManager wsManager,
                             LanguageServerContext serverContext,
                             Position position) {
        super(operation, uri, wsManager, serverContext);
        this.position = position;
    }

    @Override
    public void setCursorPositionInTree(int offset) {
        this.offset = offset;
    }

    @Override
    public int getCursorPositionInTree() {
        return this.offset;
    }

    @Override
    public Position getCursorPosition() {
        return this.position;
    }

    public void setTokenAtCursor(Token tokenAtCursor) {
        this.tokenAtCursor = tokenAtCursor;
    }

    public Token getTokenAtCursor() {
        return tokenAtCursor;
    }

    public void setNodeAtCursor(NonTerminalNode nodeAtCursor) {
        this.nodeAtCursor = nodeAtCursor;
    }

    public NonTerminalNode getNodeAtCursor() {
        return nodeAtCursor;
    }
}
