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
 * This contains methods to test external time window behaviour in Ballerina Streaming V2.
 *
 * @since 0.982.0
 */
public class BallerinaStreamsV2ExternalWindowTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/native/streamingv2-external-window-test.bal");
    }

    @Test(description = "Test streaming query with external time window.")
    public void testFilterQuery() {
        System.setProperty("enable.siddhiRuntime", "true");
        BValue[] outputEvents = BRunUtil.invoke(result, "startExternalTimeWindowQuery");
        Assert.assertNotNull(outputEvents);

        Assert.assertEquals(outputEvents.length, 4, "Expected events are not received");

        BMap<String, BValue> firstEvent = (BMap<String, BValue>) outputEvents[0];
        BMap<String, BValue> secondEvent = (BMap<String, BValue>) outputEvents[1];
        BMap<String, BValue> thirdEvent = (BMap<String, BValue>) outputEvents[2];
        BMap<String, BValue> forthEvent = (BMap<String, BValue>) outputEvents[3];

        Assert.assertEquals(firstEvent.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) firstEvent.get("sumAge")).intValue(), 30);

        Assert.assertEquals(secondEvent.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) secondEvent.get("sumAge")).intValue(), 75);

        Assert.assertEquals(thirdEvent.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) thirdEvent.get("sumAge")).intValue(), 35);

        Assert.assertEquals(forthEvent.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) forthEvent.get("sumAge")).intValue(), 85);
    }
}
