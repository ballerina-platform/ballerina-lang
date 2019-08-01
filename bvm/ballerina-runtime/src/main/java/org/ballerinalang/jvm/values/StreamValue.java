/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.streams.StreamSubscriptionManager;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.types.BType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The {@link StreamValue} represents a stream in Ballerina.
 *
 * @since 0.995.0
 */
public class StreamValue implements RefValue {

    private static final String TOPIC_NAME_PREFIX = "TOPIC_NAME_";

    private BType type;
    private BType constraintType;

    private String streamId;

    private StreamSubscriptionManager streamSubscriptionManager;

    /**
     * The name of the underlying broker topic representing the stream object.
     */
    public String topicName;

    public StreamValue(BType type, String name) {
        this.streamSubscriptionManager = StreamSubscriptionManager.getInstance();
        this.constraintType = ((BStreamType) type).getConstrainedType();
        this.type = new BStreamType(constraintType);

        /*if (constraintType instanceof BIndexedType) {
            this.topicName = TOPIC_NAME_PREFIX + ((BIndexedType) constraintType).getElementType() + "_" + name;
        } */

        if (constraintType != null) {
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

    public String stringValue() {
        return "stream " + streamId + " " + getType().toString();
    }

    @Override
    public BType getType() {
        return this.type;
    }

    @Override
    public void stamp(BType type, List<TypeValuePair> unresolvedValues) {

    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public Object frozenCopy(Map<Object, Object> refs) {
            throw new UnsupportedOperationException();
    }

    public BType getConstraintType() {
        return constraintType;
    }

    /**
     * Method to publish to a topic representing the stream in the broker.
     *
     * @param strand the strand in which the data being published
     * @param data the data to publish to the stream
     */
    public void publish(Strand strand, Object data) {
        streamSubscriptionManager.sendMessage(this, strand, data);
    }

    /**
     * Method to register a subscription to the underlying topic representing the stream in the broker.
     *
     * @param functionPointer represents the function pointer reference for the function to be invoked on receiving
     *                        messages
     */
    public void subscribe(FPValue<Object[], Object> functionPointer) {
        streamSubscriptionManager.registerMessageProcessor(this, functionPointer);
    }

//    public void subscribe(InputHandler inputHandler) {
//        if (constraintType.getTag() != TypeTags.OBJECT_TYPE_TAG
//            && constraintType.getTag() != TypeTags.RECORD_TYPE_TAG) {
//            throw BallerinaErrors.createError("Streaming Support is only available with streams accepting objects");
//        }
//        streamSubscriptionManager.registerMessageProcessor(this, inputHandler);
//    }
}
