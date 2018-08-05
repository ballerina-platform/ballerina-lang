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

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
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
 *  TODO: Needs to be refactored to unzip distro just once, once the required changes are merged
 */
public class RunFunctionPositiveTestCase extends IntegrationTestCase {

    private String serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
    private ServerInstance serverInstance;

    private String filePath = (new File("src/test/resources/run/file/test_entry_function.bal")).getAbsolutePath();

    private String sourceArg;

    @BeforeMethod()
    public void setUp() throws BallerinaTestException, IOException {
        serverInstance = new ServerInstance(serverZipPath);
    }

    @Test
    public void testNoArg() throws BallerinaTestException {
        String functionName = "noargentry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("1");
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{sourceArg});
        outLogLeecher.waitForText(2000);
    }

    @Test (dataProvider = "jsonValues")
    public void testValidJsonArg(String arg) throws BallerinaTestException {
        String functionName = "jsonentry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher(arg);
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, arg});
        outLogLeecher.waitForText(2000);
    }

    @Test
    public void testValidStringArrayArg() throws BallerinaTestException {
        String arg = "[\"hello\", \"world\"]";
        String functionName = "stringarrayentry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher(arg);
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, arg});
        outLogLeecher.waitForText(2000);
    }

    @Test (dataProvider = "arrayValues")
    public void testValidArrayArg(String arg) throws BallerinaTestException {
        String functionName = "unionarrayentry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher(arg);
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, arg});
        outLogLeecher.waitForText(2000);
    }

    @Test
    public void testAllNamedArgs() throws BallerinaTestException {
        String functionName = "defaultparamentry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("2 hi world: is false");
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "--", "-i=2", "-s=hi", "false", "is"});
        outLogLeecher.waitForText(2000);
    }

    @Test
    public void testSingleNamedArg() throws BallerinaTestException {
        String functionName = "defaultparamentry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("1 hi world: is false");
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "--", "-s=hi", "false", "is"});
        outLogLeecher.waitForText(2000);
    }

    @Test
    public void testNoNamedArg() throws BallerinaTestException {
        String functionName = "defaultparamentry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("1 default hello world: is true");
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "--", "true", "is"});
        outLogLeecher.waitForText(2000);
    }

    @Test
    public void testMultipleParam() throws BallerinaTestException {
        String functionName = "combinedmain";
        sourceArg = filePath + ":" + functionName;
        LogLeecher outLogLeecher = new LogLeecher("integer: 1000, float: 1.0, string: Hello Ballerina, byte: 255, "
                                                          + "boolean: true, JSON Name Field: Maryam, XML Element Name: "
                                                          + "book, Employee Name Field: Em, string rest args: just the"
                                                          + " rest ");
        serverInstance.addLogLeecher(outLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "1000", "1.0", "Hello Ballerina", "255", "true",
                                        "{ \"name\": \"Maryam\" }", "<book>Harry Potter</book>", "{ \"name\": \"Em\" }",
                                        "just", "the", "rest"});
        outLogLeecher.waitForText(2000);
    }

    @AfterMethod
    public void tearDown() throws BallerinaTestException {
        serverInstance.stopServer();
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
