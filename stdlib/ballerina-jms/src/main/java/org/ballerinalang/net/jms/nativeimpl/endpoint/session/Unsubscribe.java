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

package org.ballerinalang.net.jms.nativeimpl.endpoint.session;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.AbstractBlockinAction;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * Unsubscribe a durable subscriber.
 */
@BallerinaFunction(orgName = "ballerina",
                   packageName = "jms",
                   functionName = "unsubscribe",
                   receiver = @Receiver(type = TypeKind.STRUCT,
                                        structType = "Session",
                                        structPackage = "ballerina.jms"),
                   args = {
                           @Argument(name = "subscriptionId",
                                     type = TypeKind.STRING)
                   },
                   isPublic = true)
public class Unsubscribe extends AbstractBlockinAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(Unsubscribe.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {

        Struct sessionBObject = BallerinaAdapter.getReceiverObject(context);

        Session session = BallerinaAdapter.getNativeObject(sessionBObject,
                                                           Constants.JMS_SESSION,
                                                           Session.class,
                                                           context);

        String subscriptionId = context.getStringArgument(0);

        try {
            session.unsubscribe(subscriptionId);
        } catch (JMSException e) {
            BallerinaAdapter.returnError("Unsubscribe request failed.", context, e);
        }
    }
}
