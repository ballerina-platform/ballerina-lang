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
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.internal.analysis.BundleCoverageImpl;
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
            final CoverageBuilder coverageBuilder = analyzeStructure();
            IBundleCoverage bundleCoverage = coverageBuilder.getBundle(title);
            ModuleCoverage moduleCoverage = new ModuleCoverage();
            createReport(bundleCoverage, moduleCoverage);
//            bundleCoverage = getModifiedBundle(coverageBuilder);
            createXMLReport(bundleCoverage);
            CodeCoverageUtils.deleteDirectory(coverageDir.resolve(BIN_DIR).toFile());
            return moduleCoverage;
        } else {
            String msg = "Unable to generate code coverage for the module " + packageName + ". Jar files dont exist.";
            throw new NoSuchFileException(msg);
        }
    }

    private CoverageBuilder analyzeStructure() throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);
        analyzer.analyzeAll(classesDirectory.toFile());
        return coverageBuilder;
    }

    private IBundleCoverage getModifiedBundle(CoverageBuilder coverageBuilder) {
        final Collection<ISourceFileCoverage> sourcefiles = coverageBuilder.getSourceFiles();
        final Collection<ISourceFileCoverage> modifiedSourceFiles = modifySources(sourcefiles);
        final IBundleCoverage bundleCoverage = new BundleCoverageImpl(title, coverageBuilder.getClasses(),
                modifiedSourceFiles);
        return bundleCoverage;
    }

    private void createXMLReport(IBundleCoverage bundleCoverage) throws IOException {
        final XMLFormatter xmlFormatter = new XMLFormatter();
        final File reportFile = new File(target.getReportPath().resolve(
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

    public Collection<ISourceFileCoverage> modifySources(Collection<ISourceFileCoverage> sourcefiles) {
        Collection<ISourceFileCoverage> modifiedSourceFiles = new ArrayList<>();
        for (ISourceFileCoverage sourcefile : sourcefiles) {
            List<ILine> modifiedLines = new ArrayList<>();
            for (int i = sourcefile.getFirstLine(); i <= sourcefile.getLastLine(); i++) {
                ILine line = sourcefile.getLine(i);
                ILine modifiedLine = new ILine() {
                    private final CustomCounterImpl instructions = new CustomCounterImpl(line.getInstructionCounter());
                    private final CustomCounterImpl branches = new CustomCounterImpl(line.getBranchCounter());

                    @Override
                    public ICounter getInstructionCounter() {
                        return instructions;
                    }

                    @Override
                    public ICounter getBranchCounter() {
                        return branches;
                    }

                    @Override
                    public int getStatus() {
                        return branches.getStatus() | instructions.getStatus();
                    }
                };
                modifiedLines.add(modifiedLine);
            }
            ISourceFileCoverage modifiedSourceFile = new ISourceFileCoverage() {
                @Override
                public String getPackageName() {
                    return sourcefile.getPackageName();
                }

                @Override
                public int getFirstLine() {
                    return sourcefile.getFirstLine();
                }

                @Override
                public int getLastLine() {
                    return sourcefile.getLastLine();
                }

                //Return the modified lines instead of lines stored in the original source file
                @Override
                public ILine getLine(int nr) {
                    if (modifiedLines.size() == 0 || nr < getFirstLine() || nr > getLastLine()) {
                        return sourcefile.getLine(nr);
                    }
                    ILine reqLine = modifiedLines.get(nr - getFirstLine());
                    return reqLine == null ? sourcefile.getLine(nr) : reqLine;
                }

                @Override
                public ElementType getElementType() {
                    return sourcefile.getElementType();
                }

                @Override
                public String getName() {
                    return sourcefile.getName();
                }

                @Override
                public ICounter getInstructionCounter() {
                    return sourcefile.getInstructionCounter();
                }

                @Override
                public ICounter getBranchCounter() {
                    return sourcefile.getBranchCounter();
                }

                @Override
                public ICounter getLineCounter() {
                    return sourcefile.getLineCounter();
                }

                @Override
                public ICounter getComplexityCounter() {
                    return sourcefile.getComplexityCounter();
                }

                @Override
                public ICounter getMethodCounter() {
                    return sourcefile.getMethodCounter();
                }

                @Override
                public ICounter getClassCounter() {
                    return sourcefile.getClassCounter();
                }

                @Override
                public ICounter getCounter(CounterEntity entity) {
                    return sourcefile.getCounter(entity);
                }

                @Override
                public boolean containsCode() {
                    return sourcefile.containsCode();
                }

                @Override
                public ICoverageNode getPlainCopy() {
                    return sourcefile.getPlainCopy();
                }
            };
            modifiedSourceFiles.add(modifiedSourceFile);
        }
        return modifiedSourceFiles;
    }

    private class CustomCounterImpl implements ICounter {
        private int covered;
        private int missed;

        public CustomCounterImpl(ICounter prevCounter) {
            this.covered = prevCounter.getCoveredCount();
            this.missed = prevCounter.getMissedCount();
            modifyCoverageNumbers();
        }

        //Modify the covered and missed numbers in cases the counter status is calculated as PARTLY_COVERED
        //It converts the counter status to FULLY_COVERED
        public void modifyCoverageNumbers() {
            if (getStatus() == PARTLY_COVERED) {
                covered = covered + missed;
                missed = 0;
            }
        }

        @Override
        public double getValue(CounterValue value) {
            switch (value) {
                case TOTALCOUNT:
                    return getTotalCount();
                case MISSEDCOUNT:
                    return getMissedCount();
                case COVEREDCOUNT:
                    return getCoveredCount();
                case MISSEDRATIO:
                    return getMissedRatio();
                case COVEREDRATIO:
                    return getCoveredRatio();
                default:
                    throw new AssertionError(value);
            }
        }

        @Override
        public int getTotalCount() {
            return covered + missed;
        }

        @Override
        public int getCoveredCount() {
            return covered;
        }

        @Override
        public int getMissedCount() {
            return missed;
        }

        @Override
        public double getCoveredRatio() {
            return (double) covered / (missed + covered);
        }

        @Override
        public double getMissedRatio() {
            return (double) missed / (missed + covered);
        }

        @Override
        public int getStatus() {
            int status = covered > 0 ? FULLY_COVERED : EMPTY;
            if (missed > 0) {
                status |= NOT_COVERED;
            }
            return status;
        }
    }
}
