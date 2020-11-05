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

import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientException;
import org.ballerinalang.central.client.NoPackageException;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.cli.cmd.Constants.PUSH_COMMAND;
import static io.ballerina.runtime.util.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.IMPLEMENTATION_VERSION;
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
        userDir = Paths.get(System.getProperty("user.dir"));
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

        // Get source root path.
        Path sourceRootPath = userDir;

        // Push command only works inside a project
        if (!ProjectUtils.isBallerinaProject(sourceRootPath)) {
            Path findRoot = ProjectUtils.findProjectRoot(sourceRootPath);
            if (null == findRoot) {
                CommandUtil.printError(errStream,
                        "Push command can be only run inside a Ballerina project",
                        null,
                        false);
                return;
            }
            sourceRootPath = findRoot;
        }

        // todo: load project and get src path and all other stuff
        // todo: get target path and balo path using project
        BuildProject project = BuildProject.load(sourceRootPath);

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
        throw new UnsupportedOperationException();
    }

    private void pushPackage(BuildProject project) {
        try {
            Path baloFilePath = validateBalo(project);
            pushBaloToRemote(baloFilePath, project.currentPackage().packageName().toString());
        } catch (IOException e) {
            throw createLauncherException(
                    "unexpected error occurred when trying to push to remote repository: " + getRemoteRepoURL());
        }
    }

    private static Path validateBalo(BuildProject project) throws IOException {
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

        Optional<Path> packageBaloFile;
        try (Stream<Path> baloFilesStream = Files.list(baloOutputDir)) {
            packageBaloFile = baloFilesStream
                    .filter(baloFile -> null != baloFile.getFileName() && baloFile.getFileName().toString()
                            .startsWith(pkgName + "-" + IMPLEMENTATION_VERSION)).findFirst();
        }

        if (!packageBaloFile.isPresent()) {
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
//        List<String> dependenciesWithBaloPath = baloProject.currentPackage().packageDescriptor().dependencies().stream()
//                .filter(dep -> dep.getMetadata().getPath() != null).map(Dependency::getModuleID)
//                .collect(Collectors.toList());
//
//        if (!dependenciesWithBaloPath.isEmpty()) {
//            throw createLauncherException("dependencies cannot be given by path when pushing module(s) to "
//                    + "remote. check dependencies in Ballerina.toml: [" + String.join(", ", dependenciesWithBaloPath)
//                    + "]");
//        }

        // check if the package is already there in remote repository
        PackageDescriptor.Dependency pkgAsDependency = new PackageDescriptor.Dependency(pkgName, orgName, version);

        if (isPackageAvailableInRemote(pkgAsDependency)) {
            throw createLauncherException(
                    "package '" + pkgAsDependency.toString() + "' already exists in " + "remote repository("
                            + getRemoteRepoURL() + "). build and push after "
                            + "updating the version in the Ballerina.toml.");
        }

        // balo file path
        return packageBaloFile.get();
    }

    /**
     * Push a balo file to remote repository.
     *
     * @param baloPath Path to the balo file.
     */
    private static void pushBaloToRemote(Path baloPath, String pkgName) {
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

                    throw createLauncherException(
                            "unexpected error occurred while pushing package '" + pkgName + "' to remote repository("
                                    + getRemoteRepoURL() + "): " + errorMessage);
                }
            }
        }
    }

    private static boolean isPackageAvailableInRemote(PackageDescriptor.Dependency dep) {
        List<String> supportedPlatforms = Arrays.stream(SUPPORTED_PLATFORMS).collect(Collectors.toList());
        supportedPlatforms.add("any");

        for (String supportedPlatform : supportedPlatforms) {
            CentralAPIClient client = new CentralAPIClient();
            try {
                client.getPackage(dep.org().toString(), dep.name().toString(), dep.version().toString(),
                                supportedPlatform);
                return true;
            } catch (NoPackageException e) {
                return false;
            }
        }

        return false;
    }
}
