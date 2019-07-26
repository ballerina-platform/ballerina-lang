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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Constants used in Ballerina Kafka tests.
 */
public class KafkaTestUtils {

    private KafkaTestUtils() {
    }

    public static final int KAFKA_BROKER_PORT = 9094;
    public static final int ZOOKEEPER_PORT_1 = 2181;

    public static final Path TEST_PATH = Paths.get("src", "test", "resources", "test-src");

    public static String getFilePath(String fileName) {
        return TEST_PATH.resolve(fileName).toAbsolutePath().toString();
    }
}
