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

import org.ballerinalang.test.runtime.util.TesterinaConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Final test report with coverage (if enabled) matching test_result.json format.
 *
 * @since 1.2.0
 */
public class TestReport {
    private String workspaceName;

    // attributes related to overall test results summary
    private int totalTests;
    private int passed;
    private int failed;
    private int skipped;

    // attributes related to overall test coverage summary
    private int coveredLines;
    private int missedLines;
    private float coveragePercentage;

    private final List<PackageTestResult> packages = new ArrayList<>();

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public List<PackageTestResult> getPackages() {
        return packages;
    }

    public void addPackage(PackageTestResult packageObj) {
        this.packages.add(packageObj);
    }

    public int getTotalTests() {
        return totalTests;
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

    public int getCoveredLines() {
        return coveredLines;
    }

    public int getMissedLines() {
        return missedLines;
    }

    public float getCoveragePercentage() {
        return coveragePercentage;
    }


    /**
     * Calculates the project level results summary by getting the totals and averages.
     *
     * @param coverage if coverage is enabled or not
     */
    public void finalizeTestResults(boolean coverage) {
        int totalTestsSum = 0;
        int passedSum = 0;
        int failedSum = 0;
        int skippedSum = 0;
        int coveredLinesSum = 0;
        int missedLinesSum = 0;

        for (PackageTestResult pkg : packages) {
            // Calculate package-level totals from its modules
            int pkgTotalTests = 0;
            int pkgPassed = 0;
            int pkgFailed = 0;
            int pkgSkipped = 0;
            int pkgCoveredLines = 0;
            int pkgMissedLines = 0;

            for (ModuleStatus modStatus : pkg.getModuleStatus()) {
                if (TesterinaConstants.DOT.equals(modStatus.getName())) {
                    modStatus.setName(pkg.getProjectName());
                }
                pkgPassed += modStatus.getPassed();
                pkgFailed += modStatus.getFailed();
                pkgSkipped += modStatus.getSkipped();
                pkgTotalTests = pkgPassed + pkgFailed + pkgSkipped;
            }

            if (coverage) {
                for (ModuleCoverage modCov : pkg.getModuleCoverage()) {
                    if (TesterinaConstants.DOT.equals(modCov.getName())) {
                        modCov.setName(pkg.getProjectName());
                    }
                    pkgCoveredLines += modCov.getCoveredLines();
                    pkgMissedLines += modCov.getMissedLines();
                }
                if (pkgCoveredLines + pkgMissedLines > 0) {
                    float pkgCoverageVal = (float) pkgCoveredLines / (pkgCoveredLines + pkgMissedLines) * 100;
                    pkg.setCoveragePercentage((float) (Math.round(pkgCoverageVal * 100.0) / 100.0));
                }
            }

            // Set package-level totals
            pkg.setTotalTests(pkgTotalTests);
            pkg.setPassed(pkgPassed);
            pkg.setFailed(pkgFailed);
            pkg.setSkipped(pkgSkipped);
            pkg.setCoveredLines(pkgCoveredLines);
            pkg.setMissedLines(pkgMissedLines);

            // Add to project-level totals
            totalTestsSum += pkgTotalTests;
            passedSum += pkgPassed;
            failedSum += pkgFailed;
            skippedSum += pkgSkipped;
            coveredLinesSum += pkgCoveredLines;
            missedLinesSum += pkgMissedLines;

            // For each module coverage, check if there is a module status as well
            for (ModuleCoverage modCov : pkg.getModuleCoverage()) {
                boolean doesExist = false;
                for (ModuleStatus modStatus : pkg.getModuleStatus()) {
                    if (modCov.getName().equals(modStatus.getName())) {
                        doesExist = true;
                        break;
                    }
                }
                if (!doesExist) {
                    ModuleStatus missingModuleStatus = new ModuleStatus();
                    missingModuleStatus.setName(modCov.getName());
                    pkg.addModuleStatus(missingModuleStatus);
                }
            }

            // sort the module list to be in the alphabetical order
            pkg.setModuleStatus(pkg.getModuleStatus().stream()
                    .sorted(Comparator.comparing(ModuleStatus::getName))
                    .toList());

            pkg.setModuleCoverage(pkg.getModuleCoverage().stream()
                    .sorted(Comparator.comparing(ModuleCoverage::getName))
                    .toList());
        }

        // Set project-level totals
        this.totalTests = totalTestsSum;
        this.passed = passedSum;
        this.failed = failedSum;
        this.skipped = skippedSum;
        this.coveredLines = coveredLinesSum;
        this.missedLines = missedLinesSum;

        if (coverage && (coveredLinesSum + missedLinesSum > 0)) {
            float coverageVal = (float) coveredLinesSum / (coveredLinesSum + missedLinesSum) * 100;
            this.coveragePercentage = (float) (Math.round(coverageVal * 100.0) / 100.0);
        }

        // Sort packages alphabetically
        packages.sort(Comparator.comparing(PackageTestResult::getProjectName));
    }
}
