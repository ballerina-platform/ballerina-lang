/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.cli.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.cli.cmd.Constants.CLEAN_COMMAND;

/**
 * This class represents the "bal clean" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = CLEAN_COMMAND, description = "Clean the artifacts generated during the build")
public class CleanCommand implements BLauncherCmd {
    private final PrintStream outStream;
    private final Path projectPath;
    private boolean exitWhenFinish;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--target-dir", description = "target directory path")
    private Path targetDir;

    public CleanCommand(Path projectPath, boolean exitWhenFinish) {
        this.projectPath = projectPath;
        this.outStream = System.out;
        this.exitWhenFinish = exitWhenFinish;
    }

    public CleanCommand() {
        this.projectPath = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.out;
        this.exitWhenFinish = true;
    }

    public CleanCommand(Path projectPath, PrintStream printStream, boolean exitWhenFinish, Path targetDir) {
        this.projectPath = projectPath;
        this.outStream =  printStream;
        this.exitWhenFinish = exitWhenFinish;
        this.targetDir = targetDir;
    }
    
    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(CLEAN_COMMAND);
            this.outStream.println(commandUsageInfo);
            return;
        }

        if (this.targetDir == null) {
            try {
                Project project = BuildProject.load(this.projectPath);
                this.targetDir = project.targetDir();
            } catch (ProjectException e) {
                CommandUtil.printError(this.outStream, e.getMessage(), null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        // Delete the target directory
        if (Files.notExists(this.targetDir)) {
            CommandUtil.printError(this.outStream,
                    "provided target directory '" + this.targetDir + "' does not exist.",
                    null, false);
        } else if (!Files.isDirectory(this.targetDir)) {
            CommandUtil.printError(this.outStream,
                    "provided target path '" + this.targetDir + "' is not a directory.",
                    null, false);
        } else {
            ProjectUtils.deleteDirectory(this.targetDir);
            this.outStream.println("Successfully deleted '" + this.targetDir + "'.");
        }

        //delete the generated directory
        Path generatedDir;
        try {
            Project project = BuildProject.load(this.projectPath);
            generatedDir = project.sourceRoot().resolve(ProjectConstants.GENERATED_MODULES_ROOT);
        } catch (ProjectException e) {
            CommandUtil.printError(this.outStream, e.getMessage(), null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (Files.notExists(generatedDir)) {
            this.outStream.println("Existing generated directory was not found");
        } else {
            ProjectUtils.deleteDirectory(generatedDir);
            this.outStream.println("Successfully deleted '" + generatedDir + "'.");
        }
    }
    
    @Override
    public String getName() {
        return CLEAN_COMMAND;
    }
    
    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(CLEAN_COMMAND));
    }
    
    @Override
    public void printUsage(StringBuilder out) {
        out.append(" bal clean \n");
    }
    
    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
