/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.run;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * This class tests invoking an entry function via the Ballerina Run Command and the data binding functionality.
 *
 * e.g., ballerina run abc:nomoremain 1 "Hello World" data binding main
 *  where nomoremain is the following function
 *      public function nomoremain(int i, string s, string... args) {
 *          ...
 *      }
 */
public class RunFunctionNegativeTestCase {

    private ServerInstance serverInstance;

    private String fileName = "test_entry_function.bal";
    private String filePath = (new File("src/test/resources/run/file/" + fileName)).getAbsolutePath();

    private String sourceArg;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        serverInstance = ServerInstance.initBallerinaServer();
    }

    @Test
    public void testNonPublicMainFunction() throws BallerinaTestException {
        LogLeecher errLogLeecher = new LogLeecher("error: '" + fileName
                                                          + "' does not contain a main function or a service");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{filePath});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testEmptyEntryFunctionName() throws BallerinaTestException {
        sourceArg = filePath + ":";
        LogLeecher errLogLeecher = new LogLeecher("usage error: expected function name after final ':'");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testTooManyArgs() throws BallerinaTestException {
        String functionName = "publicNonMainEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: too many arguments to call entry function '"
                                                   + functionName + "'");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "1", "Hello World", "I'm just extra!"});
        errLogLeecher.waitForText(2000);

    }

    @Test
    public void testInsufficientArgs() throws BallerinaTestException {
        String functionName = "combinedTypeEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: insufficient arguments to call entry function '"
                                                          + functionName + "'");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "1"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testNonPublicFunctionAsEntryFunction() throws BallerinaTestException {
        String functionName = "nonPublicEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("non public function '" + functionName
                                                          + "' not allowed as entry function");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "1"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidIntArg() throws BallerinaTestException {
        String functionName = "combinedTypeEntry";
        String argument = "5ss";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument '" + argument
                                                          + "', expected integer value");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, argument, "a", "b", "c", "d", "e", "f", "g"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidFloatArg() throws BallerinaTestException {
        String functionName = "combinedTypeEntry";
        String argument = "5.5s";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument '" + argument
                                                          + "', expected float value");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "10", argument, "b", "c", "d", "e", "f", "g"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidJsonObjectArg() throws BallerinaTestException {
        String functionName = "jsonEntry";
        String argument = "{name: \"maryam\"}";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid JSON value: " + argument);
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, argument});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidXmlArg() throws BallerinaTestException {
        String functionName = "combinedTypeEntry";
        String argument = "<book>Harry Potter";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid XML value: " + argument);
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "10", "1.0", "string!", "255", "true", "1", argument, "g"});
        errLogLeecher.waitForText(2000);
    }

    @Test (dataProvider = "byteValues")
    public void testInvalidByteArg(String arg) throws BallerinaTestException {
        String functionName = "combinedTypeEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument '" + arg
                                                          + "', expected byte value");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "--", "10", "1.0", "string!", arg, "a", "b", "c", "d"});
        errLogLeecher.waitForText(2000);
    }


    @Test
    public void testInvalidBooleanArg() throws BallerinaTestException {
        String functionName = "combinedTypeEntry";
        String argument = "truer";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument '" + argument
                                                          + "', expected boolean value");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "10", "1.0", "string!", "255", argument, "a", "b", "c"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidUnionArg() throws BallerinaTestException {
        String functionName = "arrayUnionEntry";
        String argument = "true false true";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: incompatible argument '" + argument + "' specified"
                                                        + " for union type: int[]|float[]|boolean[]|json[]|string[]");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, argument});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidArgsWithDefaultableParams() throws BallerinaTestException {
        String functionName = "defaultableParamEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: too many arguments to call entry function '"
                                                          + functionName + "'");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "1", "true", "hi", "not"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidIntArrayArg() throws BallerinaTestException {
        String functionName = "oneDimensionalArrayEntry";
        String argument = "[true, \"hi\", 5]";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument '" + argument
                                                          + "', expected array elements of type: int");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, argument, "a", "b", "c", "d", "e"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidStringArrayArg() throws BallerinaTestException {
        String functionName = "oneDimensionalArrayEntry";
        String argument = "[true, hi, 5]";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument '" + argument
                                                          + "', expected array elements of type: string");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "[1, 2]", argument, "b", "c", "d", "e"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidFloatArrayArg() throws BallerinaTestException {
        String functionName = "oneDimensionalArrayEntry";
        String argument = "[5, 1, hi.there]";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument '" + argument
                                                          + "', expected array elements of type: float");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "[1, 2]", "[\"hello\", \"world\"]", argument, "c", "d", "e"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidBooleanArrayArg() throws BallerinaTestException {
        String functionName = "oneDimensionalArrayEntry";
        String argument = "[0, 1, tru]";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument '" + argument
                                                          + "', expected array elements of type: boolean");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "[1, 2]", "[\"hello\", \"world\"]", "[1.0, 5.2]", argument, "d",
                "e"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidRecordArrayArg() throws BallerinaTestException {
        String functionName = "oneDimensionalArrayEntry";
        String argument = "[{name: \"maryam\"}, {names: \"maryam ziyad\"}]";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument '" + argument
                                                          + "', expected array elements of type: Employee");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "[1, 2]", "[\"hello\", \"world\"]", "[1.0, 5.2]",
                "[true, false]", "[\"hi\", 5, 2.0]", argument});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidIntMapArg() throws BallerinaTestException {
        String functionName = "mapEntry";
        String argument = "{\"test\":\"Ballerina\", \"string\":\"str\"}";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument '" + argument
                                                          + "', expected map argument of element type: int");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, argument, "a", "b", "c", "d", "e"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidFloatMapArg() throws BallerinaTestException {
        String functionName = "mapEntry";
        String argument = "{\"int\":\"hi\", \"test\":\"there\"}";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument '" + argument
                                                          + "', expected map argument of element type: float");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "{\"int\":10, \"test\":120}",
                                                "{\"int\":\"10\", \"test\":\"120\"}", argument, "c", "d", "e"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidBooleanMapArg() throws BallerinaTestException {
        String functionName = "mapEntry";
        String argument = "{\"int\":\"true\", \"test\":\"false\"}";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument '" + argument
                                                          + "', expected map argument of element type: boolean");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "{\"int\":10, \"test\":120}",
                "{\"int\":\"10\", \"test\":\"120\"}", "{\"float\":1.0, \"test\":12.0}", argument, "d", "e"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidRecordMapArg() throws BallerinaTestException {
        String functionName = "mapEntry";
        String argument = "[{name: \"maryam\"}, {names: \"maryam ziyad\"}]";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument '" + argument
                                                          + "', expected map argument of element type: Employee");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "{\"int\":10, \"test\":120}",
                "{\"int\":\"10\", \"test\":\"120\"}", "{\"float\":1.0, \"test\":12.0}",
                "{\"boolean\":true, \"test\":true}", "{\"json\":1, \"test\":\"12.0\"}", argument});
        errLogLeecher.waitForText(2000);
    }

    @Test (dataProvider = "typedescValues")
    public void testInvalidTypedescArg(String arg) throws BallerinaTestException {
        String functionName = "typedescEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: unsupported/unknown typedesc expected with entry "
                                                          + "function '" + arg + "'");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, arg});
        errLogLeecher.waitForText(2000);
    }

    @Test (dataProvider = "sensitiveParamedFunction")
    public void testSensitiveParamedFunction(String functionName) throws BallerinaTestException {
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher(
                "usage error: function with sensitive parameters cannot be invoked as the entry function");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg});
        errLogLeecher.waitForText(2000);
    }

    @AfterMethod
    public void stopServer() throws BallerinaTestException {
        serverInstance.stopServer();
    }

    @AfterClass
    public void tearDown() throws BallerinaTestException {
        serverInstance.cleanup();
    }

    @DataProvider(name = "byteValues")
    public Object[][] byteValues() {
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
}
