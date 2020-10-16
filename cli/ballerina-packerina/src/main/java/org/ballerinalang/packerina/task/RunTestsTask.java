/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.packerina.task;

import com.google.gson.Gson;
import org.ballerinalang.compiler.JarResolver;
import org.ballerinalang.packerina.OsUtils;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.test.runtime.entity.ModuleCoverage;
import org.ballerinalang.test.runtime.entity.ModuleStatus;
import org.ballerinalang.test.runtime.entity.TestReport;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.test.runtime.util.CodeCoverageUtils;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.ballerinalang.test.runtime.util.TesterinaUtils;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.tool.LauncherUtils;
import org.ballerinalang.tool.util.BFileUtil;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.util.Lists;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.ballerinalang.packerina.utils.DebugUtils.getDebugArgs;
import static org.ballerinalang.packerina.utils.DebugUtils.isInDebugMode;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.FILE_PROTOCOL;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_DATA_PLACEHOLDER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_DIR_NAME;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_ZIP_NAME;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RERUN_TEST_JSON_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RESULTS_HTML_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RESULTS_JSON_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.TEST_RUNTIME_JAR_PREFIX;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

/**
 * Task for executing tests.
 */
public class RunTestsTask implements Task {
    private final String[] args;
    private boolean report;
    private boolean coverage;
    private boolean isSingleTestExecution;
    private boolean isRerunTestExection;
    private List<String> singleExecTests;
    TestReport testReport;
    private JarResolver jarResolver;

    public RunTestsTask(boolean report, boolean coverage, String[] args) {
        this.coverage = coverage;
        this.args = args;
        this.report = report;
        if (report || coverage) {
            testReport = new TestReport();
        }
    }

    public RunTestsTask(boolean report, boolean coverage, boolean rerunTests, String[] args,
                        List<String> groupList,
                        List<String> disableGroupList,  List<String> testList) {
        this.args = args;
        this.report = report;
        this.coverage = coverage;
        this.isSingleTestExecution = false;
        this.isRerunTestExection = rerunTests;
        TesterinaRegistry testerinaRegistry = TesterinaRegistry.getInstance();

        // If rerunTests is true, we get the rerun test list and assign it to 'testList'
        if (rerunTests) {
            testList = new ArrayList<>();
        }

        if (disableGroupList != null) {
            testerinaRegistry.setGroups(disableGroupList);
            testerinaRegistry.setShouldIncludeGroups(false);
        } else if (groupList != null) {
            testerinaRegistry.setGroups(groupList);
            testerinaRegistry.setShouldIncludeGroups(true);
        } else if (testList != null) {
            isSingleTestExecution = true;
            singleExecTests = testList;
        }

        if (report || coverage) {
            testReport = new TestReport();
        }
    }

