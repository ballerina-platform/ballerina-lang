/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.commons;

import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.List;

/**
 * Represents the Command execution operation context.
 *
 * @since 2.0.0
 */
public interface ExecuteCommandContext extends WorkspaceServiceContext {

    /**
     * Get the command execution's arguments.
     *
     * @return {@link List} of arguments
     */
    List<CommandArgument> getArguments();

    /**
     * Get the lang server client capabilities.
     *
     * @return {@link LSClientCapabilities)
     */
    LSClientCapabilities getClientCapabilities();

    /**
     * Get the language server instance.
     *
     * @return {@link LanguageServer}
     */
    LanguageServer getLanguageServer();

    /**
     * Get the language client.
     * 
     * @return {@link LanguageClient}
     */
    ExtendedLanguageClient getLanguageClient();
}
