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
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.Module;
import io.ballerina.projects.internal.model.Target;
import org.ballerinalang.test.runtime.util.CodeCoverageUtils;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.xml.XMLFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.ballerina.runtime.api.utils.IdentifierUtils.decodeIdentifier;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.BIN_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.BLANG_SRC_FILE_SUFFIX;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_XML_FILE;
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
    public ModuleCoverage generateReport(JarResolver jarResolver) throws IOException {
        String orgName = this.module.packageInstance().packageOrg().toString();
        String packageName = this.module.packageInstance().packageName().toString();
        String version = this.module.packageInstance().packageVersion().toString();

        List<Path> filteredPathList;

        if (!module.testDocumentIds().isEmpty()) {
            filteredPathList =
                    filterPaths(jarResolver.getJarFilePathsRequiredForTestExecution(this.module.moduleName()));
        } else {
            filteredPathList =
                    filterPaths(jarResolver.getJarFilePathsRequiredForExecution());
        }

        if (!filteredPathList.isEmpty()) {
            // For each jar file found, we unzip it for this particular module
            for (Path jarPath : filteredPathList) {
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

            createXMLReport(bundleCoverage);
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

    private void createXMLReport(IBundleCoverage bundleCoverage) throws IOException {
            XMLFormatter xmlFormatter = new XMLFormatter();
            File reportFile = new File(target.getReportPath().resolve(
                    this.module.moduleName().toString()).resolve(REPORT_XML_FILE).toString());
            reportFile.getParentFile().mkdirs();

            try (FileOutputStream fileOutputStream = new FileOutputStream(reportFile)) {
                IReportVisitor visitor = xmlFormatter.createVisitor(fileOutputStream);
                visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
                        execFileLoader.getExecutionDataStore().getContents());

                visitor.visitBundle(bundleCoverage, null);

                visitor.visitEnd();
            }
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
                            if (line.getStatus() == NOT_COVERED) {
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

    private List<Path> filterPaths(Collection<Path> pathCollection) {
        List<Path> filteredPathList = new ArrayList<>();

        for (Path path : pathCollection) {
            if (path.toString().contains(this.module.project().sourceRoot().toString()) &&
                    path.toString().contains(target.cachesPath().toString())) {
                filteredPathList.add(path);
            }
        }

        return filteredPathList;
    }
}
