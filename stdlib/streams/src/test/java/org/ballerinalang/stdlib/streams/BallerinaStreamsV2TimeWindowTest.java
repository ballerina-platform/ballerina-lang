/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.streams;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test timeWindow behaviour in Ballerina Streaming V2.
 *
 * @since 0.981.2
 */
public class BallerinaStreamsV2TimeWindowTest {
    private CompileResult result1, result2, resultNegative;

    @BeforeClass
    public void setup() {
        result1 = BCompileUtil.compile("test-src/streamingv2-time-window-test.bal");
        result2 = BCompileUtil.compile("test-src/streamingv2-time-window-test2.bal");
        resultNegative =
                BCompileUtil.compile("test-src/negative/streamingv2-window-negative-test.bal");
    }

    @Test(description = "Test Time window query")
    public void testTimeQuery1() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result1, "startTimeWindowTest");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(employee1.get("name").stringValue(), "Raja");
    }

    @Test(description = "Test Time window query")
    public void testTimeQuery2() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result2, "startTimeWindowTest2");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 6, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 1);

        Assert.assertEquals(employee1.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 2);
    }

    @Test(description = "Checks whether the window function exists or not")
    public void testForWindowFunction() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "undefined function 'undefinedWindow'", 62, 47);
    }
}
