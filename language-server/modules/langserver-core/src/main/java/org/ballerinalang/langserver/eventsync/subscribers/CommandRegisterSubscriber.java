/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.eventsync.subscribers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber;
import org.eclipse.lsp4j.ServerCapabilities;

import static org.ballerinalang.langserver.util.LSClientUtil.compileAndRegisterCommands;
import static org.ballerinalang.langserver.util.LSClientUtil.isDynamicCommandRegistrationSupported;

/**
 * Publishes command registering.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber")
public class CommandRegisterSubscriber implements EventSubscriber {
    public static final String NAME = "Command register subscriber";

    @Override
    public EventKind eventKind() {
        return EventKind.PROJECT_UPDATE;
    }
    
    @Override
    public void onEvent(ExtendedLanguageClient client, DocumentServiceContext context,
                        LanguageServerContext languageServerContext) {
        checkAndRegisterCommands(context);
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Calculates the commands dynamically added by compiler plugins of imported modules and register the new ones in
     * LS client side.
     *
     * @param context           Document service context
     */
    public static synchronized void checkAndRegisterCommands(DocumentServiceContext context) {
        LanguageServerContext serverContext = context.languageServercontext();
        LSClientLogger clientLogger = LSClientLogger.getInstance(serverContext);

        ServerCapabilities serverCapabilities = serverContext.get(ServerCapabilities.class);
        if (serverCapabilities.getExecuteCommandProvider() == null) {
            clientLogger.logTrace("Not registering commands: server isn't a execute commands provider");
            return;
        }

        if (!isDynamicCommandRegistrationSupported(serverContext)) {
            clientLogger.logTrace("Not registering commands: client doesn't support dynamic commands registration");
            return;
        }
        context.workspace().waitAndGetPackageCompilation(context.filePath())
                .ifPresent(pkgCompilation 
                        -> compileAndRegisterCommands(serverContext, clientLogger, serverCapabilities, pkgCompilation));
    }
}
