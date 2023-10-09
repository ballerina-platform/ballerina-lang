/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.cli;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test option CLI arguments.
 */
public class OptionTest {

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: undefined option: 'na'")
    public void testCliOperandWithOptionNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/option_with_operand.bal");
        String[] args = {"--na", "Riyafa", "--", "--Sri Lanka"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test(dataProvider = "cliOptionTestProvider")
    public void testCliOption(String fileName, String[] args) {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/" + fileName + ".bal");
        BRunUtil.runMain(compileResult, args);
    }

    @DataProvider
    public static Object[][] cliOptionTestProvider() {
        return new Object[][]{
                {"option_all", new String[] {"--name", "Riyafa=Riyafa", "--good", "--score", "100", "--height",
                        "5.5", "--energy", "10e99", "--ratings", "5", "--ratings", "3", "--friends", "--friends"}},
                {"option_all", new String[] {"--name=Riyafa=Riyafa", "--good", "--score=100", "--height=5.5",
                        "--energy=10e99", "--ratings=5", "--ratings=3", "--friends", "--friends"}},
                {"option_defaultable_optional", new String[0]},
                {"option_defaultable_optional_with_arg", new String[] {"--name=Riyafa", "--score=100",
                        "--height=5.6"} },
                {"option_with_operand", new String[] {"--name", "Riyafa", "--", "--Sri Lanka"}},
                {"option_with_types", new String[] {"--union", "e-fac", "--name", "Riyafa", "--age", "25", "--score",
                        "99.99", "--value", "1e10"}},
                {"option_with_types", new String[] {"--union=e-fac", "--name=Riyafa", "--age=25", "--score=99.99",
                        "--value=1e10"}},
                {"option_with_nil_union", new String[]{"--union=hello"}},
                {"option_with_array_type", new String[] {"--array=e-fac"}},
                {"option_with_array_type", new String[] {"--array", "e-fac"}},
                {"option_with_array_type", new String[] {"--array=e-fac", "--array=hello"}},
                {"option_with_array_type", new String[] {"--array", "e-fac", "--array", "hello"}},
                {"option_with_boolean_array", new String[] {"--array"}},
                {"option_with_boolean_array", new String[] {"--array", "--array"}},
                {"option_with_boolean", new String[] {"--bool"}},
        };
    }

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: The option 'stringVal' cannot be repeated")
    public void testCliRepeatedOption() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/invalid_repeated_option.bal");
        String[] args = {"--stringVal", "val1", "--stringVal", "val2"};
        BRunUtil.runMain(compileResult, args);
    }
    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: The option 'stringVal' cannot be repeated")
    public void testCliRepeatedNamedOption() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/invalid_repeated_option.bal");
        String[] args = {"--stringVal=val1", "--stringVal=val2"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: The option 'booleanVal' cannot be repeated")
    public void testCliRepeatedBooleanOption() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/invalid_repeated_boolean_option.bal");
        String[] args = {"--booleanVal", "--booleanVal"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: invalid argument 'arr-val' for parameter 'array', expected " +
                    "integer value")
    public void testCliOptionAInvalidArrayType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/option_with_invalid_array.bal");
        String[] args = {"--array", "10", "--array", "arr-val"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: the option 'array' of type 'boolean' is expected without a value")
    public void testCliOptionArgsWithInvalidBooleanArrayTrue() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/option_with_boolean_array.bal");
        String[] args = {"--array=true"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: the option 'array' of type 'boolean' is expected without a value")
    public void testCliOptionArgsWithInvalidBooleanArrayFalse() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/option_with_boolean_array.bal");
        String[] args = {"--array=false"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: the option 'bool' of type 'boolean' is expected without a value")
    public void testCliInvalidOptionArgWithBoolean() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/option_with_boolean.bal");
        String[] args = {"--bool=false"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: invalid argument 'name' for parameter 'val', expected " +
                    "integer value")
    public void testCliInvalidTypeOptionArgWithInt() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/invalid_option_with_int.bal");
        String[] args = {"--val=name"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: invalid argument 'name' for parameter 'val', expected " +
                    "float value")
    public void testCliInvalidTypeOptionArgWithFloat() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/invalid_option_with_float.bal");
        String[] args = {"--val=name"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: invalid argument 'name' for parameter 'val', expected " +
                    "decimal value")
    public void testCliInvalidTypeOptionArgWithDecimal() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/invalid_option_with_decimal.bal");
        String[] args = {"--val=name"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: invalid argument '99999999.9e9999999999' for parameter 'val'," +
                    " expected decimal value")
    public void testCliInvalidValueOptionArgWithDecimal() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/invalid_option_with_decimal.bal");
        String[] args = {"--val=99999999.9e9999999999"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: undefined CLI argument: '-val'")
    public void testCliInvalidOptionArgWithUndefined() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/invalid_option_with_string.bal");
        String[] args = {"-val"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Missing option argument for '--val'")
    public void testCliInvalidOptionArgWithMissing() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/invalid_option_with_string.bal");
        String[] args = {"--val"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test (expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Missing option argument for '--val'")
    public void testCliInvalidValueOptionArgWithMissingMultiple() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/invalid_option_with_string.bal");
        String[] args = {"--val", "--name"};
        BRunUtil.runMain(compileResult, args);
    }
}
