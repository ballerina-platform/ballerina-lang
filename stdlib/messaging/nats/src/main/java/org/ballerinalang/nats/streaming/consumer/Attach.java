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

import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.observability.NatsMetricsReporter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.ballerinalang.nats.Constants.STREAMING_DISPATCHER_LIST;

/**
 * Create a listener and attach service.
 *
 * @since 1.0.0
 */
public class Attach {

    public static void streamingAttach(ObjectValue streamingListener, ObjectValue service,
                                       ObjectValue connectionObject) {
        List<ObjectValue> serviceList = (List<ObjectValue>) connectionObject.getNativeData(Constants.SERVICE_LIST);
        serviceList.add(service);
        ConcurrentHashMap<ObjectValue, StreamingListener> serviceListenerMap =
                (ConcurrentHashMap<ObjectValue, StreamingListener>) streamingListener
                        .getNativeData(STREAMING_DISPATCHER_LIST);
        boolean manualAck = getAckMode(service);
        String streamingConnectionUrl =
                streamingListener.getObjectValue(StringUtils.fromString("connection"))
                        .get(StringUtils.fromString("url")).toString();
        NatsMetricsReporter natsMetricsReporter =
                (NatsMetricsReporter) connectionObject.getNativeData(Constants.NATS_METRIC_UTIL);
        serviceListenerMap.put(service, new StreamingListener(service, manualAck, BRuntime.getCurrentRuntime(),
                                                              streamingConnectionUrl,
                                                              natsMetricsReporter));
    }

    private static boolean getAckMode(ObjectValue service) {
        MapValue serviceConfig = (MapValue) service.getType().getAnnotation(Constants.NATS_PACKAGE,
                Constants.NATS_STREAMING_SUBSCRIPTION_ANNOTATION);
        return serviceConfig.getBooleanValue(Constants.NATS_STREAMING_MANUAL_ACK);
    }
}
