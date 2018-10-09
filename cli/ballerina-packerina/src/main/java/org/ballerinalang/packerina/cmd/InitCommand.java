/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.cmd;

import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.packerina.init.InitHandler;
import org.ballerinalang.packerina.init.models.FileType;
import org.ballerinalang.packerina.init.models.PackageMdFile;
import org.ballerinalang.packerina.init.models.SrcFile;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.ballerinalang.packerina.cmd.Constants.INIT_COMMAND;

/**
 * Init command for creating a ballerina project.
 */
@CommandLine.Command(name = INIT_COMMAND, description = "initialize a Ballerina project")
public class InitCommand implements BLauncherCmd {

    public static final String DEFAULT_VERSION = "0.0.1";
    private static final String USER_DIR = "user.dir";
    private static final PrintStream outStream = System.err;

    @CommandLine.Option(names = {"--interactive", "-i"})
    private boolean interactiveFlag;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    private static boolean isDirEmpty(final Path directory) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    @Override
    public void execute() {
        PrintStream out = System.out;

        // Get source root path.
        Path projectPath = Paths.get(System.getProperty(USER_DIR));
        Scanner scanner = new Scanner(System.in, Charset.defaultCharset().name());
        try {
            Manifest manifest = null;

            if (helpFlag) {
                String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(INIT_COMMAND);
                outStream.println(commandUsageInfo);
                return;
            }

            List<SrcFile> sourceFiles = new ArrayList<>();
            List<PackageMdFile> packageMdFiles = new ArrayList<>();
            if (interactiveFlag) {

                // Check if Ballerina.toml file needs to be created.
                out.print("Create Ballerina.toml [yes/y, no/n]: (y) ");
                String createToml = scanner.nextLine().trim();

                if (createToml.equalsIgnoreCase("yes") || createToml.equalsIgnoreCase("y") || createToml.isEmpty()) {
                    manifest = new Manifest();

                    String defaultOrg = guessOrgName();

                    String orgName;
                    do {
                        out.print("Organization name: (" + defaultOrg + ") ");
                        orgName = scanner.nextLine().trim();
                    } while (!validateOrgName(out, orgName));
                    // Set org-name
                    manifest.setName(orgName.isEmpty() ? defaultOrg : orgName);
                    String version;
                    do {
                        out.print("Version: (" + DEFAULT_VERSION + ") ");
                        version = scanner.nextLine().trim();
                        version = version.isEmpty() ? DEFAULT_VERSION : version;
                    } while (!validateVersion(out, version));

                    manifest.setVersion(version);
                }

                String srcInput;
                boolean validInput = false;
                boolean firstPrompt = true;
                do {
                    // Following will be the first prompt and it will create a service by default. This is to align
                    // with the non-interactive implementation.
                    if (firstPrompt) {
                        // Here if the user presses enter or "s" a service will be created (This will have the same
                        // behavior as running ballerina init without the interactive mode)
                        out.print("Ballerina source [service/s, main/m, finish/f]: (s) ");
                    } else {
                        // Following will be prompted after the first prompt
                        // Here if the user presses enter, "f" or "finish" the command will be exited. If user gives
                        // "m" a main function and "s" a service will be created.
                        out.print("Ballerina source [service/s, main/m, finish/f]: (f) ");
                    }
                    srcInput = scanner.nextLine().trim();

                    if (srcInput.equalsIgnoreCase("service") || srcInput.equalsIgnoreCase("s")
                            || (srcInput.isEmpty() && firstPrompt)) {
                        String packageName;
                        do {
                            out.print("Package for the service: (no package) ");
                            packageName = scanner.nextLine().trim();
                        } while (!validatePkgName(out, packageName));
                        SrcFile srcFile = new SrcFile(packageName, FileType.SERVICE);
                        sourceFiles.add(srcFile);
                        SrcFile srcTestFile = new SrcFile(packageName, FileType.SERVICE_TEST);
                        sourceFiles.add(srcTestFile);
                        if (!packageName.isEmpty()) {
                            PackageMdFile packageMdFile = new PackageMdFile(packageName, FileType.SERVICE);
                            packageMdFiles.add(packageMdFile);
                        }
                        firstPrompt = false;
                    } else if (srcInput.equalsIgnoreCase("main") || srcInput.equalsIgnoreCase("m")) {
                        String packageName;
                        do {
                            out.print("Package for the main: (no package) ");
                            packageName = scanner.nextLine().trim();
                        } while (!validatePkgName(out, packageName));
                        SrcFile srcFile = new SrcFile(packageName, FileType.MAIN);
                        sourceFiles.add(srcFile);
                        SrcFile srcTestFile = new SrcFile(packageName, FileType.MAIN_TEST);
                        sourceFiles.add(srcTestFile);
                        if (!packageName.isEmpty()) {
                            PackageMdFile packageMdFile = new PackageMdFile(packageName, FileType.MAIN);
                            packageMdFiles.add(packageMdFile);
                        }
                        firstPrompt = false;
                    } else if (srcInput.isEmpty() || srcInput.equalsIgnoreCase("f") ||
                            srcInput.equalsIgnoreCase("finish")) {
                        validInput = true;
                        firstPrompt = false;
                    } else {
                        out.println("Invalid input");
                    }
                } while (!validInput);

                out.print("\n");
            } else {
                manifest = new Manifest();
                manifest.setName(guessOrgName());
                manifest.setVersion(DEFAULT_VERSION);
                if (isDirEmpty(projectPath)) {
                    SrcFile srcFile = new SrcFile("", FileType.SERVICE);
                    sourceFiles.add(srcFile);
                }
            }

            InitHandler.initialize(projectPath, manifest, sourceFiles, packageMdFiles);
            out.println("Ballerina project initialized");

        } catch (IOException e) {
            out.println("Error occurred while creating project: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return INIT_COMMAND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Initializes a Ballerina Project. \n");
        out.append("\n");
        out.append("Use --interactive or -i to create a ballerina project in interactive mode.\n");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina init [-i] \n");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelfCmdParser(CommandLine selfCmdParser) {

    }

    /**
     * Validates the version is a semver version.
     *
     * @param versionAsString The version.
     * @return True if valid version, else false.
     */
    private boolean validateVersion(PrintStream out, String versionAsString) {
        String semverRegex = "((?:0|[1-9]\\d*)\\.(?:0|[1-9]\\d*)\\.(?:0|[1-9]\\d*))";
        boolean matches = Pattern.matches(semverRegex, versionAsString);
        if (!matches) {
            out.println("--Invalid version: \"" + versionAsString + "\"");
        }
        return matches;
    }

    private String guessOrgName() {
        String guessOrgName = System.getProperty("user.name");
        if (guessOrgName == null) {
            guessOrgName = "my_org";
        } else {
            guessOrgName = guessOrgName.toLowerCase(Locale.getDefault());
        }
        return guessOrgName;
    }

    /**
     * Validates the org-name.
     *
     * @param orgName The org-name.
     * @return True if valid org-name, else false.
     */
    private boolean validateOrgName(PrintStream out, String orgName) {
        boolean matches = RepoUtils.validateOrg(orgName);
        if (!matches) {
            out.println("--Invalid organization name: \'" + orgName + "\'. Organization name can only contain " +
                                "lowercase alphanumerics and underscores and the maximum length is 256 characters");
        }
        return matches;
    }

    /**
     * Validates the package name.
     *
     * @param pkgName The package name.
     * @return True if valid package name, else false.
     */
    private boolean validatePkgName(PrintStream out, String pkgName) {
        if (pkgName.isEmpty()) {
           return true;
        }
        boolean matches = RepoUtils.validatePkg(pkgName);
        if (!matches) {
            out.println("--Invalid package name: \'" + pkgName + "\'. Package name can only contain " +
                                "alphanumerics, underscores and periods and the maximum length is 256 characters");
        }
        return matches;
    }
}
