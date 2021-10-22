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
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.utils.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Testing initializing a project using the init command.
 *
 * @since 0.982.0
 */
public class ModuleInitTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempProjectDirectory;
    private String moduleName = "test";
    private String orgName = "bcintegrationtest";
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws IOException {
        tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-home-");
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        TestUtils.createSettingToml(tempHomeDirectory);
        moduleName = moduleName + TestUtils.randomModuleName(10);
        envVariables = TestUtils.getEnvVariables();
    }

    @Test(description = "Test creating a project with a main in a module")
    public void testInitWithMainInModule() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("firstTestWithModulesMain");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", "foo\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[]{},
                projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("tests").resolve("main_test.bal")));

        // Test bal build
        balClient.runMain("build", new String[] {"--with-tests"}, envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        Path generatedBalx = projectPath.resolve("target").resolve("foo.balx");
        Assert.assertTrue(Files.exists(generatedBalx));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                .resolve("foo").resolve("0.0.1").resolve("foo.zip")));

        // Test ballerina run
        runMainFunction(projectPath, "foo");

        // Test ballerina run with balx
        runMainFunction(projectPath, generatedBalx.toString());
    }

    @Test(description = "Test creating a project with a service in a module")
    public void testInitWithServiceInModule() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("firstTestWithModulesService");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "s\n", "foo\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[]{},
                projectPath.toString());

        Path serviceBalPath = projectPath.resolve("foo").resolve("hello_service.bal");
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(serviceBalPath));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("tests").resolve("hello_service_test.bal")));

        // Test bal build
        balClient.runMain("build", new String[] {"--with-tests"}, envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        Path generatedBalx = projectPath.resolve("target").resolve("foo.balx");
        Assert.assertTrue(Files.exists(generatedBalx));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                .resolve("foo").resolve("0.0.1").resolve("foo.zip")));

        // Test ballerina run
        runService(projectPath.resolve("foo").resolve("hello_service.bal"));

        // Test ballerina run with balx
        runService(generatedBalx);
    }

    @Test(description = "Test creating a project with a service and main in different modules")
    public void testInitWithMainServiceInDiffmodule() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("secondTestWithmodules");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", "foo\n", "s\n", "bar\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[]{},
                projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("main.bal")));
        Path serviceBalPath = projectPath.resolve("bar").resolve("hello_service.bal");
        Assert.assertTrue(Files.exists(serviceBalPath));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("tests").resolve("main_test.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("bar").resolve("tests").resolve("hello_service_test.bal")));

        // Test bal build
        balClient.runMain("build", new String[] {"--with-tests"}, envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("foo.balx")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("bar.balx")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                .resolve("foo").resolve("0.0.1").resolve("foo.zip")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                .resolve("bar").resolve("0.0.1").resolve("bar.zip")));
        // Test ballerina run
        runMainFunction(projectPath, "foo");
        runService(serviceBalPath);

        // Test ballerina run with balx
        runMainFunction(projectPath, projectPath.resolve("target").resolve("foo.balx").toString());
        runService(projectPath.resolve("target").resolve("bar.balx"));
    }

    @Test(description = "Test creating a project without going to interactive mode")
    public void testInitWithoutGoingToInteractiveMode() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("testWithoutmodule");
        Files.createDirectories(projectPath);

        balClient.runMain("init", new String[0], envVariables, new String[0], new LogLeecher[]{},
                projectPath.toString());

        Path serviceBalPath = projectPath.resolve("hello_service.bal");
        Assert.assertTrue(Files.exists(serviceBalPath));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina")));

        balClient.runMain("build", new String[0], envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        Path generatedBalx = projectPath.resolve("target").resolve("hello_service.balx");
        Assert.assertTrue(Files.exists(generatedBalx));

        // Test ballerina run
        runService(serviceBalPath);

        // Test ballerina run with balx
        runService(generatedBalx);
    }

    @Test(description = "Test creating a project with a main without a module")
    public void testInitWithoutmodule() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("testWithoutmoduleForMain");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", "\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[]{},
                projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));

        // Test bal build
        balClient.runMain("build", new String[0], envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        Path generatedBalx = projectPath.resolve("target").resolve("main.balx");
        Assert.assertTrue(Files.exists(generatedBalx));

        // Test ballerina run
        runMainFunction(projectPath, projectPath.resolve("main.bal").toString());

        // Test ballerina run with balx
        runMainFunction(projectPath, projectPath.resolve("target").resolve("main.balx").toString());
    }

    @Test(description = "Test running init without doing any changes on an already existing project",
            dependsOnMethods = "testInitWithMainInModule")
    public void testInitOnExistingProject() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("firstTestWithModulesMain");
        balClient.runMain("init", new String[0], envVariables, new String[0], new LogLeecher[]{},
                projectPath.toString());

        Path modulePath = projectPath.resolve("foo");
        Assert.assertTrue(Files.exists(modulePath.resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(modulePath.resolve("tests").resolve("main_test.bal")));

        // Test bal build
        balClient.runMain("build", new String[] {"--with-tests"}, envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("foo.balx")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                .resolve("foo").resolve("0.0.1").resolve("foo.zip")));
        // Test ballerina run
        runMainFunction(projectPath, "foo");

        // Test ballerina run with balx
        runMainFunction(projectPath, projectPath.resolve("target").resolve("foo.balx").toString());
    }

    @Test(description = "Test running init on an already existing project and create a new module",
            dependsOnMethods = "testInitWithMainInModule")
    public void testInitOnExistingProjectWithNewModule() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("firstTestWithModulesMain");

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", "m\n", "newpkg\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[]{},
                projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve("newpkg").resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("newpkg").resolve("tests").resolve("main_test.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("tests").resolve("main_test.bal")));

        // Test bal build
        balClient.runMain("build", new String[0], envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("newpkg.balx")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("foo.balx")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                .resolve("foo").resolve("0.0.1").resolve("foo.zip")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                .resolve("newpkg").resolve("0.0.1").resolve("newpkg.zip")));


        // Test ballerina run on the new module
        runMainFunction(projectPath, "newpkg");

        // Test ballerina run with balx
        runMainFunction(projectPath, projectPath.resolve("target").resolve("newpkg.balx").toString());
    }

    @Test(description = "Test running init on an already existing project and create a new module with the same name " +
            "as existing module", dependsOnMethods = "testInitOnExistingProjectWithNewModule")
    public void testInitOnExistingProjectWithNewModuleWithSameName() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("firstTestWithModulesMain");

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", "m\n", "newpkg\n", "foo\n", "anotherpkg\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[]{},
                projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve("anotherpkg").resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("anotherpkg").resolve("tests").resolve("main_test.bal")));

        // Test bal build
        balClient.runMain("build", new String[]{"anotherpkg"}, envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("anotherpkg.balx")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                .resolve("anotherpkg").resolve("0.0.1").resolve("anotherpkg.zip")));


        // Test ballerina run on the new module
        runMainFunction(projectPath, "anotherpkg");

        // Test ballerina run with balx
        runMainFunction(projectPath, projectPath.resolve("target").resolve("anotherpkg.balx").toString());
    }

    @Test(description = "Test running init on an already existing module",
            dependsOnMethods = "testInitWithMainInModule")
    public void testInitInsideAModule() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("firstTestWithModulesMain").resolve("foo")
                                               .resolve("tests").resolve("newProj");

        Files.createDirectories(projectPath);
        String[] clientArgsForInit = {"-i"};
        LogLeecher leecher = new LogLeecher("Directory is already within a ballerina project :" +
                tempProjectDirectory.resolve("firstTestWithModulesMain"), LogLeecher.LeecherType.ERROR);
        balClient.runMain("init", clientArgsForInit, envVariables, new String[0], new LogLeecher[]{leecher},
                projectPath.toString());
    }

    @Test(description = "Test running init on an already existing module",
            dependsOnMethods = "testInitWithMainInModule")
    public void testInitOutsideAProject() throws Exception {
        // Test ballerina init
        String[] clientArgsForInit = {"-i"};
        LogLeecher leecher = new LogLeecher("A ballerina project is already initialized in " +
                tempProjectDirectory, LogLeecher.LeecherType.ERROR);
        balClient.runMain("init", clientArgsForInit, envVariables, new String[0], new LogLeecher[]{leecher},
                tempProjectDirectory.toString());
    }

    @Test(description = "Test creating a project with invalid options")
    public void testInitWithInvalidOptions() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("testsWithoutmodule");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "123\n", "jkl\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[]{},
                projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
    }

    @Test(description = "Test creating a project with invalid module")
    public void testInitWithInvalidModule() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("invalidTestWithmodule");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", "foo-bar\n", "foo bar module\n", "foo$bar\n",
                "foobar\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[]{},
                projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve("foobar").resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foobar").resolve("tests").resolve("main_test.bal")));

        // Test bal build
        balClient.runMain("build", new String[0], envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        Path generatedBalx = projectPath.resolve("target").resolve("foobar.balx");
        Assert.assertTrue(Files.exists(generatedBalx));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                .resolve("foobar").resolve("0.0.1").resolve("foobar.zip")));

        // Test ballerina run
        runMainFunction(projectPath, "foobar");

        // Test ballerina run with balx
        runMainFunction(projectPath, generatedBalx.toString());
    }

    @Test(description = "Test building a module in a project")
    public void testBuildmodule() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("testBuildmodule");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", "foo\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[]{},
                projectPath.toString());

        // Test bal build
        balClient.runMain("build", new String[]{"foo"}, envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("foo.balx")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve("bcintegrationtest")
                .resolve("foo").resolve("0.0.1").resolve("foo.zip")));
    }

    @Test(description = "Test creating a project in the interactive mode by giving enter as the option")
    public void testInitPrjctWithoutOpts() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("projectWithoutOpts");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", "\n", "\n", "\n", "\n", "\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[]{},
                          projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("hello_service.bal")));
    }

    @Test(description = "Test creating a project with 'ballerina' and 'ballerinax' as the org-name")
    public void testInitWithInvalidOrg() throws Exception {
        Path projectWithBallerinaAsOrg = tempProjectDirectory.resolve("testsWithBallerinaAsOrg");
        Files.createDirectories(projectWithBallerinaAsOrg);

        String[] clientArgsForInit = {"-i"};
        String[] optionsWithBallerina = {"\n", "ballerina\n", "\n", "\n", "f\n"};
        LogLeecher leecherForBallerina = new LogLeecher("--Invalid organization name: 'ballerina'. 'ballerina' " +
                "and 'ballerinax' are reserved organization names that are used by Ballerina");
        balClient.runMain("init", clientArgsForInit, envVariables, optionsWithBallerina,
                new LogLeecher[]{leecherForBallerina}, projectWithBallerinaAsOrg.toString());
        leecherForBallerina.waitForText(3000);

        Path projectWithBallerinaXAsOrg = tempProjectDirectory.resolve("testsWithBallerinaXAsOrg");
        Files.createDirectories(projectWithBallerinaXAsOrg);

        String[] optionsWithBallerinaX = {"\n", "ballerinax\n", "\n", "\n", "f\n"};
        LogLeecher leecherForBallerinaX = new LogLeecher("--Invalid organization name: 'ballerinax'. 'ballerina'" +
                " and 'ballerinax' are reserved organization names that are used by Ballerina");
        balClient.runMain("init", clientArgsForInit, envVariables, optionsWithBallerinaX,
                new LogLeecher[]{leecherForBallerinaX}, projectWithBallerinaXAsOrg.toString());
        leecherForBallerinaX.waitForText(3000);
    }

    /**
     * Run and test main function in project.
     *
     * @param projectPath path of the project
     * @param pkg         module name or balx file path
     * @throws BallerinaTestException
     */
    private void runMainFunction(Path projectPath, String pkg)
            throws BallerinaTestException {
        LogLeecher logLeecher = new LogLeecher("Hello World!");
        balClient.runMain(projectPath.toString(), pkg, new String[]{}, new String[]{},
                envVariables, new String[]{}, new LogLeecher[]{logLeecher});
    }

    /**
     * Run and test service in project.
     *
     * @param serviceBalPath path of the service bal file
     * @throws BallerinaTestException on test error
     * @throws IOException on io error
     */
    private void runService(Path serviceBalPath) throws BallerinaTestException, IOException {
        BServerInstance ballerinaServerForService = new BServerInstance(balServer);
        ballerinaServerForService.startServer(serviceBalPath.toString(), true);
        HttpResponse response = HttpClientRequest.doGet(ballerinaServerForService
                .getServiceURLHttp(9090, "hello/sayHello"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        ballerinaServerForService.shutdownServer();
    }

    @AfterClass
    private void cleanup() throws Exception {
        TestUtils.deleteFiles(tempHomeDirectory);
        TestUtils.deleteFiles(tempProjectDirectory);
    }
}
