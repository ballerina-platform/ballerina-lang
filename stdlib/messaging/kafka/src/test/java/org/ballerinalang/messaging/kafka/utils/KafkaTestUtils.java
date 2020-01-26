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

package org.ballerinalang.messaging.kafka.utils;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Collect;
import kafka.server.KafkaConfig;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

/**
 * Constants used in Ballerina Kafka tests.
 */
public class KafkaTestUtils {

    private KafkaTestUtils() {
    }

    private static final Path TEST_PATH = Paths.get("src", "test", "resources");
    public static final String TEST_SRC = "test-src";
    public static final String TEST_CONSUMER = "consumer";
    public static final String TEST_PRODUCER = "producer";
    public static final String TEST_SERVICES = "services";
    public static final String TEST_COMPILER = "compiler-validation";
    public static final String TEST_SSL = "ssl";
    public static final String TEST_TRANSACTIONS = "transactions";

    public static String getFilePath(Path filePath) {
        return TEST_PATH.resolve(filePath).toAbsolutePath().toString();
    }

    public static void produceToKafkaCluster(KafkaCluster kafkaCluster, String topic, String message) {
        CountDownLatch completion = new CountDownLatch(1);
        kafkaCluster.useTo().produceStrings(topic, 10, completion::countDown, () -> message);
        try {
            completion.await();
        } catch (Exception ex) {
            //Ignore
        }
    }

    public static KafkaCluster createKafkaCluster(File dataDir, int zkPort, int brokerPort) {
        String timeout = "30000";
        return new KafkaCluster()
                .usingDirectory(dataDir)
                .deleteDataPriorToStartup(true)
                .deleteDataUponShutdown(true)
                .withKafkaConfiguration(Collect.propertiesOf(KafkaConfig.ZkSessionTimeoutMsProp(), timeout))
                .withPorts(zkPort, brokerPort);
    }
}
