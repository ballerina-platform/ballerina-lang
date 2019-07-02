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
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.services.ErrorHandlerUtils;

/**
 * Handles incoming message for a given subscription.
 */
public class DefaultMessageHandler implements MessageHandler {
    /**
     * Resource which the message should be dispatched.
     */
    private ObjectValue serviceObject;

    DefaultMessageHandler(ObjectValue serviceObject) {
        this.serviceObject = serviceObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(Message message) {
        ObjectValue msgObj = BallerinaValues.createObjectValue(Constants.NATS_PACKAGE,
                Constants.NATS_MESSAGE_OBJ_NAME, message.getData(), message.getSubject(), message.getReplyTo());
        msgObj.addNativeData(Constants.NATS_MSG, message);
        Executor.submit(serviceObject, "onMessage", new ResponseCallback(), null, msgObj);
    }

    /**
     * Represents the callback which will be triggered upon submitting to resource.
     */
    private static class ResponseCallback implements CallableUnitCallback {
        /**
         * {@inheritDoc}
         */
        @Override
        public void notifySuccess() {
            // Nothing to do on success
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void notifyFailure(ErrorValue error) {
            ErrorHandlerUtils.printError("error: " + error.getPrintableStackTrace());
        }
    }
}


