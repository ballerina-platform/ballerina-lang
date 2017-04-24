/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.tcp.transport.converter;


import io.netty.buffer.ByteBuf;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.tcp.transport.utils.BinaryMessageConverterUtil;
import org.wso2.siddhi.tcp.transport.utils.Constant;
import org.wso2.siddhi.tcp.transport.utils.EventComposite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.netty.buffer.Unpooled.buffer;


/**
 * This is a Util class which does the Binary message transformation for publish, login, logout operations.
 */
public class BinaryEventConverter {

    public static void convertToBinaryMessage(EventComposite eventComposite, ByteBuf messageBuffer) throws IOException {
        String sessionId = eventComposite.getSessionId();
        String streamId = eventComposite.getStreamId();
        int eventCount = eventComposite.getEvents().length;

        int messageSize = 4 + sessionId.length() + 4 + streamId.length() + 4;

        List<ByteBuf> eventBuffers = new ArrayList<ByteBuf>();
        for (Event event : eventComposite.getEvents()) {
            int eventSize = getEventSize(event);
            messageSize += eventSize + 4;
            ByteBuf eventDataBuffer = buffer(4 + eventSize);

            //ByteBuffer eventDataBuffer = ByteBuffer.allocate(4 + eventSize);
            eventDataBuffer.writeInt(eventSize);
            eventDataBuffer.writeLong(event.getTimestamp());
            if (event.getData() != null && event.getData().length != 0) {
                for (Object aData : event.getData()) {
                    BinaryMessageConverterUtil.assignData(aData, eventDataBuffer);
                }
            }
            eventBuffers.add(eventDataBuffer);
        }
            /*
            if (event.getArbitraryDataMap() != null && event.getArbitraryDataMap().size() != 0) {
                for (Map.Entry<String, String> aArbitraryData : event.getArbitraryDataMap().entrySet()) {
                    assignData(aArbitraryData.getKey(), eventDataBuffer);
                    assignData(aArbitraryData.getValue(), eventDataBuffer);
                }
            }
            */
        messageBuffer.writeByte((byte) 2);  //1
        messageBuffer.writeInt(messageSize); //4
        messageBuffer.writeInt(sessionId.length()); //4
        messageBuffer.writeBytes(sessionId.getBytes(Constant.DEFAULT_CHARSET));
        messageBuffer.writeInt(streamId.length()); //4
        messageBuffer.writeBytes(streamId.getBytes(Constant.DEFAULT_CHARSET));
        messageBuffer.writeInt(eventCount); //4
        for (ByteBuf byteBuf : eventBuffers) {
            messageBuffer.writeBytes(byteBuf);
        }
    }

    private static int getEventSize(Event event) {
        int eventSize = 8;
        Object[] data = event.getData();
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
}