    @Override
    public void execute(BuildContext buildContext) {
        jarResolver = buildContext.get(BuildContextField.JAR_RESOLVER);
        Path targetDir = Paths.get(buildContext.get(BuildContextField.TARGET_DIR).toString());
        buildContext.out().println();
        buildContext.out().print("Running Tests");
        if (coverage) {
            buildContext.out().print(" with Coverage");
            try {
                CodeCoverageUtils.deleteDirectory(targetDir.resolve(TesterinaConstants.COVERAGE_DIR).toFile());
            } catch (IOException e) {
                throw createLauncherException("error while cleaning up coverage data", e);
            }
        }
        buildContext.out().println();

        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        List<BLangPackage> moduleBirMap = buildContext.getModules();

        int result = 0;

        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "ballerina test" command which only executes tests
        // in packages.
        for (BLangPackage bLangPackage : moduleBirMap) {
            TestSuite suite = TesterinaRegistry.getInstance().getTestSuites().get(bLangPackage.packageID.toString());

            if (isRerunTestExection) {
                Path jsonPath = buildContext.getTestJsonPathTargetCache(bLangPackage.packageID);
                singleExecTests = readFailedTestsFromFile(jsonPath);
            }

            if (isSingleTestExecution || isRerunTestExection) {
                suite.setTests(TesterinaUtils.getSingleExecutionTests(suite.getTests(), singleExecTests));
            }
            if (suite == null) {
                if (!DOT.equals(bLangPackage.packageID.toString())) {
                    buildContext.out().println();
                    buildContext.out().println("\t" + bLangPackage.packageID);
                }
                buildContext.out().println("\t" + "No tests found");
                buildContext.out().println();
                continue;
            } else if (isRerunTestExection && suite.getTests().size() == 0) {
                buildContext.out().println("\t" + "No failed test/s found in cache");
                buildContext.out().println();
                continue;
            } else if (isSingleTestExecution && suite.getTests().size() == 0) {
                buildContext.out().println("\t" + "No tests found with the given name/s");
                buildContext.out().println();
                continue;
            }
            suite.setReportRequired(report || coverage);
            HashSet<Path> testDependencies = new HashSet<>(jarResolver.allTestDependencies(bLangPackage));
            Path jsonPath = buildContext.getTestJsonPathTargetCache(bLangPackage.packageID);
            createTestJson(bLangPackage, suite, sourceRootPath, jsonPath);
            int testResult = runTestSuit(jsonPath, buildContext, testDependencies, bLangPackage);
            if (result == 0) {
                result = testResult;
            }

            if (report || coverage) {
                // Set projectName in test report
                String projectName;
                if (buildContext.get(BuildContextField.SOURCE_CONTEXT) instanceof SingleFileContext) {
                    SingleFileContext singleFileContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    projectName = singleFileContext.getBalFile().toFile().getName();
                } else {
                    projectName = sourceRootPath.toFile().getName();
                }
                testReport.setProjectName(projectName);
                Path statusJsonPath = jsonPath.resolve(TesterinaConstants.STATUS_FILE);
                try {
                    ModuleStatus moduleStatus = loadModuleStatusFromFile(statusJsonPath);
                    testReport.addModuleStatus(String.valueOf(bLangPackage.packageID.name), moduleStatus);
                } catch (IOException e) {
                    throw createLauncherException("error while generating test report", e);
                }
            }

            if (coverage) {
                int coverageResult = generateCoverageReport(buildContext, testDependencies, bLangPackage);
                if (coverageResult != 0) {
                    throw createLauncherException("error while generating test report");
                }
            }
        }

        // Load Coverage data from the files only after each module's coverage data has been finalized
        for (BLangPackage bLangPackage : moduleBirMap) {
            // Check and update coverage
            Path jsonPath = buildContext.getTestJsonPathTargetCache(bLangPackage.packageID);
            Path coverageJsonPath = jsonPath.resolve(TesterinaConstants.COVERAGE_FILE);

            if (coverageJsonPath.toFile().exists()) {
                try {
                    ModuleCoverage moduleCoverage = loadModuleCoverageFromFile(coverageJsonPath);
                    testReport.addCoverage(String.valueOf(bLangPackage.packageID.name), moduleCoverage);
                } catch (IOException e) {
                    throw createLauncherException("error while generating test report :", e);
                }
            }
        }

        if ((report || coverage) && (testReport.getModuleStatus().size() > 0)) {
            testReport.finalizeTestResults(coverage);
            generateHtmlReport(buildContext.out(), testReport, targetDir);
        }
        if (result != 0) {
            throw createLauncherException("there are test failures");
        }
    }

    /**
     * Extract data from the given bLangPackage.
     *
     * @param bLangPackage Ballerina package
     * @param sourceRootPath Source root path
     * @param jsonPath Path to the test json
     */
    private static void createTestJson(BLangPackage bLangPackage, TestSuite suite, Path sourceRootPath, Path jsonPath) {
        // set data
        suite.setInitFunctionName(bLangPackage.initFunction.name.value);
        suite.setStartFunctionName(bLangPackage.startFunction.name.value);
        suite.setStopFunctionName(bLangPackage.stopFunction.name.value);
        suite.setPackageName(bLangPackage.packageID.toString());
        suite.setSourceRootPath(sourceRootPath.toString());
        // add module functions
        bLangPackage.functions.forEach(function -> {
            String functionClassName = BFileUtil.getQualifiedClassName(bLangPackage.packageID.orgName.value,
                                                                       bLangPackage.packageID.name.value,
                                                                       bLangPackage.packageID.version.value,
                                                                       getClassName(function.pos.src.cUnitName));
            suite.addTestUtilityFunction(function.name.value, functionClassName);
        });
        // add test functions
        if (bLangPackage.containsTestablePkg()) {
            suite.setTestInitFunctionName(bLangPackage.getTestablePkg().initFunction.name.value);
            suite.setTestStartFunctionName(bLangPackage.getTestablePkg().startFunction.name.value);
            suite.setTestStopFunctionName(bLangPackage.getTestablePkg().stopFunction.name.value);
            bLangPackage.getTestablePkg().functions.forEach(function -> {
                String functionClassName = BFileUtil.getQualifiedClassName(bLangPackage.packageID.orgName.value,
                                                                           bLangPackage.packageID.name.value,
                                                                           bLangPackage.packageID.version.value,
                                                                           getClassName(function.pos.src.cUnitName));
                suite.addTestUtilityFunction(function.name.value, functionClassName);
            });
        } else {
            suite.setSourceFileName(bLangPackage.packageID.sourceFileName.value);
        }
        // write to json
        writeToJson(suite, jsonPath);
    }

