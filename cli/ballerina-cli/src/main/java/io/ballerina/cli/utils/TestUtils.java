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
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Project;
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

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.COVERAGE_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.FILE_PROTOCOL;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_DATA_PLACEHOLDER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_ZIP_NAME;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RERUN_TEST_JSON_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RESULTS_HTML_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RESULTS_JSON_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.TOOLS_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;

/**
 * Utility functions and classes for Run Test Task.
 *
 * @since 2.3.0
 */
public class TestUtils {

    public static void generateCoverage(Project project, TestReport testReport, JBallerinaBackend jBallerinaBackend,
                                        String includesInCoverage, String coverageReportFormat,
                                        Map<String, Module> coverageModules)
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
            coverageReport.generateReport(jBallerinaBackend, includesInCoverage, coverageReportFormat,
                    coverageModules.get(module.moduleName().toString()));
        }
        // Traverse coverage map and add module wise coverage to test report
        for (Map.Entry mapElement : moduleCoverageMap.entrySet()) {
            String moduleName = (String) mapElement.getKey();
            ModuleCoverage moduleCoverage = (ModuleCoverage) mapElement.getValue();
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
        String json = gson.toJson(testReport).replaceAll("\\\\\\(", "(");

        File jsonFile = new File(reportDir.resolve(RESULTS_JSON_FILE).toString());
        try (FileOutputStream fileOutputStream = new FileOutputStream(jsonFile)) {
            try (Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
                out.println("\t" + jsonFile.toPath() + "\n");
            }
        }

        Path reportZipPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_LIB).
                resolve(TesterinaConstants.TOOLS_DIR_NAME).resolve(TesterinaConstants.COVERAGE_DIR).
                resolve(REPORT_ZIP_NAME);
        // Dump the Testerina html report only if '--test-report' flag is provided
        if (project.buildOptions().testReport()) {
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

    /**
     * Loads the ModuleStatus object by reading a given Json.
     *
     * @param statusJsonPath file path of json file
     * @return ModuleStatus object
     * @throws IOException if file does not exist
     */
    public static ModuleStatus loadModuleStatusFromFile(Path statusJsonPath) throws IOException {
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
}
