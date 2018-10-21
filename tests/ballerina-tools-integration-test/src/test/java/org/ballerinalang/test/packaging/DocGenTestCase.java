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
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.utils.PackagingTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Testing doc generation using the doc command.
 *
 * @since 0.982.0
 */
public class DocGenTestCase extends BaseTest {
    private Path tempProjectDirectory;
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        envVariables = PackagingTestUtils.getEnvVariables();
    }

    @Test(description = "Test init a ballerina project to generate docs")
    public void testInitProject() throws Exception {
        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", "integrationtests\n", "\n", "m\n", "foo\n", "s\n", "bar\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[0],
                                                tempProjectDirectory.toString());
    }

    @Test(description = "Test doc generation for module", dependsOnMethods = "testInitProject")
    public void testDocGenerationForModule() throws Exception {
        String[] clientArgs = {"foo"};
        balClient.runMain("doc", clientArgs, envVariables, new String[0], new LogLeecher[0],
                tempProjectDirectory.toString());

        Path apiDocsGenerated = tempProjectDirectory.resolve("target").resolve("api-docs");
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("index.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("module-list.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("foo.html")));
    }

    @Test(description = "Test doc generation for single bal file", dependsOnMethods = "testInitProject")
    public void testDocGenerationForSingleBalFile() throws Exception {
        String[] clientArgs = {"main.bal"};
        balClient.runMain("doc", clientArgs, envVariables, new String[0], new LogLeecher[0],
                tempProjectDirectory.resolve("foo").toString());

        Path apiDocsGenerated = tempProjectDirectory.resolve("foo").resolve("target").resolve("api-docs");
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("index.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("module-list.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("main.bal.html")));
    }

    @Test(description = "Test doc generation for a project", dependsOnMethods = "testInitProject")
    public void testDocGenerationForProject() throws Exception {
        balClient.runMain("doc", new String[0], envVariables, new String[0], new LogLeecher[0],
                tempProjectDirectory.toString());

        Path apiDocsGenerated = tempProjectDirectory.resolve("target").resolve("api-docs");
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("index.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("module-list.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("foo.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("bar.html")));
    }

    @AfterClass
    private void cleanup() throws Exception {
        PackagingTestUtils.deleteFiles(tempProjectDirectory);
    }
}
