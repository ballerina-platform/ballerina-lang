/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.rabbitmq.nativeimpl.channel.listener;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConnectorException;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Starting the channel listener.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "start",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.CHANNEL_LISTENER_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ)
)
public class Start extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        BMap<String, BValue> channelListObject = (BMap<String, BValue>) context.getRefArgument(0);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        BMap<String, BValue> channelObj =
                (BMap<String, BValue>) channelListObject.get(RabbitMQConstants.CHANNEL_REFERENCE);
        Channel channel = (Channel) channelObj.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        ArrayList<Service> services =
                (ArrayList<Service>) channelListObject.getNativeData(RabbitMQConstants.CONSUMER_SERVICES);
        for (Service service : services) {
            List<Annotation> annotationList = service.getAnnotationList(RabbitMQConstants.PACKAGE_RABBITMQ,
                    RabbitMQConstants.SERVICE_CONFIG);
            Annotation annotation = annotationList.get(0);
            Struct value = annotation.getValue();
            Map<String, Value> queueConfig = value.getMapField(RabbitMQConstants.QUEUE_CONFIG);
            String queueName = queueConfig.get(RabbitMQConstants.ALIAS_QUEUE_NAME).getStringValue();
            Resource onMessageResource = service.getResources()[0];
            String ackMode = value.getStringField(RabbitMQConstants.ACK_MODE);
            boolean autoAck;
            switch (ackMode) {
                case RabbitMQConstants.AUTO_ACKMODE:
                    autoAck = true;
                    break;
                case RabbitMQConstants.CLIENT_ACKMODE:
                    autoAck = false;
                    break;
                default:
                    throw new BallerinaException("Unsupported acknowledgement mode");
            }
            boolean isQosSet = channelObj.getNativeData(RabbitMQConstants.QOS_STATUS) != null;
            if (!isQosSet) {
                try {
                    handleBasicQos(channel, value);
                } catch (RabbitMQConnectorException exception) {
                    RabbitMQUtils.returnError("Error occurred while setting the QoS settings", context,
                            exception);
                }
            }
            receiveMessages(onMessageResource, channel, queueName, autoAck);
        }
    }

    /**
     * Dispatch messages in String format.
     *
     * @param resource Ballerina resource function.
     * @param message  Message content to be dispatched to the resource function.
     */
    private void dispatchMessage(Resource resource, byte[] message, Channel channel, long deliveryTag,
                                 boolean autoAck) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executor.submit(resource, new RabbitMQResourceCallback(countDownLatch), null, null,
                getMessageBMap(resource, message, channel, deliveryTag, autoAck));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Receive messages from the RabbitMQ server.
     *
     * @param resource  Ballerina resource function.
     * @param channel   RabbitMQ Channel object.
     * @param queueName Name of the queue messages are consumed from.
     * @param autoAck   True if the server should consider messages acknowledged once delivered;
     *                  false if the server should expect explicit acknowledgements.
     */
    private void receiveMessages(Resource resource, Channel channel, String queueName, boolean autoAck) {
        try {
            channel.basicConsume(queueName, autoAck,
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag,
                                                   Envelope envelope,
                                                   AMQP.BasicProperties properties,
                                                   byte[] body) throws IOException {
                            dispatchMessage(resource, body, channel, envelope.getDeliveryTag(), autoAck);
                        }
                    });
        } catch (IOException exception) {
            throw new BallerinaException(exception);
        }
    }

    /**
     * Create and get message BMap.
     *
     * @param resource Ballerina resource function.
     * @param message  Message content received from the RabbitMQ server.
     * @param channel  RabbitMQ Channel object.
     * @return Ballerina RabbitMQ message BValue.
     */
    private BValue getMessageBMap(Resource resource, byte[] message, Channel channel, long deliveryTag,
                                  boolean autoAck) {
        ProgramFile programFile = resource.getResourceInfo().getPackageInfo().getProgramFile();
        BMap<String, BValue> messageObj = BLangConnectorSPIUtil.createBStruct(
                programFile, RabbitMQConstants.PACKAGE_RABBITMQ, RabbitMQConstants.MESSAGE_OBJECT);
        messageObj.addNativeData(RabbitMQConstants.DELIVERY_TAG, deliveryTag);
        messageObj.addNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT, channel);
        messageObj.addNativeData(RabbitMQConstants.MESSAGE_CONTENT, message);
        messageObj.addNativeData(RabbitMQConstants.AUTO_ACK_STATUS, autoAck);
        return messageObj;
    }

    /**
     * Request specific "quality of service" settings.
     *
     * @param channel         RabbitMQ Channel object.
     * @param annotationValue Struct value of the Annotation.
     */
    private static void handleBasicQos(Channel channel, Struct annotationValue) {
        long prefetchCount = RabbitMQConstants.DEFAULT_PREFETCH;
        if (annotationValue.getRefField(RabbitMQConstants.PREFETCH_COUNT) != null) {
            prefetchCount = annotationValue.getIntField(RabbitMQConstants.PREFETCH_COUNT);
        }
        boolean isValidPrefetchSize = annotationValue.getRefField(RabbitMQConstants.PREFETCH_SIZE) != null;
        try {
            if (isValidPrefetchSize) {
                channel.basicQos(Math.toIntExact(annotationValue.getIntField(RabbitMQConstants.PREFETCH_SIZE)),
                        Math.toIntExact(prefetchCount),
                        annotationValue.getBooleanField(RabbitMQConstants.PREFETCH_GLOBAL));
            } else {
                channel.basicQos(Math.toIntExact(prefetchCount));
            }
        } catch (IOException | ArithmeticException exception) {
            String errorMessage = "An error occurred while setting the basic quality of service settings ";
            throw new RabbitMQConnectorException(errorMessage + exception.getMessage(), exception);
        }
    }
}
