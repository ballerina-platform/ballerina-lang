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
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test ExternalTimeBatchWindow behaviour in Ballerina Streaming V2.
 *
 * @since 0.981.2
 */
@Test(groups = { "TakesTooMuchTime" })
public class BallerinaStreamsV2ExternalTimeBatchWindowTest {

    private CompileResult result1, result2, result3, result4, result5, result6;

    @BeforeClass
    public void setup() {
        result1 = BCompileUtil.compile("test-src/streamingv2-external-time-batch-window-test.bal");
        result2 = BCompileUtil.compile("test-src/streamingv2-external-time-batch-window-test2.bal");
        result3 = BCompileUtil.compile("test-src/streamingv2-external-time-batch-window-test3.bal");
        result4 = BCompileUtil.compile("test-src/streamingv2-external-time-batch-window-test4.bal");
        result5 = BCompileUtil.compile("test-src/streamingv2-external-time-batch-window-test5.bal");
        result6 = BCompileUtil.compile("test-src/streamingv2-external-time-batch-window-test6.bal");
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery1() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result1, "startExternalTimeBatchwindowTest1");

        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 3);

        Assert.assertEquals(employee1.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 1);
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery2() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result2, "startExternalTimeBatchwindowTest2");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 2);

        Assert.assertEquals(employee1.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 2);
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery3() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result3, "startExternalTimeBatchwindowTest3");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 1, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];

        Assert.assertEquals(employee0.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 4);
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery4() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result4, "startExternalTimeBatchwindowTest4");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 3);

        Assert.assertEquals(employee1.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 1);
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery5() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result5, "startExternalTimeBatchwindowTest5");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 3);

        Assert.assertEquals(employee1.get("name").stringValue(), "Nimal");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 2);
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery6() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result6, "startExternalTimeBatchwindowTest6");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 2);

        Assert.assertEquals(employee1.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 2);
    }
}
