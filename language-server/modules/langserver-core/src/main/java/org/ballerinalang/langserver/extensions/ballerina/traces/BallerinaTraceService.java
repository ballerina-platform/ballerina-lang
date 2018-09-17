package org.ballerinalang.langserver.extensions.ballerina.traces;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
/**
 * An extension interface for Language server to add features related to ballerina files.
 */
@JsonSegment("ballerinaTrace")
public interface BallerinaTraceService {

    @JsonNotification("pushLogToClient")
    void pushLogToClient(TraceRecord traceRecord);

}
