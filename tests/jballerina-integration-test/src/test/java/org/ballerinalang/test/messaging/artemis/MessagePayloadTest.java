/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.messaging.artemis;

import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.test.messaging.artemis.ArtemisTestUtils.testSend;

/**
 * Includes tests for different payload types for ANYCAST and MULTICAST queues.
 */
@Test(groups = {"artemis-test"})
public class MessagePayloadTest extends ArtemisTestCommons {
    private CompileResult anyCastResult;
    private CompileResult multiCastResult;
    private static final String MULTICAST_MSG = " multicast ";
    private Path sourcePath;

    @BeforeClass
    public void setup() throws URISyntaxException {
        TestUtils.prepareBalo(this);
        sourcePath = Paths.get("src", "test", "resources", "messaging", "artemis", "producers");
        anyCastResult = BCompileUtil.compile(sourcePath.resolve("anycast_message.bal").toAbsolutePath().toString());
        multiCastResult = BCompileUtil.compile(sourcePath.resolve("multicast_message.bal").toAbsolutePath().toString());
    }

    @Test(description = "Tests the sending of a string message to a queue")
    public void testSendString() {
        String log = "string message Hello World";
        String functionName = "testSendString";
        testSend(anyCastResult, log, functionName, serverInstance);
        testSend(multiCastResult, log + MULTICAST_MSG, functionName, serverInstance);

    }

    @Test(description = "Tests the sending of a byte[] message to a queue", dependsOnMethods = "testSendString")
    public void testSendByteArray() {
        String log = "byte[] message [1, 2, 2, 3, 3, 2]";
        String functionName = "testSendByteArray";
        testSend(anyCastResult, log, functionName, serverInstance);
        testSend(multiCastResult, log + MULTICAST_MSG, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a map<string> message to a queue", dependsOnMethods = "testSendByteArray")
    public void testSendMapString() {
        String log = "map<string> message {\"name\":\"Riyafa\", \"hello\":\"world\"}";
        String functionName = "testSendMapString";
        testSend(anyCastResult, log, functionName, serverInstance);
        testSend(multiCastResult, log + MULTICAST_MSG, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a map<int> message to a queue", dependsOnMethods = "testSendMapString")
    public void testSendMapInt() {
        String log = "map<int> message {\"num\":1, \"num2\":2}";
        String functionName = "testSendMapInt";
        testSend(anyCastResult, log, functionName, serverInstance);
        testSend(multiCastResult, log + MULTICAST_MSG, functionName, serverInstance);
    }


    @Test(description = "Tests the sending of a map<float> message to a queue", dependsOnMethods = "testSendMapInt")
    public void testSendMapFloat() {
        String log = "map<float> message {\"numf1\":1.1, \"numf2\":1.2}";
        String functionName = "testSendMapFloat";
        testSend(anyCastResult, log, functionName, serverInstance);
        testSend(multiCastResult, log + MULTICAST_MSG, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a map<byte> message to a queue", dependsOnMethods = "testSendMapFloat")
    public void testSendMapByte() {
        String log = "map<byte> message {\"byte1\":1, \"byte2\":7}";
        String functionName = "testSendMapByte";
        testSend(anyCastResult, log, functionName, serverInstance);
        testSend(multiCastResult, log + MULTICAST_MSG, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a map<boolean> message to a queue", dependsOnMethods = "testSendMapByte")
    public void testSendMapBoolean() {
        String log = "map<boolean> message {\"first\":true, \"second\":false}";
        String functionName = "testSendMapBoolean";
        testSend(anyCastResult, log, functionName, serverInstance);
        testSend(multiCastResult, log + MULTICAST_MSG, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a map<byte[]> message to a queue",
            dependsOnMethods = "testSendMapBoolean")
    public void testSendMapByteArray() {
        String log = "map<byte[]> message {\"array2\":[5], \"array1\":[1, 2, 3]}";
        String functionName = "testSendMapByteArray";
        testSend(anyCastResult, log, functionName, serverInstance);
        testSend(multiCastResult, log + MULTICAST_MSG, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of different types of properties with a message to a queue",
            dependsOnMethods = "testSendMapByteArray")
    public void testSendProperties() {
        String log =
                "string property Hello again!, int property 123, float property 1.111, boolean property true, byte " +
                        "property 1, byteArray property [1, 1, 0, 0] message Properties' test";
        String functionName = "testSendProperties";
        testSend(anyCastResult, log, functionName, serverInstance);
        testSend(multiCastResult, log + MULTICAST_MSG, functionName, serverInstance);
    }

    @Test(description = "Tests the closing of the producer, session and connection",
            dependsOnMethods = "testSendProperties")
    public void testClose() {
        String functionName = "testClose";
        CompileResult result = BCompileUtil.compile(sourcePath.resolve("close.bal").toAbsolutePath().toString());
        BValue[] val = ((BValueArray) BRunUtil.invokeFunction(result, functionName)[0]).getValues();
        Assert.assertTrue(((ClientProducer) ((BMap) val[0]).getNativeData("artemis-producer")).isClosed());
        Assert.assertTrue(((ClientSession) ((BMap) val[1]).getNativeData("artemis-session")).isClosed());
        Assert.assertTrue(((ServerLocator) ((BMap) val[2]).getNativeData("artemis-connection-pool")).isClosed());
    }
}
