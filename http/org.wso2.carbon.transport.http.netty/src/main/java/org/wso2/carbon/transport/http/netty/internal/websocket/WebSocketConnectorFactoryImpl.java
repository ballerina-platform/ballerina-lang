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
 *
 */

package org.wso2.carbon.transport.http.netty.internal.websocket;

import org.wso2.carbon.connector.framework.websocket.WebSocketConnectorFactory;
import org.wso2.carbon.connector.framework.websocket.WebSocketObserver;

import java.util.Properties;
import javax.websocket.Session;

/**
 * Implementation of WebSocket connector factory.
 */
public class WebSocketConnectorFactoryImpl implements WebSocketConnectorFactory {

    @Override
    public WebSocketObserver getServerConnector(Properties properties) {
        return null;
    }

    @Override
    public Session getClientConnector(Properties properties) {
        return null;
    }
}
