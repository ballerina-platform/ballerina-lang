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

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.BaseTest;
import org.ballerinalang.test.utils.PackagingTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Testing doc generation using the doc command.
 *
 * @since 0.982.0
 */
public class PackagingDocGenTestCase extends BaseTest {
    private Path tempProjectDirectory;
    private String[] envVariables;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        envVariables = PackagingTestUtils.getEnvVariables();
    }

    @Test(description = "Test init a ballerina project to generate docs")
    public void testInitProject() throws Exception {
        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", "integrationtests\n", "\n", "m\n", "foo\n", "s\n", "bar\n", "f\n"};
        serverInstance.runMainWithClientOptions(clientArgsForInit, options, envVariables, "init",
                                                tempProjectDirectory.toString());
    }

    @Test(description = "Test doc generation for package", dependsOnMethods = "testInitProject")
    public void testDocGenerationForPackage() throws Exception {
        String[] clientArgs = {"foo"};
        serverInstance.runMain(clientArgs, envVariables, "doc", tempProjectDirectory.toString());

        Path apiDocsGenerated = tempProjectDirectory.resolve("target").resolve("api-docs");
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("index.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("package-list.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("foo.html")));
    }

    @Test(description = "Test doc generation for single bal file", dependsOnMethods = "testInitProject")
    public void testDocGenerationForSingleBalFile() throws Exception {
        String[] clientArgs = {"main.bal"};
        serverInstance.runMain(clientArgs, envVariables, "doc", tempProjectDirectory.resolve("foo").toString());

        Path apiDocsGenerated = tempProjectDirectory.resolve("foo").resolve("target").resolve("api-docs");
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("index.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("package-list.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("main.bal.html")));
    }

    @Test(description = "Test doc generation for a project", dependsOnMethods = "testInitProject")
    public void testDocGenerationForProject() throws Exception {
        serverInstance.runMain(new String[0], envVariables, "doc", tempProjectDirectory.toString());

        Path apiDocsGenerated = tempProjectDirectory.resolve("target").resolve("api-docs");
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("index.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("package-list.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("foo.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("bar.html")));
    }

    @AfterClass
    private void cleanup() throws Exception {
        PackagingTestUtils.deleteFiles(tempProjectDirectory);
    }
}
