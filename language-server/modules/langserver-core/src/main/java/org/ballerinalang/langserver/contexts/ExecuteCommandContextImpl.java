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
package org.ballerinalang.langserver.contexts;

import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.List;

/**
 * Language server context implementation.
 *
 * @since 1.2.0
 */
public class ExecuteCommandContextImpl extends AbstractWorkspaceServiceContext implements ExecuteCommandContext {

    private final List<CommandArgument> arguments;

    private final LSClientCapabilities clientCapabilities;

    private final BallerinaLanguageServer languageServer;

    ExecuteCommandContextImpl(WorkspaceManager wsManager,
                              LanguageServerContext serverContext,
                              List<CommandArgument> arguments,
                              LSClientCapabilities clientCapabilities,
                              BallerinaLanguageServer languageServer) {
        super(LSContextOperation.WS_EXEC_CMD, wsManager, serverContext);
        this.arguments = arguments;
        this.clientCapabilities = clientCapabilities;
        this.languageServer = languageServer;
    }

    @Override
    public List<CommandArgument> getArguments() {
        return this.arguments;
    }

    @Override
    public LSClientCapabilities getClientCapabilities() {
        return this.clientCapabilities;
    }

    @Override
    public LanguageServer getLanguageServer() {
        return this.languageServer;
    }

    @Override
    public ExtendedLanguageClient getLanguageClient() {
        return this.languageServer.getClient();
    }

    /**
     * Represents Language server completion context Builder.
     *
     * @since 2.0.0
     */
    protected static class ExecuteCommandContextBuilder extends AbstractContextBuilder<ExecuteCommandContextBuilder> {

        private final List<CommandArgument> arguments;

        private final LSClientCapabilities clientCapabilities;

        private final BallerinaLanguageServer languageServer;

        private final LanguageServerContext serverContext;

        /**
         * Context Builder constructor.
         */
        public ExecuteCommandContextBuilder(LanguageServerContext serverContext,
                                            List<CommandArgument> arguments,
                                            LSClientCapabilities clientCapabilities,
                                            BallerinaLanguageServer languageServer) {
            super(LSContextOperation.WS_EXEC_CMD, serverContext);
            this.serverContext = serverContext;
            this.arguments = arguments;
            this.clientCapabilities = clientCapabilities;
            this.languageServer = languageServer;
        }

        public ExecuteCommandContext build() {
            return new ExecuteCommandContextImpl(
                    this.wsManager,
                    this.serverContext,
                    this.arguments,
                    this.clientCapabilities,
                    this.languageServer);
        }

        @Override
        public ExecuteCommandContextBuilder self() {
            return this;
        }
    }
}
