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
import io.ballerina.projects.Package;
import io.ballerina.projects.PlatformLibrary;
import io.ballerina.projects.PlatformLibraryScope;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectUtils;
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
import org.jacoco.core.tools.ExecFileLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.identifier.Utils.decodeIdentifier;
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
    private Map<String, ModuleCoverage> moduleCoverageMap;
    private List<IClassCoverage> packageNativeClassCoverageList;
    private List<IClassCoverage> packageBalClassCoverageList;
    private List<ISourceFileCoverage> packageSourceCoverageList;
    private List<ExecutionData> packageExecData;
    private List<SessionInfo> sessionInfoList;

    public CoverageReport(Module module, Map<String, ModuleCoverage> moduleCoverageMap,
                          List<IClassCoverage> packageNativeClassCoverageList,
                          List<IClassCoverage> packageBalClassCoverageList,
                          List<ISourceFileCoverage> packageSourceCoverageList, List<ExecutionData> packageExecData,
                          List<SessionInfo> sessionInfoList) throws IOException {
        this.module = module;
        this.target = new Target(module.project().targetDir());
        this.coverageDir = target.getTestsCachePath().resolve(TesterinaConstants.COVERAGE_DIR);
        this.title = coverageDir.toFile().getName();
        this.classesDirectory = coverageDir.resolve(TesterinaConstants.BIN_DIR);
        this.executionDataFile = coverageDir.resolve(TesterinaConstants.EXEC_FILE_NAME);
        this.execFileLoader = new ExecFileLoader();
        this.moduleCoverageMap = moduleCoverageMap;
        this.packageNativeClassCoverageList = packageNativeClassCoverageList;
        this.packageBalClassCoverageList = packageBalClassCoverageList;
        this.packageSourceCoverageList = packageSourceCoverageList;
        this.packageExecData = packageExecData;
        this.sessionInfoList = sessionInfoList;
    }

    /**
     * Generates the testerina coverage report.
     *
     * @param jBallerinaBackend JBallerinaBackend
     * @param includesInCoverage boolean
     * @throws IOException
     */
    public void generateReport(JBallerinaBackend jBallerinaBackend, String includesInCoverage,
                               String reportFormat)
            throws IOException {
        String orgName = this.module.packageInstance().packageOrg().toString();
        String packageName = this.module.packageInstance().packageName().toString();

        List<Path> filteredPathList;

        if (!module.testDocumentIds().isEmpty()) {
            filteredPathList =
                    filterPaths(jBallerinaBackend.jarResolver().getJarFilePathsRequiredForTestExecution(
                            this.module.moduleName()), jBallerinaBackend, module.packageInstance());
        } else {
            filteredPathList = filterPaths(jBallerinaBackend.jarResolver().getJarFilePathsRequiredForExecution(),
                    jBallerinaBackend, module.packageInstance());
        }
        if (!filteredPathList.isEmpty()) {
            CoverageBuilder coverageBuilder = generateTesterinaCoverageReport(orgName, packageName, filteredPathList);
            if (CodeCoverageUtils.isRequestedReportFormat(reportFormat, TesterinaConstants.JACOCO_XML_FORMAT)) {
                // Add additional dependency jars for Jacoco Coverage XML if included
                if (includesInCoverage != null) {
                    List<Path> dependencyPathList = getDependenciesForJacocoXML(jBallerinaBackend);
                    addCompiledSources(dependencyPathList, orgName, packageName, includesInCoverage);
                    execFileLoader.load(executionDataFile.toFile());
                    final CoverageBuilder xmlCoverageBuilder = analyzeStructure();
                    updatePackageLevelCoverage(xmlCoverageBuilder);
                } else {
                    updatePackageLevelCoverage(coverageBuilder);
                }
            }
            CodeCoverageUtils.deleteDirectory(coverageDir.resolve(BIN_DIR).toFile());
        } else {
            String msg = "Unable to generate code coverage for the module " + packageName + ". Jar files dont exist.";
            throw new NoSuchFileException(msg);
        }
    }

    /**
     * Return the required dependencies for Jacoco XML creation.
     *
     * @param jBallerinaBackend JBallerinaBackend
     * @return List of Path
     */
    private List<Path> getDependenciesForJacocoXML(JBallerinaBackend jBallerinaBackend) {
        List<Path> dependencyPathList = getDependencyJarList(jBallerinaBackend);
        List<Path> platformLibsList = getPlatformLibsList(jBallerinaBackend, module.packageInstance());
        for (Path otherDependencyPath : platformLibsList) {
            if (!dependencyPathList.contains(otherDependencyPath)) {
                dependencyPathList.add(otherDependencyPath);
            }
        }
        return dependencyPathList;
    }

    /**
     * Generate the json coverage report for Testerina.
     *
     * @param orgName package org name
     * @param packageName package name
     * @param filteredPathList List of the extracted source path
     * @return CoverageBuilder
     * @throws IOException
     */
    private CoverageBuilder generateTesterinaCoverageReport(String orgName, String packageName,
                                                            List<Path> filteredPathList) throws IOException {
        // For the Testerina report only the ballerina specific sources need to be extracted
        addCompiledSources(filteredPathList, orgName, packageName);
        execFileLoader.load(executionDataFile.toFile());
        final CoverageBuilder coverageBuilder = analyzeStructure();
        // Create Testerina coverage report
        createReport(coverageBuilder.getBundle(title), moduleCoverageMap);
        return coverageBuilder;
    }

    /**
     * Traverse through the coverageBuilder generated for the current module and update
     * package level information to be used for coverage generation for the ballerina package.
     *
     * @param coverageBuilder CoverageBuilder after processing this module
     */
    private void updatePackageLevelCoverage(CoverageBuilder coverageBuilder) {
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
                removeFromCoverageList(classCov);
                packageNativeClassCoverageList.add(classCov);
            }
        }
        // Update module wise session info to a list, compare and add only unique information.
        for (SessionInfo sessionInfo : execFileLoader.getSessionInfoStore().getInfos()) {
            if (!isExistingSessionInfo(sessionInfo)) {
                sessionInfoList.add(sessionInfo);
            }
        }
        // Jacoco is capable of handling duplicated execution data,
        // so it is not needed to remove duplicates.
        for (ExecutionData executionData : execFileLoader.getExecutionDataStore().getContents()) {
            packageExecData.add(executionData);
        }
    }

    private boolean isExistingSessionInfo(SessionInfo sessionInfo) {
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
     * @param classCoverage            IClassCoverage to check if already exixts
     */
    private void removeFromCoverageList(IClassCoverage classCoverage) {
        boolean isExists = false;
        IClassCoverage coverageToRemove = null;
        for (IClassCoverage coverage : packageNativeClassCoverageList) {
            if (classCoverage.getName().equals(coverage.getName())) {
                //Remove existing coverage class from the list
                isExists = true;
                coverageToRemove = coverage;
            }
        }
        if (isExists && coverageToRemove != null) {
            packageNativeClassCoverageList.remove(coverageToRemove);
        }
    }

    private void addCompiledSources(List<Path> pathList, String orgName, String packageName)
            throws IOException {
        if (!pathList.isEmpty()) {
            // For each jar file found, we unzip it for this particular module
            for (Path jarPath : pathList) {
                try {
                    // Creates coverage folder with each class per module
                    CodeCoverageUtils.unzipCompiledSource(jarPath, coverageDir.resolve(BIN_DIR),
                            orgName, packageName, false, null);
                } catch (NoSuchFileException e) {
                    if (Files.exists(coverageDir.resolve(BIN_DIR))) {
                        CodeCoverageUtils.deleteDirectory(coverageDir.resolve(BIN_DIR).toFile());
                    }
                    return;
                }
            }
        }
    }

    private void addCompiledSources(List<Path> pathList, String orgName, String packageName,
                                    String includesInCoverage) throws IOException {
        if (!pathList.isEmpty()) {
            // For each jar file found, we unzip it for this particular module
            for (Path jarPath : pathList) {
                try {
                    // Creates coverage folder with each class per module
                    CodeCoverageUtils.unzipCompiledSource(jarPath, coverageDir.resolve(BIN_DIR), orgName, packageName,
                            true, includesInCoverage);
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

    private List<Path> filterPaths(Collection<JarLibrary> pathCollection, JBallerinaBackend jBallerinaBackend,
                                   Package pkg) {
        List<Path> filteredPathList = new ArrayList<>();
        List<Path> exclusionPathList = getExclusionJarList(jBallerinaBackend, pkg);
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

    private List<Path> getExclusionJarList(JBallerinaBackend jBallerinaBackend, Package pkg) {
        List<Path> exclusionPathList = new ArrayList<>(getDependencyJarList(jBallerinaBackend));
        exclusionPathList.add(jBallerinaBackend.runtimeLibrary().path());
        for (JarLibrary library : ProjectUtils.testDependencies()) {
            exclusionPathList.add(library.path());
        }
        // Add platform libs of this package to exclusion jar list
        exclusionPathList.addAll(getPlatformLibsList(jBallerinaBackend, pkg));
        return exclusionPathList;
    }

    private List<Path> getPlatformLibsList(JBallerinaBackend jBallerinaBackend, Package pkg) {
        List<Path> platformLibsList = new ArrayList<>();
        Collection<PlatformLibrary> otherJarDependencies = jBallerinaBackend.platformLibraryDependencies(
                pkg.packageId(), PlatformLibraryScope.DEFAULT);
        for (PlatformLibrary otherJarDependency : otherJarDependencies) {
            if (!platformLibsList.contains(otherJarDependency.path())) {
                platformLibsList.add(otherJarDependency.path());
            }
        };
        return platformLibsList;
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

}
