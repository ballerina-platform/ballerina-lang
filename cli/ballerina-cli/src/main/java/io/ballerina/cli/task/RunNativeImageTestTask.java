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
import io.ballerina.cli.utils.GraalVMCompatibilityUtils;
import io.ballerina.cli.utils.NativeUtils;
import io.ballerina.cli.utils.TestUtils;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.test.runtime.entity.ModuleStatus;
import org.ballerinalang.test.runtime.entity.TestReport;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.ballerinalang.testerina.core.TestProcessor;
import org.wso2.ballerinalang.util.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.TestUtils.generateTesterinaReports;
import static io.ballerina.projects.util.ProjectConstants.BIN_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.TESTS_CACHE_DIR_NAME;
import static io.ballerina.projects.util.ProjectUtils.getResourcesPath;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.CACHE_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.TESTERINA_TEST_SUITE;

/**
 * Task for executing tests.
 *
 * @since 2.3.0
 */
public class RunNativeImageTestTask implements Task {
    private static final String WIN_EXEC_EXT = "exe";

    private static class StreamGobbler extends Thread {
        private final InputStream inputStream;
        private final PrintStream printStream;

        public StreamGobbler(InputStream inputStream, PrintStream printStream) {
            this.inputStream = inputStream;
            this.printStream = printStream;
        }

        @Override
        public void run() {
            try (Scanner sc = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                while (sc.hasNextLine()) {
                    printStream.println(sc.nextLine());
                }
            }
        }
    }

    private final PrintStream out;
    private String groupList;
    private String disableGroupList;
    private boolean report;
    private boolean coverage;
    private final boolean isRerunTestExecution;
    private String singleExecTests;
    private final boolean listGroups;
    private final boolean  isParallelExecution;

    TestReport testReport;

    public RunNativeImageTestTask(PrintStream out, boolean rerunTests, String groupList,
                                  String disableGroupList, String testList, String includes, String coverageFormat,
                                  Map<String, Module> modules, boolean listGroups, boolean isParallelExecution) {
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
        this.listGroups = listGroups;
        this.isParallelExecution = isParallelExecution;
    }