    /**
     * return the function name.
     *
     * @param function String value of a function
     * @return function name
     */
    private static String getClassName(String function) {
        return function.replace(".bal", "").replace("/", ".");
    }

    /**
     * Write the content into a json.
     *
     * @param testSuite Data that are parsed to the json
     */
    private static void writeToJson(TestSuite testSuite, Path jsonPath) {
        Path tmpJsonPath = Paths.get(jsonPath.toString(), TesterinaConstants.TESTERINA_TEST_SUITE);
        File jsonFile = new File(tmpJsonPath.toString());
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            String json = gson.toJson(testSuite);
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw LauncherUtils.createLauncherException("couldn't read data from the Json file : " + e.toString());
        }
    }

    /**
     * Write the test report content into a json file.
     *
     * @param out PrintStream object to print messages to console
     * @param testReport Data that are parsed to the json
     */
    private void generateHtmlReport(PrintStream out, TestReport testReport, Path jsonPath) {
        out.println();
        out.println("Generating Test Report");

        Gson gson = new Gson();
        String json = gson.toJson(testReport).replaceAll("\\\\\\(", "(");

        File jsonFile = new File(jsonPath.resolve(RESULTS_JSON_FILE).toString());
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            out.println("\t" + Paths.get("").toAbsolutePath().relativize(jsonFile.toPath()) + "\n");
        } catch (IOException e) {
            throw LauncherUtils.createLauncherException("couldn't read data from the Json file : " + e.toString());
        }

        String content;
        try {
            CodeCoverageUtils.unzipReportResources(getClass().getClassLoader().getResourceAsStream(REPORT_ZIP_NAME),
                    jsonPath.resolve(REPORT_DIR_NAME).toFile());

            content = new String(Files.readAllBytes(jsonPath.resolve(REPORT_DIR_NAME).resolve(RESULTS_HTML_FILE)),
                    StandardCharsets.UTF_8);
            content = content.replace(REPORT_DATA_PLACEHOLDER, json);
        } catch (IOException e) {
            throw LauncherUtils.createLauncherException("error occurred while preparing test report: " + e.toString());
        }
        File htmlFile = new File(jsonPath.resolve(REPORT_DIR_NAME).resolve(RESULTS_HTML_FILE).toString());
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(htmlFile), StandardCharsets.UTF_8)) {
            writer.write(new String(content.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            out.println("\tView the test report at: " + FILE_PROTOCOL + htmlFile.getAbsolutePath());
        } catch (IOException e) {
            throw LauncherUtils.createLauncherException("couldn't read data from the Json file : " + e.toString());
        }
    }

    private int runTestSuit(Path jsonPath, BuildContext buildContext, HashSet<Path> testDependencies,
                            BLangPackage bLangPackage) {
        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.add(System.getProperty("java.command"));
        String mainClassName = TesterinaConstants.TESTERINA_LAUNCHER_CLASS_NAME;
        Path targetDir = Paths.get(buildContext.get(BuildContextField.TARGET_DIR).toString());
        String orgName = String.valueOf(bLangPackage.packageID.orgName);
        String packageName = String.valueOf(bLangPackage.packageID.name);

        String jacocoAgentJarPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                .resolve(BALLERINA_HOME_LIB).resolve(TesterinaConstants.AGENT_FILE_NAME).toString();

        try {
            if (coverage) {
                String agentCommand = "-javaagent:"
                        + jacocoAgentJarPath
                        + "=destfile="
                        + targetDir.resolve(TesterinaConstants.COVERAGE_DIR)
                        .resolve(TesterinaConstants.EXEC_FILE_NAME).toString();
                if (!TesterinaConstants.DOT.equals(packageName)) {
                    agentCommand += ",includes=" + orgName + ".*";
                }
                cmdArgs.add(agentCommand);
            }

            resolveTestDependencies(testDependencies);

            String classPath = getClassPath(getTestRuntimeJar(buildContext), testDependencies);
            cmdArgs.addAll(Lists.of("-cp", classPath));
            if (isInDebugMode()) {
                cmdArgs.add(getDebugArgs(buildContext));
            }
            cmdArgs.add(mainClassName);
            cmdArgs.add(jsonPath.toString());
            cmdArgs.addAll(Arrays.asList(args));
            cmdArgs.add(targetDir.toString());
            cmdArgs.add(jarResolver.moduleTestJar(bLangPackage).toString());
            cmdArgs.add(orgName);
            cmdArgs.add(packageName);
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).inheritIO();
            Process proc = processBuilder.start();
            return proc.waitFor();
        } catch (IOException | InterruptedException e) {
            throw createLauncherException("unable to run the tests: " + e.getMessage());
        }
    }

    private int generateCoverageReport(BuildContext buildContext, HashSet<Path> testDependencies,
                                        BLangPackage bLangPackage) {
        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.add(System.getProperty("java.command"));
        String mainClassName = TesterinaConstants.CODE_COV_GENERATOR_CLASS_NAME;
        Path jsonPath = buildContext.getTestJsonPathTargetCache(bLangPackage.packageID);
        Path targetDir = Paths.get(buildContext.get(BuildContextField.TARGET_DIR).toString());
        String orgName = String.valueOf(bLangPackage.packageID.orgName);
        String packageName = String.valueOf(bLangPackage.packageID.name);
        String version = String.valueOf(bLangPackage.packageID.version);
        try {
            String classPath = getClassPath(getTestRuntimeJar(buildContext), testDependencies);
            cmdArgs.addAll(Lists.of("-cp", classPath, mainClassName, jsonPath.toString()));
            cmdArgs.add(targetDir.toString());
            cmdArgs.add(jarResolver.moduleTestJar(bLangPackage).toString());
            cmdArgs.add(orgName);
            cmdArgs.add(packageName);
            cmdArgs.add(version);
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).inheritIO();
            Process proc = processBuilder.start();
            return proc.waitFor();

        } catch (IOException | InterruptedException e) {
            throw createLauncherException("unable to run the tests: " + e.getMessage());
        }
    }

    private String getClassPath(Path testRuntimeJar, HashSet<Path> testDependencies) {
        String separator = ":";
        StringBuilder classPath = new StringBuilder();
        classPath.append(testRuntimeJar);
        if (OsUtils.isWindows()) {
            separator = ";";
        }
        for (Path testDependency : testDependencies) {
            classPath.append(separator).append(testDependency);
        }
        return classPath.toString();
    }

    private Path getTestRuntimeJar(BuildContext buildContext) {
        String balHomePath = buildContext.get(BuildContextField.HOME_REPO).toString();
        String ballerinaVersion = RepoUtils.getBallerinaPackVersion();
        String runtimeJarName = TEST_RUNTIME_JAR_PREFIX + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        return Paths.get(balHomePath, "bre", "lib", runtimeJarName);
    }

    /**
     * Loads the ModuleCoverage object by reading a given Json.
     *
     * @param coverageJsonPath file path of json file
     * @return ModuleCoverage object
     * @throws FileNotFoundException if file does not exist
     */
    private ModuleCoverage loadModuleCoverageFromFile(Path coverageJsonPath) throws IOException {
        Gson gson = new Gson();
        BufferedReader bufferedReader = Files.newBufferedReader(coverageJsonPath, StandardCharsets.UTF_8);
        return gson.fromJson(bufferedReader, ModuleCoverage.class);
    }

    /**
     * Loads the ModuleStatus object by reading a given Json.
     * @param statusJsonPath file path of json file
     * @return ModuleStatus object
     * @throws FileNotFoundException if file does not exist
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
        } catch (NoSuchFileException e) {
            createLauncherException("No failed test cache present in target directory. ", e);
        } catch (IOException e) {
            createLauncherException("error while running failed tests. ", e);
        }

        return new ArrayList<String>();
    }

    private void resolveTestDependencies(HashSet<Path> testDependencies) {
        Path jacocoCoreJarPath =  Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                .resolve(BALLERINA_HOME_LIB).resolve(TesterinaConstants.JACOCO_CORE_JAR);
        Path jacocoReportJarPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                .resolve(BALLERINA_HOME_LIB).resolve(TesterinaConstants.JACOCO_REPORT_JAR);
        Path asmJarPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                .resolve(BALLERINA_HOME_LIB).resolve(TesterinaConstants.ASM_JAR);
        Path asmTreeJarPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                .resolve(BALLERINA_HOME_LIB).resolve(TesterinaConstants.ASM_TREE_JAR);
        Path asmCommonsJarPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                .resolve(BALLERINA_HOME_LIB).resolve(TesterinaConstants.ASM_COMMONS_JAR);

        testDependencies.add(jacocoCoreJarPath);
        testDependencies.add(jacocoReportJarPath);
        testDependencies.add(asmJarPath);
        testDependencies.add(asmTreeJarPath);
        testDependencies.add(asmCommonsJarPath);
    }
}
