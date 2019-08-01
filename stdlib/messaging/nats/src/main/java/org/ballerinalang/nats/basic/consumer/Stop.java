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

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.nats.Constants.DISPATCHER_LIST;

/**
 * Extern function to stop the NATS subscriber.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "nats",
        functionName = "stop",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener",
                structPackage = "ballerina/nats"),
        isPublic = true
)
public class Stop {

    private static final Logger LOG = LoggerFactory.getLogger(Stop.class);

    public static void stop(Strand strand, ObjectValue listenerObject) {
        ObjectValue connectionObject = (ObjectValue) listenerObject.get(Constants.CONNECTION_OBJ);
        if (connectionObject == null) {
            LOG.debug("Connection object reference not exists. Possibly connection already closed.");
            return;
        }
        Connection natsConnection =
                (Connection) connectionObject.getNativeData(Constants.NATS_CONNECTION);
        if (natsConnection == null) {
            LOG.debug("NATS connection not exists. Possibly connection already closed.");
            listenerObject.set(Constants.CONNECTION_OBJ, null);
            return;
        }
        @SuppressWarnings("unchecked")
        List<Dispatcher> dispatcherList = (List<Dispatcher>) listenerObject.getNativeData(DISPATCHER_LIST);
        dispatcherList.forEach(natsConnection::closeDispatcher);

        int clientsCount =
                ((AtomicInteger) connectionObject.getNativeData(Constants.CONNECTED_CLIENTS)).decrementAndGet();
        listenerObject.set(Constants.CONNECTION_OBJ, null);

        if (clientsCount == 0) {
            // Actual NATS connection is not used in any other clients. So we can close the actual connection.
            try {
                natsConnection.close();
            } catch (InterruptedException e) {
                throw new BallerinaConnectorException("Listener interrupted while closing NATS connection");
            }
        }
    }
}
