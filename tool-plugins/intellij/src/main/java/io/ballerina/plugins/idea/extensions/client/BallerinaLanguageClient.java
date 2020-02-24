/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.ballerina.plugins.idea.extensions.client;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.remoteServer.util.CloudNotifier;
import io.ballerina.plugins.idea.settings.langserverlogs.LangServerLogsSettings;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.wso2.lsp4intellij.client.ClientContext;
import org.wso2.lsp4intellij.client.DefaultLanguageClient;

/**
 * Extended LSP client implementation for ballerina.
 */
public class BallerinaLanguageClient extends DefaultLanguageClient {

    private final ClientContext context;
    private static final Logger LOGGER = Logger.getInstance(BallerinaLanguageClient.class);
    private static final CloudNotifier notifier = new CloudNotifier("Ballerina LS Logs");

    public BallerinaLanguageClient(ClientContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public void logMessage(MessageParams messageParams) {
        String message = messageParams.getMessage();
        MessageType msgType = messageParams.getType();

        // Todo - Revisit after the language server logger implementation is fixed with proper message types.
        LangServerLogsSettings logSettings = LangServerLogsSettings.getInstance(context.getProject());
        if (msgType == MessageType.Error && logSettings.isLangServerDebugLogsEnabled()) {
            notifier.showMessage(message, com.intellij.openapi.ui.MessageType.ERROR);
        } else if (msgType == MessageType.Warning && logSettings.isLangServerDebugLogsEnabled()) {
            notifier.showMessage(message, com.intellij.openapi.ui.MessageType.WARNING);
        } else if (msgType == MessageType.Info && logSettings.isLangServerTraceLogsEnabled()) {
            notifier.showMessage(message, com.intellij.openapi.ui.MessageType.INFO);
        } else if (msgType == MessageType.Log && logSettings.isLangServerDebugLogsEnabled()) {
            notifier.showMessage(message, com.intellij.openapi.ui.MessageType.INFO);
        } else if (msgType == null) {
            LOGGER.warn("unknown message type for " + message);
        }
    }
}
