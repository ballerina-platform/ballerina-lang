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
import org.ballerinalang.jvm.Scheduler;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.services.ErrorHandlerUtils;

/**
 * {@link MessageHandler} implementation to listen to Messages of the subscribed subject from NATS streaming server.
 */
public class StreamingListener implements MessageHandler {
    private ObjectValue service;
    private Scheduler scheduler;
    private static final String NATS_STREAMING_MESSAGE_OBJ_NAME = "StreamingMessage";
    private static final String ON_MESSAGE_RESOURCE = "onMessage";

    public StreamingListener(ObjectValue service, Scheduler threadScheduler) {
        this.service = service;
        this.scheduler = threadScheduler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(Message msg) {
        ObjectValue ballerinaNatsMessage = BallerinaValues
                .createObjectValue(Constants.NATS_PACKAGE, NATS_STREAMING_MESSAGE_OBJ_NAME,
                        msg.getSubject(), new ArrayValue(msg.getData()), msg.getReplyTo());
        ballerinaNatsMessage.addNativeData(Constants.NATS_STREAMING_MSG, msg);
        Executor.submit(scheduler, service, ON_MESSAGE_RESOURCE, new DispatcherCallback(), null, ballerinaNatsMessage,
                true);
    }

    private static class DispatcherCallback implements CallableUnitCallback {

        @Override
        public void notifySuccess() {
            // Ignore since there aren't any actions to be taken upon success
        }

        @Override
        public void notifyFailure(ErrorValue error) {
            ErrorHandlerUtils.printError("error:" + error.getPrintableStackTrace());
        }
    }
}
