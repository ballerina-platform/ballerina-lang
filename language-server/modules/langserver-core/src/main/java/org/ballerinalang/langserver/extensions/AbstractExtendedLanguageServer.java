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
package org.ballerinalang.langserver.extensions;

import com.google.common.base.Objects;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.workspace.BallerinaBallerinaWorkspaceManagerProxyImpl;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManagerProxy;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethodProvider;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;

/**
 * Provides capabilities for the extended language server services.
 *
 * @see ExtendedLanguageServerService
 * @since 2.0.0
 */
public abstract class AbstractExtendedLanguageServer implements LanguageServer, Endpoint, JsonRpcMethodProvider {
    protected List<ExtendedLanguageServerService> extendedServices = new ArrayList<>();
    private Map<String, JsonRpcMethod> supportedMethods;
    private final Multimap<String, Endpoint> extensionServices = LinkedListMultimap.create();
    protected final LanguageServerContext serverContext;
    protected BallerinaWorkspaceManagerProxy workspaceManagerProxy;

    public AbstractExtendedLanguageServer(LanguageServerContext serverContext) {
        this.workspaceManagerProxy = new BallerinaBallerinaWorkspaceManagerProxyImpl(serverContext);
        this.serverContext = serverContext;
        ServiceLoader<ExtendedLanguageServerService> serviceLoader = ServiceLoader.load(
                ExtendedLanguageServerService.class);
        for (ExtendedLanguageServerService service : serviceLoader) {
            extendedServices.add(service);
        }
    }

    @Override
    public Map<String, JsonRpcMethod> supportedMethods() {
        if (this.supportedMethods != null) {
            return this.supportedMethods;
        }
        synchronized (this.extensionServices) {
            Map<String, JsonRpcMethod> supportedMethods = new LinkedHashMap<>(
                    ServiceEndpoints.getSupportedMethods(getClass()));

            Map<String, JsonRpcMethod> extensions = new LinkedHashMap<>();
            for (ExtendedLanguageServerService ext : this.extendedServices) {
                if (ext != null) {
                    ext.init(this, this.workspaceManagerProxy, serverContext);
                    Map<String, JsonRpcMethod> supportedExtensions = ext.supportedMethods();
                    for (Map.Entry<String, JsonRpcMethod> entry : supportedExtensions.entrySet()) {
                        if (supportedMethods.containsKey(entry.getKey())) {
                            String msg = "The json rpc method '" + entry.getKey()
                                    + "' can not be an extension as it is already defined in the LSP standard.";
                            LSClientLogger.getInstance(this.serverContext)
                                    .logError(null, msg, new RuntimeException(msg), null, (Position) null);
                        } else {
                            JsonRpcMethod existing = extensions.put(entry.getKey(), entry.getValue());
                            if (existing != null && !Objects.equal(existing, entry.getValue())) {
                                String msg = "An incompatible LSP extension '" + entry.getKey()
                                        + "' has already been registered. Using 1 ignoring 2. \n1 : " +
                                        existing + " \n2 : " + entry.getValue();
                                LSClientLogger.getInstance(this.serverContext)
                                        .logError(null, msg, new RuntimeException(msg), null, (Position) null);
                                extensions.put(entry.getKey(), existing);
                            } else {
                                Endpoint endpoint = ServiceEndpoints.toEndpoint(ext);
                                this.extensionServices.put(entry.getKey(), endpoint);
                                supportedMethods.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }
            }
            this.supportedMethods = supportedMethods;
            return supportedMethods;
        }
    }

    @Override
    public void notify(String method, Object parameter) {
        for (Endpoint endpoint : extensionServices.get(method)) {
            endpoint.notify(method, parameter);
        }
    }

    @Override
    public CompletableFuture<?> request(String method, Object parameter) {
        if (!extensionServices.containsKey(method)) {
            throw new UnsupportedOperationException("The json request '" + method + "' is unknown.");
        }
        for (Endpoint endpoint : extensionServices.get(method)) {
            return endpoint.request(method, parameter);
        }
        return null;
    }

    public WorkspaceManager getWorkspaceManager() {
        return this.workspaceManagerProxy.get();
    }

    public LanguageServerContext getServerContext() {
        return serverContext;
    }
}
