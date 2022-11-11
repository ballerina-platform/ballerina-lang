/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.cli.cmd;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Pull command tests.
 *
 * @since 2.0.0
 */
public class PullCommandTest extends BaseCommandTest {

    private static final String TEST_PKG_NAME = "wso2/winery:1.2.3";
    private Path testResources;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Files.walkFileTree(Paths.get(testResourcesURI), new BuildCommandTest.Copy(Paths.get(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Pull package with too many args")
    public void testPullWithTooManyArgs() throws IOException {
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parseArgs(TEST_PKG_NAME, "tests");
        pullCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: too many arguments"));
        Assert.assertTrue(actual.contains("bal pull <package-name>"));
    }

    @Test(description = "Pull package with invalid package name")
    public void testPullInvalidPackage() throws IOException {
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parseArgs("wso2/winery/1.0.0");
        pullCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(
                actual.contains("ballerina: invalid package name. Provide the package name with the organization."));
        Assert.assertTrue(
                actual.contains("bal pull [<org-name>/<package-name> | <org-name>/<package-name>:<version>]"));
    }

    @Test(description = "Pull package with invalid org")
    public void testPullPackageWithInvalidOrg() throws IOException {
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parseArgs("wso2-dev/winery");
        pullCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(
                actual.contains("ballerina: invalid organization. Provide the package name with the organization."));
        Assert.assertTrue(
                actual.contains("bal pull [<org-name>/<package-name> | <org-name>/<package-name>:<version>]"));
    }

    @Test(description = "Pull package with invalid name")
    public void testPullPackageWithInvalidName() throws IOException {
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parseArgs("wso2/winery$:1.0.0");
        pullCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(
                actual.contains("ballerina: invalid package name. Provide the package name with the organization."));
        Assert.assertTrue(
                actual.contains("bal pull [<org-name>/<package-name> | <org-name>/<package-name>:<version>]"));
    }

    @Test(description = "Pull package with invalid version")
    public void testPullPackageWithInvalidVersion() throws IOException {
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parseArgs("wso2/winery:1.0.0.0");
        pullCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: invalid package version. Invalid version: '1.0.0.0'. "
                + "Unexpected character 'DOT(.)' at position '5', expecting '[HYPHEN, PLUS, EOI]'"));
        Assert.assertTrue(
                actual.contains("bal pull [<org-name>/<package-name> | <org-name>/<package-name>:<version>]"));
    }

    @Test(description = "Test pull command with argument and a help")
    public void testPullCommandArgAndHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = { "sample2", "--help" };
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parseArgs(args);
        pullCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-pull - Fetch packages from Ballerina Central"));
    }

    @Test(description = "Test pull command with help flag")
    public void testPullCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = { "-h" };
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parseArgs(args);
        pullCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-pull - Fetch packages from Ballerina Central"));
    }

    @Test(description = "Pull package dependencies without current directory being a Ballerina pkg")
    public void testPullWithoutPkgArgAndWorkingDirNotABalPkg() throws IOException {
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parseArgs();
        pullCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("working directory is not a ballerina project."));
        Assert.assertTrue(actual.contains("bal pull [<org-name>/<package-name> | " +
                "<org-name>/<package-name>:<version>]"));
    }

    @Test(description = "Pull package dependencies inside a bal pkg that does not have source files")
    public void testPullWithoutPkgArgInsideAnEmptyBalPkg() throws IOException {
        Path projectPath = this.testResources.resolve("emptyPackage");
        System.setProperty("user.dir", projectPath.toString());

        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parseArgs();
        pullCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("package is empty. Please add at least one .bal file."));
    }
}
