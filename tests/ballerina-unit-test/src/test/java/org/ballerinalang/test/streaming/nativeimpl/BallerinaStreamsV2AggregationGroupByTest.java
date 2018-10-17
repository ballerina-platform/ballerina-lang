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

package org.ballerinalang.test.streaming.nativeimpl;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test aggregator behaviour in Ballerina Streaming V2.
 *
 * @since 0.980.0
 */
public class BallerinaStreamsV2AggregationGroupByTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/native/streamingv2-aggregation-with-groupby-test.bal");
    }

    @Test(description = "Test streaming query with aggregation and group by.")
    public void testFilterQuery() {
        System.setProperty("enable.siddhiRuntime", "true");
        BValue[] outputEvents = BRunUtil.invoke(result, "startAggregationQuery");
        Assert.assertNotNull(outputEvents);

        Assert.assertEquals(outputEvents.length, 5, "Expected events are not received");

        BMap<String, BValue> anxEvent = (BMap<String, BValue>) outputEvents[1];
        BMap<String, BValue> bmxEvent = (BMap<String, BValue>) outputEvents[3];

        Assert.assertEquals(anxEvent.get("category").stringValue(), "ANX");
        Assert.assertEquals(((BInteger) anxEvent.get("iSum")).intValue(), 6);
        Assert.assertEquals(((BFloat) anxEvent.get("fSum")).floatValue(), 7.0);
        Assert.assertEquals(((BInteger) anxEvent.get("count")).intValue(), 2);
        Assert.assertEquals(((BFloat) anxEvent.get("iAvg")).floatValue(), 3.0);
        Assert.assertEquals(((BFloat) anxEvent.get("fAvg")).floatValue(), 3.5);
        Assert.assertEquals(((BInteger) anxEvent.get("distCount")).intValue(), 2);
        Assert.assertEquals(((BFloat) anxEvent.get("stdDev")).floatValue(), 1.0);
        Assert.assertEquals(((BInteger) anxEvent.get("iMaxForever")).intValue(), 4);
        Assert.assertEquals(((BFloat) anxEvent.get("fMaxForever")).floatValue(), 4.5);
        Assert.assertEquals(((BInteger) anxEvent.get("iMinForever")).intValue(), 2);
        Assert.assertEquals(((BFloat) anxEvent.get("fMinForever")).floatValue(), 2.5);
        Assert.assertEquals(((BInteger) anxEvent.get("iMax")).intValue(), 4);
        Assert.assertEquals(((BFloat) anxEvent.get("fMax")).floatValue(), 4.5);
        Assert.assertEquals(((BInteger) anxEvent.get("iMin")).intValue(), 2);
        Assert.assertEquals(((BFloat) anxEvent.get("fMin")).floatValue(), 2.5);

        Assert.assertEquals(bmxEvent.get("category").stringValue(), "BMX");
        Assert.assertEquals(((BInteger) bmxEvent.get("iSum")).intValue(), 4);
        Assert.assertEquals(((BFloat) bmxEvent.get("fSum")).floatValue(), 5.0);
        Assert.assertEquals(((BInteger) bmxEvent.get("count")).intValue(), 2);
        Assert.assertEquals(((BFloat) bmxEvent.get("iAvg")).floatValue(), 2.0);
        Assert.assertEquals(((BFloat) bmxEvent.get("fAvg")).floatValue(), 2.5);
        Assert.assertEquals(((BInteger) bmxEvent.get("distCount")).intValue(), 2);
        Assert.assertEquals(((BFloat) bmxEvent.get("stdDev")).floatValue(), 1.0);
        Assert.assertEquals(((BInteger) bmxEvent.get("iMaxForever")).intValue(), 3);
        Assert.assertEquals(((BFloat) bmxEvent.get("fMaxForever")).floatValue(), 3.5);
        Assert.assertEquals(((BInteger) bmxEvent.get("iMinForever")).intValue(), 1);
        Assert.assertEquals(((BFloat) bmxEvent.get("fMinForever")).floatValue(), 1.5);
        Assert.assertEquals(((BInteger) bmxEvent.get("iMax")).intValue(), 3);
        Assert.assertEquals(((BFloat) bmxEvent.get("fMax")).floatValue(), 3.5);
        Assert.assertEquals(((BInteger) bmxEvent.get("iMin")).intValue(), 1);
        Assert.assertEquals(((BFloat) bmxEvent.get("fMin")).floatValue(), 1.5);
    }
}
