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
import org.apache.commons.compress.utils.IOUtils;
import org.ballerinalang.test.runtime.entity.MockFunctionReplaceVisitor;
import org.ballerinalang.test.runtime.entity.ModuleStatus;
import org.ballerinalang.test.runtime.entity.TestReport;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.ballerinalang.test.runtime.util.TesterinaUtils;
import org.ballerinalang.testerina.core.TestProcessor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.wso2.ballerinalang.util.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.NativeUtils.createReflectConfig;
import static io.ballerina.cli.utils.NativeUtils.getURLList;
import static io.ballerina.cli.utils.TestUtils.generateTesterinaReports;
import static io.ballerina.projects.util.ProjectConstants.BIN_DIR_NAME;
import static io.ballerina.runtime.api.constants.RuntimeConstants.FILE_NAME_PERIOD_SEPARATOR;
import static java.util.Objects.requireNonNull;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.CACHE_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.CLASS_EXTENSION;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT_REPLACER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.HYPHEN;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.JAR_EXTENSION;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.JAVA_11_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_FN_DELIMITER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_FUNC_NAME_PREFIX;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_LEGACY_DELIMITER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MODIFIED;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.PATH_SEPARATOR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.TESTABLE;

/**
 * Task for executing tests.
 *
 * @since 2.3.0
 */
public class RunNativeImageTestTask implements Task {

    private static final String OS = System.getProperty("os.name").toLowerCase(Locale.getDefault());
    private static final String WIN_EXEC_EXT = "exe";

    private static class StreamGobbler extends Thread {
        private InputStream inputStream;
        private PrintStream printStream;

        public StreamGobbler(InputStream inputStream, PrintStream printStream) {
            this.inputStream = inputStream;
            this.printStream = printStream;
        }

        @Override
        public void run() {
            Scanner sc = new Scanner(inputStream, StandardCharsets.UTF_8);
            while (sc.hasNextLine()) {
                printStream.println(sc.nextLine());
            }
        }
    }

