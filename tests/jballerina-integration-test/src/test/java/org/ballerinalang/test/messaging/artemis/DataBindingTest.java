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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.util.TestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;

import static org.ballerinalang.test.messaging.artemis.ArtemisTestUtils.testSend;

/**
 * Includes tests for different payload types for ANYCAST and MULTICAST queues.
 */
@Test(groups = {"artemis-test"})
public class DataBindingTest extends ArtemisTestCommons {
    private CompileResult anyCastResult;

    @BeforeClass
    public void setup() throws URISyntaxException {
        TestUtils.prepareBalo(this);
        anyCastResult = BCompileUtil.compile(producersPath.resolve("data_binding_message.bal").toAbsolutePath()
                                                     .toString());
    }

    @Test(description = "Tests the sending of a string message to a queue")
    public void testSendString() {
        String log = "string data Hello World";
        String functionName = "testSendString";
        testSend(anyCastResult, log, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a json message to a queue")
    public void testSendJson() {
        String log = "json data {\"name\":\"Riyafa\"}";
        String functionName = "testSendJson";
        testSend(anyCastResult, log, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a xml message to a queue")
    public void testSendXml() {
        String log = "<book>The Lost World</book>";
        String functionName = "testSendXml";
        testSend(anyCastResult, log, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a record message to a queue")
    public void testSendRecord() {
        String log = "person data name=John age=20";
        String functionName = "testSendRecord";
        testSend(anyCastResult, log, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a byte[] message to a queue")
    public void testSendByteArray() {
        String log = "byte[] data 1 2 2 3 3 2";
        String functionName = "testSendByteArray";
        testSend(anyCastResult, log, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a map<string> message to a queue")
    public void testSendMapString() {
        String log = "map<string> data name=Riyafa hello=world";
        String functionName = "testSendMapString";
        testSend(anyCastResult, log, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a map<int> message to a queue")
    public void testSendMapInt() {
        String log = "map<int> data num=1 num2=2";
        String functionName = "testSendMapInt";
        testSend(anyCastResult, log, functionName, serverInstance);
    }


    @Test(description = "Tests the sending of a map<float> message to a queue")
    public void testSendMapFloat() {
        String log = "map<float> data numf1=1.1 numf2=1.2";
        String functionName = "testSendMapFloat";
        testSend(anyCastResult, log, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a map<byte> message to a queue")
    public void testSendMapByte() {
        String log = "map<byte> data byte1=1 byte2=7";
        String functionName = "testSendMapByte";
        testSend(anyCastResult, log, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a map<byte[]> message to a queue")
    public void testSendMapByteArray() {
        String log = "map<byte[]> data array2=5 array1=1 2 3";
        String functionName = "testSendMapByteArray";
        testSend(anyCastResult, log, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a map<boolean> message to a queue")
    public void testSendMapBoolean() {
        String log = "map<boolean> data first=true second=false";
        String functionName = "testSendMapBoolean";
        testSend(anyCastResult, log, functionName, serverInstance);
    }
}
