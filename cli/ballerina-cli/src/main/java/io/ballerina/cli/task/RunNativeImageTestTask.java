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

package io.ballerina.cli.task;

import io.ballerina.cli.utils.BuildTime;
import io.ballerina.cli.utils.TestUtils;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.internal.model.Target;
import org.ballerinalang.test.runtime.entity.ModuleStatus;
import org.ballerinalang.test.runtime.entity.TestReport;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.ballerinalang.testerina.core.TestProcessor;
import org.wso2.ballerinalang.util.Lists;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.NativeUtils.createReflectConfig;
import static io.ballerina.cli.utils.TestUtils.generateCoverage;
import static io.ballerina.cli.utils.TestUtils.generateTesterinaReports;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;

/**
 * Task for executing tests.
 *
 * @since 2.3.0
 */
public class RunNativeImageTestTask implements Task {
    private final PrintStream out;
    private final String includesInCoverage;
    private String groupList;
    private String disableGroupList;
    private boolean report;
    private boolean coverage;
    private String coverageReportFormat;
    private boolean isRerunTestExecution;
    private String singleExecTests;
    private Map<String, Module> coverageModules;
    private boolean listGroups;

    TestReport testReport;

    public RunNativeImageTestTask(PrintStream out, boolean rerunTests, String groupList,
                        String disableGroupList, String testList, String includes, String coverageFormat,
                        Map<String, Module> modules, boolean listGroups) {
        this.out = out;
        this.isRerunTestExecution = rerunTests;

        if (disableGroupList != null) {
            this.disableGroupList = disableGroupList;
        } else if (groupList != null) {
            this.groupList = groupList;
        }

        if (testList != null) {
            singleExecTests = testList;
        }
        this.includesInCoverage = includes;
        this.coverageReportFormat = coverageFormat;
        this.coverageModules = modules;
        this.listGroups = listGroups;
    }

