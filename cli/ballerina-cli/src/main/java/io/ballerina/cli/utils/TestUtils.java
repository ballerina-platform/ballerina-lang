/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.utils;

import com.google.gson.Gson;
import io.ballerina.cli.launcher.LauncherUtils;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PlatformLibrary;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.test.runtime.entity.CoverageReport;
import org.ballerinalang.test.runtime.entity.ModuleCoverage;
import org.ballerinalang.test.runtime.entity.ModuleStatus;
import org.ballerinalang.test.runtime.entity.TestReport;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.test.runtime.util.CodeCoverageUtils;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.ballerinalang.testerina.core.TestProcessor;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.COVERAGE_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.FILE_PROTOCOL;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_FN_DELIMITER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_LEGACY_DELIMITER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_DATA_PLACEHOLDER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_ZIP_NAME;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RERUN_TEST_JSON_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RESULTS_HTML_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RESULTS_JSON_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.TOOLS_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.USER_DIR;

/**
 * Utility functions and classes for Run Test Task and Create Test Executable Task.
 *
 * @since 2.3.0
 */
public final class TestUtils {

    private TestUtils() {
    }

    public static void generateCoverage(Project project, TestReport testReport, JBallerinaBackend jBallerinaBackend,
                                        String includesInCoverage, String coverageReportFormat,
                                        Map<String, Module> coverageModules, Set<String> exclusionClassList)
            throws IOException {
        // Generate code coverage
        if (!project.buildOptions().codeCoverage()) {
            return;
        }
        if (testReport == null) { // This to avoid the spotbugs failure.
            return;
        }

        Map<String, ModuleCoverage> moduleCoverageMap = initializeCoverageMap(project);
        // Following lists will hold the coverage information needed for the coverage XML file generation.
        List<ISourceFileCoverage> packageSourceCoverageList = new ArrayList<>();
        List<IClassCoverage> packageNativeClassCoverageList = new ArrayList<>();
        List<IClassCoverage> packageBalClassCoverageList = new ArrayList<>();
        List<ExecutionData> packageExecData = new ArrayList<>();
        List<SessionInfo> packageSessionInfo = new ArrayList<>();
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            CoverageReport coverageReport = new CoverageReport(module, moduleCoverageMap,
                    packageNativeClassCoverageList, packageBalClassCoverageList, packageSourceCoverageList,
                    packageExecData, packageSessionInfo);
            coverageReport.generateReport(jBallerinaBackend, includesInCoverage, coverageReportFormat,
                    coverageModules.get(module.moduleName().toString()), exclusionClassList);
        }
        // Traverse coverage map and add module wise coverage to test report
        for (var mapElement : moduleCoverageMap.entrySet()) {
            String moduleName = mapElement.getKey();
            ModuleCoverage moduleCoverage = mapElement.getValue();
            testReport.addCoverage(moduleName, moduleCoverage);
        }
        if (CodeCoverageUtils.isRequestedReportFormat(coverageReportFormat,
                TesterinaConstants.JACOCO_XML_FORMAT)) {
            // Generate coverage XML report
            CodeCoverageUtils.createXMLReport(project, packageExecData, packageNativeClassCoverageList,
                    packageBalClassCoverageList, packageSourceCoverageList, packageSessionInfo);
        }
    }

    /**
     * Initialize coverage map used for aggregating module wise coverage.
     *
     * @param project Project
     * @return Map<String, ModuleCoverage>
     */
    private static Map<String, ModuleCoverage> initializeCoverageMap(Project project) {
        Map<String, ModuleCoverage> moduleCoverageMap = new HashMap<>();
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            moduleCoverageMap.put(module.moduleName().toString(), new ModuleCoverage());
        }
        return moduleCoverageMap;
    }

    /**
     * Write the test report content into testerina report formats(json and html).
     *
     * @param out        PrintStream object to print messages to console
     * @param testReport Data that are parsed to the json
     */
    public static void generateTesterinaReports(Project project, TestReport testReport, PrintStream out, Target target)
            throws IOException {
        if (!project.buildOptions().testReport() && !project.buildOptions().codeCoverage()) {
            return;
        }
        if (testReport.getModuleStatus().size() <= 0) {
            return;
        }

        out.println();
        out.println("Generating Test Report");

        Path reportDir = target.getReportPath();

        // Set projectName in test report
        String projectName;
        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            projectName = ProjectUtils.getJarFileName(project.currentPackage().getDefaultModule())
                    + ProjectConstants.BLANG_SOURCE_EXT;
        } else {
            projectName = project.currentPackage().packageName().toString();
        }
        testReport.setProjectName(projectName);
        testReport.finalizeTestResults(project.buildOptions().codeCoverage());

        Gson gson = new Gson();
        String json = gson.toJson(testReport).replace("\\(", "(");

        File jsonFile = new File(reportDir.resolve(RESULTS_JSON_FILE).toString());
        try (FileOutputStream fileOutputStream = new FileOutputStream(jsonFile)) {
            try (Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
                out.println("\t" + jsonFile.toPath() + "\n");
            }
        }

        // Dump the Testerina html report only if '--test-report' flag is provided
        if (project.buildOptions().testReport()) {
            Path reportZipPath = getReportToolsPath();
            if (Files.exists(reportZipPath)) {
                String content;
                try {
                    try (FileInputStream fileInputStream = new FileInputStream(reportZipPath.toFile())) {
                        CodeCoverageUtils.unzipReportResources(fileInputStream, reportDir.toFile());
                    }
                    content = Files.readString(reportDir.resolve(RESULTS_HTML_FILE));
                    content = content.replace(REPORT_DATA_PLACEHOLDER, json);
                } catch (IOException e) {
                    throw createLauncherException("error occurred while preparing test report: " + e);
                }
                File htmlFile = new File(reportDir.resolve(RESULTS_HTML_FILE).toString());
                try (FileOutputStream fileOutputStream = new FileOutputStream(htmlFile)) {
                    try (Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                        writer.write(new String(content.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
                        out.println("\tView the test report at: " +
                                FILE_PROTOCOL + Paths.get(htmlFile.getPath()).toAbsolutePath().normalize());
                    }
                }
            } else {
                String reportToolsPath = "<" + BALLERINA_HOME + ">" + File.separator + BALLERINA_HOME_LIB +
                        File.separator + TOOLS_DIR_NAME + File.separator + COVERAGE_DIR + File.separator +
                        REPORT_ZIP_NAME;
                out.println("warning: Could not find the required HTML report tools for code coverage at "
                        + reportToolsPath);
            }
        }
    }

    /**
     * Get the path of the report tools template from Ballerina home.
     *
     * @return path of the report tools template
     */
    public static Path getReportToolsPath() {
        return Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_LIB).
                resolve(TesterinaConstants.TOOLS_DIR_NAME).resolve(TesterinaConstants.COVERAGE_DIR).
                resolve(REPORT_ZIP_NAME);
    }

    /**
     * Loads the ModuleStatus object by reading a given Json.
     *
     * @param statusJsonPath file path of json file
     * @return ModuleStatus object
     * @throws IOException if file does not exist
     */
    public static ModuleStatus loadModuleStatusFromFile(Path statusJsonPath) throws IOException {
        File statusJsonFile = new File(statusJsonPath.toUri());
        if (!statusJsonFile.isFile()) {
            return null;
        }
        Gson gson = new Gson();
        BufferedReader bufferedReader = Files.newBufferedReader(statusJsonPath, StandardCharsets.UTF_8);
        return gson.fromJson(bufferedReader, ModuleStatus.class);
    }

    /**
     * Write the content of each test suite into a common json.
     */
    public static void writeToTestSuiteJson(Map<String, TestSuite> testSuiteMap, Path testsCachePath) {
        if (!Files.exists(testsCachePath)) {
            try {
                Files.createDirectories(testsCachePath);
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException("couldn't create test cache directories : " + e);
            }
        }

        Path jsonFilePath = getJsonFilePath(testsCachePath);
        File jsonFile = new File(jsonFilePath.toString());
        try (FileOutputStream fileOutputStream = new FileOutputStream(jsonFile)) {
            try (Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                String json = gson.toJson(testSuiteMap);
                writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException("couldn't write data to test suite file : " + e);
            }
        } catch (IOException e) {
            throw LauncherUtils.createLauncherException("couldn't write data to test suite file : " + e);
        }
    }

    /**
     * Create test suites for the project.
     * @param project Project
     * @param target Target
     * @param testProcessor Test processor to create test suites
     * @param testSuiteMap  Test suite map that is used to store test suites
     * @param moduleNamesList   List of module names that will be created
     * @param mockClassNames    List of mock class names that will be created
     * @param isRerunTestExecution  Whether to rerun test execution
     * @param report    Whether to report
     * @param coverage  Whether to generate coverage
     * @return                    Whether the project has tests
     */
    public static boolean createTestSuitesForProject(Project project, Target target, TestProcessor testProcessor,
                                                     Map<String, TestSuite> testSuiteMap, List<String> moduleNamesList,
                                                     List<String> mockClassNames, boolean isRerunTestExecution,
                                                     boolean report, boolean coverage) {
        boolean hasTests = false;
        for (ModuleDescriptor moduleDescriptor :
                project.currentPackage().moduleDependencyGraph().toTopologicallySortedList()) {
            Module module = project.currentPackage().module(moduleDescriptor.name());
            ModuleName moduleName = module.moduleName();

            TestSuite suite = testProcessor.testSuite(module).orElse(null);
            if (suite == null) {
                continue;
            }

            hasTests = true;

            if (!isRerunTestExecution) {
                clearFailedTestsJson(target.path());
            }
            if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                Optional<Path> sourceRootFileName = Optional.ofNullable(project.sourceRoot().getFileName());
                if (sourceRootFileName.isPresent()) {
                    suite.setSourceFileName(sourceRootFileName.get().toString());
                } else {
                    throw new IllegalStateException("Source root file name is not present");
                }
            }
            suite.setReportRequired(report || coverage);
            String resolvedModuleName = getResolvedModuleName(module, moduleName);
            testSuiteMap.put(resolvedModuleName, suite);
            moduleNamesList.add(resolvedModuleName);

            addMockClasses(suite, mockClassNames);
        }
        return hasTests;
    }

    /**
     * Get the json file path for the test suite.
     * @param testsCachePath    Path to the tests cache
     * @return                  Path to the json file
     */
    public static Path getJsonFilePath(Path testsCachePath) {
        return Paths.get(testsCachePath.toString(), TesterinaConstants.TESTERINA_TEST_SUITE);
    }

    public static String getJsonFilePathInFatJar(String separator) {
        return ProjectConstants.CACHES_DIR_NAME
                + separator + ProjectConstants.TESTS_CACHE_DIR_NAME
                + separator + ProjectConstants.TEST_SUITE_JSON;
    }

    public static void clearFailedTestsJson(Path targetPath) {
        Path rerunTestJsonPath = Paths.get(targetPath.toString(), RERUN_TEST_JSON_FILE);
        if (Files.exists(rerunTestJsonPath)) {
            try {
                Files.delete(rerunTestJsonPath);
            } catch (IOException e) {
                throw createLauncherException("error while clearing failed tests : ", e);
            }
        }
    }

    public static void cleanTempCache(Project project, Path cachesRoot) {
        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            ProjectUtils.deleteDirectory(cachesRoot);
        }
    }

    /**
     * Get the jacoco agent jar path.
     *
     * @return jacoco agent jar path
     */
    public static String getJacocoAgentJarPath() {
        return Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                .resolve(BALLERINA_HOME_LIB).resolve(TesterinaConstants.AGENT_FILE_NAME).toString();
    }

    /**
     * Get the initial command arguments for the test execution.
     * @param javaCommand   Specific java command to use
     * @param userDir       User directory for the out of memory heap dump
     * @return              List of command arguments to be used
     */
    public static List<String> getInitialCmdArgs(String javaCommand, String userDir) {
        List<String> cmdArgs = new ArrayList<>();

        if (javaCommand == null) {
            cmdArgs.add(System.getProperty("java.command"));
        } else {
            cmdArgs.add(javaCommand);
        }

        cmdArgs.add("-XX:+HeapDumpOnOutOfMemoryError");

        if (userDir == null) {
            cmdArgs.add("-XX:HeapDumpPath=" + System.getProperty(USER_DIR));
        } else {
            cmdArgs.add("-XX:HeapDumpPath=" + userDir);
        }

        return cmdArgs;
    }

    /**
     * Append the required arguments to the command arguments list.
     * @param cmdArgs               List of command arguments to append to
     * @param target                Target
     * @param jacocoAgentJarPath    Jacoco agent jar path
     * @param testSuiteJsonPath     Test suite json path
     * @param report                Whether to report
     * @param coverage              Whether to generate coverage
     * @param groupList             Group list
     * @param disableGroupList      Disable group list
     * @param singleExecTests       Single execution tests
     * @param isRerunTestExecution  Whether to rerun test execution
     * @param listGroups            Whether to list groups
     * @param cliArgs               List of cli arguments
     * @param isFatJarExecution     Whether to execute from a fat jar
     */
    public static void appendRequiredArgs(List<String> cmdArgs, String target, String jacocoAgentJarPath,
                                          String testSuiteJsonPath, boolean report,
                                          boolean coverage, String groupList, String disableGroupList,
                                          String singleExecTests, boolean isRerunTestExecution,
                                          boolean listGroups, List<String> cliArgs, boolean isFatJarExecution,
                                          boolean isParallelExecution) {

        cmdArgs.add(Boolean.toString(isFatJarExecution));
        cmdArgs.add(testSuiteJsonPath);
        cmdArgs.add(target);
        cmdArgs.add(jacocoAgentJarPath);
        cmdArgs.add(Boolean.toString(report));
        cmdArgs.add(Boolean.toString(coverage));
        cmdArgs.add(groupList != null ? groupList : "");
        cmdArgs.add(disableGroupList != null ? disableGroupList : "");
        cmdArgs.add(singleExecTests != null ? singleExecTests : "");
        cmdArgs.add(Boolean.toString(isRerunTestExecution));
        cmdArgs.add(Boolean.toString(listGroups));
        cmdArgs.add(Boolean.toString(isParallelExecution));
        cmdArgs.addAll(cliArgs);
    }

    public static String getResolvedModuleName(Module module, ModuleName moduleName) {
        return module.isDefaultModule() ? moduleName.toString() : module.moduleName().moduleNamePart();
    }

    /**
     * Add the mock classes of the test suite to the list of mock class names.
     * @param suite            TestSuite to get the mock classes from
     * @param mockClassNames    List of mock class names
     */
    public static void addMockClasses(TestSuite suite, List<String> mockClassNames) {
        Map<String, String> mockFunctionMap = suite.getMockFunctionNamesMap();
        for (Map.Entry<String, String> entry : mockFunctionMap.entrySet()) {
            String key = entry.getKey();
            String functionToMockClassName;
            // Find the first delimiter and compare the indexes
            // The first index should always be a delimiter. Which ever one that is denotes the mocking type
            functionToMockClassName = getFunctionToMockClassName(key);
            mockClassNames.add(functionToMockClassName);
        }
    }

    private static String getFunctionToMockClassName(String id) {
        String functionToMockClassName;
        if (!id.contains(MOCK_LEGACY_DELIMITER)) {
            functionToMockClassName = id.substring(0, id.indexOf(MOCK_FN_DELIMITER));
        } else if (!id.contains(MOCK_FN_DELIMITER)) {
            functionToMockClassName = id.substring(0, id.indexOf(MOCK_LEGACY_DELIMITER));
        } else {
            if (id.indexOf(MOCK_FN_DELIMITER) < id.indexOf(MOCK_LEGACY_DELIMITER)) {
                functionToMockClassName = id.substring(0, id.indexOf(MOCK_FN_DELIMITER));
            } else {
                functionToMockClassName = id.substring(0, id.indexOf(MOCK_LEGACY_DELIMITER));
            }
        }
        return functionToMockClassName;
    }

    /**
     * Get the classpath for the test execution.
     * @param jBallerinaBackend JBallerinaBackend
     * @param currentPackage    Package
     * @return String containing the classpath
     */
    public static String getClassPath(JBallerinaBackend jBallerinaBackend, Package currentPackage) {
        JarResolver jarResolver = jBallerinaBackend.jarResolver();

        List<Path> dependencies = getTestDependencyPaths(currentPackage, jarResolver);

        List<Path> jarList = getModuleJarPaths(jBallerinaBackend, currentPackage);
        dependencies.removeAll(jarList);

        StringJoiner classPath = joinClassPaths(dependencies);
        return classPath.toString();
    }

    /**
     * Join the list of paths to a single string.
     * @param dependencies  List of paths
     * @return            StringJoiner containing the joined paths
     */
    public static StringJoiner joinClassPaths(List<Path> dependencies) {
        StringJoiner classPath = new StringJoiner(File.pathSeparator);
        dependencies.stream().map(Path::toString).forEach(classPath::add);
        return classPath;
    }

    /**
     * Get the dependencies required for test execution.
     * @param currentPackage    Package
     * @param jarResolver       JarResolver to get the jar paths
     * @return                List of paths
     */
    public static List<Path> getTestDependencyPaths(Package currentPackage, JarResolver jarResolver) {
        List<Path> dependencies = new ArrayList<>();
        for (ModuleId moduleId : currentPackage.moduleIds()) {
            Module module = currentPackage.module(moduleId);

            // Skip getting file paths for execution if module doesnt contain a testable jar
            if (!module.testDocumentIds().isEmpty() || module.project().kind()
                    .equals(ProjectKind.SINGLE_FILE_PROJECT)) {
                for (JarLibrary jarLibs : jarResolver.getJarFilePathsRequiredForTestExecution(module.moduleName())) {
                    dependencies.add(jarLibs.path());
                }
            }
        }
        return dependencies.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Get the jar paths that should be excluded from the classpath.
     * @param jBallerinaBackend JBallerinaBackend
     * @param currentPackage    Package
     * @return               List of paths to be excluded
     */
    public static List<Path> getModuleJarPaths(JBallerinaBackend jBallerinaBackend, Package currentPackage) {
        List<Path> moduleJarPaths = new ArrayList<>();

        for (ModuleId moduleId : currentPackage.moduleIds()) {
            Module module = currentPackage.module(moduleId);

            moduleJarPaths.addAll(getModuleJarPathsForModule(currentPackage, jBallerinaBackend, module));
        }

        for (ResolvedPackageDependency resolvedPackageDependency : currentPackage.getResolution().allDependencies()) {
            Package pkg = resolvedPackageDependency.packageInstance();
            for (ModuleId moduleId : pkg.moduleIds()) {
                Module module = pkg.module(moduleId);
                moduleJarPaths.add(
                        jBallerinaBackend.codeGeneratedLibrary(pkg.packageId(), module.moduleName()).path());
            }
        }

        return moduleJarPaths.stream().distinct().collect(Collectors.toList());
    }

    private static PlatformLibrary getCodeGeneratedTestLibrary(JBallerinaBackend jBallerinaBackend,
                                                               Package currentPackage, Module module) {
        return jBallerinaBackend.codeGeneratedTestLibrary(
                currentPackage.packageId(), module.moduleName());
    }

    private static PlatformLibrary getPlatformLibrary(JBallerinaBackend jBallerinaBackend,
                                                      Package currentPackage, Module module) {
        return jBallerinaBackend.codeGeneratedLibrary(currentPackage.packageId(),
                module.moduleName());
    }

    /**
     * Get the excluded jar paths for a particular module.
     * @param currentPackage    Package
     * @param jBallerinaBackend JBallerinaBackend
     * @param module            Module
     * @return                  List of paths to be excluded from the classpath
     */
    public static List<Path> getModuleJarPathsForModule(Package currentPackage,
                                                        JBallerinaBackend jBallerinaBackend, Module module) {
        List<Path> moduleJarPaths = new ArrayList<>();
        PlatformLibrary generatedJarLibrary = getPlatformLibrary(jBallerinaBackend, currentPackage, module);
        moduleJarPaths.add(generatedJarLibrary.path());

        if (!module.testDocumentIds().isEmpty()) {
            PlatformLibrary codeGeneratedTestLibrary = getCodeGeneratedTestLibrary(jBallerinaBackend,
                    currentPackage, module);
            moduleJarPaths.add(codeGeneratedTestLibrary.path());
        }
        return moduleJarPaths;
    }
}
