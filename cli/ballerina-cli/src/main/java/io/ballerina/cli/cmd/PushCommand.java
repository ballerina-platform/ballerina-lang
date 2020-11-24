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

import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientException;
import org.ballerinalang.central.client.NoPackageException;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.cli.cmd.Constants.PUSH_COMMAND;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;
import static org.wso2.ballerinalang.util.RepoUtils.getRemoteRepoURL;

/**
 * This class represents the "ballerina push" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = PUSH_COMMAND, description = "push packages and binaries available locally to "
        + "Ballerina Central")
public class PushCommand implements BLauncherCmd {
    private static PrintStream outStream = System.err;

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
    
    public PushCommand() {
        userDir = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        errStream = System.err;
    }
    
    public PushCommand(Path userDir, PrintStream errStream) {
        this.userDir = userDir;
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
            pushPackage(project);
        } else {
            throw LauncherUtils.createUsageExceptionWithHelp("too many arguments");
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
        out.append("  ballerina push \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private void pushPackage(BuildProject project) {
        Path baloFilePath = validateBalo(project);
        pushBaloToRemote(baloFilePath);
    }

    private static Path validateBalo(BuildProject project) {
        final PackageName pkgName = project.currentPackage().packageName();
        final PackageOrg orgName = project.currentPackage().packageOrg();
        final PackageVersion version = project.currentPackage().packageVersion();

        // Get balo output path
        Path baloOutputDir = project.currentPackage().project().sourceRoot().resolve(ProjectConstants.TARGET_DIR_NAME)
                .resolve(ProjectConstants.TARGET_BALO_DIR_NAME);

        if (Files.notExists(baloOutputDir)) {
            throw createLauncherException("cannot find balo file for the package: " + pkgName + ". Run "
                    + "'ballerina build' to compile and generate the balo.");
        }

        Path packageBaloFile = findBaloFile(pkgName, orgName, baloOutputDir);
        if (null != packageBaloFile && !packageBaloFile.toFile().exists()) {
            throw createLauncherException("cannot find balo file for the package: " + pkgName + ". Run "
                    + "'ballerina build' to compile and generate the balo.");
        }

        // Validate the org-name
        if (!RepoUtils.validateOrg(orgName.toString())) {
            throw createLauncherException(
                    "invalid organization name provided \'" + orgName
                            + "\'. Only lowercase alphanumerics "
                            + "and underscores are allowed in an organization name and the maximum "
                            + "length is 256 characters");
        }

        // Validate the pkg-name
        if (!RepoUtils.validatePkg(pkgName.toString())) {
            throw createLauncherException("invalid package name provided \'" + pkgName + "\'. Only "
                    + "alphanumerics, underscores and periods are allowed in a package name "
                    + "and the maximum length is 256 characters");
        }

        // todo: need to add after ballerina.toml changes
        // check if there are any dependencies with balo path
//        List<String> dependenciesWithBaloPath = baloProject.currentPackage().packageDescriptor().dependencies()
//                .stream()
//                .filter(dep -> dep.getMetadata().getPath() != null).map(Dependency::getModuleID)
//                .collect(Collectors.toList());
//
//        if (!dependenciesWithBaloPath.isEmpty()) {
//            throw createLauncherException("dependencies cannot be given by path when pushing module(s) to "
//                    + "remote. check dependencies in Ballerina.toml: [" + String.join(", ", dependenciesWithBaloPath)
//                    + "]");
//        }

        // check if the package is already there in remote repository
        PackageManifest.Dependency pkgAsDependency = new PackageManifest.Dependency(pkgName, orgName, version);

        if (isPackageAvailableInRemote(pkgAsDependency)) {
            throw createLauncherException(
                    "package '" + pkgAsDependency.toString() + "' already exists in " + "remote repository("
                            + getRemoteRepoURL() + "). build and push after "
                            + "updating the version in the Ballerina.toml.");
        }

        // balo file path
        return packageBaloFile;
    }

    /**
     * Push a balo file to remote repository.
     *
     * @param baloPath Path to the balo file.
     */
    private static void pushBaloToRemote(Path baloPath) {
        Path baloFileName = baloPath.getFileName();
        if (null != baloFileName) {
            try {
                CentralAPIClient client = new CentralAPIClient();
                client.pushPackage(baloPath);
            } catch (CentralClientException e) {
                String errorMessage = e.getMessage();
                if (null != errorMessage && !"".equals(errorMessage.trim())) {
                    // removing the error stack
                    if (errorMessage.contains("\n\tat")) {
                        errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n\tat"));
                    }

                    errorMessage = errorMessage.replaceAll("error: ", "");
                    throw createLauncherException(errorMessage);
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
    private static boolean isPackageAvailableInRemote(PackageManifest.Dependency pkg) {
        List<String> supportedPlatforms = Arrays.stream(SUPPORTED_PLATFORMS).collect(Collectors.toList());
        supportedPlatforms.add("any");

        for (String supportedPlatform : supportedPlatforms) {
            CentralAPIClient client = new CentralAPIClient();
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
     * Find and return matching balo file from balo output directory.
     *
     * @param pkgName       package name
     * @param orgName       org name
     * @param baloOutputDir balo output directory
     * @return matching balo file path
     */
    private static Path findBaloFile(PackageName pkgName, PackageOrg orgName, Path baloOutputDir) {
        Path baloFilePath = null;
        File[] baloFiles = new File(baloOutputDir.toString()).listFiles();
        if (baloFiles != null && baloFiles.length > 0) {
            for (File baloFile : baloFiles) {
                if (baloFile != null && baloFile.getName().startsWith(orgName + "-" + pkgName)) {
                    baloFilePath = baloFile.toPath();
                    break;
                }
            }
        }
        return baloFilePath;
    }
}
