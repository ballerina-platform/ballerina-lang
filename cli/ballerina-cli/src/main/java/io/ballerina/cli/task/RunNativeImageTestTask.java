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
import io.ballerina.cli.utils.MethodCallReplaceVisitor;
import io.ballerina.cli.utils.OrigMockFunctionReplaceVisitor;
import io.ballerina.cli.utils.TestUtils;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
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
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.internal.model.Target;
import org.apache.commons.compress.utils.IOUtils;
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
import org.wso2.ballerinalang.util.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
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
import java.util.StringJoiner;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.NativeUtils.createReflectConfig;
import static io.ballerina.cli.utils.TestUtils.generateCoverage;
import static io.ballerina.cli.utils.TestUtils.generateTesterinaReports;
import static io.ballerina.projects.util.ProjectConstants.BIN_DIR_NAME;
import static io.ballerina.runtime.api.constants.RuntimeConstants.FILE_NAME_PERIOD_SEPARATOR;
import static java.util.Objects.requireNonNull;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.CACHE_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.CLASS_EXTENSION;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.HYPHEN;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.JAR_EXTENSION;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.JAVA_11_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_FN_DELIMITER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_FUNC_NAME_PREFIX;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_LEGACY_DELIMITER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MODIFIED;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.PATH_SEPARATOR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.TESTABLE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;

/**
 * Task for executing tests.
 *
 * @since 2.3.0
 */
public class RunNativeImageTestTask implements Task {

    private static final String OS = System.getProperty("os.name").toLowerCase(Locale.getDefault());

    private final PrintStream out;
    private final PrintStream err;
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

