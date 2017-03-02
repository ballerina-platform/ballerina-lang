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
import org.wso2.siddhi.tcp.transport.dto.StreamTypeHolder;
import org.wso2.siddhi.tcp.transport.exception.MalformedEventException;
import org.wso2.siddhi.tcp.transport.utils.BinaryMessageConverterUtil;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a implementation EventConverter to create the event from the Binary message.
 * This is used within data bridge to create the event from the row message received.
 */
public class SiddhiEventConverter implements EventConverter {
    private static SiddhiEventConverter instance = new SiddhiEventConverter();

    private SiddhiEventConverter() {
    }

    @Override
    public List<SiddhiEventComposite> toEventList(Object eventBundle, StreamTypeHolder streamTypeHolder) {

        ByteBuf byteBuffer = (ByteBuf) eventBundle;
        int sessionIdSize = byteBuffer.readInt();
        byteBuffer.readBytes(new byte[sessionIdSize]);
        int events = byteBuffer.readInt();

        List<SiddhiEventComposite> eventList = new ArrayList<SiddhiEventComposite>();
        for (int i = 0; i < events; i++) {
            int eventSize = byteBuffer.readInt();
            byte[] bytes= new byte[eventSize];
            byteBuffer.readBytes(bytes);
            ByteBuffer eventByteBuffer = ByteBuffer.wrap(bytes);
            eventList.add(getEvent(eventByteBuffer, streamTypeHolder));
        }
        byteBuffer.markReaderIndex();
        return eventList;
    }

    @Override
    public int getSize(Object eventBundle) {
        return ((byte[])eventBundle).length;
    }

    @Override
    public int getNumberOfEvents(Object eventBundle) {
        ByteBuffer byteBuffer = ByteBuffer.wrap((byte[]) eventBundle);
        int sessionIdSize = byteBuffer.getInt();
        byteBuffer.get(new byte[sessionIdSize]);
        return byteBuffer.getInt();
    }

    public SiddhiEventComposite getEvent(ByteBuffer byteBuffer, StreamTypeHolder streamTypeHolder) throws MalformedEventException {
        long timeStamp = byteBuffer.getLong();
        int streamIdSize = byteBuffer.getInt();
        String streamId = BinaryMessageConverterUtil.getString(byteBuffer, streamIdSize);

        Event event = new Event();
        event.setTimestamp(timeStamp);

        Attribute.Type[] attributeTypeOrder = streamTypeHolder.getDataType(streamId);
        /*if (attributeTypeOrder == null) {
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            if (privilegedCarbonContext.getTenantDomain() == null) {
                privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            }
            streamTypeHolder.reloadStreamTypeHolder();
            attributeTypeOrder = streamTypeHolder.getDataType(event.getStreamId());
            if (attributeTypeOrder == null) {
                throw new EventConversionException("No StreamDefinition for streamId " + event.getStreamId()
                        + " present in cache ");
            }
        }*/
        event.setData(toObjectArray(byteBuffer, attributeTypeOrder));
        return new SiddhiEventComposite(event, streamId);
    }

    public Object[] toObjectArray(ByteBuffer byteBuffer,
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

    public Object[] toObjectArray(ByteBuf byteBuffer,
                                  Attribute.Type[] attributeTypeOrder) {
        if (attributeTypeOrder != null) {
            if (byteBuffer == null) {
                throw new MalformedEventException("Received byte stream in null. Hence cannot convert to event");
            }
            Object[] objects = new Object[attributeTypeOrder.length];
            for (int i = 0; i < attributeTypeOrder.length; i++) {
                switch (attributeTypeOrder[i]) {
                    case INT:
                        objects[i] = byteBuffer.readInt();
                        break;
                    case LONG:
                        objects[i] = byteBuffer.readLong();
                        break;
                    case STRING:
                        int stringSize = byteBuffer.readInt();
                        if (stringSize == 0) {
                            objects[i] = null;
                        } else {
                            objects[i] = BinaryMessageConverterUtil.getString(byteBuffer, stringSize);
                        }
                        break;
                    case DOUBLE:
                        objects[i] = byteBuffer.readDouble();
                        break;
                    case FLOAT:
                        objects[i] = byteBuffer.readFloat();
                        break;
                    case BOOL:
                        objects[i] = byteBuffer.readByte() == 1;
                        break;
                }
            }
            return objects;
        } else {
            return null;
        }
    }

    public Map<String, String> toStringMap(ByteBuffer byteBuffer) {
        if (byteBuffer != null) {
            Map<String, String> eventProps = new HashMap<String, String>();

            while (byteBuffer.remaining() > 0) {
                int keySize = byteBuffer.getInt();
                String key = BinaryMessageConverterUtil.getString(byteBuffer, keySize);
                int valueSize = byteBuffer.getInt();
                String value = BinaryMessageConverterUtil.getString(byteBuffer,valueSize);
                eventProps.put(key, value);
            }
            return eventProps;
        }
        return null;
    }

    public static SiddhiEventConverter getConverter() {
        return instance;
    }

}
