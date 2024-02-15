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

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Settings;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

import static io.ballerina.cli.cmd.Constants.DEPRECATE_COMMAND;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;

/**
 * This class represents the "bal deprecate" command.
 *
 * @since 2201.5.0
 */
@CommandLine.Command(name = DEPRECATE_COMMAND, description = "Deprecate a package in Ballerina Central")
public class DeprecateCommand implements BLauncherCmd {

    private PrintStream outStream;
    private PrintStream errStream;
    private boolean exitWhenFinish;

    private static final String USAGE_TEXT =
            "bal deprecate {<org-name>/<package-name>:<version>} [OPTIONS]";

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    @CommandLine.Option(names = {"--message"})
    private String deprecationMsg;

    @CommandLine.Option(names = {"--undo"})
    private boolean undoFlag;

    public DeprecateCommand() {
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    public DeprecateCommand(PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(DEPRECATE_COMMAND);
            outStream.println(commandUsageInfo);
            return;
        }

        if (null != debugPort) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }

        if (argList == null || argList.isEmpty()) {
            CommandUtil.printError(this.errStream, "no package given", "bal deprecate <package> [OPTIONS]",
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (argList.size() > 1) {
            CommandUtil.printError(this.errStream, "too many arguments", "bal deprecate <package> ",
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // validate deprecation message
        if (!validateDeprecationMsg(deprecationMsg)) {
            CommandUtil.printError(errStream, "invalid deprecation message. The message cannot contain " +
                            "non-space whitespace or back slash characters.",
                    USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Skip --message flag if it is set with undo option
        if (deprecationMsg != null && undoFlag) {
            this.outStream.println("warning: ignoring --message flag since this is an undo request");
        }
        deprecateInCentral(argList.get(0));

        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    private boolean validateDeprecationMsg(String deprecationMsg) {
        if (deprecationMsg != null) {
            return deprecationMsg.matches("^[^\\f\\n\\r\\t\\v\\\\]*$");
        }
        return true;
    }

    @Override
    public String getName() {
        return DEPRECATE_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(DEPRECATE_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append(" bal deprecate {<org-name>/<package-name>:<version>} \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    /**
     * Deprecate a package in central.
     *
     * @param packageInfo package to deprecate.
     */
    private void deprecateInCentral(String packageInfo) {
        try {
            Settings settings;
            try {
                settings = RepoUtils.readSettings();
                // Ignore Settings.toml diagnostics in the deprecate command
            } catch (SettingsTomlException e) {
                // Ignore 'Settings.toml' parsing errors and return empty Settings object
                settings = Settings.from();
            }
            String packageValue = packageInfo;
            if (packageInfo.split(":").length != 2) {
                packageValue = packageInfo + ":*";
            }
            CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                    initializeProxy(settings.getProxy()), settings.getProxy().username(),
                    settings.getProxy().password(),
                    getAccessTokenOfCLI(settings), settings.getCentral().getConnectTimeout(),
                    settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                    settings.getCentral().getCallTimeout());
            client.deprecatePackage(packageValue, deprecationMsg,
                    JvmTarget.JAVA_17.code(),
                    RepoUtils.getBallerinaVersion(), this.undoFlag);
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
