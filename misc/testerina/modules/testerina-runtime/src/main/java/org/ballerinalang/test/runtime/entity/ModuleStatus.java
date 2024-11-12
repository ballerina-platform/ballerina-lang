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
package org.ballerinalang.test.runtime.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Status of a specific module used to generate the Json object based on the test results.
 *
 * @since 1.2.0
 */
public class ModuleStatus {
    private String name;
    private int totalTests;
    private int passed;
    private int failed;
    private int skipped;
    private final List<Test> tests = new ArrayList<>();

    private static ModuleStatus instance = new ModuleStatus();

    public static ModuleStatus getInstance() {
        return instance;
    }

    public static void clearInstance() {
        instance = new ModuleStatus();
    }

    public void addTestSummary(String testName, Status status, String failureMessage) {
        Test test = new Test(testName, status, failureMessage);
        this.tests.add(test);
        totalTests++;
        if (test.status.equals(Status.PASSED)) {
            this.passed++;
        } else if (test.status.equals(Status.FAILURE)) {
            this.failed++;
        } else {
            this.skipped++;
        }
    }

    public int getPassed() {
        return passed;
    }

    public int getFailed() {
        return failed;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getTotalTests() {
        return totalTests;
    }

    /**
     * Inner class for individual Test object.
     */
    private static class Test {
        private final String name;
        private final Status status;
        private String failureMessage = "";

        public Test(String name, Status status, String failureMessage) {
            this.name = name;
            this.status = status;
            this.failureMessage = failureMessage;
        }

        public String getFailureMessage() {
            return failureMessage;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Enum to represent test statuses.
     */
    public enum Status {

        PASSED("passed"),
        FAILURE("failure"),
        SKIPPED("skipped");

        private final String value;

        Status(String value) {
            this.value = value;
        }
    }
}
