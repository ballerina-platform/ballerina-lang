package io.ballerina.generators;

import io.ballerina.generators.exceptions.MetadataToRecordGeneratorException;
import io.ballerina.generators.platform.MetadataConverter;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.concurrent.CompletableFuture;

import static io.ballerina.generators.MetadataToRecordConstants.CAPABILITY_NAME;

/**
 * The extended service for the custom record generation from metadata.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("customRecordGenerator")
public class MetadataToRecordService implements ExtendedLanguageServerService {

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager) {
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    @JsonRequest
    public CompletableFuture<MetadataToRecordSchemaResponse> generate
            (MetadataToRecordSchemaRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            MetadataToRecordSchemaResponse response = null;
            String metadata = request.getMetadata();
            String schemaSource = request.getSchemaSource();
            try {
                MetadataToRecordConverterFactory factory = new MetadataToRecordConverterFactory();
                MetadataConverter source = factory.getConvert(schemaSource);
                response = source.convert(metadata);
            } catch (MetadataToRecordGeneratorException e) {
                response = new MetadataToRecordSchemaResponse();
                response.setCodeBlock("");
            }
            return response;
        });
    }
    @Override
    public String getName() {
        return CAPABILITY_NAME;
    }
}
