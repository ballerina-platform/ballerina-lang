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
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.ballerina.projects.Package;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.ballerinalang.test.runtime.entity.MockFunctionReplaceVisitor;
import org.ballerinalang.test.runtime.entity.TestSuite;
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.identifier.Utils.encodeNonFunctionIdentifier;
import static io.ballerina.runtime.api.constants.RuntimeConstants.FILE_NAME_PERIOD_SEPARATOR;
import static java.util.Objects.requireNonNull;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.ANON_ORG;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.CACHE_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.CLASS_EXTENSION;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT_REPLACER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.HYPHEN;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.JAR_EXTENSION;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.JAVA_17_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_FN_DELIMITER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_FUNC_NAME_PREFIX;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_LEGACY_DELIMITER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MODIFIED;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.PATH_SEPARATOR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.TESTABLE;

/**
 * Utility functions and classes for test native-image generation.
 *
 * @since 2.3.0
 */
public class NativeUtils {
    private static final String MODULE_INIT_CLASS_NAME = "$_init";
    private static final String TEST_EXEC_FUNCTION = "__execute__";
    public static final String OS = System.getProperty("os.name").toLowerCase(Locale.getDefault());

    private static final ReflectConfigClassMethod REFLECTION_CONFIG_EXECUTE_METHOD = new ReflectConfigClassMethod(
            TEST_EXEC_FUNCTION, new String[]{"io.ballerina.runtime.internal.scheduling.Strand",
            "io.ballerina.runtime.api.values.BString",
            "io.ballerina.runtime.api.values.BString",
            "io.ballerina.runtime.api.values.BString",
            "io.ballerina.runtime.api.values.BString",
            "io.ballerina.runtime.api.values.BString",
            "io.ballerina.runtime.api.values.BString",
            "io.ballerina.runtime.api.values.BString",
            "io.ballerina.runtime.api.values.BString",
            "io.ballerina.runtime.api.values.BString",
            "io.ballerina.runtime.api.values.BString",
            "io.ballerina.runtime.api.values.BString"
    });

