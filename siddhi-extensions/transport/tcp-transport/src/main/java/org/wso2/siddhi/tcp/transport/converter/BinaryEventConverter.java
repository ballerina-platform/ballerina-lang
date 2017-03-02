/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.siddhi.tcp.transport.converter;


import io.netty.buffer.ByteBuf;
import org.wso2.siddhi.tcp.transport.dto.SiddhiEventComposite;
import org.wso2.siddhi.tcp.transport.utils.BinaryMessageConstants;
import org.wso2.siddhi.tcp.transport.utils.BinaryMessageConverterUtil;
import org.wso2.siddhi.core.event.Event;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static io.netty.buffer.Unpooled.buffer;


/**
 * This is a Util class which does the Binary message transformation for publish, login, logout operations.
 */
public class BinaryEventConverter {
    public static void sendBinaryLoginMessage(Socket socket, String userName, String password) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(13 + userName.length() + password.length());
        buf.put((byte) 0);
        buf.putInt(8 + userName.length() + password.length());
        buf.putInt(userName.length());
        buf.putInt(password.length());
        buf.put(userName.getBytes(BinaryMessageConstants.DEFAULT_CHARSET));
        buf.put(password.getBytes(BinaryMessageConstants.DEFAULT_CHARSET));

        OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
        outputStream.write(buf.array());
        outputStream.flush();
    }

    public static void sendBinaryLogoutMessage(Socket socket, String sessionId) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(9 + sessionId.length());
        buf.put((byte) 1);
        buf.putInt(4 + sessionId.length());
        buf.putInt(sessionId.length());
        buf.put(sessionId.getBytes(BinaryMessageConstants.DEFAULT_CHARSET));

        OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
        outputStream.write(buf.array());
        outputStream.flush();
    }

    public static void convertToBinaryMessage(List<SiddhiEventComposite> events, String sessionId, ByteBuf messageBuffer) throws IOException {
        int messageSize = 8 + sessionId.length();
        List<ByteBuf> eventBuffers = new ArrayList<ByteBuf>();

        for (SiddhiEventComposite event : events) {

            int eventSize = getEventSize(event);
            messageSize += eventSize + 4;
            Event siddhiEvent = event.getSiddhiEvent();
            ByteBuf eventDataBuffer = buffer(4 + eventSize);
            //ByteBuffer eventDataBuffer = ByteBuffer.allocate(4 + eventSize);
            eventDataBuffer.writeInt(eventSize);

            eventDataBuffer.writeLong(siddhiEvent.getTimestamp());
            eventDataBuffer.writeInt(event.getStreamID().length());
            eventDataBuffer.writeBytes(event.getStreamID().getBytes(BinaryMessageConstants.DEFAULT_CHARSET));

            if (siddhiEvent.getData() != null && siddhiEvent.getData().length != 0) {
                for (Object aData : siddhiEvent.getData()) {
                    BinaryMessageConverterUtil.assignData(aData, eventDataBuffer);
                }
            }
/*            if (event.getArbitraryDataMap() != null && event.getArbitraryDataMap().size() != 0) {
                for (Map.Entry<String, String> aArbitraryData : event.getArbitraryDataMap().entrySet()) {
                    assignData(aArbitraryData.getKey(), eventDataBuffer);
                    assignData(aArbitraryData.getValue(), eventDataBuffer);
                }
            }*/
            eventBuffers.add(eventDataBuffer);
        }

        messageBuffer.writeByte((byte) 2);  //1
        messageBuffer.writeInt(messageSize); //4
        messageBuffer.writeInt(sessionId.length()); //4
        messageBuffer.writeBytes(sessionId.getBytes(BinaryMessageConstants.DEFAULT_CHARSET));
        messageBuffer.writeInt(events.size()); //4

        for (ByteBuf byteBuf : eventBuffers) {
            messageBuffer.writeBytes(byteBuf);
        }
    }

    private static int getEventSize(SiddhiEventComposite event) {
        int eventSize = 4 + event.getStreamID().length() + 8;
        Object[] data = event.getSiddhiEvent().getData();
        if (data != null) {
            for (Object aData : data) {
                eventSize += BinaryMessageConverterUtil.getSize(aData);
            }
        }
        /*if (event.getArbitraryDataMap() != null && event.getArbitraryDataMap().size() != 0) {
            for (Map.Entry<String, String> aArbitraryData : event.getArbitraryDataMap().entrySet()) {
                eventSize += 8 + aArbitraryData.getKey().length() + aArbitraryData.getValue().length();
            }
        }*/
        return eventSize;
    }

    public static String processResponse(Socket socket) throws Exception {

        InputStream inputStream = socket.getInputStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        int messageType = bufferedInputStream.read();
        ByteBuffer bbuf;
        switch (messageType) {
            case 0:
                //OK message
                break;
            case 1:
                //Error Message
                bbuf = ByteBuffer.wrap(BinaryMessageConverterUtil.loadData(bufferedInputStream, new byte[8]));
                int errorClassNameLength = bbuf.getInt();
                int errorMsgLength = bbuf.getInt();

                String className = new String(ByteBuffer.wrap(BinaryMessageConverterUtil.loadData(bufferedInputStream, new byte[errorClassNameLength])).array());
                String errorMsg = new String(ByteBuffer.wrap(BinaryMessageConverterUtil.loadData(bufferedInputStream, new byte[errorMsgLength])).array());
                throw new Exception(errorMsg);
            case 2:
                //Logging OK response
                bbuf = ByteBuffer.wrap(BinaryMessageConverterUtil.loadData(bufferedInputStream, new byte[4]));
                int sessionIdLength = bbuf.getInt();
                String sessionId = new String(ByteBuffer.wrap(BinaryMessageConverterUtil.loadData(bufferedInputStream, new byte[sessionIdLength])).array());
                return sessionId;
        }
        return null;
    }

}
