/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.streaming.legacy;

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
 * Streaming sequence testcase.
 */
public class SequenceTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/streaming/legacy/sequence-streaming-test.bal");
    }

    @Test(description = "Test sequence streaming query")
    public void testSequenceQuery1() {
        BValue[] initialAndPeakTemps = BRunUtil.invoke(result, "runSequenceQuery1");

        Assert.assertNotNull(initialAndPeakTemps);

        BMap<String, BValue> initialAndPeakTemp1 = (BMap<String, BValue>) initialAndPeakTemps[0];
        Assert.assertEquals(((BFloat) initialAndPeakTemp1.get("initialTemp")).floatValue(), 20.0);
        Assert.assertEquals(((BFloat) initialAndPeakTemp1.get("peakTemp")).floatValue(), 23.0);

        BMap<String, BValue> initialAndPeakTemp2 = (BMap<String, BValue>) initialAndPeakTemps[1];
        Assert.assertEquals(((BFloat) initialAndPeakTemp2.get("initialTemp")).floatValue(), 21.0);
        Assert.assertEquals(((BFloat) initialAndPeakTemp2.get("peakTemp")).floatValue(), 24.0);
    }
}
