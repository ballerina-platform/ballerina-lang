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

import io.nats.streaming.StreamingConnection;
import io.nats.streaming.Subscription;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.connection.NatsStreamingConnection;
import org.ballerinalang.nats.observability.NatsMetricsUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Close NATS streaming listener.
 *
 * @since 1.1.0
 */
public class Close {

    public static Object streamingListenerClose(ObjectValue streamingListener, Object natsConnection) {
        StreamingConnection streamingConnection = (StreamingConnection) streamingListener.getNativeData(
                Constants.NATS_STREAMING_CONNECTION);
        ConcurrentHashMap<ObjectValue, Subscription> subscriptionsMap =
                (ConcurrentHashMap<ObjectValue, Subscription>) streamingListener
                        .getNativeData(Constants.STREAMING_SUBSCRIPTION_LIST);
        for (Map.Entry<ObjectValue, Subscription> entry : subscriptionsMap.entrySet()) {
            String subject = entry.getValue().getSubject();
            NatsMetricsUtil.reportUnsubscription(streamingConnection.getNatsConnection().getConnectedUrl(), subject);
        }
        return NatsStreamingConnection.closeConnection(streamingListener, natsConnection);
    }
}
