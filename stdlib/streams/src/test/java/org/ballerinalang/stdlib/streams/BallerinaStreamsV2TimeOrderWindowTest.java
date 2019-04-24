/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test TimeOrder window behaviour in Ballerina Streaming V2.
 *
 * @since 0.990.2
 */
public class BallerinaStreamsV2TimeOrderWindowTest {
    private CompileResult result, result2;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/streamingv2-time-order-window-test.bal");
        result2 = BCompileUtil.compile("test-src/streamingv2-time-order-window-test2.bal");
    }

    @Test(description = "Test timeOrder window query")
    public void testExternalTimeQuery() {
                BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startTimeOrderWindowTest");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 4, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];
        BMap<String, BValue> employee3 = (BMap<String, BValue>) outputEmployeeEvents[3];

        Assert.assertEquals(employee0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee0.get("sumAge")).intValue(), 30);

        Assert.assertEquals(employee1.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee1.get("sumAge")).intValue(), 75);

        Assert.assertEquals(employee2.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee2.get("sumAge")).intValue(), 110);

        Assert.assertEquals(employee3.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee3.get("sumAge")).intValue(), 125);
    }

    @Test(description = "Test timeOrder window query with dropOlderEvents enabled")
    public void testExternalTimeQuery2() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result2, "startTimeOrderWindowTest2");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee0.get("sumAge")).intValue(), 30);

        Assert.assertEquals(employee1.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee1.get("sumAge")).intValue(), 75);

        Assert.assertEquals(employee2.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee2.get("sumAge")).intValue(), 125);
    }
}