    private final PrintStream out;
    private String groupList;
    private String disableGroupList;
    private boolean report;
    private boolean coverage;
    private boolean isRerunTestExecution;
    private String singleExecTests;
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
        this.listGroups = listGroups;
    }


    private static byte[] getModifiedClassBytes(String className, List<String> functionNames, TestSuite suite,
                                                ClassLoader classLoader) {
        Class<?> functionToMockClass;
        try {
            functionToMockClass = classLoader.loadClass(className);
        } catch (Throwable e) {
            throw createLauncherException("failed to load class: " + className);
        }

        byte[] classFile = new byte[0];
        boolean readFromBytes = false;
        for (Method method1 : functionToMockClass.getDeclaredMethods()) {
            if (functionNames.contains(MOCK_FN_DELIMITER + method1.getName())) {
                String desugaredMockFunctionName = MOCK_FUNC_NAME_PREFIX + method1.getName();
                String testClassName = TesterinaUtils.getQualifiedClassName(suite.getOrgName(),
                        suite.getTestPackageID(), suite.getVersion(),
                        suite.getPackageID().replace(DOT, FILE_NAME_PERIOD_SEPARATOR));
                Class<?> testClass;
                try {
                    testClass = classLoader.loadClass(testClassName);
                } catch (Throwable e) {
                    throw createLauncherException("failed to prepare " + testClassName + " for mocking reason:" +
                            e.getMessage());
                }
                for (Method method2 : testClass.getDeclaredMethods()) {
                    if (method2.getName().equals(desugaredMockFunctionName)) {
                        if (!readFromBytes) {
                            classFile = replaceMethodBody(method1, method2);
                            readFromBytes = true;
                        } else {
                            classFile = replaceMethodBody(classFile, method1, method2);
                        }
                    }
                }
            } else if (functionNames.contains(MOCK_LEGACY_DELIMITER + method1.getName())) {
                String key = className + MOCK_LEGACY_DELIMITER + method1.getName();
                String mockFunctionName = suite.getMockFunctionNamesMap().get(key);
                if (mockFunctionName != null) {
                    String mockFunctionClassName = suite.getTestUtilityFunctions().get(mockFunctionName);
                    Class<?> mockFunctionClass;
                    try {
                        mockFunctionClass = classLoader.loadClass(mockFunctionClassName);
                    } catch (ClassNotFoundException e) {
                        throw createLauncherException("failed to prepare " + mockFunctionClassName +
                                " for mocking reason:" + e.getMessage());
                    }
                    for (Method method2 : mockFunctionClass.getDeclaredMethods()) {
                        if (method2.getName().equals(mockFunctionName)) {
                            if (!readFromBytes) {
                                classFile = replaceMethodBody(method1, method2);
                                readFromBytes = true;
                            } else {
                                classFile = replaceMethodBody(classFile, method1, method2);
                            }
                        }
                    }
                }
            }
        }
        return classFile;
    }

    private static byte[] replaceMethodBody(Method method, Method mockMethod) {
        Class<?> clazz = method.getDeclaringClass();
        ClassReader cr;
        try {
            InputStream ins;
            ins = clazz.getResourceAsStream(clazz.getSimpleName() + CLASS_EXTENSION);
            cr = new ClassReader(requireNonNull(ins));
        } catch (IOException e) {
            throw createLauncherException("failed to get the class reader object for the class "
                    + clazz.getSimpleName());
        }
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new MockFunctionReplaceVisitor(Opcodes.ASM7, cw, method.getName(),
                Type.getMethodDescriptor(method), mockMethod);
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    private static byte[] replaceMethodBody(byte[] classFile, Method method, Method mockMethod) {
        ClassReader cr = new ClassReader(classFile);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new MockFunctionReplaceVisitor(Opcodes.ASM7, cw, method.getName(),
                Type.getMethodDescriptor(method), mockMethod);
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    //Get all mocked functions in a class
    private static void populateClassNameVsFunctionToMockMap(Map<String, List<String>> classVsMockFunctionsMap,
                                                             Map<String, String> mockFunctionMap) {
        for (Map.Entry<String, String> entry : mockFunctionMap.entrySet()) {
            String key = entry.getKey();
            String functionToMockClassName;
            String functionToMock;
            if (key.indexOf(MOCK_LEGACY_DELIMITER) == -1) {
                functionToMockClassName = key.substring(0, key.indexOf(MOCK_FN_DELIMITER));
                functionToMock = key.substring(key.indexOf(MOCK_FN_DELIMITER));
            } else if (key.indexOf(MOCK_FN_DELIMITER) == -1) {
                functionToMockClassName = key.substring(0, key.indexOf(MOCK_LEGACY_DELIMITER));
                functionToMock = key.substring(key.indexOf(MOCK_LEGACY_DELIMITER));
            } else {
                if (key.indexOf(MOCK_FN_DELIMITER) < key.indexOf(MOCK_LEGACY_DELIMITER)) {
                    functionToMockClassName = key.substring(0, key.indexOf(MOCK_FN_DELIMITER));
                    functionToMock = key.substring(key.indexOf(MOCK_FN_DELIMITER));
                } else {
                    functionToMockClassName = key.substring(0, key.indexOf(MOCK_LEGACY_DELIMITER));
                    functionToMock = key.substring(key.indexOf(MOCK_LEGACY_DELIMITER));
                }
            }
            functionToMock = functionToMock.replaceAll("\\\\", "");
            classVsMockFunctionsMap.computeIfAbsent(functionToMockClassName,
                    k -> new ArrayList<>()).add(functionToMock);
        }
    }

    @Override
    public void execute(Project project) {
        long start = 0;
        if (project.buildOptions().dumpBuildTime()) {
            start = System.currentTimeMillis();
        }

        report = project.buildOptions().testReport();
        coverage = project.buildOptions().codeCoverage();

        if (coverage) {
            this.out.println("WARNING: Code coverage generation is not supported with Ballerina native test");
        }

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
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
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

        // If the function mocking does not exist, combine all test suite map entries
        if (!isMockFunctionExist && testSuiteMapEntries.size() != 0) {
            HashMap<String, TestSuite> testSuiteMap = testSuiteMapEntries.remove(0);
            while (testSuiteMapEntries.size() > 0) {
                testSuiteMap.putAll(testSuiteMapEntries.remove(0));
            }
            testSuiteMapEntries.add(testSuiteMap);
        }

        int accumulatedTestResult = 0;
        //Execute each testsuite within list one by one
        for (Map<String, TestSuite> testSuiteMap : testSuiteMapEntries) {
            try {
                Path nativeConfigPath = target.getNativeConfigPath();
                createReflectConfig(nativeConfigPath, project.currentPackage(), testSuiteMap);
            } catch (IOException e) {
                throw createLauncherException("error while generating the necessary graalvm reflection config ", e);
            }

            // Try to modify jar if the testsuite map contains only one entry
            if (testSuiteMap.size() == 1) {
                TestSuite testSuite = testSuiteMap.values().toArray(new TestSuite[0])[0];
                String moduleName = testSuite.getPackageID();
                try {
                    modifyJarForFunctionMock(testSuite, target, moduleName);
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
                    testResult = runTestSuiteWithNativeImage(project.currentPackage(), target, testSuiteMap);
                    if (testResult != 0) {
                        accumulatedTestResult = testResult;
                    }
                    if (report) {
                        for (Map.Entry<String, TestSuite> testSuiteEntry : testSuiteMap.entrySet()) {
                            String moduleName = testSuiteEntry.getKey();
                            ModuleStatus moduleStatus = TestUtils.loadModuleStatusFromFile(
                                    testsCachePath.resolve(moduleName).resolve(TesterinaConstants.STATUS_FILE));

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
        String classPath = getClassPath(testSuiteMap);

        String jacocoAgentJarPath = "";
        String nativeImageCommand = System.getenv("GRAALVM_HOME");

        try {
            if (nativeImageCommand == null) {
                throw new ProjectException("GraalVM installation directory not found. Set GRAALVM_HOME as an " +
                        "environment variable\nHINT: To install GraalVM, follow the link: " +
                        "https://ballerina.io/learn/build-a-native-executable/#configure-graalvm");
            }
            nativeImageCommand += File.separator + BIN_DIR_NAME + File.separator
                    + (OS.contains("win") ? "native-image.cmd" : "native-image");

            File commandExecutable = Paths.get(nativeImageCommand).toFile();
            if (!commandExecutable.exists()) {
                throw new ProjectException("Cannot find '" + commandExecutable.getName() + "' in the GRAALVM_HOME. " +
                        "Install it using: gu install native-image");
            }
        } catch (ProjectException e) {
            throw createLauncherException(e.getMessage());
        }

        List<String> cmdArgs = new ArrayList<>();
        List<String> nativeArgs = new ArrayList<>();
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
        nativeArgs.add("-H:Name=" + addQuotationMarkToString(packageName));
        nativeArgs.add("-H:Path=" + convertWinPathToUnixFormat(addQuotationMarkToString(nativeTargetPath.toString())));

        // native-image configs
        nativeArgs.add("-H:ReflectionConfigurationFiles=" + convertWinPathToUnixFormat(addQuotationMarkToString(
                    nativeConfigPath.resolve("reflect-config.json").toString())));
        nativeArgs.add("--no-fallback");

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
        StreamGobbler outputGobbler =
                new StreamGobbler(process.getInputStream(), out);
        outputGobbler.start();

        if (process.waitFor() == 0) {
            outputGobbler.join();
            cmdArgs = new ArrayList<>();

            // Run the generated image
            String generatedImagePath = nativeTargetPath.resolve(packageName) + getGeneratedImageExtension();
            cmdArgs.add(generatedImagePath);

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
            process = builder.start();
            outputGobbler =
                    new StreamGobbler(process.getInputStream(), out);
            outputGobbler.start();
            int exitCode = process.waitFor();
            outputGobbler.join();
            return exitCode;
        } else {
            return 1;
        }
    }

    private String addQuotationMarkToString(String word) {
        return "\"" + word + "\"";
    }

    private String convertWinPathToUnixFormat(String path) {
        if (OS.contains("win")) {
            path = path.replace("\\", "/");
        }
        return path;
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


    private String getClassPath(Map<String, TestSuite> testSuiteMap) {
        List<String> dependencies = new ArrayList<>();
        for (Map.Entry<String, TestSuite> testSuiteEntry : testSuiteMap.entrySet()) {
            dependencies.addAll(testSuiteEntry.getValue().getTestExecutionDependencies());

        }
        dependencies = dependencies.stream().distinct().collect(Collectors.toList());
        dependencies = dependencies.stream().map((x) -> convertWinPathToUnixFormat(addQuotationMarkToString(x)))
                        .collect(Collectors.toList());

        StringJoiner classPath = new StringJoiner(File.pathSeparator);
        dependencies.stream().forEach(classPath::add);
        return classPath.toString();
    }

    private void modifyJarForFunctionMock(TestSuite testSuite, Target target, String moduleName) throws IOException {
        String testJarName = testSuite.getOrgName() + HYPHEN + moduleName + HYPHEN +
                testSuite.getVersion() + HYPHEN + TESTABLE + JAR_EXTENSION;
        String testJarPath = "";
        String modifiedJarName = "";
        String mainJarPath = "";
        String mainJarName = "";

        if (testSuite.getMockFunctionNamesMap().isEmpty()) {
            return;
        }

        //Add testable jar path to classloader URLs
        List<String> testExecutionDependencies = testSuite.getTestExecutionDependencies();
        List<String> classLoaderUrlList = new ArrayList<>();
        for (String testExecutionDependency : testExecutionDependencies) {
            if (testExecutionDependency.endsWith(testJarName)) {
                testJarPath = testExecutionDependency;
                classLoaderUrlList.add(testJarPath);
            }
        }

        ClassLoader classLoader = null;

        //Extract the className vs mocking functions list
        Map<String, List<String>> classVsMockFunctionsMap = new HashMap<>();
        Map<String, String> mockFunctionMap = testSuite.getMockFunctionNamesMap();
        populateClassNameVsFunctionToMockMap(classVsMockFunctionsMap, mockFunctionMap);

        //Extract a mapping between classes and corresponding module jar
        Map<String, List<String>> mainJarVsClassMapping = new HashMap<>();
        for (Map.Entry<String, List<String>> classVsMockFunctionsEntry : classVsMockFunctionsMap.entrySet()) {
            String className = classVsMockFunctionsEntry.getKey();
            String[] classMetaData = className.split("\\.");
            mainJarName = classMetaData[0] + HYPHEN + classMetaData[1].replace(DOT_REPLACER, DOT) +
                    HYPHEN + classMetaData[2];

            if (mainJarVsClassMapping.containsKey(mainJarName)) {
                mainJarVsClassMapping.get(mainJarName).add(className);
            } else {
                List<String> classList = new ArrayList<>();
                classList.add(className);
                mainJarVsClassMapping.put(mainJarName, classList);
            }
        }

        //Modify classes within module jar based on above mapping
        for (Map.Entry<String, List<String>> mainJarVsClassEntry : mainJarVsClassMapping.entrySet()) {

            mainJarName = mainJarVsClassEntry.getKey();
            modifiedJarName = mainJarName + HYPHEN + MODIFIED + JAR_EXTENSION;

            for (String testExecutionDependency : testExecutionDependencies) {
                if (testExecutionDependency.contains(mainJarName) && !testExecutionDependency.contains(TESTABLE)) {
                    mainJarPath = testExecutionDependency;
                }
            }
            //Add module jar path to classloader URLs
            classLoaderUrlList.add(mainJarPath);
            classLoader = AccessController.doPrivileged(
                    (PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(getURLList(classLoaderUrlList).
                            toArray(new URL[0]), ClassLoader.getSystemClassLoader()));

            //Modify classes within jar
            Map<String, byte[]> modifiedClassDef = new HashMap<>();
            for (String className : mainJarVsClassEntry.getValue()) {
                List<String> functionNamesList = classVsMockFunctionsMap.get(className);
                byte[] classFile = getModifiedClassBytes(className, functionNamesList, testSuite, classLoader);
                modifiedClassDef.put(className, classFile);
            }

            //Load all classes within module jar
            Map<String, byte[]> unmodifiedFiles = loadUnmodifiedFilesWithinJar(mainJarPath);
            String modifiedJarPath = (target.path().resolve(CACHE_DIR).resolve(testSuite.getOrgName()).resolve
                    (testSuite.getPackageName()).resolve(testSuite.getVersion()).resolve(JAVA_11_DIR)).toString()
                    + PATH_SEPARATOR + modifiedJarName;
            //Dump modified jar
            dumpJar(modifiedClassDef, unmodifiedFiles, modifiedJarPath);

            testExecutionDependencies.remove(mainJarPath);
            testExecutionDependencies.add(modifiedJarPath);
        }
    }

    //Replace unmodified classes with corresponding modified classes and dump jar
    private void dumpJar(Map<String, byte[]> modifiedClassDefs, Map<String, byte[]> unmodifiedFiles,
                         String modifiedJarPath) throws IOException {
        List<String> duplicatePaths = new ArrayList<>();
        try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(modifiedJarPath))) {
            for (Map.Entry<String, byte[]> modifiedClassDef : modifiedClassDefs.entrySet()) {
                if (modifiedClassDef.getValue().length > 0) {
                    String entry = modifiedClassDef.getKey();
                    String path = entry.replaceAll("\\.", PATH_SEPARATOR) + CLASS_EXTENSION;
                    duplicatePaths.add(path);
                    jarOutputStream.putNextEntry(new ZipEntry(path));
                    jarOutputStream.write(modifiedClassDefs.get(entry));
                    jarOutputStream.closeEntry();
                }
            }
            for (Map.Entry<String, byte[]> unmodifiedFile : unmodifiedFiles.entrySet()) {
                String entry = unmodifiedFile.getKey();
                if (!duplicatePaths.contains(entry)) {
                    jarOutputStream.putNextEntry(new ZipEntry(entry));
                    jarOutputStream.write(unmodifiedFiles.get(entry));
                    jarOutputStream.closeEntry();
                }
            }
        }
    }

    private Map<String, byte[]> loadUnmodifiedFilesWithinJar(String mainJarPath)
            throws IOException {
        Map<String, byte[]> unmodifiedFiles = new HashMap<String, byte[]>();
        File jarFile = new File(mainJarPath);
        ZipInputStream jarInputStream = new ZipInputStream(new FileInputStream(jarFile));
        ZipEntry entry;
        while ((entry = jarInputStream.getNextEntry()) != null) {
            String path = entry.getName();
            if (!entry.isDirectory()) {
                byte[] bytes = IOUtils.toByteArray(jarInputStream);
                unmodifiedFiles.put(path, bytes);
            }
            jarInputStream.closeEntry();
        }
        jarInputStream.close();
        return unmodifiedFiles;
    }

    private String getGeneratedImageExtension() {
        if (OS.contains("win")) {
            return DOT + WIN_EXEC_EXT;
        }
        return "";
    }
}
