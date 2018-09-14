package org.ballerinalang.langserver.extensions.ballerina.traces;

import com.google.gson.JsonObject;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
/**
 * An extension interface for Language server to add features related to ballerina files.
 */
@JsonSegment("ballerinaTrace")
public interface BallerinaTraceService {

    @JsonNotification("pushLogToClient")
    void pushLogToClient(JsonObject o);
}
