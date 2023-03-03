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
import io.ballerina.runtime.internal.util.RuntimeUtils;
import org.ballerinalang.test.runtime.entity.TestSuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.Map;

import static io.ballerina.identifier.Utils.encodeNonFunctionIdentifier;
import static io.ballerina.runtime.api.constants.RuntimeConstants.FILE_NAME_PERIOD_SEPARATOR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.ANON_ORG;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_FN_DELIMITER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.MOCK_LEGACY_DELIMITER;

/**
 * Utility functions and classes for test native-image generation.
 *
 * @since 2.3.0
 */
public class NativeUtils {
    private static final String MODULE_INIT_CLASS_NAME = "$_init";
    private static final String MODULE_CONFIGURATION_MAPPER = "$configurationMapper";
    private static final String MODULE_EXECUTE_GENERATED = "tests.test_execute-generated_";
    private static final String TEST_EXEC_FUNCTION = "__execute__";

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

            //Add init class
            ReflectConfigClass testInitRefConfClz = new ReflectConfigClass(getQualifiedClassName(org, name, version,
                    MODULE_INIT_CLASS_NAME));

            testInitRefConfClz.addReflectConfigClassMethod(
                    new ReflectConfigClassMethod(
                            "$moduleInit",
                            new String[]{"io.ballerina.runtime.internal.scheduling.Strand"}
                    )
            );

            testInitRefConfClz.addReflectConfigClassMethod(
                    new ReflectConfigClassMethod(
                            "$moduleStart",
                            new String[]{"io.ballerina.runtime.internal.scheduling.Strand"}
                    )
            );

            testInitRefConfClz.addReflectConfigClassMethod(
                    new ReflectConfigClassMethod(
                            "$moduleStop",
                            new String[]{"io.ballerina.runtime.internal.scheduling.RuntimeRegistry"}
                    )
            );
            //Add configuration mapper
            ReflectConfigClass testConfigurationMapperRefConfClz = new ReflectConfigClass(
                    getQualifiedClassName(org, name, version, MODULE_CONFIGURATION_MAPPER));

            testConfigurationMapperRefConfClz.addReflectConfigClassMethod(
                    new ReflectConfigClassMethod(
                            "$configureInit",
                            new String[]{"java.lang.String[]", "java.nio.file.Path[]", "java.lang.String"}
                    )
            );
            ReflectConfigClass testTestExecuteGeneratedRefConfClz = new ReflectConfigClass(
                    testSuiteMap.get(moduleName).getTestUtilityFunctions().get(TEST_EXEC_FUNCTION));
            testTestExecuteGeneratedRefConfClz.addReflectConfigClassMethod(
                    new ReflectConfigClassMethod(
                            TEST_EXEC_FUNCTION,
                            new String[]{
                                    "io.ballerina.runtime.internal.scheduling.Strand",
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
                            }
                    )
            );
            //Add classes with $MOCK_function methods (mock function case)
            if (!testSuiteMap.get(moduleName).getMockFunctionNamesMap().isEmpty()) {
                ReflectConfigClass functionMockingEntryClz = new ReflectConfigClass(getQualifiedClassName(
                        org, name, version, name.replace(DOT, FILE_NAME_PERIOD_SEPARATOR)));
                functionMockingEntryClz.setQueryAllDeclaredMethods(true);
                functionMockingEntryClz.setAllDeclaredFields(true);
                functionMockingEntryClz.setUnsafeAllocated(true);
                classList.add(functionMockingEntryClz);
            }

            //Add all class values to the array
            classList.add(testInitRefConfClz);
            classList.add(testConfigurationMapperRefConfClz);
            classList.add(testTestExecuteGeneratedRefConfClz);

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
                        for (int i = 0; i < mockedFunctions.length; i++) {
                            if (!methodSet.contains(mockedFunctions[i])) {
                                continue;
                            }
                            originalTestFileRefConfClz.addReflectConfigClassMethod(
                                    new ReflectConfigClassMethod(mockedFunctions[i]));
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
                functionToMock = functionToMock.replaceAll("\\\\", "");
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
        private String name;
        private String[] locales;

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
