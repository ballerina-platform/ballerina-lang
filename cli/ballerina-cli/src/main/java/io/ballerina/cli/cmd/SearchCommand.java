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

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Settings;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.model.PackageSearchResult;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.cli.cmd.Constants.SEARCH_COMMAND;
import static io.ballerina.cli.utils.PrintUtils.printPackages;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;

/**
 * This class represents the "bal search" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = SEARCH_COMMAND, description = "Search Ballerina Central for packages")
public class SearchCommand implements BLauncherCmd {

    private PrintStream outStream;
    private PrintStream errStream;
    private boolean exitWhenFinish;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    public SearchCommand() {
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    public SearchCommand(PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(SEARCH_COMMAND);
            outStream.println(commandUsageInfo);
            return;
        }

        if (null != debugPort) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }

        if (argList == null || argList.isEmpty()) {
            CommandUtil.printError(this.errStream, "no keyword given", "bal search [<org>|<package>|<text>] ",
                                   false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (argList.size() > 1) {
            CommandUtil.printError(this.errStream, "too many arguments", "bal search [<org>|<package>|<text>] ",
                                   false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String searchArgs = argList.get(0);
        searchInCentral(searchArgs);

        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    @Override
    public String getName() {
        return SEARCH_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(SEARCH_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append(" bal search [<org>|<package>|<text>] \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    /**
     * Search for packages in central.
     *
     * @param query search keyword.
     */
    private void searchInCentral(String query) {
        try {
            Settings settings;
            try {
                settings = RepoUtils.readSettings();
                // Ignore Settings.toml diagnostics in the search command
            } catch (SettingsTomlException e) {
                // Ignore 'Settings.toml' parsing errors and return empty Settings object
                settings = Settings.from();
            }
            CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                                                           initializeProxy(settings.getProxy()),
                                                            settings.getProxy().username(),
                                                            settings.getProxy().password(),
                                                                getAccessTokenOfCLI(settings),
                                                            settings.getCentral().getConnectTimeout(),
                                                            settings.getCentral().getReadTimeout(),
                                                            settings.getCentral().getWriteTimeout(),
                                                            settings.getCentral().getCallTimeout());
            boolean foundSearch = false;
            String supportedPlatform = Arrays.stream(JvmTarget.values())
                    .map(target -> target.code())
                    .collect(Collectors.joining(","));
            PackageSearchResult packageSearchResult = client.searchPackage(query,
                    supportedPlatform, RepoUtils.getBallerinaVersion());
            if (packageSearchResult.getCount() > 0) {
                printPackages(packageSearchResult.getPackages(), RepoUtils.getTerminalWidth());
                foundSearch = true;
            }
            if (!foundSearch) {
                outStream.println("no modules found");
            }
        } catch (CentralClientException e) {
            String errorMessage = e.getMessage();
            if (null != errorMessage && !"".equals(errorMessage.trim())) {
                // removing the error stack
                if (errorMessage.contains("\n\tat")) {
                    errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n\tat"));
                }
                CommandUtil.printError(this.errStream, errorMessage, null, false);
                CommandUtil.exitError(this.exitWhenFinish);
            }
        }
    }
}
