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

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
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
import java.util.Set;

import static io.ballerina.identifier.Utils.decodeIdentifier;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.BIN_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.BLANG_SRC_FILE_SUFFIX;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.PATH_SEPARATOR;
import static org.jacoco.core.analysis.ICounter.EMPTY;
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
    private final Path executionDataFile;
    private final Path classesDirectory;
    private final ExecFileLoader execFileLoader;
    private final Module module;
    private final Map<String, ModuleCoverage> moduleCoverageMap;
    private final List<IClassCoverage> packageNativeClassCoverageList;
    private final List<IClassCoverage> packageBalClassCoverageList;
    private final List<ISourceFileCoverage> packageSourceCoverageList;
    private final List<ExecutionData> packageExecData;
    private final List<SessionInfo> sessionInfoList;

    public CoverageReport(Module module, Map<String, ModuleCoverage> moduleCoverageMap,
                          List<IClassCoverage> packageNativeClassCoverageList,
                          List<IClassCoverage> packageBalClassCoverageList,
                          List<ISourceFileCoverage> packageSourceCoverageList, List<ExecutionData> packageExecData,
                          List<SessionInfo> sessionInfoList) throws IOException {
        this.module = module;
        Target target = new Target(module.project().targetDir());
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
     * @param jBallerinaBackend  JBallerinaBackend
     * @param includesInCoverage boolean
     * @param exclusionClassList list of classes to be excluded
     * @throws IOException if an error occurs while generating the report
     */
    public void generateReport(JBallerinaBackend jBallerinaBackend, String includesInCoverage,
                               String reportFormat, Module originalModule, Set<String> exclusionClassList)
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
            CoverageBuilder coverageBuilder = generateTesterinaCoverageReport(orgName, packageName,
                                                filteredPathList, originalModule, exclusionClassList);
            if (CodeCoverageUtils.isRequestedReportFormat(reportFormat, TesterinaConstants.JACOCO_XML_FORMAT)) {
                // Add additional dependency jars for Jacoco Coverage XML if included
                if (includesInCoverage != null) {
                    List<Path> dependencyPathList = getDependenciesForJacocoXML(jBallerinaBackend);
                    addCompiledSources(dependencyPathList, orgName, packageName, includesInCoverage,
                            exclusionClassList);
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
     * @param orgName            package org name
     * @param packageName        package name
     * @param filteredPathList   List of the extracted source path
     * @param originalModule     Module
     * @param exclusionClassList list of classes to be excluded
     * @return CoverageBuilder
     * @throws IOException if an error occurs while generating the report
     */
    private CoverageBuilder generateTesterinaCoverageReport(String orgName, String packageName,
                                                            List<Path> filteredPathList,
                                                            Module originalModule, Set<String> exclusionClassList)
                                                            throws IOException {
        // For the Testerina report only the ballerina specific sources need to be extracted
        addCompiledSources(filteredPathList, orgName, packageName, exclusionClassList);
        execFileLoader.load(executionDataFile.toFile());
        final CoverageBuilder coverageBuilder = analyzeStructure();
        List<DocumentId> excludedFiles = getGeneratedFilesToExclude(originalModule);
        createReport(coverageBuilder.getBundle(title), moduleCoverageMap, excludedFiles, exclusionClassList);
        filterGeneratedCoverage(moduleCoverageMap, originalModule);
        return coverageBuilder;
    }

    private List<DocumentId> getGeneratedFilesToExclude(Module originalModules) {
        List<DocumentId> excludedDocumentIds = new ArrayList<>(this.module.documentIds());
        List<DocumentId> oldDocumentIds = new ArrayList<>(originalModules.documentIds());
        excludedDocumentIds.removeAll(oldDocumentIds);
        return excludedDocumentIds;
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
        packageExecData.addAll(execFileLoader.getExecutionDataStore().getContents());
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
     * @param classCoverage IClassCoverage to check if already exixts
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
        if (isExists) {
            packageNativeClassCoverageList.remove(coverageToRemove);
        }
    }

    private void addCompiledSources(List<Path> pathList, String orgName, String packageName, Set<String>
            exclusionClassList) throws IOException {
        if (!pathList.isEmpty()) {
            // For each jar file found, we unzip it for this particular module
            for (Path jarPath : pathList) {
                try {
                    // Creates coverage folder with each class per module
                    CodeCoverageUtils.unzipCompiledSource(jarPath, coverageDir.resolve(BIN_DIR),
                            orgName, packageName, false, null, exclusionClassList);
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
                                    String includesInCoverage, Set<String> exclusionClassList) throws IOException {
        if (!pathList.isEmpty()) {
            // For each jar file found, we unzip it for this particular module
            for (Path jarPath : pathList) {
                try {
                    // Creates coverage folder with each class per module
                    CodeCoverageUtils.unzipCompiledSource(jarPath, coverageDir.resolve(BIN_DIR), orgName, packageName,
                            true, includesInCoverage, exclusionClassList);
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
        createReport(bundleCoverage, moduleCoverageMap, null, null);
    }

    private void createReport(final IBundleCoverage bundleCoverage, Map<String, ModuleCoverage> moduleCoverageMap,
                              List<DocumentId> exclusionList, Set<String> exclusionClassList) {
        boolean containsSourceFiles = true;

        for (IPackageCoverage packageCoverage : bundleCoverage.getPackages()) {

            if (TesterinaConstants.DOT.equals(this.module.moduleName().toString())) {
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
                    Document document = getDocument(sourceFileModule, sourceFileCoverage.getName());
                    // If the document exists in the exclusion list then we continue on with the loop
                    if (document != null && exclusionList.contains(document.documentId())) {
                        continue;
                    }
                    // If file is a source bal file
                    if (sourceFileCoverage.getName().contains(BLANG_SRC_FILE_SUFFIX) &&
                            !sourceFileCoverage.getName().contains("tests/")) {
                        String exclusionClassFileName = sourceFileCoverage.getPackageName() + PATH_SEPARATOR +
                                sourceFileCoverage.getName().replace(BLANG_SRC_FILE_SUFFIX, "");
                        exclusionClassFileName = exclusionClassFileName.replace(PATH_SEPARATOR, DOT);
                        if (exclusionClassList.contains(exclusionClassFileName)) {
                            continue;
                        }
                        if (moduleCoverage.containsSourceFile(sourceFileCoverage.getName())) {
                            // Update coverage for missed lines if covered
                            Optional<List<Integer>> missedLinesList = moduleCoverage.getMissedLinesList(
                                    sourceFileCoverage.getName());
                            Optional<List<Integer>> coveredLinesList =
                                    moduleCoverage.getCoveredLinesList(
                                            sourceFileCoverage.getName());
                            if (missedLinesList.isPresent() && coveredLinesList.isPresent()) {
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
                                        missedLines.remove(missedLine);
                                        coveredLines.add(missedLine);
                                        updateMissedLineCount++;
                                    }
                                }
                                if (isCoverageUpdated) {
                                    // Update the coverage only if there is a coverage change
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

    private void filterGeneratedCoverage(Map<String, ModuleCoverage> moduleCoverageMap, Module originalModule) {

        for (DocumentId documentId : originalModule.documentIds()) {
            Document originalDocument = originalModule.document(documentId);
            Document modifiedDocument = this.module.document(originalDocument.documentId());

            if (originalDocument.equals(modifiedDocument)) {
                continue;
            }

            try {
                // Use diff utils to analyze the text lines in the doc
                Patch<String> stringPatch = DiffUtils.diff(originalDocument.textDocument().textLines(),
                        modifiedDocument.textDocument().textLines());
                if (!stringPatch.getDeltas().isEmpty()) {
                    List<AbstractDelta<String>> patchDeltas = stringPatch.getDeltas();
                    List<Integer> insertedLines = new ArrayList<>();
                    List<Integer> modifiedLines = new ArrayList<>();
                    List<Integer> deletedLines = new ArrayList<>();
                    for (AbstractDelta<String> delta : patchDeltas) {
                        // This means that we have to consider the block added
                        if (delta.getType().equals(DeltaType.INSERT)) {
                            int lineNumber = delta.getTarget().getPosition();
                            int size = delta.getTarget().size();
                            for (int i = lineNumber; i < lineNumber + size; i++) {
                                insertedLines.add(i);
                            }
                        } else if (delta.getType().equals(DeltaType.CHANGE)) {
                            int lineNumber = delta.getTarget().getPosition();
                            // Add blank lines to modified lines
                            if (delta.getSource().getLines().get(0).isBlank()) {
                                modifiedLines.add(lineNumber);
                            }
                        } else if (delta.getType().equals(DeltaType.DELETE)) {
                            int lineNumber = delta.getSource().getPosition();
                            int size = delta.getSource().size();
                            for (int i = lineNumber; i < lineNumber + size; i++) {
                                deletedLines.add(i);
                            }
                        }
                    }

                    ModuleCoverage moduleCoverage = moduleCoverageMap.get(originalModule.moduleName().toString());
                    List<Integer> coveredLinesList = moduleCoverage.getCoveredLinesList(modifiedDocument.name()).get();
                    List<Integer> missedLinesList = moduleCoverage.getMissedLinesList(modifiedDocument.name()).get();

                    // create the modified document coverage list
                    List<Integer> modifiedDocLineStatus = new ArrayList<>();
                    for (int i = 0; i < modifiedDocument.textDocument().textLines().size(); i++) {
                        if (coveredLinesList.contains(i + 1)) {
                            modifiedDocLineStatus.add(FULLY_COVERED);
                        } else if (missedLinesList.contains(i + 1)) {
                            modifiedDocLineStatus.add(NOT_COVERED);
                        } else {
                            modifiedDocLineStatus.add(EMPTY);
                        }
                    }

                    // iterate the modified coverage list and map to the original document
                    List<Integer> originalDocLineStatus = new ArrayList<>();
                    for (int i = 0; i < modifiedDocument.textDocument().textLines().size(); i++) {
                        while (deletedLines.contains(originalDocLineStatus.size() + 1)) {
                            // if the next line is deleted in the source, we add the line status as empty
                            originalDocLineStatus.add(EMPTY);
                        }
                        if (modifiedLines.contains(i)) {
                            // if the line is modified, we add the line status as empty
                            originalDocLineStatus.add(EMPTY);
                        } else if (!insertedLines.contains(i)) {
                            // if the line is not modified, nor inserted, we add the line status as it is
                            originalDocLineStatus.add(modifiedDocLineStatus.get(i));
                        }
                        // if the line is inserted, we ignore it
                    }

                    List<Integer> newCoveredLines = new ArrayList<>();
                    List<Integer> newMissedLines = new ArrayList<>();

                    // Go through line status and get the new covered and missed lines
                    for (int i = 0; i < originalDocLineStatus.size(); i++) {
                        if (originalDocLineStatus.get(i).equals(FULLY_COVERED)) {
                            newCoveredLines.add(i + 1);
                        } else if (originalDocLineStatus.get(i).equals(NOT_COVERED)) {
                            newMissedLines.add(i + 1);
                        }
                    }

                    // Remove previous source file module and replace it with new module coverage
                    moduleCoverageMap.remove(originalModule.moduleName().toString());
                    moduleCoverage.replaceCoverage(originalDocument, newCoveredLines, newMissedLines);
                    moduleCoverageMap.put(originalModule.moduleName().toString(), moduleCoverage);
                }

            } catch (DiffException | NullPointerException e) {
                // Diff exception caught when diff cannot be calculated properly
                // NullPointer caught when a Generated Source File is passed or if its an empty file
                // continue to consider other files in the coverage
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
    private Document getDocument(String moduleName, String sourceFileName) {
        Document document = null;
        for (Module moduleInstance : module.packageInstance().modules()) {
            if (moduleInstance.moduleName().toString().equals(moduleName)) {
                document = getDocumentFromModule(moduleInstance, sourceFileName);
                if (document != null) {
                    break;
                }
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
        otherJarDependencies.addAll(jBallerinaBackend.platformLibraryDependencies(
                pkg.packageId(), PlatformLibraryScope.PROVIDED));
        for (PlatformLibrary otherJarDependency : otherJarDependencies) {
            if (!platformLibsList.contains(otherJarDependency.path())) {
                platformLibsList.add(otherJarDependency.path());
            }
        }
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
                    otherJarDependencies.addAll(jBallerinaBackend.platformLibraryDependencies(
                            pkg.packageId(), PlatformLibraryScope.PROVIDED));
                    for (PlatformLibrary otherJarDependency : otherJarDependencies) {
                        if (!dependencyPathList.contains(otherJarDependency.path())) {
                            dependencyPathList.add(otherJarDependency.path());
                        }
                    }

                });
        return dependencyPathList;
    }
}
