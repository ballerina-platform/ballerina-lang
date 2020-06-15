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

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SERDES;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;

/**
 * Test class for Ballerina Kafka Serializer / Deserializer tests.
 */
public class SerializerDeserializerTest {

    private CompileResult result;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        String balFile = "invalid_producers.bal";
        result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_SERDES, balFile)));
    }

    @Test(description = "Test Kafka producer avro serializer without schema url")
    public void testAvroProducerWithoutSchemaUrl() {
        String errorMessage = "Missing schema registry URL for the Avro serializer. Please provide " +
                "'schemaRegistryUrl' configuration in 'kafka:ProducerConfiguration'.";
        BValue[] returnValues = BRunUtil.invoke(result, "createAvroProducerWithoutSchemaUrlConfig");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BError, "Erroneous producer creation did not return error");
        String receivedMessage = ((BError) returnValues[0]).getMessage();
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
        String receivedMessage = ((BError) returnValues[0]).getMessage();
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
        String receivedMessage = ((BError) returnValues[0]).getMessage();
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
        String receivedMessage = ((BError) returnValues[0]).getMessage();
        Assert.assertEquals(receivedMessage, errorMessage, "Error message mismatch");
    }
}
