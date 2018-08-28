/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Testing building of a single bal file.
 */
public class PackagingSingleBalBuildTestCase extends BaseTest {
    private String serverZipPath;
    private Path tempProjectDirectory;
    private Path balFilePath;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-single-bal-");
        Path tempPackage = tempProjectDirectory.resolve("sourcePkg");
        Files.createDirectories(tempPackage);

        // Write bal file
        balFilePath = tempPackage.resolve("main.bal");
        Files.createFile(balFilePath);
        String mainFuncContent = "import ballerina/io;\n" +
                "\n" +
                "documentation {\n" +
                "   Prints `Hello World`.\n" +
                "}\n" +
                "function main(string... args) {\n" +
                "    io:println(\"Hello World!\");\n" +
                "}\n";
        Files.write(balFilePath, mainFuncContent.getBytes(), StandardOpenOption.CREATE);

        serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
    }

    @Test(description = "Test building a bal file by giving the absolute path")
    public void testBuildingSourceWithAbsolutePath() throws Exception {
        Path currentDirPath = tempProjectDirectory.resolve("foo");
        Files.createDirectories(currentDirPath);

        // Test ballerina build
        BMainInstance ballerinaServer = new BMainInstance(balServer);
        String[] clientArgs = {balFilePath.toString()};
        ballerinaServer.runMain("build", clientArgs, System.getenv(),
                new String[]{}, new LogLeecher[]{}, currentDirPath.toString());
        Path generatedBalx = currentDirPath.resolve("main.balx");
        Assert.assertTrue(Files.exists(generatedBalx));
    }

    @Test(description = "Test building a bal file by giving the path from the current directory")
    public void testBuildingSourceWithCurrentDir() throws Exception {
        // Test ballerina build
        BMainInstance ballerinaServer = new BMainInstance(balServer);
        String[] clientArgs = {Paths.get("sourcePkg", "main.bal").toString()};
        ballerinaServer.runMain("build", clientArgs, System.getenv(), new String[]{},
                new LogLeecher[]{}, tempProjectDirectory.toString());
        Path generatedBalx = tempProjectDirectory.resolve("main.balx");
        Assert.assertTrue(Files.exists(generatedBalx));
    }

    @Test(description = "Test building a bal file to the output file given by the user")
    public void testBuildingSourceToOutput() throws Exception {
        Path targetDirPath = tempProjectDirectory.resolve("target");
        Files.createDirectories(targetDirPath);

        // Test ballerina build
        BMainInstance ballerinaServer = new BMainInstance(balServer);
        String[] clientArgs = {"-o", targetDirPath.resolve("main.bal").toString(),
                balFilePath.toString()};
        ballerinaServer.runMain("build", clientArgs, getEnv(), new String[]{},
                new LogLeecher[]{}, tempProjectDirectory.toString());
        Path generatedBalx = targetDirPath.resolve("main.balx");
        Assert.assertTrue(Files.exists(generatedBalx));
    }

    private Map<String, String> getEnv() {
        Map<String, String> envVarMap = System.getenv();
        Map<String, String> retMap = new HashMap<>();
        envVarMap.forEach(retMap::put);
        return retMap;
    }

    @AfterClass
    private void cleanup() throws Exception {
        deleteFiles(tempProjectDirectory);
    }

    /**
     * Delete files inside directories.
     *
     * @param dirPath directory path
     * @throws IOException throw an exception if an issue occurs
     */
    private void deleteFiles(Path dirPath) throws IOException {
        Files.walk(dirPath)
             .sorted(Comparator.reverseOrder())
             .forEach(path -> {
                 try {
                     Files.delete(path);
                 } catch (IOException e) {
                     Assert.fail(e.getMessage(), e);
                 }
             });
    }
}
