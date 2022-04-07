/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.runtime.entity;

import org.ballerinalang.test.runtime.util.TesterinaUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class for printing Testerina test report.
 */
public class TesterinaReport {

    private PrintStream outStream;
    private Map<String, TestSummary> testReportOfPackage = new HashMap<>();
    private boolean failure;
    private boolean isReportRequired;

    /**
     * Create an instance of Testerina Report.
     */
    public TesterinaReport() {
        this(System.out);
    }
    
    /**
     * Create an instance of Testerina Report with a given log stream.
     *
     * @param outStream The log stream.
     */
    public TesterinaReport(PrintStream outStream) {
        this.outStream = outStream;
    }
    
    public void printTestSuiteSummary(String packageName) {
        TestSummary testSummary = testReportOfPackage.get(packageName);
        if (testSummary == null) {
            printTestSuiteResult(0, 0, 0);
            return;
        }
        outStream.println();
        if (!testSummary.failedTests.isEmpty()) {
            for (TesterinaResult failedResult : testSummary.failedTests) {
                outStream.println("\t\t[fail] " + failedResult.getTestFunctionName() + ":");
                outStream.println("\n\t\t    " + TesterinaUtils.formatError(failedResult.getAssertFailureMessage()));
            }
        }
        if (System.console() == null) {
            if (!testSummary.passedTests.isEmpty()) {
                for (TesterinaResult passedResult : testSummary.passedTests) {
                    outStream.println("\t\t[pass] " + passedResult.getTestFunctionName());
                }
            }
        }

        printTestSuiteResult(testSummary.passedTests.size(), testSummary.failedTests.size(), testSummary.skippedTests
                .size());
    }

    private void printTestSuiteResult(int passed, int failed, int skipped) {
        outStream.println();
        outStream.println("\t\t" + passed + " passing");
        outStream.println("\t\t" + failed + " failing");
        outStream.println("\t\t" + skipped + " skipped");
        outStream.println();
    }

    public void addPackageReport(String packageName) {
        testReportOfPackage.computeIfAbsent(packageName, summary -> new TestSummary());
    }

    public void addFunctionResult(String packageName, TesterinaResult result) {
        testReportOfPackage.computeIfAbsent(packageName, summary -> new TestSummary());
        TestSummary testSummary = testReportOfPackage.get(packageName);
        ModuleStatus.Status status;
        if (result.isSkipped()) {
            failure = true;
            status = ModuleStatus.Status.SKIPPED;
            testSummary.skippedTests.add(result);
        } else if (result.isPassed()) {
            status = ModuleStatus.Status.PASSED;
            testSummary.passedTests.add(result);
        } else {
            failure = true;
            status = ModuleStatus.Status.FAILURE;
            testSummary.failedTests.add(result);
        }

        if (isReportRequired) {
            ModuleStatus.getInstance()
                    .addTestSummary(result.getTestFunctionName(), status, result.getAssertFailureMessage());
        }
    }

    /**
     * Was there at least one test failure or skip.
     *
     * @return whether there's a test failure or not
     */
    public boolean isFailure() {
        return failure;
    }

    public void setReportRequired(boolean reportRequired) {
        isReportRequired = reportRequired;
    }

    /**
     * Summary results of a test package.
     */
    private static class TestSummary {
        List<TesterinaResult> passedTests = new ArrayList<>();
        List<TesterinaResult> failedTests = new ArrayList<>();
        List<TesterinaResult> skippedTests = new ArrayList<>();

        public List<TesterinaResult> getPassedTests() {
            return passedTests;
        }

        public List<TesterinaResult> getFailedTests() {
            return failedTests;
        }

        public List<TesterinaResult> getSkippedTests() {
            return skippedTests;
        }
    }
}
