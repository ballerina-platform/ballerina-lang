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

import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

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
    private ByteArrayOutputStream tempOutStream = new ByteArrayOutputStream();
    private PrintStream defaultOut;

    @BeforeClass
    public void setup() {
        defaultOut = System.out;
    }

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
    public void testNilReturningMain() throws IOException {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR + "test_main_with_nil_return.bal");
        resetTempOut();
        runMain(compileResult, new String[]{});
        String result = tempOutStream.toString();
        assertTrue(result.contains("nil returning main invoked"),
                            "expected the main function to be invoked");
        assertTrue(result.endsWith("nil returning main invoked"), "expected nil to be returned");
    }

    @Test
    public void testErrorReturningMain() throws IOException {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                + "test_main_with_error_return.bal");
        resetTempOut();
        runMain(compileResult, new String[]{});
        String result = tempOutStream.toString();
        assertTrue(result.contains("error returning main invoked"),
                            "expected the main function to be invoked");
        assertTrue(result.contains("error return"), "invalid error reason");
    }

    @Test
    public void testErrorOrNilReturningMainReturningError() throws IOException {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                + "test_main_with_error_or_nil_return.bal");
        resetTempOut();
        runMain(compileResult, new String[]{"error", "1"});
        String result = tempOutStream.toString();
        assertTrue(result.contains("error? returning main invoked"),
                            "expected the main function to be invoked");
        assertTrue(result.contains("generic error"), "invalid error reason");
    }

    @Test
    public void testErrorOrNilReturningMainReturningNil() throws IOException {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                + "test_main_with_error_or_nil_return.bal");
        resetTempOut();
        runMain(compileResult, new String[]{"nil", "0"});
        String result = tempOutStream.toString();
        assertEquals(tempOutStream.toString(), "error? returning main invoked",
                            "expected the main function to be invoked");
        assertTrue(result.endsWith("error? returning main invoked"), "expected nil to be returned");
    }

    @Test
    public void testErrorOrNilReturningMainReturningCustomError() throws IOException {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                + "test_main_with_error_or_nil_return.bal");
        resetTempOut();
        runMain(compileResult, new String[]{"user_def_error", "1"});
        String result = tempOutStream.toString();
        assertTrue(result.startsWith("error? returning main invoked"),
                            "expected the main function to be invoked");
        assertTrue(result.contains("const error reason"), "invalid error reason");
        assertTrue(result.contains("message=error message"), "invalid error message");
    }

    @Test
    public void invalidMainFunctionSignatureTest() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/main.function/test_main_function_negative.bal");
        assertEquals(negativeResult.getErrorCount(), 5);
        validateError(negativeResult, 0, "the main function should be public", 17, 1);
        validateError(negativeResult, 1, "invalid type 'typedesc' as main function parameter, expected anydata",
                      17, 15);
        validateError(negativeResult, 2, "invalid type '(int|typedesc)' as main function parameter, expected anydata",
                      17, 32);
        validateError(negativeResult, 3, "invalid type 'FooObject[]' as main function parameter, expected anydata",
                      17, 57);
        validateError(negativeResult, 4, "invalid main function return type 'string', expected a subtype of 'error?'",
                      17, 81);
    }

    @AfterClass
    public void tearDown() throws IOException {
        tempOutStream.close();
        System.setOut(defaultOut);
    }

    private void resetTempOut() throws IOException {
        tempOutStream.close();
        tempOutStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(tempOutStream));
    }

    private void runMain(CompileResult compileResult, String[] args) {
        try {
            BCompileUtil.runMain(compileResult, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
