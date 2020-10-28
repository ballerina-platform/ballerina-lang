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

import io.ballerina.projects.utils.ProjectConstants;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static io.ballerina.cli.cmd.Constants.PULL_COMMAND;
import static io.ballerina.projects.utils.ProjectConstants.PKG_NAME_REGEX;
import static io.ballerina.runtime.util.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static java.nio.file.Files.createDirectory;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 * This class represents the "ballerina pull" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = PULL_COMMAND,
        description = "download the module source and binaries from a remote repository")
public class PullCommand implements BLauncherCmd {
    private static PrintStream outStream = System.err;

    @CommandLine.Parameters private List<String> argList;

    @CommandLine.Option(names = { "--help", "-h" }, hidden = true) private boolean helpFlag;

    @CommandLine.Option(names = "--debug", hidden = true) private String debugPort;

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(PULL_COMMAND);
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.isEmpty()) {
            throw LauncherUtils.createUsageExceptionWithHelp("no package given");
        }

        if (argList.size() > 1) {
            throw LauncherUtils.createUsageExceptionWithHelp("too many arguments");
        }

        // Enable remote debugging
        if (null != debugPort) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }

        String resourceName = argList.get(0);
        String orgName;
        String packageName;
        String version;

        if (!validPackageName(resourceName)) {
            CommandUtil.printError(outStream, "invalid package name. Provide the package name with the org name ",
                    "ballerina pull {<org-name>/<package-name> | <org-name>/<package-name>:<version>}", false);
            Runtime.getRuntime().exit(1);
            return;
        }

        // Get org name
        String[] moduleInfo = resourceName.split("/");
        orgName = moduleInfo[0];
        String moduleNameAndVersion = moduleInfo[1];

        // Get package name
        String[] packageInfo = moduleNameAndVersion.split(":");
        if (packageInfo.length == 2) {
            packageName = packageInfo[0];
            version = packageInfo[1];
        } else {
            packageName = moduleNameAndVersion;
            version = Names.EMPTY.getValue();
        }

        Path packagePathInBaloCache = RepoUtils.createAndGetHomeReposPath()
                .resolve(ProjectConstants.BALO_CACHE_DIR_NAME).resolve(orgName).resolve(packageName);
        // create directory path in balo cache
        try {
            createDirectory(packagePathInBaloCache);
        } catch (IOException e) {
            throw createLauncherException(
                    "unexpected error occurred while creating package repository in balo cache: " + e.getMessage());
        }

        for (String supportedPlatform : SUPPORTED_PLATFORMS) {
            CentralAPIClient client = new CentralAPIClient();
            client.pullPackage(orgName, packageName, version, packagePathInBaloCache, supportedPlatform, false);
        }
    }

    @Override
    public String getName() {
        return PULL_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("download modules to the user repository \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina pull <module-name> \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
        throw new UnsupportedOperationException();
    }

    private String getPullCommandRegex() {
        return PKG_NAME_REGEX;
    }

    private boolean validPackageName(String str) {
        return Pattern.matches(getPullCommandRegex(), str);
    }
}
