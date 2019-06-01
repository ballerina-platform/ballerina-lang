package org.ballerinalang.langserver.extensions.ballerina.project;

import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaASTRequest;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaASTResponse;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.util.concurrent.CompletableFuture;

/**
 * An extension interface for Language server to add features related to ballerina files.
 *
 * @since 1.0.0
 */
@JsonSegment("ballerinaProject")
public interface BallerinaProjectService {
    @JsonRequest
    CompletableFuture<BallerinaASTResponse> modules(BallerinaASTRequest request);
}
