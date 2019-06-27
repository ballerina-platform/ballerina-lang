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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.TestUtils;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Includes tests for local transaction scenarios.
 */
@Test(groups = {"artemis-test"})
public class LocalTransactionTest {
    private CompileResult producerResult, consumerResult;

    @BeforeClass
    public void setup() throws URISyntaxException {
        TestUtils.prepareBalo(this);
        Path sourcePath = Paths.get("src", "test", "resources", "messaging", "artemis");
        producerResult = BCompileUtil.compile(sourcePath.resolve("producers").resolve("transaction_producer.bal")
                                                      .toAbsolutePath().toString());
        consumerResult = BCompileUtil.compile(sourcePath.resolve("consumers").resolve("transaction_consumer.bal")
                                                      .toAbsolutePath().toString());
    }

    @Test(description = "Tests the sending of a string message to a queue")
    public void testSimpleSend() {
        //Invoking this function is needed to make sure the queue is created before the sending
        BValue[] consumerVal = BRunUtil.invoke(consumerResult, "createSimpleConsumer");
        BRunUtil.invoke(producerResult, "testSimpleTransactionSend");
        String returnVal = BRunUtil.invoke(consumerResult, "transactionSimpleConsumerReceive", consumerVal)[0]
                .stringValue();
        Assert.assertEquals(returnVal, "Example Example ", "Invalid message received");
    }

    @Test(description = "Tests the sending of a string message to a queue")
    public void testSend() {
        //Invoking this function is needed to make sure the queue is created before the sending
        BValue[] consumerVal = BRunUtil.invoke(consumerResult, "createConsumer");
        BRunUtil.invoke(producerResult, "testTransactionSend");
        String returnVal = BRunUtil.invoke(consumerResult, "transactionConsumerReceive", consumerVal)[0].stringValue();
        Assert.assertEquals(returnVal, "Example Example ", "Invalid message received");
    }

    @Test(description = "Tests transaction erring")
    public void testErringSend() {
        //Invoking this function is needed to make sure the queue is created before the sending
        BValue[] consumerVal = BRunUtil.invoke(consumerResult, "createErringConsumer");
        try {
            BRunUtil.invoke(producerResult, "testErringSend");
        } catch (BLangRuntimeException ex) {
            // Ignore
        }
        String returnVal = BRunUtil.invoke(consumerResult, "receiveAndGetText", consumerVal)[0].stringValue();
        Assert.assertEquals(returnVal, "Example ", "Invalid message received");
    }
}
