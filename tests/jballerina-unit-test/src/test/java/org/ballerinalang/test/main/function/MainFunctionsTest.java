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
package org.ballerinalang.test.main.function;

import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests related to the main function.
 *
 * @since 0.990.4
 */

public class MainFunctionsTest {

    private static final String MAIN_FUNCTION_TEST_SRC_DIR = "test-src/main.function/";

    private CompileResult compileResult;

    @Test
    public void basicMainInvocationTest() {
        CompileResult result = BCompileUtil.compile("test-src/main.function/test_basic_main_function.bal");
        assertEquals(result.getErrorCount(), 0);
        BValueArray args = new BValueArray(BTypes.typeString);
        args.add(0, "V1");
        args.add(1, "V2");
        BRunUtil.invoke(result, "main", new BValue[] { args });
    }

    @Test
    public void testNilReturningMain() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR + "test_main_with_nil_return.bal");
        String result = runMain(compileResult, new String[]{});
        assertTrue(result.contains("nil returning main invoked"),
                            "expected the main function to be invoked");
        assertTrue(result.endsWith("nil returning main invoked"), "expected nil to be returned");
    }

    @Test
    public void testErrorOrNilReturningMainReturningError() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                + "test_main_with_error_or_nil_return.bal");
        BCompileUtil.ExitDetails result = BCompileUtil.run(compileResult, new String[]{"error", "1"});
        assertTrue(result.consoleOutput.contains("error? returning main invoked"),
                            "expected the main function to be invoked");
        assertTrue(result.errorOutput.contains("generic error"), "invalid error reason");
    }

    @Test
    public void testErrorOrNilReturningMainReturningNil() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                + "test_main_with_error_or_nil_return.bal");
        String result = runMain(compileResult, new String[]{"nil", "0"});
        assertEquals(result, "error? returning main invoked",
                            "expected the main function to be invoked");
        assertTrue(result.endsWith("error? returning main invoked"), "expected nil to be returned");
    }

    @Test
    public void testErrorOrNilReturningMainReturningCustomError() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                + "test_main_with_error_or_nil_return.bal");
        BCompileUtil.ExitDetails result = BCompileUtil.run(compileResult, new String[]{"user_def_error", "1"});
        assertTrue(result.consoleOutput.startsWith("error? returning main invoked"),
                            "expected the main function to be invoked");
        assertTrue(result.errorOutput.contains("const error reason"), "invalid error reason");
        assertTrue(result.errorOutput.contains("{\"message\":\"error message\""), "invalid error message");
    }

    @Test(groups = { "disableOnOldParser" })
    public void invalidMainFunctionSignatureTest() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/main.function/test_main_function_negative.bal");
        assertEquals(negativeResult.getErrorCount(), 5);
        validateError(negativeResult, 0, "the 'main' function should be public", 17, 1);
        validateError(negativeResult, 1,
                "invalid type 'typedesc' as 'main' function parameter, expected anydata", 17, 15);
        validateError(negativeResult, 2,
                "invalid type '(int|typedesc)' as 'main' function parameter, expected anydata", 17, 32);
        validateError(negativeResult, 3,
                "invalid type 'FooObject[]' as 'main' function parameter, expected anydata", 17, 57);
        validateError(negativeResult, 4, "invalid 'main' function return type 'string', expected a subtype of " +
                              "'error?' containing '()'", 17, 81);
    }

    @Test
    public void testInvalidErrorReturningMain() {
        CompileResult negativeResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                                                                    "test_main_with_error_return_negative.bal");
        assertEquals(negativeResult.getErrorCount(), 1);
        validateError(negativeResult, 0, "invalid 'main' function return type 'error', expected a subtype of " +
                "'error?' containing '()'", 17, 32);
    }

    @Test
    public void testMainWithStackOverflow() {
        CompileResult compileResult = BCompileUtil
                .compile("test-src/main.function/test_main_with_stackoverflow.bal");
        BCompileUtil.ExitDetails details = BCompileUtil.run(compileResult, new String[]{});
        assertTrue(details.errorOutput.contains("error: {ballerina}StackOverflow\n\tat $value$Foo:init" +
                "(test_main_with_stackoverflow.bal:19)\n\t   $value$Foo:init(test_main_with_stackoverflow.bal:19)" +
                "\n\t   $value$Foo:init(test_main_with_stackoverflow.bal:19)"));
    }


    private String runMain(CompileResult compileResult, String[] args) {
        try {
            return BCompileUtil.runMain(compileResult, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
