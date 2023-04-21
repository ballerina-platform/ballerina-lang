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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;

/**
 * Tool command tests.
 *
 * @since 2201.6.0
 */
public class ToolCommandTest extends BaseCommandTest {
    @Test(description = "Test tool command with the help flag")
    public void testToolCommandWithHelpFlag() throws IOException {
        String expected = getOutput("tool-help.txt");

        ToolCommand toolCommand = new ToolCommand(printStream, false);
        new CommandLine(toolCommand).parseArgs("--help");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), expected);

        toolCommand = new ToolCommand(printStream, false);
        new CommandLine(toolCommand).parseArgs("-h");
        toolCommand.execute();
        buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), expected);
    }

    @Test(description = "Test tool command with no arguments")
    public void testToolCommandWithNoArgs() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, false);
        new CommandLine(toolCommand).parseArgs();
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-with-no-args.txt"));
    }

    @Test(description = "Test tool command with invalid sub command")
    public void testToolCommandWithInvalidSubCommand() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, false);
        new CommandLine(toolCommand).parseArgs("invalid-cmd");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-with-invalid-sub-command.txt"));
    }

    @Test(description = "Test tool pull sub-command with no arguments")
    public void testToolPullSubCommandWithNoArgs() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, false);
        new CommandLine(toolCommand).parseArgs("pull");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-pull-with-no-args.txt"));
    }

    @Test(description = "Test tool pull sub-command with too many arguments")
    public void testToolPullSubCommandWithTooManyArgs() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, false);
        new CommandLine(toolCommand).parseArgs("pull", "arg1", "arg2");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-pull-with-too-many-args.txt"));
    }

    @Test(description = "Test tool pull sub-command with invalid argument format")
    public void testToolPullSubCommandWithInvalidArgFormat() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, false);
        new CommandLine(toolCommand).parseArgs("pull", "id:1.0.1:extra");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-pull-with-invalid-tool-id.txt"));
    }

    @Test(dataProvider = "invalidToolIds", description = "Test tool pull sub-command with invalid argument format")
    public void testToolPullSubCommandWithInvalidToolId(String toolId) throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, false);
        new CommandLine(toolCommand).parseArgs("pull", toolId);
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-pull-with-invalid-tool-id.txt"));
    }

    @Test(description = "Test tool pull sub-command with invalid tool version")
    public void testToolPullSubCommandWithInvalidToolVersion() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, false);
        new CommandLine(toolCommand).parseArgs("pull", "tool_id:1.1");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-pull-with-invalid-tool-version.txt"));
    }

    // TODO: add tests for search, list, uninstall
    // TODO: look if it is possible to mock central API calls and add positive test cases here

    @DataProvider(name = "invalidToolIds")
    public Object[] invalidToolIds() {
        return new String[] { "_underscore", "underscore_", "under__score", "1initialnumeric", "..", "special$char"};
    }
}
