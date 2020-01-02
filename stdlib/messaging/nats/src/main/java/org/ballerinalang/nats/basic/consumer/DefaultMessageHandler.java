/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nats.basic.consumer;

import io.nats.client.Message;
import io.nats.client.MessageHandler;
import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;
import org.ballerinalang.nats.observability.NatsMetricsUtil;
import org.ballerinalang.nats.observability.NatsObservabilityConstants;
import org.ballerinalang.nats.observability.NatsObserverContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.nats.Constants.ON_MESSAGE_RESOURCE;
import static org.ballerinalang.nats.Utils.bindDataToIntendedType;
import static org.ballerinalang.nats.Utils.getAttachedFunction;

/**
 * Handles incoming message for a given subscription.
 *
 * @since 1.0.0
 */
public class DefaultMessageHandler implements MessageHandler {

    // Resource which the message should be dispatched.
    private ObjectValue serviceObject;
    private String connectedUrl;
    private BRuntime runtime;

    DefaultMessageHandler(ObjectValue serviceObject, BRuntime runtime, String connectedUrl) {
        this.serviceObject = serviceObject;
        this.runtime = runtime;
        this.connectedUrl = connectedUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(Message message) {
        NatsMetricsUtil.reportConsume(connectedUrl, message.getSubject(), message.getData().length);
        ArrayValue msgData = new ArrayValueImpl(message.getData());
        ObjectValue msgObj = BallerinaValues.createObjectValue(Constants.NATS_PACKAGE_ID,
                Constants.NATS_MESSAGE_OBJ_NAME, message.getSubject(), msgData, message.getReplyTo());
        AttachedFunction onMessage = getAttachedFunction(serviceObject, ON_MESSAGE_RESOURCE);
        BType[] parameterTypes = onMessage.getParameterType();
        if (parameterTypes.length == 1) {
            dispatch(msgObj);
        } else {
            BType intendedTypeForData = parameterTypes[1];
            dispatchWithDataBinding(msgObj, intendedTypeForData, message.getData());
        }
    }

    /**
     * Dispatch only the message to the onMessage resource.
     *
     * @param msgObj Message object
     */
    private void dispatch(ObjectValue msgObj) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        executeResource(msgObj, countDownLatch);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            NatsMetricsUtil.reportConsumerError(connectedUrl, msgObj.getStringValue(Constants.SUBJECT),
                                                NatsObservabilityConstants.ERROR_TYPE_MSG_RECEIVED);
            throw Utils.createNatsError(Constants.THREAD_INTERRUPTED_ERROR);
        }
    }

    /**
     * Dispatch message and type bound data to the onMessage resource.
     *
     * @param msgObj       Message object
     * @param intendedType Message type for data binding
     * @param data         Message data
     */
    private void dispatchWithDataBinding(ObjectValue msgObj, BType intendedType, byte[] data) {
        try {
            Object typeBoundData = bindDataToIntendedType(data, intendedType);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            executeResource(msgObj, countDownLatch, typeBoundData);
            countDownLatch.await();
        } catch (NumberFormatException e) {
            ErrorValue dataBindError = Utils
                    .createNatsError("The received message is unsupported by the resource signature");
            ErrorHandler.dispatchError(serviceObject, msgObj, dataBindError, runtime, connectedUrl);
        } catch (ErrorValue e) {
            ErrorHandler.dispatchError(serviceObject, msgObj, e, runtime, connectedUrl);
        } catch (InterruptedException e) {
            NatsMetricsUtil.reportConsumerError(connectedUrl, msgObj.getStringValue(Constants.SUBJECT),
                                                NatsObservabilityConstants.ERROR_TYPE_MSG_RECEIVED);
            Thread.currentThread().interrupt();
            throw Utils.createNatsError(Constants.THREAD_INTERRUPTED_ERROR);
        }
    }

    private void executeResource(ObjectValue msgObj, CountDownLatch countDownLatch) {
        String subject = msgObj.getStringValue(Constants.SUBJECT);
        if (ObserveUtils.isTracingEnabled()) {
            Map<String, Object> properties = new HashMap<>();
            NatsObserverContext observerContext = new NatsObserverContext(NatsObservabilityConstants.CONTEXT_CONSUMER,
                                                                          connectedUrl,
                                                                          msgObj.getStringValue(Constants.SUBJECT));
            properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);
            runtime.invokeMethodAsync(serviceObject, ON_MESSAGE_RESOURCE,
                                      new ResponseCallback(countDownLatch, connectedUrl, subject),
                                      properties, msgObj, Boolean.TRUE);
        } else {
            runtime.invokeMethodAsync(serviceObject, ON_MESSAGE_RESOURCE,
                                      new ResponseCallback(countDownLatch, connectedUrl, subject),
                                      msgObj, Boolean.TRUE);
        }
    }

    private void executeResource(ObjectValue msgObj, CountDownLatch countDownLatch, Object typeBoundData) {
        String subject = msgObj.getStringValue(Constants.SUBJECT);
        if (ObserveUtils.isTracingEnabled()) {
            Map<String, Object> properties = new HashMap<>();
            NatsObserverContext observerContext = new NatsObserverContext(NatsObservabilityConstants.CONTEXT_CONSUMER,
                                                                          connectedUrl,
                                                                          msgObj.getStringValue(Constants.SUBJECT));
            properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);
            runtime.invokeMethodAsync(serviceObject, ON_MESSAGE_RESOURCE,
                                                  new ResponseCallback(countDownLatch, connectedUrl, subject),
                                      properties, msgObj, true, typeBoundData, true);
        } else {
            runtime.invokeMethodAsync(serviceObject, ON_MESSAGE_RESOURCE,
                                                  new ResponseCallback(countDownLatch, connectedUrl, subject),
                                                  msgObj, true, typeBoundData, true);
        }
    }

    /**
     * Represents the callback which will be triggered upon submitting to resource.
     */
    public static class ResponseCallback implements CallableUnitCallback {
        private CountDownLatch countDownLatch;
        private String subject;
        private String connectedUrl;

        ResponseCallback(CountDownLatch countDownLatch, String connectedUrl, String subject) {
            this.countDownLatch = countDownLatch;
            this.connectedUrl = connectedUrl;
            this.subject = subject;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void notifySuccess() {
            NatsMetricsUtil.reportDelivery(connectedUrl, subject);
            countDownLatch.countDown();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void notifyFailure(ErrorValue error) {
            ErrorHandlerUtils.printError(error);
            NatsMetricsUtil.reportConsumerError(connectedUrl, subject,
                                                NatsObservabilityConstants.ERROR_TYPE_MSG_RECEIVED);
            countDownLatch.countDown();
        }
    }
}
