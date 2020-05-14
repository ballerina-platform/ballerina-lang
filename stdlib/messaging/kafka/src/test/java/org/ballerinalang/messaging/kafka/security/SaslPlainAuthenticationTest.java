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

package org.ballerinalang.messaging.kafka.security;

import kafka.server.KafkaConfig;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.ballerinalang.messaging.kafka.utils.KafkaCluster;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.TestUtils.PROTOCOL_SASL_PLAIN;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.STRING_DESERIALIZER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SECURITY;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.ZOOKEEPER_CONNECTION_TIMEOUT_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.finishTest;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getDataDirectoryName;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getErrorMessageFromReturnValue;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;

/**
 * This class contains tests for Ballerina Kafka producers with SASL authentication.
 */
public class SaslPlainAuthenticationTest {
    private CompileResult producerResult;
    private CompileResult consumerResult;
    private static KafkaCluster kafkaCluster;
    private static final String dataDir = getDataDirectoryName(SaslPlainAuthenticationTest.class.getSimpleName());
    private static final String producerBalFile = "sasl_plain_producer.bal";
    private static final String consumerBalFile = "sasl_plain_consumer.bal";
    private static final String topic = "test-1";
    private static final List<String> topicsList = Collections.singletonList(topic);

    @BeforeClass(alwaysRun = true)
    public void setup() throws Throwable {
        kafkaCluster = new KafkaCluster(dataDir)
                .withZookeeper(14021)
                .withBroker(PROTOCOL_SASL_PLAIN, 14121, getKafkaBrokerProperties())
                .withAdminClient(getClientProperties())
                .withConsumer(STRING_DESERIALIZER, STRING_DESERIALIZER, "sasl-java-consumer",
                              topicsList, getClientProperties())
                .start();
        kafkaCluster.createTopic(topic, 1, 1);
        producerResult = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_SECURITY, producerBalFile)));
        consumerResult = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_SECURITY, consumerBalFile)));
    }

    @Test(description = "Test kafka producer with SASL PLAIN authentication")
    public void testSaslAuthenticationProducer() {
        BValue[] returnBValues = BRunUtil.invoke(producerResult, "sendFromValidProducer");
        Assert.assertEquals(returnBValues.length, 1);
        String message = kafkaCluster.consumeMessage(3000);
        Assert.assertEquals(message, "Hello from Ballerina");
    }

    @Test(description = "Test kafka producer with SASL PLAIN authentication providing invalid username")
    public void testSaslAuthenticationProducerWithInvalidUsername() {
        String expectedError =
                "Failed to send data to Kafka server: Authentication failed: Invalid username or password";
        BValue[] returnBValues = BRunUtil.invoke(producerResult, "sendFromInvalidUsernameProducer");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BError);
        Assert.assertEquals(getErrorMessageFromReturnValue(returnBValues[0]), expectedError);
    }

    @Test(description = "Test kafka consumer with SASL PLAIN authentication")
    public void testSaslAuthenticationConsumer() {
        BValue[] returnBValues = BRunUtil.invoke(consumerResult, "getTopicsForValidConsumer");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertFalse(returnBValues[0] instanceof BError, returnBValues[0].stringValue());
    }

    @Test(description = "Test kafka consumer with SASL PLAIN authentication providing invalid password")
    public void testSaslAuthenticationConsumerWithInvalidPassword() {
        String expectedError =
                "Failed to retrieve available topics: Authentication failed: Invalid username or password";
        BValue[] returnBValues = BRunUtil.invoke(consumerResult, "getTopicsForInvalidPasswordConsumer");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BError);
        Assert.assertEquals(getErrorMessageFromReturnValue(returnBValues[0]), expectedError);
    }

    private static Properties getClientProperties() {
        Properties properties = new Properties();
        String jaasConfigValue =
                "org.apache.kafka.common.security.plain.PlainLoginModule required" +
                        "  username=\"ballerina\"" +
                        "  password=\"ballerina-secret\";";
        properties.setProperty(SaslConfigs.SASL_MECHANISM, "PLAIN");
        properties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        properties.setProperty(SaslConfigs.SASL_JAAS_CONFIG, jaasConfigValue);
        return properties;
    }

    private static Properties getKafkaBrokerProperties() {
        String jaasConfig = "org.apache.kafka.common.security.plain.PlainLoginModule required" +
                "  username=\"admin\"" +
                "  password=\"admin-secret\"" +
                "  user_admin=\"admin-secret\"" +
                "  user_ballerina=\"ballerina-secret\";";
        Properties properties = new Properties();
        properties.setProperty(KafkaConfig.ListenersProp(), "SASL_PLAINTEXT://localhost:14121");
        properties.setProperty("listener.name.sasl_plaintext.plain.sasl.jaas.config", jaasConfig);
        properties.setProperty("advertised.listeners", "SASL_PLAINTEXT://localhost:14121");
        properties.setProperty("security.inter.broker.protocol", "SASL_PLAINTEXT");
        properties.setProperty("sasl.mechanism.inter.broker.protocol", "PLAIN");
        properties.setProperty("sasl.enabled.mechanisms", "PLAIN");
        properties.setProperty(KafkaConfig.ZkConnectionTimeoutMsDoc(), ZOOKEEPER_CONNECTION_TIMEOUT_CONFIG);
        return properties;
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        finishTest(kafkaCluster, dataDir);
    }
}
