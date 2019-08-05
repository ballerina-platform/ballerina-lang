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

package org.ballerinalang.messaging.kafka.ssl;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.KAFKA_BROKER_PORT;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.ZOOKEEPER_PORT_1;

/**
 * Test cases for ballerina.kafka consumer and producer with SSL.
 */
@Test(singleThreaded = true)
public class KafkaConsumerAndProducerWithSSLTest {

    private CompileResult result;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;
    private static Path resourceDir = Paths.get("src/test/resources").toAbsolutePath();
    private String configFile = "test-src/ssl/kafka_ssl.bal";
    private String message = "Hello World SSL Test";

    //Constants
    private String filePath = "<FILE_PATH>";
    private static String keystoresAndTruststores = "data-files/keystores-truststores";

    @BeforeClass
    public void setup() throws IOException {
        Properties prop = getKafkaBrokerProperties();
        kafkaCluster = kafkaCluster(prop).deleteDataPriorToStartup(true)
                .deleteDataUponShutdown(true).addBrokers(1).startup();
        kafkaCluster.createTopic("test-topic-ssl", 2, 1);
        //Setting the keystore and trust-store file paths
        setFilePath(resourceDir.toString() + "/" + configFile, filePath, resourceDir.toString()
                + "/" + keystoresAndTruststores);
        result = BCompileUtil.compile(configFile);
    }

    @Test(description = "Test SSL producer and consumer")
    public void testKafkaProducerWithSSL() {
        BValue[] args = new BValue[1];
        args[0] = new BString(message);
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] returnBValues = BRunUtil.invoke(result, "funcTestKafkaProduceWithSSL", args);
            Assert.assertEquals(returnBValues.length, 1);
            Assert.assertTrue(returnBValues[0] instanceof BBoolean);
            return ((BBoolean) returnBValues[0]).booleanValue();
        });

        await().atMost(100000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaPollWithSSL");
            Assert.assertEquals(returnBValues.length, 1);
            Assert.assertTrue(returnBValues[0] instanceof BString);
            return returnBValues[0].stringValue().equals(message);
        });
    }

    @Test(description = "Test kafka consumer connect with no SSL config values")
    public void testKafkaConsumerSSLConnectNegative() {
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaSSLConnectNegative");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BError);
        String errorMessage = "Failed to send data to Kafka server: Failed to update metadata after 1000 ms.";
        Assert.assertEquals(((BMap) ((BError) returnBValues[0]).getDetails()).get("message").stringValue(),
                errorMessage);
    }

    @AfterClass
    public void tearDown() {
        //Reverting the keystore and trust-store file paths
        setFilePath(resourceDir.toString() + "/" + configFile, resourceDir.toString()
                + "/" + keystoresAndTruststores, filePath);
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
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-ssl-test");
        kafkaCluster = new KafkaCluster().usingDirectory(dataDir)
                .withPorts(ZOOKEEPER_PORT_1, KAFKA_BROKER_PORT)
                .withKafkaConfiguration(prop);
        return kafkaCluster;
    }

    private static void setFilePath(String path, String searchValue, String newValue) {
        List<String> lines = new ArrayList<String>();
        String line;
        try {
            File file = new File(path);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                if (line.contains(searchValue)) {
                    line = line.replace(searchValue, newValue);
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

    private static Properties getKafkaBrokerProperties() {
        Properties prop = new Properties();
        prop.put("listeners", "SSL://localhost:9094");
        prop.put("security.inter.broker.protocol", "SSL");
        prop.put("ssl.client.auth", "required");
        prop.put("ssl.keystore.location", resourceDir + "/" + keystoresAndTruststores + "/kafka.server.keystore.jks");
        prop.put("ssl.keystore.password", "test1234");
        prop.put("ssl.key.password", "test1234");
        prop.put("ssl.truststore.location", resourceDir + "/" + keystoresAndTruststores
                + "/kafka.server.truststore.jks");
        prop.put("ssl.truststore.password", "test1234");

        return prop;
    }
}
