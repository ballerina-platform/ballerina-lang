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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.parser;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test for using experimental features without enabling them.
 */
public class ExperimentalFeaturesTest {

    //TODO Transaction
    @Test (enabled = false)
    public void testExperimentalFeaturesNegative() {
        CompileResult result = BCompileUtil.compile("test-src/parser/experimental-features-negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "using experimental feature 'transaction'. " +
                "use '--experimental' flag to enable the experimental features", 4, 5);
        Assert.assertEquals(result.getDiagnostics().length, i,
                            "Error count mismatch" + Arrays.toString(result.getDiagnostics()));
    }
}
