/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.observe;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for Counter metric.
 *
 * @since 0.980.0
 */
public class CounterTest extends MetricTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path testResourceRoot = Paths.get(resourceRoot, "test-src");
        compileResult = BCompileUtil.compile(testResourceRoot.resolve("counter_test.bal").toString());
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

    @Test(dependsOnGroups = "RegistryTest.testRegister")
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

    @Test(dependsOnGroups = "RegistryTest.testRegister")
    public void testReset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReset");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }
}
