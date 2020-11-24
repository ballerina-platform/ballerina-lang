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
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.JdkVersion;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.testsuite.TestSuite;
import io.ballerina.projects.testsuite.TesterinaRegistry;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.test.runtime.entity.CoverageReport;
import org.ballerinalang.test.runtime.entity.ModuleStatus;
import org.ballerinalang.test.runtime.entity.TestReport;
import org.ballerinalang.test.runtime.util.CodeCoverageUtils;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.ballerinalang.test.runtime.util.TesterinaUtils;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.util.Lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

import static io.ballerina.cli.utils.DebugUtils.getDebugArgs;
import static io.ballerina.cli.utils.DebugUtils.isInDebugMode;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.COVERAGE_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.FILE_PROTOCOL;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_DATA_PLACEHOLDER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_ZIP_NAME;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RERUN_TEST_JSON_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RESULTS_HTML_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RESULTS_JSON_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.TOOLS_DIR_NAME;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;

/**
 * Task for executing tests.
 *
 * @since 2.0.0
 */
public class RunTestsTask implements Task {
    private final PrintStream out;
    private final PrintStream err;
    private final List<String> args;
    private List<String> groupList;
    private List<String> disableGroupList;
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

        //TODO: fix --rerun-failed and enable it
        this.isRerunTestExection = false;

        // If rerunTests is true, we get the rerun test list and assign it to 'testList'
        if (this.isRerunTestExection) {
            testList = new ArrayList<>();
        }

