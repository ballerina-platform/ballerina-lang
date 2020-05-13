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
import org.ballerinalang.messaging.kafka.utils.KafkaCluster;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.UNCHECKED;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.PROTOCOL_SSL;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SECURITY;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.ZOOKEEPER_CONNECTION_TIMEOUT_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.deleteDirectory;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.finishTest;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getDataDirectoryName;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getErrorMessageFromReturnValue;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;

/**
 * Test cases for ballerina.kafka consumer and producer with SSL.
 */
public class SslConsumerAndProducerTest {

    private CompileResult result;
    private static KafkaCluster kafkaCluster;
    private static final String balFile = "ssl_producer_consumer.bal";
    private static final String balFilePath = getResourcePath(Paths.get(TEST_SRC, TEST_SECURITY, balFile));
    private static final String dataDir = getDataDirectoryName(SslConsumerAndProducerTest.class.getSimpleName());
    private static final String filePath = "<FILE_PATH>";
    private static final String keystoreAndTruststore = getResourcePath(Paths.get("data-files", "keystore-truststore"));

    @BeforeClass(alwaysRun = true)
    public void setup() throws Throwable {
        deleteDirectory(dataDir);
        kafkaCluster = new KafkaCluster(dataDir)
                .withZookeeper(14022)
                .withBroker(PROTOCOL_SSL, 14122, getKafkaBrokerProperties())
                .withAdminClient(getClientProperties())
                .start();
        kafkaCluster.createTopic("test-topic-ssl", 2, 1);
        //Setting the keystore and trust-store file paths
        String filePathString = Paths.get(balFilePath).toAbsolutePath().toString();
        setFilePath(filePathString, filePath, Paths.get(keystoreAndTruststore).toAbsolutePath().toString());
        result = BCompileUtil.compile(balFilePath);
    }

    @Test(description = "Test SSL producer and consumer")
    public void testWithValidSslConfigs() {
        String message = "Hello World SSL Test";
        BValue[] args = new BValue[1];
        args[0] = new BString(message);
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] returnBValues = BRunUtil.invoke(result, "testProducerWithSsl", args);
            Assert.assertEquals(returnBValues.length, 1);
            Assert.assertTrue(returnBValues[0] instanceof BBoolean);
            return ((BBoolean) returnBValues[0]).booleanValue();
        });

        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] returnBValues = BRunUtil.invoke(result, "testPollWithSsl");
            Assert.assertEquals(returnBValues.length, 1);
            Assert.assertTrue(returnBValues[0] instanceof BString);
            return returnBValues[0].stringValue().equals(message);
        });
    }

    @SuppressWarnings(UNCHECKED)
    @Test(description = "Test kafka consumer connect with no SSL config values")
    public void testWithInvalidSslConfigs() {
        BValue[] returnBValues = BRunUtil.invoke(result, "testSslConnectNegative");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BError);
        String errorMessage =
                "Failed to send data to Kafka server: Topic test-topic-ssl not present in metadata after 1000 ms.";
        Assert.assertEquals(getErrorMessageFromReturnValue(returnBValues[0]), errorMessage);
    }

    private static void setFilePath(String path, String searchValue, String newValue) {
        List<String> lines = new ArrayList<>();
        String line;
        try {
            File file = new File(path);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                if (line.contains(searchValue)) {
                    line = line.replace(searchValue, newValue);
                    // This is to fix windows tests, which are failing because '\' is identifies as escape character.
                    // As RegEx and String both considers '\' as an escape character, we have to escape them both.
                    line = line.replaceAll("\\\\", "\\\\\\\\");
                }
                lines.add(line);
            }
            fr.close();
            br.close();

            FileWriter fw = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fw);
            for (String s : lines) {
                out.write(s);
                out.newLine();
            }
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Properties getClientProperties() {
        Properties properties = new Properties();
        properties.setProperty("security.protocol", "SSL");
        properties.setProperty("ssl.truststore.location",
                               keystoreAndTruststore + File.separator + "kafka.client.truststore.jks");
        properties.setProperty("ssl.truststore.password", "test1234");
        properties.setProperty("ssl.keystore.location",
                               keystoreAndTruststore + File.separator + "kafka.client.keystore.jks");
        properties.setProperty("ssl.keystore.password", "test1234");
        properties.setProperty("ssl.key.password", "test1234");
        return properties;
    }

    private static Properties getKafkaBrokerProperties() {
        Properties properties = new Properties();
        properties.put("listeners", "SSL://localhost:14122");
        properties.put("security.inter.broker.protocol", "SSL");
        properties.put("ssl.client.auth", "required");
        properties.put("ssl.keystore.location",
                       keystoreAndTruststore + File.separator + "kafka.server.keystore.jks");
        properties.put("ssl.keystore.password", "test1234");
        properties.put("ssl.key.password", "test1234");
        properties.put("ssl.truststore.location",
                       keystoreAndTruststore + File.separator + "kafka.server.truststore.jks");
        properties.put("ssl.truststore.password", "test1234");
        properties.put("zookeeper.session.timeout.ms", "30000");
        properties.put(KafkaConfig.ZkConnectionTimeoutMsProp(), ZOOKEEPER_CONNECTION_TIMEOUT_CONFIG);

        return properties;
    }

    @AfterTest(alwaysRun = true)
    public void tearDown() {
        // Reverting the keystore and trust-store file paths
        setFilePath(Paths.get(balFilePath).toAbsolutePath().toString(),
                    Paths.get(keystoreAndTruststore).toAbsolutePath().toString(), filePath);
        finishTest(kafkaCluster, dataDir);
    }
}
