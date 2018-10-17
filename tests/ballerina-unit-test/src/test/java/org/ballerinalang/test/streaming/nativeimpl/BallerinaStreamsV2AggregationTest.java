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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test aggregator behaviour in Ballerina Streaming V2.
 *
 * @since 0.982.0
 */
public class BallerinaStreamsV2AggregationTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/native/streamingv2-aggregation-test.bal");
    }

    @Test(description = "Test streaming query with aggregation.")
    public void testFilterQuery() {
        System.setProperty("enable.siddhiRuntime", "true");
        BValue[] outputEvents = BRunUtil.invoke(result, "startAggregationQuery");
        Assert.assertNotNull(outputEvents);

        Assert.assertEquals(outputEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> firstEvent = (BMap<String, BValue>) outputEvents[0];
        BMap<String, BValue> secondEvent = (BMap<String, BValue>) outputEvents[1];
        BMap<String, BValue> thirdEvent = (BMap<String, BValue>) outputEvents[2];

        Assert.assertEquals(firstEvent.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) firstEvent.get("sumAge")).intValue(), 30);

        Assert.assertEquals(secondEvent.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) secondEvent.get("sumAge")).intValue(), 75);

        Assert.assertEquals(thirdEvent.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) thirdEvent.get("sumAge")).intValue(), 120);
    }
}
