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
public class RunFunctionPositiveTestCase {

    private static final String PRINT_RETURN = "--printreturn";
    private ServerInstance serverInstance;

    private String filePath = (new File("src/test/resources/run/file/test_entry_function.bal")).getAbsolutePath();

    private String sourceArg;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        serverInstance = ServerInstance.initBallerinaServer();
    }

    @Test
    public void testNoArg() throws BallerinaTestException {
        String functionName = "noParamEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("1");
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{PRINT_RETURN, sourceArg});
        outLogLeecher.waitForText(2000);
    }

    @Test (dataProvider = "intValues")
    public void testIntArg(String specifiedInt, String printedInt) throws BallerinaTestException {
        String functionName = "intEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher(printedInt);
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{PRINT_RETURN, sourceArg, specifiedInt});
        outLogLeecher.waitForText(2000);
    }

    @Test (dataProvider = "jsonValues")
    public void testValidJsonArg(String arg) throws BallerinaTestException {
        String functionName = "jsonEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher(arg);
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{PRINT_RETURN, sourceArg, arg});
        outLogLeecher.waitForText(2000);
    }

    @Test (dataProvider = "typedescValues")
    public void testTypedescArg(String arg) throws BallerinaTestException {
        String functionName = "typedescEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher(arg);
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{PRINT_RETURN, sourceArg, arg});
        outLogLeecher.waitForText(2000);
    }
    
    @Test (dataProvider = "arrayValues")
    public void testValidArrayArg(String arg) throws BallerinaTestException {
        String functionName = "arrayUnionEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher(arg);
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{PRINT_RETURN, sourceArg, arg});
        outLogLeecher.waitForText(2000);
    }

    @Test
    public void testAllNamedArgs() throws BallerinaTestException {
        String functionName = "defaultableParamEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("2 hi world: is false");
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{PRINT_RETURN, sourceArg, "-i=2", "-s=hi", "false", "is"});
        outLogLeecher.waitForText(2000);
    }

    @Test
    public void testSingleNamedArg() throws BallerinaTestException {
        String functionName = "defaultableParamEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("1 hi world: is false");
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{PRINT_RETURN, sourceArg, "-s=hi", "false", "is"});
        outLogLeecher.waitForText(2000);
    }

    @Test
    public void testNoNamedArg() throws BallerinaTestException {
        String functionName = "defaultableParamEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("1 default hello world: is true");
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{PRINT_RETURN, sourceArg, "true", "is"});
        outLogLeecher.waitForText(2000);
    }

    @Test
    public void testMultipleParam() throws BallerinaTestException {
        String functionName = "combinedTypeEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("integer: 1000, float: 1.0, string: Hello Ballerina, byte: 255, "
                                                          + "boolean: true, JSON Name Field: Maryam, XML Element Name: "
                                                          + "book, Employee Name Field: Em, string rest args: just the"
                                                          + " rest ");
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{PRINT_RETURN, sourceArg, "1000", "1.0", "Hello Ballerina", "255", "true",
                                        "{ \"name\": \"Maryam\" }", "<book>Harry Potter</book>", "{ \"name\": \"Em\" }",
                                        "just", "the", "rest"});
        outLogLeecher.waitForText(2000);
    }

    @Test
    public void testOneDimArrParam() throws BallerinaTestException {
        String functionName = "oneDimensionalArrayEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("integer: 200, string: ballerina, float: 20.4, boolean: true, "
                                                          + "json: Maryam, Employee Name Field: Em");
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{PRINT_RETURN, sourceArg, "[1, 200, 3]", "[\"hello\", \"ballerina\"]",
                "[1.0, 20.4, 30.3]", "[true, true, false]", "[5, \"Maryam\", { \"name\": \"Maryam\" }]",
                "[{ \"name\": \"Maryam\" }, { \"name\": \"Em\" }, { \"name\": \"Ziyad\" }]"});
        outLogLeecher.waitForText(2000);
    }

    @Test
    public void testMapParam() throws BallerinaTestException {
        String functionName = "mapEntry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("integer: 120, string: Ballerina, float: 12.0, boolean: true, "
                                                          + "json: 5, Test Employee Name Field: Maryam");
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{PRINT_RETURN, sourceArg, "{\"int\":10, \"test\":120}",
                "{\"test\":\"Ballerina\", \"string\":\"str\"}", "{\"test\":12.0, \"float\":11.1}",
                "{\"boolean\":false, \"test\":true}", "{\"test\":5, \"json\":{ \"name\": \"Maryam\" }}",
                "{\"test\": { \"name\": \"Maryam\" }, \"record\": { \"name\": \"Em\" }}"});
        outLogLeecher.waitForText(2000);
    }

    @AfterMethod
    public void stopServer() throws BallerinaTestException {
        serverInstance.stopServer();
    }

    @AfterClass
    public void tearDown() throws BallerinaTestException {
        serverInstance.cleanup();
    }

    @DataProvider(name = "intValues")
    public Object[][] intValues() {
        return new Object[][] {
                { "10", "10" },
                { "0b1101", "13" },
                { "0B110101", "53" },
                { "0x1efa2", "126882" },
                { "0XFAF1", "64241" }
        };
    }

    @DataProvider(name = "jsonValues")
    public Object[][] jsonValues() {
        return new Object[][] {
                { "10" },
                { "1.0" },
                { "true" },
                { "{\"name\":\"Maryam\"}" }
        };
    }

    @DataProvider(name = "typedescValues")
    public Object[][] typedescValues() {
        return new Object[][] {
                { "string" },
                { "int" },
                { "float" },
                { "byte" },
                { "boolean" },
                { "json" },
                { "xml" },
                { "map" },
                { "future" },
                { "table" },
                { "stream" },
                { "any" },
                { "typedesc" }
        };
    }

    @DataProvider(name = "arrayValues")
    public Object[][] arrayValues() {
        return new Object[][] {
                { "[1, 2, 3]" },
                { "[1.1, 1.2, 1.3]" },
                { "[true, false, true]" },
                { "[{\"name\":\"Maryam\"}, {\"address\":\"Colombo\"}]" }
        };
    }
}
