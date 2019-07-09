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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;

/**
 * Test for using experimental features without enabling them.
 */
public class ExperimentalFeaturesTest {

    @Test
    public void testExperimentalFeaturesNegative() {
        CompileResult result =
                BCompileUtil.compileWithoutExperimentalFeatures("test-src/parser/experimental-features-negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "using experimental feature 'stream'. " +
                "use '--experimental' flag to enable the experimental features", 8, 1);
        BAssertUtil.validateError(result, i++, "using experimental feature 'channel'. " +
                "use '--experimental' flag to enable the experimental features", 9, 1);
        BAssertUtil.validateError(result, i++, "using experimental feature 'streaming queries'. " +
                "use '--experimental' flag to enable the experimental features", 15, 5);
        BAssertUtil.validateError(result, i++, "using experimental feature 'stream'. " +
                "use '--experimental' flag to enable the experimental features", 29, 5);
        BAssertUtil.validateError(result, i++, "using experimental feature 'table queries'. " +
                "use '--experimental' flag to enable the experimental features", 33, 37);
        BAssertUtil.validateError(result, i++, "using experimental feature 'transaction'. " +
                "use '--experimental' flag to enable the experimental features", 43, 5);
    }
}
