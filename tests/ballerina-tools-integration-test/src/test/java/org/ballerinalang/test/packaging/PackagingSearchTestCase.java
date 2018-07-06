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

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Testing searching packages from central.
 */
public class PackagingSearchTestCase extends IntegrationTestCase {
    private String serverZipPath;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
    }

    @Test(description = "Test pulling a package from central")
    public void testPull() throws Exception {
        ServerInstance ballerinaClient = new ServerInstance(serverZipPath);
        String[] clientArgs = {"searchTestPackage"};

        Path outputFilePath = Paths.get(new File("src" + File.separator + "test" + File.separator + "resources"
                                                         + File.separator + "packaging" + File.separator +
                                                         "ballerinaSearch.out").getAbsolutePath());
        String loggedMsg = new String(Files.readAllBytes(outputFilePath));

        LogLeecher clientLeecher = new LogLeecher(loggedMsg);
        ballerinaClient.addLogLeecher(clientLeecher);
        ballerinaClient.runMain(clientArgs, getEnvVariables(), "search");
        clientLeecher.waitForText(5000);
    }

    /**
     * Get environment variables and add ballerina_home as a env variable the tmp directory.
     *
     * @return env directory variable array
     */
    private String[] getEnvVariables() {
        List<String> variables = new ArrayList<>();
        Map<String, String> envVarMap = System.getenv();

        envVarMap.forEach((key, value) -> variables.add(key + "=" + value));
        variables.add("BALLERINA_DEV_STAGE_CENTRAL" + "=" + "true");
        return variables.toArray(new String[variables.size()]);
    }
}
