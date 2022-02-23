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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.StringUtils.fromString;
import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests related to the module init function.
 *
 * @since 1.0
 */
public class InitFunctionTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testMainFunctionWithUserDefinedInit() {
        CompileResult compileResult = BCompileUtil.compile("test-src/functions/test_main_with_init_function.bal");
        Object value = BRunUtil.invokeAndGetJVMResult(compileResult, "main");

        assertEquals(((BValue) value).getType().getTag(), TypeTags.ERROR_TAG);

        BError error = (BError) value;
        assertEquals(error.getErrorMessage().getValue(), "errorCode");

        BMap<BString, BValue> details = (BMap<BString, BValue>) error.getDetails();
        assertEquals(details.get(fromString("i")), 24L);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMainFunctionWithImportsWithUserDefinedInit() {
        CompileResult compileResult = BCompileUtil.compile("test-src/functions/test_proj_with_init_funcs");
        Object value = BRunUtil.invokeAndGetJVMResult(compileResult, "main");

        assertEquals(((BValue) value).getType().getTag(), TypeTags.ERROR_TAG);

        BError error = (BError) value;
        assertEquals(error.getErrorMessage().getValue(), "errorCode");

        BMap<BString, BValue> details = (BMap<BString, BValue>) error.getDetails();
        assertEquals(details.get(fromString("i")), 110L);
        assertEquals(details.get(fromString("s")), fromString("hello world"));
    }

    @Test
    public void invalidInitFunctionSignatureTest() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/functions/test_init_function_negative.bal");
        assertEquals(negativeResult.getErrorCount(), 3);
        validateError(negativeResult, 0, "the module 'init()' function cannot accept parameters", 17, 1);
        validateError(negativeResult, 1, "the module 'init()' function cannot be public", 17, 1);
        validateError(negativeResult, 2, "invalid module 'init()' function return type 'error', expected a subtype " +
                "of 'error?' containing '()'", 17, 65);
    }

    @Test
    public void testInitFunctionWithNilUnionReturnType() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/functions/test_init_function_nil_union_return.bal");
        assertEquals(negativeResult.getErrorCount(), 0);
        assertEquals(negativeResult.getWarnCount(), 0);
    }
}
