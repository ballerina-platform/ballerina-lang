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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Testing running a bal file with a config file.
 *
 * @since 0.982.0
 */
public class BalRunWithConfigTestCase extends BaseTest {
    private Map<String, String> envVariables;
    private String balSourcePkgPath = (new File("src/test/resources/config")).getAbsolutePath();

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        envVariables = PackagingTestUtils.getEnvVariables();
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
        String[] clientArgs = {"--config", confPath, "read_from_config.bal"};
        LogLeecher clientLeecher = new LogLeecher("localhost");
        balClient.runMain("run", clientArgs, envVariables, new String[0], new LogLeecher[]{clientLeecher},
                          balSourcePkgPath);
    }
}
