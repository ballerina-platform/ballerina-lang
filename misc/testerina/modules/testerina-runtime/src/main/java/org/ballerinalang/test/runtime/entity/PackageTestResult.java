/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
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
package org.ballerinalang.test.runtime.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Package entity for test report to match test_result.json format.
 *
 * @since 2201.13.0
 */
public class PackageTestResult {
    private String projectName;
    private int totalTests;
    private int passed;
    private int failed;
    private int skipped;
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

    public int getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }

    public int getPassed() {
        return passed;
    }

    public void setPassed(int passed) {
        this.passed = passed;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public int getCoveredLines() {
        return coveredLines;
    }

    public void setCoveredLines(int coveredLines) {
        this.coveredLines = coveredLines;
    }

    public int getMissedLines() {
        return missedLines;
    }

    public void setMissedLines(int missedLines) {
        this.missedLines = missedLines;
    }

    public float getCoveragePercentage() {
        return coveragePercentage;
    }

    public void setCoveragePercentage(float coveragePercentage) {
        this.coveragePercentage = coveragePercentage;
    }

    public List<ModuleStatus> getModuleStatus() {
        return moduleStatus;
    }

    public void setModuleStatus(List<ModuleStatus> moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    public void addModuleStatus(ModuleStatus status) {
        this.moduleStatus.add(status);
    }

    public List<ModuleCoverage> getModuleCoverage() {
        return moduleCoverage;
    }

    public void setModuleCoverage(List<ModuleCoverage> moduleCoverage) {
        this.moduleCoverage = moduleCoverage;
    }

    public void addModuleCoverage(ModuleCoverage coverage) {
        if (coverage != null) {
            this.moduleCoverage.add(coverage);
        }
    }
}
