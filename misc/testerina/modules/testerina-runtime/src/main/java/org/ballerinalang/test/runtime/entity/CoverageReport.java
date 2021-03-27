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
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PlatformLibrary;
import io.ballerina.projects.PlatformLibraryScope;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.runtime.api.utils.IdentifierUtils;
import org.ballerinalang.test.runtime.util.CodeCoverageUtils;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
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
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

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
    public void generateReport(Map<String, ModuleCoverage> moduleCoverageMap,
                               List<IClassCoverage> packageNativeClassCoverageList,
                               List<IClassCoverage> packageBalClassCoverageList,
                               List<ISourceFileCoverage> packageSourceCoverageList, JBallerinaBackend jBallerinaBackend,
                               String includesInCoverage,
                               List<ExecutionData> packageExecData, List<SessionInfo> sessionInfoList)
            throws IOException {
        String orgName = this.module.packageInstance().packageOrg().toString();
        String packageName = this.module.packageInstance().packageName().toString();
        String version = this.module.packageInstance().packageVersion().toString();

        List<Path> filteredPathList;

        if (!module.testDocumentIds().isEmpty()) {
            filteredPathList =
                    filterPaths(jBallerinaBackend.jarResolver().getJarFilePathsRequiredForTestExecution(
                            this.module.moduleName()), jBallerinaBackend);
        } else {
            filteredPathList = filterPaths(jBallerinaBackend.jarResolver().getJarFilePathsRequiredForExecution(),
                    jBallerinaBackend);
        }

        if (!filteredPathList.isEmpty()) {
            addCompiledSources(filteredPathList, orgName, packageName, version, false,
                    includesInCoverage);
            execFileLoader.load(executionDataFile.toFile());
            final CoverageBuilder coverageBuilder = analyzeStructure();
            // Create Testerina coverage report
            createReport(coverageBuilder.getBundle(title), moduleCoverageMap);
            // Add additional dependency jars for Coverage XML if included
            if (includesInCoverage != null) {
                addCompiledSources(getDependencyJarList(jBallerinaBackend), orgName, packageName, version,
                       true, includesInCoverage);
                execFileLoader.load(executionDataFile.toFile());
                final CoverageBuilder xmlCoverageBuilder = analyzeStructure();
                updatePackageLevelCoverage(packageExecData, sessionInfoList,
                        xmlCoverageBuilder, packageNativeClassCoverageList, packageBalClassCoverageList,
                        packageSourceCoverageList);
            } else {
                updatePackageLevelCoverage(packageExecData, sessionInfoList, coverageBuilder,
                        packageNativeClassCoverageList, packageBalClassCoverageList, packageSourceCoverageList);
            }
            CodeCoverageUtils.deleteDirectory(coverageDir.resolve(BIN_DIR).toFile());
        } else {
            String msg = "Unable to generate code coverage for the module " + packageName + ". Jar files dont exist.";
            throw new NoSuchFileException(msg);
        }
    }

    /**
     * Traverse through the coverageBuilder generated for the current module and update
     * package level information to be used for coverage generation for the ballerina package.
     *
     * @param packagePrefix "orgName"/"pakageName" as String
     * @param packageExecData ExecutionData list for package
     * @param sessionInfoList SessionInfo list for package
     * @param coverageBuilder CoverageBuilder after processing this module
     * @param packageNativeClassCoverageList List of package native IClassCoverage
     * @param packageBalClassCoverageList List of bal IClassCoverage for package
     * @param packageSourceCoverageList List of ISourceFileCoverage for package
     */
    private void updatePackageLevelCoverage(List<ExecutionData> packageExecData,
                                            List<SessionInfo> sessionInfoList, CoverageBuilder coverageBuilder,
                                            List<IClassCoverage> packageNativeClassCoverageList,
                                            List<IClassCoverage> packageBalClassCoverageList,
                                            List<ISourceFileCoverage> packageSourceCoverageList) {
        // Traverse through the class coverages and store only ballerina source file coverages.
        // Native source coverages can be collected by visiting the class coverages.
        for (ISourceFileCoverage sourceFileCoverage : coverageBuilder.getSourceFiles()) {
            if (sourceFileCoverage.getName().endsWith(BLANG_SRC_FILE_SUFFIX)) {
                packageSourceCoverageList.add(sourceFileCoverage);
            }
        }
        // Traverse through all the class coverages and do the following.
        // 1. Add the ballerina class coverages specific for this package to a list to process later.
        //    We need to add duplicated class coverages generated from each module to make sure that the ballerina
        //    coverage information is aggregated correctly for the package.
        // 2. Add the native class coverages to another list to be processed later.
        //    In this case, we need to keep only the last updated class coverage because Jacoco internally aggregates
        //    the coverage for native classes using the exec data in the common binary file.
        for (IClassCoverage classCov : coverageBuilder.getClasses()) {
            if (classCov.getSourceFileName() != null && classCov.getSourceFileName().endsWith(BLANG_SRC_FILE_SUFFIX)) {
                packageBalClassCoverageList.add(classCov);
            } else {
                // Remove old coverage class to keep only the lastest coverage class.
                removeFromCoverageList(packageNativeClassCoverageList, classCov);
                packageNativeClassCoverageList.add(classCov);
            }
        }
        // Update module wise session info to a list, compare and add only unique information.
        for (SessionInfo sessionInfo : execFileLoader.getSessionInfoStore().getInfos()) {
            if (!isExistingSessionInfo(sessionInfoList, sessionInfo)) {
                sessionInfoList.add(sessionInfo);
            }
        }
        // Jacoco is capable of handling duplicated execution data,
        // so it is not needed to remove duplicates.
        for (ExecutionData executionData : execFileLoader.getExecutionDataStore().getContents()) {
            packageExecData.add(executionData);
        }
    }

    private boolean isExistingSessionInfo(List<SessionInfo> sessionInfoList, SessionInfo sessionInfo) {
        for (SessionInfo existingSessionInfo : sessionInfoList) {
            if (existingSessionInfo.compareTo(sessionInfo) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove IClassCoverage from package Class coverage list if it exists already.
     *
     * @param packageClassCoverageList list of IClassCoverage for package
     * @param classCoverage            IClassCoverage to check if already exixts
     */
    private void removeFromCoverageList(List<IClassCoverage> packageClassCoverageList, IClassCoverage classCoverage) {
        boolean isExists = false;
        IClassCoverage coverageToRemove = null;
        for (IClassCoverage coverage : packageClassCoverageList) {
            if (classCoverage.getName().equals(coverage.getName())) {
                //Remove existing coverage class from the list
                isExists = true;
                coverageToRemove = coverage;
            }
        }
        if (isExists && coverageToRemove != null) {
            packageClassCoverageList.remove(coverageToRemove);
        }
    }

    private void addCompiledSources(List<Path> pathList, String orgName, String packageName, String version,
                                    boolean enableIncludesFilter, String includesInCoverage) throws IOException {
        if (!pathList.isEmpty()) {
            // For each jar file found, we unzip it for this particular module
            for (Path jarPath : pathList) {
                try {
                    // Creates coverage folder with each class per module
                    CodeCoverageUtils.unzipCompiledSource(jarPath, coverageDir, orgName, packageName, version,
                            enableIncludesFilter, includesInCoverage);
                } catch (NoSuchFileException e) {
                    if (Files.exists(coverageDir.resolve(BIN_DIR))) {
                        CodeCoverageUtils.deleteDirectory(coverageDir.resolve(BIN_DIR).toFile());
                    }
                    return;
                }
            }
        }
    }

    private CoverageBuilder analyzeStructure() throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);
        analyzer.analyzeAll(classesDirectory.toFile());
        return coverageBuilder;
    }

    private IBundleCoverage getPartialCoverageModifiedBundle(CoverageBuilder coverageBuilder) {
        return new BundleCoverageImpl(title, modifyClasses(coverageBuilder.getClasses()),
                modifySourceFiles(coverageBuilder.getSourceFiles()));
    }

    /**
     * Modify Classes in CoverageBuilder to reflect ballerina source root.
     *
     * @param classesList Collection<IClassCoverage>
     * @return Collection<IClassCoverage>
     */
    private Collection<IClassCoverage> modifyClasses(Collection<IClassCoverage> classesList) {
        Collection<IClassCoverage> modifiedClasses = new ArrayList<>();
        for (IClassCoverage classCoverage : classesList) {
            if (classCoverage.getSourceFileName() != null) {
                if (classCoverage.getSourceFileName().startsWith("tests/")) {
                    continue;
                } else {
                    //Normalize package name and class name for classes generated for bal files
                    IClassCoverage modifiedClassCoverage = new NormalizedCoverageClass(classCoverage,
                            normalizeFileName(classCoverage.getPackageName()),
                            normalizeFileName(classCoverage.getName()));
                    modifiedClasses.add(modifiedClassCoverage);
                }
            } else {
                modifiedClasses.add(classCoverage);
            }
        }
        return modifiedClasses;
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

    private void createReport(final IBundleCoverage bundleCoverage, Map<String, ModuleCoverage> moduleCoverageMap) {
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
                    if (sourceFileCoverage.getPackageName().split("/").length <= 1) {
                        continue;
                    }
                    String sourceFileModule = decodeIdentifier(sourceFileCoverage.getPackageName().split("/")[1]);
                    ModuleCoverage moduleCoverage;
                    if (moduleCoverageMap.containsKey(sourceFileModule)) {
                        moduleCoverage = moduleCoverageMap.get(sourceFileModule);
                    } else {
                        moduleCoverage = new ModuleCoverage();
                    }
                    // If file is a source bal file
                    if (sourceFileCoverage.getName().contains(BLANG_SRC_FILE_SUFFIX) &&
                            !sourceFileCoverage.getName().contains("tests/")) {
                        if (moduleCoverage.containsSourceFile(sourceFileCoverage.getName())) {
                            // Update coverage for missed lines if covered
                            Optional<List<Integer>> missedLinesList = moduleCoverage.getMissedLinesList(
                                    sourceFileCoverage.getName());
                            Optional<List<Integer>> coveredLinesList =
                                    moduleCoverage.getCoveredLinesList(
                                            sourceFileCoverage.getName());
                            if (!missedLinesList.isEmpty() && !coveredLinesList.isEmpty()) {
                                List<Integer> missedLines = missedLinesList.get();
                                List<Integer> coveredLines = coveredLinesList.get();
                                List<Integer> existingMissedLines = new ArrayList<>(missedLines);
                                boolean isCoverageUpdated = false;
                                int updateMissedLineCount = 0;
                                for (int missedLine : existingMissedLines) {
                                    // Traverse through the missed lines of a source file and update
                                    // coverage status if it is covered in the current module.
                                    // This is to make sure multi module tests are reflected in test coverage
                                    ILine line = sourceFileCoverage.getLine(missedLine);
                                    if (line.getStatus() == PARTLY_COVERED || line.getStatus() == FULLY_COVERED) {
                                        isCoverageUpdated = true;
                                        missedLines.remove(Integer.valueOf(missedLine));
                                        coveredLines.add(Integer.valueOf(missedLine));
                                        updateMissedLineCount++;
                                    }
                                }
                                if (isCoverageUpdated) {
                                    // Retrieve relevant document and update the coverage only if there is
                                    // a coverage change
                                    Document document = getDocument(sourceFileCoverage.getName());
                                    if (document != null) {
                                        moduleCoverage.updateCoverage(document, coveredLines, missedLines,
                                                updateMissedLineCount);
                                    }
                                }
                            }
                        } else {
                            // Calculate coverage for new source file only if belongs to current module
                            List<Integer> coveredLines = new ArrayList<>();
                            List<Integer> missedLines = new ArrayList<>();
                            for (int i = sourceFileCoverage.getFirstLine(); i <= sourceFileCoverage.getLastLine();
                                 i++) {
                                ILine line = sourceFileCoverage.getLine(i);
                                if (line.getStatus() == NOT_COVERED) {
                                    missedLines.add(i);
                                } else if (line.getStatus() == PARTLY_COVERED ||
                                        line.getStatus() == FULLY_COVERED) {
                                    coveredLines.add(i);
                                }
                            }
                            Document document = getDocument(sourceFileCoverage.getName());
                            if (document != null) {
                                moduleCoverage.addSourceFileCoverage(document, coveredLines,
                                        missedLines);
                            }
                            moduleCoverageMap.put(sourceFileModule, moduleCoverage);

                        }

                    }
                }
            }
        }
    }

    private List<Path> filterPaths(Collection<JarLibrary> pathCollection, JBallerinaBackend jBallerinaBackend) {
        List<Path> filteredPathList = new ArrayList<>();
        List<Path> exclusionPathList = getExclusionJarList(jBallerinaBackend);
        for (JarLibrary library : pathCollection) {
            Path path = library.path();
            if (!exclusionPathList.contains(path)) {
                filteredPathList.add(path);
            }
        }
        return filteredPathList;
    }

    /**
     * Retrieve relevant document for provided file name.
     *
     * @param sourceFileName String
     * @return Document
     */
    private Document getDocument(String sourceFileName) {
        Document document = null;
        for (Module moduleInstance : module.packageInstance().modules()) {
            document = getDocumentFromModule(moduleInstance, sourceFileName);
            if (document != null) {
                break;
            }
        }
        return document;
    }

    /**
     * Retrieve relevant document from a module if exists.
     *
     * @param moduleInstance Module
     * @param sourceFileName String
     * @return Document
     */
    private Document getDocumentFromModule(Module moduleInstance, String sourceFileName) {
        Document document = null;
        for (DocumentId documentId : moduleInstance.documentIds()) {
            if (moduleInstance.document(documentId).name().equals(sourceFileName)) {
                document = moduleInstance.document(documentId);
                break;
            }
        }
        return document;
    }

    private List<Path> getExclusionJarList(JBallerinaBackend jBallerinaBackend) {
        List<Path> exclusionPathList = new ArrayList<>(getDependencyJarList(jBallerinaBackend));
        exclusionPathList.add(jBallerinaBackend.runtimeLibrary().path());
        for (JarLibrary library : ProjectUtils.testDependencies()) {
            exclusionPathList.add(library.path());
        }
        return exclusionPathList;
    }

    private List<Path> getDependencyJarList(JBallerinaBackend jBallerinaBackend) {
        List<Path> dependencyPathList = new ArrayList<>();
        module.packageInstance().getResolution().allDependencies()
                .stream()
                .map(ResolvedPackageDependency::packageInstance)
                .forEach(pkg -> {
                    for (ModuleId dependencyModuleId : pkg.moduleIds()) {
                        Module dependencyModule = pkg.module(dependencyModuleId);
                        PlatformLibrary generatedJarLibrary = jBallerinaBackend.codeGeneratedLibrary(
                                pkg.packageId(), dependencyModule.moduleName());
                        if (!dependencyPathList.contains(generatedJarLibrary.path())) {
                            dependencyPathList.add(generatedJarLibrary.path());
                        }
                    }
                    Collection<PlatformLibrary> otherJarDependencies = jBallerinaBackend.platformLibraryDependencies(
                            pkg.packageId(), PlatformLibraryScope.DEFAULT);
                    for (PlatformLibrary otherJarDependency : otherJarDependencies) {
                        if (!dependencyPathList.contains(otherJarDependency.path())) {
                            dependencyPathList.add(otherJarDependency.path());
                        }
                    }
                });
        return dependencyPathList;
    }

    private Collection<ISourceFileCoverage> modifySourceFiles(Collection<ISourceFileCoverage> sourcefiles) {
        Collection<ISourceFileCoverage> modifiedSourceFiles = new ArrayList<>();
        for (ISourceFileCoverage sourcefile : sourcefiles) {
            ISourceFileCoverage modifiedSourceFile;
            List<ILine> modifiedLines;
            if (sourcefile.getName().endsWith(BLANG_SRC_FILE_SUFFIX)) {
                if (sourcefile.getName().startsWith("tests/")) {
                    continue;
                } else {
                    modifiedLines = modifyLines(sourcefile);
                    //Normalize source file package name
                    modifiedSourceFile = new PartialCoverageModifiedSourceFile(sourcefile,
                            modifiedLines, normalizeFileName(sourcefile.getPackageName()));
                    modifiedSourceFiles.add(modifiedSourceFile);
                }
            } else {
                modifiedSourceFiles.add(sourcefile);
            }

        }
        return modifiedSourceFiles;
    }

    private String normalizeFileName(String fileName) {
        String orgName = IdentifierUtils.encodeNonFunctionIdentifier(
                this.module.packageInstance().packageOrg().toString());
        //Get package instance and traverse through all the modules
        for (Module module : this.module.packageInstance().modules()) {
            String packageName = IdentifierUtils.encodeNonFunctionIdentifier(
                    module.moduleName().toString());
            String sourceRoot = module.project().sourceRoot().getFileName().toString();
            if (!module.isDefaultModule()) {
                sourceRoot = sourceRoot + "/" + ProjectConstants.MODULES_ROOT + "/" +
                        module.moduleName().moduleNamePart();
            }
            if (fileName.contains(orgName + "/" + packageName + "/")) {
                //Escape special characters before using in regex
                orgName = Pattern.quote(orgName);
                packageName = Pattern.quote(packageName);
                // Capture file paths with the format "orgName/packageName/xxxx/file-name" and replace with
                // "<source-root>/file-name"
                String normalizedFileName = fileName.replaceAll("^" + orgName + "/" +
                        packageName + "/.*/", sourceRoot + "/");
                // Capture remaining file paths with the format "orgName/packageName/file-name" and replace
                // with "<source-root>"
                normalizedFileName = normalizedFileName.replaceAll("^" + orgName + "/" +
                        packageName + "/.*", sourceRoot);
                return normalizedFileName;
            }
        }
        return fileName;
    }

    private List<ILine> modifyLines(ISourceFileCoverage sourcefile) {
        List<ILine> modifiedLines = new ArrayList<>();
        for (int i = sourcefile.getFirstLine(); i <= sourcefile.getLastLine(); i++) {
            ILine line = sourcefile.getLine(i);
            ILine modifiedLine = new PartialCoverageModifiedLine(line.getInstructionCounter(), line.getBranchCounter());
            modifiedLines.add(modifiedLine);
        }
        return modifiedLines;
    }

}
