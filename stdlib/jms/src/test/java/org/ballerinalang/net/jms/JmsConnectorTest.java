/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.net.jms;

import org.awaitility.Awaitility;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * JMS connector related tests.
 */
@Test(groups = {"jms-test"})
public class JmsConnectorTest extends BaseTest {

    private Path clientsPath;
    private Path servicesPath;

    @BeforeClass
    public void setup() {
        clientsPath = Paths.get("src", "test", "resources", "test-src", "clients");
        servicesPath = Paths.get("src", "test", "resources", "test-src", "services");
    }

    @Test(description = "Test JMS Connector Queue consumer producer")
    public void testQueueConsumerProducer() {

        sendMessages("jms_queue_producer.bal", "jms_queue_consumer.bal", "Test Text");

    }

    @Test(description = "Test JMS Connector topic subscriber producer")
    public void testTopicSubscriberPublisher() {
        sendMessages("jms_topic_publisher.bal", "jms_topic_subscriber.bal", "Test Text");
    }

    @Test(description = "Test JMS Connector durable topic subscriber producer")
    public void testDurableTopicSubscriberPublisher() {
        sendMessages("jms_durable_topic_publisher.bal", "jms_durable_topic_subscriber.bal", "Test Text");

    }

    @Test(description = "Test JMS Connector simple queue receiver and producer")
    public void testJmsSimpleQueueReceiverProducer() {
        sendMessages("jms_simple_queue_producer.bal", "jms_simple_queue_consumer.bal", "Test Text");
    }

    @Test(description = "Test JMS property setters and getters")
    public void testJmsProperties() {
        String expectedLog = "booleanVal:false|intVal:10|floatVal:10.5|stringVal:TestString|message:Test Text";
        sendMessages("jms_properties_queue_sender.bal", "jms_properties_queue_receiver.bal", expectedLog);

    }

    @Test(description = "Test MB Connector simple topic subscriber and publisher")
    public void testJmsMapMessagePublisherAndSubscriber() {
        sendMessages("jms_map_message_publisher.bal", "jms_map_message_subscriber.bal", "1abctrue1.297 98 99 100 101");
    }

    private void sendMessages(String clientFileName, String serviceFileName, String expected) {
        CompileResult clientResult = BCompileUtil.compile(clientsPath.resolve(clientFileName).toAbsolutePath()
                                                                  .toString());
        CompileResult serviceResult = BCompileUtil.compile(true, servicesPath.resolve(serviceFileName)
                .toAbsolutePath().toString());
        BRunUtil.invoke(clientResult, "sendTextMessage");
        Awaitility.await().atMost(30, SECONDS).until(() -> {
            BValue[] result = BRunUtil.invoke(serviceResult, "getMsgVal");
            return result[0].stringValue().equals(expected);
        });
    }
}
