/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.semver.checker.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.launcher.LauncherUtils;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.semver.checker.SemverChecker;
import io.ballerina.semver.checker.exception.SemverToolException;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Class to implement `semver` command for Ballerina.
 * Ex: `bal semver [--help|-h]`
 *
 * @since 2201.2.0
 */
@CommandLine.Command(name = "semver", description = "check semver compliance between the source code changes and " +
        "the package version")
public class SemverCmd implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private Path projectPath;
    private static final String CMD_NAME = "semver";

    @SuppressWarnings("unused")
    @CommandLine.Parameters
    private List<String> argList;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-v", "--suggest-version"})
    private boolean suggestVersion;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-d", "--diff"})
    private boolean showDiff;

    public SemverCmd() {
        this.projectPath = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.err;
        this.errStream = System.err;
    }

    @Override
    public void execute() {
        if (this.helpFlag) {
            // Todo: add help flag info
            //  String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(RUN_COMMAND);
            // this.errStream.println(commandUsageInfo);
            return;
        }

        try {
            if (argList != null && !argList.isEmpty()) {
                this.projectPath = Paths.get(argList.get(0));
            }

            SemverChecker semverChecker = new SemverChecker(projectPath);

            if (showDiff) {
                outStream.println(semverChecker.getCompatibilitySummary());
            }
            if (suggestVersion) {
                outStream.println(semverChecker.suggestVersion());
            }

        } catch (InvalidPathException e) {
            throw LauncherUtils.createLauncherException("invalid project path provided for the semver tool: ", e);
        } catch (SemverToolException e) {
            throw LauncherUtils.createLauncherException("", e);
        } catch (Throwable t) {
            throw LauncherUtils.createLauncherException("semver checker execution failed due to an unhandled " +
                    "exception: ", t);
        }
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder out) {

    }

    @Override
    public void printUsage(StringBuilder out) {

    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {

    }
}
