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
package org.ballerinalang.test.swagger;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.utils.PackagingTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Test if folder and files are correctly generated when generating swagger clients.
 */
public class OutputPathCreationTestCase extends BaseTest {
    private static final String MODULE_NAME = "say";
    private static final String SWAGGER_YAML = (new File("src/test/resources/swagger/sample.yaml")).getAbsolutePath();
    private Path balProject;
    private Path balModule;
    
    
    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        balProject = Files.createTempDirectory("bal-test-integration-swagger-out-");
        balClient.runMain("init", new String[0], new HashMap<>(), new String[0], new LogLeecher[]{},
                balProject.toString());
        balModule = balProject.resolve(MODULE_NAME);
    }
    
    @Test(description = "Test if folder paths are created correctly.")
    public void testFolderStructureCreation() throws BallerinaTestException {
        String[] clientArgsForSwaggerClientGen = {"client", SWAGGER_YAML, "-o", balProject.toString(), "-m",
                MODULE_NAME};
        balClient.runMain("swagger", clientArgsForSwaggerClientGen, new HashMap<>(), new String[]{},
                new LogLeecher[]{}, balServer.getServerHome());
        Assert.assertTrue(Files.exists(balModule), "Swagger client was not generated");
    }
    
    @Test(description = "Test if folders/files are recreated correctly when regenerating.")
    public void testFolderStructureRecreation() throws BallerinaTestException, IOException {
        Path tempBalFile = balModule.resolve("temp.bal");
        Files.createFile(tempBalFile);
    
        String[] clientArgsForSwaggerClientGen = {"client", SWAGGER_YAML, "-o", balProject.toString(), "--module",
                MODULE_NAME};
        balClient.runMain("swagger", clientArgsForSwaggerClientGen, new String[]{});
        Assert.assertTrue(Files.exists(balModule), "Swagger client was not generated");
        Assert.assertTrue(Files.exists(tempBalFile), "Created temp file has been deleted");
    }
    
    @AfterClass
    private void cleanup() throws Exception {
        PackagingTestUtils.deleteFiles(balProject);
    }
}
