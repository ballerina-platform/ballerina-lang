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
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.repos.TempDirCompilationCache;
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.NoPackageException;
import org.ballerinalang.toml.model.Settings;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.cli.cmd.Constants.PUSH_COMMAND;
import static io.ballerina.cli.utils.CentralUtils.authenticate;
import static io.ballerina.cli.utils.CentralUtils.getBallerinaCentralCliTokenUrl;
import static io.ballerina.cli.utils.CentralUtils.readSettings;
import static io.ballerina.projects.util.ProjectConstants.SETTINGS_FILE_NAME;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;
import static org.wso2.ballerinalang.util.RepoUtils.getRemoteRepoURL;

/**
 * This class represents the "bal push" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = PUSH_COMMAND, description = "push packages and binaries available locally to "
        + "Ballerina Central")
public class PushCommand implements BLauncherCmd {

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    @CommandLine.Option(names = "--repository", hidden = true)
    private String repositoryHome;

    @CommandLine.Option(names = {"--skip-source-check"}, description = "skip checking if source has changed")
    private boolean skipSourceCheck;

    @CommandLine.Option(names = "--experimental", description = "enable experimental language features")
    private boolean experimentalFlag;

    private Path userDir;
    private PrintStream errStream;
    private PrintStream outStream;

    public PushCommand() {
        userDir = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        errStream = System.err;
        outStream = System.out;
    }

    public PushCommand(Path userDir, PrintStream outStream, PrintStream errStream) {
        this.userDir = userDir;
        this.outStream = outStream;
        this.errStream = errStream;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(PUSH_COMMAND);
            outStream.println(commandUsageInfo);
            return;
        }

        BuildProject project;
        try {
            project = BuildProject.load(userDir);
        } catch (ProjectException e) {
            CommandUtil.printError(errStream, e.getMessage(), null, false);
            return;
        }

        // Enable remote debugging
        if (null != debugPort) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }

        if (argList == null || argList.isEmpty()) {
            Settings settings = readSettings();
            Proxy proxy = initializeProxy(settings.getProxy());
            CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(), proxy);

            try {
                pushPackage(project, client, settings);
            } catch (ProjectException | CentralClientException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), null, false);
                return;
            }
        } else {
            CommandUtil.printError(this.errStream, "too many arguments", "bal push ", false);
            return;
        }

        // Exit status, zero for OK, non-zero for error
        Runtime.getRuntime().exit(0);
    }

    @Override
    public String getName() {
        return PUSH_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("push packages to Ballerina Central");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal push \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private void pushPackage(BuildProject project, CentralAPIClient client, Settings settings)
            throws CentralClientException {
        Path balaFilePath = validateBala(project, client);
        pushBalaToRemote(balaFilePath, client, settings);
    }

    private static Path validateBala(BuildProject project, CentralAPIClient client) throws CentralClientException {
        final PackageName pkgName = project.currentPackage().packageName();
        final PackageOrg orgName = project.currentPackage().packageOrg();
        final PackageVersion version = project.currentPackage().packageVersion();

        // Get bala output path
        Path balaOutputDir = project.currentPackage().project().sourceRoot().resolve(ProjectConstants.TARGET_DIR_NAME)
                .resolve(ProjectConstants.TARGET_BALA_DIR_NAME);

        if (Files.notExists(balaOutputDir)) {
            throw new ProjectException("cannot find bala file for the package: " + pkgName + ". Run "
                    + "'bal build' to compile and generate the bala.");
        }

        Path packageBalaFile = findBalaFile(pkgName, orgName, balaOutputDir);
        if (null == packageBalaFile) {
            throw new ProjectException("cannot find bala file for the package: " + pkgName + ". Run "
                    + "'bal build' to compile and generate the bala.");
        }

        // check if the package is already there in remote repository
        PackageManifest.Dependency pkgAsDependency = new PackageManifest.Dependency(pkgName, orgName, version);

        if (isPackageAvailableInRemote(pkgAsDependency, client)) {
            String pkg = pkgAsDependency.org().toString() + "/"
                    + pkgAsDependency.name().toString() + ":"
                    + pkgAsDependency.version().toString();
            throw new ProjectException(
                    "package '" + pkg + "' already exists in " + "remote repository("
                            + getRemoteRepoURL() + "). build and push after "
                            + "updating the version in the Ballerina.toml.");
        }

        // bala file path
        return packageBalaFile;
    }

    /**
     * Push a bala file to remote repository.
     *
     * @param balaPath Path to the bala file.
     */
    private void pushBalaToRemote(Path balaPath, CentralAPIClient client, Settings settings) {
        Path balaFileName = balaPath.getFileName();
        if (null != balaFileName) {
            ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
            defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
            BalaProject balaProject = BalaProject.loadProject(defaultBuilder, balaPath);

            String org = balaProject.currentPackage().manifest().org().toString();
            String name = balaProject.currentPackage().manifest().name().toString();
            String version = balaProject.currentPackage().manifest().version().toString();

            Path ballerinaHomePath = RepoUtils.createAndGetHomeReposPath();
            Path settingsTomlFilePath = ballerinaHomePath.resolve(SETTINGS_FILE_NAME);
            String accessToken = authenticate(errStream, getBallerinaCentralCliTokenUrl(), settings,
                                              settingsTomlFilePath);

            try {
                client.pushPackage(balaPath, org, name, version, accessToken, RepoUtils.getBallerinaVersion());
            } catch (CentralClientException e) {
                String errorMessage = e.getMessage();
                if (null != errorMessage && !"".equals(errorMessage.trim())) {
                    // removing the error stack
                    if (errorMessage.contains("\n\tat")) {
                        errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n\tat"));
                    }

                    errorMessage = errorMessage.replaceAll("error: ", "");

                    // when unauthorized access token for organization is given
                    if (errorMessage.contains("subject claims missing in the user info repsonse")) {
                        errorMessage = "invalid access token in the '" + SETTINGS_FILE_NAME + "'";
                    }
                    throw new ProjectException(errorMessage);
                }
            }
        }
    }

    /**
     * Check if package already available in the remote.
     *
     * @param pkg package
     * @return is package available in the remote
     */
    private static boolean isPackageAvailableInRemote(PackageManifest.Dependency pkg, CentralAPIClient client)
            throws CentralClientException {
        List<String> supportedPlatforms = Arrays.stream(SUPPORTED_PLATFORMS).collect(Collectors.toList());
        supportedPlatforms.add("any");

        for (String supportedPlatform : supportedPlatforms) {
            try {
                client.getPackage(pkg.org().toString(), pkg.name().toString(), pkg.version().toString(),
                                  supportedPlatform);
                return true;
            } catch (NoPackageException e) {
                return false;
            }
        }

        return false;
    }

    /**
     * Find and return matching bala file from bala output directory.
     *
     * @param pkgName       package name
     * @param orgName       org name
     * @param balaOutputDir bala output directory
     * @return matching bala file path
     */
    private static Path findBalaFile(PackageName pkgName, PackageOrg orgName, Path balaOutputDir) {
        Path balaFilePath = null;
        File[] balaFiles = new File(balaOutputDir.toString()).listFiles();
        if (balaFiles != null && balaFiles.length > 0) {
            for (File balaFile : balaFiles) {
                if (balaFile != null && balaFile.getName().startsWith(orgName + "-" + pkgName)) {
                    balaFilePath = balaFile.toPath();
                    break;
                }
            }
        }
        return balaFilePath;
    }
}
