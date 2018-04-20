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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.metrics;

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

import java.util.Map;

/**
 * Tests for summary metric.
 */
public class SummaryTest extends MetricTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/metrics/summary-test.bal");
    }

    @Test
    public void testSummary() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCountSummary");
        Assert.assertEquals(returns[0], new BInteger(6));
    }

    @Test
    public void testMaxSummary() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMaxSummary");
        Assert.assertEquals(returns[0], new BFloat(3));
    }

    @Test
    public void testMeanSummary() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMeanSummary");
        Assert.assertEquals(returns[0], new BFloat(2));
    }

    @Test
    public void testPercentileSummary() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPercentileSummary");
        BMap<String, BFloat> bMap = (BMap) returns[0];
        Assert.assertEquals(bMap.size(), 5);
        Map<String, BFloat> map = bMap.getMap();
        map.forEach((percentile, value) -> {
            Double p = Double.parseDouble(percentile);
            Assert.assertTrue(p >= 0 && p <= 1);
            Assert.assertTrue(value.floatValue() > 0);
        });
    }

    @Test
    public void testSummaryWithoutTags() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSummaryWithoutTags");
        Assert.assertEquals(returns[0], new BFloat(3));
    }
}