    @Override
    public void execute(Project project) {
        long start = 0;
        if (project.buildOptions().dumpBuildTime()) {
            start = System.currentTimeMillis();
        }

        report = project.buildOptions().testReport();
        coverage = project.buildOptions().codeCoverage();


        if (report) {
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
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
        JarResolver jarResolver = jBallerinaBackend.jarResolver();
        TestProcessor testProcessor = new TestProcessor(jarResolver);
        List<String> updatedSingleExecTests;
        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "bal test" command which only executes tests
        // in packages.

        // Create seperate test suite map for each module.
        List<HashMap<String, TestSuite>> testSuiteMapEntries = new ArrayList<>();
        boolean isMockFunctionExist = false;
        for (ModuleDescriptor moduleDescriptor :
                project.currentPackage().moduleDependencyGraph().toTopologicallySortedList()) {
            HashMap<String, TestSuite> testSuiteMap = new HashMap<>();
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
            suite.setReportRequired(report);
            if (!isMockFunctionExist) {
                isMockFunctionExist = !suite.getMockFunctionNamesMap().isEmpty();
            }

            String resolvedModuleName =
                    module.isDefaultModule() ? moduleName.toString() : module.moduleName().moduleNamePart();
            testSuiteMap.put(resolvedModuleName, suite);
            testSuiteMapEntries.add(testSuiteMap);
        }

        if (!hasTests) {
            out.println("\tNo tests found");
        }

        // If the function mocking does not exist, combine all test suite map entries
        if (!isMockFunctionExist && !testSuiteMapEntries.isEmpty()) {
            HashMap<String, TestSuite> testSuiteMap = testSuiteMapEntries.remove(0);
            while (!testSuiteMapEntries.isEmpty()) {
                testSuiteMap.putAll(testSuiteMapEntries.remove(0));
            }
            testSuiteMapEntries.add(testSuiteMap);
        }

        int accumulatedTestResult = 0;
        //Execute each testsuite within list one by one
        for (Map<String, TestSuite> testSuiteMap : testSuiteMapEntries) {
            try {
                Path nativeConfigPath = target.getNativeConfigPath();
                NativeUtils.createReflectConfig(nativeConfigPath, project.currentPackage(), testSuiteMap);
            } catch (IOException e) {
                throw createLauncherException("error while generating the necessary graalvm reflection config ", e);
            }

            // Try to modify jar if the testsuite map contains only one entry
            if (testSuiteMap.size() == 1) {
                TestSuite testSuite = testSuiteMap.values().toArray(new TestSuite[0])[0];
                String moduleName = testSuite.getPackageID();
                try {
                    NativeUtils.modifyJarForFunctionMock(testSuite, target, moduleName);
                } catch (IOException e) {
                    throw createLauncherException("error occurred while running tests", e);
                }
            }



            //Remove all mock function entries from test suites
            for (Map.Entry<String, TestSuite> testSuiteEntry : testSuiteMap.entrySet()) {
                TestSuite testSuite = testSuiteEntry.getValue();
                if (!testSuite.getMockFunctionNamesMap().isEmpty()) {
                    testSuite.removeAllMockFunctions();
                }
            }

            //Write the testsuite to the disk
            TestUtils.writeToTestSuiteJson(testSuiteMap, testsCachePath);

            if (hasTests) {
                int testResult = 1;
                try {
                    String warnings = GraalVMCompatibilityUtils.getAllWarnings(
                            project.currentPackage(), jBallerinaBackend.targetPlatform().code(), true);
                    if (!warnings.isEmpty()) {
                        out.println(warnings);
                    }
                    testResult = runTestSuiteWithNativeImage(project.currentPackage(), target, testSuiteMap);
                    if (testResult != 0) {
                        accumulatedTestResult = testResult;
                    }
                    if (report) {
                        for (Map.Entry<String, TestSuite> testSuiteEntry : testSuiteMap.entrySet()) {
                            String moduleName = testSuiteEntry.getKey();
                            ModuleStatus moduleStatus = TestUtils.loadModuleStatusFromFile(
                                    testsCachePath.resolve(moduleName).resolve(TesterinaConstants.STATUS_FILE));
                            if (moduleStatus == null) {
                                continue;
                            }

                            if (!moduleName.equals(project.currentPackage().packageName().toString())) {
                                moduleName = ModuleName.from(project.currentPackage().packageName(),
                                        moduleName).toString();
                            }
                            testReport.addModuleStatus(moduleName, moduleStatus);
                        }
                    }
                } catch (IOException e) {
                    TestUtils.cleanTempCache(project, cachesRoot);
                    throw createLauncherException("error occurred while running tests", e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if (report && hasTests) {
            try {
                generateTesterinaReports(project, testReport, this.out, target);
            } catch (IOException e) {
                TestUtils.cleanTempCache(project, cachesRoot);
                throw createLauncherException("error occurred while generating test report:", e);
            }
        }

        if (accumulatedTestResult != 0) {
            TestUtils.cleanTempCache(project, cachesRoot);
            throw createLauncherException("there are test failures");
        }


        // Cleanup temp cache for SingleFileProject
        TestUtils.cleanTempCache(project, cachesRoot);
        if (project.buildOptions().dumpBuildTime()) {
            BuildTime.getInstance().testingExecutionDuration = System.currentTimeMillis() - start;
        }
    }

    private int runTestSuiteWithNativeImage(Package currentPackage, Target target,
                                            Map<String, TestSuite> testSuiteMap)
            throws IOException, InterruptedException {
        String packageName = currentPackage.packageName().toString();
        String classPath = NativeUtils.getClassPath(testSuiteMap);

        String jacocoAgentJarPath = "";
        String nativeImageCommand = System.getenv("GRAALVM_HOME");

        try {
            if (nativeImageCommand == null) {
                throw new ProjectException("GraalVM installation directory not found. Set GRAALVM_HOME as an " +
                        "environment variable\nHINT: To install GraalVM, follow the link: " +
                        "https://ballerina.io/learn/build-the-executable-locally/#configure-graalvm");
            }
            nativeImageCommand += File.separator + BIN_DIR_NAME + File.separator
                    + (NativeUtils.OS.contains("win") ? "native-image.cmd" : "native-image");

            File commandExecutable = Paths.get(nativeImageCommand).toFile();
            if (!commandExecutable.exists()) {
                throw new ProjectException("Cannot find '" + commandExecutable.getName() + "' in the GRAALVM_HOME/bin "
                        + "directory. Install it using: gu install native-image");
            }
        } catch (ProjectException e) {
            throw createLauncherException(e.getMessage());
        }

        List<String> cmdArgs = new ArrayList<>();
        List<String> nativeArgs = new ArrayList<>();

        String graalVMBuildOptions = currentPackage.project().buildOptions().graalVMBuildOptions();
        nativeArgs.add(graalVMBuildOptions);

        cmdArgs.add(nativeImageCommand);

        Path nativeConfigPath = target.getNativeConfigPath();   // <abs>target/cache/test_cache/native-config
        Path nativeTargetPath = target.getNativePath();         // <abs>target/native

        // Run native-image command with generated configs
        cmdArgs.add(TesterinaConstants.TESTERINA_LAUNCHER_CLASS_NAME);
        cmdArgs.add("@" + (nativeConfigPath.resolve("native-image-args.txt")));
        nativeArgs.addAll(Lists.of("-cp", classPath));

        if (currentPackage.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            String[] splittedArray = currentPackage.project().sourceRoot().toString().
                    replace(ProjectConstants.BLANG_SOURCE_EXT, "").split("/");
            packageName = splittedArray[splittedArray.length - 1];
            validateResourcesWithinJar(testSuiteMap, packageName);
        } else if (testSuiteMap.size() == 1) {
            packageName = (testSuiteMap.values().toArray(new TestSuite[0])[0]).getPackageID();
        }


        // set name and path
        nativeArgs.add("-H:Name=" + NativeUtils.addQuotationMarkToString(packageName));
        nativeArgs.add("-H:Path=" + NativeUtils.convertWinPathToUnixFormat(NativeUtils
                .addQuotationMarkToString(nativeTargetPath.toString())));

        // native-image configs
        nativeArgs.add("-H:ReflectionConfigurationFiles=" + NativeUtils
                .convertWinPathToUnixFormat(NativeUtils.addQuotationMarkToString(
                nativeConfigPath.resolve("reflect-config.json").toString())));
        nativeArgs.add("--no-fallback");


        // There is a command line length limit in Windows. Therefore, we need to write the arguments to a file and
        // use it as an argument.
        try (FileWriter nativeArgumentWriter = new FileWriter(nativeConfigPath.resolve("native-image-args.txt")
                .toString(), Charset.defaultCharset())) {
            nativeArgumentWriter.write(String.join(" ", nativeArgs));
            nativeArgumentWriter.flush();
        } catch (IOException e) {
            throw createLauncherException("error while generating the necessary graalvm argument file", e);
        }

        ProcessBuilder builder = (new ProcessBuilder()).redirectErrorStream(true);
        builder.command(cmdArgs.toArray(new String[0]));
        Process process = builder.start();
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), out);
        outputGobbler.start();

        if (process.waitFor() == 0) {
            outputGobbler.join();
            cmdArgs = new ArrayList<>();

            // Run the generated image
            String generatedImagePath = nativeTargetPath.resolve(packageName) + getGeneratedImageExtension();
            cmdArgs.add(generatedImagePath);

            // Test Runner Class arguments
            cmdArgs.add(Boolean.toString(false));                                 //0
            cmdArgs.add(target.path().resolve(CACHE_DIR).resolve(TESTS_CACHE_DIR_NAME)
                    .resolve(TESTERINA_TEST_SUITE).toString());
            cmdArgs.add(target.path().toString());
            cmdArgs.add(jacocoAgentJarPath);
            cmdArgs.add(Boolean.toString(report));
            cmdArgs.add(Boolean.toString(coverage));
            cmdArgs.add(this.groupList != null ? this.groupList : "");
            cmdArgs.add(this.disableGroupList != null ? this.disableGroupList : "");
            cmdArgs.add(this.singleExecTests != null ? this.singleExecTests : "");
            cmdArgs.add(Boolean.toString(isRerunTestExecution));
            cmdArgs.add(Boolean.toString(listGroups));                              // 10             // 8
            cmdArgs.add(Boolean.toString(isParallelExecution));

            builder.command(cmdArgs.toArray(new String[0]));
            process = builder.start();
            outputGobbler = new StreamGobbler(process.getInputStream(), out);
            outputGobbler.start();
            int exitCode = process.waitFor();
            outputGobbler.join();
            return exitCode;
        } else {
            return 1;
        }
    }

    private void validateResourcesWithinJar(Map<String, TestSuite> testSuiteMap, String packageName)
            throws IOException {
        TestSuite testSuite = testSuiteMap.values().toArray(new TestSuite[0])[0];
        List<String> dependencies = testSuite.getTestExecutionDependencies();
        String jarPath = "";
        for (String dependency : dependencies) {
            if (dependency.contains(packageName + ".jar")) {
                jarPath = dependency;
            }
        }
        try (ZipInputStream jarInputStream = new ZipInputStream(new FileInputStream(jarPath))) {
            ZipEntry entry;
            boolean isResourceExist = false;
            while ((entry = jarInputStream.getNextEntry()) != null) {
                String path = entry.getName();
                if (path.startsWith("resources/")) {
                    isResourceExist = true;
                }
                jarInputStream.closeEntry();
            }
            if (isResourceExist) {
                throw createLauncherException("native image testing is not supported for standalone " +
                        "Ballerina files containing resources");
            }
        }
    }

    private String getGeneratedImageExtension() {
        if (NativeUtils.OS.contains("win")) {
            return DOT + WIN_EXEC_EXT;
        }
        return "";
    }
}