    @Override
    public void execute(Project project) {
        long start = 0;
        if (project.buildOptions().dumpBuildTime()) {
            start = System.currentTimeMillis();
        }

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
        for (ModuleDescriptor moduleDescriptor :
                project.currentPackage().moduleDependencyGraph().toTopologicallySortedList()) {
            Module module = project.currentPackage().module(moduleDescriptor.name());
            ModuleName moduleName = module.moduleName();

            TestSuite suite = testProcessor.testSuite(module).orElse(null);
            if (suite == null) {
                continue;
            }

            //Set 'hasTests' flag if there are any tests available in the package
            if (!hasTests) {
                hasTests = true;
            }

            if (!isRerunTestExecution) {
                TestUtils.clearFailedTestsJson(target.path());
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

        TestUtils.writeToTestSuiteJson(testSuiteMap, testsCachePath);

        if (hasTests) {
            int testResult;
            try {
                testResult = runTestSuiteWithNativeImage(project.currentPackage(), jBallerinaBackend, target);

                if (report || coverage) {
                    for (String moduleName : moduleNamesList) {
                        ModuleStatus moduleStatus = TestUtils.loadModuleStatusFromFile(
                                testsCachePath.resolve(moduleName).resolve(TesterinaConstants.STATUS_FILE));

                        if (!moduleName.equals(project.currentPackage().packageName().toString())) {
                            moduleName = ModuleName.from(project.currentPackage().packageName(), moduleName).toString();
                        }
                        testReport.addModuleStatus(moduleName, moduleStatus);
                    }
                    try {
                        generateCoverage(project, testReport, jBallerinaBackend,
                                this.includesInCoverage, this.coverageReportFormat, this.coverageModules);
                        generateTesterinaReports(project, testReport, this.out, target);
                    } catch (IOException e) {
                        TestUtils.cleanTempCache(project, cachesRoot);
                        throw createLauncherException("error occurred while generating test report :", e);
                    }
                }
            } catch (IOException | InterruptedException e) {
                TestUtils.cleanTempCache(project, cachesRoot);
                throw createLauncherException("error occurred while running tests", e);
            }

            if (testResult != 0) {
                TestUtils.cleanTempCache(project, cachesRoot);
                throw createLauncherException("there are test failures");
            }
        }

        // Cleanup temp cache for SingleFileProject
        TestUtils.cleanTempCache(project, cachesRoot);
        if (project.buildOptions().dumpBuildTime()) {
            BuildTime.getInstance().testingExecutionDuration = System.currentTimeMillis() - start;
        }
    }

    private int runTestSuiteWithNativeImage(Package currentPackage, JBallerinaBackend jBallerinaBackend, Target target)
            throws IOException, InterruptedException {
        String packageName = currentPackage.packageName().toString();
        String classPath = getClassPath(jBallerinaBackend, currentPackage);
        String jacocoAgentJarPath = "";

        if (coverage) {
            // Generate the exec in a separate process
            List<String> execArgs = new ArrayList<>();
            execArgs.add(System.getProperty("java.command"));

            String mainClassName = TesterinaConstants.TESTERINA_LAUNCHER_CLASS_NAME;

            jacocoAgentJarPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                    .resolve(BALLERINA_HOME_LIB).resolve(TesterinaConstants.AGENT_FILE_NAME).toString();

            String agentCommand = "-javaagent:" +
                    jacocoAgentJarPath + "=destfile="
                    + target.getTestsCachePath().resolve(TesterinaConstants.COVERAGE_DIR)
                    .resolve(TesterinaConstants.EXEC_FILE_NAME);

            if (!TesterinaConstants.DOT.equals(packageName) && this.includesInCoverage == null) {
                // add user defined classes for generating the jacoco exec file
                agentCommand += ",includes=" + currentPackage.packageOrg().toString() + ".*";
            } else {
                agentCommand += ",includes=" + this.includesInCoverage;
            }

            execArgs.add(agentCommand);
            execArgs.addAll(Lists.of("-cp", classPath));
            execArgs.add(mainClassName);

            // Adds arguments to be read at the Test Runner
            execArgs.add(target.path().toString());
            execArgs.add(jacocoAgentJarPath);
            execArgs.add(Boolean.toString(report));
            execArgs.add(Boolean.toString(coverage));
            execArgs.add(this.groupList != null ? this.groupList : "");
            execArgs.add(this.disableGroupList != null ? this.disableGroupList : "");
            execArgs.add(this.singleExecTests != null ? this.singleExecTests : "");
            execArgs.add(Boolean.toString(isRerunTestExecution));
            execArgs.add(Boolean.toString(listGroups));

            ProcessBuilder processBuilder = new ProcessBuilder(execArgs).inheritIO();
            Process proc = processBuilder.start();

            if (proc.waitFor() != 0) {
                out.println("Jacoco exec generation failed");
            }
        }

        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("native-image");

        Path nativeConfigPath = target.getNativeConfigPath();   // <abs>target/cache/test_cache/native-config
        Path nativeTargetPath = target.getNativePath();         // <abs>target/native

        // Create Configs
        createReflectConfig(nativeConfigPath, currentPackage);

        // Run native-image command with generated configs
        cmdArgs.addAll(Lists.of("-cp", classPath));
        cmdArgs.add(TesterinaConstants.TESTERINA_LAUNCHER_CLASS_NAME);

        // set name and path
        cmdArgs.add("-H:Name=" + packageName);
        cmdArgs.add("-H:Path=" + nativeTargetPath);

        // native-image configs
        cmdArgs.add("-H:ReflectionConfigurationFiles=" + nativeConfigPath.resolve("reflect-config.json"));
        cmdArgs.add("--no-fallback");

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(cmdArgs.toArray(new String[0]));
        builder.inheritIO();
        Process process = builder.start();

        if (process.waitFor() == 0) {
            cmdArgs = new ArrayList<>();

            // Run the generated image
            cmdArgs.add(nativeTargetPath.resolve(packageName).toString());

            // Test Runner Class arguments
            cmdArgs.add(target.path().toString());                                  // 0
            cmdArgs.add(jacocoAgentJarPath);
            cmdArgs.add(Boolean.toString(report));
            cmdArgs.add(Boolean.toString(coverage));
            cmdArgs.add(this.groupList != null ? this.groupList : "");
            cmdArgs.add(this.disableGroupList != null ? this.disableGroupList : "");
            cmdArgs.add(this.singleExecTests != null ? this.singleExecTests : "");
            cmdArgs.add(Boolean.toString(isRerunTestExecution));
            cmdArgs.add(Boolean.toString(listGroups));                              // 8

            builder.command(cmdArgs.toArray(new String[0]));
            builder.inheritIO();
            process = builder.start();
            return process.waitFor();
        } else {
            return 1;
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

        StringJoiner classPath = new StringJoiner(File.pathSeparator);
        dependencies.stream().map(Path::toString).forEach(classPath::add);
        return classPath.toString();
    }

}
