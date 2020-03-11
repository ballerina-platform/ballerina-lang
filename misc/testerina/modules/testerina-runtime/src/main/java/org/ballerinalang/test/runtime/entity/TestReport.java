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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Final test report with coverage (if enabled).
 *
 * @since 1.2.0
 */
public class TestReport {
    private String projectName;
    private String orgName;

    // attributes related to overall test results summary
    private int passed;
    private int failed;
    private int skipped;

    // attributes related to overall test coverage summary
    private int coveredLines;
    private int missedLines;
    private float coveragePercentage;

    private Map<String, ModuleStatus> moduleStatus = new HashMap<>();
    private Map<String, ModuleCoverage> moduleCoverage = new HashMap<>();

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Map<String, ModuleStatus> getModuleStatus() {
        return moduleStatus;
    }

    public void setModuleStatus(Map<String, ModuleStatus> moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    public void addModuleStatus(String moduleName, ModuleStatus status) {
        this.moduleStatus.put(moduleName, status);
    }

    public Map<String, ModuleCoverage> getModuleCoverage() {
        return moduleCoverage;
    }

    public void setModuleCoverage(Map<String, ModuleCoverage> moduleCoverage) {
        this.moduleCoverage = moduleCoverage;
    }

    public void addCoverage(String moduleName, ModuleCoverage coverage) {
        this.moduleCoverage.put(moduleName, coverage);
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgName() {
        return orgName;
    }

    /**
     * Calculates the project level results summary by getting the totals and averages.
     *
     * @param coverage if coverage is enabled or not
     */
    public void finalizeTestResults(boolean coverage) {
        for (Map.Entry<String, ModuleStatus> modStatus : moduleStatus.entrySet()) {
            passed += modStatus.getValue().getPassed();
            failed += modStatus.getValue().getFailed();
            skipped += modStatus.getValue().getSkipped();

        }
        if (coverage) {
            for (Map.Entry<String, ModuleCoverage> modCov : moduleCoverage.entrySet()) {
                coveredLines += modCov.getValue().getCoveredLines();
                missedLines += modCov.getValue().getMissedLines();
                coveragePercentage = (float) coveredLines / (coveredLines + missedLines) * 100;

            }
        }
    }

    public float getCoveragePercentage() {
        return coveragePercentage;
    }

    /**
     * Inner class for excluding coverage information if coverage is not enabled.
     * This class should be accessible from RunTestsTask when dumping the report to a json file.
     */
    public static class ReportExclusionStrategy implements ExclusionStrategy {

        public boolean shouldSkipClass(Class<?> arg0) {
            return false;
        }

        public boolean shouldSkipField(FieldAttributes f) {

            return (f.getDeclaringClass() == TestReport.class && f.getName().equals("coveredLines"))
                    || (f.getDeclaringClass() == TestReport.class && f.getName().equals("missedLines"))
                    || (f.getDeclaringClass() == TestReport.class && f.getName().equals("coveragePercentage"))
                    || (f.getDeclaringClass() == TestReport.class && f.getName().equals("moduleCoverage"));
        }

    }
}
