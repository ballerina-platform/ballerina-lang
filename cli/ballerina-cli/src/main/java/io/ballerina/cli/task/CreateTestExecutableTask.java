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
import io.ballerina.cli.utils.BuildUtils;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.cli.utils.NativeUtils;
import io.ballerina.cli.utils.TestUtils;
import io.ballerina.projects.EmitResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.ballerinalang.testerina.core.TestProcessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipFile;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.FileUtils.getFileNameWithoutExtension;
import static io.ballerina.cli.utils.NativeUtils.modifyJarForFunctionMock;
import static io.ballerina.projects.util.ProjectConstants.BLANG_COMPILED_JAR_EXT;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR;

public class CreateTestExecutableTask implements Task {
    private final transient PrintStream out;
    private Path output;
    private Path currentDir;
    private final RunTestsTask runTestsTask;

    public CreateTestExecutableTask(PrintStream out, String output, RunTestsTask runTestsTask) {
        this.out = out;
        if (output != null) {
            this.output = Paths.get(output);
        }
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
            List<Diagnostic> emitDiagnostics = new ArrayList<>();
            Path testCachePath = target.getTestsCachePath();
            long start = 0;
            if (project.buildOptions().dumpBuildTime()) {
                start = System.currentTimeMillis();
            }
            HashSet<JarLibrary> testExecDependencies = new HashSet<>();
            Map<String, TestSuite> testSuiteMap = new HashMap<>();

            // Create and Write the test suite json that is used to execute the tests
            boolean suiteCreated = createTestSuiteForCloudArtifacts(project, jBallerinaBackend, target, testSuiteMap);

            if (suiteCreated) {
                // Write the cmd args to a file, so it can be read on c2c side
                writeCmdArgsToFile(getTestExecutableBasePath(target),
                        target, TestUtils.getJsonFilePath(testCachePath));

                if (project.buildOptions().nativeImage()) {
                    NativeUtils.createReflectConfig(target.getNativeConfigPath(),
                            project.currentPackage(), testSuiteMap);
                    // Traverse the map and check if a suite has mock functions
                    boolean hasMockFunctions = false;
                    for (Map.Entry<String, TestSuite> entry : testSuiteMap.entrySet()) {
                        if (!entry.getValue().getMockFunctionNamesMap().isEmpty()) {
                            hasMockFunctions = true;
                            break;
                        }
                    }
                    if (hasMockFunctions) {
                        perModuleFatJarGeneration(testSuiteMap, target, jBallerinaBackend, emitDiagnostics, project);
                    } else {
                        standaloneFatJarGeneration(project, jBallerinaBackend, target, testExecDependencies,
                                testCachePath, emitDiagnostics);
                    }
                } else {
                    standaloneFatJarGeneration(project, jBallerinaBackend, target, testExecDependencies,
                            testCachePath, emitDiagnostics);
                }
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
        BuildUtils.notifyPlugins(project, target);
    }

    private void standaloneFatJarGeneration(Project project, JBallerinaBackend jBallerinaBackend, Target target,
                                           HashSet<JarLibrary> testExecDependencies, Path testCachePath,
                                            List<Diagnostic> emitDiagnostics) throws IOException {
        // Get all the dependencies required for test execution for each module
        for (ModuleDescriptor moduleDescriptor :
                project.currentPackage().moduleDependencyGraph().toTopologicallySortedList()) {
            Module module = project.currentPackage().module(moduleDescriptor.name());
            testExecDependencies.addAll(jBallerinaBackend.jarResolver()
                    .getJarFilePathsRequiredForTestExecution(module.moduleName())
            );
        }

        Path testExecutablePath = getTestExecutableBasePath(target).resolve(
                project.currentPackage().packageName().toString() +
                        ProjectConstants.TEST_UBER_JAR_SUFFIX +
                        ProjectConstants.BLANG_COMPILED_JAR_EXT);

        List<Path> moduleJarPaths = TestUtils.getModuleJarPaths(jBallerinaBackend, project.currentPackage());
        List<String> excludingClassPaths = new ArrayList<>();
        for (Path moduleJarPath : moduleJarPaths) {
            ZipFile zipFile = new ZipFile(moduleJarPath.toFile());
            zipFile.stream().forEach(entry -> {
                if (entry.getName().endsWith(ProjectConstants.JAVA_CLASS_EXT)) {
                    excludingClassPaths.add(entry.getName().replace(File.separator, ProjectConstants.DOT)
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
                TestUtils.getJsonFilePathInFatJar(File.separator),
                excludingClassPaths,
                ProjectConstants.EXCLUDING_CLASSES_FILE
        );
        emitDiagnostics.addAll(result.diagnostics().diagnostics());
    }

    private void perModuleFatJarGeneration(Map<String, TestSuite> testSuiteMap, Target target,
                                           JBallerinaBackend jBallerinaBackend, List<Diagnostic> emitDiagnostics,
                                           Project project)
            throws IOException {
        // Clone the map to the count of test suites
        int testSuiteCount = 0;
        List<Map<String, TestSuite>> clonedMaps = new ArrayList<>();
        for (int i = 0; i < testSuiteMap.size(); i++) {
            clonedMaps.add(new HashMap<>(testSuiteMap));
            testSuiteCount++;
        }

        // For each map, remove all the test suites except the one at the current index
        for (int i = 0; i < testSuiteCount; i++) {
            Map<String, TestSuite> clonedMap = clonedMaps.get(i);
            Iterator<Map.Entry<String, TestSuite>> iterator = clonedMap.entrySet().iterator();
            int index = 0;
            while (iterator.hasNext()) {
                Map.Entry<String, TestSuite> entry = iterator.next();
                if (index != i) {
                    iterator.remove();
                }
                index++;
            }
        }

        List<ModuleName> moduleNames = project.currentPackage().moduleDependencyGraph()
                .toTopologicallySortedList().stream().map(ModuleDescriptor::name).toList();
        // Modify the relevant jars of each test suite
        for (Map<String, TestSuite> clonedMap : clonedMaps) {
            TestSuite testSuite = clonedMap.values().toArray(new TestSuite[0])[0];
            String moduleName = testSuite.getPackageID();
            NativeUtils.createReflectConfig(target.getNativeConfigPath(),
                    project.currentPackage(), clonedMap);    // Rewrite the reflect config for each module
            try {
                modifyJarForFunctionMock(testSuite, target, moduleName);
            } catch (IOException e) {
                throw createLauncherException("error occurred while running tests", e);
            }

            ModuleName moduleNameObj = moduleNames.stream().filter(name -> name.toString().equals(moduleName))
                    .findFirst().orElseThrow();
            // Remove the mock function classes from the test suite
            testSuite.removeAllMockFunctions();
            Collection<JarLibrary> testDependencies = jBallerinaBackend.jarResolver()
                    .getJarFilePathsRequiredForTestExecution(moduleNameObj);

            // Filter the testDependencies from the testSuite's test dependencies
            Collection<Path> neededDependencies = testSuite.getTestExecutionDependencies().stream()
                    .map(Paths::get).toList();
            HashSet<JarLibrary> filteredTestDependencies = new HashSet<>();
            neededDependencies.forEach(neededDependency -> {
                String comparingStr = TesterinaConstants.HYPHEN
                        + TesterinaConstants.MODIFIED
                        + BLANG_COMPILED_JAR_EXT;
                String neededDependencyFileName = neededDependency.getFileName().toString();
                if (neededDependencyFileName.contains(comparingStr)) {
                    String originalFileName = neededDependencyFileName.replace(comparingStr, "");
                    Optional<JarLibrary> foundDependency = testDependencies.stream().filter(dep ->
                            dep.path().getFileName().toString().contains(originalFileName) &&
                                    !dep.path().getFileName().toString().contains(TesterinaConstants.TESTABLE)
                    ).findFirst();
                    //TODO:: if modified found, find the original jar and get its scope to create the jar library
                    //TODO:: use modified jar path to create the jar library
                    if (foundDependency.isPresent()) {
                        JarLibrary modifiedJarLibrary = new JarLibrary(neededDependency,
                                foundDependency.get().scope());
                        filteredTestDependencies.add(modifiedJarLibrary);
                        testDependencies.remove(foundDependency.get());
                    }
                }
            });

            // Add the remaining dependencies
            filteredTestDependencies.addAll(testDependencies);

            // Remove the test execution dependencies from the test suite and write to json
            testSuite.removeAllTestExecutionDependencies();
            try {
                TestUtils.writeToTestSuiteJson(clonedMap, target.getTestsCachePath());
            } catch (IOException e) {
                throw createLauncherException("error while writing to test suite json file: " + e.getMessage());
            }

            Path testExecutablePath = getTestExecutableBasePath(target).resolve(
                    moduleName + ProjectConstants.TEST_UBER_JAR_SUFFIX + ProjectConstants.BLANG_COMPILED_JAR_EXT);
            // Create the fat jar for the test suite
            // excluding class paths are not needed because we do not modify any classes in this scenario
            EmitResult result = jBallerinaBackend.emit(
                    JBallerinaBackend.OutputType.TEST,
                    testExecutablePath,
                    filteredTestDependencies,
                    TestUtils.getJsonFilePath(target.getTestsCachePath()),
                    TestUtils.getJsonFilePathInFatJar(File.separator),
                    new ArrayList<>(),
                    ProjectConstants.EXCLUDING_CLASSES_FILE
            );
            emitDiagnostics.addAll(result.diagnostics().diagnostics());
        }
    }

    private boolean createTestSuiteForCloudArtifacts(Project project, JBallerinaBackend jBallerinaBackend,
                                                     Target target, Map<String, TestSuite> testSuiteMap) {
        boolean report = project.buildOptions().testReport();
        boolean coverage = project.buildOptions().codeCoverage();
        TestProcessor testProcessor = new TestProcessor(jBallerinaBackend.jarResolver());
        List<String> moduleNamesList = new ArrayList<>();
        List<String> updatedSingleExecTests;
        List<String> mockClassNames = new ArrayList<>();
        boolean hasTests = RunTestsTask.createTestSuitesForProject(project, target, testProcessor, testSuiteMap,
                moduleNamesList, mockClassNames, runTestsTask.isRerunTestExecution(), report, coverage);

        // Set the module names list and the mock classes to the run tests task
        this.runTestsTask.setModuleNamesList(moduleNamesList);
        this.runTestsTask.setMockClasses(mockClassNames);
        if (hasTests) {
            // Now write the map to a json file
            try {
                TestUtils.writeToTestSuiteJson(testSuiteMap, target.getTestsCachePath());
                return true;
            } catch (IOException e) {
                throw createLauncherException("error while writing to test suite json file: " + e.getMessage());
            }
        } else {
            out.println("\tNo tests found");
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

    private Target getTarget(Project project) {
        Target target;
        try {
            if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
                target = new Target(project.targetDir());
            } else {
                target = new Target(Files.createTempDirectory("ballerina-cache" + System.nanoTime()));
                target.setOutputPath(getExecutablePath(project));
            }
        } catch (IOException e) {
            throw createLauncherException("unable to resolve target path:" + e.getMessage());
        } catch (ProjectException e) {
            throw createLauncherException("unable to create executable:" + e.getMessage());
        }
        return target;
    }

    private Path getExecutablePath(Project project) {

        Path fileName = project.sourceRoot().getFileName();

        // If the --output flag is not set, create the executable in the current directory
        if (this.output == null) {
            return this.currentDir.resolve(getFileNameWithoutExtension(fileName) + BLANG_COMPILED_JAR_EXT);
        }

        if (!this.output.isAbsolute()) {
            this.output = currentDir.resolve(this.output);
        }

        // If the --output is a directory create the executable in the given directory
        if (Files.isDirectory(this.output)) {
            return output.resolve(getFileNameWithoutExtension(fileName) + BLANG_COMPILED_JAR_EXT);
        }

        // If the --output does not have an extension, append the .jar extension
        if (!FileUtils.hasExtension(this.output)) {
            return Paths.get(this.output.toString() + BLANG_COMPILED_JAR_EXT);
        }

        return this.output;
    }
}
