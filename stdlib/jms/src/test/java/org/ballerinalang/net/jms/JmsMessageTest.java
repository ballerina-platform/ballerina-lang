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
public class JmsMessageTest extends BaseTest {

    private CompileResult clientResult;
    private CompileResult serviceResult;

    @BeforeClass
    public void setup() {
        Path clientsPath = Paths.get("src", "test", "resources", "test-src", "clients", "jms_message_send.bal");
        Path servicesPath = Paths.get("src", "test", "resources", "test-src", "services", "jms_message_receiver.bal");
        clientResult = BCompileUtil.compile(clientsPath.toAbsolutePath()
                                                    .toString());
        serviceResult = BCompileUtil.compile(true, servicesPath.toAbsolutePath().toString());
    }

    @Test(description = "Test sending xml text message")
    public void testSendXml() {
        sendMessageAndAssertResponse("testSendXml", "received: <book>The Lost World</book>");
    }

    @Test(description = "Test sending json text message")
    public void testSendJson() {
        sendMessageAndAssertResponse("testSendJson", "received: {\"hello\":1.2}");
    }

    @Test(description = "Test sending int as text message")
    public void testSendIntAsText() {
        sendMessageAndAssertResponse("testSendIntAsText", "received: 123");
    }

    @Test(description = "Test sending bytes message")
    public void testSendBytesMessage() {
        sendMessageAndAssertResponse("testSendBytesMessage",
                                     "bytes 1 2 2 3 3 211.22truehelloname=Riyafa hello=123<book>The Lost " +
                                             "World</book>{\"hello\":true}");

    }

    @Test(description = "Test sending map message")
    public void testSendMapMessage() {
        sendMessageAndAssertResponse("testSendMapMessage",
                                     "boolean=true null=null byteArr=1 2 3 4 name=Riyafa hello=1 float=1.2 int=123");
    }

    @Test(description = "Test sending stream message")
    public void testSendStreamMessage() {
        sendMessageAndAssertResponse("testSendStreamMessage",
                                     "stream 1 2 2 3 3 211.22truehelloname=Riyafa hello=123<book>The Lost " +
                                             "World</book>{\"hello\":true}");
    }

    @Test(description = "Test sending message headers")
    public void testSendCustomHeaders() {
        sendMessageAndAssertResponse("testSendCustomHeaders",
                                     "replyTo={} correlationId=123 jmsType=Message");
    }

    @Test(description = "Test sending json text message")
    public void testSendProperties() {
        sendMessageAndAssertResponse("testSendProperties", "boolean JMSXDeliveryCount string xml byte json float " +
                "intHello again!1231.111true1{\"hello\":true}<book>The Lost World</book>");

    }

    private void sendMessageAndAssertResponse(String funcName, String expected) {
        BRunUtil.invoke(clientResult, funcName);
        Awaitility.await().atMost(30, SECONDS).until(() -> {
            BValue[] result = BRunUtil.invoke(serviceResult, "getMsgVal");
            return result[0].stringValue().equals(expected);
        });
    }

}
