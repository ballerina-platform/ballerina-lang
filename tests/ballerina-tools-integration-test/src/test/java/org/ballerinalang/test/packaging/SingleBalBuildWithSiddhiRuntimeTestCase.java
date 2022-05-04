/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.packaging;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.utils.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * This class tests building of a single bal file with siddhi runtime.
 */
public class SingleBalBuildWithSiddhiRuntimeTestCase extends BaseTest {
    private Path tempProjectDirectory;
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-packaging-siddhi-bal-");
        envVariables = TestUtils.getEnvVariables();
        Path tempModule = tempProjectDirectory.resolve("sourcePkg");
        Files.createDirectories(tempModule);

        // Write bal file
        Path balFilePath = tempModule.resolve("main.bal");
        Files.createFile(balFilePath);
        String mainFuncContent = "import ballerina/runtime;\n" +
                                 "import ballerina/io;\n" +
                                 "\n" +
                                 "type DeviceTempInfo record {\n" +
                                 "    int deviceID;\n" +
                                 "    int roomNo;\n" +
                                 "    float temp;\n" +
                                 "};\n" +
                                 "\n" +
                                 "type TempDiffInfo record {\n" +
                                 "    float initialTemp;\n" +
                                 "    float peakTemp;\n" +
                                 "};\n" +
                                 "stream<DeviceTempInfo> tempStream = new;\n" +
                                 "stream<TempDiffInfo> tempDiffInfoStream = new;\n" +
                                 "\n" +
                                 "function deployPeakTempDetectionRules() {\n" +
                                 "    forever {\n" +
                                 "        from every tempStream as e1, tempStream\n" +
                                 "            where e1.temp <= temp [1..] as e2,\n" +
                                 "        tempStream where e2[e2.length - 1].temp > temp as e3\n" +
                                 "        select e1.temp as initialTemp,\n" +
                                 "            e2[e2.length - 1].temp as peakTemp\n" +
                                 "        => (TempDiffInfo[] tempDiffInfos) {\n" +
                                 "            foreach var tempDiffInfo in tempDiffInfos {\n" +
                                 "                tempDiffInfoStream.publish(tempDiffInfo);\n" +
                                 "            }\n" +
                                 "        }\n" +
                                 "    }\n" +
                                 "}\n" +
                                 "\n" +
                                 "public function main() {\n" +
                                 "    // Deploy the streaming sequence rules.\n" +
                                 "    deployPeakTempDetectionRules();\n" +
                                 "}";
        Files.write(balFilePath, mainFuncContent.getBytes(), StandardOpenOption.CREATE);
    }

    @Test(description = "Test building a bal file without the siddhiruntime flag")
    public void testBuildingWithoutSiddhiRuntime() throws Exception {
        String[] clientArgs = {Paths.get("sourcePkg", "main.bal").toString()};
        String errOutput = balClient.runMainAndReadStdOut("build", clientArgs, new HashMap<>(),
                                                  tempProjectDirectory.toString(), true);
        Assert.assertEquals(errOutput, "error: .::main.bal:22:16: undefined symbol 'e1'\n" +
                                       "error: .::main.bal:23:13: undefined symbol 'e2'\n" +
                                       "error: .::main.bal:20:19: undefined symbol 'e1'\n" +
                                       "error: .::main.bal:20:30: undefined symbol 'temp'\n" +
                                       "error: .::main.bal:21:26: undefined symbol 'e2'\n" +
                                       "error: .::main.bal:21:51: undefined symbol 'temp'\n" +
                                       "error: .::main.bal:24:9: fields defined in select clause, incompatible with " +
                                       "output fields in type 'TempDiffInfo', expected '[initialTemp, peakTemp]' " +
                                       "but found '[peakTemp, initialTemp]'");
        Path generatedBalx = tempProjectDirectory.resolve("main.balx");
        Assert.assertFalse(Files.exists(generatedBalx));
    }

    @Test(description = "Test building a bal file with the siddhiruntime flag",
            dependsOnMethods = "testBuildingWithoutSiddhiRuntime")
    public void testBuildingWithSiddhiRuntime() throws Exception {
        String[] clientArgs = {"--siddhiruntime", Paths.get("sourcePkg", "main.bal").toString()};
        balClient.runMain("build", clientArgs, envVariables, new String[]{},
                new LogLeecher[]{}, tempProjectDirectory.toString());
        Path generatedBalx = tempProjectDirectory.resolve("main.balx");
        Assert.assertTrue(Files.exists(generatedBalx));
    }

    @AfterClass
    private void cleanup() throws Exception {
        TestUtils.deleteFiles(tempProjectDirectory);
    }
}
