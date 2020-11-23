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

    private Path tempProjectsDirectory;
    private Path projectPath;
    private BMainInstance balClient;

    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        tempProjectsDirectory = Files.createTempDirectory("bal-test-integration-bindgen-");
        // copy TestProject to a temp
        Path originalTestProj1 = Paths.get("src", "test", "resources", "bindgen", "TestProject")
                .toAbsolutePath();
        projectPath = tempProjectsDirectory.resolve("module1");
        copyFolder(originalTestProj1, projectPath);
        balClient = new BMainInstance(balServer);
    }

    /**
     * Generate Ballerina bindings covering multiple scenarios and build the project.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Test the bindgen command functionality.")
    public void bindgenTest() throws BallerinaTestException {
        String buildMsg = "target/bin/module1.jar";
        LogLeecher buildLeecher = new LogLeecher(buildMsg);

        String bindgenMsg = "class could not be generated.";
        LogLeecher bindgenLeecher = new LogLeecher(bindgenMsg, ERROR);

        String runMsg1 = "Array util tests successful.";
        String runMsg2 = "Exception handling tests successful.";
        LogLeecher runLeecher1 = new LogLeecher(runMsg1);
        LogLeecher runLeecher2 = new LogLeecher(runMsg2);

        String[] args = {"-mvn=org.yaml:snakeyaml:1.25", "-o=./src/module1", "org.yaml.snakeyaml.Yaml",
                "java.lang.Object", "java.lang.String", "java.lang.Class", "java.lang.StringBuffer",
                "java.lang.ArithmeticException", "java.lang.AssertionError", "java.lang.StackTraceElement",
                "java.lang.Character$Subset", "java.lang.Character", "java.io.File", "java.lang.Short",
                "java.io.FileInputStream", "java.io.OutputStream", "java.io.FileOutputStream", "java.util.HashSet",
                "java.util.Set", "java.util.Iterator", "java.awt.Window$Type", "java.util.ArrayList"};
        try {
            balClient.runMain("bindgen", args, null, new String[]{},
                    new LogLeecher[]{bindgenLeecher}, projectPath.toString());
            throw new BallerinaTestException("Contains classes not generated.");
        } catch (BallerinaTestException e) {
            if (bindgenLeecher.isTextFound()) {
                throw new BallerinaTestException("Contains classes not generated.", e);
            }
        }
        balClient.runMain("build", new String[]{"-a"}, null, new String[]{},
                new LogLeecher[]{buildLeecher}, projectPath.toString());
        balClient.runMain("run", new String[]{"target/bin/module1.jar"}, null, new String[]{},
                new LogLeecher[]{runLeecher1, runLeecher2}, projectPath.toString());
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
        deleteFiles(this.tempProjectsDirectory);
    }
}
