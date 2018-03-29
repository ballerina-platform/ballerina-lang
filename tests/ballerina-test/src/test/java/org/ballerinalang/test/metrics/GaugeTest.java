/*
 *
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.ballerinalang.test.metrics;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Tests for Gauge metric.
 */
public class GaugeTest {
    private CompileResult compileResult;

    @BeforeTest
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/metrics/gauge-test.bal");
    }

    @Test (priority = 1)
    public void testRegisterGauge() {
        BRunUtil.invoke(compileResult, "testRegisterGauge");
    }

    @Test (priority = 2)
    public void testCounterIncrementByOne() {
        BRunUtil.invoke(compileResult, "testIncrementGaugeByOne");
    }

    @Test (priority = 3)
    public void testCounterIncrement() {
        BRunUtil.invoke(compileResult, "testIncrementGauge");
    }

    @Test (priority = 4)
    public void testDecrementGaugeByOne() {
        BRunUtil.invoke(compileResult, "testDecrementGaugeByOne");
    }

    @Test (priority = 5)
    public void testDecrementGauge() {
        BRunUtil.invoke(compileResult, "testDecrementGauge");
    }

    @Test (priority = 6)
    public void testSetGauge() {
        BRunUtil.invoke(compileResult, "testSetGauge");
    }

    @Test (priority = 7)
    public void testGetCounter() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetGauge");
     }
}
