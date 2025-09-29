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
import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;
import static io.ballerina.cli.cmd.Constants.TOOL_PULL_COMMAND;
import static io.ballerina.cli.utils.ToolUtils.addToBalToolsToml;
import static io.ballerina.cli.utils.ToolUtils.getToolFromLocalRepo;
import static io.ballerina.cli.utils.ToolUtils.getToolAvailableLocally;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.LOCAL_REPOSITORY_NAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.projects.util.ProjectUtils.validateToolName;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.HYPHEN;

/**
 * Command to pull a tool from the central or local repository.
 *
 * @since 2201.13.0
 */
@CommandLine.Command(name = TOOL_PULL_COMMAND, description = "Pull the specified tool")
public class ToolPullCommand implements BLauncherCmd {
    public static final String EMPTY_STRING = "";
    private final boolean exitWhenFinish;
    private final PrintStream outStream;
    private final PrintStream errStream;

    private String toolId;
    private String version;

    private static final String TOOL_PULL_USAGE_TEXT = "bal tool pull <tool-id>";

    @CommandLine.Parameters(description = "Pull a tool")
    private List<String> argList;

    @CommandLine.Option(names = "--repository")
    private String repositoryName;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    public ToolPullCommand() {
        this.exitWhenFinish = true;
        this.outStream = System.out;
        this.errStream = System.err;
    }

    public ToolPullCommand(PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            outStream.println(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_PULL_COMMAND));
            return;
        }
        if (argList == null || argList.isEmpty()) {
            CommandUtil.printError(this.errStream, "tool id is not provided.", TOOL_PULL_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (argList.size() > 1) {
            CommandUtil.printError(
                    this.errStream, "too many arguments.", TOOL_PULL_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (repositoryName != null && !repositoryName.equals(LOCAL_REPOSITORY_NAME)) {
            String errMsg = "unsupported repository '" + repositoryName + "' found. Only '"
                    + LOCAL_REPOSITORY_NAME + "' repository is supported.";
            CommandUtil.printError(this.errStream, errMsg, null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String toolIdAndVersion = argList.get(0);
        String[] toolInfo = toolIdAndVersion.split(":");
        if (toolInfo.length == 2) {
            if (repositoryName == null) {
                outStream.println("WARNING: Specifying a version of the tool is deprecated and may be removed " +
                        "in a future version. Use 'bal tool pull <tool-id>' to pull the latest version of the tool.");
            }
            toolId = toolInfo[0];
            version = toolInfo[1];
        } else if (toolInfo.length == 1) {
            toolId = toolIdAndVersion;
            version = EMPTY_STRING;
        } else {
            CommandUtil.printError(errStream, "invalid tool id.", TOOL_PULL_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (LOCAL_REPOSITORY_NAME.equals(repositoryName) && "".equals(version)) {
            CommandUtil.printError(errStream, "tool version should be provided when pulling a tool from local " +
                    "repository", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (!validateToolName(toolId)) {
            CommandUtil.printError(errStream, "invalid tool id.", TOOL_PULL_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (!Names.EMPTY.getValue().equals(version)) {
            try {
                SemanticVersion.from(version);
            } catch (ProjectException e) {
                CommandUtil.printError(errStream, "invalid tool version. " + e.getMessage(), TOOL_PULL_USAGE_TEXT,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        pullToolAndUpdateBalToolsToml(toolId, version);
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_PULL_COMMAND);
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("bal tool pull <tool-id>[:<version>]");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {

    }

    public void pullToolAndUpdateBalToolsToml(String toolIdArg, String version) {
        toolId = toolIdArg;
        this.version = version;
        Path balaCacheDirPath = RepoUtils.createAndGetHomeReposPath()
                .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME);

        String supportedPlatform = Arrays.stream(JvmTarget.values())
                .map(JvmTarget::code)
                .collect(Collectors.joining(","));
        if (LOCAL_REPOSITORY_NAME.equals(repositoryName)) {
            Optional<BalToolsManifest.Tool> toolFromLocalRepo = getToolFromLocalRepo(toolId, version);
            if (toolFromLocalRepo.isEmpty()) {
                errStream.println("tool '" + toolId + ":" + version + "' is not available in local repository." +
                        "\nUse 'bal push --repository=local' to publish it.");
                CommandUtil.exitError(this.exitWhenFinish);
            }

            addToBalToolsToml(toolFromLocalRepo.orElseThrow(), errStream);
            outStream.println("tool '" + toolId + ":" + version + "' successfully set as the active version.");
            return;
        }
        try {
            Optional<BalToolsManifest.Tool> toolAvailableLocally =
                    getToolAvailableLocally(toolId, version, repositoryName);
            if (toolAvailableLocally.isPresent()) {
                outStream.println("tool '" + toolId + ":" + version + "' is already available locally.");
            } else {
                BalToolsManifest.Tool toolFromCentral = pullToolFromCentral(supportedPlatform, balaCacheDirPath);
                if (addToBalToolsToml(toolFromCentral, errStream)) {
                    CommandUtil.exitError(this.exitWhenFinish);
                }
            }
        } catch (PackageAlreadyExistsException e) {
            errStream.println(e.getMessage());
            CommandUtil.exitError(this.exitWhenFinish);
        } catch (CentralClientException | ProjectException e) {
            CommandUtil.printError(errStream, "unexpected error occurred while pulling tool:" + e.getMessage(),
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
        }
    }

    private BalToolsManifest.Tool pullToolFromCentral(String supportedPlatform, Path balaCacheDirPath)
            throws CentralClientException {
        Settings settings;
        settings = RepoUtils.readSettings();
        // Ignore Settings.toml diagnostics in the pull command

        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");
        CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings),
                settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout(), settings.getCentral().getMaxRetries());
        String[] toolInfo = client.pullTool(toolId, version, balaCacheDirPath, supportedPlatform,
                RepoUtils.getBallerinaVersion(), false);
        boolean isPulled = Boolean.parseBoolean(toolInfo[0]);
        String org = toolInfo[0];
        String name = toolInfo[1];
        version = toolInfo[2];

        if (isPulled) {
            outStream.println("tool '" + toolId + ":" + version + "' pulled successfully.");
        }
        return new BalToolsManifest.Tool(toolId, org, name, version, true, null);
    }
}
