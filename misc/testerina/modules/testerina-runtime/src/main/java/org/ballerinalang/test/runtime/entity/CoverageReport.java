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
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.test.runtime.util.TesterinaConstants.BLANG_SRC_FILE_SUFFIX;
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
    private final Path projectDir;
    private final Path jarCache;
    private final Path jsonCache;
    private Path executionDataFile;
    private Path classesDirectory;
    private ExecFileLoader execFileLoader;

    private String orgName;
    private String moduleName;
    private String version;

    public CoverageReport(Path targetDirPath, String orgName, String moduleName, String version) {
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.version = version;
        this.projectDir = targetDirPath.resolve(TesterinaConstants.COVERAGE_DIR);
        this.title = projectDir.toFile().getName();
        this.classesDirectory = projectDir.resolve(TesterinaConstants.BIN_DIR);
        this.executionDataFile = projectDir.resolve(TesterinaConstants.EXEC_FILE_NAME);
        this.execFileLoader = new ExecFileLoader();

        this.jarCache = targetDirPath.resolve("caches").resolve("jar_cache").resolve(orgName);
        this.jsonCache = targetDirPath.resolve("caches").resolve("json_cache");
    }

    /**
     * Generates the report.
     *
     * @throws IOException when file operations are failed
     */
    public void generateReport() throws IOException {

        // Obtain a path list of all the .jar files generated
        List<Path> pathList;
        try (Stream<Path> walk = Files.walk(this.jarCache, TesterinaConstants.FILE_DEPTH)) {
            pathList = walk.map(path -> path).filter(f -> f.toString().endsWith(version + ".jar")).collect(
                    Collectors.toList());
        } catch (IOException e) {
            return;
        }

        if (!pathList.isEmpty()) {
            // For each jar file found, we unzip it for this particular module
            for (Path moduleJarPath : pathList) {
                try {
                    // Creates coverage folder with each class per module
                    CodeCoverageUtils.unzipCompiledSource(moduleJarPath, projectDir, orgName, moduleName, version);
                } catch (NoSuchFileException e) {
                    return;
                }
            }

            execFileLoader.load(executionDataFile.toFile());
            final IBundleCoverage bundleCoverage = analyzeStructure();
            createReport(bundleCoverage);
        } else {
            String msg = "Unable to generate code coverage for the module " + moduleName + ". Jar files dont exist.";
            throw new NoSuchFileException(msg);
        }

    }

    private IBundleCoverage analyzeStructure() throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);
        analyzer.analyzeAll(classesDirectory.toFile());
        return coverageBuilder.getBundle(title);
    }

    private void createReport(final IBundleCoverage bundleCoverage) {
        boolean containsSourceFiles = true;

        for (IPackageCoverage packageCoverage : bundleCoverage.getPackages()) {
            if (TesterinaConstants.DOT.equals(moduleName)) {
                containsSourceFiles = packageCoverage.getName().isEmpty();
            }

            if (containsSourceFiles) {
                for (ISourceFileCoverage sourceFileCoverage : packageCoverage.getSourceFiles()) {
                    // Extract the Module name individually for each source file
                    // This is done since some source files come from other modules
                    // sourceFileCoverage : "<orgname>/<moduleName>:<version>
                    String sourceFileModule = sourceFileCoverage.getPackageName().split("/")[1];

                    if (sourceFileCoverage.getName().contains(BLANG_SRC_FILE_SUFFIX)
                            && !sourceFileCoverage.getName().contains("tests/")) {
                        List<Integer> coveredLines = new ArrayList<>();
                        List<Integer> missedLines = new ArrayList<>();

                        for (int i = sourceFileCoverage.getFirstLine(); i <= sourceFileCoverage.getLastLine(); i++) {
                            ILine line = sourceFileCoverage.getLine(i);

                            if (line.getInstructionCounter().getTotalCount() == 0 &&
                                    line.getBranchCounter().getTotalCount() == 0) {
                                // do nothing. This is to capture the empty lines
                            } else if ((line.getBranchCounter().getCoveredCount() == 0 &&
                                    line.getBranchCounter().getMissedCount() > 0) || line.getStatus() == NOT_COVERED) {
                                missedLines.add(i);
                            } else if (line.getStatus() == PARTLY_COVERED || line.getStatus() == FULLY_COVERED) {
                                coveredLines.add(i);
                            }
                        }

                        // Only add the source files that belong to the same module
                        if (sourceFileModule.equals(moduleName.replace(".", "_"))) {
                            ModuleCoverage.getInstance().addSourceFileCoverage(moduleName,
                                    sourceFileCoverage.getName(), coveredLines, missedLines);
                        } else {
                            // Disabling temporarily till support for modules with '.' is implemented
//                            // <org>/<modulename>/<version>
//                            ModuleCoverage.getInstance().updateSourceFileCoverage(jsonCachePath, sourceFileModule,
//                                    sourceFileCoverage.getName(), coveredLines, missedLines);
                        }
                    }
                }
            }

        }
    }

    private String resolveSourcePackage(String sourcePackage) {
        String[] split = sourcePackage.split("/");
        String resolvedSource = "";

        // Replace version value only
        split[split.length - 1] = split[split.length - 1].replace("_", ".");

        for (String str : split) {
            resolvedSource += "/" + str;
        }

        return resolvedSource;
    }

}