    public RunNativeImageTestTask(PrintStream out, PrintStream errStream, boolean rerunTests, String groupList,
                                  String disableGroupList, String testList, String includes, String coverageFormat,
                                  Map<String, Module> modules, boolean listGroups) {
        this.out = out;
        this.err = errStream;
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

    //Get modified class bytes for function mock
    public static byte[] getModifiedClassBytes(String className, List<String> functionNames, TestSuite suite,
                                               ClassLoader classLoader, List<String> modifiedMethods) {
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
                if (modifiedMethods.contains(className + "-" + method1.getName())) {
                    continue;
                } else {
                    modifiedMethods.add(className + "-" + method1.getName());
                }
                String desugaredMockFunctionName = MOCK_FUNC_NAME_PREFIX + method1.getName();
                String testClassName = TesterinaUtils.getQualifiedClassName(suite.getOrgName(),
                        suite.getTestPackageID(), suite.getVersion(),
                        suite.getPackageID().replace(DOT, FILE_NAME_PERIOD_SEPARATOR));
                Class<?> testClass;
                try {
                    testClass = classLoader.loadClass(testClassName);
                } catch (Throwable e) {
                    throw createLauncherException("failed to load class :" + testClassName);
                }
                for (Method method2 : testClass.getDeclaredMethods()) {
                    if (method2.getName().equals(desugaredMockFunctionName)) {
                        if (!readFromBytes) {
                            classFile = replaceMethodBody(method1);
                            readFromBytes = true;
                        } else {
                            classFile = replaceMethodBody(classFile, method1);
                        }
                    }
                }
            } else if (functionNames.contains(MOCK_LEGACY_DELIMITER + method1.getName())) {
                if (modifiedMethods.contains(className + "-" + method1.getName())) {
                    continue;
                } else {
                    modifiedMethods.add(className + "-" + method1.getName());
                }
                String key = className + MOCK_LEGACY_DELIMITER + method1.getName();
                String mockFunctionName = suite.getMockFunctionNamesMap().get(key);
                if (mockFunctionName != null) {
                    String mockFunctionClassName = suite.getTestUtilityFunctions().get(mockFunctionName);
                    Class<?> mockFunctionClass;
                    try {
                        mockFunctionClass = classLoader.loadClass(mockFunctionClassName);
                    } catch (ClassNotFoundException e) {
                        throw createLauncherException("failed to load class: " + mockFunctionClassName);
                    }
                    for (Method method2 : mockFunctionClass.getDeclaredMethods()) {
                        if (method2.getName().equals(mockFunctionName)) {
                            if (!readFromBytes) {
                                classFile = replaceMethodBody(method1);
                                readFromBytes = true;
                            } else {
                                classFile = replaceMethodBody(classFile, method1);
                            }
                        }
                    }
                }
            }
        }
        return classFile;
    }

    //Get modified test class bytes for function mock
    public static byte[] getModifiedTestClassBytes(String className, List<String> functionNames, TestSuite suite,
                                                   ClassLoader classLoader, Class<?> testDocumentClass,
                                                   byte[] classFile) {
        Class<?> functionToMockClass;
        try {
            functionToMockClass = classLoader.loadClass(className);
        } catch (Throwable e) {
            throw createLauncherException("failed to load class: " + className);
        }

        boolean readFromBytes;
        if (classFile.length == 0) {
            readFromBytes = false;
        } else {
            readFromBytes = true;
        }

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
                    throw createLauncherException("failed to load class :" + testClassName);
                }
                for (Method method2 : testClass.getDeclaredMethods()) {
                    if (method2.getName().equals(desugaredMockFunctionName)) {
                        if (!readFromBytes) {
                            classFile = replaceTestClzMethodBody(testDocumentClass, method2, method1);
                            readFromBytes = true;
                        } else {
                            classFile = replaceTestClzMethodBody(classFile, method2, method1);
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
                        throw createLauncherException("failed to load class: " + mockFunctionClassName);
                    }
                    for (Method method2 : mockFunctionClass.getDeclaredMethods()) {
                        if (method2.getName().equals(mockFunctionName)) {
                            if (!readFromBytes) {
                                classFile = replaceTestClzMethodBody(testDocumentClass, method1, method2);
                                readFromBytes = true;
                            } else {
                                classFile = replaceTestClzMethodBody(classFile, method1, method2);
                            }
                        }
                    }
                } else {
                    continue;
                }
            }
        }
        return classFile;
    }

    private static byte[] replaceTestClzMethodBody(Class<?> testDocumentClass, Method toFunc, Method fromFunc) {
        Class<?> clazz = testDocumentClass;
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
        ClassVisitor cv = new MethodCallReplaceVisitor(Opcodes.ASM7, cw, toFunc, fromFunc);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    private static byte[] replaceTestClzMethodBody(byte[] classFile, Method toFunc, Method fromFunc) {
        ClassReader cr = new ClassReader(classFile);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new MethodCallReplaceVisitor(Opcodes.ASM7, cw, toFunc, fromFunc);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    private static byte[] replaceMethodBody(Method method) {
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
        ClassVisitor cv = new OrigMockFunctionReplaceVisitor(Opcodes.ASM7, cw, method);
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    //Move mocked function as $ORIG_function
    private static byte[] replaceMethodBody(byte[] classFile, Method method) {
        ClassReader cr = new ClassReader(classFile);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new OrigMockFunctionReplaceVisitor(Opcodes.ASM7, cw, method);
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    public static List<URL> getURLList(List<String> jarFilePaths) {
        List<URL> urlList = new ArrayList<>();

        for (String jarFilePath : jarFilePaths) {
            try {
                urlList.add(Paths.get(jarFilePath).toUri().toURL());
            } catch (MalformedURLException e) {
                // This path cannot get executed
                throw new RuntimeException("Failed to create classloader with all jar files", e);
            }
        }
        return urlList;
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

        //Track modified functions in a module jar and Have a mpping between modified jar and original jar
        Map<String, String> originalVsModifiedJarMap = new HashMap<>();
        List<String> modifiedMethods = new ArrayList<>();
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
            try {
                modifyJarForFunctionMock(suite, target, module,
                       originalVsModifiedJarMap, modifiedMethods);
            } catch (IOException e) {
                throw createLauncherException("error occurred while running tests", e);
            }

            String resolvedModuleName =
                    module.isDefaultModule() ? moduleName.toString() : module.moduleName().moduleNamePart();
            testSuiteMap.put(resolvedModuleName, suite);
            moduleNamesList.add(resolvedModuleName);
        }

        try {
            Path nativeConfigPath = target.getNativeConfigPath();
            createReflectConfig(nativeConfigPath, project.currentPackage(), testSuiteMap);
        } catch (IOException e) {
            throw createLauncherException("error while generating the necessary graalvm reflection config ", e);
        }

        //Remove all mock function entries from test suites
        for (Map.Entry<String, TestSuite> testSuiteEntry : testSuiteMap.entrySet()) {
            TestSuite testSuite = testSuiteEntry.getValue();
            if (!testSuite.getMockFunctionNamesMap().isEmpty()) {
                testSuite.removeAllMockFunctions();
            }
        }

        TestUtils.writeToTestSuiteJson(testSuiteMap, testsCachePath);



        if (hasTests) {
            int testResult = 1;
            try {
                testResult = runTestSuiteWithNativeImage(project.currentPackage(), jBallerinaBackend, target,
                        originalVsModifiedJarMap);

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
            } catch (IOException e) {
                TestUtils.cleanTempCache(project, cachesRoot);
                throw createLauncherException("error occurred while running tests", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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

    private int runTestSuiteWithNativeImage(Package currentPackage, JBallerinaBackend jBallerinaBackend, Target target,
                                            Map<String, String> originalVsModifiedJarMap)
            throws IOException, InterruptedException {
        String packageName = currentPackage.packageName().toString();
        String classPath = getClassPath(jBallerinaBackend, currentPackage);
        String modClassPath;
        for (Map.Entry<String, String> functionMockModuleMapping : originalVsModifiedJarMap.entrySet()) {
            String moduleJar = functionMockModuleMapping.getKey();
            String replacedJar = originalVsModifiedJarMap.get(moduleJar);
            modClassPath = classPath.replace(moduleJar, replacedJar);
            classPath = modClassPath;

        }

        String jacocoAgentJarPath = "";
        String nativeImageCommand = System.getenv("GRAALVM_HOME");

        if (nativeImageCommand == null) {
            throw new ProjectException("GraalVM installation directory not found. Set GRAALVM_HOME as an " +
                    "environment variable");
        }
        nativeImageCommand += File.separator + BIN_DIR_NAME + File.separator
                + (OS.contains("win") ? "native-image.cmd" : "native-image");

        File commandExecutable = Paths.get(nativeImageCommand).toFile();
        if (!commandExecutable.exists()) {
            throw new ProjectException("Cannot find '" + commandExecutable.getName() + "' in the GRAALVM_HOME. " +
                    "Install it using: gu install native-image");
        }

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
        List<String> nativeArgs = new ArrayList<>();
        cmdArgs.add(nativeImageCommand);

        Path nativeConfigPath = target.getNativeConfigPath();   // <abs>target/cache/test_cache/native-config
        Path nativeTargetPath = target.getNativePath();         // <abs>target/native

        // Run native-image command with generated configs
        cmdArgs.add(TesterinaConstants.TESTERINA_LAUNCHER_CLASS_NAME);
        cmdArgs.add("@" + (nativeConfigPath.resolve("native-image-args.txt")));
        nativeArgs.addAll(Lists.of("-cp", classPath));


        // set name and path
        nativeArgs.add("-H:Name=" + packageName);
        nativeArgs.add("-H:Path=" + nativeTargetPath);

        // native-image configs
        nativeArgs.add("-H:ReflectionConfigurationFiles=" + nativeConfigPath.resolve("reflect-config.json"));
        nativeArgs.add("--no-fallback");

        try (FileWriter nativeArgumentWriter = new FileWriter(nativeConfigPath.resolve("native-image-args.txt")
                .toString(), Charset.defaultCharset())) {
            nativeArgumentWriter.write(String.join(" ", nativeArgs));
            nativeArgumentWriter.flush();
        } catch (IOException e) {
            throw createLauncherException("error while generating the necessary graalvm argument file", e);
        }

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(cmdArgs.toArray(new String[0]));
        Process process = builder.start();
        IOUtils.copy(process.getInputStream(), out);
        IOUtils.copy(process.getErrorStream(), err);

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
            process = builder.start();
            IOUtils.copy(process.getInputStream(), out);
            IOUtils.copy(process.getErrorStream(), err);
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

    private void modifyJarForFunctionMock(TestSuite testSuite, Target target, Module module,
                                          Map<String, String> originalVsModifiedJarMap,
                                          List<String> modifiedMethods) throws IOException {
        String moduleName = module.moduleName().toString();
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
            mainJarName = classMetaData[0] + HYPHEN + classMetaData[1].replace("$0046", ".") +
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

            //If module jar is modified already, use modified one
            for (String testExecutionDependency : testExecutionDependencies) {
                if (testExecutionDependency.contains(mainJarName) && !testExecutionDependency.contains(TESTABLE)) {
                    mainJarPath = testExecutionDependency;
                    if (originalVsModifiedJarMap.containsKey(mainJarPath)) {
                        mainJarPath = originalVsModifiedJarMap.get(mainJarPath);
                    }
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
                byte[] classFile = getModifiedClassBytes(className, functionNamesList, testSuite, classLoader,
                                    modifiedMethods);
                modifiedClassDef.put(className, classFile);
            }

            //Load all classes within module jar
            Map<String, byte[]> unmodifiedFiles = loadUnmodifiedFilesWithinJar(mainJarPath);
            String modifiedJarPath = (target.path().resolve(CACHE_DIR).resolve(testSuite.getOrgName()).resolve
                    (testSuite.getPackageName()).resolve(testSuite.getVersion()).resolve(JAVA_11_DIR)).toString()
                    + PATH_SEPARATOR + modifiedJarName;
            //Dump modified jar
            dumpJar(modifiedClassDef, unmodifiedFiles, modifiedJarPath);

            //Have a modified jar mapping to modify graal vm classpath
            if (!originalVsModifiedJarMap.containsKey(mainJarPath)) {
                originalVsModifiedJarMap.put(mainJarPath, modifiedJarPath);
            }
        }

        //Modify testable jar for function mocking
        Map<String, byte[]> modifiedTestClassDef = new HashMap<>();
        for (DocumentId testDocId : module.testDocumentIds()) {
            //Get the class for test document
            Document testDocument = module.document(testDocId);
            String testDocumentName = testDocument.name().replace(".bal", "")
                                                            .replace("/", ".");
            String testDocumentClassName = TesterinaUtils.getQualifiedClassName(testSuite.getOrgName(),
                    testSuite.getTestPackageID(), testSuite.getVersion(), testDocumentName);
            Class<?> testDocumentClass;
            try {
                testDocumentClass = classLoader.loadClass(testDocumentClassName);
            } catch (Throwable e) {
                throw createLauncherException("failed to load class: " + testDocumentClassName);
            }

            //Replace the original functions(mocked) in test document with the corresponding $MOCK_ function
            byte[] classFile = new byte[0];
            for (Map.Entry<String, List<String>> entry : classVsMockFunctionsMap.entrySet()) {
                String className = entry.getKey();
                List<String> functionNamesList = entry.getValue();
                classFile = getModifiedTestClassBytes(className, functionNamesList, testSuite, classLoader,
                        testDocumentClass, classFile);
            }
            modifiedTestClassDef.put(testDocumentClassName, classFile);
        }
        //Get all unmodified classes within testable jar and dump modified version
        Map<String, byte[]> unmodifiedTestFiles = loadUnmodifiedFilesWithinJar(testJarPath);
        String modifiedTestJarPath = testJarPath.replace(JAR_EXTENSION, HYPHEN + MODIFIED + JAR_EXTENSION);
        dumpJar(modifiedTestClassDef, unmodifiedTestFiles, modifiedTestJarPath);

        //Have a modified jar mapping to modify graal vm classpath
        if (!originalVsModifiedJarMap.containsKey(testJarPath)) {
            originalVsModifiedJarMap.put(testJarPath, modifiedTestJarPath);
        }
    }

    //Replace unmodified classes with corresponding modified classes and dump jar
    private void dumpJar(Map<String, byte[]> modifiedClassDefs, Map<String, byte[]> unmodifiedFiles,
                         String modifiedJarPath) throws IOException {
        List<String> duplicatePaths = new ArrayList<>();
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(modifiedJarPath));
        try {
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
        } finally {
            jarOutputStream.close();
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

}
