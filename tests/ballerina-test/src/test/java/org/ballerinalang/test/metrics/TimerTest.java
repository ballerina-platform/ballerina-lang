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

import io.micrometer.core.instrument.Timer;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.observe.metrics.Registry;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * TODO: Class level comment.
 */
public class TimerTest {
    private CompileResult compileResult;
    private String timerName = "3rdPartyService";
    private String[] tags = new String[]{"method", "GET"};

    @BeforeTest
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/metrics/timer-test.bal");
    }

    @Test(priority = 1)
    public void testRegister() {
        BRunUtil.invoke(compileResult, "testRegister");
        Timer timer = Registry.getRegistry().timer(timerName, tags);
    }

    @Test (priority = 2)
    public void testRecord() {
        BRunUtil.invoke(compileResult, "testRecord");
    }

    @Test (priority = 3)
    public void testMax() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMax");
        BFloat max = (BFloat) returns[0];
        Assert.assertEquals(max, new BFloat(5000));
    }

    @Test (priority = 4)
    public void testMean() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMean");
        BFloat mean = (BFloat) returns[0];
        Assert.assertEquals(mean, new BFloat(3000));
    }

    @Test (priority = 5)
    public void testPercentile() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPercentile");
        BFloat percentile = (BFloat) returns[0];
    }

    @Test (priority = 6)
    public void testCount() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCount");
        BInteger count = (BInteger) returns[0];
        Assert.assertEquals(count, new BInteger(5));
    }
}
