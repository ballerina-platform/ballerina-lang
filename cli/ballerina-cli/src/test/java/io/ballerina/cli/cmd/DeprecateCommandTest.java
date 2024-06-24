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
 * @since 2201.5.0
 */
public class DeprecateCommandTest extends BaseCommandTest {

    @Test(description = "Test deprecate with invalid org name")
    public void testDeprecationWithInvalidOrg() throws IOException {
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs("my-newdil/deppack:1.0.1", "--message", "for testing");
        deprecationCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replace("\r", "");
        Assert.assertTrue(actual.contains("ballerina: invalid organization name. Organization name can only contain " +
                "alphanumeric values with lower case letters and cannot start with numeric values, " +
                "org_name = my-newdil"));
    }

    @Test(description = "Test deprecate with invalid package name")
    public void testDeprecationWithInvalidPackageName() throws IOException {
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs("mynewdil/dep-pack:0.1.0");
        deprecationCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replace("\r", "");
        Assert.assertTrue(actual.contains("ballerina: invalid package name provided"));
    }

    @Test(description = "Test deprecate with invalid version")
    public void testDeprecationWithInvalidVersion() throws IOException {
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs("mynewdil/deppack:xxx");
        deprecationCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replace("\r", "");
        Assert.assertTrue(actual.contains(
                "ballerina: error: could not connect to remote repository to find package: mynewdil/deppack:xxx. " +
                "reason: package not found: mynewdil/deppack:xxx_java17"),
                actual);
    }

    @Test(description = "Test deprecate with invalid message")
    public void testDeprecationWithInvalidMessage() throws IOException {
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs("mynewdil/deppack:xxx", "--message", "this is a test message \n" +
                "with multiple lines");
        deprecationCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replace("\r", "");
        Assert.assertTrue(actual.contains("ballerina: invalid deprecation message. " +
                "The message cannot contain non-space whitespace or back slash characters."));
        Assert.assertTrue(actual.contains("bal deprecate {<org-name>/<package-name>:<version>} [OPTIONS]"));
    }

    @Test(description = "Test deprecate with unused flags")
    public void testDeprecationWithUnusedFlags() throws IOException {
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs("mynewdil/deppack:0.1.0", "--undo", "--message", "for testing");
        deprecationCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replace("\r", "");
        Assert.assertTrue(actual.contains("warning: ignoring --message flag since this is an undo request"));
    }

    @Test(description = "Test deprecate with too many args")
    public void testDeprecationWithTooManyArgs() throws IOException {
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs("wso2", "tests");
        deprecationCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replace("\r", "");
        Assert.assertTrue(actual.contains("ballerina: too many arguments"));
        Assert.assertTrue(actual.contains("bal deprecate <package>"));
    }

    @Test(description = "Test deprecate command with argument and a help")
    public void testDeprecateCommandArgAndHelp() throws IOException {
        String[] args = { "sample2", "--help" };
        DeprecateCommand deprecationCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecationCommand).parseArgs(args);
        deprecationCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-deprecate - Deprecates a published package"));
    }

    @Test(description = "Test deprecate command with help flag")
    public void testDeprecateCommandWithHelp() throws IOException {
        String[] args = { "-h" };
        DeprecateCommand deprecateCommand = new DeprecateCommand(printStream, printStream, false);
        new CommandLine(deprecateCommand).parseArgs(args);
        deprecateCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-deprecate - Deprecates a published package"));
    }
}
