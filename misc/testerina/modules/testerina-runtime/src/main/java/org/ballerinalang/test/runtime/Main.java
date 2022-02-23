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
import org.ballerinalang.test.runtime.entity.ModuleStatus;
import org.ballerinalang.test.runtime.entity.TestReport;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.ballerinalang.test.runtime.util.TesterinaUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
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
import java.util.List;
import java.util.Map;

/**
 * Main class to init the test suit.
 */
public class Main {
    private static final PrintStream out = System.out;
    static TestReport testReport;
    static ClassLoader classLoader;

    public static void main(String[] args) throws IOException {
        int exitStatus = 0;
        int result;

        if (args.length >= 3) {
            Path targetPath = Paths.get(args[0]);
            Path testCache = targetPath.resolve(ProjectConstants.CACHES_DIR_NAME)
                            .resolve(ProjectConstants.TESTS_CACHE_DIR_NAME);
            boolean report = Boolean.parseBoolean(args[1]);
            boolean coverage = Boolean.parseBoolean(args[2]);

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

                        out.println("\n\t" + (moduleName.equals(testSuite.getPackageName()) ?
                                (moduleName.equals(TesterinaConstants.DOT) ? testSuite.getSourceFileName() : moduleName)
                                : testSuite.getPackageName() + TesterinaConstants.DOT + moduleName));

                        testSuite.setModuleName(moduleName);
                        List<String> testExecutionDependencies = testSuite.getTestExecutionDependencies();
                        classLoader = createClassLoader(testExecutionDependencies);

                        Path jsonTmpSummaryPath = testCache.resolve(moduleName).resolve(TesterinaConstants.STATUS_FILE);
                        result = startTestSuit(Paths.get(testSuite.getSourceRootPath()), testSuite, jsonTmpSummaryPath,
                                targetPath, classLoader);
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

    private static int startTestSuit(Path sourceRootPath, TestSuite testSuite, Path jsonTmpSummaryPath,
                                     Path targetPath, ClassLoader classLoader) throws IOException {
        int exitStatus = 0;
        try {
            TesterinaUtils.executeTests(sourceRootPath, targetPath, testSuite, classLoader);
        } catch (RuntimeException e) {
            exitStatus = 1;
        } finally {
            if (testSuite.isReportRequired()) {
                writeStatusToJsonFile(ModuleStatus.getInstance(), jsonTmpSummaryPath);
                ModuleStatus.clearInstance();
            }
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

    public static URLClassLoader createClassLoader(List<String> jarFilePaths) {
        List<URL> urlList = new ArrayList<>();

        for (String jarFilePath : jarFilePaths) {
            try {
                urlList.add(Paths.get(jarFilePath).toUri().toURL());
            } catch (MalformedURLException e) {
                // This path cannot get executed
                throw new RuntimeException("Failed to create classloader with all jar files", e);
            }
        }
        return AccessController.doPrivileged(
                (PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(urlList.toArray(new URL[0]),
                        ClassLoader.getSystemClassLoader()));
    }

    public static ClassLoader getClassLoader() {
        return classLoader;
    }

}
