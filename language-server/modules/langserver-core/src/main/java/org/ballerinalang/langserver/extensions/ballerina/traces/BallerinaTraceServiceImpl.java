package org.ballerinalang.langserver.extensions.ballerina.traces;

import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSGlobalContext;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Ballerina Traces extension for Language Server.
 */
public class BallerinaTraceServiceImpl implements BallerinaTraceService {
    private static final Logger logger = LoggerFactory.getLogger(BallerinaTraceServiceImpl.class);

    private final BallerinaLanguageServer ballerinaLanguageServer;
    private final WorkspaceDocumentManager documentManager;

    public BallerinaTraceServiceImpl(LSGlobalContext globalContext) {
        this.ballerinaLanguageServer = globalContext.get(LSGlobalContextKeys.LANGUAGE_SERVER_KEY);
        this.documentManager = globalContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
    }

    @Override
    public void pushLogToClient(TraceRecord traceRecord) {
        ExtendedLanguageClient client = this.ballerinaLanguageServer.getClient();
        if (client == null) {
            return;
        }
        client.traceLogs(traceRecord);
    }
}
