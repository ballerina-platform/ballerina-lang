/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.converters.exception.ConverterException;
import io.ballerina.converters.util.ConverterUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;


/**
 * The extended service for the JsonToBalRecord endpoint.
 *
 * @since 2.0.0
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
                nodes = JsonToRecordConverter.convert(jsonString);
                String codeBlock = ConverterUtils.typeNodesToFormattedString(nodes);
                response.setCodeBlock(codeBlock);
            } catch (IOException | ConverterException e) {
                response.setCodeBlock("");
            }
            return response;
        });
    }
}
