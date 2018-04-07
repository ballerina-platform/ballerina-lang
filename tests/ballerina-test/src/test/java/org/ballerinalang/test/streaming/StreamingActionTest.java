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
package org.ballerinalang.test.streaming;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test streaming action behaviour of Ballerina Streaming.
 *
 * @since 0.970.0
 */
public class StreamingActionTest {

    private CompileResult resultNegativeInvalidType;
    private CompileResult resultNegativeInvalidArgumentCount;

    @BeforeClass
    public void setup() {
        resultNegativeInvalidType = BCompileUtil.compile("test-src/streaming/streaming-action-negative-test-v1.bal");
        resultNegativeInvalidArgumentCount = BCompileUtil.
                compile("test-src/streaming/streaming-action-negative-test-v2.bal");
    }

    @Test(description = "Test streaming action query with errors")
    public void testStreamingActionNegativeType() {
        Assert.assertEquals(resultNegativeInvalidType.getErrorCount(), 2);
        BAssertUtil.validateError(resultNegativeInvalidType, 0,
                "Invalid stream action argument type found. it should be a struct array type argument",
                44, 9);
        BAssertUtil.validateError(resultNegativeInvalidType, 1, "undefined symbol 'emp'",
                45, 37);
    }

    @Test(description = "Test streaming action query with errors")
    public void testStreamingActionNegativeArgumentCount() {
        Assert.assertEquals(resultNegativeInvalidArgumentCount.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegativeInvalidArgumentCount, 0,
                "Invalid number of arguments found for stream action function. " +
                        "found '2' argument but required exactly 1 argument", 44, 9);
    }

}
