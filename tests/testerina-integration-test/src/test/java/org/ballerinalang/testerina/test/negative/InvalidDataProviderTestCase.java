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
import org.ballerinalang.testerina.test.BaseTestCase;
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

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
        projectPath = singleFileTestsPath.resolve("invalid-data-providers").toString();
        errorlogFile = Paths.get(projectPath).resolve("ballerina-internal.log");
    }

    @BeforeMethod
    public void beforeEach() throws IOException {
        Files.deleteIfExists(errorlogFile);
    }

    @Test
    public void testInvalidDataProvider() throws BallerinaTestException, IOException {
        String errMsg1 = "[fail] testInvalidDataProvider:";
        String errMsg2 = "[fail data provider for the function testInvalidDataProvider]";
        String errMsg3 = "arguments of incompatible types: argument list '(int)' " +
                "cannot be passed to function expecting parameter list '(string)'";
        String[] args = mergeCoverageArgs(new String[]{"invalid-data-provider-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("InvalidDataProviderTestCase-testInvalidDataProvider.txt", output);
    }

    @Test
    public void testInvalidDataProvider2() throws BallerinaTestException, IOException {
        String errMsg1 = "[fail] testInvalidDataProvider2:";
        String errMsg2 = "[fail data provider for the function testInvalidDataProvider2]";
        String errMsg3 = "arguments of incompatible types: argument list '(int,int,int)' cannot be " +
                "passed to function expecting parameter list '(string,string,string)'";
        String[] args = mergeCoverageArgs(new String[]{"invalid-data-provider-test2.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("InvalidDataProviderTestCase-testInvalidDataProvider2.txt", output);
    }

    @Test
    public void testInvalidTupleDataProvider() throws BallerinaTestException, IOException {
        String errMsg1 = "[fail] testInvalidTupleDataProvider:";
        String errMsg2 = "[fail data provider for the function testInvalidTupleDataProvider]";
        String errMsg3 = "arguments of incompatible types: argument list '(string,int)' cannot be passed " +
                "to function expecting parameter list '(string,string)'";
        String[] args = mergeCoverageArgs(new String[]{"invalid-data-provider-test3.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("InvalidDataProviderTestCase-testInvalidTupleDataProvider.txt",
                output);
    }

    @Test
    public void testEmptyDataProvider() throws BallerinaTestException, IOException {
        String errMsg1 = "ERROR [empty-data-provider-test.bal:(14:19,14:28)] incompatible types: " +
                "expected 'function () returns (ballerina/test:0.0.0:DataProviderReturnType)?', " +
                "found 'function () returns (int[])'";
        String errMsg2 = "error: compilation contains errors";
        String[] args = mergeCoverageArgs(new String[]{"empty-data-provider-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("InvalidDataProviderTestCase-testEmptyDataProvider.txt",
                output);
    }
}
