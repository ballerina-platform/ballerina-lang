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
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.AbstractNonBlockinAction;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Session;

/**
 * To commit the jms transacted sessions.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "queue",
        functionName = "commit",
        receiver = @Receiver(type = TypeKind.STRUCT,
                             structType = "Message",
                             structPackage = "ballerina.queue"),
        isPublic = true
)
public class Commit extends AbstractNonBlockinAction {
    private static final Logger log = LoggerFactory.getLogger(Commit.class);

    public void execute(Context ctx, CallableUnitCallback callback) {

        if (null == callback) {
            throw new BallerinaException("JMS Commit function can only be used with JMS Messages. "
                                                 + Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE
                                                 + " property is not found in the message.", ctx);
        }

        BStruct messageStruct = ((BStruct) ctx.getRefArgument(0));
        if (messageStruct.getNativeData(Constants.INBOUND_REQUEST) != null && !(Boolean) messageStruct
                .getNativeData(Constants.INBOUND_REQUEST)) {
            throw new BallerinaException("JMS Commit function can only be used with Inbound JMS Messages.", ctx);
        }

        Object jmsSessionAcknowledgementMode = ctx.getProperties().get(Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE);
        if (!(jmsSessionAcknowledgementMode instanceof Integer)) {
            throw new BallerinaException(
                    Constants.JMS_SESSION_ACKNOWLEDGEMENT_MODE + " property should hold a " + "integer value. ");
        }
        if (Session.SESSION_TRANSACTED == (Integer) jmsSessionAcknowledgementMode) {
            callback.notifySuccess();

        } else {
            String errorMessage = "JMS Commit function can only be used with JMS Session Transacted Mode.";
            log.warn(errorMessage);
            callback.notifyFailure(BLangVMErrors.createError(ctx, errorMessage));
        }
    }
}
