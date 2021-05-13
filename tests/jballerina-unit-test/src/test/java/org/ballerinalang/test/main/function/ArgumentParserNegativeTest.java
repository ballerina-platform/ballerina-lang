/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.main.function;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test class for entry function argument parsing negative scenarios.
 *
 * @since 0.982.0
 */
public class ArgumentParserNegativeTest {

    private static final String MAIN_FUNCTION_TEST_SRC_DIR = "test-src/main.function/";

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = "error: all operand arguments are not matched")
    public void testTooManyArgs() {
        CompileResult compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                                                                   + "test_main_with_int_param.bal");
        BRunUtil.runMain(compileResult, new String[]{"1", "Hello World"});
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = "error: missing operand arguments for parameter 'f' of type 'float'")
    public void testInsufficientArgs() {
        CompileResult compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                                                                   + "test_main_with_multiple_typed_params.bal");
        BRunUtil.runMain(compileResult, new String[]{"1"});
    }

    @Test(description = "Test passing undefined parameters to the main function",
          expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = "error: undefined CLI argument: '-j=1'")
    public void testIncorrectArgs() {
        CompileResult compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                                                                   + "test_main_with_int_param.bal");
        BRunUtil.runMain(compileResult, new String[]{"-j=1"});
    }

    @Test(dataProvider = "intValues")
    public void testInvalidIntArg(String arg) {
        try {
            CompileResult compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                                                                       + "test_main_with_int_param.bal");
            BRunUtil.runMain(compileResult, new String[]{arg});
        } catch (RuntimeException e) {
            Assert.assertEquals(e.getMessage(), "error: invalid argument '" + arg +
                    "' for parameter 'i', expected integer value");
        }
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = "error: invalid argument '5.5s' for parameter 'f', expected float value")
    public void testInvalidFloatArg() {
        String argument = "5.5s";
        CompileResult compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                                                                   + "test_main_with_multiple_typed_params.bal");
        BRunUtil.runMain(compileResult, new String[]{"10", argument});
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = "error: all operand arguments are not matched")
    public void testInvalidArgsWithDefaultableParams() {
        CompileResult compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                                                                   + "test_main_with_defaultable_param.bal");
        BRunUtil.runMain(compileResult, new String[]{"1", "true", "hi", "not", "yes"});
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = "error: all operand arguments are not matched")
    public void testInvalidIntArrayArg() {
        String argument = "[true, \"hi\", 5]";
        CompileResult compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                                                                   + "test_main_with_int_array_param.bal");
        BRunUtil.runMain(compileResult, new String[]{argument});
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = "error: all operand arguments are not matched")
    public void testInvalidFloatArrayArg() {
        String argument = "5, 1, hi.there";
        CompileResult compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR
                                                                   + "test_main_with_float_array_param.bal");
        BRunUtil.runMain(compileResult, new String[]{argument});
    }

    @DataProvider(name = "intValues")
    public Object[][] intValues() {
        return new Object[][]{
                {"5ss"},
                {"0c10"},
                {"0b2101"},
                {"0B11015"},
                {"0xkef"},
                {"0XFSF1"}
        };
    }
}
