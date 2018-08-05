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
public class RunFunctionNegativeTestCase extends IntegrationTestCase {

    private String serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
    private ServerInstance serverInstance;

    private String filePath = (new File("src/test/resources/run/file/test_entry_function.bal")).getAbsolutePath();

    private String sourceArg;

    @BeforeMethod()
    public void setUp() throws BallerinaTestException, IOException {
        serverInstance = new ServerInstance(serverZipPath);
    }

    @Test
    public void testTooManyArgs() throws BallerinaTestException {
        String functionName = "nomoremain";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: too many arguments to call entry function '"
                                                   + functionName + "'");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "1", "Hello World", "I'm just extra!"});
        errLogLeecher.waitForText(2000);

    }

    @Test
    public void testInsufficientArgs() throws BallerinaTestException {
        String functionName = "combinedmain";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: insufficient arguments to call entry function '"
                                                          + functionName + "'");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "1"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testNonPublicFunctionAsEntryFunction() throws BallerinaTestException {
        String functionName = "nonpublicnonmain";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("non public function '" + functionName
                                                          + "' not allowed as entry function");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "1"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidIntArg() throws BallerinaTestException {
        String functionName = "combinedmain";
        String argument = "5ss";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument: " + argument
                                                          + ", expected integer value");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, argument, "a", "b", "c", "d", "e", "f", "g"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidFloatArg() throws BallerinaTestException {
        String functionName = "combinedmain";
        String argument = "5.5s";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument: " + argument
                                                          + ", expected float value");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "10", argument, "b", "c", "d", "e", "f", "g"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidJsonObjectArg() throws BallerinaTestException {
        String functionName = "jsonentry";
        String argument = "{name: \"maryam\"}";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid JSON value: " + argument);
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, argument});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidXmlArg() throws BallerinaTestException {
        String functionName = "combinedmain";
        String argument = "<book>Harry Potter";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid XML value: " + argument);
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "10", "1.0", "string!", "255", "true", "1", argument, "g"});
        errLogLeecher.waitForText(2000);
    }

    @Test (dataProvider = "byteValues")
    public void testInvalidByteArg(String arg) throws BallerinaTestException {
        String functionName = "combinedmain";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument: " + arg
                                                          + ", expected byte value");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "--", "10", "1.0", "string!", arg, "a", "b", "c", "d"});
        errLogLeecher.waitForText(2000);
    }


    @Test
    public void testInvalidBooleanArg() throws BallerinaTestException {
        String functionName = "combinedmain";
        String argument = "truer";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: invalid argument: " + argument
                                                          + ", expected boolean value");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "10", "1.0", "string!", "255", argument, "a", "b", "c"});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidArrayArg() throws BallerinaTestException {
        String functionName = "stringarrayentry";
        String argument = "true false true";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: expected array notation (\"[]\") with array arg");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, argument});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidUnionArg() throws BallerinaTestException {
        String functionName = "unionarrayentry";
        String argument = "true false true";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: incompatible argument specified for union type: "
                                                          + "int[]|float[]|boolean[]|json[]|string[]");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, argument});
        errLogLeecher.waitForText(2000);
    }

    @Test
    public void testInvalidArgsWithDefaultableParams() throws BallerinaTestException {
        String functionName = "defaultparamentry";
        sourceArg = filePath + ":" + functionName;
        LogLeecher errLogLeecher = new LogLeecher("usage error: too many arguments to call entry function '"
                                                          + functionName + "'");
        serverInstance.addErrorLogLeecher(errLogLeecher);
        serverInstance.runMain(new String[]{sourceArg, "1", "true", "hi", "not"});
        errLogLeecher.waitForText(2000);
    }

    @AfterMethod
    public void tearDown() throws BallerinaTestException {
        serverInstance.stopServer();
    }

    @DataProvider(name = "byteValues")
    public Object[][] byteValues() {
        return new Object[][] {
                { "-1" },
                { "256" }
        };
    }
}
