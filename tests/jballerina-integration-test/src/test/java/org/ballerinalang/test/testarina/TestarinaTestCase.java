/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.testarina;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;

/**
 * Testarina integration tests.
 *
 * @since 1.0.0
 */
public class TestarinaTestCase extends BaseTest {
    private Path tempProjectDirectory;
    private Path serviceProjectPath;
    private BMainInstance balClient;

    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-testarina-project-");

        // copy TestProject1 to a temp
        Path originalTestProj1 = Paths.get("src", "test", "resources",
                "testarina", "ServiceTestProject").toAbsolutePath();
        this.serviceProjectPath = this.tempProjectDirectory.resolve("ServiceTestProject");
        copyFolder(originalTestProj1, this.serviceProjectPath);

        balClient = new BMainInstance(balServer);
    }

    @Test(description = "Test service start and stop during ballerina tests")
    public void testServiceTesting() throws Exception {
        String firstMsg = "Service 2 completed";
        LogLeecher clientLeecher1 = new LogLeecher(firstMsg);
        String secondMsg = "Service 1 completed";
        LogLeecher clientLeecher2 = new LogLeecher(secondMsg);
        balClient.runMain("test", new String[]{"--all"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2},
                serviceProjectPath.toString());
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
    }


    public void copyFolder(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }

    private void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @AfterClass
    private void cleanup() throws Exception {
        deleteFiles(this.tempProjectDirectory);
    }
}
