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

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SECURITY;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getErrorMessageFromReturnValue;

/**
 * This class contains tests for Ballerina Kafka producers with SASL authentication.
 */
public class SaslProducerTest {
    private CompileResult result;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;
    private static final String resourceDir = Paths.get("src", "test", "resources").toString();
    private static final String balFile = Paths.get(TEST_SRC, TEST_SECURITY, "sasl_plain_producer.bal").toString();
    private static final String jaasConfigs = Paths.get("data-files", "sasl_configs").toString();

    @BeforeSuite
    public void setJaasFilePath() {
         /* Set system property for JAAS config. This should set Before Suit since TestNg not picking it from before
         class when there are more than one test in the test suite. */
        System.setProperty("java.security.auth.login.config",
                           resourceDir + File.separator + jaasConfigs + File.separator + "kafka_server_jaas.conf");
    }

    @BeforeClass
    public void setup() throws IOException {
        Properties prop = getKafkaBrokerProperties();
        kafkaCluster = kafkaCluster(prop).deleteDataPriorToStartup(true)
                .deleteDataUponShutdown(true).addBrokers(1).startup();
        kafkaCluster.createTopic("test-1", 1, 1);
        result = BCompileUtil.compile(Paths.get(resourceDir, balFile).toAbsolutePath().toString());
    }

    // TODO: Disabled since topic auto creation is not working. Check further and enable this.
    @Test(description = "Test kafka consumer connect with SASL Plain authentication", enabled = false)
    public void testSaslAuthentication() {
        BValue[] returnBValues = BRunUtil.invoke(result, "sendFromValidProducer");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertFalse(returnBValues[0] instanceof BError, returnBValues[0].stringValue());
    }

    @Test(description = "Test kafka consumer connect with SASL Plain authentication with invalid username")
    public void testSaslAuthenticationWithInvalidUsername() {
        String expectedError = "Failed to send data to Kafka server: Authentication failed: Invalid username " +
                "or password";
        BValue[] returnBValues = BRunUtil.invoke(result, "sendFromInvalidUsernameProducer");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BError);
        Assert.assertEquals(getErrorMessageFromReturnValue(returnBValues[0]), expectedError);
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

    private static KafkaCluster kafkaCluster(Properties prop) {
        if (kafkaCluster != null) {
            throw new IllegalStateException();
        }
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-sasl-test");
        kafkaCluster = new KafkaCluster().usingDirectory(dataDir)
                .withKafkaConfiguration(prop)
                .withPorts(14015, 14115);
        return kafkaCluster;
    }

    private static Properties getKafkaBrokerProperties() {
        Properties prop = new Properties();
        prop.put("listeners", "SASL_PLAINTEXT://localhost:14115");
        prop.put("advertised.listeners", "SASL_PLAINTEXT://localhost:14115");
        prop.put("security.inter.broker.protocol", "SASL_PLAINTEXT");
        prop.put("sasl.mechanism.inter.broker.protocol", "PLAIN");
        prop.put("sasl.enabled.mechanisms", "PLAIN");
        prop.put("zookeeper.session.timeout.ms", "25000");
        prop.put("zookeeper.connection.timeout.ms", "25000");
        return prop;
    }
}
