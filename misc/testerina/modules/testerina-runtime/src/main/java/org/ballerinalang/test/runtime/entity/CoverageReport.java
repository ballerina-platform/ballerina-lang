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

import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.test.runtime.util.CodeCoverageUtils;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.jacoco.core.analysis.ICounter.FULLY_COVERED;
import static org.jacoco.core.analysis.ICounter.NOT_COVERED;
import static org.jacoco.core.analysis.ICounter.PARTLY_COVERED;

/**
 * Code coverage report generated and analyzed using Jacoco.
 *
 * @since 1.2.0
 */
public class CoverageReport {

    private final String title;

    private Path executionDataFile;
    private Path classesDirectory;
    private final Path projectDir;

    private ExecFileLoader execFileLoader;

    private Path sourceJarPath;
    private String orgName;
    private String moduleName;
    private String version;

    public CoverageReport(Path sourceJarPath, Path targetDirPath, String orgName, String moduleName, String version) {
        this.sourceJarPath = sourceJarPath;
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.version = version;
        this.projectDir = targetDirPath.resolve(TesterinaConstants.COVERAGE_DIR);
        this.title = projectDir.toFile().getName();
        this.classesDirectory = projectDir.resolve(TesterinaConstants.BIN_DIR);
        this.executionDataFile = projectDir.resolve(TesterinaConstants.EXEC_FILE_NAME);
        this.execFileLoader = new ExecFileLoader();
    }

    /**
     * Generates the report.
     *
     * @throws IOException when file operations are failed
     */
    public void generateReport() throws IOException {
        CodeCoverageUtils.unzipCompiledSource(sourceJarPath, projectDir, orgName, moduleName, version);
        execFileLoader.load(executionDataFile.toFile());

        final IBundleCoverage bundleCoverage = analyzeStructure();
        createReport(bundleCoverage);
    }

    private IBundleCoverage analyzeStructure() throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(
                execFileLoader.getExecutionDataStore(), coverageBuilder);
        analyzer.analyzeAll(classesDirectory.toFile());
        return coverageBuilder.getBundle(title);
    }

    private void createReport(final IBundleCoverage bundleCoverage) {
        boolean containsSourceFiles;

        for (IPackageCoverage packageCoverage : bundleCoverage.getPackages()) {
            if (TesterinaConstants.DOT.equals(moduleName)) {
                containsSourceFiles = packageCoverage.getName().isEmpty();
            } else {
                containsSourceFiles = packageCoverage.getName().contains(orgName + "/" + moduleName);
            }
            if (containsSourceFiles) {
                for (ISourceFileCoverage sourceFileCoverage : packageCoverage.getSourceFiles()) {
                    if (sourceFileCoverage.getName().contains(BLangConstants.BLANG_SRC_FILE_SUFFIX) &&
                            !sourceFileCoverage.getName().contains("tests/")) {
                        List<Integer> coveredLines = new ArrayList<>();
                        List<Integer> missedLines = new ArrayList<>();
                        for (int i = sourceFileCoverage.getFirstLine(); i <= sourceFileCoverage.getLastLine(); i++) {
                            ILine line = sourceFileCoverage.getLine(i);
                            if (line.getInstructionCounter().getTotalCount() == 0
                                    && line.getBranchCounter().getTotalCount() == 0) {
                                // do nothing. This is to capture the empty lines
                            } else if ((line.getBranchCounter().getCoveredCount() == 0
                                    && line.getBranchCounter().getMissedCount() > 0)
                                    || line.getStatus() == NOT_COVERED) {
                                missedLines.add(i);
                            } else if (line.getStatus() == PARTLY_COVERED || line.getStatus() == FULLY_COVERED) {
                                coveredLines.add(i);
                            }
                        }
                        ModuleCoverage.getInstance().addSourceFileCoverage(moduleName, sourceFileCoverage.getName(),
                                coveredLines, missedLines);
                    }
                }
            }
        }

    }
}
