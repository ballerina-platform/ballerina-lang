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

package org.ballerinalang.test.streaming;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SequenceTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/streaming/sequence-streaming-test.bal");
    }

    @Test(description = "Test sequence streaming query")
    public void testSequenceQuery1() {
        BValue[] initialAndPeakTemps = BRunUtil.invoke(result, "runSequenceQuery1");

        Assert.assertNotNull(initialAndPeakTemps);

        BStruct initialAndPeakTemp1 = (BStruct) initialAndPeakTemps[0];
        Assert.assertEquals(initialAndPeakTemp1.getFloatField(0), 20.0);
        Assert.assertEquals(initialAndPeakTemp1.getFloatField(1), 23.0);

        BStruct initialAndPeakTemp2 = (BStruct) initialAndPeakTemps[1];
        Assert.assertEquals(initialAndPeakTemp2.getFloatField(0), 21.0);
        Assert.assertEquals(initialAndPeakTemp2.getFloatField(1), 24.0);
    }
}
