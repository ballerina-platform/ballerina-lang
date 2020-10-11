/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.observability;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Base Test Case of all Observability related test cases.
 */
public class ObservabilityBaseTest extends BaseTest {
    private static BServerInstance servicesServerInstance;

    private static final String OBESERVABILITY_TEST_BIR = System.getProperty("observability.test.utils");
    private static final String TEST_NATIVES_JAR = "observability-test-natives.jar";
    private static final String TEST_OBSERVE_JAR = "ballerina-testobserve-0.0.0.jar";

    protected static final String SERVER_CONNECTOR_NAME = "testobserve_listener";

    protected void setupServer(String testProject, String testModule, int[] requiredPorts) throws Exception {
        final String serverHome = balServer.getServerHome();

        // Copy for Ballerina.toml reference to natives Jar
        copyFile(new File(System.getProperty(TEST_NATIVES_JAR)),
                Paths.get(Paths.get(System.getProperty(TEST_NATIVES_JAR)).getParent().toString(), TEST_NATIVES_JAR)
                        .toFile());

        // Copy to bre/libs
        copyFile(new File(System.getProperty(TEST_NATIVES_JAR)),
                Paths.get(serverHome, "bre", "lib", TEST_NATIVES_JAR).toFile());
        copyFile(Paths.get(OBESERVABILITY_TEST_BIR, "build", "generated-bir-jar", TEST_OBSERVE_JAR).toFile(),
                Paths.get(serverHome, "bre", "lib", TEST_OBSERVE_JAR).toFile());

        // Copy to lib/repo
        Path observeTestBaloPath =
                Paths.get(OBESERVABILITY_TEST_BIR, "build", "generated-balo", "repo", "ballerina", "testobserve");
        FileUtils.copyDirectoryToDirectory(observeTestBaloPath.toFile(),
                Paths.get(serverHome, "lib", "repo", "ballerina").toFile());

        // Copy to bir-cache
        FileUtils.copyDirectoryToDirectory(observeTestBaloPath.toFile(),
                Paths.get(serverHome, "bir-cache", "ballerina").toFile());
        FileUtils.copyDirectoryToDirectory(
                Paths.get(OBESERVABILITY_TEST_BIR, "build", "generated-bir", "ballerina", "testobserve").toFile(),
                Paths.get(serverHome, "bir-cache", "ballerina").toFile());

        String basePath = Paths.get("src", "test", "resources", "observability", testProject).toFile()
                .getAbsolutePath();

        String configFile = Paths.get("src", "test", "resources", "observability", testProject, "ballerina.conf")
                .toFile().getAbsolutePath();
        String[] args = new String[] { "--b7a.config.file=" + configFile };

        // Don't use 9898 port here. It is used in metrics test cases.
        servicesServerInstance = new BServerInstance(balServer);
        servicesServerInstance.startServer(basePath, testModule, null, args, requiredPorts);
    }

    protected void cleanupServer() throws BallerinaTestException {
        servicesServerInstance.removeAllLeechers();
        servicesServerInstance.shutdownServer();
    }

    private void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
    }
}
