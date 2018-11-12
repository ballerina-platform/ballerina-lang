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
 * This contains methods to test streams and table join in Ballerina Streaming V2.
 *
 * @since 0.982.1
 */
public class BallerinaStreamsV2TableJoinTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/streamingv2-table-join-test.bal");
    }

    @Test(description = "Test stream & table join query.", enabled = false)
    public void testStreamJoinQuery() {
        System.setProperty("enable.siddhiRuntime", "true");
        BValue[] stocksWithPrices = BRunUtil.invoke(result, "startTableJoinQuery");
        Assert.assertNotNull(stocksWithPrices);
        Assert.assertEquals(stocksWithPrices.length, 2, "Expected events are not received");

        BMap<String, BValue> stock1 = (BMap<String, BValue>) stocksWithPrices[0];
        BMap<String, BValue> stock2 = (BMap<String, BValue>) stocksWithPrices[1];

        Assert.assertEquals(stock1.get("symbol").stringValue(), "WSO2");
        Assert.assertEquals(stock1.get("tweet").stringValue(), "Hello WSO2, happy to be a user.");
        Assert.assertEquals(((BFloat) stock1.get("price")).floatValue(), 55.6);
        Assert.assertEquals(stock2.get("symbol").stringValue(), "IBM");
        Assert.assertEquals(stock2.get("tweet").stringValue(), "Hello IBM, happy to be a user.");
        Assert.assertEquals(((BFloat) stock2.get("price")).floatValue(), 58.6);
    }

    @Test(description = "Test stream & table outer join query.", enabled = false)
    public void testStreamOuterJoinQuery() {
        System.setProperty("enable.siddhiRuntime", "true");
        BValue[] stocksWithPrices = BRunUtil.invoke(result, "startTableOuterJoinQuery");
        Assert.assertNotNull(stocksWithPrices);
        Assert.assertEquals(stocksWithPrices.length, 2, "Expected events are not received");

        BMap<String, BValue> stock1 = (BMap<String, BValue>) stocksWithPrices[0];
        BMap<String, BValue> stock2 = (BMap<String, BValue>) stocksWithPrices[1];

        Assert.assertEquals(stock1.get("symbol").stringValue(), "WSO2");
        Assert.assertEquals(stock1.get("tweet").stringValue(), "Hello WSO2, happy to be a user.");
        Assert.assertEquals(((BFloat) stock1.get("price")).floatValue(), 55.6);
        Assert.assertNull(stock2.get("symbol"));
        Assert.assertEquals(stock2.get("tweet").stringValue(), "Hello BMW, happy to be a user.");
        Assert.assertNull(stock2.get("price"));
    }
}
