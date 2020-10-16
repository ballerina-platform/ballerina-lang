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

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

/**
 * Tests for summary metric.
 *
 * @since 0.980.0
 */
public class SummaryTest extends MetricTest {
    private CompileResult compileResult;
    private DecimalFormat df = new DecimalFormat("#.#");


    @BeforeClass
    public void setup() {
        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path testResourceRoot = Paths.get(resourceRoot, "test-src");
        compileResult = BCompileUtil.compile(testResourceRoot.resolve("summary_test.bal").toString());
    }

    @Test
    public void testMaxSummary() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMaxSummary");
        Assert.assertEquals(round(((BFloat) returns[0]).floatValue()), 3.0);
    }

    @Test
    public void testMeanSummary() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMeanSummary");
        Assert.assertEquals(round(((BFloat) returns[0]).floatValue()), 2.0);
    }

    @Test
    public void testValueSummary() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testValueSummary");
        Assert.assertEquals(round(((BFloat) returns[0]).floatValue()), 500.0);
    }

    @Test
    public void testSummaryWithoutTags() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSummaryWithoutTags");
        Assert.assertEquals(round(((BFloat) returns[0]).floatValue()), 2.0);
    }

    @Test(groups = "SummaryTest.testRegisteredGauge")
    public void testRegisteredGauge() {
        BValue[] returns = null;
        for (int i = 0; i < 3; i++) {
            returns = BRunUtil.invoke(compileResult, "registerAndIncrement");
        }
        Assert.assertEquals(round(((BFloat) returns[0]).floatValue()), 3.0);
    }

    private double round(double value) {
        return Double.parseDouble(df.format(value));
    }
}
