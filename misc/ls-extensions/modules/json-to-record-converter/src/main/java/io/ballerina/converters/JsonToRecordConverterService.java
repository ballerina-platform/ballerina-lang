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

import io.ballerina.converters.exception.JsonToRecordConverterException;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.io.IOException;
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
            JsonToRecordResponse response;
            try {
                String jsonString = request.getJsonString();
                String recordName = request.getRecordName();
                boolean isRecordTypeDesc = request.getIsRecordTypeDesc();
                boolean isClosed = request.getIsClosed();
                response = JsonToRecordConverter.convert(jsonString, recordName, isRecordTypeDesc, isClosed);
            } catch (IOException | JsonToRecordConverterException | FormatterException e) {
                response = new JsonToRecordResponse();
                response.setCodeBlock("");
            }
            return response;
        });
    }

    @Override
    public String getName() {
        return Constants.CAPABILITY_NAME;
    }
}
