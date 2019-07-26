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
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.util.TestUtils;
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
    private CompileResult erringTransactResult;
    private CompileResult transactConsumerResult;
    private CompileResult transactSimpleConsumerResult;

    @BeforeClass
    public void setup() throws URISyntaxException {
        TestUtils.prepareBalo(this);
        Path sourcePath = Paths.get("src", "test", "resources", "messaging", "artemis", "src");
        erringTransactResult = BCompileUtil.compile(sourcePath.resolve("transaction").resolve("erring_transaction.bal")
                .toAbsolutePath().toString());
        transactConsumerResult = BCompileUtil.compile(sourcePath.resolve("transaction").resolve("consumer.bal")
                .toAbsolutePath().toString());
        transactSimpleConsumerResult = BCompileUtil.compile(sourcePath.resolve("transaction")
                .resolve("simple_consumer.bal").toAbsolutePath().toString());
    }

    @Test(description = "Tests the sending of a string message to a queue")
    public void testSimpleSend() {
        validateTestReturnValue(transactSimpleConsumerResult, "testSimpleTransaction",
                "Example Example ");
    }

    @Test(description = "Tests the sending of a string message to a queue")
    public void testSend() {
        validateTestReturnValue(transactConsumerResult, "testTransaction", "Example Example ");
    }

    @Test(description = "Tests transaction erring")
    public void testErringSend() {
        validateTestReturnValue(erringTransactResult, "testErringSend", "Example ");
    }

    private void validateTestReturnValue(CompileResult compileResult, String functionName, String expected) {
        String returnVal = BRunUtil.invoke(compileResult, functionName)[0]
                .stringValue();
        Assert.assertEquals(returnVal, expected, "Invalid message received");
    }
}
