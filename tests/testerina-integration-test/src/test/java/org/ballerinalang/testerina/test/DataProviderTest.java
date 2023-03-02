/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.ballerinalang.testerina.test.utils.CommonUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Test class to test map based data provider implementation.
 */
public class DataProviderTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass()
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.toString();
    }

    @Test
    public void testValidDataProvider() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--tests", "jsonDataProviderTest", "data-providers"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testValidDataProvider.txt", output);
    }

    @Test (dependsOnMethods = "testValidDataProvider")
    public void testValidDataProviderWithFail() throws BallerinaTestException, IOException {
        String msg1 = "1 passing";
        String msg2 = "2 failing";
        String[] args = mergeCoverageArgs(new String[]{"--tests", "intDataProviderTest", "data-providers"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testValidDataProviderWithFail.txt", output);
    }

    @Test (dependsOnMethods = "testValidDataProviderWithFail")
    public void testRerunFailedTest() throws BallerinaTestException, IOException {
        String msg1 = "0 passing";
        String msg2 = "2 failing";
        String[] args = mergeCoverageArgs(new String[]{"--rerun-failed", "data-providers"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testRerunFailedTest.txt", output);
    }

    @Test (dependsOnMethods = "testValidDataProviderWithFail")
    public void testValidDataProviderCase() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--tests", "dataproviders:jsonDataProviderTest#'json1'",
                "data-providers"});
        String msg1 = "1 passing";
        String msg2 = "0 failing";
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testValidDataProviderCase.txt", output);
    }

    @Test (dependsOnMethods = "testValidDataProviderCase")
    public void testDataProviderWithMixedType() throws BallerinaTestException, IOException {
        String msg1 = "2 passing";
        String msg2 = "0 failing";
        String[] args = mergeCoverageArgs(new String[]{"--tests", "testFunction1#'CaseNew*'",
                "data-providers"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testDataProviderWithMixedType.txt", output);
    }

    @Test (dependsOnMethods = "testDataProviderWithMixedType")
    public void testWithSpecialKeys() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--tests", "testFunction2",
                "data-providers"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testWithSpecialKeys.txt", output);
    }

    @Test (dependsOnMethods = "testWithSpecialKeys")
    public void testArrayDataProviderWithFail() throws BallerinaTestException, IOException {
        String msg1 = "1 passing";
        String msg2 = "2 failing";
        String[] args = mergeCoverageArgs(new String[]{"--tests", "intArrayDataProviderTest", "data-providers"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testArrayDataProviderWithFail.txt", output);
    }

    @Test (dependsOnMethods = "testArrayDataProviderWithFail")
    public void testArrayDataRerunFailedTest() throws BallerinaTestException, IOException {
        String msg1 = "0 passing";
        String msg2 = "2 failing";
        String[] args = mergeCoverageArgs(new String[]{"--rerun-failed", "data-providers"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testArrayDataRerunFailedTest.txt", output);
    }

    @Test (dependsOnMethods = "testArrayDataRerunFailedTest")
    public void testMultiModuleSingleTestExec() throws BallerinaTestException, IOException {
        String msg1 = "1 passing";
        String msg2 = "0 failing";
        String[] args = mergeCoverageArgs(new String[]{"--tests", "stringDataProviderMod1Test#1", "data-providers"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testMultiModuleSingleTestExec.txt", output);
    }

    @Test
    public void testCodeFragmentKeys() throws BallerinaTestException, IOException {
        String endString = " SEVERE {b7a.log.crash} - ";
        String firstString = "We thank you for helping make us better.";
        List<String> keys = new ArrayList<>(Arrays.asList("'%60'%22%5Ca%22%22'",
                "'%22%5Cu{D7FF}%22%22%09%22'", "'a +%0A%0D b'",
                "'(x * 1) %21= (y / 3) || (a ^ b) == (b & c) >> (1 % 2)'", "'%281'", "'a:x(c%2Cd)[]; ^(x|y).ok();'",
                "'map<any> v = { %22x%22: 1 };'"));
        int count = 0;
        for (String key:keys) {
            String[] args = mergeCoverageArgs(new String[]{"--tests", "testFunction3#" + key, "data-providers"});
            String output = balClient.runMainAndReadStdOut("test", args,
                    new HashMap<>(), projectPath, false);
            if (output.contains(endString) && output.contains(firstString)) {
                output = CommonUtils.replaceVaryingString(firstString, endString, output);
            }
            AssertionUtils.assertOutput("DataProviderTest-testCodeFragmentKeys"
                    + count + ".txt", output);
            count++;
        }
    }

    @Test
    public void testMapValueDataProvider() throws BallerinaTestException, IOException {

        String msg1 = "1 passing";
        String msg2 = "1 failing";
        String[] args = mergeCoverageArgs(new String[]{"--tests", "testGetState", "data-providers"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testMapValueDataProvider.txt", output);

    }

    @Test(enabled = false) // Enable once https://github.com/ballerina-platform/ballerina-lang/issues/39671 completed
    public void testValidDataProviderWithBeforeAfterFunctions() throws BallerinaTestException, IOException {
        String msg1 = "6 passing";
        String msg2 = "0 failing";
        String[] args = mergeCoverageArgs(new String[]{"--tests", "testExecutionOfBeforeAfter", "data-providers"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testValidDataProviderWithBeforeAfterFunctions.txt"
                , output);
    }

    @Test
    public void testValidDataProviderWithBeforeFailing() throws BallerinaTestException, IOException {
        String msg1 = "5 passing";
        String msg2 = "0 failing";
        String msg3 = "1 skipped";
        String[] args = mergeCoverageArgs(new String[]{"--tests",
                "testDividingValuesWithBeforeFailing,testExecutionOfBeforeFailing", "data-providers"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testValidDataProviderWithBeforeFailing.txt", output);
    }

    @Test
    public void testValidDataProviderWithAfterFailing() throws BallerinaTestException, IOException {
        String msg1 = "6 passing";
        String msg2 = "0 failing";
        String msg3 = "0 skipped";
        String[] args = mergeCoverageArgs(new String[]{"--tests",
                "testDividingValuesWithAfterFailing,testExecutionOfAfterFailing", "data-providers"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testValidDataProviderWithAfterFailing.txt", output);
    }

    @Test
    public void testDataProviderSingleFailure() throws BallerinaTestException, IOException {
        String msg1 = "5 passing";
        String msg2 = "1 failing";
        String msg3 = "0 skipped";
        String[] args = mergeCoverageArgs(new String[]{"--tests",
                "testExecutionOfDataValueFailing", "data-providers"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("DataProviderTest-testDataProviderSingleFailure.txt", output);
    }
}
