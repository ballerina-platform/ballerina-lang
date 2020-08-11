/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.websocket;

import org.junit.Assert;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.message.DefaultWebSocketBinaryMessage;

import java.nio.ByteBuffer;

/**
 * Unit test for WebSocket.
 */
public class WebSocketUnitTestCase {

    @Test(description = "Direct byte buffer in WebSocketBinaryMessage.")
    public void testDirectByteBufferWithWithBinaryMessage() {
        byte[] textByteArray = "Hello World!".getBytes();
        ByteBuffer buffer = ByteBuffer.allocateDirect(textByteArray.length);
        buffer.put(textByteArray);
        buffer.flip();
        WebSocketBinaryMessage binaryMessage = new DefaultWebSocketBinaryMessage(buffer, true);
        Assert.assertArrayEquals(textByteArray, binaryMessage.getByteArray());
    }

}
