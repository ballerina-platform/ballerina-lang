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
 *
 */

package org.ballerinalang.messaging.artemis.externimpl.consumer;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.messaging.artemis.ArtemisConnectorException;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.services.ErrorHandlerUtils;

import java.util.concurrent.CountDownLatch;

/**
 * The resource call back implementation for Artemis async consumer.
 *
 * @since 0.995
 */
public class ArtemisResourceCallback implements CallableUnitCallback {
    private ClientMessage message;
    private boolean autoAck;
    private ObjectValue sessionObj;
    private CountDownLatch countDownLatch;

    ArtemisResourceCallback(ClientMessage message, boolean autoAck, ObjectValue sessionObj,
                            CountDownLatch countDownLatch) {
        this.message = message;
        this.autoAck = autoAck;
        this.sessionObj = sessionObj;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void notifySuccess() {
        try {
            handleAutoAck();
        } finally {
            countDownLatch.countDown();
        }
    }

    @Override
    public void notifyFailure(ErrorValue error) {
        try {
            handleAutoAck();
        } finally {
            countDownLatch.countDown();
        }
        ErrorHandlerUtils.printError("error: " + error.getPrintableStackTrace());
    }

    private void handleAutoAck() {
        if (autoAck) {
            try {
                message.acknowledge();
                if (ArtemisUtils.isAnonymousSession(sessionObj)) {
                    ((ClientSession) sessionObj.getNativeData(ArtemisConstants.ARTEMIS_SESSION)).commit();
                }
            } catch (ActiveMQException e) {
                throw new ArtemisConnectorException("Failure during acknowledging the message", e);
            }
        }
    }
}
