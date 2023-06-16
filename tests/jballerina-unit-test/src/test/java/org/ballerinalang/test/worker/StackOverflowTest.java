/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.worker;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Stack overflow tests.
 */
public class StackOverflowTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/stack_overflow_negative.bal");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}StackOverflow \\{\"message\":\"stack " +
                    "overflow\"\\}.*")
    public void recursiveFunction() {
        BRunUtil.invoke(result, "recursiveFunction");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}StackOverflow \\{\"message\":\"stack " +
                    "overflow\"\\}.*")
    public void testStackOverflowInFunction() {
        BRunUtil.invoke(result, "testStackOverflowInFunction");
    }

    // Worker errors are not thrown.
    @Test
    public void stackOverflowInWorker() {
        BRunUtil.invoke(result, "stackOverflowInWorker");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
