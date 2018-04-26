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
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Testing pushing a package to central.
 */
public class PackagingPushTestCase extends IntegrationTestCase {
    private ServerInstance ballerinaClient;
    private String serverZipPath;
    private Path tempHomeDirectory;
    private Path tempProjectDirectory;
    private Path projectPath;
    private String packageName = "test";

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
                                              "IntegrationTest",
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
        String ballerinaTomlContent = "[project]\n org-name = \"IntegrationTest\"\n version = \"1.0.0\"";
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

        compressFiles(tempDir, new FileOutputStream(projectPath.resolve(generatedPackagePath)
                                                               .resolve(packageName + ".zip").toFile()));
    }

    @Test(description = "Test pushing a package to central")
    public void testPush() throws Exception {
        ballerinaClient = new ServerInstance(serverZipPath);
        String sourceRootPath = projectPath.toString();
        String[] clientArgs = {"--sourceroot", sourceRootPath, packageName};

        String msg = "IntegrationTest/" + packageName + ":1.0.0 [project repo -> central]";

        LogLeecher clientLeecher = new LogLeecher(msg);
        ballerinaClient.addLogLeecher(clientLeecher);
        ballerinaClient.runMain(clientArgs, getEnvVariables(), "push");
        clientLeecher.waitForText(5000);

        // Check if package Exists
        String stagingURL = "https://api.staging-central.ballerina.io/packages/";
        HttpResponse response = HttpClientRequest.doGet(stagingURL + "IntegrationTest/" + packageName
                                                                + "/1.0.0");
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");

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

        return variables.toArray(new String[0]);
    }

    /**
     * Generate random package name.
     *
     * @param count number of characters required
     * @return generated name
     */
    public String randomPackageName(int count) {
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
        ballerinaClient.stopServer();
        Files.walk(tempHomeDirectory)
             .sorted(Comparator.reverseOrder())
             .forEach(path -> {
                 try {
                     Files.delete(path);
                 } catch (IOException e) {
                     Assert.fail(e.getMessage(), e);
                 }
             });
        Files.walk(tempProjectDirectory)
             .sorted(Comparator.reverseOrder())
             .forEach(path -> {
                 try {
                     Files.delete(path);
                 } catch (IOException e) {
                     Assert.fail(e.getMessage(), e);
                 }
             });
    }

    /**
     * Add file inside the src directory to the ZipOutputStream.
     *
     * @param zos      ZipOutputStream
     * @param filePath file path of each file inside the driectory
     * @throws IOException exception if an error occurrs when compressing
     */
    private static void addEntry(ZipOutputStream zos, Path filePath, String fileStr) throws IOException {
        ZipEntry ze = new ZipEntry(fileStr);
        zos.putNextEntry(ze);
        Files.copy(filePath, zos);
        zos.closeEntry();
    }

    /**
     * Compresses files.
     *
     * @param outputStream outputstream
     * @throws IOException exception if an error occurrs when compressing
     */
    private static void compressFiles(Path dir, OutputStream outputStream) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(outputStream);
        if (Files.isRegularFile(dir)) {
            Path fileName = dir.getFileName();
            if (fileName != null) {
                addEntry(zos, dir, fileName.toString());
            } else {
                throw new RuntimeException("Error occurred when compressing");
            }
        } else {
            Stream<Path> list = Files.walk(dir);
            list.forEach(p -> {
                StringJoiner joiner = new StringJoiner("/");
                for (Path path : dir.relativize(p)) {
                    joiner.add(path.toString());
                }
                if (Files.isRegularFile(p)) {
                    try {
                        addEntry(zos, p, joiner.toString());
                    } catch (IOException ignore) {
                    }
                }
            });
        }
        zos.close();
    }
}
