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
import org.ballerinalang.bre.bvm.BVM;
import org.ballerinalang.bre.bvm.BVMExecutor;
import org.ballerinalang.broker.BallerinaBroker;
import org.ballerinalang.broker.BallerinaBrokerByteBuf;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BIndexedType;
import org.ballerinalang.model.types.BStreamType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * The {@code BStream} represents a stream in Ballerina.
 *
 * @since 0.965.0
 */
public class BStream implements BRefType<Object> {

    private static final String TOPIC_NAME_PREFIX = "TOPIC_NAME_";

    private BType type;
    private BType constraintType;

    private String streamId = "";

    private BallerinaBroker brokerInstance;

    /**
     * The name of the underlying broker topic representing the stream object.
     */
    private String topicName;

    public BStream(BType type, String name) {
        if (((BStreamType) type).getConstrainedType() == null) {
            throw new BallerinaException("a stream cannot be declared without a constraint");
        }
        try {
            this.brokerInstance = BallerinaBroker.getBrokerInstance();
        } catch (Exception e) {
            throw new BallerinaException("Error starting up internal broker for streams");
        }
        this.constraintType = ((BStreamType) type).getConstrainedType();
        this.type = new BStreamType(constraintType);
        if (constraintType instanceof BIndexedType) {
            this.topicName = TOPIC_NAME_PREFIX + ((BIndexedType) constraintType).getElementType() + "_" + name;
        } else if (constraintType != null) {
            this.topicName = TOPIC_NAME_PREFIX + constraintType + "_" + name;
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
        return this.type;
    }

    @Override
    public void stamp(BType type, List<BVM.TypeValuePair> unresolvedValues) {

    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
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
        BType dataType = data.getType();
        if (!BVM.checkCast(data, constraintType)) {
            throw new BallerinaException("incompatible types: value of type:" + dataType
                    + " cannot be added to a stream of type:" + this.constraintType);
        }
        brokerInstance.publish(topicName, new BallerinaBrokerByteBuf(data));
    }

    /**
     * Method to register a subscription to the underlying topic representing the stream in the broker.
     *
     * @param functionPointer represents the function pointer reference for the function to be invoked on receiving
     *                        messages
     */
    public void subscribe(BFunctionPointer functionPointer) {
        BType[] parameters = functionPointer.value().getParamTypes();
        int lastArrayIndex = parameters.length - 1;
        if (!BVM.isAssignable(constraintType, parameters[lastArrayIndex], new ArrayList<>())) {
            throw new BallerinaException("incompatible function: subscription function needs to be a function"
                                                 + " accepting:" + this.constraintType);
        }
        String queueName = String.valueOf(System.currentTimeMillis()) + UUID.randomUUID().toString();
        brokerInstance.addSubscription(topicName, new StreamSubscriber(queueName, functionPointer));
    }

    public void subscribe(InputHandler inputHandler) {
        if (constraintType.getTag() != TypeTags.OBJECT_TYPE_TAG
                && constraintType.getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw new BallerinaException("Streaming Support is only available with streams accepting objects");
        }
        String queueName = String.valueOf(UUID.randomUUID());
        brokerInstance.addSubscription(topicName, new InternalStreamSubscriber(topicName, queueName, inputHandler));
    }

    private class StreamSubscriber extends Consumer {
        final String queueName;
        final BFunctionPointer functionPointer;
        List<BValue> closureArgs = new ArrayList<>();

        StreamSubscriber(String queueName, BFunctionPointer functionPointer) {
            this.queueName = queueName;
            this.functionPointer = functionPointer;
            for (BClosure closure : functionPointer.getClosureVars()) {
                closureArgs.add(closure.value());
            }
        }

        @Override
        protected void send(Message message) {
            try {
                BValue data =
                        ((BallerinaBrokerByteBuf) (message.getContentChunks().get(0).getByteBuf()).unwrap()).getValue();
                List<BValue> argsList = new ArrayList<>(closureArgs);
                argsList.add(data);
                BVMExecutor.executeFunction(functionPointer.value().getPackageInfo().getProgramFile(),
                        functionPointer.value(), argsList.toArray(new BValue[0]));
            } catch (Exception e) {
                throw new BallerinaException("Error delivering event to subscriber: ", e);
            }
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

        @Override
        public Properties getTransportProperties() {
            return new Properties();
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
                    ((BallerinaBrokerByteBuf) (message.getContentChunks().get(0).getByteBuf()).unwrap()).getValue();
            Object[] event = createEvent((BMap) data);
            try {
                inputHandler.send(event);
            } catch (InterruptedException e) {
                throw new BallerinaException("Error while sending events to stream: " + topic + ": " + e.getMessage()
                        , e);
            }
        }

        private Object[] createEvent(BMap<String, BValue> data) {
            BStructureType streamType = (BStructureType) data.getType();
            Object[] event = new Object[streamType.getFields().size()];
            int index = 0;
            for (Map.Entry<String, BField> fieldEntry : streamType.getFields().entrySet()) {
                BField field = fieldEntry.getValue();
                switch (field.getFieldType().getTag()) {
                    case TypeTags.INT_TAG:
                        event[index++] = ((BInteger) data.get(field.fieldName)).intValue();
                        break;
                    case TypeTags.FLOAT_TAG:
                        event[index++] = ((BFloat) data.get(field.fieldName)).floatValue();
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        event[index++] = ((BBoolean) data.get(field.fieldName)).booleanValue();
                        break;
                    case TypeTags.STRING_TAG:
                        event[index++] = data.get(field.fieldName).stringValue();
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

        @Override
        public Properties getTransportProperties() {
            return new Properties();
        }
    }
}
