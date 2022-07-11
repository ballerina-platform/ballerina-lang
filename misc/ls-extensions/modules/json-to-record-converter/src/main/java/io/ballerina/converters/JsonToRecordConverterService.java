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

//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.ballerina.converters.exception.JsonToRecordConverterException;
import io.ballerina.jsonmapper.JsonToRecordMapper;
import io.ballerina.jsonmapper.JsonToRecordResponse;
import io.ballerina.jsonmapper.diagnostic.DiagnosticMessage;
import io.ballerina.jsonmapper.diagnostic.DiagnosticUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * The extended service for the JsonToBalRecord endpoint.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("jsonToRecord")
public class JsonToRecordConverterService implements ExtendedLanguageServerService {

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    @JsonRequest
    public CompletableFuture<JsonToRecordResponse> convert(JsonToRecordRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            JsonToRecordResponse response = new JsonToRecordResponse();

            String jsonString = request.getJsonString();
            String recordName = request.getRecordName();
            boolean isRecordTypeDesc = request.getIsRecordTypeDesc();
            boolean isClosed = request.getIsClosed();

            try {
                JsonElement parsedJson = JsonParser.parseString(jsonString);
                if (parsedJson.isJsonObject() && parsedJson.getAsJsonObject().has("$schema")) {
                    try {
                        response.setCodeBlock(JsonToRecordConverter.convert(jsonString, recordName, isRecordTypeDesc,
                                isClosed).getCodeBlock());
                    } catch (IOException | JsonToRecordConverterException | FormatterException e) {
                        DiagnosticMessage message = DiagnosticMessage.jsonToRecordConverter100(null);
                        return DiagnosticUtils.getDiagnosticResponse(List.of(message), response);
                    }
                } else {
                    response = JsonToRecordMapper.convert(jsonString, recordName, isRecordTypeDesc, isClosed);
                }
            } catch (JsonSyntaxException e) {
                response = JsonToRecordMapper.convert(jsonString, recordName, isRecordTypeDesc, isClosed);
            }
            return response;

//            try {
//                JsonElement parsedJson = JsonParser.parseString(jsonString);
//
//                ObjectMapper objectMapper = new ObjectMapper();
//                JsonNode inputJson = objectMapper.readTree(jsonString);
//                if (inputJson.has("$schema")) {
//                    response.setCodeBlock(JsonToRecordConverter.convert(jsonString, recordName, isRecordTypeDesc,
//                            isClosed).getCodeBlock());
//                } else {
//                    String codeBloc = JsonToRecordMapper.convert(jsonString, recordName, isRecordTypeDesc, isClosed)
//                            .getCodeBlock();
//                    response.setCodeBlock("codeBloc");
//                    response.setDiagnostics(List.of("run"));
//                }
//            } catch (IOException | JsonToRecordConverterException | FormatterException e) {
//                response.setCodeBlock("exception");
//                response.setDiagnostics(List.of("run exception"));
//            }
//            return response;
        });
    }

    @Override
    public String getName() {
        return Constants.CAPABILITY_NAME;
    }
}