    //Add dynamically loading classes and methods to reflection config
    public static void createReflectConfig(Path nativeConfigPath, Package currentPackage,
                                           Map<String, TestSuite> testSuiteMap) throws IOException {
        String org = currentPackage.packageOrg().toString();
        String version = currentPackage.packageVersion().toString();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<ReflectConfigClass> classList = new ArrayList<>();



        for (Map.Entry<String, TestSuite> entry : testSuiteMap.entrySet()) {
            String moduleName = entry.getKey();
            TestSuite testSuite = entry.getValue();
            String name = testSuite.getPackageID();

            Map<String, String> testUtilityFunctions = testSuiteMap.get(moduleName).getTestUtilityFunctions();
            //Add init class
            if (testUtilityFunctions.containsKey(TEST_EXEC_FUNCTION)) {
                ReflectConfigClass testTestExecuteGeneratedRefConfClz = new ReflectConfigClass(
                        testSuiteMap.get(moduleName).getTestUtilityFunctions().get(TEST_EXEC_FUNCTION));
                testTestExecuteGeneratedRefConfClz.addReflectConfigClassMethod(REFLECTION_CONFIG_EXECUTE_METHOD);
                classList.add(testTestExecuteGeneratedRefConfClz);
                ReflectConfigClass testInitRefConfClz = new ReflectConfigClass(getQualifiedClassName(org, name, version,
                        MODULE_INIT_CLASS_NAME));

                testInitRefConfClz.addReflectConfigClassMethod(
                        new ReflectConfigClassMethod(
                                "main",
                                new String[]{"java.lang.String[]"}
                        )
                );

                testInitRefConfClz.addReflectConfigClassMethod(
                        new ReflectConfigClassMethod(
                                "$getTestExecutionState",
                                new String[]{}
                        )
                );

                //Add all class values to the array
                classList.add(testInitRefConfClz);
            }

            //Add classes with $MOCK_function methods (mock function case)
            if (!testSuiteMap.get(moduleName).getMockFunctionNamesMap().isEmpty()) {
                ReflectConfigClass functionMockingEntryClz = new ReflectConfigClass(getQualifiedClassName(
                        org, name, version, name.replace(DOT, FILE_NAME_PERIOD_SEPARATOR)));
                functionMockingEntryClz.setQueryAllDeclaredMethods(true);
                functionMockingEntryClz.setAllDeclaredFields(true);
                functionMockingEntryClz.setUnsafeAllocated(true);
                classList.add(functionMockingEntryClz);
            }

            //Add classes corresponding to test documents
            Path mockedFunctionClassPath = nativeConfigPath.resolve("mocked-func-class-map.json");
            File mockedFunctionClassFile = new File(mockedFunctionClassPath.toString());
            if (mockedFunctionClassFile.isFile()) {
                BufferedReader br = Files.newBufferedReader(mockedFunctionClassPath, StandardCharsets.UTF_8);
                Gson gsonRead = new Gson();
                Map<String, String[]> testFileMockedFunctionMapping = gsonRead.fromJson(br,
                        new TypeToken<Map<String, String[]>>() {
                        }.getType());
                if (!testFileMockedFunctionMapping.isEmpty()) {
                    ReflectConfigClass originalTestFileRefConfClz;
                    for (Map.Entry<String, String[]> testFileMockedFunctionMappingEntry :
                            testFileMockedFunctionMapping.entrySet()) {
                        String moduleNameForTestClz = testFileMockedFunctionMappingEntry.getKey().split("/")[0];
                        if (!moduleNameForTestClz.equals(name)) {
                            continue;
                        }
                        String testFile = testFileMockedFunctionMappingEntry.getKey().split("/")[1];
                        String[] mockedFunctions = testFileMockedFunctionMappingEntry.getValue();

                        String qualifiedTestClassName =
                                getQualifiedClassName(org, moduleNameForTestClz, version, testFile);
                        Class<?> qualifiedTestClass = validateClassExistance(testSuite, qualifiedTestClassName);
                        if (qualifiedTestClass == null) {
                            continue;
                        }
                        HashSet<String> methodSet = getMethodSet(qualifiedTestClass);
                        originalTestFileRefConfClz = new ReflectConfigClass(qualifiedTestClassName);
                        for (String mockedFunction : mockedFunctions) {
                            if (!methodSet.contains(mockedFunction)) {
                                continue;
                            }
                            originalTestFileRefConfClz.addReflectConfigClassMethod(
                                    new ReflectConfigClassMethod(mockedFunction));
                            originalTestFileRefConfClz.setUnsafeAllocated(true);
                            originalTestFileRefConfClz.setAllDeclaredFields(true);
                            originalTestFileRefConfClz.setQueryAllDeclaredMethods(true);
                        }
                        classList.add(originalTestFileRefConfClz);
                    }
                }
            }
        }

        //Add classes corresponding to ballerina source files
        ReflectConfigClass originalBalFileRefConfClz;
        Map<String, List<String>> mockFunctionClassMapping = new HashMap<>();
        extractMockFunctionClassMapping(testSuiteMap, mockFunctionClassMapping);
        for (Map.Entry<String, List<String>> mockFunctionClassMapEntry : mockFunctionClassMapping.entrySet()) {
            String mockFunctionClass = mockFunctionClassMapEntry.getKey();
            originalBalFileRefConfClz = new ReflectConfigClass(mockFunctionClass);
            for (String originalMockFunction : mockFunctionClassMapEntry.getValue()) {
                originalBalFileRefConfClz.addReflectConfigClassMethod(
                        new ReflectConfigClassMethod(originalMockFunction));
            }
            originalBalFileRefConfClz.setQueryAllDeclaredMethods(true);
            originalBalFileRefConfClz.setQueryAllDeclaredMethods(true);
            originalBalFileRefConfClz.setUnsafeAllocated(true);
            classList.add(originalBalFileRefConfClz);
        }

        //Add test suite class
        ReflectConfigClass runtimeEntityTestSuiteRefConfClz = new ReflectConfigClass(
                "org.ballerinalang.test.runtime.entity.TestSuite");
        runtimeEntityTestSuiteRefConfClz.setAllDeclaredFields(true);
        runtimeEntityTestSuiteRefConfClz.setUnsafeAllocated(true);

        classList.add(runtimeEntityTestSuiteRefConfClz);


        // Write the array to the config file
        try (Writer writer = new FileWriter(nativeConfigPath.resolve("reflect-config.json").toString(),
                Charset.defaultCharset())) {
            gson.toJson(classList, writer);
            writer.flush();
        }
    }

    private static HashSet<String> getMethodSet(Class<?> qualifiedTestClass) {
        HashSet<String> methodSet = new HashSet<>();
        for (Method method : qualifiedTestClass.getMethods()) {
            methodSet.add(method.getName());
        }
        return methodSet;
    }

