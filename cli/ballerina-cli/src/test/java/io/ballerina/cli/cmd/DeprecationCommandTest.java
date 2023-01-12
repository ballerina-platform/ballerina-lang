/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;

/**
 * Deprecate command tests.
 *
 * @since 2201.4.0
 */
public class DeprecationCommandTest extends BaseCommandTest {

    @Test(description = "Test deprecate without package version")
    public void testDeprecationWithoutVersion() throws IOException {
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs("mynewdil/deppack", "-m", "for testing");
        deprecationCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: invalid package. Provide the package with the version."));
        Assert.assertTrue(actual.contains("bal deprecate {<org-name>/<package-name>:<version>} [OPTIONS]"));
    }

    @Test(description = "Test deprecate with invalid org name")
    public void testDeprecationWithInvalidOrg() throws IOException {
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs("my-newdil/deppack:0.1.0", "-m", "for testing");
        deprecationCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: invalid package organization. Only alphanumerics and " +
                "underscores are allowed in organization name. Provide a valid package organization."));
        Assert.assertTrue(actual.contains("bal deprecate {<org-name>/<package-name>:<version>} [OPTIONS]"));
    }

    @Test(description = "Test deprecate with invalid package name")
    public void testDeprecationWithInvalidPackageName() throws IOException {
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs("mynewdil/dep-pack:0.1.0", "-m", "for testing");
        deprecationCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: invalid package name. Only alphanumerics, underscores and " +
                "periods are allowed in a package name and the maximum length is 256 characters. " +
                "Provide a valid package name."));
        Assert.assertTrue(actual.contains("bal deprecate {<org-name>/<package-name>:<version>} [OPTIONS]"));
    }

    @Test(description = "Test deprecate with invalid version")
    public void testDeprecationWithInvalidVersion() throws IOException {
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs("mynewdil/deppack:xxx", "-m", "for testing");
        deprecationCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: invalid package. Provide a valid package version."));
        Assert.assertTrue(actual.contains("bal deprecate {<org-name>/<package-name>:<version>} [OPTIONS]"));
    }

    @Test(description = "Test deprecate with unused flags")
    public void testDeprecationWithUnusedFlags() throws IOException {
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs("mynewdil/deppack:0.1.0", "--undo", "-m", "for testing");
        deprecationCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("warning: ignoring --message (-m) flag since this is an undo request"));
    }

    @Test(description = "Test deprecate with too many args")
    public void testDeprecationWithTooManyArgs() throws IOException {
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs("wso2", "tests");
        deprecationCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: too many arguments"));
        Assert.assertTrue(actual.contains("bal deprecate <package>"));
    }

    @Test(description = "Test deprecate command with argument and a help")
    public void testSearchCommandArgAndHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = { "sample2", "--help" };
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs(args);
        deprecationCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-deprecate - Deprecates a published package"));
    }

    @Test(description = "Test deprecate command with help flag")
    public void testSearchCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = { "-h" };
        DeprecateCommand deprecateCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecateCommand).parseArgs(args);
        deprecateCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-deprecate - Deprecates a published package"));
    }
}
