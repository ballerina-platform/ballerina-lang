/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.ballerinalang.net.jms;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * JMS Message listener implementation.
 */
public class JmsMessageListenerImpl implements MessageListener {

    private ObjectValue service;
    private ObjectValue queueConsumerBObject;
    private Scheduler scheduler;
    private AttachedFunction resource;
    private ObjectValue sessionObj;

    private ResponseCallback callback;

    public JmsMessageListenerImpl(Scheduler scheduler, ObjectValue service, ObjectValue queueConsumerBObject,
                                  ObjectValue sessionObj) {
        this.scheduler = scheduler;
        this.service = service;
        this.queueConsumerBObject = queueConsumerBObject;
        this.callback = new ResponseCallback();
        resource = service.getType().getAttachedFunctions()[0];
        this.sessionObj = sessionObj;
    }

    @Override
    public void onMessage(Message message) {
        Executor.submit(scheduler, service, resource.getName(), callback, null, getSignatureParameters(message));
    }

    private Object[] getSignatureParameters(Message jmsMessage) {
        ObjectValue messageObj = JmsUtils.createAndPopulateMessageObject(jmsMessage, sessionObj);
        BType[] parameterTypes = resource.getParameterType();

        Object[] bValues = new Object[parameterTypes.length * 2];

        bValues[0] = queueConsumerBObject;
        bValues[1] = true;
        bValues[2] = messageObj;
        bValues[3] = true;
        return bValues;
    }


    private static class ResponseCallback implements CallableUnitCallback {

        @Override
        public void notifySuccess() {
            // Nothing to do on success
        }

        @Override
        public void notifyFailure(ErrorValue error) {
            ErrorHandlerUtils.printError(error);
        }
    }
}
