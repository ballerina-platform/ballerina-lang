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
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.tcp.transport.exception.MalformedEventException;
import org.wso2.siddhi.tcp.transport.utils.BinaryMessageConverterUtil;
import org.wso2.siddhi.tcp.transport.utils.StreamInfo;
import org.wso2.siddhi.tcp.transport.utils.StreamTypeHolder;

import java.nio.ByteBuffer;

/**
 * This class is a implementation EventConverter to create the event from the Binary message.
 * This is used within data bridge to create the event from the row message received.
 */
public class SiddhiEventConverter {
    static final Logger log = Logger.getLogger(SiddhiEventConverter.class);

    public static void toConvertToSiddhiEvents(Object eventBundle, StreamTypeHolder streamTypeHolder) {

        ByteBuf byteBuffer = (ByteBuf) eventBundle;

        int protocol = byteBuffer.readByte();
        int messageSize = byteBuffer.readInt();
        if (protocol != 2 || messageSize > byteBuffer.readableBytes()) {
            byteBuffer.resetReaderIndex();
            return;
        }

        int sessionIdSize = byteBuffer.readInt();
        String sessionId = BinaryMessageConverterUtil.getString(byteBuffer, sessionIdSize);

        int streamIdSize = byteBuffer.readInt();
        String streamId = BinaryMessageConverterUtil.getString(byteBuffer, streamIdSize);

        StreamInfo streamInfo = streamTypeHolder.getStreamInfo(streamId);
        int numberOfEvents = byteBuffer.readInt();

        if (streamInfo == null) {
            for (int i = 0; i < numberOfEvents; i++) {
                int eventSize = byteBuffer.readInt();
                byte[] bytes = new byte[eventSize];
                byteBuffer.readBytes(bytes);
            }
            byteBuffer.markReaderIndex();
            log.error("Events with unknown streamId : '" + streamId + "' hence dropping the events!");
            return;
        }

        Event[] events = new Event[numberOfEvents];
        for (int i = 0; i < numberOfEvents; i++) {
            int eventSize = byteBuffer.readInt();
            //System.out.println("Event Size:" + eventSize);
            byte[] bytes = new byte[eventSize];
            byteBuffer.readBytes(bytes);
            ByteBuffer eventByteBuffer = ByteBuffer.wrap(bytes);
            events[i] = getEvent(eventByteBuffer, streamInfo.getAttributeTypes());
        }
        byteBuffer.markReaderIndex();
        streamInfo.getStreamListener().onEvents(events);
    }

    public static Event getEvent(ByteBuffer byteBuffer, Attribute.Type[] attributeTypes) throws MalformedEventException {
        Event event = new Event();
        long timeStamp = byteBuffer.getLong();
        event.setTimestamp(timeStamp);
        event.setData(toObjectArray(byteBuffer, attributeTypes));
        return event;
    }

    public static Object[] toObjectArray(ByteBuffer byteBuffer,
                                         Attribute.Type[] attributeTypeOrder) {
        if (attributeTypeOrder != null) {
            if (byteBuffer == null) {
                throw new MalformedEventException("Received byte stream in null. Hence cannot convert to event");
            }
            Object[] objects = new Object[attributeTypeOrder.length];
            for (int i = 0; i < attributeTypeOrder.length; i++) {
                switch (attributeTypeOrder[i]) {
                    case INT:
                        objects[i] = byteBuffer.getInt();
                        break;
                    case LONG:
                        objects[i] = byteBuffer.getLong();
                        break;
                    case STRING:
                        int stringSize = byteBuffer.getInt();
                        //System.out.println("String Size:" + stringSize);
                        if (stringSize == 0) {
                            objects[i] = null;
                        } else {
                            objects[i] = BinaryMessageConverterUtil.getString(byteBuffer, stringSize);
                        }
                        break;
                    case DOUBLE:
                        objects[i] = byteBuffer.getDouble();
                        break;
                    case FLOAT:
                        objects[i] = byteBuffer.getFloat();
                        break;
                    case BOOL:
                        objects[i] = byteBuffer.get() == 1;
                        break;
                }
            }
            return objects;
        } else {
            return null;
        }
    }

//    public Map<String, String> toStringMap(ByteBuffer byteBuffer) {
//        if (byteBuffer != null) {
//            Map<String, String> eventProps = new HashMap<String, String>();
//
//            while (byteBuffer.remaining() > 0) {
//                int keySize = byteBuffer.getInt();
//                String key = BinaryMessageConverterUtil.getString(byteBuffer, keySize);
//                int valueSize = byteBuffer.getInt();
//                String value = BinaryMessageConverterUtil.getString(byteBuffer, valueSize);
//                eventProps.put(key, value);
//            }
//            return eventProps;
//        }
//        return null;
//    }

}
