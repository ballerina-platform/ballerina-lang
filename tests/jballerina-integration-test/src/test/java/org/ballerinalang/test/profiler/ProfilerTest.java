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

package org.ballerinalang.test.profiler;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Tests ballerina profiler.
 *
 * @since 2201.7.0
 */
public class ProfilerTest extends BaseTest {
    private static final String testFileLocation =
            Paths.get("src", "test", "resources", "profiler")
                    .toAbsolutePath()
                    .toString();

    String sourceRoot = testFileLocation + "/";
    String packageName = "singleBalFile";
    String outputFileName = "performance_report.json";
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
    }

    @Test
    public void testProfilerExecution() throws BallerinaTestException {
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.addJavaAgents(envProperties);
        CompletableFuture<Void> runMainTask = CompletableFuture.runAsync(() -> {
            try {
                bMainInstance.runMain(
                        "run",
                        new String[]{"--profile", packageName},
                        envProperties,
                        null,
                        null,
                        sourceRoot
                );

            } catch (BallerinaTestException ignore) {
            }
        });
        try {
            runMainTask.get(20, TimeUnit.SECONDS);
        } catch (Exception ignore) {
        }
        List<String> fileNames = Arrays.stream(
                        Objects.requireNonNull(
                                new File(sourceRoot + packageName + "/target/bin/").listFiles(File::isFile)
                        )
                )
                .map(File::getName)
                .collect(Collectors.toList());
        Assert.assertTrue(fileNames.contains(outputFileName), "Error testing the profiler execution");
    }

    @Test
    public void testProfilerOutput() throws BallerinaTestException {
        String outputFilePath = sourceRoot + packageName + "/target/bin/" + outputFileName;
        try {
            String outputJsonString = Files.readString(Paths.get(outputFilePath));
            int functionCount = countFunctionOccurrences(outputJsonString);
            Assert.assertEquals(functionCount, 182);
        } catch (IOException e) {
            throw new BallerinaTestException("Error testing the profiler output");
        }
        stopProfiler();
    }

    private static void stopProfiler() {
        try {
            URL terminateUrl = new URL("http://localhost:2324/terminate");
            HttpURLConnection connection = (HttpURLConnection) terminateUrl.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }
            connection.disconnect();
        } catch (Exception ignore) {
        }
    }

    private static int countFunctionOccurrences(String outputJsonString) {
        outputJsonString = outputJsonString.replaceAll("\\s+", "");
        return outputJsonString.split("\"name\"").length - 1;
    }
}
