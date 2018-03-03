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
import io.ballerina.messaging.broker.core.ContentChunk;
import io.ballerina.messaging.broker.core.Message;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.WorkerContext;
import org.ballerinalang.model.types.BStreamType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.util.BrokerUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * The {@code BStream} represents a stream in Ballerina.
 *
 * @since 0.964.0
 */
public class BStream implements BRefType<Object> {

    private BStructType constraintType;

    private String topicName;

    public BStream(BType type) {
        if (((BStreamType) type).getConstrainedType() == null) {
            throw  new BallerinaException("A stream cannot be created without a constraint");
        }
        this.constraintType = (BStructType) ((BStreamType) type).getConstrainedType();
        //temporarily until topic name is set to identifier name
        this.topicName = "TOPIC_NAME_" + UUID.randomUUID();
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

    /**
     * Method to publish to a topic representing the stream in the broker.
     *
     * @param data  the data to publish to the stream
     */
    public void publish(BStruct data) {
        if (data.getType() != this.constraintType) {
            throw new BallerinaException("Incompatible Types: struct of type:" + data.getType().getName()
                                             + " cannot be added to a stream of type:" + this.constraintType.getName());
        }
        BrokerUtils.publish(topicName, JSONUtils.convertStructToJSON(data).stringValue().getBytes());
    }

    /**
     * Method to register a subscription to the underlying topic representing the stream in the broker.
     *
     * @param context           the context object representing runtime state
     * @param functionPointer   represents the funtion pointer reference for the function to be invoked on receiving
     *                          messages
     */
    public void subscribe(Context context, BFunctionPointer functionPointer) {
        String queueName = String.valueOf(System.currentTimeMillis());
        BrokerUtils.addSubscription(topicName, new StreamSubscriber(queueName, context, functionPointer));
    }

    private class StreamSubscriber extends Consumer {

        final String queueName;
        final Context context;
        final BFunctionPointer functionPointer;

        StreamSubscriber(String queueName, Context context, BFunctionPointer functionPointer) {
            this.queueName = queueName;
            this.context = context;
            this.functionPointer = functionPointer;
        }

        @Override
        protected void send(Message message) throws BrokerException {
            byte[] bytes = new byte[0];
            for (ContentChunk chunk:message.getContentChunks()) {
                bytes = new byte[chunk.getBytes().readableBytes()];
                chunk.getBytes().getBytes(0, bytes);
            }
            BJSON json = new BJSON(new String(bytes, StandardCharsets.UTF_8));
//            BStruct data = JSONUtils.convertJSONToStruct(json, constraintType); TODO: replace with rebase
            BStruct data = JSONUtils.convertJSONToStruct(json, constraintType,
                                             context.getProgramFile().getEntryPackage());
            BValue[] args = {data};
            Context workerContext = new WorkerContext(context.getProgramFile(), context);
            BLangFunctions.invokeFunction(context.getProgramFile(), functionPointer.value().getFunctionInfo(), args,
                                          workerContext);
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

}