    private static Class<?> validateClassExistance(TestSuite testSuite, String qualifiedTestClassName) {
        List<String> testExecutionDependencies = testSuite.getTestExecutionDependencies();
        ClassLoader classLoader = AccessController.doPrivileged(
                (PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(
                        getURLList(testExecutionDependencies).toArray(new URL[0]),
                        ClassLoader.getSystemClassLoader()));

        Class<?> classToCheck;
        try {
            classToCheck = classLoader.loadClass(qualifiedTestClassName);
        } catch (Throwable e) {
            return null;
        }
        return classToCheck;
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

    //Create $ORIG_function methods class mapping
    private static void extractMockFunctionClassMapping(Map<String, TestSuite> testSuiteMap,
                                                        Map<String, List<String>> mockFunctionClassMapping) {
        for (Map.Entry<String, TestSuite> testSuiteEntry : testSuiteMap.entrySet()) {
            TestSuite suite = testSuiteEntry.getValue();
            for (Map.Entry<String, String> mockFunctionEntry : suite.getMockFunctionNamesMap().entrySet()) {
                String key = mockFunctionEntry.getKey();
                String functionToMockClassName;
                String functionToMock;
                if (key.indexOf(MOCK_LEGACY_DELIMITER) == -1) {
                    functionToMockClassName = key.substring(0, key.indexOf(MOCK_FN_DELIMITER));
                    functionToMock = key.substring(key.indexOf(MOCK_FN_DELIMITER) + 1);
                } else if (key.indexOf(MOCK_FN_DELIMITER) == -1) {
                    functionToMockClassName = key.substring(0, key.indexOf(MOCK_LEGACY_DELIMITER));
                    functionToMock = key.substring(key.indexOf(MOCK_LEGACY_DELIMITER) + 1);
                } else {
                    if (key.indexOf(MOCK_FN_DELIMITER) < key.indexOf(MOCK_LEGACY_DELIMITER)) {
                        functionToMockClassName = key.substring(0, key.indexOf(MOCK_FN_DELIMITER));
                        functionToMock = key.substring(key.indexOf(MOCK_FN_DELIMITER) + 1);
                    } else {
                        functionToMockClassName = key.substring(0, key.indexOf(MOCK_LEGACY_DELIMITER));
                        functionToMock = key.substring(key.indexOf(MOCK_LEGACY_DELIMITER) + 1);
                    }
                }
                functionToMock = functionToMock.replace("\\", "");
                mockFunctionClassMapping.computeIfAbsent(functionToMockClassName,
                        k -> new ArrayList<>()).add("$ORIG_" + functionToMock);
            }
        }
    }

    public static void createResourceConfig(Path nativeConfigPath) throws IOException {
        //  {
        //      "resources": {
        //      "includes": []
        //  },
        //  "bundles": [
        //      {
        //          "name": "MessagesBundle",
        //          "locales": [
        //              ""
        //          ]
        //      }
        //  ]
        //  }

        ResourceConfigClass resourceConfigClass = new ResourceConfigClass();
        resourceConfigClass.addResourceConfigBundle(
                new ResourceConfigBundles("MessagesBundle", new String[]{""})
        );

        // Write the array to the config file
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(nativeConfigPath.resolve("resource-config.json").toString(),
                Charset.defaultCharset())) {
            gson.toJson(resourceConfigClass, writer);
            writer.flush();
        }
    }

    private static String getQualifiedClassName(String orgName, String packageName, String version, String className) {
        if (!DOT.equals(packageName)) {
            className = encodeNonFunctionIdentifier(packageName) + "$test" + "." +
                    RuntimeUtils.getMajorVersion(version) + "." + className;
        }
        if (!ANON_ORG.equals(orgName)) {
            className = encodeNonFunctionIdentifier(orgName) + "." + className;
        }
        return className;
    }

    public static void modifyJarForFunctionMock(TestSuite testSuite, Target target, String moduleName)
            throws IOException {
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
                    break;
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
                    (testSuite.getPackageName()).resolve(testSuite.getVersion()).resolve(JAVA_17_DIR)).toString()
                    + PATH_SEPARATOR + modifiedJarName;
            //Dump modified jar
            dumpJar(modifiedClassDef, unmodifiedFiles, modifiedJarPath);

            testExecutionDependencies.remove(mainJarPath);
            testExecutionDependencies.add(modifiedJarPath);
        }
    }

    //Replace unmodified classes with corresponding modified classes and dump jar
    private static void dumpJar(Map<String, byte[]> modifiedClassDefs, Map<String, byte[]> unmodifiedFiles,
                         String modifiedJarPath) throws IOException {
        List<String> duplicatePaths = new ArrayList<>();
        try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(modifiedJarPath))) {
            for (Map.Entry<String, byte[]> modifiedClassDef : modifiedClassDefs.entrySet()) {
                if (modifiedClassDef.getValue().length > 0) {
                    String entry = modifiedClassDef.getKey();
                    String path = entry.replace(".", PATH_SEPARATOR) + CLASS_EXTENSION;
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

    private static Map<String, byte[]> loadUnmodifiedFilesWithinJar(String mainJarPath)
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

    //Get all mocked functions in a class
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
            functionToMock = functionToMock.replace("\\", "");
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
        try (InputStream ins = clazz.getResourceAsStream(clazz.getSimpleName() + CLASS_EXTENSION)) {
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

    public static String getClassPath(Map<String, TestSuite> testSuiteMap) {
        List<String> dependencies = new ArrayList<>();
        for (Map.Entry<String, TestSuite> testSuiteEntry : testSuiteMap.entrySet()) {
            dependencies.addAll(testSuiteEntry.getValue().getTestExecutionDependencies());

        }
        dependencies = dependencies.stream().distinct().collect(Collectors.toList());
        dependencies = dependencies.stream().map((x) -> convertWinPathToUnixFormat(addQuotationMarkToString(x)))
                .collect(Collectors.toList());

        StringJoiner classPath = new StringJoiner(File.pathSeparator);
        dependencies.forEach(classPath::add);
        return classPath.toString();
    }

    public static String addQuotationMarkToString(String word) {
        return "\"" + word + "\"";
    }

    public static String convertWinPathToUnixFormat(String path) {
        if (OS.contains("win")) {
            path = path.replace("\\", "/");
        }
        return path;
    }

    private static class ReflectConfigClass {
        private final String name;
        private List<ReflectConfigClassMethod> methods;
        private boolean allDeclaredFields;
        private boolean unsafeAllocated;
        private boolean queryAllDeclaredMethods;

        public ReflectConfigClass(String name) {
            this.name = name;
        }

        public void addReflectConfigClassMethod(
                ReflectConfigClassMethod method) {
            if (this.methods == null) {
                this.methods = new ArrayList<>();
            }
            this.methods.add(method);
        }

        public void setAllDeclaredFields(boolean allDeclaredFields) {
            this.allDeclaredFields = allDeclaredFields;
        }

        public void setUnsafeAllocated(boolean unsafeAllocated) {
            this.unsafeAllocated = unsafeAllocated;
        }

        public void setQueryAllDeclaredMethods(boolean queryAllDeclaredMethods) {

            this.queryAllDeclaredMethods = queryAllDeclaredMethods;
        }

        public String getName() {
            return name;
        }

        public boolean isAllDeclaredFields() {
            return allDeclaredFields;
        }

        public boolean isUnsafeAllocated() {
            return unsafeAllocated;
        }

        public boolean isQueryAllDeclaredMethods() {
            return queryAllDeclaredMethods;
        }
    }

    private static class ReflectConfigClassMethod {
        private final String name;
        private String[] parameterTypes;

        public ReflectConfigClassMethod(String name) {
            this.name = name;
        }

        public ReflectConfigClassMethod(String name, String[] parameterTypes) {
            this.name = name;
            this.parameterTypes = parameterTypes;
        }

        public String getName() {
            return name;
        }

        public String[] getParameterTypes() {
            return parameterTypes;
        }
    }

    private static class ResourceConfigClass {
        private final List<ResourceConfigBundles> bundles;

        public ResourceConfigClass() {
            this.bundles = new ArrayList<>();
        }

        public void addResourceConfigBundle(ResourceConfigBundles resourceConfigBundles) {
            this.bundles.add(resourceConfigBundles);
        }
    }

    private static class ResourceConfigBundles {
        private final String name;
        private final String[] locales;

        private ResourceConfigBundles(String name, String[] locales) {
            this.name = name;
            this.locales = locales;
        }

        public String getName() {
            return name;
        }

        public String[] getLocales() {
            return locales;
        }
    }

}
