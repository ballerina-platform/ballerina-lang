/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.composer.service.workspace.langserver;

import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.ballerinalang.composer.service.workspace.langserver.dto.RequestMessage;
import org.ballerinalang.composer.service.workspace.langserver.dto.TextDocumentPositionParams;

/**
 * Message utils for manipulating the Message objects
 */
public class MessageUtil {
    private static final String METHOD_COMPLETION = "textDocument/completion";

    /**
     * Get a new request message from the content
     * @param balContent text content
     * @param position position of the cursor
     * @param id message id
     * @return {@link RequestMessage}
     */
    public static RequestMessage getRequestMessage(String balContent, Position position, String id) {
        RequestMessage requestMessage = new RequestMessage();
        TextDocumentPositionParams textDocumentPositionParams = new TextDocumentPositionParams();

        textDocumentPositionParams.setFilePath("/");
        textDocumentPositionParams.setFileName("untitled");
        textDocumentPositionParams.setPackageName(".");
        textDocumentPositionParams.setPosition(position);
        textDocumentPositionParams.setText(balContent);

        requestMessage.setId(id);
        requestMessage.setMethod(METHOD_COMPLETION);
        requestMessage.setParams(textDocumentPositionParams);
        requestMessage.setJsonrpc("2.0");

        return requestMessage;
    }
}
