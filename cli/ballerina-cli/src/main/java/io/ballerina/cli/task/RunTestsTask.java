/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.cli.task;

import com.google.gson.Gson;
import io.ballerina.cli.launcher.LauncherUtils;
import io.ballerina.cli.utils.BuildTime;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PlatformLibrary;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
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
import org.ballerinalang.test.runtime.util.TesterinaUtils;
import org.ballerinalang.testerina.core.TestProcessor;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.wso2.ballerinalang.util.Lists;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.DebugUtils.getDebugArgs;
import static io.ballerina.cli.utils.DebugUtils.isInDebugMode;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.COVERAGE_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DATA_KEY_SEPARATOR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.FILE_PROTOCOL;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MODULE_SEPARATOR;
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
 * Task for executing tests.
 *
 * @since 2.0.0
 */
public class RunTestsTask implements Task {
    private final PrintStream out;
    private final PrintStream err;
    private final String includesInCoverage;
    private List<String> groupList;
    private List<String> disableGroupList;
    private boolean report;
    private boolean coverage;
    private String coverageReportFormat;
    private boolean isSingleTestExecution;
    private boolean isRerunTestExecution;
    private List<String> singleExecTests;
    TestReport testReport;

    public RunTestsTask(PrintStream out, PrintStream err, String includes, String coverageFormat) {
        this.out = out;
        this.err = err;
        this.includesInCoverage = includes;
        this.coverageReportFormat = coverageFormat;
    }

    public RunTestsTask(PrintStream out, PrintStream err, boolean rerunTests, List<String> groupList,
                        List<String> disableGroupList, List<String> testList, String includes, String coverageFormat) {
        this.out = out;
        this.err = err;
        this.isSingleTestExecution = false;
        this.isRerunTestExecution = rerunTests;

        // If rerunTests is true, we get the rerun test list and assign it to 'testList'
        if (this.isRerunTestExecution) {
            testList = new ArrayList<>();
        }

        if (disableGroupList != null) {
            this.disableGroupList = disableGroupList;
        } else if (groupList != null) {
            this.groupList = groupList;
        }

        if (testList != null) {
            isSingleTestExecution = true;
            singleExecTests = testList;
        }
        this.includesInCoverage = includes;
        this.coverageReportFormat = coverageFormat;
    }

    @Override
    public void execute(Project project) {
        long start = 0;
        if (project.buildOptions().dumpBuildTime()) {
            start = System.currentTimeMillis();
        }
        try {
            ProjectUtils.checkExecutePermission(project.sourceRoot());
        } catch (ProjectException e) {
            throw createLauncherException(e.getMessage());
        }

        filterTestGroups();
        report = project.buildOptions().testReport();
        coverage = project.buildOptions().codeCoverage();

        if (report || coverage) {
            testReport = new TestReport();
        }

        Path cachesRoot;
        Target target;
        Path testsCachePath;
        try {
            if (project.kind() == ProjectKind.BUILD_PROJECT) {
                cachesRoot = project.sourceRoot();
                target = new Target(project.targetDir());
            } else {
                cachesRoot = Files.createTempDirectory("ballerina-test-cache" + System.nanoTime());
                target = new Target(cachesRoot);
            }

            testsCachePath = target.getTestsCachePath();
        } catch (IOException e) {
            throw createLauncherException("error while creating target directory: ", e);
        }

        boolean hasTests = false;

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
        JarResolver jarResolver = jBallerinaBackend.jarResolver();
        TestProcessor testProcessor = new TestProcessor(jarResolver);
        List<String> moduleNamesList = new ArrayList<>();
        Map<String, TestSuite> testSuiteMap = new HashMap<>();
        List<String> updatedSingleExecTests;
        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "bal test" command which only executes tests
        // in packages.
        for (ModuleId moduleId : project.currentPackage().moduleDependencyGraph().toTopologicallySortedList()) {
            Module module = project.currentPackage().module(moduleId);
            ModuleName moduleName = module.moduleName();

            TestSuite suite = testProcessor.testSuite(module).orElse(null);

            if (suite == null) {
                continue;
            } else if (isRerunTestExecution && suite.getTests().isEmpty()) {
                continue;
            } else if (isSingleTestExecution && suite.getTests().isEmpty()) {
                continue;
            }
            //Set 'hasTests' flag if there are any tests available in the package
            if (!hasTests) {
                hasTests = true;
            }

            if (isRerunTestExecution) {
                singleExecTests = readFailedTestsFromFile(target.path());
            }
            if (isSingleTestExecution || isRerunTestExecution) {
                // Update data driven tests with key
                updatedSingleExecTests = filterKeyBasedTests(moduleName.moduleNamePart(), suite, singleExecTests);
                suite.setTests(TesterinaUtils.getSingleExecutionTests(suite, updatedSingleExecTests));
            }
            if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                suite.setSourceFileName(project.sourceRoot().getFileName().toString());
            }
            suite.setReportRequired(report || coverage);
            String resolvedModuleName =
                    module.isDefaultModule() ? moduleName.toString() : module.moduleName().moduleNamePart();
            testSuiteMap.put(resolvedModuleName, suite);
            moduleNamesList.add(resolvedModuleName);
        }

