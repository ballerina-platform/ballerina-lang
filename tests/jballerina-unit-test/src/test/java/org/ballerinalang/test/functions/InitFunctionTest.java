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

import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
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
    public void testMainFunctionWithUserDefinedInit() {
        CompileResult compileResult = BCompileUtil.compile("test-src/functions/test_main_with_init_function.bal");
        BValue[] result = BRunUtil.invoke(compileResult, "main");

        assertEquals(result.length, 1);
        assertEquals(result[0].getType().getTag(), TypeTags.ERROR_TAG);

        BError errorValue = (BError) result[0];

        BValue iValue = ((BMap) errorValue.getDetails()).get("i");
        assertEquals(iValue.getType().getTag(), TypeTags.INT_TAG);
        assertEquals(((BInteger) iValue).intValue(), 24L);
    }

    @Test
    public void testMainFunctionWithImportsWithUserDefinedInit() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/functions/TestProjWithInitFuncs", "a.b");
        BValue[] result = BRunUtil.invoke(compileResult, "main");

        assertEquals(result.length, 1);
        assertEquals(result[0].getType().getTag(), TypeTags.ERROR_TAG);

        BError errorValue = (BError) result[0];

        BValue iValue = ((BMap) errorValue.getDetails()).get("i");
        assertEquals(iValue.getType().getTag(), TypeTags.INT_TAG);
        assertEquals(((BInteger) iValue).intValue(), 110L);

        BValue sValue = ((BMap) errorValue.getDetails()).get("s");
        assertEquals(sValue.getType().getTag(), TypeTags.STRING_TAG);
        assertEquals(sValue.stringValue(), "hello world");
    }

    @Test(groups = { "brokenOnNewParser" })
    public void invalidInitFunctionSignatureTest() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/functions/test_init_function_negative.bal");
        assertEquals(negativeResult.getErrorCount(), 3);
        validateError(negativeResult, 0, "the module 'init()' function cannot accept parameters", 17, 1);
        validateError(negativeResult, 1, "the module 'init()' function cannot be public", 17, 1);
        validateError(negativeResult, 2, "invalid module 'init()' function return type 'error', expected a subtype " +
                "of 'error?' containing '()'", 17, 65);
    }
}
