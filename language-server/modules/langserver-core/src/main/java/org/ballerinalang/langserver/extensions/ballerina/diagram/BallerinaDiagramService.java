package org.ballerinalang.langserver.extensions.ballerina.diagram;

import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.extensions.ballerina.diagram.completion.DiagramCompletionContext;
import org.ballerinalang.langserver.extensions.ballerina.diagram.completion.DiagramCompletionUtil;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Position;

import java.util.concurrent.CompletableFuture;

public class BallerinaDiagramService implements DiagramService {

    private final BallerinaLanguageServer languageServer;
    private final WorkspaceManager workspaceManager;
    private final LanguageServerContext serverContext;
    private final LSClientLogger clientLogger;

    public BallerinaDiagramService(BallerinaLanguageServer languageServer,
                                   WorkspaceManager workspaceManager,
                                   LanguageServerContext serverContext) {
        this.workspaceManager = workspaceManager;
        this.languageServer = languageServer;
        this.serverContext = serverContext;
        this.clientLogger = LSClientLogger.getInstance(this.serverContext);
    }

    @Override
    public CompletableFuture<Object> completions(CompletionParams completionParams) {
        return CompletableFuture.supplyAsync(() -> {
            String uri = completionParams.getTextDocument().getUri();
            Position position = completionParams.getPosition();
            DiagramCompletionContext completionContext = this.buildDiagramCompletionContext(uri, position);

            try {
                DiagramCompletionUtil.fillTokenInfoAtCursor(completionContext);
                
            } catch (TokenOrSymbolNotFoundException e) {
                // Ignore
            } catch (Throwable e) {
                // Note: Not catching UserErrorException separately to avoid flooding error msgs popups
                String msg = "Operation '" + DiagramContextOperation.DIAGRAM_COMPLETION + "' failed!";
                this.clientLogger.logError(msg, e, completionParams.getTextDocument(), completionParams.getPosition());
            }
            return null;
        });
    }

    private DiagramCompletionContext buildDiagramCompletionContext(String uri, Position position) {
        return new DiagramCompletionContext(DiagramContextOperation.DIAGRAM_COMPLETION, uri, this.workspaceManager,
                this.serverContext, position);
    }
}
