package io.ballerina.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.converters.util.ConverterUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;


/**
 * The extended service for the JsonToBalRecord endpoint.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("LSExtensions")
public class JsonToRecordConverterService implements ExtendedLanguageServerService {
    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    @JsonRequest
    public CompletableFuture<JsonToRecordResponse> convertJsonToBalRecord(JsonToRecordRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<TypeDefinitionNode> nodes;
            JsonToRecordResponse response = new JsonToRecordResponse();
            try {
                String jsonString = request.getJsonString();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode inputJson = objectMapper.readTree(jsonString);
                if (inputJson.has("$schema")) {
                    nodes = JsonToRecordConverter.fromSchema(jsonString);
                } else {
                    nodes = JsonToRecordConverter.fromJSON(jsonString);
                }
                String codeBlock = ConverterUtils.typeNodesToFormattedString(nodes);
                response.setCodeBlock(codeBlock);
            } catch (Throwable e) {
                String msg = "Operation 'jsonToRecordConverter/convert' failed!";
                response.setCodeBlock("");
            }
            return response;
        });
    }
}
