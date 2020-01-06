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

package org.ballerinalang.nats.basic.producer;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.observability.NatsMetricsUtil;

import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.nats.Constants.CONNECTED_CLIENTS;

/**
 * Initialize NATS producer using the connection.
 *
 * @since 0.995
 */
public class Init {

    public static void producerInit(ObjectValue connectionObject) {
        // This is to add producer to the connected client list in connection object.
        ((AtomicInteger) connectionObject.getNativeData(CONNECTED_CLIENTS)).incrementAndGet();
        NatsMetricsUtil.reportNewProducer(connectionObject.getStringValue(Constants.URL));
    }
}
