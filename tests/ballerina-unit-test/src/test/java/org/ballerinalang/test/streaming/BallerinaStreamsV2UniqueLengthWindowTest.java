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

package org.ballerinalang.test.streaming;

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
 * This contains methods to test uniqueLengthWindow behaviour in Ballerina Streaming V2.
 *
 * @since 0.982.1
 */
public class BallerinaStreamsV2UniqueLengthWindowTest {

    private CompileResult result1, result2, resultWithAlias;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result1 = BCompileUtil.compile("test-src/streaming/streamingv2-unique-length-window-test.bal");
        result2 = BCompileUtil.compile("test-src/streaming/streamingv2-unique-length-window-test2.bal");
        resultWithAlias = BCompileUtil.
                compile("test-src/streaming/alias/streamingv2-unique-length-window-test.bal");
    }

    @Test(description = "Test uniqueLength window query")
    public void testUniqueLengthQuery1() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result1, "startUniqueLengthwindowTest1");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 1);

        Assert.assertEquals(employee1.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 3);
    }

    @Test(description = "Test uniqueLength window query")
    public void testUniqueLengthQuery2() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result2, "startUniqueLengthwindowTest2");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 6, "Expected events are not received");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 1);

        Assert.assertEquals(employee1.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 2);
    }

    @Test(description = "Test uniqueLength window query with stream alias")
    public void testUniqueLengthQueryWithAlias() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultWithAlias, "startUniqueLengthwindowTest3");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 1);

        Assert.assertEquals(employee1.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 3);
    }
}
