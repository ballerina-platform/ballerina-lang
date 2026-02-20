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
        private final String failureMessage;
        private final EvaluationSummary evaluationSummary;

        public Test(String name, Status status, String failureMessage) {
            this(name, status, failureMessage, null);
        }

        public Test(String name, Status status, String failureMessage, EvaluationSummary evaluationSummary) {
            this.name = name;
            this.status = status;
            this.failureMessage = failureMessage;
            this.evaluationSummary = evaluationSummary;
        }

        public String getFailureMessage() {
            return failureMessage;
        }

        public String getName() {
            return name;
        }

        public Status getStatus() {
            return status;
        }

        public EvaluationSummary getEvaluationSummary() {
            return evaluationSummary;
        }
    }

    /**
     * Class to represent the evaluation summary.
     */
    private static class EvaluationSummary {
        private final EvaluationRun[] evaluationRuns;
        private final double targetConfidence;
        private final double observedConfidence;

        private EvaluationSummary(EvaluationRun[] evalRuns, double targetConfidence, double observedConfidence) {
            this.evaluationRuns = evalRuns;
            this.targetConfidence = targetConfidence;
            this.observedConfidence = observedConfidence;
        }

        public EvaluationRun[] getEvaluationRuns() {
            return evaluationRuns;
        }

        public double getTargetConfidence() {
            return targetConfidence;
        }

        public double getObservedConfidence() {
            return observedConfidence;
        }
    }

    /**
     * Base class for evaluation runs.
     */
    private static class EvaluationRun {
        private int id;
        private String errorMessage; // Optional - for runs without dataset
        private EvaluationOutcome[] outcomes; // Optional - for runs with dataset
        private Float passRate; // Optional - for runs with dataset (using Float for nullability)

        public EvaluationRun() {
            // Default constructor for JSON deserialization
        }

        public EvaluationRun(int id, String errorMessage) {
            this.id = id;
            this.errorMessage = errorMessage;
        }

        public EvaluationRun(int id, EvaluationOutcome[] outcomes, float passRate) {
            this.id = id;
            this.outcomes = outcomes;
            this.passRate = passRate;
        }

        public int getId() {
            return id;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public EvaluationOutcome[] getOutcomes() {
            return outcomes;
        }

        public Float getPassRate() {
            return passRate;
        }

        // Helper method to check type
        public boolean hasDataSet() {
            return outcomes != null;
        }
    }

    /**
     * Represents the outcome of evaluating a single data entry
     * within an evaluation run.
     */
    private static class EvaluationOutcome {
        private String id;
        private String errorMessage; // Optional field

        public EvaluationOutcome() {
            // Default constructor for JSON deserialization
        }

        public EvaluationOutcome(String id) {
            this.id = id;
        }

        public EvaluationOutcome(String id, String errorMessage) {
            this.id = id;
            this.errorMessage = errorMessage;
        }

        public String getId() {
            return id;
        }

        public String getErrorMessage() {
            return errorMessage;
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
