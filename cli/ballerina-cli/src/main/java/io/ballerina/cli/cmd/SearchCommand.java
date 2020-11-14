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

import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientException;
import org.ballerinalang.central.client.model.PackageSearchResult;
import org.ballerinalang.tool.BLauncherCmd;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

import static io.ballerina.cli.cmd.Constants.SEARCH_COMMAND;
import static io.ballerina.cli.utils.PrintUtils.printPackages;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.tool.LauncherUtils.createUsageExceptionWithHelp;

/**
 * This class represents the "ballerina search" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = SEARCH_COMMAND, description = "search for packages within Ballerina Central")
public class SearchCommand implements BLauncherCmd {
    private static PrintStream outStream = System.err;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

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
            throw createUsageExceptionWithHelp("no keyword given");
        }

        if (argList.size() > 1) {
            throw createUsageExceptionWithHelp("too many arguments");
        }

        String searchArgs = argList.get(0);
        searchInCentral(searchArgs);
        Runtime.getRuntime().exit(0);
    }

    @Override
    public String getName() {
        return SEARCH_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("searches for packages within Ballerina Central \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append(" ballerina search [<org>|<package>|<text>] \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    /**
     * Search for packages in central.
     *
     * @param query search keyword.
     */
    private static void searchInCentral(String query) {
        try {
            CentralAPIClient client = new CentralAPIClient();
            PackageSearchResult packageSearchResult = client.searchPackage(query);

            if (packageSearchResult.getCount() > 0) {
                printPackages(packageSearchResult.getPackages(), RepoUtils.getTerminalWidth());
            } else {
                outStream.println("no modules found");
            }
        } catch (CentralClientException e) {
            String errorMessage = e.getMessage();
            if (null != errorMessage && !"".equals(errorMessage.trim())) {
                // removing the error stack
                if (errorMessage.contains("\n\tat")) {
                    errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n\tat"));
                }

                outStream.println(errorMessage);
            }
        }
    }
}
