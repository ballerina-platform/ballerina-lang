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

package org.ballerinalang.nats.streaming.producer;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.connection.NatsStreamingConnection;
import org.ballerinalang.nats.observability.NatsMetricsUtil;

/**
 * Initialize NATS producer using the connection.
 *
 * @since 1.1.0
 */
public class Init {

    public static void streamingProducerInit(ObjectValue streamingClientObject, ObjectValue connectionObject,
                                             String clusterId, Object clientIdNillable, Object streamingConfig) {
        NatsMetricsUtil natsMetricsUtil = (NatsMetricsUtil) connectionObject.getNativeData(Constants.NATS_METRIC_UTIL);
        natsMetricsUtil.reportNewProducer();
        NatsStreamingConnection.createConnection(streamingClientObject, connectionObject, clusterId, clientIdNillable,
                                                 streamingConfig);
    }
}
