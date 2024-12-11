/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
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

package org.ballerinalang.debugadapter;

import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import java.util.concurrent.CompletableFuture;

/**
 * Extended debug server interface for Ballerina debug adapter.
 *
 * @since 2201.11.0
 */
public interface BallerinaExtendedDebugServer extends IDebugProtocolServer {

    /**
     * Custom request to forward the program output from the remote VM (initiated by the debug client) to the debug
     * server. This extension is specifically designed for fast-run mode, enabling the debug adapter to forward the
     * program output to the client.
     *
     * @param arguments the output event arguments
     * @return a CompletableFuture representing the completion of the request
     */
    @JsonRequest
    CompletableFuture<Void> output(OutputEventArguments arguments);
}
