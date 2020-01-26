/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.command.executors;

import com.google.gson.JsonObject;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;

import static org.ballerinalang.langserver.command.CommandUtil.notifyClient;

/**
 * Command executor for pulling a package from central.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.command.LSCommandExecutor")
public class MessageExecutor implements LSCommandExecutor {

    public static final String COMMAND = "MESSAGE";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        // Derive message type and message
        MessageType messageType = null;
        String message = "";
        for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            String argKey = ((JsonObject) arg).get(ARG_KEY).getAsString();
            String argVal = ((JsonObject) arg).get(ARG_VALUE).getAsString();
            if (argKey.equals(CommandConstants.ARG_KEY_MESSAGE_TYPE)) {
                messageType = MessageType.forValue(Integer.parseInt(argVal));
            } else if (argKey.equals(CommandConstants.ARG_KEY_MESSAGE)) {
                message = argVal;
            }
        }
        // If no package, or no doc uri; then just skip
        if (messageType == null || message.isEmpty()) {
            return new Object();
        }
        LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_SERVER_KEY).getClient();
        client.showMessage(new MessageParams());
        notifyClient(client, messageType, message);
        return new Object();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
