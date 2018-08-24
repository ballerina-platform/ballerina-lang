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

import org.awaitility.Duration;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.awaitility.Awaitility.given;
import static org.ballerinalang.test.ToolsTestUtils.deleteFiles;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Testing pushing, pulling, searching a package from central and installing package to home repository.
 */
public class PackagingTestCase {
    private ServerInstance ballerinaClient;
    private String serverZipPath;
    private Path tempHomeDirectory;
    private Path tempProjectDirectory;
    private Path projectPath;
    private String packageName = "test";
    private String datePushed;

    /**
     * Compress files.
     *
     * @param sourceDirPath source directory path to be compressed
     * @param zipFilePath   destination directory path
     * @throws IOException throw I/O exception if an issue occurs
     */
    private static void compressFiles(Path sourceDirPath, Path zipFilePath) throws IOException {
        Files.deleteIfExists(zipFilePath);
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        URI uri = URI.create("jar:" + zipFilePath.toUri());
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            Files.walk(sourceDirPath)
                 .filter(d -> !d.equals(sourceDirPath))
                 .forEach(fileToZip -> {
                     Path pathInZipFile = zipfs.getPath("/");
                     for (Path part : sourceDirPath.relativize(fileToZip)) {
                         pathInZipFile = pathInZipFile.resolve(part.toString());
                     }
                     try {
                         Files.copy(fileToZip, pathInZipFile);
                     } catch (IOException e) {
                         Assert.fail(e.getMessage(), e);
                     }
                 });
        } catch (UncheckedIOException e) {
            Assert.fail(e.getMessage(), e);
        }

    }

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-home-");
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);

        createSettingToml();

        packageName = packageName + randomPackageName(10);
        projectPath = tempProjectDirectory.resolve("myproject");
        Files.createDirectory(projectPath);

        createBallerinaToml();

        Path generatedPackagePath = Paths.get(ProjectDirConstants.DOT_BALLERINA_DIR_NAME,
                                              ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME,
                                              "integrationtests",
                                              packageName,
                                              "1.0.0");
        Files.createDirectories(projectPath.resolve(generatedPackagePath));

        createProjectArchive(generatedPackagePath);

    }

    /**
     * Create Ballerina.toml inside project.
     *
     * @throws IOException i/o exception when writing to file
     */
    private void createBallerinaToml() throws IOException {
        Path ballerinaToml = projectPath.resolve("Ballerina.toml");
        String ballerinaTomlContent = "[project]\n org-name = \"integrationtests\"\n version = \"1.0.0\"";
        Files.write(ballerinaToml, ballerinaTomlContent.getBytes(), StandardOpenOption.CREATE);
    }

    /**
     * Create Settings.toml inside the home repository.
     *
     * @throws IOException i/o exception when writing to file
     */
    private void createSettingToml() throws IOException {
        Path tomlFilePath = tempHomeDirectory.resolve("Settings.toml");
        String content = "[central]\n accesstoken = \"0f647e67-857d-32e8-a679-bd3c1c3a7eb2\"";
        Files.write(tomlFilePath, content.getBytes(), StandardOpenOption.CREATE);
    }

    /**
     * Create the content inside the zipped artifact.
     *
     * @param generatedPackagePath path of the package to place the artifact
     * @throws IOException i/o exception when manipulating files
     */
    private void createProjectArchive(Path generatedPackagePath) throws IOException {
        Path tempDir = tempProjectDirectory.resolve(packageName);

        Path src = tempDir.resolve("src").resolve(packageName);
        Files.createDirectories(src);
        Path srcFilePath = Paths.get(new File("src" + File.separator + "test" + File.separator + "resources"
                                                      + File.separator + "packaging" + File.separator +
                                                      "functions.bal").getAbsolutePath());
        Files.copy(srcFilePath, src.resolve("functions.bal"));

        Path obj = tempDir.resolve("obj");
        Files.createDirectories(obj);
        Path objFilePath = Paths.get(new File("src" + File.separator + "test" + File.separator + "resources"
                                                      + File.separator + "packaging" + File.separator +
                                                      "my.app.balo").getAbsolutePath());
        Files.copy(objFilePath, obj.resolve(packageName + ".balo"));

        Path mdDir = tempDir.resolve(packageName);
        Files.createDirectories(mdDir);
        Path mdFilePath = Paths.get(new File("src" + File.separator + "test" + File.separator + "resources"
                                                     + File.separator + "packaging" + File.separator +
                                                     "Package.md").getAbsolutePath());
        Files.copy(mdFilePath, mdDir.resolve("Package.md"));

        compressFiles(tempDir, projectPath.resolve(generatedPackagePath).resolve(packageName + ".zip"));
    }

    @Test(description = "Test pushing a package to central")
    public void testPush() throws Exception {
        ballerinaClient = new ServerInstance(serverZipPath);
        String sourceRootPath = projectPath.toString();
        String[] clientArgs = {"--sourceroot", sourceRootPath, packageName};

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-EE");
        datePushed = dtf.format(LocalDateTime.now());

        String msg = "integrationtests/" + packageName + ":1.0.0 [project repo -> central]";
        LogLeecher clientLeecher = new LogLeecher(msg);
        ballerinaClient.addLogLeecher(clientLeecher);
        ballerinaClient.runMain(clientArgs, getEnvVariables(), "push");
        clientLeecher.waitForText(5000);
    }

    @Test(description = "Test pushing a package to the home repository (installing a package)")
    public void testInstall() throws Exception {
        ballerinaClient = new ServerInstance(serverZipPath);
        String sourceRootPath = projectPath.toString();

        String[] clientArgs = {"--sourceroot", sourceRootPath, packageName};
        ballerinaClient.runMain(clientArgs, getEnvVariables(), "install");

        Path dirPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, "integrationtests",
                                 packageName, "1.0.0");
        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(dirPath)));
        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(dirPath).resolve(packageName + ".zip")));
    }

    @Test(description = "Test pulling a package from central", dependsOnMethods = "testPush")
    public void testPull() throws Exception {
        Path dirPath = Paths.get(ProjectDirConstants.CACHES_DIR_NAME,
                                 ProjectDirConstants.BALLERINA_CENTRAL_DIR_NAME,
                                 "integrationtests", packageName, "1.0.0");

        given().with().pollInterval(Duration.TEN_SECONDS).and()
               .with().pollDelay(Duration.FIVE_SECONDS)
               .await().atMost(60, SECONDS).until(() -> {
            ballerinaClient = new ServerInstance(serverZipPath);
            String[] clientArgs = {"integrationtests/" + packageName + ":1.0.0"};
            ballerinaClient.runMain(clientArgs, getEnvVariables(), "pull");
            return Files.exists(tempHomeDirectory.resolve(dirPath).resolve(packageName + ".zip"));
        });

        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(dirPath).resolve(packageName + ".zip")));
    }

    @Test(description = "Test searching a package from central", dependsOnMethods = "testPush")
    public void testSearch() throws BallerinaTestException, IOException {
        ballerinaClient = new ServerInstance(serverZipPath);
        String[] clientArgs = {packageName};
        String loggedMsg = "Ballerina Central\n" +
                "=================\n" +
                "\n" +
                "|NAME                                                  | DESCRIPTION                                " +
                "                                       | AUTHOR         | DATE           | VERSION |\n" +
                "|------------------------------------------------------| -------------------------------------------" +
                "---------------------------------------| ---------------| ---------------| --------|\n" +
                "|integrationtests/" + packageName + "                             | Allows connecting Test REST API." +
                "                                       |                | " + datePushed + " | 1.0.0   |\n";

        LogLeecher clientLeecher = new LogLeecher(loggedMsg);
        ballerinaClient.addLogLeecher(clientLeecher);
        ballerinaClient.runMain(clientArgs, getEnvVariables(), "search");
        clientLeecher.waitForText(3000);
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
        variables.add(ProjectDirConstants.HOME_REPO_ENV_KEY + "=" + tempHomeDirectory.toString());
        variables.add("BALLERINA_DEV_STAGE_CENTRAL" + "=" + "true");

        return variables.toArray(new String[variables.size()]);
    }

    /**
     * Generate random package name.
     *
     * @param count number of characters required
     * @return generated name
     */
    private String randomPackageName(int count) {
        String upperCaseAlpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseAlpha = "abcdefghijklmnopqrstuvwxyz";
        String alpha = upperCaseAlpha + lowerCaseAlpha;
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * alpha.length());
            builder.append(alpha.charAt(character));
        }
        return builder.toString();
    }

    @AfterClass
    private void cleanup() throws Exception {
        deleteFiles(tempHomeDirectory);
        deleteFiles(tempProjectDirectory);
    }
}
