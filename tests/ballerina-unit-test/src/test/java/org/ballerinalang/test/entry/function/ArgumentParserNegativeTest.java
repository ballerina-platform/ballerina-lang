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
package org.ballerinalang.test.entry.function;

import org.ballerinalang.launcher.BLauncherException;
import org.ballerinalang.launcher.LauncherUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for entry function argument parsing negative scenarios.
 *
 * @since 0.982.0
 */
public class ArgumentParserNegativeTest {

    private static final String FILE_NAME = "test_entry_function.bal";
    
    private final Map<String, String> runtimeParams = new HashMap<>();
    private final String configFilePath = null;
    private final boolean offline = true;
    private final boolean observe = false;
    private final boolean printReturn = true;

    @Test
    public void testTooManyArgs() {
        String functionName = "intEntry";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath, new String[]{"1", "Hello World"},
                                     offline, observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: too many arguments to call entry function '"
                                                               + functionName + "'"),
                              "invalid error message, usage error for too many arguments not found");
            return;
        }
        Assert.fail("too many arguments not identified");
    }

    @Test
    public void testNonExistingEntryFunction() {
        String functionName = "iDontExist";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath, new String[]{"1", "Hello World"},
                                     offline, observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: '" + functionName + "' function not found in '"
                                                               + FILE_NAME + "'"),
                              "invalid error message, usage error for too many arguments not found");
            return;
        }
        Assert.fail("too many arguments not identified");
    }

    @Test
    public void testInsufficientArgs() {
        String functionName = "combinedTypeEntry";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath, new String[]{"1"}, offline, observe,
                                     printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: insufficient arguments to call entry function '"
                                                               + functionName + "'"),
                              "invalid error message, usage error for insufficient arguments not found");
            return;
        }
        Assert.fail("insufficient arguments not identified");
    }

    @Test (dataProvider = "intValues")
    public void testInvalidIntArg(String arg) {
        String functionName = "intEntry";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath, new String[]{arg}, offline, observe,
                                     printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + arg + "', expected integer "
                                                               + "value"),
                              "invalid error message, usage error for invalid int argument not found");
            return;
        }
        Assert.fail("invalid int argument not identified");
    }

    @Test
    public void testNonPublicFunctionAsEntryFunction() {
        String functionName = "nonPublicEntry";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath, new String[]{"1"}, offline, observe,
                                     printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: non public function '" + functionName
                                                               + "' not allowed as entry function"),
                              "invalid error message, error message for non public entry function not found");
            return;
        }
        Assert.fail("non public entry function not identified");
    }

    @Test
    public void testInvalidFloatArg() {
        String functionName = "combinedTypeEntry";
        String argument = "5.5s";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{"10", argument, "b", "c", "d", "e", "f", "g"}, offline, observe,
                                     printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument
                                                               + "', expected float value"),
                              "invalid error message, error message for invalid float value not found");
            return;
        }
        Assert.fail("invalid float value not identified");
    }

    @Test
    public void testInvalidJsonObjectArg() {
        String functionName = "jsonEntry";
        String argument = "{name: \"maryam\"}";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath, new String[]{argument}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument
                                                               + "', expected JSON value"),
                              "invalid error message, error message for invalid JSON object not found");
            return;
        }
        Assert.fail("invalid JSON object not identified");
    }

    @Test
    public void testInvalidXmlArg() {
        String functionName = "combinedTypeEntry";
        String argument = "<book>Harry Potter";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{"10", "1.0", "string!", "255", "true", "1", argument, "g"}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument
                                                               + "', expected XML value"),
                              "invalid error message, error message for invalid XML value not found");
            return;
        }
        Assert.fail("invalid XML value not identified");
    }

    @Test (dataProvider = "invalidByteValues")
    public void testInvalidByteArg(String arg) {
        String functionName = "combinedTypeEntry";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{"10", "1.0", "string!", arg, "a", "b", "c", "d"}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + arg
                                                               + "', expected byte value"),
                              "invalid error message, error message for invalid byte value not found");
            return;
        }
        Assert.fail("invalid byte value not identified");
    }

    @Test (dataProvider = "invalidIntByteValues")
    public void testInvalidIntByteArg(String arg) {
        String functionName = "combinedTypeEntry";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{"10", "1.0", "string!", arg, "a", "b", "c", "d"}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + arg
                                                               + "', expected byte value, found int"),
                              "invalid error message, error message for int as byte not found");
            return;
        }
        Assert.fail("int as byte not identified");
    }

    @Test
    public void testInvalidBooleanArg() {
        String functionName = "combinedTypeEntry";
        String argument = "truer";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{"10", "1.0", "string!", "255", argument, "a", "b", "c"}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument
                                                               + "', expected boolean value 'true' or 'false'"),
                              "invalid error message, error message for invalid boolean value not found");
            return;
        }
        Assert.fail("invalid boolean value not identified");
    }

    @Test
    public void testInvalidUnionArg() {
        String functionName = "arrayUnionEntry";
        String argument = "true false true";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath, new String[]{argument}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument + "' specified"
                                                               + " for union type: int[]|float[]|boolean[]|json[]"),
                              "invalid error message, error message for invalid value for array union not found");
            return;
        }
        Assert.fail("invalid value for array union not identified");
    }

    @Test
    public void testInvalidStringElementTupleArg() {
        String functionName = "tupleEntry";
        String argument = "(101, {\"name\":\"Maryam\"}, finance)";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath, new String[]{argument}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid tuple element argument 'finance', expected"
                                                               + " argument in the format \\\"str\\\" for tuple element"
                                                               + " of type 'string'"),
                              "invalid error message, error message for invalid tuple string element not found");
            return;
        }
        Assert.fail("invalid tuple string element not identified");
    }

    @Test (dataProvider = "tupleArgsWithoutBrackets")
    public void testInvalidTupleArgWithoutBrackets(String arg) {
        String functionName = "tupleEntry";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath, new String[]{arg}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + arg + "', expected tuple "
                                                               + "notation (\"()\") with tuple arg"),
                              "invalid error message, error message for invalid tuple arg not found");
            return;
        }
        Assert.fail("invalid tuple arg, with no brackets, not identified");
    }

    @Test (dataProvider = "tupleArgsWithIncorrectElementCount")
    public void testInvalidTupleArgWithIncorrectElementCount(String arg) {
        String functionName = "tupleEntry";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath, new String[]{arg}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + arg + "', element count "
                                                               + "mismatch for tuple type: '(int,Employee,string)'"),
                              "invalid error message, error message for tuple element count mismatch not found");
            return;
        }
        Assert.fail("invalid tuple arg, with incorrect element count, not identified");
    }

    @Test
    public void testInvalidTupleArgWithIncorrectElementArg() {
        String functionName = "tupleEntry";
        String arg = "(\"101\", {\"name\":\"Maryam\"}, \"finance\")";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath, new String[]{arg}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid tuple member argument '\"101\"', "
                                                               + "expected value of type 'int'"),
                              "invalid error message, error message for invalid value for tuple element not "
                                      + "found");
            return;
        }
        Assert.fail("invalid tuple arg, with invalid element value, not identified");
    }

    @Test
    public void testInvalidArgsWithDefaultableParams() {
        String functionName = "defaultableParamEntry";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{"1", "true", "hi", "not"}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: too many arguments to call entry function '"
                                                               + functionName + "'"),
                              "invalid error message, error message for too many args with defaultable params not "
                                      + "found");
            return;
        }
        Assert.fail("too many args with defaultable params not identified");
    }

    @Test
    public void testInvalidIntArrayArg() {
        String functionName = "oneDimensionalArrayEntry";
        String argument = "[true, \"hi\", 5]";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{argument, "a", "b", "c", "d", "e"}, offline, observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument
                                                               + "', expected array elements of type: int"),
                              "invalid error message, error message for invalid int array value not found");
            return;
        }
        Assert.fail("invalid int array value not identified");
    }

    @Test
    public void testInvalidStringArrayArg() {
        String functionName = "oneDimensionalArrayEntry";
        String argument = "[true, hi, 5]";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{"[1, 2]", argument, "b", "c", "d", "e"}, offline, observe,
                                     printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument
                                                               + "', expected array elements of type: string"),
                              "invalid error message, error message for invalid string array value not found");
            return;
        }
        Assert.fail("invalid string array value not identified");
    }

    @Test
    public void testInvalidFloatArrayArg() {
        String functionName = "oneDimensionalArrayEntry";
        String argument = "[5, 1, hi.there]";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{"[1, 2]", "[\"hello\", \"world\"]", argument, "c", "d", "e"}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument
                                                               + "', expected array elements of type: float"),
                              "invalid error message, error message for invalid float array value not found");
            return;
        }
        Assert.fail("invalid float array value not identified");
    }

    @Test
    public void testInvalidBooleanArrayArg() {
        String functionName = "oneDimensionalArrayEntry";
        String argument = "[0, 1, tree]";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{"[1, 2]", "[\"hello\", \"world\"]", "[1.0, 5.2]", argument, "d", "e"},
                                     offline, observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument
                                                               + "', expected array elements of type: boolean"),
                              "invalid error message, error message for invalid boolean array value not found");
            return;
        }
        Assert.fail("invalid boolean array value not identified");
    }

    @Test
    public void testInvalidRecordArrayArg() {
        String functionName = "oneDimensionalArrayEntry";
        String argument = "[{name: \"maryam\"}, {names: \"maryam ziyad\"}]";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{"[1, 2]", "[\"hello\", \"world\"]", "[1.0, 5.2]", "[true, false]",
                                             "[\"hi\", 5, 2.0]", argument},
                                     offline, observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument
                                                               + "', expected array elements of type: Employee"),
                              "invalid error message, error message for invalid record array value not found");
            return;
        }
        Assert.fail("invalid record array value not identified");
    }

    @Test
    public void testInvalidIntMapArg() {
        String functionName = "mapEntry";
        String argument = "{\"test\":\"Ballerina\", \"string\":\"str\"}";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{argument, "a", "b", "c", "d", "e"}, offline, observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument
                                                               + "', expected map argument of element type: int"),
                              "invalid error message, error message for invalid int map value not found");
            return;
        }
        Assert.fail("invalid int map value not identified");
    }

    @Test
    public void testInvalidFloatMapArg() {
        String functionName = "mapEntry";
        String argument = "{\"int\":\"hi\", \"test\":\"there\"}";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{"{\"int\":10, \"test\":120}", "{\"int\":\"10\", \"test\":\"120\"}",
                                             argument, "c", "d", "e"},
                                     offline, observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument
                                                               + "', expected map argument of element type: float"),
                              "invalid error message, error message for invalid float map value not found");
            return;
        }
        Assert.fail("invalid float map value not identified");
    }

    @Test
    public void testInvalidBooleanMapArg() {
        String functionName = "mapEntry";
        String argument = "{\"int\":\"true\", \"test\":\"false\"}";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{"{\"int\":10, \"test\":120}", "{\"int\":\"10\", \"test\":\"120\"}",
                                             "{\"float\":1.0, \"test\":12.0}", argument, "d", "e"},
                                     offline, observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument
                                                               + "', expected map argument of element type: boolean"),
                              "invalid error message, error message for invalid boolean map value not found");
            return;
        }
        Assert.fail("invalid boolean map value not identified");
    }

    @Test
    public void testInvalidRecordMapArg() {
        String functionName = "mapEntry";
        String argument = "[{name: \"maryam\"}, {names: \"maryam ziyad\"}]";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath,
                                     new String[]{"{\"int\":10, \"test\":120}", "{\"int\":\"10\", \"test\":\"120\"}",
                                             "{\"float\":1.0, \"test\":12.0}", "{\"boolean\":true, \"test\":true}",
                                             "{\"json\":1, \"test\":\"12.0\"}", argument},
                                     offline, observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + argument
                                                               + "', expected map argument of element type: Employee"),
                              "invalid error message, error message for invalid record map value not found");
            return;
        }
        Assert.fail("invalid record map value not identified");
    }

    @Test (dataProvider = "typedescValues")
    public void testInvalidTypedescArg(String arg) {
        String functionName = "typedescEntry";
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath, new String[]{arg}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: invalid argument '" + arg
                                                               + "', unsupported/unknown typedesc expected with entry"
                                                               + " function"),
                              "invalid error message, error message for invalid typedesc value not found");
            return;
        }
        Assert.fail("invalid typedesc value not identified");
    }

    @Test (dataProvider = "sensitiveParamedFunction")
    public void testSensitiveParamedFunction(String functionName) {
        try {
            LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/entry.function"), Paths.get(FILE_NAME),
                                     functionName, runtimeParams, configFilePath, new String[]{}, offline,
                                     observe, printReturn);
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getMessages().contains("ballerina: function with sensitive parameters cannot be"
                                                               + " invoked as the entry function"),
                              "invalid error message, error message disallowing sensitive paramed function as entry "
                                      + "function not found");
            return;
        }
        Assert.fail("sensitive paramed function not disallowed as entry function");
    }

    @DataProvider(name = "intValues")
    public Object[][] intValues() {
        return new Object[][] {
                { "5ss" },
                { "0c10" },
                { "0b2101" },
                { "0B11015"},
                { "0xkef" },
                { "0XFSF1" }
        };
    }

    @DataProvider(name = "invalidByteValues")
    public Object[][] invalidByteValues() {
        return new Object[][] {
                { "s1w" },
                { "true" }
        };
    }

    @DataProvider(name = "invalidIntByteValues")
    public Object[][] invalidIntByteValues() {
        return new Object[][] {
                { "-1" },
                { "256" }
        };
    }


    @DataProvider(name = "typedescValues")
    public Object[][] typedescValues() {
        return new Object[][] {
                { "strings" },
                { "int[]" },
                { "map<float>" },
                { "Employee" }
        };
    }

    @DataProvider(name = "sensitiveParamedFunction")
    public Object[][] sensitiveParamedFunction() {
        return new Object[][] {
                { "oneSensitiveParamEntry" },
                { "allSensitiveParamsEntry" }
        };
    }

    @DataProvider(name = "tupleArgsWithoutBrackets")
    public Object[][] tupleArgsWithoutBrackets() {
        return new Object[][] {
                { "(1, {\"name\":\"Maryam\"}, \"ABC\"" },
                { "1, {\"name\":\"Maryam\"}, \"ABC\")" },
                { "1, {\"name\":\"Maryam\"}, \"ABC\""  }
        };
    }

    @DataProvider(name = "tupleArgsWithIncorrectElementCount")
    public Object[][] tupleArgsWithIncorrectElementCount() {
        return new Object[][] {
                { "(1, {\"name\":\"Maryam\"})" },
                { "(1, {\"name\":\"Maryam\"}, \"ABC\", 1500)" }
        };
    }
}
