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
package org.ballerinalang.test.streaming.legacy;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test output rate limiting behaviour of Ballerina Streaming.
 *
 * @since 0.965.0
 */
public class OutputRateLimitTest {

    private CompileResult result;
    private CompileResult resultForTimeBasedRateLimiting;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/streaming/legacy/output-rate-limiting-test.bal");
        resultForTimeBasedRateLimiting = BCompileUtil.compile("test-src/streaming/legacy/output-rate-limiting-time-test.bal");
    }

    @Test(description = "Test output rate limiting query")
    public void testOutputRateLimitQuery() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startOutputRateLimitQuery");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Raja");
        Assert.assertEquals(employee1.get("name").stringValue(), "Praveen");
    }

    @Test(description = "Test output rate limiting query based on time")
    public void testOutputRateLimitWithTimeQuery() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultForTimeBasedRateLimiting, "startOutputRateLimitQuery");
        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertEquals(outputEmployeeEvents.length, 1, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        Assert.assertEquals(employee0.get("name").stringValue(), "Raja");
    }
}
