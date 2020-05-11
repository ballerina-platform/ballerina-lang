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

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Constants used in Ballerina Kafka tests.
 */
public class TestUtils {

    private TestUtils() {
    }

    public static final Path TEST_PATH = Paths.get("src", "test", "resources");
    public static final String TEST_SRC = "test-src";
    public static final String TEST_CONSUMER = "consumer";
    public static final String TEST_PRODUCER = "producer";
    public static final String TEST_SERVICES = "services";
    public static final String TEST_SERDES = "serdes";
    public static final String TEST_COMPILER = "compiler-validation";
    public static final String TEST_SECURITY = "security";
    public static final String TEST_TRANSACTIONS = "transactions";

    public static final String PROTOCOL_PLAINTEXT = "PLAINTEXT";
    public static final String PROTOCOL_SSL = "SSL";
    public static final String PROTOCOL_SASL_PLAIN = "SASL_PLAINTEXT";
    public static final String STRING_SERIALIZER = StringSerializer.class.getName();
    public static final String STRING_DESERIALIZER = StringDeserializer.class.getName();

    private static final int ZOOKEEPER_CONNECTION_TIMEOUT = 10000;

    public static String getResourcePath(Path filePath) {
        return TEST_PATH.resolve(filePath).toAbsolutePath().toString();
    }

    public static void produceToKafkaCluster(KafkaCluster kafkaCluster, String topic, String message)
            throws ExecutionException, InterruptedException {
        for (int i = 0; i < 10; i++) {
            kafkaCluster.sendMessage(topic, message);
        }
    }

    public static Properties getZookeeperTimeoutProperty() {
        Properties properties = new Properties();
        properties.setProperty("zookeeper.connection.timeout.ms", Integer.toString(ZOOKEEPER_CONNECTION_TIMEOUT));
        return properties;
    }

    public static String getErrorMessageFromReturnValue(BValue value) {
        return ((BMap) ((BError) value).getDetails()).get("message").stringValue();
    }

    public static void finishTest(KafkaCluster kafkaCluster, String dataDir) throws IOException {
        if (kafkaCluster != null) {
            kafkaCluster.stop();
        }
        boolean deleted = Files.deleteIfExists(Paths.get(dataDir));
        if (!deleted) {
            File file = new File(dataDir);
            file.deleteOnExit();
        }
    }

    public static String getDataDirectoryName(String testName) {
        return Paths.get("build", "kafka-logs", testName).toString();
    }
}
