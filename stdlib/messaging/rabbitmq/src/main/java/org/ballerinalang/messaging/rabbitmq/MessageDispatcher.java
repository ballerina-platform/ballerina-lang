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

package org.ballerinalang.messaging.rabbitmq;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.*;
import org.ballerinalang.model.types.*;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.util.XMLNodeType;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * Handles and dispatched messages with data binding.
 *
 * @since 0.995
 */
public class MessageDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

    private RabbitMQTransactionContext rabbitMQTransactionContext;
    private Resource onMessageResource;
    private Resource onErrorResource;
    private Channel channel;
    private boolean autoAck;
    private Context context;

    public MessageDispatcher(RabbitMQTransactionContext rabbitMQTransactionContext, Resource onMessageResource,
                             Resource onErrorResource, Channel channel, boolean autoAck, Context context) {
        this.rabbitMQTransactionContext = rabbitMQTransactionContext;
        this.onMessageResource = onMessageResource;
        this.onErrorResource = onErrorResource;
        this.channel = channel;
        this.autoAck = autoAck;
        this.context = context;
    }

    /**
     * Dispatch messages.
     *
     * @param message     Message content to be dispatched to the resource function.
     * @param deliveryTag Delivery tag of the message.
     */
    public void handleDispatch(byte[] message, long deliveryTag) {
        List<ParamDetail> paramDetails = onMessageResource.getParamDetails();
        int paramSize = paramDetails.size();
        if (paramSize > 1) {
            dispatchMessageWithDataBinding(message, deliveryTag);
        } else {
            dispatchMessage(message, deliveryTag);
        }
    }

    /**
     * Dispatch messages.
     *
     * @param message     Message content to be dispatched to the resource function.
     * @param deliveryTag Delivery tag of the message.
     */
    private void dispatchMessage(byte[] message, long deliveryTag) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            Executor.submit(onMessageResource, new RabbitMQResourceCallback(countDownLatch), null, null,
                    getMessageBMap(message, deliveryTag, autoAck));
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (AlreadyClosedException | BallerinaConnectorException exception) {
            handleRequeingMessage(deliveryTag);
        }
    }

    /**
     * Dispatches messages with data binding.
     *
     * @param message     Message content to be dispatched to the resource function.
     * @param deliveryTag Delivery tag of the message.
     */
    private void dispatchMessageWithDataBinding(byte[] message, long deliveryTag) {
        List<ParamDetail> paramDetails = onMessageResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        try {
            bValues[1] = getMessageContentForType(message, paramDetails.get(1).getVarType());
            bValues[0] = getMessageBMap(message, deliveryTag, autoAck);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Executor.submit(onMessageResource, new RabbitMQResourceCallback(countDownLatch), null, null,
                    bValues);
            countDownLatch.await();
        } catch (BallerinaConnectorException exception) {
            logger.error("The message received is not supported by the resource signature.", exception);
            handleRequeingMessage(deliveryTag);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (UnsupportedEncodingException exception) {
            handleError(message, deliveryTag);
        }
    }

    /**
     * If the message are received in client ack mode, re-queue messages upon internal errors.
     *
     * @param deliveryTag Delivery tag of the message.
     */
    public void handleRequeingMessage(long deliveryTag) {
        if (!autoAck) {
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException e) {

            }
        }
    }

    /**
     * Retrieve messages in the given type.
     *
     * @param message  The message body in bytes.
     * @param dataType The data type of the message for data binding.
     * @return Message in the given type.
     */
    private BValue getMessageContentForType(byte[] message, BType dataType) throws UnsupportedEncodingException {
        int dataTypeTag = dataType.getTag();

        switch (dataTypeTag) {
            case TypeTags.STRING_TAG:
                return new BString(new String(message, StandardCharsets.UTF_8.name()));
            case TypeTags.JSON_TAG:
                return JsonParser.parse(new String(message, StandardCharsets.UTF_8.name()));

            case TypeTags.XML_TAG:
                BXML bxml = XMLUtils.parse(new String(message, StandardCharsets.UTF_8.name()));
                if (bxml.getNodeType() != XMLNodeType.ELEMENT) {
                    throw new RabbitMQConnectorException("Invalid XML data");
                }
                return bxml;
            case TypeTags.FLOAT_TAG:
                return new BFloat(Float.parseFloat(new String(message, StandardCharsets.UTF_8.name())));
            case TypeTags.INT_TAG:
                return new BInteger(Integer.parseInt(new String(message, StandardCharsets.UTF_8.name())));
            case TypeTags.RECORD_TYPE_TAG:
                return JSONUtils.convertJSONToStruct
                        (JsonParser.parse(new String(message, StandardCharsets.UTF_8.name())),
                                (BStructureType) dataType);
            case TypeTags.ARRAY_TAG:
                if (((BArrayType) dataType).getElementType().getTag() == TypeTags.BYTE_TAG) {
                    return new BValueArray(message);
                } else {
                    throw new RabbitMQConnectorException("Only type byte[] is supported in data binding.");
                }
            default:
                throw new RabbitMQConnectorException(
                        "The content type of the message received does not match the resource signature type.");
        }

    }

    /**
     * Create and get message BMap.
     *
     * @param message Message content received from the RabbitMQ server.
     * @return Ballerina RabbitMQ message BValue.
     */
    private BValue getMessageBMap(byte[] message, long deliveryTag,
                                  boolean autoAck) {
        ProgramFile programFile = onMessageResource.getResourceInfo().getPackageInfo().getProgramFile();
        BMap<String, BValue> messageObj = BLangConnectorSPIUtil.createBStruct(
                programFile, RabbitMQConstants.PACKAGE_RABBITMQ, RabbitMQConstants.MESSAGE_OBJECT);
        messageObj.addNativeData(RabbitMQConstants.DELIVERY_TAG, deliveryTag);
        messageObj.addNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT, channel);
        messageObj.addNativeData(RabbitMQConstants.MESSAGE_CONTENT, message);
        messageObj.addNativeData(RabbitMQConstants.AUTO_ACK_STATUS, autoAck);
        if (!Objects.isNull(rabbitMQTransactionContext)) {
            messageObj.addNativeData(RabbitMQConstants.RABBITMQ_TRANSACTION_CONTEXT, rabbitMQTransactionContext);
        }
        messageObj.addNativeData(RabbitMQConstants.MESSAGE_ACK_STATUS, false);
        return messageObj;
    }

    /**
     * Triggers onError resource function upon error.
     *
     * @param message     Message content received from the RabbitMQ server.
     * @param deliveryTag Delivery tag of the message.
     */
    public void handleError(byte[] message, long deliveryTag) {
        List<ParamDetail> paramDetails = onErrorResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        BMap<String, BValue> errorRecord = RabbitMQUtils.createErrorRecord(context,
                RabbitMQConstants.MESSAGE_PROCESSING_ERROR,
                new RabbitMQConnectorException(RabbitMQConstants.MESSAGE_PROCESSING_ERROR));
        bValues[1] = BLangVMErrors.
                createError(context, true, BTypes.typeError, RabbitMQConstants.RABBITMQ_ERROR_CODE,
                        errorRecord);
        bValues[0] = getMessageBMap(message, deliveryTag, autoAck);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            Executor.submit(onErrorResource, new RabbitMQResourceCallback(countDownLatch), null, null,
                    bValues);
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (AlreadyClosedException | BallerinaConnectorException exception) {
            handleRequeingMessage(deliveryTag);
        }
    }
}
