/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.commons.service.spi;

import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethodProvider;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents extended language server service interface.
 *
 * @since 2.0.0
 */
public interface ExtendedLanguageServerService extends JsonRpcMethodProvider {

    /**
     * Initialize callback for the service.
     *
     * @param documentManager document manager
     * @param langServer      language server
     */
    default void init(WorkspaceDocumentManager documentManager, LanguageServer langServer) {
    }

    /**
     * Callback when client connected.
     *
     * @param client {@link ExtendedLanguageClient}
     */
    default void connect(ExtendedLanguageClient client) {
    }

    /**
     * Callback for shutdown.
     */
    default void shutdown() {
    }

    /**
     * Callback for exit.
     *
     * @param exitStatus exit status
     */
    default void exit(int exitStatus) {
    }

    /**
     * Returns remote interface for the extended service.
     *
     * @return remote interface
     */
    Class<?> getRemoteInterface();

    @Override
    default Map<String, JsonRpcMethod> supportedMethods() {
        Map<String, JsonRpcMethod> supportedMethods = new HashMap<>();
        supportedMethods.putAll(ServiceEndpoints.getSupportedMethods(getClass()));
        supportedMethods.putAll(ServiceEndpoints.getSupportedMethods(getRemoteInterface()));
        return supportedMethods;
    }
}
