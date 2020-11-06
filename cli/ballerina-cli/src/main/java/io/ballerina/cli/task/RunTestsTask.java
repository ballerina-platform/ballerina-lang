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
import io.ballerina.cli.utils.OsUtils;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.model.Target;
import io.ballerina.projects.testsuite.TestSuite;
import io.ballerina.projects.testsuite.TesterinaRegistry;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.test.runtime.entity.ModuleCoverage;
import org.ballerinalang.test.runtime.entity.ModuleStatus;
import org.ballerinalang.test.runtime.entity.TestReport;
import org.ballerinalang.test.runtime.util.CodeCoverageUtils;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.ballerinalang.test.runtime.util.TesterinaUtils;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.util.Lists;

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
import java.util.HashSet;
import java.util.List;

import static io.ballerina.cli.utils.DebugUtils.getDebugArgs;
import static io.ballerina.cli.utils.DebugUtils.isInDebugMode;
import static io.ballerina.projects.util.ProjectUtils.getBalHomePath;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.FILE_PROTOCOL;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_DATA_PLACEHOLDER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_DIR_NAME;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_ZIP_NAME;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RERUN_TEST_JSON_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RESULTS_HTML_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RESULTS_JSON_FILE;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

/**
 * Task for executing tests.
 */
public class RunTestsTask implements Task {
    private final PrintStream out;
    private final PrintStream err;
    private final List<String> args;
    private boolean report;
    private boolean coverage;
    private boolean isSingleTestExecution;
    private boolean isRerunTestExection;
    private List<String> singleExecTests;
    TestReport testReport;

    public RunTestsTask(PrintStream out, PrintStream err, String[] args) {
        this.out = out;
        this.err = err;
        this.args = Lists.of(args);
    }

    public RunTestsTask(PrintStream out, PrintStream err, String[] args, boolean rerunTests, List<String> groupList,
                        List<String> disableGroupList, List<String> testList) {
        this.out = out;
        this.err = err;
        this.args = Lists.of(args);
        this.isSingleTestExecution = false;
        this.isRerunTestExection = rerunTests;

        // If rerunTests is true, we get the rerun test list and assign it to 'testList'
        if (rerunTests) {
            testList = new ArrayList<>();
        }

        TesterinaRegistry testerinaRegistry = TesterinaRegistry.getInstance();
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
    }

    @Override
    public void execute(Project project) {
        if (report || coverage) {
            testReport = new TestReport();
        }
        Path sourceRootPath = project.sourceRoot();
        Target target;
        try {
            target = new Target(sourceRootPath);
        } catch (IOException e) {
            throw createLauncherException("error while creating target directory: ", e);
        }
        this.out.println();
        this.out.print("Running Tests");
        if (coverage) {
            out.print(" with Coverage");
            try {
                CodeCoverageUtils.deleteDirectory(
                        target.getTestsCachePath().resolve(TesterinaConstants.COVERAGE_DIR).toFile());
            } catch (IOException e) {
                throw createLauncherException("error while cleaning up coverage data", e);
            }
        }
        this.out.println();

        int result = 0;

        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "ballerina test" command which only executes tests
        // in packages.
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            ModuleName moduleName = module.moduleName();
            TestSuite suite = TesterinaRegistry.getInstance().getTestSuites().get(moduleName.toString());

            Path jsonPath;
            try {
                jsonPath = target.getTestsCachePath();
            } catch (IOException e) {
                throw createLauncherException("error while creating json caches for tests: ", e);
            }

            if (isRerunTestExection) {
                singleExecTests = readFailedTestsFromFile(jsonPath);
            }

            if (isSingleTestExecution || isRerunTestExection) {
                suite.setTests(TesterinaUtils.getSingleExecutionTests(suite.getTests(), singleExecTests));
            }
            if (suite == null) {
                if (!project.currentPackage().packageOrg().anonymous()) {
                    out.println();
                    out.println("\t" + moduleName.toString());
                }
                out.println("\t" + "No tests found");
                continue;
            } else if (isRerunTestExection && suite.getTests().size() == 0) {
                out.println("\t" + "No failed test/s found in cache");
                continue;
            } else if (isSingleTestExecution && suite.getTests().size() == 0) {
                out.println("\t" + "No tests found with the given name/s");
                continue;
            }
            suite.setReportRequired(report || coverage);
            HashSet<Path> testDependencies = new HashSet<>();
            if (project instanceof SingleFileProject) {
                out.println("\t" + module.document(module.documentIds().iterator().next()).name());
            } else {
                out.println("\t" + module.moduleName().toString());
            }
            int testResult = runTestSuit(jsonPath, target, testDependencies, module);
            if (result == 0) {
                result = testResult;
            }

            if (report || coverage) {
                // Set projectName in test report
                String projectName;
                if (project instanceof SingleFileProject) {
                    ProjectUtils.getJarName(module);
                    projectName = ProjectUtils.getJarName(module) + ProjectConstants.BLANG_SOURCE_EXT;
                } else {
                    projectName = sourceRootPath.toFile().getName();
                }
                testReport.setProjectName(projectName);
                Path statusJsonPath = jsonPath.resolve(TesterinaConstants.STATUS_FILE);
                try {
                    ModuleStatus moduleStatus = loadModuleStatusFromFile(statusJsonPath);
                    testReport.addModuleStatus(moduleName.toString(), moduleStatus);
                } catch (IOException e) {
                    throw createLauncherException("error while generating test report", e);
                }
            }

            if (coverage) {
                int coverageResult;
                try {
                    coverageResult = generateCoverageReport(target, testDependencies, module);
                } catch (IOException e) {
                    throw createLauncherException("error while generating coverage report" + e.getMessage());
                }
                if (coverageResult != 0) {
                    throw createLauncherException("error while generating test report");
                }
            }
        }

