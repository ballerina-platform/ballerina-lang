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
import io.ballerina.projects.DependencyManifest;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.Settings;
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.repos.TempDirCompilationCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.NoPackageException;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.ballerina.cli.cmd.Constants.PUSH_COMMAND;
import static io.ballerina.cli.utils.CentralUtils.authenticate;
import static io.ballerina.cli.utils.CentralUtils.getBallerinaCentralCliTokenUrl;
import static io.ballerina.cli.utils.CentralUtils.getCentralPackageURL;
import static io.ballerina.projects.util.ProjectConstants.SETTINGS_FILE_NAME;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 * This class represents the "bal push" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = PUSH_COMMAND, description = "push packages and binaries available locally to "
        + "Ballerina Central")
public class PushCommand implements BLauncherCmd {

    @CommandLine.Parameters (arity = "0..1")
    private Path balaPath;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    @CommandLine.Option(names = "--repository")
    private String repositoryName;

    @CommandLine.Option(names = {"--skip-source-check"}, description = "skip checking if source has changed")
    private boolean skipSourceCheck;

    @CommandLine.Option(names = "--experimental", description = "enable experimental language features")
    private boolean experimentalFlag;

    private Path userDir;
    private PrintStream errStream;
    private PrintStream outStream;
    private boolean exitWhenFinish;

    public PushCommand() {
        this.userDir = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        this.errStream = System.err;
        this.outStream = System.out;
        this.exitWhenFinish = true;
    }

