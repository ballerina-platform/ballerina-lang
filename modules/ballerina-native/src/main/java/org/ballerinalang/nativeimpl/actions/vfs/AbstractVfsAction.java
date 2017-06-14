/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.nativeimpl.actions.vfs;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.natives.connectors.BalConnectorCallback;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;

import java.util.Map;

/**
 * {@code AbstractVfsAction} is the base class for all VFS Connector Actions.
 */

public abstract class AbstractVfsAction extends AbstractNativeAction {
    private static final Logger logger = LoggerFactory.getLogger(AbstractVfsAction.class);
    private static final long SENDER_TIMEOUT = 180000;

    /**
     *Send message to file client connector expecting a callback.
     *
     * @param message     Message to send to client connector. Value can be null
     * @param propertyMap Property map to send to client connector. Can be null
     * @param context     Context to send to client connector. Cannot be null.
     * @return Response message from transport
     */
    protected CarbonMessage executeCallbackAction(CarbonMessage message, Map<String, String> propertyMap,
                                                  Context context) {
        BalConnectorCallback callback = new BalConnectorCallback(context);
        try {
            //Getting the sender instance and sending the message.
            BallerinaConnectorManager.getInstance().getClientConnector(Constants.VFS_CONNECTOR_NAME)
                                     .send(message, callback, propertyMap);
        } catch (ClientConnectorException e) {
            throw new BallerinaException(e.getMessage(), e, context);
        }
        try {
            // Wait till Response comes
            long startTime = System.currentTimeMillis();
            while (!callback.isResponseArrived()) {
                synchronized (context) {
                    if (!callback.isResponseArrived()) {
                        logger.debug("Waiting for a response");
                        context.wait(SENDER_TIMEOUT);
                        if (System.currentTimeMillis() >= (startTime + SENDER_TIMEOUT)) {
                            throw new RuntimeException("response was not received within sender timeout of " +
                                                       SENDER_TIMEOUT / 1000 + " seconds");
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.debug("Interrupted while waiting for isExists callback", e);
        }
        return ((BMessage) callback.getValueRef()).value();
    }
}
