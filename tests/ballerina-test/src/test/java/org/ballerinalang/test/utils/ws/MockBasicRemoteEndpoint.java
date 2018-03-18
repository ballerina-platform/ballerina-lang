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

package org.ballerinalang.test.utils.ws;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import javax.websocket.EncodeException;
import javax.websocket.RemoteEndpoint;

/**
 * Mock basic remote endpoint for WebSocket test cases.
 */
public class MockBasicRemoteEndpoint implements RemoteEndpoint.Basic {

    private String textReceived = null;
    private ByteBuffer bufferReceived = null;
    private boolean isPing;
    private boolean isPong;

    public String getTextReceived() {
        String tmp = textReceived;
        textReceived = null;
        return tmp;
    }

    public ByteBuffer getBufferReceived() {
        ByteBuffer tmp = bufferReceived;
        bufferReceived = null;
        return tmp;
    }

    public boolean isPing() {
        boolean tmp = isPing;
        isPing = false;
        return tmp;
    }

    public boolean isPong() {
        boolean tmp = isPong;
        isPong = false;
        return tmp;
    }

    @Override
    public void sendText(String text) throws IOException {
        textReceived = text;
    }

    @Override
    public void sendBinary(ByteBuffer data) throws IOException {
        bufferReceived = data;
    }

    @Override
    public void sendText(String partialMessage, boolean isLast) throws IOException {
    }

    @Override
    public void sendBinary(ByteBuffer partialByte, boolean isLast) throws IOException {

    }

    @Override
    public OutputStream getSendStream() throws IOException {
        return null;
    }

    @Override
    public Writer getSendWriter() throws IOException {
        return null;
    }

    @Override
    public void sendObject(Object data) throws IOException, EncodeException {

    }

    @Override
    public void setBatchingAllowed(boolean allowed) throws IOException {

    }

    @Override
    public boolean getBatchingAllowed() {
        return false;
    }

    @Override
    public void flushBatch() throws IOException {

    }

    @Override
    public void sendPing(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
        bufferReceived = applicationData;
        isPing = true;
    }

    @Override
    public void sendPong(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
        bufferReceived = applicationData;
        isPong = true;
    }
}
