package org.ballerinalang.langserver.extensions.ballerina.diagram;

import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.util.concurrent.CompletableFuture;

@JsonSegment("diagram")
public interface DiagramService {
    CompletableFuture<Object> completions(CompletionParams completionParams);
}
