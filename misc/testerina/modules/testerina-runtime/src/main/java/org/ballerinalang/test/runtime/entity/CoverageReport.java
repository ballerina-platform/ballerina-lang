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

import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
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

import static io.ballerina.runtime.api.utils.IdentifierUtils.decodeIdentifier;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.BIN_DIR;
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
    private final Path coverageDir;
    private Path executionDataFile;
    private Path classesDirectory;
    private ExecFileLoader execFileLoader;

    private Module module;
    private Target target;

    public CoverageReport(Module module) throws IOException {
        this.module = module;
        this.target = new Target(module.project().sourceRoot());

        this.coverageDir = target.getTestsCachePath().resolve(TesterinaConstants.COVERAGE_DIR);
        this.title = coverageDir.toFile().getName();
        this.classesDirectory = coverageDir.resolve(TesterinaConstants.BIN_DIR);
        this.executionDataFile = coverageDir.resolve(TesterinaConstants.EXEC_FILE_NAME);
        this.execFileLoader = new ExecFileLoader();
    }

    /**
     * Generates the report.
     *
     * @throws IOException when file operations are failed
     */
    public ModuleCoverage generateReport() throws IOException {
        String orgName = this.module.packageInstance().packageOrg().toString();
        String packageName = this.module.packageInstance().packageName().toString();
        String version = this.module.packageInstance().packageVersion().toString();

        Path moduleJarPath = target.cachesPath().resolve(orgName)
                .resolve(packageName)
                .resolve(version).resolve(JvmTarget.JAVA_11.code());
        // Obtain a path list of all the .jar files generated
        List<Path> pathList;
        try (Stream<Path> walk = Files.walk(moduleJarPath, 1)) {
            pathList = walk.filter(f -> f.toString().endsWith(ProjectConstants.BLANG_COMPILED_JAR_EXT)).collect(
                    Collectors.toList());
        } catch (IOException e) {
            return null;
        }

        if (!pathList.isEmpty()) {
            // For each jar file found, we unzip it for this particular module
            for (Path jarPath : pathList) {
                try {
                    // Creates coverage folder with each class per module
                    CodeCoverageUtils.unzipCompiledSource(jarPath, coverageDir, orgName, packageName, version);
                } catch (NoSuchFileException e) {
                    if (Files.exists(coverageDir.resolve(BIN_DIR))) {
                        CodeCoverageUtils.deleteDirectory(coverageDir.resolve(BIN_DIR).toFile());
                    }
                    return null;
                }
            }

            execFileLoader.load(executionDataFile.toFile());
            final IBundleCoverage bundleCoverage = analyzeStructure();

            ModuleCoverage moduleCoverage = new ModuleCoverage();
            createReport(bundleCoverage, moduleCoverage);
            CodeCoverageUtils.deleteDirectory(coverageDir.resolve(BIN_DIR).toFile());
            return moduleCoverage;
        } else {
            String msg = "Unable to generate code coverage for the module " + packageName + ". Jar files dont exist.";
            throw new NoSuchFileException(msg);
        }

    }

    private IBundleCoverage analyzeStructure() throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);
        analyzer.analyzeAll(classesDirectory.toFile());
        return coverageBuilder.getBundle(title);
    }

    private void createReport(final IBundleCoverage bundleCoverage, ModuleCoverage moduleCoverage) {
        boolean containsSourceFiles = true;

        for (IPackageCoverage packageCoverage : bundleCoverage.getPackages()) {
            if (TesterinaConstants.DOT.equals(this.module.moduleName())) {
                containsSourceFiles = packageCoverage.getName().isEmpty();
            }

            if (containsSourceFiles) {
                for (ISourceFileCoverage sourceFileCoverage : packageCoverage.getSourceFiles()) {
                    // Extract the Module name individually for each source file
                    // This is done since some source files come from other modules
                    // sourceFileCoverage : "<orgname>/<moduleName>:<version>
                    String sourceFileModule = decodeIdentifier(sourceFileCoverage.getPackageName().split("/")[1]);

                    // Only add the source files that belong to the same module and it is a source bal file
                    if (sourceFileModule.equals(this.module.moduleName().toString())
                            && sourceFileCoverage.getName().contains(BLANG_SRC_FILE_SUFFIX)
                            && !sourceFileCoverage.getName().contains("tests/")) {
                        List<Integer> coveredLines = new ArrayList<>();
                        List<Integer> missedLines = new ArrayList<>();

                        for (int i = sourceFileCoverage.getFirstLine(); i <= sourceFileCoverage.getLastLine(); i++) {
                            ILine line = sourceFileCoverage.getLine(i);

                            if (line.getInstructionCounter().getTotalCount() == 0 &&
                                    line.getBranchCounter().getTotalCount() == 0) {
                                // do nothing. This is to capture the empty lines
                            } else if ((line.getBranchCounter().getCoveredCount() == 0
                                    && line.getBranchCounter().getMissedCount() > 0)
                                    || line.getStatus() == NOT_COVERED) {
                                missedLines.add(i);
                            } else if (line.getStatus() == PARTLY_COVERED || line.getStatus() == FULLY_COVERED) {
                                coveredLines.add(i);
                            }
                        }
                        Document document = null;
                        for (DocumentId documentId : module.documentIds()) {
                            if (module.document(documentId).name().equals(sourceFileCoverage.getName())) {
                                document = module.document(documentId);
                            }
                        }
                        moduleCoverage.addSourceFileCoverage(document, coveredLines, missedLines);

                    }
                }
            }

        }
    }
}
