/*
 *  Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.messaging.kafka.utils;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;

import java.util.Properties;

/**
 * Creates a local Kafka server instance for testing Kafka.
 */
public class KafkaLocal {
    public KafkaServerStartable kafka;

    public KafkaLocal(Properties properties) {
        KafkaConfig kafkaConfig = KafkaConfig.fromProps(properties);

        // start local kafka broker
        kafka = new KafkaServerStartable(kafkaConfig);
    }

    public void start() throws Exception {
        kafka.startup();
    }

    public void stop() {
        kafka.shutdown();
    }
}
