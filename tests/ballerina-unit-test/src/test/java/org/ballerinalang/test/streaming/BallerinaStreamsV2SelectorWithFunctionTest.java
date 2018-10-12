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
 * This contains methods to test external function call in select clause in Ballerina Streaming V2.
 *
 * @since 0.980.0
 */
public class BallerinaStreamsV2SelectorWithFunctionTest {

    private CompileResult result;
    private CompileResult resultForFunctionArgs;
    private CompileResult resultForFunctionArgs2;
    private CompileResult resultForFunctionArgs3;
    private CompileResult resultWithAlias;
    private CompileResult resultForFunctionArgsWithAlias;
    private CompileResult resultForFunctionArgs2WithAlias;
    private CompileResult resultForFunctionArgs3WithAlias;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/streamingv2-select-with-function-test.bal");
        resultForFunctionArgs = BCompileUtil.
                compile("test-src/streaming/streamingv2-select-with-function-args-test.bal");
        resultForFunctionArgs2 = BCompileUtil.
                compile("test-src/streaming/streamingv2-select-with-function-args-test2.bal");
        resultForFunctionArgs3 = BCompileUtil.
                compile("test-src/streaming/streamingv2-select-with-function-args-test3.bal");
        resultWithAlias = BCompileUtil.
                compile("test-src/streaming/alias/streamingv2-select-with-function-test.bal");
        resultForFunctionArgsWithAlias = BCompileUtil.
                compile("test-src/streaming/alias/streamingv2-select-with-function-args-test.bal");
        resultForFunctionArgs2WithAlias = BCompileUtil.
                compile("test-src/streaming/alias/streamingv2-select-with-function-args-test2.bal");
        resultForFunctionArgs3WithAlias = BCompileUtil.
                compile("test-src/streaming/alias/streamingv2-select-with-function-args-test3.bal");
    }

    @Test(description = "Test selector streaming query with function")
    public void testSelectQuery() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startSelectQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("teacherName").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 20);
        Assert.assertEquals(employee1.get("teacherName").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 20);
        Assert.assertEquals(employee2.get("teacherName").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee2.get("age")).intValue(), 20);
    }

    @Test(description = "Test queries which have functions in select clause")
    public void testSelectQueryWithFunctionArgs() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultForFunctionArgs, "startSelectQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("teacherName").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 25);
        Assert.assertEquals(employee1.get("teacherName").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 25);
        Assert.assertEquals(employee2.get("teacherName").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee2.get("age")).intValue(), 25);
    }

    @Test(description = "Test queries which have functions with args in select clause")
    public void testSelectQueryWithFunctionArgs2() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultForFunctionArgs2, "startSelectQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("teacherName").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 25);
        Assert.assertEquals(employee1.get("teacherName").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 45);
        Assert.assertEquals(employee2.get("teacherName").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee2.get("age")).intValue(), 50);
    }

    @Test(description = "Test queries which have functions with multiple args in select clause")
    public void testSelectQueryWithFunctionArgs3() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultForFunctionArgs3, "startSelectQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("teacherName").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 50);
        Assert.assertEquals(employee1.get("teacherName").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 90);
        Assert.assertEquals(employee2.get("teacherName").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee2.get("age")).intValue(), 100);
    }

    @Test(description = "Test selector streaming query with function with stream alias")
    public void testSelectQueryWithAlias() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultWithAlias, "startSelectQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("teacherName").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 20);
        Assert.assertEquals(employee1.get("teacherName").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 20);
        Assert.assertEquals(employee2.get("teacherName").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee2.get("age")).intValue(), 20);
    }

    @Test(description = "Test queries which have functions in select clause with stream alias")
    public void testSelectQueryWithFunctionArgsWithAlias() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultForFunctionArgsWithAlias, "startSelectQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("teacherName").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 25);
        Assert.assertEquals(employee1.get("teacherName").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 25);
        Assert.assertEquals(employee2.get("teacherName").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee2.get("age")).intValue(), 25);
    }

    @Test(description = "Test queries which have functions with args in select clause with stream alias")
    public void testSelectQueryWithFunctionArgs2WithAlias() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultForFunctionArgs2WithAlias, "startSelectQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("teacherName").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 25);
        Assert.assertEquals(employee1.get("teacherName").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 45);
        Assert.assertEquals(employee2.get("teacherName").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee2.get("age")).intValue(), 50);
    }

    @Test(description = "Test queries which have functions with multiple args in select clause with stream alias")
    public void testSelectQueryWithFunctionArgs3WithAlias() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultForFunctionArgs3WithAlias, "startSelectQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("teacherName").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 50);
        Assert.assertEquals(employee1.get("teacherName").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 90);
        Assert.assertEquals(employee2.get("teacherName").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee2.get("age")).intValue(), 100);
    }
}
