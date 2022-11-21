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
package org.ballerinalang.test.runtime;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.test.runtime.entity.MockFunctionReplaceVisitor;
import org.ballerinalang.test.runtime.entity.ModuleStatus;
import org.ballerinalang.test.runtime.entity.TestArguments;
import org.ballerinalang.test.runtime.entity.TestReport;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.test.runtime.exceptions.BallerinaTestException;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.ballerinalang.test.runtime.util.TesterinaUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.api.constants.RuntimeConstants.FILE_NAME_PERIOD_SEPARATOR;
import static java.util.Objects.requireNonNull;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_FN_DELIMITER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_LEGACY_DELIMITER;

/**
 * Main class to init the test suit.
 */
public class Main {
    private static final PrintStream out = System.out;
    static TestReport testReport;
    static ClassLoader classLoader;
    static Map<String, List<String>> classVsMockFunctionsMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        int exitStatus = 0;
        int result;

        if (args.length >= 4) {
            Path targetPath = Paths.get(args[0]);
            Path testCache = targetPath.resolve(ProjectConstants.CACHES_DIR_NAME)
                            .resolve(ProjectConstants.TESTS_CACHE_DIR_NAME);
            String jacocoAgentJarPath = args[1];
            boolean report = Boolean.parseBoolean(args[2]);
            boolean coverage = Boolean.parseBoolean(args[3]);

            if (report || coverage) {
                testReport = new TestReport();
            }

            out.println();
            out.print("Running Tests");
            if (coverage) {
                out.print(" with Coverage");
            }
            out.println();

            Path testSuiteCachePath = testCache.resolve(TesterinaConstants.TESTERINA_TEST_SUITE);

            try (BufferedReader br = Files.newBufferedReader(testSuiteCachePath, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                Map<String, TestSuite> testSuiteMap = gson.fromJson(br,
                        new TypeToken<Map<String, TestSuite>>() { }.getType());

                if (!testSuiteMap.isEmpty()) {
                    for (Map.Entry<String, TestSuite> entry : testSuiteMap.entrySet()) {
                        String moduleName = entry.getKey();
                        TestSuite testSuite = entry.getValue();
                        String packageName = testSuite.getPackageName();
                        out.println("\n\t" + (moduleName.equals(packageName) ?
                                (moduleName.equals(TesterinaConstants.DOT) ? testSuite.getSourceFileName() : moduleName)
                                : packageName + TesterinaConstants.DOT + moduleName));

                        testSuite.setModuleName(moduleName);
                        List<String> testExecutionDependencies = testSuite.getTestExecutionDependencies();
                        classLoader = createURLClassLoader(testExecutionDependencies);

                        if (!testSuite.getMockFunctionNamesMap().isEmpty()) {
                            if (coverage) {
                                testExecutionDependencies.add(jacocoAgentJarPath);
                            }
                            String instrumentDir = testCache.resolve(TesterinaConstants.COVERAGE_DIR)
                                    .resolve(TesterinaConstants.JACOCO_INSTRUMENTED_DIR).toString();
                            replaceMockedFunctions(testSuite, testExecutionDependencies, instrumentDir, coverage);
                        }

                        result = startTestSuit(Paths.get(testSuite.getSourceRootPath()), testSuite, classLoader,
                                new TestArguments(args[0], packageName, moduleName,
                                        args[2], args[3], args[4], args[5], args[6], args[7], args[8]));
                        exitStatus = (result == 1) ? result : exitStatus;
                    }
                } else {
                    exitStatus = 1;
                }
            }
        } else {
            exitStatus = 1;
        }

        Runtime.getRuntime().exit(exitStatus);
    }

    private static int startTestSuit(Path sourceRootPath, TestSuite testSuite, ClassLoader classLoader,
                                     TestArguments args) {
        int exitStatus = 0;
        try {
            TesterinaUtils.executeTests(sourceRootPath, testSuite, classLoader, args);
        } catch (RuntimeException e) {
            exitStatus = 1;
        } finally {
            return exitStatus;
        }
    }

