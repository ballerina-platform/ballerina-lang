/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.test.utils;

import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.io.PrintStream;
import java.util.HashSet;

/**
 * Test listener for listening to test events.
 *
 * @since 1.0
 */
public class TestEventListener extends TestListenerAdapter {

    private static final HashSet<String> processedTestCases = new HashSet<>();

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String BOLD = "\u001B[1m";

    @Override
    public void beforeConfiguration(ITestResult tr) {
        PrintStream printStream = new PrintStream(System.out);
        String testClassName = tr.getTestClass().getRealClass().getSimpleName();

        if (tr.getMethod().isBeforeClassConfiguration() && !processedTestCases.contains(testClassName)) {
            printStream.println("\n" + BOLD + "// Start Running " + testClassName + " ...\n" + RESET);
            processedTestCases.add(testClassName);
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testCase = result.getName();
        LoggerFactory.getLogger(result.getTestClass().getRealClass().getSimpleName()).info(
                BOLD + testCase + ": " + BLUE + BOLD + "RUNNING" + RESET);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        String testCase = tr.getName();
        LoggerFactory.getLogger(tr.getTestClass().getRealClass().getSimpleName()).info(
                BOLD + testCase + ": " + GREEN + BOLD + "PASSED" + RESET);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        String testCase = tr.getName();
        LoggerFactory.getLogger(tr.getTestClass().getRealClass()).info(
                BOLD + testCase + ": " + YELLOW + BOLD + "SKIPPED" + RESET);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        String testCase = tr.getName();
        Throwable e = tr.getThrowable();
        LoggerFactory.getLogger(tr.getTestClass().getRealClass())
                .error(BOLD + testCase + ": " + RED + BOLD + "FAILED" + " -> " + e.getMessage() + RESET);
    }
}
