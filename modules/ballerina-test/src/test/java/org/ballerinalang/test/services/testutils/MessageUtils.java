/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.services.testutils;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.net.http.Constants;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.StatusCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.websocket.Session;

/**
 * RequestResponseUtil Class contains method for generating a message.
 *
 * @since 0.8.0
 */
public class MessageUtils {

    public static CarbonMessage generateRawMessage() {
        return new DefaultCarbonMessage();
    }

    public static HTTPTestRequest generateHTTPMessage(String path, String method) {
        return generateHTTPMessage(path, method, null, null);
    }

    public static HTTPTestRequest generateHTTPMessage(String path, String method, String payload) {
        return generateHTTPMessage(path, method, null, payload);
    }

    public static HTTPTestRequest generateHTTPMessage(String path, String method, List<Header> headers,
            String payload) {
        HTTPTestRequest carbonMessage = new HTTPTestRequest();

        // Set meta data
        carbonMessage.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL,
                Constants.PROTOCOL_HTTP);
        carbonMessage.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_INTERFACE_ID,
                Constants.DEFAULT_INTERFACE);
        // Set url
        carbonMessage.setProperty(org.wso2.carbon.messaging.Constants.TO, path);

        // Set method
        carbonMessage.setProperty(Constants.HTTP_METHOD, method.trim().toUpperCase(Locale.getDefault()));

        // Set Headers
        HttpHeaders httpHeaders = carbonMessage.getHeaders();
        if (headers != null) {
            for (Header header : headers) {
                httpHeaders.set(header.getName(), header.getValue());
            }
        }

        // Set message body
        if (payload != null) {
            carbonMessage.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(payload.getBytes())));
        } else {
            carbonMessage.setEndOfMsgAdded(true);
        }
        return carbonMessage;
    }

    public static CarbonMessage generateWebSocketTextMessage(String text, Session session, String path) {
        TextCarbonMessage textMessage = new TextCarbonMessage(text);
        return setWebSocketCommonProperties(textMessage, session, path);
    }

    public static CarbonMessage generateWebSocketOnOpenMessage(Session session, String path) {
        StatusCarbonMessage statusCarbonMessage = new StatusCarbonMessage(
                org.wso2.carbon.messaging.Constants.STATUS_OPEN, 0, null);
        statusCarbonMessage.setProperty(org.ballerinalang.net.ws.Constants.CONNECTION, org
                .ballerinalang.net.ws.Constants.UPGRADE);
        statusCarbonMessage.setProperty(org.ballerinalang.net.ws.Constants.UPGRADE, org
                .ballerinalang.net.ws.Constants.WEBSOCKET_UPGRADE);
        return setWebSocketCommonProperties(statusCarbonMessage, session, path);
    }

    public static CarbonMessage generateWebSocketOnOpenMessage(Session session, String path,
                                                               Map<String, String> headers) {
        StatusCarbonMessage statusCarbonMessage = new StatusCarbonMessage(
                org.wso2.carbon.messaging.Constants.STATUS_OPEN, 0, null);
        statusCarbonMessage.setProperty(org.ballerinalang.net.ws.Constants.CONNECTION, org
                .ballerinalang.net.ws.Constants.UPGRADE);
        statusCarbonMessage.setProperty(org.ballerinalang.net.ws.Constants.UPGRADE, org
                .ballerinalang.net.ws.Constants.WEBSOCKET_UPGRADE);
        headers.entrySet().stream().forEach(
                entry -> {
                    statusCarbonMessage.setHeader(entry.getKey(), entry.getValue());
                }
        );
        return setWebSocketCommonProperties(statusCarbonMessage, session, path);
    }

    public static CarbonMessage generateWebSocketOnCloseMessage(Session session, String path) {
        StatusCarbonMessage statusCarbonMessage = new StatusCarbonMessage(
                org.wso2.carbon.messaging.Constants.STATUS_CLOSE, 1000, "Normal closure");
        return setWebSocketCommonProperties(statusCarbonMessage, session, path);
    }

    private static CarbonMessage setWebSocketCommonProperties(CarbonMessage carbonMessage, Session session,
                                                              String path) {
        carbonMessage.setProperty(Constants.PROTOCOL,
                                  org.ballerinalang.net.ws.Constants.PROTOCOL_WEBSOCKET);
        carbonMessage.setProperty(Constants.TO, path);
        carbonMessage.setProperty(org.ballerinalang.net.ws.Constants.IS_WEBSOCKET_SERVER, true);
        carbonMessage.setProperty(
                org.ballerinalang.net.ws.Constants.WEBSOCKET_SERVER_SESSION, session);
        carbonMessage.setProperty(org.ballerinalang.net.ws.Constants.WEBSOCKET_CLIENT_SESSIONS_LIST,
                                  new LinkedList<>());
        return carbonMessage;
    }
}
