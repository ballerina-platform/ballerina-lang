/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.testerina.test.negative;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.testerina.test.BaseTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.test.context.LogLeecher.LeecherType.ERROR;

/**
 * Negative test cases for data providers.
 */
public class InvalidDataProviderTestCase extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;
    private Path errorlogFile;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = singleFilesProjectPath.resolve("invalid-data-providers").toString();
        errorlogFile = Paths.get(projectPath).resolve("ballerina-internal.log");
    }

    @BeforeMethod
    public void beforeEach() throws IOException {
        Files.deleteIfExists(errorlogFile);
    }

    @Test
    public void testInvalidDataProvider() throws BallerinaTestException {
        String errMsg = "error: Error while invoking function 'testInvalidDataProvider'";
        String errMsg2 = "If you are using data providers please check if types return from data provider match test " +
                "function parameter types";
        LogLeecher clientLeecher = new LogLeecher(errMsg, ERROR);
        LogLeecher clientLeecher2 = new LogLeecher(errMsg2, ERROR);
        balClient.runMain("test", new String[]{"invalid-data-provider-test.bal"}, null, new String[]{},
                          new LogLeecher[]{clientLeecher, clientLeecher2}, projectPath);
        clientLeecher.waitForText(20000);
        clientLeecher2.waitForText(20000);
    }

    @Test
    public void testInvalidDataProvider2() throws BallerinaTestException {
        String errMsg = "error: Error while invoking function 'testInvalidDataProvider2'";
        String errMsg2 = "If you are using data providers please check if types return from data provider match test " +
                "function parameter types";
        LogLeecher clientLeecher = new LogLeecher(errMsg, ERROR);
        LogLeecher clientLeecher2 = new LogLeecher(errMsg2, ERROR);
        balClient.runMain("test", new String[]{"invalid-data-provider-test2.bal"}, null, new String[]{},
                          new LogLeecher[]{clientLeecher, clientLeecher2}, projectPath);
        clientLeecher.waitForText(20000);
        clientLeecher2.waitForText(20000);
    }

    @Test(enabled = false)
    public void testInvalidTupleDataProvider() throws BallerinaTestException {
        String errMsg = "error: Error while invoking function 'testInvalidTupleDataProvider'";
        String errMsg2 = "If you are using data providers please check if types return from data provider match" +
                " test function parameter types";
        LogLeecher clientLeecher = new LogLeecher(errMsg, ERROR);
        LogLeecher clientLeecher2 = new LogLeecher(errMsg2, ERROR);
        balClient.runMain("test", new String[]{"invalid-data-provider-test3.bal"}, null, new String[]{},
                          new LogLeecher[]{clientLeecher, clientLeecher2}, projectPath);
        clientLeecher.waitForText(20000);
        clientLeecher2.waitForText(20000);
    }
}
