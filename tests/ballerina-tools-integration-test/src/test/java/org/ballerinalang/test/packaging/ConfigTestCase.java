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

import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.utils.TestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Testing running a bal file with a config file.
 *
 * @since 0.982.0
 */
public class ConfigTestCase extends BaseTest {
    private Map<String, String> envVariables;
    private Path tempProjectDirectory;
    private String balSourcePkgPath = (new File("src/test/resources/config")).getAbsolutePath();

    @BeforeClass()
    public void setUp() throws IOException {
        envVariables = TestUtils.getEnvVariables();

        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-config-test-project-");
        FileUtils.copyDirectory(Paths.get((new File("src/test/resources/project")).getAbsolutePath()).toFile(),
                                tempProjectDirectory.toFile());
        Files.createDirectories(tempProjectDirectory.resolve(".ballerina"));
    }

    @Test(description = "Test running a ballerina file with the default config from the same directory")
    public void testRunWithDefaultConfig() throws Exception {
        String[] clientArgs = {"read_from_config.bal"};
        LogLeecher clientLeecher = new LogLeecher("localhost");
        balClient.runMain("run", clientArgs, envVariables, new String[0], new LogLeecher[]{clientLeecher},
                          balSourcePkgPath);
    }

    @Test(description = "Test running a ballerina file with the default config from another directory")
    public void testRunWithDefaultConfigFromDiff() throws Exception {
        String balSourcePath = (new File("src/test/resources/config/read_from_config.bal")).getAbsolutePath();
        String pkgDirPath = (new File("src/test/resources/config/pkg")).getAbsolutePath();
        String[] clientArgs = {balSourcePath};
        LogLeecher clientLeecher = new LogLeecher("localhost");
        balClient.runMain("run", clientArgs, envVariables, new String[0], new LogLeecher[]{clientLeecher},
                          pkgDirPath);
    }

    @Test(description = "Test running a ballerina file by specifying the config file path")
    public void testRunWithConfig() throws Exception {
        String confPath = (new File("src/test/resources/config/pkg/example.conf")).getAbsolutePath();
        String[] clientArgs = {"read_from_config.bal", "--b7a.config.file=" + confPath};
        LogLeecher clientLeecher = new LogLeecher("localhost");
        balClient.runMain("run", clientArgs, envVariables, new String[0], new LogLeecher[]{clientLeecher},
                          balSourcePkgPath);
    }

    @Test(description = "Execute tests in a ballerina module by specifying the config file path")
    public void testModuleWithConfig() throws Exception {
        String[] clientArgs = {"--b7a.config.file=sample.conf"};
        String msg = "http://localhost:9090/sample/hello";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", clientArgs, envVariables, new String[0], new LogLeecher[]{clientLeecher},
                          tempProjectDirectory.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Execute tests in a ballerina module with the default config file")
    public void testModuleWithDefaultConfig() throws Exception {
        String msg = "http://localhost:9090/default/hello";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[0], envVariables, new String[0], new LogLeecher[]{clientLeecher},
                          tempProjectDirectory.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Execute tests in a ballerina module with a non-existing config file")
    public void testModuleWithInvalidConfig() throws Exception {
        String[] clientArgs = {"--b7a.config.file=invalid.conf"};
        LogLeecher clientLeecher = new LogLeecher("configuration file not found: invalid.conf",
                                                  LogLeecher.LeecherType.ERROR);
        balClient.runMain("test", clientArgs, envVariables, new String[0], new LogLeecher[]{clientLeecher},
                          tempProjectDirectory.toString());
        clientLeecher.waitForText(2000);
    }

    @Test(description = "Test running a ballerina file with the default config from the same directory")
    public void testRunWithInvalidDefaultConfig() throws Exception {
        Path sourcePath = Paths.get(balSourcePkgPath, "invalid");
        String[] clientArgs = {"read_from_config.bal"};
        LogLeecher clientLeecher = new LogLeecher("error: invalid toml syntax at ballerina.conf:4",
                LogLeecher.LeecherType.ERROR);
        balClient.runMain("run", clientArgs, envVariables, new String[0], new LogLeecher[]{clientLeecher},
                sourcePath.toString());
    }

    @Test(description = "Test running a ballerina file by specifying an invalid config file path")
    public void testRunWithInvalidConfig() throws Exception {
        Path sourcePath = Paths.get(balSourcePkgPath, "invalid");
        String confPath = (new File("src/test/resources/config/invalid/test.conf")).getAbsolutePath();
        String[] clientArgs = {"read_from_config.bal", "--b7a.config.file="+ confPath"};
        LogLeecher clientLeecher = new LogLeecher("error: invalid toml syntax at test.conf:5",
                LogLeecher.LeecherType.ERROR);
        balClient.runMain("run", clientArgs, envVariables, new String[0], new LogLeecher[]{clientLeecher},
                sourcePath.toString());
    }

    @AfterClass
    private void cleanup() throws Exception {
        TestUtils.deleteFiles(tempProjectDirectory);
    }
}
