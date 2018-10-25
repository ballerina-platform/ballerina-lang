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

package org.ballerinalang.test.streaming.inline.streams;

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
 * This contains methods to test filter behaviour when external function used for condition in Ballerina Streaming V2
 * and streams are defined inline.
 *
 * @since 0.983.0
 */
public class BallerinaStreamsV2FilterWithFunctionTest {

    private CompileResult result;
    private CompileResult result2;
    private CompileResult resultWithAlias;
    private CompileResult result2WithAlias;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.
                compile("test-src/streaming/inline.streams/streamingv2-filter-with-function-test.bal");
        result2 = BCompileUtil.
                compile("test-src/streaming/inline.streams/streamingv2-filter-with-function-test2.bal");
        resultWithAlias = BCompileUtil.
                compile("test-src/streaming/inline.streams/alias/streamingv2-filter-with-function-test.bal");
        result2WithAlias = BCompileUtil.
                compile("test-src/streaming/inline.streams/streamingv2-filter-with-function-test2.bal");
    }

    @Test(description = "Test filter streaming query")
    public void testFilterQuery() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startFilterQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 45);
        Assert.assertEquals(employee1.get("name").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 50);
    }

    @Test(description = "Test filter streaming query with functions in filter clause")
    public void testFilterQuery2() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result2, "startFilterQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 45);
        Assert.assertEquals(employee1.get("name").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 50);
    }

    @Test(description = "Test filter streaming query with alias")
    public void testFilterQueryWithAlias() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultWithAlias, "startFilterQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 45);
        Assert.assertEquals(employee1.get("name").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 50);
    }

    @Test(description = "Test filter streaming query with functions in filter clause & stream alias")
    public void testFilterQuery2WithAlias() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result2WithAlias, "startFilterQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 45);
        Assert.assertEquals(employee1.get("name").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 50);
    }
}
