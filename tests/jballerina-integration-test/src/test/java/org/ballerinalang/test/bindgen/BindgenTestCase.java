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
package org.ballerinalang.test.bindgen;

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
import static org.ballerinalang.test.context.LogLeecher.LeecherType.ERROR;
import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;

/**
 * Integration tests for the bindgen tool.
 *
 * @since 2.0.0
 */
public class BindgenTestCase extends BaseTest {

    private Path tempProjectsDir;
    private BMainInstance balClient;

    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        tempProjectsDir = Files.createTempDirectory("bal-test-integration-bindgen-");
        // copy TestProject to a temp
        Path testProject = Paths.get("src", "test", "resources", "bindgen")
                .toAbsolutePath();
        copyFolder(testProject, tempProjectsDir);
        balClient = new BMainInstance(balServer);
    }

    /**
     * Generate Ballerina bindings for a simple scenario and build the project.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Test the bindgen command functionality.")
    public void bindgenTest() throws BallerinaTestException {
        String buildMsg = "target/bin/bindgen.jar";
        LogLeecher buildLeecher = new LogLeecher(buildMsg);

        String bindgenMsg1 = "error: unable to generate the '";
        LogLeecher bindgenLeecher1 = new LogLeecher(bindgenMsg1, ERROR);

        String bindgenMsg2 = "Oh no, something really went wrong. Bad. Sad.";
        LogLeecher bindgenLeecher2 = new LogLeecher(bindgenMsg2, ERROR);

        String[] args = {"-mvn=org.yaml:snakeyaml:1.32", "-o=.", "org.yaml.snakeyaml.Yaml"};
        try {
            balClient.runMain("bindgen", args, null, new String[]{},
                    new LogLeecher[]{bindgenLeecher1, bindgenLeecher2}, tempProjectsDir.toString());
            throw new BallerinaTestException("Contains classes not generated.");
        } catch (BallerinaTestException e) {
            if (bindgenLeecher1.isTextFound()) {
                throw new BallerinaTestException("Bindings for some classes not generated.");
            } else if (bindgenLeecher2.isTextFound()) {
                throw new BallerinaTestException("Error while generating bindings: Bad. Sad. error occurred.");
            }
        }

        balClient.runMain("build", new String[]{}, null, new String[]{},
                new LogLeecher[]{buildLeecher}, tempProjectsDir.toString());
        buildLeecher.waitForText(5000);
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
        deleteFiles(this.tempProjectsDir);
    }
}
