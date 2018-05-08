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

package org.ballerinalang.model.values;

import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Consumer;
import io.ballerina.messaging.broker.core.Message;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.ballerinalang.broker.BrokerUtils;
import org.ballerinalang.model.types.BAnyType;
import org.ballerinalang.model.types.BIndexedType;
import org.ballerinalang.model.types.BStreamType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import java.util.UUID;

/**
 * The {@code BStream} represents a stream in Ballerina.
 *
 * @since 0.965.0
 */
public class BStream implements BRefType<Object> {

    private static final String TOPIC_NAME_PREFIX = "TOPIC_NAME_";

    private BType constraintType;

    private String streamId = "";

    /**
     * The name of the underlying broker topic representing the stream object.
     */
    private String topicName;

    public BStream(BType type, String name) {
        if (((BStreamType) type).getConstrainedType() == null) {
            throw new BallerinaException("a stream cannot be declared without a constraint");
        }
        this.constraintType = ((BStreamType) type).getConstrainedType();
        if (constraintType.getName() != null) {
            this.topicName = TOPIC_NAME_PREFIX + constraintType.getName().toUpperCase() + "_" + name;
        } else if (constraintType instanceof BIndexedType) {
            this.topicName = TOPIC_NAME_PREFIX + ((BIndexedType) constraintType).getElementType() + "_" + name;
        } else {
            this.topicName = TOPIC_NAME_PREFIX + name; //TODO: check for improvement
        }
        topicName = topicName.concat("_").concat(UUID.randomUUID().toString());
        this.streamId = name;
    }

    public String getStreamId() {
        return streamId;
    }

    @Override
    public String stringValue() {
        return "";
    }

    @Override
    public BType getType() {
        return BTypes.typeStream;
    }

    @Override
    public BValue copy() {
        return null;
    }

    @Override
    public Object value() {
        return null;
    }

    public BType getConstraintType() {
        return constraintType;
    }

    /**
     * Method to publish to a topic representing the stream in the broker.
     *
     * @param data the data to publish to the stream
     */
    public void publish(BValue data) {
        //TODO: refactor and move checks to compile time
        BType dataType = data.getType();
        if (!dataType.equals(this.constraintType) && !(constraintType instanceof BUnionType
                                        && ((BUnionType) constraintType).getMemberTypes().contains(dataType))
                                        && !(constraintType instanceof BAnyType)) {
            throw new BallerinaException("incompatible types: value of type:" + dataType.getName()
                    + " cannot be added to a stream of type:" + this.constraintType.getName());
        }
        BrokerUtils.publish(topicName, new BallerinaStreamByteBuf(data));
    }

    /**
     * Method to register a subscription to the underlying topic representing the stream in the broker.
     *
     * @param functionPointer represents the function pointer reference for the function to be invoked on receiving
     *                        messages
     */
    public void subscribe(BFunctionPointer functionPointer) {
        BType[] parameters = functionPointer.funcRefCPEntry.getFunctionInfo().getParamTypes();
        if (parameters[0].getTag() != constraintType.getTag() || (constraintType instanceof BStructType
                                          && ((BStructType) parameters[0]).structInfo.getType() != constraintType)) {
            throw new BallerinaException("incompatible function: subscription function needs to be a function accepting"
                                                 + ":" + this.constraintType.getName());
        }
        String queueName = String.valueOf(System.currentTimeMillis()) + UUID.randomUUID().toString();
        BrokerUtils.addSubscription(topicName, new StreamSubscriber(queueName, functionPointer));
    }

    public void subscribe(InputHandler inputHandler) {
        if (constraintType.getTag() != TypeTags.STRUCT_TAG) {
            throw new BallerinaException("Streaming Support is only available with streams accepting objects");
        }
        String queueName = String.valueOf(UUID.randomUUID());
        BrokerUtils.addSubscription(topicName, new InternalStreamSubscriber(topicName, queueName, inputHandler));
    }

    private class StreamSubscriber extends Consumer {
        final String queueName;
        final BFunctionPointer functionPointer;

        StreamSubscriber(String queueName, BFunctionPointer functionPointer) {
            this.queueName = queueName;
            this.functionPointer = functionPointer;
        }

        @Override
        protected void send(Message message) throws BrokerException {
            BValue data =
                    ((BallerinaStreamByteBuf) (message.getContentChunks().get(0).getByteBuf()).unwrap()).streamEvent;
            BLangFunctions.invokeCallable(functionPointer.value().getFunctionInfo(), new BValue[] { data });
        }

        @Override
        public String getQueueName() {
            return queueName;
        }

        @Override
        protected void close() throws BrokerException {

        }

        @Override
        public boolean isExclusive() {
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }
    }

    //Class which handles the subscription internally
    private class InternalStreamSubscriber extends Consumer {
        private final String topic;
        private final String queueName;
        private final InputHandler inputHandler;

        InternalStreamSubscriber(String topic, String queueName, InputHandler inputHandler) {
            this.topic = topic;
            this.queueName = queueName;
            this.inputHandler = inputHandler;
        }

        @Override
        protected void send(Message message) throws BrokerException {
            BValue data =
                    ((BallerinaStreamByteBuf) (message.getContentChunks().get(0).getByteBuf()).unwrap()).streamEvent;
            Object[] event = createEvent((BStruct) data);
            try {
                inputHandler.send(event);
            } catch (InterruptedException e) {
                throw new BallerinaException("Error while sending events to stream: " + topic + ": " + e.getMessage()
                        , e);
            }
        }

        private Object[] createEvent(BStruct data) {
            BStructType streamType = data.getType();
            int intValueIndex = -1;
            int floatValueIndex = -1;
            int stringValueIndex = -1;
            int boolValueIndex = -1;
            Object[] event = new Object[streamType.getStructFields().length];
            for (int index = 0; index < streamType.getStructFields().length; index++) {
                BStructType.StructField field = streamType.getStructFields()[index];
                switch (field.getFieldType().getTag()) {
                    case TypeTags.INT_TAG:
                        event[index] = data.getIntField(++intValueIndex);
                        break;
                    case TypeTags.FLOAT_TAG:
                        event[index] = data.getFloatField(++floatValueIndex);
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        event[index] = data.getBooleanField(++boolValueIndex);
                        break;
                    case TypeTags.STRING_TAG:
                        event[index] = data.getStringField(++stringValueIndex);
                        break;
                    default:
                        throw new BallerinaException("Fields in streams do not support data types other than int, " +
                                "float, boolean and string");
                }
            }
            return event;
        }

        @Override
        public String getQueueName() {
            return queueName;
        }

        @Override
        protected void close() throws BrokerException {

        }

        @Override
        public boolean isExclusive() {
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }
    }

    /**
     * Implementation of {@link io.netty.buffer.ByteBuf} for Ballerina Streams, to hold a {@link BValue}
     */
    private class BallerinaStreamByteBuf extends UnpooledHeapByteBuf {

        private final BValue streamEvent;

        BallerinaStreamByteBuf(BValue streamEvent) {
            super(UnpooledByteBufAllocator.DEFAULT, 0, 0);
            this.streamEvent = streamEvent;
        }
    }
}
