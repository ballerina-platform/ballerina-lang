// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.testerina.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * Test class to test the parallel execution of Ballerina tests.
 */

public class TestparallelizationTest extends BaseTestCase {

    public static final String PARALLEL_FLAG = "--parallel";
    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve("parallelisation-test").toString();
    }

    @Test
    public void testParallelization() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "parallelisation-simple-test"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("61 passing") && output.contains("0 failing"));
        args = mergeCoverageArgs(new String[]{"parallelisation-simple-test"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("61 passing") && output.contains("0 failing"));
        Assert.assertTrue(executionTimeW1 / 3 > executionTimeW30);
    }

    @Test
    public void testNonParallelizable() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "parallelisation-serialExecution"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("61 passing") && output.contains("0 failing"));

        args = mergeCoverageArgs(new String[]{"parallelisation-serialExecution"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("61 passing") && output.contains("0 failing"));
        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }


    @Test
    public void testParalallelizableTupleDataProvider() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "parallelisation-tuple-data-provider"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("30 passing") && output.contains("0 failing"));
        args = mergeCoverageArgs(new String[]{"parallelisation-tuple-data-provider"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("30 passing") && output.contains("0 failing"));
        Assert.assertTrue(executionTimeW1 / 3 > executionTimeW30);
    }

    @Test
    public void testParalallelizableMapDataProvider() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "parallelisation-map-data-provider"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("12 passing") && output.contains("28 failing"));

        args = mergeCoverageArgs(new String[]{"parallelisation-map-data-provider"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("12 passing") && output.contains("28 failing"));
        Assert.assertTrue(executionTimeW1 / 3 > executionTimeW30);
    }

    @Test
    public void testNonParalallelizableTupleDataProvider() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "non-parallelisation-tuple-data-provider"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("30 passing") && output.contains("0 failing"));

        args = mergeCoverageArgs(new String[]{"non-parallelisation-tuple-data-provider"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("30 passing") && output.contains("0 failing"));
        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }

    @Test
    public void testNonIsolatedTestFunction() throws BallerinaTestException, IOException {
        String warningDiagnostics = "WARNING: Test function 'testAssertEquals*' cannot be parallelized, reason: " +
                "non-isolated test function";
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "non-isolated-tests"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("60 passing") && output.contains("0 failing"));
        for (int testNo = 1; testNo < 61; testNo++) {
            Assert.assertTrue(output.contains(warningDiagnostics.replace("*", Integer.toString(testNo))));
        }
        args = mergeCoverageArgs(new String[]{"non-isolated-tests"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("60 passing") && output.contains("0 failing"));
        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }

    @Test
    public void testNonIsolatedTestParameter() throws BallerinaTestException, IOException {
        String warningDiagnostics = "WARNING: Test function 'mapDataProviderTest' cannot be parallelized, reason: " +
                "unsafe test parameters";
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "non-isolated-test-params"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("12 passing") && output.contains("28 failing"));
        Assert.assertTrue(output.contains(warningDiagnostics));

        args = mergeCoverageArgs(new String[]{"non-isolated-test-params"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("12 passing") && output.contains("28 failing"));
        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }

    @Test
    public void testNonIsolatedDataProvider() throws BallerinaTestException, IOException {
        String warningDiagnostics = "WARNING: Test function 'mapDataProviderTest' cannot be parallelized, reason: " +
                "non-isolated data-provider function";
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "non-isolated-data-provider"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(output.contains(warningDiagnostics));

        args = mergeCoverageArgs(new String[]{"non-isolated-data-provider"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }

    @Test
    public void testNonIsolatedAfterEach() throws BallerinaTestException, IOException {
        String warningDiagnostics = "WARNING: Test function 'mapDataProviderTest' cannot be parallelized, reason: " +
                "non-isolated after-each function";
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "non-isolated-after-each"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(output.contains(warningDiagnostics));

        args = mergeCoverageArgs(new String[]{"non-isolated-after-each"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }

    @Test
    public void testNonIsolatedBeforeEach() throws BallerinaTestException, IOException {
        String warningDiagnostics = "WARNING: Test function 'mapDataProviderTest' cannot be parallelized, reason: " +
                "non-isolated before-each function";
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "non-isolated-before-each"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(output.contains(warningDiagnostics));

        args = mergeCoverageArgs(new String[]{"non-isolated-before-each"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }

    @Test
    public void testNonIsolatedAfterFunc() throws BallerinaTestException, IOException {
        String warningDiagnostics = "WARNING: Test function 'mapDataProviderTest' cannot be parallelized, reason: " +
                "non-isolated after function";
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "non-isolated-after-func"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(output.contains(warningDiagnostics));

        args = mergeCoverageArgs(new String[]{"non-isolated-after-func"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }

    @Test
    public void testNonIsolatedBeforeFunc() throws BallerinaTestException, IOException {
        String warningDiagnostics = "WARNING: Test function 'mapDataProviderTest' cannot be parallelized, reason: " +
                "non-isolated before function";
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "non-isolated-before-func"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(output.contains(warningDiagnostics));

        args = mergeCoverageArgs(new String[]{"non-isolated-before-func"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }

    @Test
    public void testNonIsolatedAfterGroup() throws BallerinaTestException, IOException {
        String warningDiagnostics = "WARNING: Test function 'mapDataProviderTest' cannot be parallelized, reason: " +
                "non-isolated after-groups function";
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "non-isolated-after-grp"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(output.contains(warningDiagnostics));

        args = mergeCoverageArgs(new String[]{"non-isolated-after-grp"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }

    @Test
    public void testNonIsolatedBeforeGroup() throws BallerinaTestException, IOException {
        String warningDiagnostics = "WARNING: Test function 'mapDataProviderTest' cannot be parallelized, reason: " +
                "non-isolated before-groups function";
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "non-isolated-before-grp"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(output.contains(warningDiagnostics));

        args = mergeCoverageArgs(new String[]{"non-isolated-before-grp"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }

    @Test
    public void testIsolatedSetUpTearDown() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "isolated-set-up-tear-down"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));

        args = mergeCoverageArgs(new String[]{"isolated-set-up-tear-down"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("40 passing") && output.contains("0 failing"));
        Assert.assertTrue(executionTimeW1 > 2 * executionTimeW30);
    }

    @Test
    public void testNonParalallelizableMapDataProvider() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "non-parallelisation-map-data-provider"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("12 passing") && output.contains("28 failing"));

        args = mergeCoverageArgs(new String[]{"parallelisation-map-data-provider"});
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        Assert.assertTrue(output.contains("12 passing") && output.contains("28 failing"));
        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }

    private float getTimeForTestExecution(String output) {
        int firstPos = output.indexOf("Test execution time :") + ("Test execution time :").length();
        int lastPos = output.indexOf("s", firstPos);
        String executionTime = output.substring(firstPos, lastPos);
        return Float.parseFloat(executionTime);
    }
}
