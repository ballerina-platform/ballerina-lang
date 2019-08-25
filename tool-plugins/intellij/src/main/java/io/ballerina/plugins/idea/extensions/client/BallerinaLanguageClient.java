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
import io.ballerina.plugins.idea.settings.debuglogs.LangServerDebugLogsSettings;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.wso2.lsp4intellij.client.ClientContext;
import org.wso2.lsp4intellij.client.DefaultLanguageClient;

import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_LS_DEBUG_LOG_PREFIX;

/**
 * Extended LSP client implementation for ballerina.
 */
public class BallerinaLanguageClient extends DefaultLanguageClient {

    private static final Logger LOGGER = Logger.getInstance(BallerinaLanguageClient.class);

    public BallerinaLanguageClient(ClientContext context) {
        super(context);
    }

    @Override
    public void logMessage(MessageParams messageParams) {
        String message = messageParams.getMessage();
        MessageType msgType = messageParams.getType();

        if (msgType == MessageType.Error) {
            LOGGER.error(messageParams);
        } else if (msgType == MessageType.Warning) {
            LOGGER.warn(message);
        } else if (msgType == MessageType.Info) {
            LOGGER.info(message);
        } else if (msgType == MessageType.Log &&
                LangServerDebugLogsSettings.getInstance().getIsLangServerDebugLogsEnabled()) {
            // We are using info logs here since there is no programmatic way to add debug logs to idea logs and
            // therefore user will have to enable it separately.
            LOGGER.info(BALLERINA_LS_DEBUG_LOG_PREFIX + message);
        } else {
            LOGGER.warn("Unknown message type for " + message);
        }
    }
}
