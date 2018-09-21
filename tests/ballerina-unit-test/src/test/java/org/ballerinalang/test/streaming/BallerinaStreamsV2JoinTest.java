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
package org.ballerinalang.test.streaming;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test `join` clause keyword in Ballerina Streaming V2.
 *
 * @since 0.980.0
 */
public class BallerinaStreamsV2JoinTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/streamingv2-join-test.bal");
    }

    @Test(description = "Test stream join query.")
    public void testStreamJoinQuery() {
        System.setProperty("enable.siddhiRuntime", "true");
        BValue[] stocksWithPrices = BRunUtil.invoke(result, "startJoinQuery");
        Assert.assertNotNull(stocksWithPrices);
        Assert.assertEquals(stocksWithPrices.length, 2, "Expected events are not received");

        BMap<String, BValue> stock1 = (BMap<String, BValue>) stocksWithPrices[0];
        BMap<String, BValue> stock2 = (BMap<String, BValue>) stocksWithPrices[1];

        Assert.assertEquals(stock1.get("symbol").stringValue(), "WSO2");
        Assert.assertEquals(((BFloat) stock1.get("price")).floatValue(), 55.6);

        Assert.assertEquals(stock2.get("symbol").stringValue(), "WSO2");
        Assert.assertEquals(((BFloat) stock2.get("price")).floatValue(), 58.6);
    }
}
