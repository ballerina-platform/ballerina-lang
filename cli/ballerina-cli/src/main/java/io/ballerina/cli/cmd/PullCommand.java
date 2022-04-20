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
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

import static io.ballerina.cli.cmd.Constants.PULL_COMMAND;
import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.projects.util.ProjectUtils.validateOrgName;
import static io.ballerina.projects.util.ProjectUtils.validatePackageName;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static java.nio.file.Files.createDirectories;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 * This class represents the "bal pull" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = PULL_COMMAND,
        description = "download the module source and binaries from a remote repository")
public class PullCommand implements BLauncherCmd {

    private static final String USAGE_TEXT =
            "bal pull {<org-name>/<package-name> | <org-name>/<package-name>:<version>}";

    private PrintStream errStream;
    private boolean exitWhenFinish;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = { "--help", "-h" }, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    public PullCommand() {
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    public PullCommand(PrintStream errStream, boolean exitWhenFinish) {
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(PULL_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.isEmpty()) {
            CommandUtil.printError(this.errStream, "no package given", "bal pull <package-name> ", false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (argList.size() > 1) {
            CommandUtil.printError(this.errStream, "too many arguments", "bal pull <package-name> ", false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Enable remote debugging
        if (null != debugPort) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }

        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");

        String resourceName = argList.get(0);
        String orgName;
        String packageName;
        String version;

        // Get org name
        String[] moduleInfo = resourceName.split("/");
        if (moduleInfo.length != 2) {
            CommandUtil.printError(errStream, "invalid package name. Provide the package name with the organization.",
                                   USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        orgName = moduleInfo[0];
        String moduleNameAndVersion = moduleInfo[1];

        // Get package name
        String[] packageInfo = moduleNameAndVersion.split(":");
        if (packageInfo.length == 2) {
            packageName = packageInfo[0];
            version = packageInfo[1];
        } else if (packageInfo.length == 1) {
            packageName = moduleNameAndVersion;
            version = Names.EMPTY.getValue();
        } else {
            CommandUtil.printError(errStream, "invalid package name. Provide the package name with the organization.",
                                   USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Validate package org, name and version
        if (!validateOrgName(orgName)) {
            CommandUtil.printError(errStream, "invalid organization. Provide the package name with the organization.",
                                   USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (!validatePackageName(packageName)) {
            CommandUtil.printError(errStream, "invalid package name. Provide the package name with the organization.",
                                   USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (!version.equals(Names.EMPTY.getValue())) {
            // check version is compatible with semver
            try {
                SemanticVersion.from(version);
            } catch (ProjectException e) {
                CommandUtil.printError(errStream, "invalid package version. " + e.getMessage(), USAGE_TEXT, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        Path packagePathInBalaCache = ProjectUtils.createAndGetHomeReposPath()
                .resolve(ProjectConstants.REPOSITORIES_DIR).resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME)
                .resolve(orgName).resolve(packageName);
        // create directory path in bala cache
        try {
            createDirectories(packagePathInBalaCache);
        } catch (IOException e) {
            CommandUtil.exitError(this.exitWhenFinish);
            throw createLauncherException(
                    "unexpected error occurred while creating package repository in bala cache: " + e.getMessage());
        }

        for (String supportedPlatform : SUPPORTED_PLATFORMS) {
            try {
                Settings settings;
                try {
                    settings = RepoUtils.readSettings();
                    // Ignore Settings.toml diagnostics in the pull command
                } catch (SettingsTomlException e) {
                    // Ignore 'Settings.toml' parsing errors and return empty Settings object
                    settings = Settings.from();
                }
                CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                                                               initializeProxy(settings.getProxy()),
                                                               getAccessTokenOfCLI(settings));
                client.pullPackage(orgName, packageName, version, packagePathInBalaCache, supportedPlatform,
                                   RepoUtils.getBallerinaVersion(), false);
            } catch (PackageAlreadyExistsException e) {
                errStream.println(e.getMessage());
                CommandUtil.exitError(this.exitWhenFinish);
            } catch (CentralClientException e) {
                errStream.println("unexpected error occurred while pulling package:" + e.getMessage());
                CommandUtil.exitError(this.exitWhenFinish);
            }
        }

        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    @Override
    public String getName() {
        return PULL_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Download modules to the user repository \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal pull\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
