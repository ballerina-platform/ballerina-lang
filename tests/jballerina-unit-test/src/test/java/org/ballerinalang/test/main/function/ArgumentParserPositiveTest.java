/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test class for entry function argument parsing positive scenarios.
 *
 * @since 0.990.4
 */
public class ArgumentParserPositiveTest {

    private static final String MAIN_FUNCTION_TEST_SRC_DIR = "test-src/main.function/";

    private CompileResult compileResult;

    @Test(dataProvider = "mainFunctionArgsAndResult")
    public void testMainWithParams(String intString, String floatString, String expectedString) {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_params.bal");
        String output = runMain(compileResult, new String[]{intString, floatString});
        Assert.assertEquals(output, expectedString, "evaluated to invalid value");
    }

    @Test
    public void testMainWithOnlyRestParams() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_function_rest_args.bal");
        String output = runMain(compileResult, new String[]{"restArg"});
        Assert.assertEquals(output, "restArg", "rest arg parsing failed");
    }

    @Test
    public void testMainWithOnlyRestParamsNotProvided() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_rest_args_missing.bal");
        String output = runMain(compileResult, new String[0]);
        Assert.assertEquals(output, "main invoked", "missing rest arg parsing failed");
    }

    @Test
    public void testNoArg() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_no_params.bal");
        String output = runMain(compileResult, new String[]{});
        Assert.assertEquals(output, "1", "evaluated to invalid value");
    }

    @Test(dataProvider = "intValues")
    public void testIntArg(String specifiedInt, String expectedInt) {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR + "test_main_with_int_param.bal");
        String output = runMain(compileResult, new String[]{specifiedInt});
        Assert.assertEquals(output, expectedInt, "string arg parsed as invalid int");
    }

    @Test(dataProvider = "floatValues")
    public void testFloatArg(String specifiedFloat, String expectedFloat) {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_float_param.bal");
        String output = runMain(compileResult, new String[]{specifiedFloat});
        Assert.assertEquals(output, expectedFloat, "string arg parsed as invalid float");
    }

    @Test(dataProvider = "decimalValues")
    public void testDecimalArg(String specifiedDecimal, String expectedDecimal) {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_decimal_param.bal");
        String output = runMain(compileResult, new String[]{specifiedDecimal});
        Assert.assertEquals(output, expectedDecimal, "string arg parsed as invalid decimal");
    }

    @Test(enabled = false)
    public void testNoNamedArg() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_defaultable_param.bal");
        String output = runMain(compileResult, new String[]{"1", "true"});
        Assert.assertEquals(output, "1 true world: default",
                            "string args with no named args parsed as invalid args");
    }

    @Test
    public void testMultipleParam() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                                                     "test_main_with_multiple_typed_params.bal");
        String output = runMain(compileResult, new String[]{"1000", "1.0", "Hello Ballerina", "255", "true",
                "<book>Harry Potter</book>", "just", "the", "rest"});
        Assert.assertEquals(output, "integer: 3e8, float: 1.0, string: Hello Ballerina, string rest args: 255" +
                " true <book>Harry Potter</book> just the rest ");
    }

    @Test
    public void testOneDimArrParam() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                                                     "test_main_with_float_array_param.bal");
        String output = runMain(compileResult, new String[]{"1", "200", "3", "1.0", "20.4", "30.3"});
        Assert.assertEquals(output, "float: [1.0,200.0,3.0,1.0,20.4,30.3]", "string args parsed as invalid array args");
    }

    @Test
    public void testMainWithOptionalParamsNoneSpecified() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                                                     "test_main_with_optional_defaultable_param.bal");
        String output = runMain(compileResult, new String[]{});
        Assert.assertEquals(output, "string value: s is nil", "evaluated to invalid value");
    }

    @Test
    public void testMainWithOptionalDefaultableParamOneArgSpecified() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                                                     "test_main_with_optional_defaultable_param.bal");
        String output = runMain(compileResult, new String[]{"hello world"});
        Assert.assertEquals(output, "string value: hello world", "evaluated to invalid value");
    }

    @Test
    public void testMainWithOptionalDefaultableParamArg() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                                                     "test_main_with_optional_defaultable_param.bal");
        String output = runMain(compileResult, new String[0]);
        Assert.assertEquals(output, "string value: s is nil", "evaluated to invalid value");
    }

    @DataProvider(name = "mainFunctionArgsAndResult")
    public Object[][] mainFunctionArgsAndResult() {
        return new Object[][]{
                {"1", "1.0", "4"},
                {"100", "50.1", "105"}
        };
    }

    @DataProvider(name = "intValues")
    public Object[][] intValues() {
        return new Object[][]{
                {"10", "10"},
                {"-10", "-10"}
        };
    }

    @DataProvider(name = "floatValues")
    public Object[][] floatValues() {
        return new Object[][]{
                {"10.5", "10.5"},
                {"-10", "-10.0"},
                {"-216.538", "-216.538"},
                {"0x1efa2p0", "126882.0"},
                {"-0x1efa2p1", "-253764.0"},
                {"0XFAF1P2", "256964.0"},
                {"-0XFAF1P2", "-256964.0"},
                {"0XFAf1p-25", "0.0019145309925079346"},
                {"-0XFaF1P-25", "-0.0019145309925079346"},
                {"0x1ef.a2p5", "15860.25"},
                {"-0x1ef.a2p5", "-15860.25"},
                {"-0X1EF.A2P-2", "-123.908203125"}
        };
    }

    @DataProvider(name = "decimalValues")
    public Object[][] decimalValues() {
        return new Object[][]{
                {"10", "10"},
                {"-10.123", "-10.123"},
                {"10.123e1423", "1.0123E+1424"},
                {"-10.123e1423", "-1.0123E+1424"}
        };
    }

    private String runMain(CompileResult compileResult, String[] args) {
        try {
            return BRunUtil.runMain(compileResult, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