        writeToTestSuiteJson(testSuiteMap, testsCachePath);

        if (hasTests) {
            int testResult;
            try {
                testResult = runTestSuit(testsCachePath, target, project.currentPackage(), jBallerinaBackend);
                if (report || coverage) {
                    for (String moduleName : moduleNamesList) {
                        ModuleStatus moduleStatus = loadModuleStatusFromFile(
                                testsCachePath.resolve(moduleName).resolve(TesterinaConstants.STATUS_FILE));

                        if (!moduleName.equals(project.currentPackage().packageName().toString())) {
                            moduleName = ModuleName.from(project.currentPackage().packageName(), moduleName).toString();
                        }
                        testReport.addModuleStatus(moduleName, moduleStatus);
                    }
                    try {
                        generateCoverage(project, jBallerinaBackend);
                        generateTesterinaReports(project, this.out, testReport, target);
                    } catch (IOException e) {
                        cleanTempCache(project, cachesRoot);
                        throw createLauncherException("error occurred while generating test report :", e);
                    }
                }
            } catch (IOException | InterruptedException e) {
                cleanTempCache(project, cachesRoot);
                throw createLauncherException("error occurred while running tests", e);
            }

            if (testResult != 0) {
                cleanTempCache(project, cachesRoot);
                throw createLauncherException("there are test failures");
            }
        }

