/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.testerina.test.evaluation;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.BaseTestCase;
import org.ballerinalang.testerina.test.utils.CommonUtils;
import org.ballerinalang.testerina.test.utils.FileUtils;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Base class for evaluation feature tests.
 * Provides common utilities and setup for all evaluation test classes.
 */
public abstract class BaseEvaluationTest extends BaseTestCase {

    protected BMainInstance balClient;
    protected String evaluationProjectPath;
    protected static final String PARALLEL_FLAG = "--parallel";
    protected static final String TEST_COMMAND = "test";
    protected static final Path COMMAND_OUTPUTS_DIR = Path
            .of("src", TEST_COMMAND, "resources", "evaluation-outputs");

    @BeforeClass()
    public void setup() throws IOException {
        balClient = new BMainInstance(balServer);
        Path originalProjTestsDir = Path.of("src/test/resources/evaluation-tests").toAbsolutePath();
        Path temporaryProjectTestDir = Files.createTempDirectory("bal-test-integration-testerina-project-")
                .resolve("evaluation-tests");
        FileUtils.copyFolder(originalProjTestsDir, temporaryProjectTestDir);
        evaluationProjectPath = temporaryProjectTestDir.toString();
    }

    /**
     * Writes test output to both Windows and Unix output directories.
     *
     * @param fileName The name of the output file
     * @param output   The test output to write
     */
    protected void writeTestOutToFile(String fileName, String output) throws IOException {
        output = CommonUtils.replaceExecutionTime(output);
        output = EvaluationUtils.replaceProjectPath(output);
        Files.writeString(COMMAND_OUTPUTS_DIR.resolve("windows").resolve(fileName), output);
        Files.writeString(COMMAND_OUTPUTS_DIR.resolve("unix").resolve(fileName), output);
    }

    /**
     * Runs a specific test and verifies its output.
     *
     * @param testName    The name of the test to run
     * @param packageName The package containing the test
     */
    protected void runTestAndVerify(String testName, String packageName) throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, "--tests", testName, packageName});
        String output = balClient.runMainAndReadStdOut(TEST_COMMAND, args, new HashMap<>(),
                evaluationProjectPath, true);
        String fileName = getOutputFileName(testName);
        // Uncomment only when regenerating expected output files for tests
        // writeTestOutToFile(fileName, output);
        EvaluationUtils.assertOutput(fileName, output);
    }

    /**
     * Runs all tests in a package and verifies output.
     *
     * @param packageName The package to test
     */
    protected void runPackageTestAndVerify(String packageName) throws BallerinaTestException, IOException {
        String output = runPackageTest(packageName);
        String fileName = getOutputFileName(packageName);
        // Uncomment only when regenerating expected output files for tests
        // writeTestOutToFile(fileName, output);
        EvaluationUtils.assertOutput(fileName, output);
    }

    /**
     * Runs all tests in a package.
     *
     * @param packageName The package to test
     */
    protected String runPackageTest(String packageName) throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{PARALLEL_FLAG, packageName});
        return balClient.runMainAndReadStdOut(TEST_COMMAND, args, new HashMap<>(),
                evaluationProjectPath, true);
    }

    protected String getEvaluationTestJsonReport(String packageName) throws IOException {
        Path evalReportPath = Path.of(evaluationProjectPath).resolve(packageName).resolve("target").resolve("report")
                .resolve("test_results.json");
        return String.valueOf(Files.readAllLines(evalReportPath));
    }

    /**
     * Generates the output file name for a test.
     *
     * @param testName The name of the test
     * @return The output file name
     */
    private String getOutputFileName(String testName) {
        return getClass().getSimpleName() + "-" + testName + ".txt";
    }
}
