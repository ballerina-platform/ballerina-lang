/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.composer.service.ballerina.langserver.service;

import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.Message;

import java.nio.charset.StandardCharsets;

/**
 * WS Based message consumer for language JSON RPC Server.
 */
public class WSRPCMessageConsumer implements MessageConsumer {

    private final String encoding = StandardCharsets.UTF_8.name();
    private final Object outputLock = new Object();
    private final MessageJsonHandler jsonHandler;
    private final BallerinaLangServerService wsLangServer;

    public WSRPCMessageConsumer(BallerinaLangServerService wsLangServer, MessageJsonHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
        this.wsLangServer = wsLangServer;
    }

    @Override
    public void consume(Message message) {
        if (message.getJsonrpc() == null) {
            message.setJsonrpc("2.0");
        }
        String content = this.jsonHandler.serialize(message);
        String header = this.getHeader(content.length());
        Object var7 = this.outputLock;
        synchronized (this.outputLock) {
            this.wsLangServer.sendMessageToAll(header + content);
        }
    }

    protected String getHeader(int contentLength) {
        StringBuilder headerBuilder = new StringBuilder();
        // TODO: Use HTTpHeaderNames constants instead
        this.appendHeader(headerBuilder, "Content-Length", contentLength).append("\r\n");
        if (!StandardCharsets.UTF_8.name().equals(this.encoding)) {
            this.appendHeader(headerBuilder, "Content-Type", "application/json");
            headerBuilder.append("; charset=").append(this.encoding).append("\r\n");
        }

        headerBuilder.append("\r\n");
        return headerBuilder.toString();
    }

    protected StringBuilder appendHeader(StringBuilder builder, String name, Object value) {
        return builder.append(name).append(": ").append(value);
    }
}
