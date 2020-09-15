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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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

    @Test(dataProvider = "decimalValues")
    public void testDecimalArg(String specifiedDecimal, String expectedDecimal) {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_decimal_param.bal");
        String output = runMain(compileResult, new String[]{specifiedDecimal});
        Assert.assertEquals(output, expectedDecimal, "string arg parsed as invalid decimal");
    }

    @Test(dataProvider = "jsonValues")
    public void testJsonArg(String arg) {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                                            "test_main_with_json_param.bal");
        String output = runMain(compileResult, new String[]{arg});
        Assert.assertEquals(output, arg, "string arg parsed as invalid JSON");
    }

    @Test
    public void testXmlArg() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR + "test_main_with_xml_param.bal");
        String output = runMain(compileResult, new String[]{"<book status=\"available\" count=\"5\"></book>"});
        Assert.assertTrue(output.contains("<book status=\"available\" count=\"5\"></book>"),
                "string arg parsed as invalid XML");
    }

    @Test(dataProvider = "arrayValues")
    public void testValidArrayArg(String arg, String expectedValue) {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_array_param.bal");
        String output = runMain(compileResult, new String[]{arg});
        Assert.assertEquals(output, expectedValue, "string arg parsed as invalid array");
    }

    @Test
    public void testTupleArg() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_tuple_param.bal");
        String output = runMain(compileResult, new String[]{"[101, {\"name\":\"Maryam\"}, \"finance\"]"});
        Assert.assertEquals(output, "Id: 101, Name: Maryam, Dept: finance",
                            "string arg parsed as invalid tuple");
    }

    @Test
    public void testAllNamedArgs() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_defaultable_param.bal");
        String output = runMain(compileResult, new String[]{"-i=2", "-s=hi", "-b=false", "-s2=is"});
        Assert.assertEquals(output, "2 hi world: is false",
                            "string arg parsed as invalid named arg");
    }

    @Test
    public void testSingleNamedArg() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_defaultable_param.bal");
        String output = runMain(compileResult, new String[]{"1", "false", "-s=hi", "-s2=is"});
        Assert.assertEquals(output, "1 hi world: is false",
                            "string arg parsed as invalid named arg");
    }

    @Test
    public void testNoNamedArg() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_defaultable_param.bal");
        String output = runMain(compileResult, new String[]{"1", "true"});
        Assert.assertEquals(output, "1 default hello world: default true",
                            "string args with no named args parsed as invalid args");
    }

    @Test
    public void testMultipleParam() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_multiple_typed_params.bal");
        String output = runMain(compileResult, new String[]{"1000", "1.0", "Hello Ballerina", "255", "true",
                "{ \"name\": \"Maryam\" }", "<book>Harry Potter</book>",
                "{ \"name\": \"Em\" }", "just", "the", "rest"});
        Assert.assertEquals(output,
                            "integer: 3e8, float: 1.0, string: Hello Ballerina, byte: 255, boolean: true, JSON Name " 
                                    + "Field: Maryam, XML Element Name: book, Employee Name Field: Em, string rest " 
                                    + "args: just the rest ");
    }

    @Test
    public void testOneDimArrParam() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_oneD_array_param.bal");
        String output = runMain(compileResult, new String[]{"[1, 200, 3]", "[\"hello\", \"ballerina\"]",
                "[1.0, 20.4, 30.3]",
                "[true, true, false]", "[5, \"Maryam\", { \"name\": \"Maryam\" }]",
                "[{ \"name\": \"Maryam\" }, { \"name\": \"Em\" }, { \"name\": \"Ziyad\" }]"});
        Assert.assertEquals(output, "integer: 200, string: ballerina, float: 20.4, " +
                                    "boolean: true, json: Maryam, Employee Name Field: Em",
                            "string args parsed as invalid array args");
    }

    @Test
    public void testMapParam() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_map_param.bal");
        String output = runMain(compileResult, new String[]{"{\"int\":10, \"test\":120}",
                "{\"test\":\"Ballerina\", \"string\":\"str\"}",
                "{\"test\":12.0, \"float\":11.1}",
                "{\"boolean\":false, \"test\":true}",
                "{\"test\":5, \"json\":{ \"name\": \"Maryam\" }}",
                "{\"test\": { \"name\": \"Maryam\" }, \"record\": {\"name\":\"Em\"}}"});
        Assert.assertTrue(output.contains(
                            "integer: 120, string: Ballerina, float: 12.0, boolean: true, json: 5, " +
                                    "Test Employee Name Field: Maryam"), "string args parsed as invalid map args");
    }

    @Test(dataProvider = "optionalParamArgAndResult")
    public void testMainWithOptionalParams(String inputValue, String expectedValue) {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_optional_param.bal");
        String output = runMain(compileResult, new String[]{inputValue});
        Assert.assertEquals(output, expectedValue, "evaluated to invalid value");
    }

    @Test
    public void testMainWithOptionalParamsNoneSpecified() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_optional_defaultable_param.bal");
        String output = runMain(compileResult, new String[]{});
        Assert.assertEquals(output, "string value: s is nil m is nil", "evaluated to invalid value");
    }

    @Test(dataProvider = "optionalDefaultableParamOneArgSpecifiedAndResult")
    public void testMainWithOptionalDefaultableParamOneArgSpecified(String inputValue, String expectedValue) {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_optional_defaultable_param.bal");
        String output = runMain(compileResult, new String[]{inputValue});
        Assert.assertEquals(output, expectedValue, "evaluated to invalid value");
    }

    @Test
    public void testMainWithOptionalParamsBothSpecified() {
        compileResult = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR +
                "test_main_with_optional_defaultable_param.bal");
        String output = runMain(compileResult, new String[]{"-s=ballerina", "-m={\"eleven\":11,\"twelve\":12}"});
        Assert.assertEquals(output, "string value: ballerina {\"eleven\":11,\"twelve\":12}",
                            "evaluated to invalid value");
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
                {"0x1efa2", "126882"},
                {"0XFAF1", "64241"}
        };
    }

    @DataProvider(name = "decimalValues")
    public Object[][] decimalValues() {
        return new Object[][]{
                {"10", "10"},
                {"-10.123", "-10.123"},
                {"10.123e1423", "1.0123E+1424"},
                {"0x1ef.a2", "495.63281250"},
                {"-0x1ef.a2p2", "-1982.531250"},
                {"0X1EF.A2P-2", "123.9082031250"}
        };
    }

    @DataProvider(name = "jsonValues")
    public Object[][] jsonValues() {
        return new Object[][]{
                {"10"},
                {"1.0"},
                {"true"},
                {"{\"name\":\"Maryam\"}"}
        };
    }

    @DataProvider(name = "arrayValues")
    public Object[][] arrayValues() {
        return new Object[][] {
                { "[1, 2, 3]", "[1, 2, 3]" },
                { "[1.1, 1.2, 1.3]", "[1.1, 1.2, 1.3]" },
                { "[true, false, true]", "[true, false, true]" },
                { "[{\"name\":\"Maryam\"}, {\"address\":\"Colombo\"}]", 
                  "[{\"name\":\"Maryam\"}, {\"address\":\"Colombo\"}]"
                }
        };
    }

    @DataProvider(name = "optionalParamArgAndResult")
    public Object[][] optionalParamArgAndResult() {
        return new Object[][]{
                {"test string", "string value: test string"},
                {"()", "nil value"}
        };
    }

    @DataProvider(name = "optionalDefaultableParamOneArgSpecifiedAndResult")
    public Object[][] optionalDefaultableParamArgAndResult() {
        return new Object[][]{
                {"-s=hello world", "string value: hello world m is nil"},
                {"-m={\"one\":1,\"two\":2}", "string value: s is nil {\"one\":1,\"two\":2}"}
        };
    }

    private String runMain(CompileResult compileResult, String[] args) {
        try {
            return BCompileUtil.runMain(compileResult, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
