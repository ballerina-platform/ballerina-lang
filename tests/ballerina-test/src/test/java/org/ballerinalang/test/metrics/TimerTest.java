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
 * Tests for timer metric.
 */
public class TimerTest extends MetricTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/metrics/timer-test.bal");
    }

    @Test
    public void testCountTimer() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCountTimer");
        Assert.assertEquals(returns[0], new BInteger(5));
    }

    @Test
    public void testMaxTimer() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMaxTimer");
        Assert.assertEquals(returns[0], new BFloat(5));
    }

    @Test
    public void testMeanTimer() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMeanTimer");
        Assert.assertEquals(returns[0], new BFloat(3000));
    }

    @Test
    public void testPercentileTimer() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPercentileTimer");
        BMap<BFloat, BFloat> bMap = (BMap) returns[0];
        Assert.assertEquals(bMap.size(), 5);
        Map<BFloat, BFloat> map = bMap.getMap();
        map.forEach((percentile, value) -> {
            Assert.assertTrue(percentile.floatValue() >= 0 && percentile.floatValue() <= 1);
            Assert.assertTrue(value.floatValue() > 0);
        });
    }

    @Test
    public void testTimerWithoutTags() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTimerWithoutTags");
        Assert.assertEquals(returns[0], new BFloat(2500));
    }
}
