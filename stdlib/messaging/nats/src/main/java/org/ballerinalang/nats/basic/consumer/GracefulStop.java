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
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;
import org.ballerinalang.nats.observability.NatsMetricsUtil;
import org.ballerinalang.nats.observability.NatsObservabilityConstants;
import org.ballerinalang.nats.observability.NatsTracingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.nats.Constants.BASIC_SUBSCRIPTION_LIST;
import static org.ballerinalang.nats.Constants.DISPATCHER_LIST;

/**
 * Extern function to gracefully stop the NATS subscriber.
 *
 * @since 1.0.0
 */
public class GracefulStop {

    private static final Logger LOG = LoggerFactory.getLogger(GracefulStop.class);

    public static void basicGracefulStop(ObjectValue listenerObject) {
        NatsTracingUtil.traceResourceInvocation(Scheduler.getStrand(), listenerObject);
        ObjectValue connectionObject = (ObjectValue) listenerObject.get(Constants.CONNECTION_OBJ);
        if (connectionObject == null) {
            NatsMetricsUtil.reportConsumerError(NatsObservabilityConstants.ERROR_TYPE_CLOSE);
            LOG.debug("Connection object reference does not exist. Possibly the connection is already closed.");
            return;
        }
        Connection natsConnection =
                (Connection) connectionObject.getNativeData(Constants.NATS_CONNECTION);
        if (natsConnection == null) {
            NatsMetricsUtil.reportConsumerError(NatsObservabilityConstants.ERROR_TYPE_CLOSE);
            LOG.debug("NATS connection does not exist. Possibly the connection is already closed.");
            return;
        }
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Dispatcher> dispatcherList = (ConcurrentHashMap<String, Dispatcher>)
                listenerObject.getNativeData(DISPATCHER_LIST);
        Iterator dispatchers = dispatcherList.entrySet().iterator();
        while (dispatchers.hasNext()) {
            Map.Entry pair = (Map.Entry) dispatchers.next();
            natsConnection.closeDispatcher((Dispatcher) pair.getValue());
            dispatchers.remove(); // avoids a ConcurrentModificationException
        }
        ArrayList<String> subscriptionsList =
                (ArrayList<String>) listenerObject
                        .getNativeData(BASIC_SUBSCRIPTION_LIST);
        NatsMetricsUtil.reportBulkUnsubscription(natsConnection.getConnectedUrl(), subscriptionsList);

        int clientsCount =
                ((AtomicInteger) connectionObject.getNativeData(Constants.CONNECTED_CLIENTS)).decrementAndGet();

        if (clientsCount == 0) {
            try {
                // Wait for the drain to succeed, passed 0 to wait forever.
                natsConnection.drain(Duration.ZERO);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                NatsMetricsUtil.reportConsumerError(natsConnection.getConnectedUrl(),
                                                    NatsObservabilityConstants.UNKNOWN,
                                                    NatsObservabilityConstants.ERROR_TYPE_CLOSE);
                throw Utils.createNatsError("Listener interrupted on graceful stop.");
            } catch (TimeoutException e) {
                NatsMetricsUtil.reportConsumerError(natsConnection.getConnectedUrl(),
                                                    NatsObservabilityConstants.UNKNOWN,
                                                    NatsObservabilityConstants.ERROR_TYPE_CLOSE);
                throw Utils.createNatsError("Timeout error occurred, on graceful stop.");
            } catch (IllegalStateException e) {
                NatsMetricsUtil.reportConsumerError(natsConnection.getConnectedUrl(),
                                                    NatsObservabilityConstants.UNKNOWN,
                                                    NatsObservabilityConstants.ERROR_TYPE_CLOSE);
                throw Utils.createNatsError("Connection is already closed.");
            }
        }
    }

}
