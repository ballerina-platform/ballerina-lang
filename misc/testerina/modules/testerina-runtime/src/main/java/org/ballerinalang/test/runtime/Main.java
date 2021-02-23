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
import io.ballerina.runtime.internal.launch.LaunchUtils;
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

/**
 * Main class to init the test suit.
 */
public class Main {
    static TestReport testReport;

    static List<String> testExecutionDependencies;

    public static List<String> getTestExecutionDependencies() {
        return testExecutionDependencies;
    }

    public static void main(String[] args) throws IOException {
        String[] moduleNameList = args[0].split("#");
        Path testCache = Paths.get(args[1]);
        String target = args[2];
        boolean report = Boolean.valueOf(args[3]);
        boolean coverage = Boolean.valueOf(args[4]);

        if (report || coverage) {
            testReport = new TestReport();
        }

        int exitStatus = 1;

        for (String moduleName : moduleNameList) {
            Path moduleCachePath = testCache.resolve(moduleName).resolve(TesterinaConstants.TESTERINA_TEST_SUITE);
            Path jsonTmpSummaryPath = testCache.resolve(moduleName).resolve(TesterinaConstants.STATUS_FILE);

            try (BufferedReader br = Files.newBufferedReader(moduleCachePath, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                TestSuite testSuite = gson.fromJson(br, TestSuite.class);
                String[] configArgs =
                        new String[]{target, testSuite.getOrgName(), testSuite.getPackageName(), moduleName};
                LaunchUtils.initConfigurations(configArgs);
                testSuite.setModuleName(moduleName);
                testExecutionDependencies = testSuite.getTestExecutionDependencies();
                ClassLoader classLoader = createClassLoader(testExecutionDependencies);
                exitStatus = startTestSuit(Paths.get(testSuite.getSourceRootPath()), testSuite, jsonTmpSummaryPath,
                        classLoader);
            } catch (Exception e) {
                exitStatus = 1;
            }
        }

        Runtime.getRuntime().exit(exitStatus);
    }

    private static int startTestSuit(Path sourceRootPath, TestSuite testSuite, Path jsonTmpSummaryPath,
                                     ClassLoader classLoader) throws IOException {
        int exitStatus = 0;
        try {
            TesterinaUtils.executeTests(sourceRootPath, testSuite, classLoader);
        } catch (RuntimeException e) {
            exitStatus = 1;
        } finally {
            if (testSuite.isReportRequired()) {
                writeStatusToJsonFile(ModuleStatus.getInstance(), jsonTmpSummaryPath);
                ModuleStatus.clearInstance(); // Check if this resets
            }
            return exitStatus;
            //Runtime.getRuntime().exit(exitStatus); Stops the runtime execution
        }
    }

    private static void writeStatusToJsonFile(ModuleStatus moduleStatus, Path tmpJsonPath) throws IOException {
        File jsonFile = new File(tmpJsonPath.toString());
        if (!Files.exists(tmpJsonPath.getParent())) {
            Files.createDirectories(tmpJsonPath.getParent());
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            String json = gson.toJson(moduleStatus);
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
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

        URLClassLoader test = AccessController.doPrivileged(
                (PrivilegedAction<URLClassLoader>)
                        () -> new URLClassLoader(urlList.toArray(new URL[0]), ClassLoader.getSystemClassLoader()));

        return test;
    }
}
