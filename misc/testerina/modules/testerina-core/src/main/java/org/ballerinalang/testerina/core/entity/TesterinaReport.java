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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Util class for printing Testerina test report.
 */
public class TesterinaReport {

    private static PrintStream outStream = System.err;

    private List<TesterinaResult> passedTests = new ArrayList<>();
    private List<TesterinaResult> failedTests = new ArrayList<>();

    public void printTestSummary() {
        if (!passedTests.isEmpty() || !failedTests.isEmpty()) {
            int totalTests = passedTests.size() + failedTests.size();
            outStream.println();
            outStream.println("result: ");
            outStream.print("tests run: " + totalTests);
            outStream.print(", passed: " + passedTests.size());
            outStream.println(", failed: " + failedTests.size());
        }

        if (!failedTests.isEmpty()) {
            outStream.println("failed tests:");
            for (TesterinaResult failedResult : failedTests) {
                outStream.println(
                        " " + failedResult.getTestFunctionName() + ": " + failedResult.getAssertFailureMessage());
            }
        }
    }

    public void addFunctionResult(TesterinaResult result) {
        if (result.isPassed()) {
            passedTests.add(result);
        } else {
            failedTests.add(result);
        }
    }

    public List<TesterinaResult> getPassedTests() {
        return Collections.unmodifiableList(passedTests);
    }

    public List<TesterinaResult> getFailedTests() {
        return Collections.unmodifiableList(failedTests);
    }

}
