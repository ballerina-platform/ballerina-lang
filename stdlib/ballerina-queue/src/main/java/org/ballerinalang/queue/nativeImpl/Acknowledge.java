/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.queue.nativeImpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.AbstractBlockinAction;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Session;

/**
 * Acknowledge the jms message.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "queue",
        functionName = "acknowledge",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Message",
                             structPackage = "ballerina.queue"),
        args = {@Argument(name = "deliveryStatus", type = TypeKind.STRING)},
        isPublic = true
)
public class Acknowledge extends AbstractBlockinAction {
    private static final Logger log = LoggerFactory.getLogger(Acknowledge.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        String deliveryStatus = context.getStringArgument(0);
        if (null == callableUnitCallback) {
            throw new BallerinaException("JMS Acknowledge function can only be used with JMS Messages. "
                    + Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE + " property is not found in the message.");
        }

        BStruct messageStruct = ((BStruct) context.getRefArgument(0));
        if (messageStruct.getNativeData(Constants.INBOUND_REQUEST) != null && !(Boolean) messageStruct
                .getNativeData(Constants.INBOUND_REQUEST)) {
            throw new BallerinaException(
                    "JMS Acknowledgement function can only be used with Inbound JMS Messages.");
        }

        Object jmsSessionAcknowledgementMode = context.getProperties().get(Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE);
        if (!(jmsSessionAcknowledgementMode instanceof Integer)) {
            throw new BallerinaException(
                    Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE + " property should hold a " + "integer value. ");
        }
        if (Session.CLIENT_ACKNOWLEDGE == (Integer) jmsSessionAcknowledgementMode) {
            if (Constants.JMS_MESSAGE_DELIVERY_ERROR.equalsIgnoreCase(deliveryStatus)) {
                throw new BallerinaException("Error when consuming the JMS message");
            } else {
                throw new BallerinaException(
                        "Second parameter for the jms:acknowledge function should be within the " + "set ["
                                + Constants.JMS_MESSAGE_DELIVERY_SUCCESS + ", " + Constants.JMS_MESSAGE_DELIVERY_ERROR
                                + "]. '" + deliveryStatus + "' is invalid.");
            }
        } else {
            log.warn("JMS Acknowledge function can only be used with JMS CLIENT ACKNOWLEDGEMENT Mode");
        }
    }
}
