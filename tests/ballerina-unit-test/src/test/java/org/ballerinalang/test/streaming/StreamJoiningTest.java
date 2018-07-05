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
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test stream join behaviour of Ballerina Streaming.
 *
 * @since 0.965.0
 */
public class StreamJoiningTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/streaming/join-streaming-test.bal");
        resultNegative = BCompileUtil.compile("test-src/streaming/join-streaming-negative-test.bal");
    }

    @Test(description = "Test streaming join query.")
    public void testStreamJoinQuery() {
        BValue[] outputStatusCountArray = BRunUtil.invoke(result, "startJoinQuery");

        Assert.assertNotNull(outputStatusCountArray);

        Assert.assertEquals(outputStatusCountArray.length, 2, "Expected events are not received");
    }

    @Test(description = "Test streaming join query with errors")
    public void testJoinNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 3);
        BAssertUtil.validateError(resultNegative, 0,
                "undefined stream name (or alias) 'stockStream' found in select clause",
                51, 9);
        BAssertUtil.validateError(resultNegative, 1,
                "undefined stream name (or alias) 'stockStream' found in select clause",
                51, 9);
        BAssertUtil.validateError(resultNegative, 2,
                "unreachable code",
                57, 5);
    }


}
