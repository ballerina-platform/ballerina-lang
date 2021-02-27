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
package org.ballerinalang.observe.metrics.extension.defaultimpl;

import io.ballerina.runtime.observability.metrics.Counter;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test to validate the counter's operations.
 *
 * @since 0.980.0
 */
public class CounterTest {

    private Counter counter;

    @BeforeMethod
    public void init() {
        counter = new DefaultCounter(null);
    }

    @Test
    public void testStartAtZero() {
        Assert.assertEquals(counter.getValue(), 0);
    }

    @Test
    public void testIncrementByOne() {
        counter.increment();
        Assert.assertEquals(counter.getValue(), 1);
    }

    @Test
    public void testIncrement() {
        counter.increment(100);
        Assert.assertEquals(counter.getValue(), 100);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeIncrement() {
        counter.increment(-100);
        Assert.fail("Counter should not allow negative values");
    }
}
