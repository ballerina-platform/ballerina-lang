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
 * This contains methods to test hoppingWindow behaviour in Ballerina Streaming V2.
 *
 * @since 0.990.3
 */
public class BallerinaStreamsV2HoppingWindowTest {
    private CompileResult result1, result2, result3;

    @BeforeClass
    public void setup() {
        result1 = BCompileUtil.compile("test-src/streamingv2-hopping-window-test.bal");
        result2 = BCompileUtil.compile("test-src/streamingv2-hopping-window-test2.bal");
        result3 = BCompileUtil.compile("test-src/streamingv2-hopping-window-test3.bal");
    }

    @Test(description = "Test hopping window query when windowSize > hopeSize")
    public void testHoppingWindowQuery1() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result1, "startHoppingWindowTest");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee3 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee4 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee3.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee3.get("count")).intValue(), 2);

        Assert.assertEquals(employee4.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee4.get("count")).intValue(), 1);

    }

    @Test(description = "Test hopping window query when windowSize << hopeSize")
    public void testHoppingWindowQuery2() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result2, "startHoppingWindowTest2");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 0, "Expected events are not received");
    }

    @Test(description = "Test hopping window query when windowSize < hopeSize")
    public void testHoppingWindowQuery3() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result3, "startHoppingWindowTest3");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 1);

        Assert.assertEquals(employee1.get("name").stringValue(), "Kavindu");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 3);
    }
}
