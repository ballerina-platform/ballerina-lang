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
import org.ballerinalang.packerina.init.models.ModuleMdFile;
import org.ballerinalang.packerina.init.models.SrcFile;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.ballerinalang.packerina.cmd.Constants.INIT_COMMAND;

/**
 * Init command for creating a ballerina project.
 */
@CommandLine.Command(name = INIT_COMMAND, description = "initialize a Ballerina project")
public class InitCommand implements BLauncherCmd {

    public static final String DEFAULT_VERSION = "0.0.1";
    private static final String USER_DIR = "user.dir";
    private static final PrintStream errStream = System.err;
    private final Path homePath = RepoUtils.createAndGetHomeReposPath();
    private boolean alreadyInitializedProject = false;
    private PrintStream out = System.out;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    private static boolean isDirEmpty(final Path directory) throws IOException {

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            //Check whether the OS is MacOS and the folder contains .DS_Store file
            Iterator pathIterator = dirStream.iterator();
            if (!pathIterator.hasNext()) {
                return true;
            }
            Path path = (Path) pathIterator.next();
            Path fileName = path.getFileName();
            return fileName != null && fileName.toString().equals(ProjectDirConstants.DS_STORE_FILE) &&
                    !pathIterator.hasNext();
        }
    }

    @Override
    public void execute() {
        // Get source root path.
        Path projectPath = Paths.get(System.getProperty(USER_DIR));
        try {
            // Check if it is a project
            boolean isProject = Files.exists(projectPath.resolve(ProjectDirConstants.DOT_BALLERINA_DIR_NAME));
            if (isProject) {
                alreadyInitializedProject = true;
                Files.exists(projectPath.resolve(ProjectDirConstants.MANIFEST_FILE_NAME));
            }
            // If the current directory is not a project traverse and check down and up
            if (!alreadyInitializedProject) {
                // Recursively traverse down
                Optional<Path> childDotBallerina = Files.walk(projectPath)
                        .filter(path -> Files.isDirectory(path) &&
                                path.toFile().getName().equals(ProjectDirConstants.DOT_BALLERINA_DIR_NAME))
                        .findFirst();
                if (childDotBallerina.isPresent()) {
                    errStream.println("A ballerina project is already initialized in " +
                            childDotBallerina.get().toFile().getParent());
                    return;
                }
                // Recursively traverse up till the root
                Path projectRoot = findProjectRoot(projectPath);
                if (projectRoot != null) {
                    errStream.println("Directory is already within a ballerina project :" + projectRoot.toString());
                    return;
                }
            }
        } catch (IOException ignore) {
        }

        try {
            Manifest manifest = null;

            if (helpFlag) {
                String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(INIT_COMMAND);
                errStream.println(commandUsageInfo);
                return;
            }

            List<SrcFile> sourceFiles = new ArrayList<>();
            List<ModuleMdFile> moduleMdFiles = new ArrayList<>();

            manifest = new Manifest();
            manifest.setName(guessOrgName());
            manifest.setVersion(DEFAULT_VERSION);
            if (isDirEmpty(projectPath)) {
                SrcFile srcFile = new SrcFile("", FileType.SERVICE);
                sourceFiles.add(srcFile);
            }

            InitHandler.initialize(projectPath, manifest, sourceFiles, moduleMdFiles);
            if (!alreadyInitializedProject) {
                out.println("Ballerina project initialized");
            } else {
                out.println("Ballerina project reinitialized");
            }

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
     * Find the project root by recursively up to the root.
     *
     * @param projectDir project path
     * @return project root
     */
    private Path findProjectRoot(Path projectDir) {
        Path path = projectDir.resolve(ProjectDirConstants.DOT_BALLERINA_DIR_NAME);
        if (!path.equals(homePath) && java.nio.file.Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            return projectDir;
        }
        Path parentsParent = projectDir.getParent();
        if (null != parentsParent) {
            return findProjectRoot(parentsParent);
        }
        return null;
    }
}
