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
package org.ballerinalang.langserver.commons.capability;

import org.ballerinalang.langserver.commons.registration.BallerinaClientCapability;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;

import java.util.List;
import java.util.Map;

/**
 * This class represents the resolved capabilities for the language server.
 *
 * @since 1.2.0
 */
public interface LSClientCapabilities {

    /**
     * Returns map of capabilities.
     *
     * @return {@link Map} of capabilities
     */
    ExperimentalClientCapabilities getExperimentalCapabilities();

    /**
     * Initialization options sent by the client.
     *
     * @return Initialization options
     */
    InitializationOptions getInitializationOptions();

    /**
     * Returns Workspace client capabilities.
     *
     * @return {@link WorkspaceClientCapabilities}
     */
    WorkspaceClientCapabilities getWorkspaceCapabilities();

    /**
     * Returns text document client capabilities.
     *
     * @return {@link TextDocumentClientCapabilities}
     */
    TextDocumentClientCapabilities getTextDocCapabilities();

    /**
     * Set the ballerina client capabilities.
     *
     * @param capabilities {@link BallerinaClientCapability}
     */
    void setBallerinaClientCapabilities(List<BallerinaClientCapability> capabilities);

    /**
     * Get the ballerina client capabilities.
     *
     * @return {@link List} of client capabilities
     */
    List<BallerinaClientCapability> getBallerinaClientCapabilities();
}
