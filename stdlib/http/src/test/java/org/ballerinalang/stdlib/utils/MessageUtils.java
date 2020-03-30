/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.utils;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpResourceArguments;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.Map;

/**
 * RequestResponseUtil Class contains method for generating a message.
 *
 * @since 0.990.3
 */
public class MessageUtils {

    public static HTTPTestRequest generateHTTPMessage(String path, String method) {
        return generateHTTPMessage(path, method, null, null, -1);
    }

    public static HTTPTestRequest generateHTTPMessage(String path, String method, int listenerPort) {
        return generateHTTPMessage(path, method, null, null, listenerPort);
    }

    public static HTTPTestRequest generateHTTPMessage(String path, String method, String payload) {
        return generateHTTPMessage(path, method, null, payload, -1);
    }

    public static HTTPTestRequest generateHTTPMessage(String path, String method, HttpHeaders headers, String payload) {
        return generateHTTPMessage(path, method, headers, payload, -1);
    }

    public static HTTPTestRequest generateHTTPMessage(String path, String method, HttpHeaders headers, String payload
            , int listenerPort) {
        HTTPTestRequest carbonMessage = getHttpTestRequest(path, method, listenerPort);
        HttpHeaders httpHeaders = carbonMessage.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, String> header : headers) {
                httpHeaders.set(header.getKey(), header.getValue());
            }
        }
        if (payload != null) {
            carbonMessage.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(payload.getBytes())));
        } else {
            carbonMessage.addHttpContent(new DefaultLastHttpContent());
        }
        carbonMessage.setLastHttpContentArrived();
        carbonMessage.setHttpVersion(HttpConstants.HTTP_VERSION_1_1);
        return carbonMessage;
    }

    public static HTTPTestRequest generateHTTPMessageForMultiparts(String path, String method) {
        return getHttpTestRequest(path, method, -1);
    }

    private static HTTPTestRequest getHttpTestRequest(String path, String method, int listenerPort) {
        HTTPTestRequest carbonMessage = new HTTPTestRequest();
        carbonMessage.setProperty(HttpConstants.PROTOCOL,
                HttpConstants.PROTOCOL_HTTP);
        carbonMessage.setProperty(HttpConstants.LISTENER_INTERFACE_ID,
                HttpConstants.DEFAULT_INTERFACE);
        // Set url
        carbonMessage.setProperty(HttpConstants.TO, path);
        carbonMessage.setRequestUrl(path);
        carbonMessage.setHttpMethod(method.trim().toUpperCase(Locale.getDefault()));
        int port = listenerPort == -1 ? 9090 : listenerPort;
        carbonMessage.setProperty(HttpConstants.LOCAL_ADDRESS,
                new InetSocketAddress(HttpConstants.HTTP_DEFAULT_HOST, port));
        carbonMessage.setProperty(HttpConstants.LISTENER_PORT, port);
        carbonMessage.setProperty(HttpConstants.RESOURCE_ARGS, new HttpResourceArguments());
        carbonMessage.setHttpVersion(HttpConstants.HTTP_VERSION_1_1);
        return carbonMessage;
    }
}