        // Cleanup temp cache for SingleFileProject
        cleanTempCache(project, cachesRoot);
        if (project.buildOptions().dumpBuildTime()) {
            BuildTime.getInstance().testingExecutionDuration = System.currentTimeMillis() - start;
        }
    }

    /**
     * Contains module name as prefix to test name.
     *
     * @param testName String
     * @return boolean
     */
    private boolean containsModulePrefix(String testName) {
        boolean hasModPrefix = false;
        if (testName.contains(MODULE_SEPARATOR)) {
            if (testName.contains(DATA_KEY_SEPARATOR)) {
                if (testName.indexOf(MODULE_SEPARATOR) < testName.indexOf(DATA_KEY_SEPARATOR)) {
                    hasModPrefix = true;
                }
            } else {
                hasModPrefix = true;
            }
        }
        return hasModPrefix;
    }

    /**
     * Check whether module information is included in the provided test name.
     *
     * @param testName    String
     * @param packageName String
     * @param moduleName  String
     * @return boolean
     */
    private boolean includesModule(String testName, String packageName, String moduleName) {
        boolean flag = false;
        if (containsModulePrefix(testName)) {
            String prefix = testName.substring(0, testName.indexOf(MODULE_SEPARATOR));
            try {
                if (prefix.equals(packageName) || prefix.equals(packageName + DOT + moduleName)) {
                    flag = true;
                }
            } catch (IndexOutOfBoundsException e) {
                throw createLauncherException("error occurred while executing tests. Test list cannot be empty", e);
            }
        } else {
            flag = true;
        }
        return flag;
    }

    /**
     * Update test filters using the given data keys.
     *
     * @param moduleName      String
     * @param suite           TestSuite
     * @param singleExecTests List<String>
     * @return List of updated tests
     */
    private List<String> filterKeyBasedTests(String moduleName, TestSuite suite, List<String> singleExecTests) {
        List<String> updatedSingleExecTests = new ArrayList<>();
        Map<String, List<String>> keyValues = new HashMap<>();
        for (String testName : singleExecTests) {
            if (testName.contains(DATA_KEY_SEPARATOR) && includesModule(testName, suite.getPackageID(), moduleName)) {
                try {
                    // Separate test name and the data set key
                    String testValue = testName.substring(0, testName.indexOf(DATA_KEY_SEPARATOR));
                    String originalTestName = testValue;
                    String caseValue = testName.substring(testName.indexOf(DATA_KEY_SEPARATOR) + 1);
                    if (originalTestName.contains(MODULE_SEPARATOR)) {
                        originalTestName = originalTestName.substring(originalTestName.indexOf(MODULE_SEPARATOR) + 1);
                    }
                    if (keyValues.containsKey(originalTestName)) {
                        keyValues.get(originalTestName).add(caseValue);
                    } else {
                        keyValues.put(originalTestName, new ArrayList<>(Arrays.asList(caseValue)));
                    }
                    // Update the test name in the filtered test list
                    if (!updatedSingleExecTests.contains(testValue)) {
                        updatedSingleExecTests.add(testValue);
                    }
                } catch (IndexOutOfBoundsException e) {
                    throw createLauncherException("error occurred while filtering tests based on provided key values",
                            e);
                }
            } else {
                if (!updatedSingleExecTests.contains(testName)) {
                    updatedSingleExecTests.add(testName);
                }
            }
        }
        if (!keyValues.isEmpty()) {
            suite.setSingleDDTExecution(true);
            suite.setDataKeyValues(keyValues);
        }
        return updatedSingleExecTests;
    }

    private void generateCoverage(Project project, JBallerinaBackend jBallerinaBackend)
            throws IOException {
        // Generate code coverage
        if (!coverage) {
            return;
        }
        if (testReport == null) { // This to avoid the spotbugs failure.
            return;
        }
        Map<String, ModuleCoverage> moduleCoverageMap = initializeCoverageMap(project);
        // Following lists will hold the coverage information needed for the coverage XML file generation.
        List<ISourceFileCoverage> packageSourceCoverageList = new ArrayList();
        List<IClassCoverage> packageNativeClassCoverageList = new ArrayList();
        List<IClassCoverage> packageBalClassCoverageList = new ArrayList();
        List<ExecutionData> packageExecData = new ArrayList();
        List<SessionInfo> packageSessionInfo = new ArrayList();
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            CoverageReport coverageReport = new CoverageReport(module, moduleCoverageMap,
                    packageNativeClassCoverageList, packageBalClassCoverageList, packageSourceCoverageList,
                    packageExecData, packageSessionInfo);
            coverageReport.generateReport(jBallerinaBackend, this.includesInCoverage, this.coverageReportFormat);
        }
        // Traverse coverage map and add module wise coverage to test report
        for (Map.Entry mapElement : moduleCoverageMap.entrySet()) {
            String moduleName = (String) mapElement.getKey();
            ModuleCoverage moduleCoverage = (ModuleCoverage) mapElement.getValue();
            testReport.addCoverage(moduleName, moduleCoverage);
        }
        if (CodeCoverageUtils.isRequestedReportFormat(this.coverageReportFormat,
                TesterinaConstants.JACOCO_XML_FORMAT)) {
            // Generate coverage XML report
            CodeCoverageUtils.createXMLReport(project, packageExecData, packageNativeClassCoverageList,
                    packageBalClassCoverageList, packageSourceCoverageList, packageSessionInfo);
        }
    }


    private void filterTestGroups() {
        TesterinaRegistry testerinaRegistry = TesterinaRegistry.getInstance();
        if (disableGroupList != null) {
            testerinaRegistry.setGroups(disableGroupList);
            testerinaRegistry.setShouldIncludeGroups(false);
        } else if (groupList != null) {
            testerinaRegistry.setGroups(groupList);
            testerinaRegistry.setShouldIncludeGroups(true);
        }
    }

    /**
     * Write the test report content into testerina report formats(json and html).
     *
     * @param out        PrintStream object to print messages to console
     * @param testReport Data that are parsed to the json
     */
    private void generateTesterinaReports(Project project, PrintStream out, TestReport testReport, Target target)
            throws IOException {
        if (!report && !coverage) {
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
        testReport.finalizeTestResults(coverage);

        Gson gson = new Gson();
        String json = gson.toJson(testReport).replaceAll("\\\\\\(", "(");

        File jsonFile = new File(reportDir.resolve(RESULTS_JSON_FILE).toString());
        try (FileOutputStream fileOutputStream = new FileOutputStream(jsonFile)) {
            try (Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
                out.println("\t" + jsonFile.getAbsolutePath() + "\n");
            }
        }

        Path reportZipPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_LIB).
                resolve(TesterinaConstants.TOOLS_DIR_NAME).resolve(TesterinaConstants.COVERAGE_DIR).
                resolve(REPORT_ZIP_NAME);
        // Dump the Testerina html report only if '--test-report' flag is provided
        if (report) {
            if (Files.exists(reportZipPath)) {
                String content;
                try {
                    try (FileInputStream fileInputStream = new FileInputStream(reportZipPath.toFile())) {
                        CodeCoverageUtils.unzipReportResources(fileInputStream,
                                reportDir.toFile());
                    }
                    content = Files.readString(reportDir.resolve(RESULTS_HTML_FILE));
                    content = content.replace(REPORT_DATA_PLACEHOLDER, json);
                } catch (IOException e) {
                    throw createLauncherException("error occurred while preparing test report: " + e.toString());
                }
                File htmlFile = new File(reportDir.resolve(RESULTS_HTML_FILE).toString());
                try (FileOutputStream fileOutputStream = new FileOutputStream(htmlFile)) {
                    try (Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                        writer.write(new String(content.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
                        out.println("\tView the test report at: " +
                                FILE_PROTOCOL + Paths.get(htmlFile.getPath()).toAbsolutePath().normalize().toString());
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

    private int runTestSuit(Path testCachePath, Target target, Package currentPackage,
                            JBallerinaBackend jBallerinaBackend) throws IOException,
            InterruptedException {
        String packageName = currentPackage.packageName().toString();
        String orgName = currentPackage.packageOrg().toString();
        String classPath = getClassPath(jBallerinaBackend, currentPackage);
        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.add(System.getProperty("java.command"));
        cmdArgs.add("-XX:+HeapDumpOnOutOfMemoryError");
        cmdArgs.add("-XX:HeapDumpPath=" + System.getProperty(USER_DIR));

        String mainClassName = TesterinaConstants.TESTERINA_LAUNCHER_CLASS_NAME;

        if (coverage) {
            String jacocoAgentJarPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                    .resolve(BALLERINA_HOME_LIB).resolve(TesterinaConstants.AGENT_FILE_NAME).toString();
            String agentCommand = "-javaagent:"
                    + jacocoAgentJarPath
                    + "=destfile="
                    + target.getTestsCachePath().resolve(TesterinaConstants.COVERAGE_DIR)
                    .resolve(TesterinaConstants.EXEC_FILE_NAME).toString();
            if (!TesterinaConstants.DOT.equals(packageName) && this.includesInCoverage == null) {
                // add user defined classes for generating the jacoco exec file
                agentCommand += ",includes=" + orgName + ".*";
            } else {
                agentCommand += ",includes=" + this.includesInCoverage;
            }

            cmdArgs.add(agentCommand);
        }

        cmdArgs.addAll(Lists.of("-cp", classPath));
        if (isInDebugMode()) {
            cmdArgs.add(getDebugArgs(this.err));
        }
        cmdArgs.add(mainClassName);

        // Adds arguments to be read at the Test Runner
        // Index [0 - 3...]
        cmdArgs.add(target.path().toString());
        cmdArgs.add(Boolean.toString(report));
        cmdArgs.add(Boolean.toString(coverage));

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).inheritIO();
        Process proc = processBuilder.start();
        return proc.waitFor();
    }

    /**
     * Loads the ModuleStatus object by reading a given Json.
     *
     * @param statusJsonPath file path of json file
     * @return ModuleStatus object
     * @throws IOException if file does not exist
     */
    private ModuleStatus loadModuleStatusFromFile(Path statusJsonPath) throws IOException {
        Gson gson = new Gson();
        BufferedReader bufferedReader = Files.newBufferedReader(statusJsonPath, StandardCharsets.UTF_8);
        return gson.fromJson(bufferedReader, ModuleStatus.class);
    }

    private List<String> readFailedTestsFromFile(Path rerunTestJsonPath) {
        Gson gson = new Gson();
        rerunTestJsonPath = Paths.get(rerunTestJsonPath.toString(), RERUN_TEST_JSON_FILE);

        try (BufferedReader bufferedReader = Files.newBufferedReader(rerunTestJsonPath, StandardCharsets.UTF_8)) {
            return gson.fromJson(bufferedReader, ArrayList.class);
        } catch (IOException e) {
            throw createLauncherException("error while running failed tests : ", e);
        }
    }

    private void cleanTempCache(Project project, Path cachesRoot) {
        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            ProjectUtils.deleteDirectory(cachesRoot);
        }
    }

    /**
     * Initialize coverage map used for aggregating module wise coverage.
     *
     * @param project Project
     * @return Map<String, ModuleCoverage>
     */
    private Map<String, ModuleCoverage> initializeCoverageMap(Project project) {
        Map<String, ModuleCoverage> moduleCoverageMap = new HashMap<>();
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            moduleCoverageMap.put(module.moduleName().toString(), new ModuleCoverage());
        }
        return moduleCoverageMap;
    }

    /**
     * Write the content of each test suite into a common json.
     */
    private static void writeToTestSuiteJson(Map<String, TestSuite> testSuiteMap, Path testsCachePath) {
        if (!Files.exists(testsCachePath)) {
            try {
                Files.createDirectories(testsCachePath);
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException("couldn't create test cache directories : " + e.toString());
            }
        }

        Path jsonFilePath = Paths.get(testsCachePath.toString(), TesterinaConstants.TESTERINA_TEST_SUITE);
        File jsonFile = new File(jsonFilePath.toString());
        try (FileOutputStream fileOutputStream = new FileOutputStream(jsonFile)) {
            try (Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                String json = gson.toJson(testSuiteMap);
                writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException("couldn't write data to test suite file : " + e.toString());
            }
        } catch (IOException e) {
            throw LauncherUtils.createLauncherException("couldn't write data to test suite file : " + e.toString());
        }
    }

    private String getClassPath(JBallerinaBackend jBallerinaBackend, Package currentPackage) {
        List<Path> dependencies = new ArrayList<>();
        JarResolver jarResolver = jBallerinaBackend.jarResolver();

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
        dependencies = dependencies.stream().distinct().collect(Collectors.toList());

        List<Path> jarList = getExclusionPathList(jBallerinaBackend, currentPackage);
        dependencies.removeAll(jarList);

        StringJoiner classPath = new StringJoiner(File.pathSeparator);
        dependencies.stream().map(Path::toString).forEach(classPath::add);
        return classPath.toString();
    }

    private List<Path> getExclusionPathList(JBallerinaBackend jBallerinaBackend, Package currentPackage) {
        List<Path> exclusionPathList = new ArrayList<>();

        for (ModuleId moduleId : currentPackage.moduleIds()) {
            Module module = currentPackage.module(moduleId);

            PlatformLibrary generatedJarLibrary = jBallerinaBackend.codeGeneratedLibrary(currentPackage.packageId(),
                    module.moduleName());
            exclusionPathList.add(generatedJarLibrary.path());

            if (!module.testDocumentIds().isEmpty()) {
                PlatformLibrary codeGeneratedTestLibrary = jBallerinaBackend.codeGeneratedTestLibrary(
                        currentPackage.packageId(), module.moduleName());
                exclusionPathList.add(codeGeneratedTestLibrary.path());
            }
        }

        return exclusionPathList.stream().distinct().collect(Collectors.toList());
    }

}
