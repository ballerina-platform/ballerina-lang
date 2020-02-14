/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.kafka;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Collect;
import io.debezium.util.Testing;
import io.netty.handler.codec.http.HttpHeaderNames;
import kafka.server.KafkaConfig;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.io.File.separator;

/**
 * Test class for test the functionality of Ballerina Kafka serializers and deserializers.
 */
public class KafkaSerializerDeserializerTest extends BaseTest {

    protected static BServerInstance serverInstance;
    private static final String resourceLocation = "src" + separator + "test" + separator + "resources" + separator
            + "messaging" + separator + "kafka" + separator;
    private static KafkaCluster kafkaCluster;
    private static final String NAME = "Thisaru Guruge";
    private static final String AGE = "29";
    private static final String PATH = separator + "sendData";

    @BeforeTest(alwaysRun = true)
    public void start() throws BallerinaTestException, IOException {
        int[] requiredPorts = new int[]{14001, 14002, 14003};
        String sourcePath = new File(resourceLocation).getAbsolutePath();
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(sourcePath, requiredPorts);
        File dataDir = Testing.Files.createTestingDirectory("cluster-kafka-serdes-test");
        kafkaCluster = createKafkaCluster(dataDir, 14002, 14102).addBrokers(1).startup();
    }

    @Test(description = "Tests Kafka custom serializer / deserializer functionality")
    public void testPublishToKafkaCluster() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        String message = "{\"name\":\"" + NAME + "\",\"age:\"" + AGE + "\"}";
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(14001, PATH), message,
                                                         headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Successfully received", "Message content mismatched");
    }

    @AfterTest(alwaysRun = true)
    private void cleanup() {
        if (kafkaCluster.isRunning()) {
            kafkaCluster.shutdown();
        }
    }

    private static KafkaCluster createKafkaCluster(File dataDir, int zkPort, int brokerPort) {
        String timeout = "20000";
        Properties properties = Collect.propertiesOf(KafkaConfig.ZkSessionTimeoutMsProp(), timeout);
        KafkaCluster kafkaCluster = new KafkaCluster()
                .usingDirectory(dataDir)
                .deleteDataPriorToStartup(true)
                .deleteDataUponShutdown(true)
                .withKafkaConfiguration(properties)
                .withPorts(zkPort, brokerPort);
        return kafkaCluster;
    }
}
