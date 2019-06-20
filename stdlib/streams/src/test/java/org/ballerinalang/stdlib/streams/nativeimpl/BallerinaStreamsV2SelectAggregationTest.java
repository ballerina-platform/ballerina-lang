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

package org.ballerinalang.stdlib.streams.nativeimpl;

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
 * This contains methods to test aggregator behaviour with group by clause in Ballerina Streaming V2.
 *
 * @since 0.982.0
 */
public class BallerinaStreamsV2SelectAggregationTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/native/streamingv2-native-aggregation-groupby-test.bal");
    }

    @Test(description = "Test streaming query with aggregation.")
    public void testGroupByQuery() {
        BValue[] outputEvents = BRunUtil.invoke(result, "startAggregationGroupByQuery");
        Assert.assertNotNull(outputEvents);

        Assert.assertEquals(outputEvents.length, 10, "Expected events are not received");

        BMap<String, BValue> firstEvent = (BMap<String, BValue>) outputEvents[0];
        BMap<String, BValue> sixthEvent = (BMap<String, BValue>) outputEvents[5];
        BMap<String, BValue> lastEvent = (BMap<String, BValue>) outputEvents[9];

        Assert.assertEquals(firstEvent.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) firstEvent.get("sumAge")).intValue(), 30);
        Assert.assertEquals(((BInteger) firstEvent.get("count")).intValue(), 1);

        Assert.assertEquals(sixthEvent.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) sixthEvent.get("sumAge")).intValue(), 90);
        Assert.assertEquals(((BInteger) sixthEvent.get("count")).intValue(), 3);

        Assert.assertEquals(lastEvent.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) lastEvent.get("sumAge")).intValue(), 150);
        Assert.assertEquals(((BInteger) lastEvent.get("count")).intValue(), 5);
    }
}