    private static void writeStatusToJsonFile(ModuleStatus moduleStatus, Path tmpJsonPath) throws IOException {
        File jsonFile = new File(tmpJsonPath.toString());
        if (!Files.exists(tmpJsonPath.getParent())) {
            Files.createDirectories(tmpJsonPath.getParent());
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(jsonFile)) {
            try (Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                String json = gson.toJson(moduleStatus);
                writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            }
        }
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

    public static URLClassLoader createURLClassLoader(List<String> jarFilePaths) {
        return AccessController.doPrivileged(
                (PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(getURLList(jarFilePaths).toArray(
                        new URL[0]), ClassLoader.getSystemClassLoader()));
    }

    public static void replaceMockedFunctions(TestSuite suite, List<String> jarFilePaths, String instrumentDir,
                                              boolean coverage) {
        populateClassNameVsFunctionToMockMap(suite);
        Map<String, byte[]> modifiedClassDef = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : classVsMockFunctionsMap.entrySet()) {
            String className = entry.getKey();
            List<String> functionNamesList = entry.getValue();
            byte[] classFile = getModifiedClassBytes(className, functionNamesList, suite, instrumentDir, coverage);
            modifiedClassDef.put(className, classFile);
        }
        classLoader = createClassLoader(jarFilePaths, modifiedClassDef);
    }

    private static void populateClassNameVsFunctionToMockMap(TestSuite suite) {
        Map<String, String> mockFunctionMap = suite.getMockFunctionNamesMap();
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

    public static byte[] getModifiedClassBytes(String className, List<String> functionNames, TestSuite suite,
                                               String instrumentDir, boolean coverage) {
        Class<?> functionToMockClass;
        try {
            functionToMockClass = classLoader.loadClass(className);
        } catch (Throwable e) {
            throw new BallerinaTestException("failed to load class: " + className);
        }

        byte[] classFile = new byte[0];
        boolean readFromBytes = false;
        for (Method method1 : functionToMockClass.getDeclaredMethods()) {
            if (functionNames.contains(MOCK_FN_DELIMITER + method1.getName())) {
                String desugaredMockFunctionName = "$MOCK_" + method1.getName();
                String testClassName = TesterinaUtils.getQualifiedClassName(suite.getOrgName(),
                        suite.getTestPackageID(), suite.getVersion(),
                        suite.getPackageID().replace(".", FILE_NAME_PERIOD_SEPARATOR));
                Class<?> testClass;
                try {
                    testClass = classLoader.loadClass(testClassName);
                } catch (Throwable e) {
                    throw new BallerinaTestException("failed to load class :" + testClassName);
                }
                for (Method method2 : testClass.getDeclaredMethods()) {
                    if (method2.getName().equals(desugaredMockFunctionName)) {
                        if (!readFromBytes) {
                            classFile = replaceMethodBody(method1, method2, instrumentDir, coverage);
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
                        throw new BallerinaTestException("failed to load class: " + mockFunctionClassName);
                    }
                    for (Method method2 : mockFunctionClass.getDeclaredMethods()) {
                        if (method2.getName().equals(mockFunctionName)) {
                            if (!readFromBytes) {
                                classFile = replaceMethodBody(method1, method2, instrumentDir, coverage);
                                readFromBytes = true;
                            } else {
                                classFile = replaceMethodBody(classFile, method1, method2);
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

    private static byte[] replaceMethodBody(Method method, Method mockMethod, String instrumentDir, boolean coverage) {
        Class<?> clazz = method.getDeclaringClass();
        ClassReader cr;
        try {
            InputStream ins;
            if (coverage) {
                String instrumentedClassPath = instrumentDir + "/" + clazz.getName().replaceAll("\\.", "/") + ".class";
                ins = new FileInputStream(instrumentedClassPath);
            } else {
                ins = clazz.getResourceAsStream(clazz.getSimpleName() + ".class");
            }
            cr = new ClassReader(requireNonNull(ins));
        } catch (IOException e) {
            throw new BallerinaTestException("failed to get the class reader object for the class "
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

    private static CustomClassLoader createClassLoader(List<String> jarFilePaths,
                                                       Map<String, byte[]> modifiedClassDef) {
        return new CustomClassLoader(getURLList(jarFilePaths).toArray(new URL[0]),
                ClassLoader.getSystemClassLoader(), modifiedClassDef);
    }

    public static ClassLoader getClassLoader() {
        return classLoader;
    }

}
