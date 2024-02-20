// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package io.ballerina.cli.task;

import io.ballerina.cli.utils.BuildTime;
import io.ballerina.cli.utils.TestUtils;
import io.ballerina.projects.EmitResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.testerina.core.TestProcessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR;

public class CreateTestExecutableTask extends CreateExecutableTask {
    private final RunTestsTask runTestsTask;

    public CreateTestExecutableTask(PrintStream out, String output, RunTestsTask runTestsTask) {
        super(out, output);
        this.runTestsTask = runTestsTask;   // To invoke related methods in the run tests task
    }

    @Override
    public void execute(Project project) {
        this.out.println();

        this.currentDir = Paths.get(System.getProperty(USER_DIR));
        Target target = getTarget(project);

        try {
            PackageCompilation pkgCompilation = project.currentPackage().getCompilation();
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(pkgCompilation, JvmTarget.JAVA_17);
            JarResolver jarResolver = jBallerinaBackend.jarResolver();

            List<Diagnostic> emitDiagnostics = new ArrayList<>();
            Path testCachePath = target.getTestsCachePath();

            long start = 0;
            if (project.buildOptions().dumpBuildTime()) {
                start = System.currentTimeMillis();
            }

            HashSet<JarLibrary> testExecDependencies = new HashSet<>();
            Map<String, TestSuite> testSuiteMap = new HashMap<>();

            // Write the test suite json that is used to execute the tests
            boolean status = createTestSuiteForCloudArtifacts(project, jBallerinaBackend, target, testSuiteMap);

            // Get all the dependencies required for test execution for each module
            for (ModuleDescriptor moduleDescriptor :
                    project.currentPackage().moduleDependencyGraph().toTopologicallySortedList()) {

                Module module = project.currentPackage().module(moduleDescriptor.name());
                testExecDependencies.addAll(jarResolver
                        .getJarFilePathsRequiredForTestExecution(module.moduleName())
                );
            }

            if (status) {
                Path testExecutablePath = getTestExecutableBasePath(target).resolve(
                        project.currentPackage().packageName().toString() +
                                ProjectConstants.TEST_UBER_JAR_SUFFIX +
                                ProjectConstants.BLANG_COMPILED_JAR_EXT);

                // Write the cmd args to a file, so it can be read on c2c side
                writeCmdArgsToFile(testExecutablePath.getParent(), target, TestUtils.getJsonFilePath(testCachePath));

                List<Path> moduleJarPaths = TestUtils.getModuleJarPaths(jBallerinaBackend, project.currentPackage());

                List<String> excludingClassPaths = new ArrayList<>();

                for (Path moduleJarPath : moduleJarPaths) {
                    ZipFile zipFile = new ZipFile(moduleJarPath.toFile());

                    zipFile.stream().forEach(entry -> {
                        if (entry.getName().endsWith(ProjectConstants.JAVA_CLASS_EXT)) {
                            excludingClassPaths.add(entry.getName().replace(File.pathSeparator, ProjectConstants.DOT)
                                    .replace(ProjectConstants.JAVA_CLASS_EXT, ""));
                        }
                    });

                    zipFile.close();
                }

                // Create the single fat jar for all the test modules that includes the test suite json
                EmitResult result = jBallerinaBackend.emit(
                        JBallerinaBackend.OutputType.TEST,
                        testExecutablePath,
                        testExecDependencies,
                        TestUtils.getJsonFilePath(testCachePath),
                        TestUtils.getJsonFilePathInFatJar(File.pathSeparator),
                        excludingClassPaths,
                        ProjectConstants.EXCLUDING_CLASSES_FILE
                );
                emitDiagnostics.addAll(result.diagnostics().diagnostics());
            }

            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().emitArtifactDuration = System.currentTimeMillis() - start;
                BuildTime.getInstance().compile = false;
            }

            // Print warnings for conflicted jars
            if (!jBallerinaBackend.conflictedJars().isEmpty()) {
                out.println("\twarning: Detected conflicting jar files:");
                for (JBallerinaBackend.JarConflict conflict : jBallerinaBackend.conflictedJars()) {
                    out.println(conflict.getWarning(project.buildOptions().listConflictedClasses()));
                }
            }

            if (!emitDiagnostics.isEmpty()) {
                emitDiagnostics.forEach(d -> out.println("\n" + d.toString()));
            }
        } catch (ProjectException | IOException e) {
            throw createLauncherException(e.getMessage());
        }
        // notify plugin
        // todo following call has to be refactored after introducing new plugin architecture
        // Similar case as in CreateExecutableTask.java
        notifyPlugins(project, target);
    }

    private boolean createTestSuiteForCloudArtifacts(Project project, JBallerinaBackend jBallerinaBackend,
                                                     Target target, Map<String, TestSuite> testSuiteMap) {
        boolean report = project.buildOptions().testReport();
        boolean coverage = project.buildOptions().codeCoverage();

        TestProcessor testProcessor = new TestProcessor(jBallerinaBackend.jarResolver());
        List<String> moduleNamesList = new ArrayList<>();
        List<String> updatedSingleExecTests;
        List<String> mockClassNames = new ArrayList<>();

        boolean status = RunTestsTask.createTestSuitesForProject(project, target, testProcessor, testSuiteMap,
                moduleNamesList, mockClassNames, runTestsTask.isRerunTestExecution(), report, coverage);

        // Set the module names list and the mock classes to the run tests task
        this.runTestsTask.setModuleNamesList(moduleNamesList);
        this.runTestsTask.setMockClasses(mockClassNames);

        if (status) {
            // Now write the map to a json file
            try {
                TestUtils.writeToTestSuiteJson(testSuiteMap, target.getTestsCachePath());
                return true;
            } catch (IOException e) {
                throw createLauncherException("error while writing to test suite json file: " + e.getMessage());
            }
        } else {
            return false;
        }
    }

    private Path getTestExecutableBasePath(Target target) {
        try {
            return target.getTestExecutableBasePath();
        } catch (IOException e) {
            throw createLauncherException(e.getMessage());
        }
    }

    private void writeCmdArgsToFile(Path path, Target target, Path testSuiteJsonPath) {
            List<String> cmdArgs = new ArrayList<>();

            TestUtils.appendRequiredArgs(
                    cmdArgs, target.path().toString(), TestUtils.getJacocoAgentJarPath(),
                    testSuiteJsonPath.toString(), this.runTestsTask.isReport(),
                    this.runTestsTask.isCoverage(), this.runTestsTask.getGroupList(),
                    this.runTestsTask.getDisableGroupList(), this.runTestsTask.getSingleExecTests(),
                    this.runTestsTask.isRerunTestExecution(), this.runTestsTask.isListGroups(),
                    this.runTestsTask.getCliArgs(), false
            );

            // Write the cmdArgs to a file in path
            Path writingPath = path.resolve(ProjectConstants.TEST_RUNTIME_MAIN_ARGS_FILE);

            try (BufferedWriter writer = java.nio.file.Files.newBufferedWriter(writingPath)) {
                for (String arg : cmdArgs) {
                    writer.write(arg);
                    writer.newLine();
                }
            } catch (IOException e) {
                throw createLauncherException("error while writing to file: " + e.getMessage());
            }
    }
}
