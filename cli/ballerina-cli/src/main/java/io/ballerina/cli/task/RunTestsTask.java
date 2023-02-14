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

import io.ballerina.cli.utils.BuildTime;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.internal.model.Target;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.DebugUtils.getDebugArgs;
import static io.ballerina.cli.utils.DebugUtils.isInDebugMode;
import static io.ballerina.cli.utils.NativeUtils.getURLList;
import static io.ballerina.cli.utils.TestUtils.cleanTempCache;
import static io.ballerina.cli.utils.TestUtils.clearFailedTestsJson;
import static io.ballerina.cli.utils.TestUtils.generateCoverage;
import static io.ballerina.cli.utils.TestUtils.generateTesterinaReports;
import static io.ballerina.cli.utils.TestUtils.loadModuleStatusFromFile;
import static io.ballerina.cli.utils.TestUtils.writeToTestSuiteJson;
import static io.ballerina.runtime.api.constants.RuntimeConstants.FILE_NAME_PERIOD_SEPARATOR;
import static io.ballerina.runtime.api.constants.RuntimeConstants.MODULE_INIT_CLASS_NAME;
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
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.USER_DIR;

/**
 * Task for executing tests.
 *
 * @since 2.0.0
 */
public class RunTestsTask implements Task {
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

    public RunTestsTask(PrintStream out, PrintStream err, boolean rerunTests, String groupList,
                        String disableGroupList, String testList, String includes, String coverageFormat,
                        Map<String, Module> modules, boolean listGroups)  {
        this.out = out;
        this.err = err;
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
        Package currentPackage = project.currentPackage();
        String packageName = currentPackage.packageName().toString();
        TestProcessor testProcessor = new TestProcessor(jarResolver);
        Map<String, TestSuite> testSuiteMap = new HashMap<>();

        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "bal test" command which only executes tests
        // in packages.
        out.println();
        out.print("Running Tests");
        if (coverage) {
            out.print(" with Coverage");
        }
        out.println();

        int exitValue = 0;
        for (ModuleDescriptor moduleDescriptor : currentPackage.moduleDependencyGraph().toTopologicallySortedList()) {
            Module module = currentPackage.module(moduleDescriptor.name());
            String moduleName = module.moduleName().toString();
            TestSuite suite = testProcessor.testSuite(module).orElse(null);

            if (suite == null) {
                continue;
            }

            //Set 'hasTests' flag if there are any tests available in the package
            if (!hasTests) {
                hasTests = true;
            }

            if (!isRerunTestExecution) {
                clearFailedTestsJson(target.path());
            }

            if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                suite.setSourceFileName(project.sourceRoot().getFileName().toString());
            }

            try {
                modifyJarForFunctionMock(suite, target, moduleName);
            } catch (IOException e) {
                throw createLauncherException("error occurred while running tests", e);
            }

            suite.setReportRequired(report || coverage);
            String resolvedModuleName = module.isDefaultModule() ? moduleName : module.moduleName().moduleNamePart();
            testSuiteMap.put(resolvedModuleName, suite);

            out.println("\n\t" + (moduleName.equals(packageName) ?
                    (moduleName.equals(TesterinaConstants.DOT) ? suite.getSourceFileName() : moduleName)
                    : packageName + TesterinaConstants.DOT + moduleName));

            try {
                int testResult = runTestSuite(target, currentPackage, jBallerinaBackend, moduleName, suite);

                if (testResult != 0) {
                    exitValue = testResult;
                }

                if (report || coverage) {
                    ModuleStatus moduleStatus = loadModuleStatusFromFile(testsCachePath.resolve(moduleName)
                            .resolve(TesterinaConstants.STATUS_FILE));

                    if (!moduleName.equals(currentPackage.packageName().toString())) {
                        moduleName = ModuleName.from(currentPackage.packageName(), moduleName).toString();
                    }
                    testReport.addModuleStatus(moduleName, moduleStatus);
                }
            } catch (IOException | ClassNotFoundException e) {
                cleanTempCache(project, cachesRoot);
                throw createLauncherException("error occurred while running tests", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if ((report || coverage) && hasTests) {
            try {
                generateCoverage(project, testReport, jBallerinaBackend, this.includesInCoverage,
                        this.coverageReportFormat, this.coverageModules);
                generateTesterinaReports(project, testReport, this.out, target);
            } catch (IOException e) {
                cleanTempCache(project, cachesRoot);
                throw createLauncherException("error occurred while generating test report :", e);
            }
        }

        writeToTestSuiteJson(testSuiteMap, testsCachePath);

        if (exitValue != 0) {
            cleanTempCache(project, cachesRoot);
            throw createLauncherException("there are test failures");
        }

        // Cleanup temp cache for SingleFileProject
        cleanTempCache(project, cachesRoot);
        if (project.buildOptions().dumpBuildTime()) {
            BuildTime.getInstance().testingExecutionDuration = System.currentTimeMillis() - start;
        }
    }

    private int runTestSuite(Target target, Package currentPackage, JBallerinaBackend jBallerinaBackend,
                             String moduleName, TestSuite suite)
            throws IOException, InterruptedException, ClassNotFoundException {
        String packageName = currentPackage.packageName().toString();
        String orgName = currentPackage.packageOrg().toString();
        String classPath = getClassPath(suite);
        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.add(System.getProperty("java.command"));
        cmdArgs.add("-XX:+HeapDumpOnOutOfMemoryError");
        cmdArgs.add("-XX:HeapDumpPath=" + System.getProperty(USER_DIR));

        String initClassName = JarResolver.getQualifiedClassName(orgName, moduleName + "$test",
                currentPackage.packageVersion().toString(), MODULE_INIT_CLASS_NAME);
        if (TesterinaConstants.DOT.equals(packageName)) {
            initClassName = JarResolver.getQualifiedClassName(orgName, moduleName,
                    currentPackage.packageVersion().toString(), MODULE_INIT_CLASS_NAME);
        }
        String jacocoAgentJarPath = Paths.get(System.getProperty(BALLERINA_HOME)).resolve(BALLERINA_HOME_BRE)
                .resolve(BALLERINA_HOME_LIB).resolve(TesterinaConstants.AGENT_FILE_NAME).toString();
        if (coverage) {
            String agentCommand = "-javaagent:"
                    + jacocoAgentJarPath
                    + "=destfile="
                    + target.getTestsCachePath().resolve(TesterinaConstants.COVERAGE_DIR)
                    .resolve(TesterinaConstants.EXEC_FILE_NAME);
            if (!TesterinaConstants.DOT.equals(packageName) && this.includesInCoverage == null) {
                // add user defined classes for generating the jacoco exec file
                agentCommand += ",includes=" + orgName + ".*";
            } else {
                agentCommand += ",includes=" + this.includesInCoverage;
            }

            cmdArgs.add(agentCommand);
        }

        cmdArgs.addAll(Lists.of("-cp", classPath));
        if (isInDebugMode()) {
            cmdArgs.add(getDebugArgs(this.err));
        }
        cmdArgs.add(initClassName);

        // Adds arguments to be read at the Test Runner
        cmdArgs.add(target.path().toString());
        cmdArgs.add(packageName);
        cmdArgs.add(moduleName);
        cmdArgs.add(Boolean.toString(report));
        cmdArgs.add(Boolean.toString(coverage));
        cmdArgs.add(this.groupList != null ? this.groupList : "");
        cmdArgs.add(this.disableGroupList != null ? this.disableGroupList : "");
        cmdArgs.add(this.singleExecTests != null ? this.singleExecTests : "");
        cmdArgs.add(Boolean.toString(isRerunTestExecution));
        cmdArgs.add(Boolean.toString(listGroups));

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).inheritIO();
        Process proc = processBuilder.start();
        return proc.waitFor();
    }

    private String getClassPath(TestSuite suite) {
        List<String> dependencies = new ArrayList<>(suite.getTestExecutionDependencies());
        StringJoiner classPath = new StringJoiner(File.pathSeparator);
        dependencies.forEach(classPath::add);
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

    private static void populateClassNameVsFunctionToMockMap(Map<String, List<String>> classVsMockFunctionsMap,
                                                             Map<String, String> mockFunctionMap) {
        for (Map.Entry<String, String> entry : mockFunctionMap.entrySet()) {
            String key = entry.getKey();
            String functionToMockClassName;
            String functionToMock;
            if (!key.contains(MOCK_LEGACY_DELIMITER)) {
                functionToMockClassName = key.substring(0, key.indexOf(MOCK_FN_DELIMITER));
                functionToMock = key.substring(key.indexOf(MOCK_FN_DELIMITER));
            } else if (!key.contains(MOCK_FN_DELIMITER)) {
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
}
