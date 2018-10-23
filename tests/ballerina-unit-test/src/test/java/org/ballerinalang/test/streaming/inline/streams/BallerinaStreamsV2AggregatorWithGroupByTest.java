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
 * This contains methods to test external function call in select clause (with aggregation) in Ballerina Streaming V2
 * while input streams are defined inline.
 *
 * @since 0.983.0
 */
public class BallerinaStreamsV2AggregatorWithGroupByTest {

    private CompileResult result;
    private CompileResult resultWithAlias;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.
                compile("test-src/streaming/inline.streams/streamingv2-aggregate-with-groupby-test.bal");
        resultWithAlias = BCompileUtil.
                compile("test-src/streaming/inline.streams/alias/streamingv2-aggregate-with-groupby-test.bal");
    }

    @Test(description = "Test filter streaming query")
    public void testSelectQuery() {
        BValue[] outputTeacherEvents = BRunUtil.invoke(result, "startAggregationWithGroupByQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputTeacherEvents);
        Assert.assertEquals(outputTeacherEvents.length, 4, "Expected events are not received");

        BMap<String, BValue> teacher0 = (BMap<String, BValue>) outputTeacherEvents[0];
        BMap<String, BValue> teacher1 = (BMap<String, BValue>) outputTeacherEvents[1];
        BMap<String, BValue> teacher2 = (BMap<String, BValue>) outputTeacherEvents[2];
        BMap<String, BValue> teacher3 = (BMap<String, BValue>) outputTeacherEvents[3];


        Assert.assertEquals(teacher0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) teacher0.get("sumAge")).intValue(), 30);
        Assert.assertEquals(((BInteger) teacher0.get("count")).intValue(), 1);

        Assert.assertEquals(teacher1.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) teacher1.get("sumAge")).intValue(), 45);
        Assert.assertEquals(((BInteger) teacher1.get("count")).intValue(), 1);

        Assert.assertEquals(teacher2.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) teacher2.get("sumAge")).intValue(), 90);
        Assert.assertEquals(((BInteger) teacher2.get("count")).intValue(), 2);

        Assert.assertEquals(teacher3.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) teacher3.get("sumAge")).intValue(), 60);
        Assert.assertEquals(((BInteger) teacher3.get("count")).intValue(), 2);
    }

    @Test(description = "Test filter streaming query with stream alias")
    public void testSelectQueryWithAlias() {
        BValue[] outputTeacherEvents = BRunUtil.invoke(resultWithAlias, "startAggregationWithGroupByQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputTeacherEvents);
        Assert.assertEquals(outputTeacherEvents.length, 4, "Expected events are not received");

        BMap<String, BValue> teacher0 = (BMap<String, BValue>) outputTeacherEvents[0];
        BMap<String, BValue> teacher1 = (BMap<String, BValue>) outputTeacherEvents[1];
        BMap<String, BValue> teacher2 = (BMap<String, BValue>) outputTeacherEvents[2];
        BMap<String, BValue> teacher3 = (BMap<String, BValue>) outputTeacherEvents[3];


        Assert.assertEquals(teacher0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) teacher0.get("sumAge")).intValue(), 30);
        Assert.assertEquals(((BInteger) teacher0.get("count")).intValue(), 1);

        Assert.assertEquals(teacher1.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) teacher1.get("sumAge")).intValue(), 45);
        Assert.assertEquals(((BInteger) teacher1.get("count")).intValue(), 1);

        Assert.assertEquals(teacher2.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) teacher2.get("sumAge")).intValue(), 90);
        Assert.assertEquals(((BInteger) teacher2.get("count")).intValue(), 2);

        Assert.assertEquals(teacher3.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) teacher3.get("sumAge")).intValue(), 60);
        Assert.assertEquals(((BInteger) teacher3.get("count")).intValue(), 2);
    }
}
