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

import org.ballerinalang.packerina.init.InitHandler;
import org.ballerinalang.packerina.init.models.FileType;
import org.ballerinalang.packerina.init.models.PackageMdFile;
import org.ballerinalang.packerina.init.models.SrcFile;
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.toml.model.Manifest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Testing init command.
 */
public class PackagingInitTestCase extends IntegrationTestCase {
    private ServerInstance ballerinaServerForMain;
    private ServerInstance ballerinaServerForService;
    private String serverZipPath;
    private LogLeecher logLeecher;
    private Path tempProjectDirectory;
    private Manifest manifest;
    private List<SrcFile> sourceFiles;
    private List<PackageMdFile> packageDescFiles;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        // Set values to manifest
        manifest = new Manifest();
        manifest.setName("integrationtests");
        manifest.setVersion("0.0.0");
    }

    @Test(description = "Test creating a project with a main in a package")
    public void testInitWithMainInPackage() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("firstTestWithPackagesMain");
        Files.createDirectories(projectPath);

        sourceFiles = new ArrayList<>();
        packageDescFiles = new ArrayList<>();

        sourceFiles.add(new SrcFile("foo", FileType.MAIN));
        packageDescFiles.add(new PackageMdFile("foo", FileType.MAIN));

        InitHandler.initialize(projectPath, manifest, sourceFiles, packageDescFiles);

        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("tests").resolve("main_test.bal")));

        ballerinaServerForMain = new ServerInstance(serverZipPath);
        String[] clientArgs = {"--sourceroot", projectPath.toString(), "foo"};
        logLeecher = new LogLeecher("Hello World!");
        ballerinaServerForMain.addLogLeecher(logLeecher);
        ballerinaServerForMain.runMain(clientArgs, getEnvVariables(), "run");
    }

    @Test(description = "Test creating a project with a service in a package")
    public void testInitWithServiceInPackage() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("firstTestWithPackagesService");
        Files.createDirectories(projectPath);

        sourceFiles = new ArrayList<>();
        packageDescFiles = new ArrayList<>();

        sourceFiles.add(new SrcFile("foo", FileType.SERVICE));
        packageDescFiles.add(new PackageMdFile("foo", FileType.SERVICE));

        InitHandler.initialize(projectPath, manifest, sourceFiles, packageDescFiles);

        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("hello_service.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("tests").resolve("hello_service_test.bal")));

        ballerinaServerForService = ServerInstance.initBallerinaServer();
        ballerinaServerForService.startBallerinaServer(projectPath.resolve("foo").resolve("hello_service.bal")
                                                                  .toString());
        HttpResponse response = HttpClientRequest.doGet(ballerinaServerForService.getServiceURLHttp
                ("hello/sayHello"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        ballerinaServerForService.stopServer();
    }

    @Test(description = "Test creating a project with a service and main in different packages")
    public void testInitWithMainServiceInDiffPackage() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("secondTestWithPackages");
        Files.createDirectories(projectPath);

        sourceFiles = new ArrayList<>();
        packageDescFiles = new ArrayList<>();

        sourceFiles.add(new SrcFile("foo", FileType.MAIN));
        packageDescFiles.add(new PackageMdFile("foo", FileType.MAIN));

        sourceFiles.add(new SrcFile("bar", FileType.SERVICE));
        packageDescFiles.add(new PackageMdFile("bar", FileType.SERVICE));

        InitHandler.initialize(projectPath, manifest, sourceFiles, packageDescFiles);

        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("bar").resolve("hello_service.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("tests").resolve("main_test.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("bar").resolve("tests").resolve("hello_service_test.bal")));
        // Test for main
        ballerinaServerForMain = new ServerInstance(serverZipPath);
        String[] clientArgs = {"--sourceroot", projectPath.toString(), "foo"};
        logLeecher = new LogLeecher("Hello World!");
        ballerinaServerForMain.addLogLeecher(logLeecher);
        ballerinaServerForMain.runMain(clientArgs, getEnvVariables(), "run");

        // Test for service
        ballerinaServerForService = ServerInstance.initBallerinaServer();
        ballerinaServerForService.startBallerinaServer(projectPath.resolve("bar").resolve("hello_service.bal")
                                                                  .toString());
        HttpResponse response = HttpClientRequest.doGet(ballerinaServerForService.getServiceURLHttp("hello/sayHello"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        ballerinaServerForService.stopServer();
    }

    @Test(description = "Test creating a project without going to interactive mode")
    public void testInitWithoutGoingToInteractiveMode() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("testWithoutPackage");
        Files.createDirectories(projectPath);

        sourceFiles = new ArrayList<>();
        sourceFiles.add(new SrcFile("", FileType.SERVICE));
        
        packageDescFiles = new ArrayList<>();

        InitHandler.initialize(projectPath, manifest, sourceFiles, packageDescFiles);

        Assert.assertTrue(Files.exists(projectPath.resolve("hello_service.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina")));

        // Test for service
        ballerinaServerForService = ServerInstance.initBallerinaServer();
        ballerinaServerForService.startBallerinaServer(projectPath.resolve("hello_service.bal").toString());
        HttpResponse response = HttpClientRequest.doGet(ballerinaServerForService.getServiceURLHttp("hello/sayHello"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        ballerinaServerForService.stopServer();
    }

    @Test(description = "Test running init without doing any changes on an already existing project",
            dependsOnMethods = "testInitWithMainInPackage")
    public void testInitOnExistingProject() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("firstTestWithPackagesMain");
        InitHandler.initialize(projectPath, manifest, new ArrayList<>(), new ArrayList<>());

        Path packagePath = projectPath.resolve("foo");
        Assert.assertTrue(Files.exists(packagePath.resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(packagePath.resolve("tests").resolve("main_test.bal")));

        ballerinaServerForMain = new ServerInstance(serverZipPath);
        String[] clientArgs = {"--sourceroot", projectPath.toString(), "foo"};
        logLeecher = new LogLeecher("Hello World!");
        ballerinaServerForMain.addLogLeecher(logLeecher);
        ballerinaServerForMain.runMain(clientArgs, getEnvVariables(), "run");
    }

    @Test(description = "Test running init on an already existing project and create a new package",
            dependsOnMethods = "testInitWithMainInPackage")
    public void testInitOnExistingProjectWithNewPackage() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("firstTestWithPackagesMain");

        sourceFiles = new ArrayList<>();
        packageDescFiles = new ArrayList<>();

        sourceFiles.add(new SrcFile("newpkg", FileType.MAIN));
        packageDescFiles.add(new PackageMdFile("newpkg", FileType.MAIN));

        InitHandler.initialize(projectPath, manifest, sourceFiles, packageDescFiles);

        Assert.assertTrue(Files.exists(projectPath.resolve("newpkg").resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("newpkg").resolve("tests").resolve("main_test.bal")));

        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("tests").resolve("main_test.bal")));

        ballerinaServerForMain = new ServerInstance(serverZipPath);
        String[] clientArgs = {"--sourceroot", projectPath.toString(), "newpkg"};
        logLeecher = new LogLeecher("Hello World!");
        ballerinaServerForMain.addLogLeecher(logLeecher);
        ballerinaServerForMain.runMain(clientArgs, getEnvVariables(), "run");
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
        return variables.toArray(new String[variables.size()]);
    }

    @AfterClass
    private void cleanup() throws Exception {
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
}