    public PushCommand(Path userDir, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.userDir = userDir;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    public PushCommand(Path userDir, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                       Path balaPath) {
        this.userDir = userDir;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.balaPath = balaPath;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(PUSH_COMMAND);
            outStream.println(commandUsageInfo);
            // Exit status, zero for OK, non-zero for error
            Runtime.getRuntime().exit(0);
            return;
        }

        BuildProject project;

        try {
            project = BuildProject.load(userDir);
        } catch (ProjectException e) {
            CommandUtil.printError(errStream, e.getMessage(), null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Enable remote debugging
        if (null != debugPort) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }

        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");

        try {
            // If the repository flag is specified, validate and push to the provided repo
            if (repositoryName != null) {
                if (!repositoryName.equals(ProjectConstants.LOCAL_REPOSITORY_NAME)) {
                    String errMsg = "unsupported repository '" + repositoryName + "' found. Only '"
                            + ProjectConstants.LOCAL_REPOSITORY_NAME + "' repository is supported.";
                    CommandUtil.printError(this.errStream, errMsg, null, false);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return;
                }

                pushPackage(project);
            } else {
                Settings settings = RepoUtils.readSettings();
                if (settings.diagnostics().hasErrors()) {
                    CommandUtil.printError(this.errStream, settings.getErrorMessage(), null, false);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return;
                }
                CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                        initializeProxy(settings.getProxy()), getAccessTokenOfCLI(settings));

                try {
                    pushPackage(project, client);
                } catch (ProjectException | CentralClientException e) {
                    CommandUtil.printError(this.errStream, e.getMessage(), null, false);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return;
                }
            }
        } catch (ProjectException | SettingsTomlException e) {
            CommandUtil.printError(this.errStream, e.getMessage(), null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }


        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    @Override
    public String getName() {
        return PUSH_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Push packages to Ballerina Central");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal push \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private void pushPackage(BuildProject project) {
        Path balaFilePath = validateBalaFile(project, this.balaPath);
        pushBalaToCustomRepo(balaFilePath);

        Path relativePathToBalaFile;
        if (this.balaPath != null) {
            relativePathToBalaFile = balaFilePath;
        } else {
            relativePathToBalaFile = userDir.relativize(balaFilePath);
        }
        outStream.println("Successfully pushed " + relativePathToBalaFile
                + " to '" + repositoryName + "' repository.");
    }

    private void pushPackage(BuildProject project, CentralAPIClient client)
            throws CentralClientException {
        Path balaFilePath = validateBala(project, client, this.balaPath);
        pushBalaToRemote(balaFilePath, client);
    }

    private static Path validateBala(BuildProject project, CentralAPIClient client, Path customBalaPath)
            throws CentralClientException {
        Path packageBalaFile = validateBalaFile(project, customBalaPath);

        // check if the package is already there in remote repository
        DependencyManifest.Package pkgAsDependency = new DependencyManifest.Package(
                project.currentPackage().packageName(),
                project.currentPackage().packageOrg(),
                project.currentPackage().packageVersion());

        if (isPackageAvailableInRemote(pkgAsDependency, client)) {
            String pkg = pkgAsDependency.org().toString() + "/"
                    + pkgAsDependency.name().toString() + ":"
                    + pkgAsDependency.version().toString();
            throw new ProjectException(
                    "package '" + pkg + "' already exists in " + "remote repository :"
                            + getCentralPackageURL(project.currentPackage().packageOrg().value(),
                                                   project.currentPackage().packageName().value())
                            + ". build and push after updating the version in the Ballerina.toml.");
        }

        // bala file path
        return packageBalaFile;
    }

    private static Path validateBalaFile(BuildProject project, Path customBalaPath) {
        final PackageName pkgName = project.currentPackage().packageName();
        final PackageOrg orgName = project.currentPackage().packageOrg();
        PackageVersion packageVersion = project.currentPackage().packageVersion();

        Path packageBalaFile = customBalaPath;

        // If the customBalaPath has not been specified we validate the default paths
        if (packageBalaFile == null) {
            // Get bala output path
            Path balaOutputDir = project.currentPackage().project().targetDir()
                    .resolve(ProjectConstants.TARGET_BALA_DIR_NAME);
            if (Files.notExists(balaOutputDir)) {
                throw new ProjectException("cannot find bala file for the package: " + pkgName + ". Run "
                        + "'bal pack' to compile and generate the bala.");
            }
            packageBalaFile = findBalaFile(pkgName, orgName, balaOutputDir);
        }

        if (null == packageBalaFile) {
            throw new ProjectException("cannot find bala file for the package: " + pkgName + ". Run "
                    + "'bal pack' to compile and generate the bala.");
        }

        if (!packageBalaFile.toString().endsWith(
                packageVersion.toString() + ProjectConstants.BLANG_COMPILED_PKG_BINARY_EXT)) {
            throw new ProjectException(
                    "'" + packageBalaFile + "' does not match with the package version '" + packageVersion.toString()
                            + "' in " + ProjectConstants.BALLERINA_TOML
                            + " file. Run 'bal pack' to recompile and generate the bala.");
        }

        try {
            validatePackageMdAndBalToml(packageBalaFile);
        } catch (IOException e) {
            throw new ProjectException("error while validating the bala file.", e);
        }

        // bala file path
        return packageBalaFile;
    }

    private static void validatePackageMdAndBalToml(Path balaPath) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(balaPath, StandardOpenOption.READ))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals(
                        ProjectConstants.BALA_DOCS_DIR + "/" + ProjectConstants.PACKAGE_MD_FILE_NAME)) {
                    if (entry.getSize() == 0) {
                        throw new ProjectException(ProjectConstants.PACKAGE_MD_FILE_NAME + " cannot be empty.");
                    }
                    return;
                }
            }
        }
        throw new ProjectException(ProjectConstants.PACKAGE_MD_FILE_NAME + " is missing in bala file:" + balaPath);
    }

    private void pushBalaToCustomRepo(Path balaFilePath) {
        ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
        BalaProject balaProject = BalaProject.loadProject(defaultBuilder, balaFilePath);

        Path repoPath = RepoUtils.createAndGetHomeReposPath()
                .resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(ProjectConstants.LOCAL_REPOSITORY_NAME);
        String org = balaProject.currentPackage().packageOrg().value();
        String packageName = balaProject.currentPackage().packageName().value();
        String version = balaProject.currentPackage().packageVersion().toString();
        String platform = balaProject.platform();
        String ballerinaShortVersion = RepoUtils.getBallerinaShortVersion();

        Path balaDestPath = repoPath.resolve(ProjectConstants.BALA_DIR_NAME)
                .resolve(org).resolve(packageName).resolve(version).resolve(platform);
        Path balaCachesPath = repoPath.resolve(ProjectConstants.CACHES_DIR_NAME + "-" + ballerinaShortVersion)
                .resolve(org).resolve(packageName).resolve(version);
        try {
            if (Files.exists(balaDestPath)) {
                ProjectUtils.deleteDirectory(balaDestPath);
            }
            if (Files.exists(balaCachesPath)) {
                ProjectUtils.deleteDirectory(balaCachesPath);
            }
            ProjectUtils.extractBala(balaFilePath, balaDestPath);
        } catch (IOException e) {
            throw new ProjectException("error while pushing bala file '" + balaFilePath + "' to '"
                    + ProjectConstants.LOCAL_REPOSITORY_NAME + "' repository. " + e.getMessage());
        }
    }

    /**
     * Push a bala file to remote repository.
     *
     * @param balaPath Path to the bala file.
     */
    private void pushBalaToRemote(Path balaPath, CentralAPIClient client) {
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

            try {
                authenticate(errStream, getBallerinaCentralCliTokenUrl(), settingsTomlFilePath, client);
            } catch (SettingsTomlException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), null, false);
                return;
            }

            try {
                client.pushPackage(balaPath, org, name, version, JvmTarget.JAVA_11.code(),
                                   RepoUtils.getBallerinaVersion());
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
    private static boolean isPackageAvailableInRemote(DependencyManifest.Package pkg, CentralAPIClient client)
            throws CentralClientException {
        List<String> supportedPlatforms = Arrays.stream(SUPPORTED_PLATFORMS).collect(Collectors.toList());
        supportedPlatforms.add("any");

        for (String supportedPlatform : supportedPlatforms) {
            try {
                client.getPackage(pkg.org().toString(), pkg.name().toString(), pkg.version().toString(),
                                  supportedPlatform, RepoUtils.getBallerinaVersion());
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
