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
package org.ballerinalang.nats.streaming.consumer;

import io.nats.streaming.Message;
import io.nats.streaming.MessageHandler;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;

import static org.ballerinalang.nats.Constants.NATS_STREAMING_MESSAGE_OBJ_NAME;
import static org.ballerinalang.nats.Constants.ON_ERROR_RESOURCE;
import static org.ballerinalang.nats.Constants.ON_MESSAGE_RESOURCE;
import static org.ballerinalang.nats.Utils.getAttachedFunction;

/**
 * {@link MessageHandler} implementation to listen to Messages of the subscribed subject from NATS streaming server.
 */
public class StreamingListener implements MessageHandler {
    private ObjectValue service;
    private Scheduler scheduler;

    public StreamingListener(ObjectValue service, Scheduler threadScheduler) {
        this.service = service;
        this.scheduler = threadScheduler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(Message msg) {
        ObjectValue ballerinaNatsMessage = BallerinaValues.createObjectValue(Constants.NATS_PACKAGE_ID,
                NATS_STREAMING_MESSAGE_OBJ_NAME, msg.getSubject(),
                        new ArrayValueImpl(msg.getData()), msg.getReplyTo());
        ballerinaNatsMessage.addNativeData(Constants.NATS_STREAMING_MSG, msg);
        AttachedFunction onMessageResource = getAttachedFunction(service, "onMessage");
        BType[] parameterTypes = onMessageResource.getParameterType();
        if (parameterTypes.length == 1) {
            dispatch(ballerinaNatsMessage);
        } else {
            BType intendedTypeForData = parameterTypes[1];
            dispatch(ballerinaNatsMessage, intendedTypeForData, msg.getData());
        }
    }

    private void dispatch(ObjectValue ballerinaNatsMessage) {
        Executor.submit(scheduler, service, ON_MESSAGE_RESOURCE, new DispatcherCallback(), null, ballerinaNatsMessage,
                true);
    }

    private void dispatch(ObjectValue ballerinaNatsMessage, BType intendedTypeForData, byte[] data) {
        try {
            Object typeBoundData = Utils.bindDataToIntendedType(data, intendedTypeForData);
            Executor.submit(scheduler, service, ON_MESSAGE_RESOURCE, new DispatcherCallback(), null,
                    ballerinaNatsMessage, true, typeBoundData, true);
        } catch (NumberFormatException e) {
            ErrorValue dataBindError = Utils
                    .createNatsError("The received message is unsupported by the resource signature");
            Executor.submit(scheduler, service, ON_ERROR_RESOURCE, new DispatcherCallback(), null,
                    ballerinaNatsMessage, true, dataBindError, true);
        } catch (ErrorValue e) {
            Executor.submit(scheduler, service, ON_ERROR_RESOURCE, new DispatcherCallback(), null,
                    ballerinaNatsMessage, true, e, true);
        }
    }

    private static class DispatcherCallback implements CallableUnitCallback {

        @Override
        public void notifySuccess() {
            // Ignore since there aren't any actions to be taken upon success
        }

        @Override
        public void notifyFailure(ErrorValue error) {
            ErrorHandlerUtils.printError(error);
        }
    }
}
