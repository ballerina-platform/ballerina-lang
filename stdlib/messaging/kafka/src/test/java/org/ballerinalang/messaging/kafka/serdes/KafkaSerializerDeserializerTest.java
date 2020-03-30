/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.messaging.kafka.serdes;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_SERDES;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.createKafkaCluster;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.getFilePath;

/**
 * Test class for Ballerina Kafka Serializer / Deserializer tests.
 */
public class KafkaSerializerDeserializerTest {

    private CompileResult result;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;

    @BeforeClass
    public void setup() throws IOException {
        result = BCompileUtil.compileOffline(getFilePath(Paths.get(TEST_SRC, TEST_SERDES, "invalid_producers.bal")));
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-serdes-test");
        kafkaCluster = createKafkaCluster(dataDir, 14013, 14113).addBrokers(1).startup();
    }

    @Test(description = "Test Kafka producer avro serializer without schema url")
    public void testAvroProducerWithoutSchemaUrl() {
        String errorMessage = "Missing schema registry URL for the Avro serializer. Please provide " +
                "'schemaRegistryUrl' configuration in 'kafka:ProducerConfiguration'.";
        BValue[] returnValues = BRunUtil.invoke(result, "createAvroProducerWithoutSchemaUrlConfig");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BError, "Erroneous producer creation did not return error");
        String receivedMessage = ((BMap) ((BError) returnValues[0]).getDetails()).getMap().get("message").toString();
        Assert.assertEquals(receivedMessage, errorMessage, "Error message mismatch");
    }

    @Test(description = "Test Kafka producer avro key serializer without schema url")
    public void testAvroKeyProducerWithoutSchemaUrl() {
        String errorMessage = "Missing schema registry URL for the Avro serializer. Please provide " +
                "'schemaRegistryUrl' configuration in 'kafka:ProducerConfiguration'.";
        BValue[] returnValues = BRunUtil.invoke(result, "createAvroKeySerializerProducerWithoutSchemaUrlConfig");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BError, "Erroneous producer creation did not return error");
        String receivedMessage = ((BMap) ((BError) returnValues[0]).getDetails()).getMap().get("message").toString();
        Assert.assertEquals(receivedMessage, errorMessage, "Error message mismatch");
    }

    @Test(description = "Test Kafka producer custom key serializer without serializer object")
    public void testCustomKeySerializerWithoutSerializerObject() {
        String errorMessage =
                "Invalid keySerializer config: Please Provide a valid custom serializer for the keySerializer";
        BValue[] returnValues = BRunUtil.invoke(result, "createCustomKeySerializerWithoutSerializerObject");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BError, "Erroneous producer creation did not return error");
        String receivedMessage = ((BMap) ((BError) returnValues[0]).getDetails()).getMap().get("message").toString();
        Assert.assertEquals(receivedMessage, errorMessage, "Error message mismatch");
    }

    @Test(description = "Test Kafka producer custom value serializer without serializer object")
    public void testCustomValueSerializerWithoutSerializerObject() {
        String errorMessage =
                "Invalid valueSerializer config: Please Provide a valid custom serializer for the valueSerializer";
        BValue[] returnValues = BRunUtil.invoke(result, "createCustomValueSerializerWithoutSerializerObject");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BError, "Erroneous producer creation did not return error");
        String receivedMessage = ((BMap) ((BError) returnValues[0]).getDetails()).getMap().get("message").toString();
        Assert.assertEquals(receivedMessage, errorMessage, "Error message mismatch");
    }

    @AfterClass
    public void tearDown() {
        if (kafkaCluster != null) {
            kafkaCluster.shutdown();
            kafkaCluster = null;
            boolean delete = dataDir.delete();
            // If files are still locked and a test fails: delete on exit to allow subsequent test execution
            if (!delete) {
                dataDir.deleteOnExit();
            }
        }
    }
}
