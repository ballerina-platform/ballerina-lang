/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.shell.service;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The extended service for the BalShell endpoint.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("balShell")
public class BalShellService implements ExtendedLanguageServerService {
    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    @JsonRequest
    public CompletableFuture<BalShellGetResultResponse> getResult(BalShellGetResultRequest request) {
        return CompletableFuture.supplyAsync(() -> ShellWrapper.getInstance().getResult(request.getSource()));
    }

    @JsonRequest
    public CompletableFuture<ShellFileSourceResponse> getShellFileSource() {
        return CompletableFuture.supplyAsync(() -> ShellWrapper.getInstance().getShellFileSource());
    }

    @JsonRequest
    public CompletableFuture<List<Map<String, String>>> getVariableValues() {
        return CompletableFuture.supplyAsync(() -> ShellWrapper.getInstance().getAvailableVariables());
    }

    @JsonRequest
    public CompletableFuture<Boolean> deleteDeclarations(MetaInfo metaInfo) {
        return CompletableFuture.supplyAsync(() ->
                ShellWrapper.getInstance().deleteDeclarations(metaInfo.getDefinedVars(), metaInfo.getModuleDclns()));
    }

    @JsonRequest
    public CompletableFuture<Boolean> restartNotebook() {
        return CompletableFuture.supplyAsync(() -> ShellWrapper.getInstance().restart());
    }

    @Override
    public String getName() {
        return Constants.CAPABILITY_NAME;
    }
}
