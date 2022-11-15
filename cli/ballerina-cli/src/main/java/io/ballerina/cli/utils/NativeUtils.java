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
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.runtime.internal.util.RuntimeUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.identifier.Utils.encodeNonFunctionIdentifier;
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

    public static void createReflectConfig(Path nativeConfigPath, Package currentPackage) throws IOException {
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

            ReflectConfigClass testNameZeroName =
                    new ReflectConfigClass(getQualifiedClassName(org, name, version, name));
            testNameZeroName.setQueryAllDeclaredMethods(true);

            // Add all class values to the array
            classList.add(testInitClass);
            classList.add(testConfigurationMapper);
            classList.add(testTestExecuteGenerated);

            classList.add(testNameZeroName);

            // Increment tally to cover executable_<tally> class
            tally += 1;
        }

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
