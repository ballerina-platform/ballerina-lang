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

package org.wso2.transport.http.netty.contractimpl.websocket.message;

import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.DefaultWebSocketMessage;


/**
 * Implementation of {@link WebSocketTextMessage}.
 */
public class DefaultWebSocketTextMessage extends DefaultWebSocketMessage implements WebSocketTextMessage {

    private final String text;
    private final boolean isFinalFragment;

    public DefaultWebSocketTextMessage(String text, boolean isFinalFragment) {
        this.text = text;
        this.isFinalFragment = isFinalFragment;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public boolean isFinalFragment() {
        return isFinalFragment;
    }
}
