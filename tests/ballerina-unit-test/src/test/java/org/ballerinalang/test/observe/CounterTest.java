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
package org.ballerinalang.test.observe;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for Counter metric.
 *
 * @since 0.980.0
 */
public class CounterTest extends MetricTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compileAndSetup("test-src/observe/counter_test.bal");
    }

    @Test
    public void testCounterIncrementByOne() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCounterIncrementByOne");
        Assert.assertEquals(returns[0], new BInteger(1));
    }

    @Test
    public void testCounterIncrement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCounterIncrement");
        Assert.assertEquals(returns[0], new BInteger(5));
    }

    @Test
    public void testCounterError() {
        try {
            BRunUtil.invoke(compileResult, "testCounterError");
            Assert.fail("Should not be registered again");
        } catch (BLangRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("is already used for a different type of metric"),
                    "Unexpected Ballerina Error");
        }
    }

    @Test
    public void testCounterWithoutTags() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCounterWithoutTags");
        Assert.assertEquals(returns[0], new BInteger(3));
    }

    @Test
    public void testReset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReset");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }
}