        if (disableGroupList != null) {
            this.disableGroupList = disableGroupList;
        } else if (groupList != null) {
            this.groupList = groupList;
        } else if (testList != null) {
            isSingleTestExecution = true;
            singleExecTests = testList;
        }
    }

    @Override
    public void execute(Project project) {
        filterTestGroups();
        report = project.buildOptions().testReport();
        coverage = project.buildOptions().codeCoverage();

        if (report || coverage) {
            testReport = new TestReport();
        }
        Path sourceRootPath = project.sourceRoot();
        Target target;
        Path testsCachePath;
        try {
            target = new Target(sourceRootPath);
            testsCachePath = target.getTestsCachePath();
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

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JdkVersion.JAVA_11);
        JarResolver jarResolver = jBallerinaBackend.jarResolver();

        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "ballerina test" command which only executes tests
        // in packages.
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            ModuleName moduleName = module.moduleName();

            TestSuite suite = jBallerinaBackend.testSuite(module).orElse(null);
            Path moduleTestCachePath = testsCachePath.resolve(moduleName.toString());
            Path reportDir;

            try {
                reportDir = target.getReportPath();
            } catch (IOException e) {
                throw createLauncherException("error while creating report directory in target", e);
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

            if (isRerunTestExection) {
                singleExecTests = readFailedTestsFromFile(reportDir);
            }

            if (isSingleTestExecution || isRerunTestExection) {
                suite.setTests(TesterinaUtils.getSingleExecutionTests(suite.getTests(), singleExecTests));
            }
            suite.setReportRequired(report || coverage);
            Collection<Path> dependencies = jarResolver.getJarFilePathsRequiredForTestExecution(moduleName);
            if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                out.println("\t" + module.document(module.documentIds().iterator().next()).name());
            } else {
                out.println("\t" + module.moduleName().toString());
            }
            writeToJson(suite, moduleTestCachePath);
            int testResult = runTestSuit(moduleTestCachePath, target, dependencies, module);
            if (result == 0) {
                result = testResult;
            }

            if (report || coverage) {
                try {
                    ModuleStatus moduleStatus = loadModuleStatusFromFile(moduleTestCachePath
                            .resolve(TesterinaConstants.STATUS_FILE));
                    testReport.addModuleStatus(moduleName.toString(), moduleStatus);
                } catch (IOException e) {
                    throw createLauncherException("error while generating test report", e);
                }
            }
        }

        try {
            generateCoverage(project);
            generateHtmlReport(project, this.out, testReport, target);
        } catch (IOException e) {
            throw createLauncherException("error while generating test report :", e);
        }

        if (result != 0) {
            throw createLauncherException("there are test failures");
        }
    }

    private void generateCoverage(Project project) throws IOException {
        // Generate code coverage
        if (coverage) {
            for (ModuleId moduleId : project.currentPackage().moduleIds()) {
                Module module = project.currentPackage().module(moduleId);
                CoverageReport coverageReport = new CoverageReport(module);
                testReport.addCoverage(module.moduleName().toString(), coverageReport.generateReport());
            }
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
     * Write the content into a json.
     *
     * @param testSuite Data that are parsed to the json
     */
    private static void writeToJson(TestSuite testSuite, Path moduleTestsCachePath) {
        if (!Files.exists(moduleTestsCachePath)) {
            try {
                Files.createDirectories(moduleTestsCachePath);
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException("couldn't create test suite : " + e.toString());
            }
        }
        Path tmpJsonPath = Paths.get(moduleTestsCachePath.toString(), TesterinaConstants.TESTERINA_TEST_SUITE);
        File jsonFile = new File(tmpJsonPath.toString());
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            String json = gson.toJson(testSuite);
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw LauncherUtils.createLauncherException("couldn't write data to test suite file : " + e.toString());
        }
    }

    /**
     * Write the test report content into a json file.
     *
     * @param out        PrintStream object to print messages to console
     * @param testReport Data that are parsed to the json
     */
    private void generateHtmlReport(Project project, PrintStream out, TestReport testReport, Target target) {
        Path reportDir;
        try {
            reportDir = target.getReportPath();
        } catch (IOException e) {
            throw createLauncherException("error while creating report directory in target", e);
        }
        if ((report || coverage) && (testReport.getModuleStatus().size() > 0)) {
            out.println();
            out.println("Generating Test Report");

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

            File jsonFile = new File(target.path().resolve(RESULTS_JSON_FILE).toString());
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
                writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
                out.println("\t" + jsonFile.getAbsolutePath() + "\n");
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException("couldn't read data from the Json file : " + e.toString());
            }

            Path reportZipPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_LIB).
                    resolve(TesterinaConstants.TOOLS_DIR_NAME).resolve(TesterinaConstants.COVERAGE_DIR).
                    resolve(REPORT_ZIP_NAME);
            if (Files.exists(reportZipPath)) {
                String content;
                try {
                    CodeCoverageUtils.unzipReportResources(new FileInputStream(reportZipPath.toFile()),
                            reportDir.toFile());
                    content = Files.readString(reportDir.resolve(RESULTS_HTML_FILE));
                    content = content.replace(REPORT_DATA_PLACEHOLDER, json);
                } catch (IOException e) {
                    throw createLauncherException("error occurred while preparing test report: " + e.toString());
                }
                File htmlFile = new File(reportDir.resolve(RESULTS_HTML_FILE).toString());
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(htmlFile), StandardCharsets.UTF_8)) {
                    writer.write(new String(content.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
                    out.println("\tView the test report at: " +
                            FILE_PROTOCOL + Paths.get(htmlFile.getPath()).toAbsolutePath().normalize().toString());
                } catch (IOException e) {
                    throw createLauncherException("couldn't read data from the Json file : " + e.toString());
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

    private int runTestSuit(Path moduleTestCache, Target target, Collection<Path> testDependencies,
                            Module module) {
        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.add(System.getProperty("java.command"));
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

            String classPath = getClassPath(testDependencies);
            cmdArgs.addAll(Lists.of("-cp", classPath));
            if (isInDebugMode()) {
                cmdArgs.add(getDebugArgs(this.err));
            }
            cmdArgs.add(mainClassName);
            cmdArgs.add(moduleTestCache.toString());
            cmdArgs.addAll(args);
            cmdArgs.add(target.path().toString());
            cmdArgs.add(orgName);
            cmdArgs.add(packageName);
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).inheritIO();
            Process proc = processBuilder.start();
            return proc.waitFor();
        } catch (IOException | InterruptedException e) {
            throw createLauncherException("unable to run the tests: " + e.getMessage());
        }
    }

    private String getClassPath(Collection<Path> dependencies) {
        StringJoiner cp = new StringJoiner(File.pathSeparator);
        dependencies.stream().map(Path::toString).forEach(cp::add);
        return cp.toString();
    }

    /**
     * Loads the ModuleStatus object by reading a given Json.
     *
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
        } catch (IOException e) {
            throw createLauncherException("error while running failed tests. ", e);
        }
    }
}
