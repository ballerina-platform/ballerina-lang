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
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import org.ballerinalang.test.runtime.entity.TestSuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.identifier.Utils.encodeNonFunctionIdentifier;
import static io.ballerina.runtime.api.constants.RuntimeConstants.FILE_NAME_PERIOD_SEPARATOR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.ANON_ORG;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;

/**
 * Utility functions and classes for test native-image generation.
 *
 * @since 2.3.0
 */
public class NativeUtils {
    private static final String MODULE_INIT_CLASS_NAME = "$_init";
    private static final String MODULE_CONFIGURATION_MAPPER = "$configurationMapper";
    private static final String MODULE_EXECUTE_GENERATED = "tests.test_execute-generated_";

    public static void createReflectConfig(Path nativeConfigPath, Package currentPackage, Map<String, TestSuite>
                                                                testSuiteMap) throws IOException {
        String org = currentPackage.packageOrg().toString();
        String version = currentPackage.packageVersion().toString();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<ReflectConfigClass> classList = new ArrayList<>();

        int tally = 1;

        for (Module module : currentPackage.modules()) {
            String name = module.moduleName().toString();

            ReflectConfigClass testInitClass = new ReflectConfigClass(getQualifiedClassName(org, name, version,
                    MODULE_INIT_CLASS_NAME));

            testInitClass.addReflectConfigClassMethod(
                    new ReflectConfigClassMethod(
                            "$moduleInit",
                            new String[]{"io.ballerina.runtime.internal.scheduling.Strand"}
                    )
            );

            testInitClass.addReflectConfigClassMethod(
                    new ReflectConfigClassMethod(
                            "$moduleStart",
                            new String[]{"io.ballerina.runtime.internal.scheduling.Strand"}
                    )
            );

            testInitClass.addReflectConfigClassMethod(
                    new ReflectConfigClassMethod(
                            "$moduleStop",
                            new String[]{"io.ballerina.runtime.internal.scheduling.RuntimeRegistry"}
                    )
            );

            ReflectConfigClass testConfigurationMapper = new ReflectConfigClass(getQualifiedClassName(org, name,
                    version, MODULE_CONFIGURATION_MAPPER));

            testConfigurationMapper.addReflectConfigClassMethod(
                    new ReflectConfigClassMethod(
                            "$configureInit",
                            new String[]{"java.lang.String[]", "java.nio.file.Path[]", "java.lang.String"}
                    )
            );

            ReflectConfigClass testTestExecuteGenerated = new ReflectConfigClass(getQualifiedClassName(org, name,
                    version, MODULE_EXECUTE_GENERATED + tally));

            testTestExecuteGenerated.addReflectConfigClassMethod(
                    new ReflectConfigClassMethod(
                            "__execute__",
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

            ReflectConfigClass testNameZeroName = new ReflectConfigClass(
                    getQualifiedClassName(org, name, version, name.replace(DOT, FILE_NAME_PERIOD_SEPARATOR)));
            testNameZeroName.setQueryAllDeclaredMethods(true);
            testNameZeroName.setAllDeclaredFields(true);
            testNameZeroName.setUnsafeAllocated(true);


            // Add all class values to the array
            classList.add(testInitClass);
            classList.add(testConfigurationMapper);
            classList.add(testTestExecuteGenerated);

            classList.add(testNameZeroName);

            // Increment tally to cover executable_<tally> class
            tally += 1;

        }

        Path mockedFunctionClassPath = nativeConfigPath.resolve("mocked-func-class-map.json");
        File mockedFunctionClassFile = new File(mockedFunctionClassPath.toString());
        if (mockedFunctionClassFile.isFile()) {
            try (BufferedReader br = Files.newBufferedReader(mockedFunctionClassPath, StandardCharsets.UTF_8)) {
                Gson gsonRead = new Gson();
                Map<String, String[]> testFileMockedFunctionMapping = gsonRead.fromJson(br,
                        new TypeToken<Map<String, String[]>>() {
                        }.getType());
                if (!testFileMockedFunctionMapping.isEmpty()) {
                    ReflectConfigClass originalTestFileRefConfClz;
                    for (Map.Entry<String, String[]> testFileMockedFunctionMappingEntry :
                            testFileMockedFunctionMapping.entrySet()) {
                        String moduleName = testFileMockedFunctionMappingEntry.getKey().split("-")[0];
                        String testFile = testFileMockedFunctionMappingEntry.getKey().split("-")[1];
                        String[] mockedFunctions = testFileMockedFunctionMappingEntry.getValue();
                        originalTestFileRefConfClz = new ReflectConfigClass(getQualifiedClassName(org, moduleName,
                                version, testFile));
                        for (int i = 0; i < mockedFunctions.length; i++) {
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
        ReflectConfigClass originalBalFileRefConfClz = new ReflectConfigClass("");
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



//        ReflectConfigClass originalBalFileRefConfClz;
//        for (Map.Entry<String, List<String>> balFileClassOrigMockFuncEntry :
//                balFileClassOrigMockFuncMapping.entrySet()) {
//            if (balFileClassOrigMockFuncEntry.getValue().size() > 0) {
//                originalBalFileRefConfClz = new ReflectConfigClass(balFileClassOrigMockFuncEntry.getKey());
//                for (String methodName : balFileClassOrigMockFuncEntry.getValue()) {
//                    originalBalFileRefConfClz.addReflectConfigClassMethod(new ReflectConfigClassMethod(methodName));
//                }
//                originalBalFileRefConfClz.setQueryAllDeclaredMethods(true);
//                originalBalFileRefConfClz.setQueryAllDeclaredMethods(true);
//                originalBalFileRefConfClz.setUnsafeAllocated(true);
//            }
//        }

//        ReflectConfigClass originalFunctionRefConfClz;

//        for (String classWithFunctionMock : balFileClassOrigMockFuncMapping) {
//            originalFunctionRefConfClz = new ReflectConfigClass(classWithFunctionMock);
//            originalFunctionRefConfClz.setQueryAllDeclaredMethods(true);
//            originalFunctionRefConfClz.setQueryAllDeclaredMethods(true);
//            originalFunctionRefConfClz.setUnsafeAllocated(true);
//            classList.add(originalFunctionRefConfClz);
//        }

        ReflectConfigClass runtimeEntityTestSuite = new ReflectConfigClass("org.ballerinalang.test.runtime.entity" +
                ".TestSuite");
        runtimeEntityTestSuite.setAllDeclaredFields(true);
        runtimeEntityTestSuite.setUnsafeAllocated(true);

        classList.add(runtimeEntityTestSuite);


        // Write the array to the config file
        try (Writer writer = new FileWriter(nativeConfigPath.resolve("reflect-config.json").toString(),
                Charset.defaultCharset())) {
            gson.toJson(classList, writer);
            writer.flush();
        }
    }

    private static void extractMockFunctionClassMapping(Map<String, TestSuite> testSuiteMap, Map<String, List<String>> mockFunctionClassMapping) {
        for (Map.Entry<String, TestSuite> testSuiteEntry : testSuiteMap.entrySet()) {
            TestSuite suite = testSuiteEntry.getValue();
            for (Map.Entry<String, String> mockFunctionEntry : suite.getMockFunctionNamesMap().entrySet()) {
                String mockFunctionId = mockFunctionEntry.getKey();
                String mockFunctionClass = mockFunctionId.split("#")[0];
                String mockFunction = mockFunctionId.split("#")[1];
                if (mockFunctionClassMapping.containsKey(mockFunctionClass)) {
                    mockFunctionClassMapping.get(mockFunctionClass).add("$ORIG_" + mockFunction);
                } else {
                    List<String> mockFunctionList = new ArrayList<>();
                    mockFunctionList.add("$ORIG_" + mockFunction);
                    mockFunctionClassMapping.put(mockFunctionClass, mockFunctionList);
                }
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
