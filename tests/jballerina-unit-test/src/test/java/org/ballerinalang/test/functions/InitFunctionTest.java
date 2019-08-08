/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.functions;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests related to the module init function.
 *
 * @since 1.0
 */
public class InitFunctionTest {

    @Test
    public void invalidInitFunctionSignatureTest() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/functions/test_init_function_negative.bal");
        assertEquals(negativeResult.getErrorCount(), 3);
        validateError(negativeResult, 0, "the module '__init()' function cannot be public", 17, 1);
        validateError(negativeResult, 1, "the module '__init()' function cannot accept parameters", 17, 1);
        validateError(negativeResult, 2, "invalid module '__init()' function return type 'error', expected a subtype " +
                "of 'error?' containing '()'", 17, 67);
    }
}