        // Load Coverage data from the files only after each module's coverage data has been finalized
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            ModuleName moduleName = module.moduleName();
            // Check and update coverage
            Path jsonPath;
            try {
                jsonPath = target.getTestsCachePath().resolve(TesterinaConstants.JSON_DIR_NAME);
            } catch (IOException e) {
                throw createLauncherException("error while creating json caches for tests: ", e);
            }
            Path coverageJsonPath = jsonPath.resolve(TesterinaConstants.COVERAGE_FILE);

            if (coverageJsonPath.toFile().exists()) {
                try {
                    ModuleCoverage moduleCoverage = loadModuleCoverageFromFile(coverageJsonPath);
                    testReport.addCoverage(moduleName.toString(), moduleCoverage);
                } catch (IOException e) {
                    throw createLauncherException("error while generating test report :", e);
                }
            }
        }

        if ((report || coverage) && (testReport.getModuleStatus().size() > 0)) {
            testReport.finalizeTestResults(coverage);
            generateHtmlReport(this.out, testReport, target.path());
        }
        if (result != 0) {
            throw createLauncherException("there are test failures");
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

            content = Files.readString(jsonPath.resolve(REPORT_DIR_NAME).resolve(RESULTS_HTML_FILE));
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

    private int runTestSuit(Path jsonPath, Target target, HashSet<Path> testDependencies,
                            Module module) {
        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.add(System.getProperty("java.command"));
//        cmdArgs.addAll(Lists.of("-Xdebug", "-Xrunjdwp:transport=dt_socket,address=5005,server=y"));
        String mainClassName = TesterinaConstants.TESTERINA_LAUNCHER_CLASS_NAME;
        String orgName = module.packageInstance().packageOrg().toString();
        String packageName = module.packageInstance().packageName().toString();

        String jacocoAgentJarPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                .resolve(BALLERINA_HOME_LIB).resolve(TesterinaConstants.AGENT_FILE_NAME).toString();

        try {
            if (coverage) {
                String agentCommand = "-javaagent:"
                        + jacocoAgentJarPath
                        + "=destfile="
                        + target.getTestsCachePath().resolve(TesterinaConstants.COVERAGE_DIR)
                        .resolve(TesterinaConstants.EXEC_FILE_NAME).toString();
                if (!TesterinaConstants.DOT.equals(packageName)) {
                    agentCommand += ",includes=" + orgName + ".*";
                }
                cmdArgs.add(agentCommand);
            }

            resolveTestDependencies(testDependencies);

//            String classPath = getClassPath(ProjectUtils.getTesterinaRuntimeJarPath(), testDependencies);
            String testJarName = ProjectUtils.getTestableJarName(module);
//            cmdArgs.addAll(Lists.of("-cp", classPath));
            cmdArgs.addAll(Lists.of("-cp", getBalHomePath() + "/bre/lib/*:"
                    + target.getTestsCachePath().resolve(testJarName).toString()));
            if (isInDebugMode()) {
                cmdArgs.add(getDebugArgs(this.err));
            }
            cmdArgs.add(mainClassName);
            cmdArgs.add(jsonPath.toString());
            cmdArgs.addAll(args);
            cmdArgs.add(target.path().toString());
            cmdArgs.add(target.getTestsCachePath().resolve(testJarName).toString());
            cmdArgs.add(orgName);
            cmdArgs.add(packageName);
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).inheritIO();
            Process proc = processBuilder.start();
            return proc.waitFor();
        } catch (IOException | InterruptedException e) {
            throw createLauncherException("unable to run the tests: " + e.getMessage());
        }
    }

    private int generateCoverageReport(Target target, HashSet<Path> testDependencies,
                                       Module module) throws IOException {
        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.add(System.getProperty("java.command"));
        String mainClassName = TesterinaConstants.CODE_COV_GENERATOR_CLASS_NAME;
        Path jsonPath = target.getTestsCachePath().resolve(TesterinaConstants.JSON_DIR_NAME);
        String orgName = module.packageInstance().packageOrg().toString();
        String packageName = module.packageInstance().packageName().toString();
        String version = module.packageInstance().packageVersion().toString();
        try {
            String classPath = getClassPath(ProjectUtils.getTesterinaRuntimeJarPath(), testDependencies);
            String testJarName = ProjectUtils.getJarName(module) + "-testable" + BLANG_COMPILED_JAR_EXT;

            cmdArgs.addAll(Lists.of("-cp", classPath, mainClassName, jsonPath.toString()));
            cmdArgs.add(target.path().toString());
            cmdArgs.add(target.getJarCachePath().resolve(testJarName).toString());
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
        return new ArrayList<>();
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
