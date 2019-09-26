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

package org.ballerinalang.net.http.websocket.server;

import org.ballerinalang.net.http.HttpResourceArguments;
import org.ballerinalang.net.http.websocket.WebSocketException;
import org.ballerinalang.net.uri.URITemplate;
import org.ballerinalang.net.uri.URITemplateException;
import org.ballerinalang.net.uri.parser.Literal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Store all the WebSocket serviceEndpointsTemplate here.
 */
public class WebSocketServicesRegistry {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServicesRegistry.class);
    private URITemplate<WebSocketServerService, WebSocketMessage> uriTemplate;

    public WebSocketServicesRegistry() {
        try {
            uriTemplate = new URITemplate<>(new Literal<>(new WebSocketDataElement(), "/"));
        } catch (URITemplateException e) {
            // Ignore as it won't be thrown because token length is not zero.
        }
    }

    public void registerService(WebSocketServerService service) {
        String basePath = service.getBasePath();
        try {
            basePath = URLDecoder.decode(basePath, StandardCharsets.UTF_8.name());
            uriTemplate.parse(basePath, service, new WebSocketDataElementFactory());
        } catch (Exception e) {
            logger.error("Error when registering service", e);
            throw new WebSocketException(e);
        }
        logger.info("WebSocketService deployed : {} with context {}", service.getName(), basePath);
    }

    public WebSocketServerService findMatching(String path, HttpResourceArguments pathParams,
                                               WebSocketHandshaker webSocketHandshaker) {
        return uriTemplate.matches(path, pathParams, webSocketHandshaker);
    }
}
