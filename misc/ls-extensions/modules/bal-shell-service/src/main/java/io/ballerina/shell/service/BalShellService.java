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
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("balShell")
public class BalShellService implements ExtendedLanguageServerService {
    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    /***
     * Execute a given source with shell implementation and provide result.
     *
     * @param request content of the source snippet
     * @return evaluated result of the snippet in BalShellGetResulResponse
     */
    @JsonRequest
    public CompletableFuture<BalShellGetResultResponse> getResult(BalShellGetResultRequest request) {
        return CompletableFuture.supplyAsync(() -> ShellWrapper.getInstance().getResult(request.getSource()));
    }

    /**
     * Creates a copy of temporary bal file used by shell with the content of it and
     * returns the uri of newly created file and its content.
     *
     * @return new file details in BalFileSourceResponse
     */
    @JsonRequest
    public CompletableFuture<ShellFileSourceResponse> getShellFileSource() {
        return CompletableFuture.supplyAsync(() -> ShellWrapper.getInstance().getShellFileSource());
    }

    /**
     * Get available variables in the memory with their name, type and current state value.
     * @return available variables
     */
    @JsonRequest
    public CompletableFuture<List<Map<String, String>>> getVariableValues() {
        return CompletableFuture.supplyAsync(() -> ShellWrapper.getInstance().getAvailableVariables());
    }

    /**
     * Delete defined variables and module declarations from the context.
     *
     * @param request defined variable or declaration that need to be deleted
     * @return whether that deletion was successful
     */
    @JsonRequest
    public CompletableFuture<Boolean> deleteDeclarations(DeleteRequest request) {
        return CompletableFuture.supplyAsync(() ->
                ShellWrapper.getInstance().deleteDeclarations(request.getVarToDelete()));
    }

    /**
     * Resets the shell into initial state.
     *
     * @return whether that restart was successful
     */
    @JsonRequest
    public CompletableFuture<Boolean> restartNotebook() {
        return CompletableFuture.supplyAsync(() -> ShellWrapper.getInstance().restart());
    }

    @Override
    public String getName() {
        return Constants.CAPABILITY_NAME;
    }
}
