/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.test.experimental;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for using experimental features without enabling them.
 *
 * @since 2201.13.0
 */
public class ExperimentalFeaturesTest {

    @Test
    public void testExperimentalFeaturesNegative() {
        CompileResult result = BCompileUtil.compileWithoutInitInvocation(
                "test-src/experimental-option/experimental_features_negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "using the experimental 'natural expression' feature, " +
                "use the '--experimental' option to enable experimental features", 17, 15);
        BAssertUtil.validateError(result, i++, "using the experimental 'natural expression' feature, " +
                "use the '--experimental' option to enable experimental features", 22, 15);
        BAssertUtil.validateError(result, i++, "using the experimental 'code generation' feature, " +
                "use the '--experimental' option to enable experimental features", 27, 47);
        Assert.assertEquals(result.getDiagnostics().length, i);
    }
}
