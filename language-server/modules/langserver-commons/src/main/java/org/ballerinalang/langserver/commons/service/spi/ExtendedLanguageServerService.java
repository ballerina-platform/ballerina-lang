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

import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManagerProxy;
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
     * @param langServer language server
     * @param workspaceManager workspace manager instance
     */
    default void init(LanguageServer langServer, WorkspaceManager workspaceManager) {
    }

    /**
     * Initialize callback for the service.
     *
     * @param langServer language server
     * @param workspaceManager workspace manager instance
     * @param serverContext language server context
     */
    default void init(LanguageServer langServer,
                      WorkspaceManager workspaceManager,
                      LanguageServerContext serverContext) {
        init(langServer, workspaceManager);
    }

    /**
     * Initialize callback for the service.
     *
     * @param langServer language server
     * @param workspaceManagerProxy workspace manager proxy instance
     * @param serverContext language server context
     */
    default void init(LanguageServer langServer,
                      WorkspaceManagerProxy workspaceManagerProxy,
                      LanguageServerContext serverContext) {
        init(langServer, workspaceManagerProxy.get(), serverContext);
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

    /**
     * Get the name of the service. Each extension service MUST have an associated client and server capabilities 
     * defined. Each client capability and server capability pair has unique and same name and MUST be the same as the
     * service name.
     *
     * @return {@link 2.0.0}
     */
    default String getName() {
        throw new RuntimeException("Method not implemented");
    }

    @Override
    default Map<String, JsonRpcMethod> supportedMethods() {
        Map<String, JsonRpcMethod> supportedMethods = new HashMap<>();
        supportedMethods.putAll(ServiceEndpoints.getSupportedMethods(getClass()));
        supportedMethods.putAll(ServiceEndpoints.getSupportedMethods(getRemoteInterface()));
        return supportedMethods;
    }
}
