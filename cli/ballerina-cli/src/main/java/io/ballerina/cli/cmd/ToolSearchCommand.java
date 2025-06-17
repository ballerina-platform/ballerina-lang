/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.cli.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Settings;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.model.Tool;
import org.ballerinalang.central.client.model.ToolSearchResult;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;
import static io.ballerina.cli.cmd.Constants.TOOL_SEARCH_COMMAND;
import static io.ballerina.cli.utils.PrintUtils.printTools;
import static io.ballerina.projects.util.ProjectConstants.EMPTY_STRING;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.HYPHEN;

/**
 * Command to search for tools in Ballerina Central.
 *
 * @since 2201.13.0
 */
@CommandLine.Command(name = TOOL_SEARCH_COMMAND, description = "Search the Ballerina Central for tools.")
public class ToolSearchCommand implements BLauncherCmd {
    private final boolean exitWhenFinish;
    private final PrintStream outStream;
    private final PrintStream errStream;

    private static final String TOOL_SEARCH_USAGE_TEXT = "bal tool search [<tool-id>|<text>]";

    public ToolSearchCommand() {
        this.exitWhenFinish = true;
        this.outStream = System.out;
        this.errStream = System.err;
    }

    @CommandLine.Parameters(description = "Use a tool")
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    public ToolSearchCommand(PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            outStream.println(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_SEARCH_COMMAND));
            return;
        }
        if (argList == null || argList.isEmpty()) {
            CommandUtil.printError(this.errStream, "no keyword given.", TOOL_SEARCH_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (argList.size() > 1) {
            CommandUtil.printError(
                    this.errStream, "too many arguments.", TOOL_SEARCH_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String searchArgs = argList.get(0);
        searchToolsInCentral(searchArgs);
    }

    @Override
    public String getName() {
        return TOOL_SEARCH_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_SEARCH_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append(TOOL_SEARCH_USAGE_TEXT);
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {

    }

    /**
     * Search for tools in central.
     *
     * @param keyword search keyword.
     */
    private void searchToolsInCentral(String keyword) {
        try {
            Settings settings;
            settings = RepoUtils.readSettings();
            // Ignore Settings.toml diagnostics in the search command

            CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                    initializeProxy(settings.getProxy()), settings.getProxy().username(),
                    settings.getProxy().password(), getAccessTokenOfCLI(settings),
                    settings.getCentral().getConnectTimeout(),
                    settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                    settings.getCentral().getCallTimeout(), settings.getCentral().getMaxRetries());
            boolean foundTools = false;
            String supportedPlatform = Arrays.stream(JvmTarget.values())
                    .map(JvmTarget::code)
                    .collect(Collectors.joining(","));
            ToolSearchResult toolSearchResult = client.searchTool(keyword, supportedPlatform,
                    RepoUtils.getBallerinaVersion());
            List<Tool> tools = toolSearchResult.getTools();
            if (tools != null && !tools.isEmpty()) {
                foundTools = true;
                printTools(toolSearchResult.getTools(), RepoUtils.getTerminalWidth());
            }
            if (!foundTools) {
                outStream.println("no tools found.");
            }
        } catch (CentralClientException e) {
            String errorMessage = e.getMessage();
            if (null != errorMessage && !EMPTY_STRING.equals(errorMessage.trim())) {
                // removing the error stack
                if (errorMessage.contains("\n\tat")) {
                    errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n\tat"));
                }
                CommandUtil.printError(this.errStream, errorMessage, null, false);
                CommandUtil.exitError(this.exitWhenFinish);
            } else {
                CommandUtil.printError(this.errStream, "error while searching for tools.", null, false);
                CommandUtil.exitError(this.exitWhenFinish);
            }
        }
    }
}
