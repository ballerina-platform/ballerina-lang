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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.ballerina.cli.BLauncherCmd;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.internal.model.Proxy;
import io.ballerina.projects.internal.model.Repository;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;
import org.ballerinalang.maven.bala.client.MavenResolverClient;
import org.ballerinalang.maven.bala.client.MavenResolverClientException;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.cli.cmd.Constants.PULL_COMMAND;
import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.util.ProjectConstants.BALA_EXTENSION;
import static io.ballerina.projects.util.ProjectConstants.PLATFORM;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.projects.util.ProjectUtils.validateOrgName;
import static io.ballerina.projects.util.ProjectUtils.validatePackageName;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static java.nio.file.Files.createDirectories;

/**
 * This class represents the "bal pull" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = PULL_COMMAND, description = "Pull a package from Ballerina Central")
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

    @CommandLine.Option(names = "--repository")
    private String repositoryName;

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

        Settings settings;
        try {
            settings = RepoUtils.readSettings();
        } catch (SettingsTomlException e) {
            settings = Settings.from();
        }

        Repository targetRepository = null;
        if (repositoryName != null) {
            for (Repository repository : settings.getRepositories()) {
                if (repositoryName.equals(repository.id())) {
                    targetRepository = repository;
                    break;
                }
            }
        }

        if (targetRepository == null && repositoryName != null) {
            String errMsg = "unsupported repository '" + repositoryName + "' found. Only " +
                    "repositories mentioned in the Settings.toml are supported.";
            CommandUtil.printError(this.errStream, errMsg, null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (targetRepository != null) {
            MavenResolverClient mavenResolverClient = new MavenResolverClient();
            if (!targetRepository.username().isEmpty() && !targetRepository.password().isEmpty()) {
                mavenResolverClient.addRepository(targetRepository.id(), targetRepository.url(),
                        targetRepository.username(), targetRepository.password());
            } else {
                mavenResolverClient.addRepository(targetRepository.id(), targetRepository.url());
            }
            Proxy proxy = settings.getProxy();
            mavenResolverClient.setProxy(proxy.host(), proxy.port(), proxy.username(), proxy.password());

            Path mavenBalaCachePath = RepoUtils.createAndGetHomeReposPath()
                    .resolve(ProjectConstants.REPOSITORIES_DIR)
                    .resolve(targetRepository.id())
                    .resolve(ProjectConstants.BALA_DIR_NAME);

            try {
                Path tmpDownloadDirectory = Files.createTempDirectory("ballerina-" + System.nanoTime());
                mavenResolverClient.pullPackage(orgName, packageName, version,
                        String.valueOf(tmpDownloadDirectory.toAbsolutePath()));
                Path balaDownloadPath = tmpDownloadDirectory.resolve(orgName).resolve(packageName).resolve(version)
                        .resolve(packageName + "-" + version + BALA_EXTENSION);
                Path temporaryExtractionPath = tmpDownloadDirectory.resolve(orgName).resolve(packageName)
                        .resolve(version).resolve(PLATFORM);
                ProjectUtils.extractBala(balaDownloadPath, temporaryExtractionPath);
                Path packageJsonPath = temporaryExtractionPath.resolve("package.json");
                try (BufferedReader bufferedReader = Files.newBufferedReader(packageJsonPath, StandardCharsets.UTF_8)) {
                    JsonObject resultObj = new Gson().fromJson(bufferedReader, JsonObject.class);
                    String platform = resultObj.get(PLATFORM).getAsString();
                    Path actualBalaPath = mavenBalaCachePath.resolve(orgName).resolve(packageName)
                            .resolve(version).resolve(platform);
                    org.apache.commons.io.FileUtils.copyDirectory(temporaryExtractionPath.toFile(),
                            actualBalaPath.toFile());
                }
            } catch (MavenResolverClientException e) {
                errStream.println("unexpected error occurred while pulling package:" + e.getMessage());
                CommandUtil.exitError(this.exitWhenFinish);
            } catch (IOException e) {
                throw createLauncherException(
                        "unexpected error occurred while creating package repository in bala cache: " + e.getMessage());
            }
            PrintStream out = System.out;
            out.println("Successfully pulled the package from the custom repository.");
            return;
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

        CommandUtil.setPrintStream(errStream);
        String supportedPlatform = Arrays.stream(JvmTarget.values())
                .map(JvmTarget::code)
                .collect(Collectors.joining(","));
        try {
            CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                    initializeProxy(settings.getProxy()), settings.getProxy().username(),
                    settings.getProxy().password(), getAccessTokenOfCLI(settings),
                    settings.getCentral().getConnectTimeout(),
                    settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                    settings.getCentral().getCallTimeout());
            client.pullPackage(orgName, packageName, version, packagePathInBalaCache, supportedPlatform,
                    RepoUtils.getBallerinaVersion(), false);
            if (version.equals(Names.EMPTY.getValue())) {
                List<String> versions = client.getPackageVersions(orgName, packageName, supportedPlatform,
                        RepoUtils.getBallerinaVersion());
                version = CommandUtil.getLatestVersion(versions);
            }
            boolean hasCompilationErrors = CommandUtil.pullDependencyPackages(orgName, packageName, version);
            if (hasCompilationErrors) {
                CommandUtil.printError(this.errStream, "compilation contains errors", null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        } catch (PackageAlreadyExistsException e) {
            errStream.println(e.getMessage());
            CommandUtil.exitError(this.exitWhenFinish);
        } catch (CentralClientException e) {
            errStream.println("package not found: " + orgName + "/" + packageName);
            CommandUtil.exitError(this.exitWhenFinish);
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
        out.append(BLauncherCmd.getCommandUsageInfo(PULL_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal pull\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
