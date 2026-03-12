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

import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.ballerina.cli.BLauncherCmd;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.internal.model.Proxy;
import io.ballerina.projects.internal.model.Repository;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;
import org.ballerinalang.maven.bala.client.MavenResolverClient;
import org.ballerinalang.maven.bala.client.MavenResolverClientException;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.ballerina.cli.cmd.Constants.PULL_COMMAND;
import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.internal.SettingsBuilder.MAVEN;
import static io.ballerina.projects.util.ProjectConstants.BALA_EXTENSION;
import static io.ballerina.projects.util.ProjectConstants.LOCAL_REPOSITORY_NAME;
import static io.ballerina.projects.util.ProjectConstants.PLATFORM;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.getLatest;
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

    private final PrintStream outStream;
    private final PrintStream errStream;
    private final boolean exitWhenFinish;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = { "--help", "-h" }, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    @CommandLine.Option(names = "--repository")
    private String repositoryName;

    @CommandLine.Option(names = "--sticky", hidden = true, defaultValue = "false")
    private boolean sticky;

    @CommandLine.Option(names = "--offline", hidden = true)
    private boolean offline;

    @CommandLine.Option(names = "--locking-mode", hidden = true,
            description = "allow passing the package locking mode.", converter = PackageLockingModeConverter.class)
    private PackageLockingMode lockingMode;

    public PullCommand() {
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    public PullCommand(PrintStream errStream, boolean exitWhenFinish) {
        this.outStream = errStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.repositoryName = null;
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
            CommandUtil.printError(errStream, "invalid package. Provide the package name with the organization.",
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
            CommandUtil.printError(errStream, "invalid package. Provide the package name with the organization.",
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
        settings = RepoUtils.readSettings();

        if (repositoryName == null) {
            repositoryName = ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
            version = pullFromCentral(settings, orgName, packageName, version);
        } else if (!LOCAL_REPOSITORY_NAME.equals(repositoryName)) {
            pullFromMavenRepo(settings, orgName, packageName, version);
        }

        if (!resolveDependencies(orgName, packageName, version)) {
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }

    }

    private String pullFromCentral(Settings settings, String orgName, String packageName, String version) {
        Repository[] mvnRepositories = settings.getRepositories();
        Repository centralProxyMavenRepository = null;
        for (Repository repository : mvnRepositories) {
            if (MAVEN.equals(repository.type()) && repository.proxyCentral()) {
                centralProxyMavenRepository = repository;
                break;
            }
        }
        Path packagePathInBalaCache = ProjectUtils.createAndGetHomeReposPath()
                .resolve(ProjectConstants.REPOSITORIES_DIR).resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME)
                .resolve(orgName).resolve(packageName);

        if (!version.equals(Names.EMPTY.getValue()) && Files.exists(packagePathInBalaCache.resolve(version))) {
            outStream.println("Package already exists.\n");
            return version;
        }
        // create directory path in bala cache
        try {
            createDirectories(packagePathInBalaCache);
        } catch (IOException e) {
            CommandUtil.exitError(this.exitWhenFinish);
            throw createLauncherException(
                    "unexpected error occurred while creating package repository in bala cache: " + e.getMessage());
        }

        CommandUtil.setPrintStream(errStream);
        try {
            if (centralProxyMavenRepository != null) {
                return pullFromMvnProxy(settings, centralProxyMavenRepository, orgName, packageName, version);
            }
            return pullFromBCentral(settings, orgName, packageName, version, packagePathInBalaCache);
        } catch (PackageAlreadyExistsException e) {
            // If version is specified by the user && the package exists, it shouldn't reach this point.
            assert version.equals(Names.EMPTY.getValue());
            outStream.println("Package already exists.\n");
            version = e.version();
        } catch (CentralClientException e) {
            errStream.println("package not found: " + orgName + "/" + packageName);
            CommandUtil.exitError(this.exitWhenFinish);
        } catch (MavenResolverClientException e) {
            errStream.println(e.getMessage());
            CommandUtil.exitError(this.exitWhenFinish);
        }
        return version;
    }

    private String pullFromMvnProxy(Settings settings, Repository centralProxyMavenRepository, String orgName,
                                    String packageName, String version) throws MavenResolverClientException {
        MavenResolverClient client = new MavenResolverClient();
        if (!centralProxyMavenRepository.username().isEmpty() && !centralProxyMavenRepository.password().isEmpty()) {
            client.addRepository(centralProxyMavenRepository.id(), centralProxyMavenRepository.url(),
                    centralProxyMavenRepository.username(), centralProxyMavenRepository.password());
        } else {
            client.addRepository(centralProxyMavenRepository.id(), centralProxyMavenRepository.url());
        }
        Proxy proxy = settings.getProxy();
        client.setProxy(proxy.host(), proxy.port(), proxy.username(), proxy.password());

        Path mavenPackageRootPath = RepoUtils.createAndGetHomeReposPath()
                .resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME)
                .resolve(orgName).resolve(packageName);
        if (version.isEmpty()) {
            //TODO :  Need to check this logic whether central client will return all versions
            // or only compatible versions with the current ballerina version. If it returns all versions,
            // we need to filter the versions which are compatible with the current ballerina version.
            List<String> packageVersions = client.getPackageVersionsInCentralProxy(orgName,
                    packageName, RepoUtils.getBallerinaShortVersion(), mavenPackageRootPath);
            List<PackageVersion> packageVersionsList = new ArrayList<>();
            packageVersions.stream().map(PackageVersion::from).forEach(packageVersionsList::add);
            PackageVersion latest = findLatest(packageVersionsList);
            version = latest.toString();
        }
        Path mavenBalaCachePath = mavenPackageRootPath.resolve(version);
        if (Files.exists(mavenBalaCachePath)) {
            outStream.println("Package already exists.\n");
            return version;
        }
        try {
            //TODO: Optimize this by using maven metadata to get platform
            Path tmpDownloadDirectory = Files.createTempDirectory("ballerina-" + System.nanoTime());
            client.pullPackage(orgName, packageName, version,
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
                Path actualBalaPath = mavenBalaCachePath.resolve(platform);
                FileUtils.copyDirectory(temporaryExtractionPath.toFile(), actualBalaPath.toFile());
            }
        } catch (IOException e) {
            throw createLauncherException(
                    "unexpected error occurred while creating package repository in bala cache: " + e.getMessage());
        }
        return version;
    }


    private List<String> getIncompatibleDistPkgVer(List<String> versions, String org, String name,
                                                   MavenResolverClient client, Path mavenPackageRootPath)
            throws MavenResolverClientException {
        List<String> incompatibleVersions = new ArrayList<>();
        if (!versions.isEmpty()) {
            for (String ver : versions) {
                String packBallerinaVer = RepoUtils.getBallerinaShortVersion();
                String packageBallerinaVer = client.getBallerinaVersionForPackage(org, name, ver, mavenPackageRootPath);
                if (!isCompatible(packageBallerinaVer, packBallerinaVer)) {
                    incompatibleVersions.add(ver);
                }
            }
        }
        return incompatibleVersions;
    }

    private PackageVersion findLatest(Collection<PackageVersion> packageVersions) {
        if (packageVersions.isEmpty()) {
            return null;
        }

        PackageVersion latestVersion = packageVersions.iterator().next();
        for (PackageVersion pkgVersion : packageVersions) {
            latestVersion = getLatest(latestVersion, pkgVersion);
        }
        return latestVersion;
    }

    private boolean isCompatible(String pkgBalVer, String distBalVer) {
        if (pkgBalVer.equals(distBalVer) || pkgBalVer.startsWith("slbeta")) {
            return true;
        }
        Version pkgSemVer;
        Version distSemVer;
        try {
            pkgSemVer = Version.valueOf(pkgBalVer);
            distSemVer = Version.valueOf(distBalVer);

            if (pkgSemVer.getMajorVersion() == distSemVer.getMajorVersion()) {
                if (pkgSemVer.getMinorVersion() == distSemVer.getMinorVersion()) {
                    return true;
                }
                return !pkgSemVer.greaterThan(distSemVer);
            }
        } catch (UnexpectedCharacterException ignore) {
            // SemVer incompatible versions will throw this exception.
            // Catching this is mainly to handle slalpha versions
        }
        return false;
    }

    private String pullFromBCentral(Settings settings, String orgName, String packageName, String version,
                                    Path packagePathInBalaCache) throws CentralClientException {
        CentralAPIClient client;
        String supportedPlatform = Arrays.stream(JvmTarget.values())
                .map(JvmTarget::code)
                .collect(Collectors.joining(","));
        client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings),
                settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout(), settings.getCentral().getMaxRetries());
        client.pullPackage(orgName, packageName, version, packagePathInBalaCache, supportedPlatform,
                RepoUtils.getBallerinaVersion(), false);
        if (version.equals(Names.EMPTY.getValue())) {
            List<String> versions = client.getPackageVersions(orgName, packageName, supportedPlatform,
                    RepoUtils.getBallerinaVersion());
            return CommandUtil.getLatestVersion(versions);
        }
        return version;
    }

    private void pullFromMavenRepo(Settings settings, String orgName, String packageName, String version) {
        Repository targetRepository = null;
        for (Repository repository : settings.getRepositories()) {
            if (repositoryName.equals(repository.id())) {
                targetRepository = repository;
                break;
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
                    .resolve(ProjectConstants.BALA_DIR_NAME)
                    .resolve(orgName).resolve(packageName).resolve(version);

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
                    Path actualBalaPath = mavenBalaCachePath.resolve(platform);
                    FileUtils.copyDirectory(temporaryExtractionPath.toFile(), actualBalaPath.toFile());
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
        }
    }

    private boolean resolveDependencies(String orgName, String packageName, String version) {
        CommandUtil.setPrintStream(errStream);
        try {
            PackageLockingMode packageLockingMode;
            if (sticky) {
                packageLockingMode = PackageLockingMode.HARD;
            }  else {
                packageLockingMode = Objects.requireNonNullElse(lockingMode, PackageLockingMode.SOFT);
            }
            BuildOptions buildOptions = BuildOptions.builder().setLockingMode(packageLockingMode).setOffline(offline)
                    .build();
            boolean hasCompilationErrors = CommandUtil.pullDependencyPackages(
                    orgName, packageName, version, buildOptions, repositoryName);
            if (hasCompilationErrors) {
                CommandUtil.printError(this.errStream, "compilation contains errors", null, false);
                return false;
            }
        } catch (ProjectException e) {
            CommandUtil.printError(this.errStream,
                    "error occurred while resolving dependencies, reason: " + e.getMessage(), null, false);
        }
        return true;
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
