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
 * This contains methods to test orderBy implementation in Ballerina Streaming V2.
 *
 * @since 0.980.0
 */
public class BallerinaStreamsV2OrderByTest {

    private CompileResult result;
    private CompileResult result2;
    private CompileResult resultWithAlias;
    private CompileResult resultWithAlias2;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/streamingv2-orderby-test.bal");
        result2 = BCompileUtil.compile("test-src/streaming/streamingv2-orderby-with-functions-test.bal");
        resultWithAlias = BCompileUtil.compile("test-src/streaming/alias/streamingv2-orderby-test.bal");
        resultWithAlias2 =
                BCompileUtil.compile("test-src/streaming/alias/streamingv2-orderby-with-functions-test.bal");
    }

    @Test(description = "Test OrderBy streaming query")
    public void testOrderByQuery() {
        executeFunction(result);
    }

    @Test(description = "Test OrderBy streaming query with Alias")
    public void testOrderByQueryWithAlias() {
        executeFunction(resultWithAlias);
    }

    @Test(description = "Test orderBy streaming query with functions")
    public void testOrderByQueryWithFunc() {
        executeFunction(result2);
    }
    @Test(description = "Test orderBy streaming query with alias with functions")
    public void testOrderByQueryWithFuncWithAlias() {
        executeFunction(resultWithAlias2);
    }

    private void executeFunction(CompileResult result) {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startOrderByQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 10, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];
        BMap<String, BValue> employee3 = (BMap<String, BValue>) outputEmployeeEvents[3];
        BMap<String, BValue> employee4 = (BMap<String, BValue>) outputEmployeeEvents[4];
        BMap<String, BValue> employee5 = (BMap<String, BValue>) outputEmployeeEvents[5];
        BMap<String, BValue> employee6 = (BMap<String, BValue>) outputEmployeeEvents[6];
        BMap<String, BValue> employee7 = (BMap<String, BValue>) outputEmployeeEvents[7];
        BMap<String, BValue> employee8 = (BMap<String, BValue>) outputEmployeeEvents[8];
        BMap<String, BValue> employee9 = (BMap<String, BValue>) outputEmployeeEvents[9];

        Assert.assertEquals(employee0.get("status").stringValue(), "a");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 12);

        Assert.assertEquals(employee1.get("status").stringValue(), "a");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 6);

        Assert.assertEquals(employee2.get("status").stringValue(), "b");
        Assert.assertEquals(((BInteger) employee2.get("age")).intValue(), 3);

        Assert.assertEquals(employee3.get("status").stringValue(), "d");
        Assert.assertEquals(((BInteger) employee3.get("age")).intValue(), 34);

        Assert.assertEquals(employee4.get("status").stringValue(), "f");
        Assert.assertEquals(((BInteger) employee4.get("age")).intValue(), 56);

        Assert.assertEquals(employee5.get("status").stringValue(), "b");
        Assert.assertEquals(((BInteger) employee5.get("age")).intValue(), 21);

        Assert.assertEquals(employee6.get("status").stringValue(), "c");
        Assert.assertEquals(((BInteger) employee6.get("age")).intValue(), 12);

        Assert.assertEquals(employee7.get("status").stringValue(), "e");
        Assert.assertEquals(((BInteger) employee7.get("age")).intValue(), 13);

        Assert.assertEquals(employee8.get("status").stringValue(), "g");
        Assert.assertEquals(((BInteger) employee8.get("age")).intValue(), 87);

        Assert.assertEquals(employee9.get("status").stringValue(), "g");
        Assert.assertEquals(((BInteger) employee9.get("age")).intValue(), 43);
    }
}
