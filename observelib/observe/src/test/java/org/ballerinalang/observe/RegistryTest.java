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
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test to verify the operations for global metrics registry.
 *
 * @since 0.980.0
 */
public class RegistryTest extends MetricTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path testResourceRoot = Paths.get(resourceRoot, "test-src");
        compileResult = BCompileUtil.
                compile(testResourceRoot.resolve("metrics_registry_test.bal").toString());
    }

    @Test(groups = "RegistryTest.testGetAllMetrics", dependsOnGroups = "SummaryTest.testRegisteredGauge")
    public void testGetAllMetrics() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getAllMetricsSize");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1,
                "There shouldn't be any metrics registered in initial state.");
    }

    @Test(groups = "RegistryTest.testRegister", dependsOnMethods = "testGetAllMetrics")
    public void testRegister() {
        BValue[] returns = BRunUtil.invoke(compileResult, "registerAngGetMetrics");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2,
                "One metric should have been registered.");
    }

    @Test(dependsOnMethods = "testRegister")
    public void lookupMetric() {
        BValue[] returns = BRunUtil.invoke(compileResult, "lookupRegisteredMetrics");
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true,
                "Cannot be looked up a registered metric");
    }

    @Test(dependsOnMethods = "lookupMetric")
    public void lookupWithOnlyNameMetric() {
        BValue[] returns = BRunUtil.invoke(compileResult, "lookupRegisteredNameOnlyMetrics");
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false,
                "No metric should be returned for only name without tags");
    }

    @Test(dependsOnMethods = "lookupWithOnlyNameMetric")
    public void lookupMetricAndIncrement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "lookupRegisterAndIncrement");
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true,
                "No metric should be returned for only name without tags");
    }

}
