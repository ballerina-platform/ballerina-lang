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

package org.ballerinalang.testerina.core.entity;

import org.wso2.ballerinalang.compiler.util.Names;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Util class for printing Testerina test report.
 */
public class TesterinaReport {

    private static PrintStream outStream = System.err;
    private Map<String, TestSummary> testReportOfPackage = new HashMap<>();
    private TestSummary testSummary;
    private boolean failure;

    public void printTestSuiteSummary(String packageName) {
        testSummary = testReportOfPackage.get(packageName);
        if (testSummary == null) {
            printTestSuiteResult(0, 0, 0);
            return;
        }
//        printTestSuiteResult(testSummary.passedTests.size(), testSummary.failedTests.size(), testSummary.skippedTests
//                .size());
//        outStream.println();
        if (!testSummary.failedTests.isEmpty()) {
//            outStream.println();
//            outStream.println("\t" + "failed tests:");
            for (TesterinaResult failedResult : testSummary.failedTests) {
//                outStream.println("\t   ✗ "+ failedResult.getTestFunctionName());
//                outStream.println("✗ "+ failedResult.getTestFunctionName());
//                outStream.println(failedResult.getAssertFailureMessage());
                                          // + ": " + failedResult.getAssertFailureMessage());
                outStream.println("✗ "+ failedResult.getTestFunctionName() + ": "
                                          + failedResult.getAssertFailureMessage());
            }
//            outStream.println();
        }

        if (!testSummary.passedTests.isEmpty()) {
//            outStream.println();
//            outStream.println("\t" + "failed tests:");
            for (TesterinaResult passedResult : testSummary.passedTests) {
//                outStream.println("\t   ✔ "+ passedResult.getTestFunctionName());
                outStream.println("✔ "+ passedResult.getTestFunctionName());
            }
            outStream.println();
        }
        printTestSuiteResult(testSummary.passedTests.size(), testSummary.failedTests.size(), testSummary.skippedTests
                .size());

        outStream.println();
    }

    private void printTestSuiteResult(int passed, int failed, int skipped) {
//        outStream.println();
        // outStream.print("Tests run: " + (passed + failed));
//        outStream.println("\t   " + passed + " passing");
//        outStream.println("\t   " + failed + " failing");
//        outStream.println("\t   " + skipped + " skipped");

        outStream.println(" " + passed + " passing");
        outStream.println(" " + failed + " failing");
        outStream.println(" " + skipped + " skipped");
//        outStream.println("\t\t" + (passed + failed) + " tests run");
        // outStream.print(" - in TestSuite");
        // outStream.println();
    }

    public void printSummary() {
        int totalPassed = 0, totalFailed = 0, totalSkipped = 0;
        for (TestSummary summary : testReportOfPackage.values()) {
            totalPassed += summary.passedTests.size();
            totalFailed += summary.failedTests.size();
            totalSkipped += summary.skippedTests.size();
        }

//        outStream.println();
//        // outStream.println("---------------------------------------------------------------------------");
//        outStream.println("\t\t" + "results:");
////        outStream.println();
//        outStream.println("\t\t" + totalPassed + " passing");
//        outStream.println("\t\t" + totalFailed + " failing");
//        outStream.println("\t\t" + totalSkipped + " skipped");
//        outStream.println("\t\t" + (totalPassed + totalFailed) + " tests run");

//        outStream.print("Tests run: " + (totalPassed + totalFailed));
//        outStream.print(", Passed: " + totalPassed);
//        outStream.print(", Failures: " + totalFailed);
//        outStream.print(", Skipped: " + totalSkipped);
//        outStream.println();
//        outStream.println("---------------------------------------------------------------------------");
//        outStream.println("Summary:");
//        outStream.println();

//        outStream.println();
        // outStream.println("---------------------------------------------------------------------------");
        // outStream.println();


//        if (!testSummary.failedTests.isEmpty()) {
//            for (TesterinaResult failedResult : testSummary.failedTests) {
////                outStream.println("\t✗ "+ failedResult.getTestFunctionName() + ": ");
////                outStream.println("\t  "+ failedResult.getAssertFailureMessage());
//
//                outStream.println("✗ "+ failedResult.getTestFunctionName() + ": "
//                                          + failedResult.getAssertFailureMessage());
//            }
//            // outStream.println();
//        }

        printSucessAndFailures();

    }

    public void printSucessAndFailures() {
        if (testReportOfPackage.size() == 0) {
//            outStream.println("Test Suites: 0");
        } else {
            LinkedList<String> keys = new LinkedList<>(testReportOfPackage.keySet());
            Collections.sort(keys);
            keys.forEach(packageName -> {
                TestSummary summary = testReportOfPackage.get(packageName);
                outStream.println();
                outStream.print(String.format("%-" + 67 + "s", packageName).replaceAll("\\s(?=\\s+$|$)", "."));
                outStream.print(" " + ((summary.failedTests.size() > 0 || summary.skippedTests.size() > 0) ?
                        "FAILURE" : "SUCCESS"));
            Optional<String> longest = testReportOfPackage.keySet().stream()
                                                          .sorted((e1, e2) -> e1.length() > e2.length() ? -1 : 1)
                                                          .findFirst();
            testReportOfPackage.forEach((packageName, summary) -> {
                int size = 0;
                if (longest.isPresent()) {
                    int lengthOfLongest = longest.get().length();
                    int pkgLength = packageName.length();
                    if (lengthOfLongest > pkgLength) {
                        size = lengthOfLongest - pkgLength + 5;
                    } else {
                        size = 5;
                    }
                }
//                outStream.println();
//                outStream.print(String.format("%-" + 67 + "s", packageName).replaceAll("\\s(?=\\s+$|$)", "."));
                if (!packageName.equals(Names.DOT.value)) {
                    outStream.println(" " + packageName + " " + String.join("", Collections.nCopies(size, "-"))
                                            + " " + ((summary.failedTests.size() > 0 || summary.skippedTests.size() > 0)
                            ? "FAILURE" : "SUCCESS"));
                }
            });
        }
    }

    public void addPackageReport(String packageName) {
        testReportOfPackage.computeIfAbsent(packageName, summary -> new TestSummary());
    }

    public void addFunctionResult(String packageName, TesterinaResult result) {
        testReportOfPackage.computeIfAbsent(packageName, summary -> new TestSummary());
        TestSummary testSummary = testReportOfPackage.get(packageName);
        if (result.isSkipped()) {
            failure = true;
            testSummary.skippedTests.add(result);
        } else if (result.isPassed()) {
            testSummary.passedTests.add(result);
        } else {
            failure = true;
            testSummary.failedTests.add(result);
        }
    }

    /**
     * Returns a count of passed/failed/skipped tests of a given package.
     *
     * @param packageName name of the package
     * @param type        category - passed/failed/skipped
     * @return count per category per package.
     */
    public int getTestSummary(String packageName, String type) {
        TestSummary summary = testReportOfPackage.get(packageName);
        if ("passed".equals(type)) {
            return summary == null ? 0 : summary.passedTests.size();
        } else if ("failed".equals(type)) {
            return summary == null ? 0 : summary.failedTests.size();
        } else if ("skipped".equals(type)) {
            return summary == null ? 0 : summary.skippedTests.size();
        }
        return -1;
    }

    /**
     * Was there at least one test failure or skip.
     *
     * @return whether there's a test failure or not
     */
    public boolean isFailure() {
        return failure;
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
