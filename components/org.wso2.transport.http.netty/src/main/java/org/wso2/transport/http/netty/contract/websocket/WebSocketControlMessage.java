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

package org.wso2.transport.http.netty.contract.websocket;

import java.nio.ByteBuffer;

/**
 * This message contains the details of WebSocket bong message.
 */
public interface WebSocketControlMessage extends WebSocketMessage {

    /**
     * Get the control signal.
     *
     * @return the control signal as a {@link WebSocketControlSignal}.
     */
    WebSocketControlSignal getControlSignal();

    /**
     * Get the payload of the control signal.
     *
     * @return the payload of the control signal.
     */
    ByteBuffer getPayload();

    /**
     * Get the binary data as a byte array.
     *
     * @return the binary data as a byte array.
     */
    byte[] getByteArray();
}
