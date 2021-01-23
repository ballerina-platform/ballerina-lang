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

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Base Test Case of all Observability related test cases.
 */
public class ObservabilityBaseTest extends BaseTest {
    private static BServerInstance servicesServerInstance;

    private static final String OBESERVABILITY_TEST_UTILS_BALO = System.getProperty("observability.test.utils.balo");
    private static final String OBESERVABILITY_TEST_UTILS_JAR = System.getProperty("observability.test.utils.jar");
    private static final String BALLERINA_TOML_TEST_NATIVES_JAR_NAME = "observability-test-utils.jar";

    protected static final String SERVER_CONNECTOR_NAME = "testobserve_listener";

    protected void setupServer(String testProject, String packageName, int[] requiredPorts) throws Exception {
        final String serverHome = balServer.getServerHome();
        final Path testUtilsJar = Paths.get(OBESERVABILITY_TEST_UTILS_JAR);

        // Copy jar for Ballerina.toml reference to natives Jar
        copyFile(testUtilsJar, Paths.get(testUtilsJar.getParent().toString(),
                BALLERINA_TOML_TEST_NATIVES_JAR_NAME));

        // Copy jar for bre/libs
        copyFile(testUtilsJar, Paths.get(serverHome, "bre", "lib", testUtilsJar.getFileName().toString()));

        // Copy caches
        try (FileSystem fs = FileSystems.newFileSystem(Paths.get(OBESERVABILITY_TEST_UTILS_BALO),
                ObservabilityBaseTest.class.getClassLoader())) {
            copyDir(fs.getPath("/"), Paths.get(serverHome, "repo"));
        }

        String sourcesDir = Paths.get("src", "test", "resources", "observability", testProject).toFile()
                .getAbsolutePath();

        String configFile = Paths.get("src", "test", "resources", "observability", testProject,
                "Config.toml").toFile().getAbsolutePath();
        Map<String, String> env = new HashMap<>();
        env.put("BALCONFIGFILE", configFile);

        // Don't use 9898 port here. It is used in metrics test cases.
        servicesServerInstance = new BServerInstance(balServer);
        servicesServerInstance.startServer(sourcesDir, packageName, null, null, env, requiredPorts);
    }

    protected void cleanupServer() throws BallerinaTestException {
        servicesServerInstance.removeAllLeechers();
        servicesServerInstance.shutdownServer();
    }

    private void copyDir(Path source, Path dest) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            Path relativeSourcePath = source.relativize(sourcePath);
            try {
                Path targetPath = dest.resolve(relativeSourcePath.toString());
                if (!targetPath.toFile().isDirectory() || !targetPath.toFile().exists()) {
                    copyFile(sourcePath, targetPath);
                }
            } catch (IOException ex) {
                Assert.fail("Failed to copy file " + relativeSourcePath + " in directory " + source.toString()
                        + " to " + dest.toString(), ex);
            }
        });
    }

    private void copyFile(Path source, Path dest) throws IOException {
        dest.getParent().toFile().mkdirs();     // Create parent directory
        Files.copy(source, dest, REPLACE_EXISTING);
    }
}
