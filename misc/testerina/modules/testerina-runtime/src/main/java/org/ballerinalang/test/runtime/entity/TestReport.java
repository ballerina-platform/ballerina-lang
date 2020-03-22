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
 * Final test report with coverage (if enabled).
 *
 * @since 1.2.0
 */
public class TestReport {
    private String projectName;
    private String orgName;

    // attributes related to overall test results summary
    private int totalTests;
    private int passed;
    private int failed;
    private int skipped;

    // attributes related to overall test coverage summary
    private int coveredLines;
    private int missedLines;
    private float coveragePercentage;

    private List<ModuleStatus> moduleStatus = new ArrayList<>();
    private List<ModuleCoverage> moduleCoverage = new ArrayList<>();

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<ModuleStatus> getModuleStatus() {
        return moduleStatus;
    }

    public void setModuleStatus(List<ModuleStatus> moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    public void addModuleStatus(String moduleName, ModuleStatus status) {
        status.setName(moduleName);
        this.moduleStatus.add(status);
    }

    public List<ModuleCoverage> getModuleCoverage() {
        return moduleCoverage;
    }

    public void setModuleCoverage(List<ModuleCoverage> moduleCoverage) {
        this.moduleCoverage = moduleCoverage;
    }

    public void addCoverage(String moduleName, ModuleCoverage coverage) {
        coverage.setName(moduleName);
        this.moduleCoverage.add(coverage);
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
        for (ModuleStatus modStatus : moduleStatus) {
            passed += modStatus.getPassed();
            failed += modStatus.getFailed();
            skipped += modStatus.getSkipped();
            totalTests = passed + failed + skipped;
        }
        if (coverage) {
            for (ModuleCoverage modCov : moduleCoverage) {
                coveredLines += modCov.getCoveredLines();
                missedLines += modCov.getMissedLines();
                coveragePercentage = (float) coveredLines / (coveredLines + missedLines) * 100;

            }
        }
    }

    public float getCoveragePercentage() {
        return coveragePercentage;
    }

    public int getTotalTests() {
        return totalTests;
    }

}
