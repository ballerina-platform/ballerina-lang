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
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.semver.checker.SemverChecker;
import io.ballerina.semver.checker.exception.SemverToolException;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to implement `semver` command for Ballerina.
 * Ex: `bal semver`
 *
 * @since 2201.2.0
 */
@CommandLine.Command(name = "semver", description = "check semver compliance between the source code changes and " +
        "the package version")
public class SemverCmd implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private static final String CMD_NAME = "semver";

    @SuppressWarnings("unused")
    @CommandLine.Parameters(arity = "0..1")
    private final Path projectPath;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-c", "--compare-version"}, description = "released version number which the current" +
            " changes should be compared against (If not provided, the tool is expected to resolve the most " +
            "compatible version available in the central).")
    private String prevVersion;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-d", "--show-diff"}, description = "show the full list of changes between the " +
            "compared versions")
    private boolean showDiff;

    public SemverCmd() {
        this.projectPath = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.err;
        this.errStream = System.err;
    }

    @Override
    public void execute() {
        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(CMD_NAME);
            this.errStream.println(commandUsageInfo);
            return;
        }

        try {
            SemanticVersion semanticVersion = null;
            if (prevVersion != null) {
                try {
                    semanticVersion = SemanticVersion.from(prevVersion);
                } catch (ProjectException e) {
                    throw new SemverToolException("invalid version '" + prevVersion + "' provided: ", e);
                } catch (Exception e) {
                    throw new SemverToolException("unhandled exception occurred while parsing the provided version '" +
                            prevVersion + "'", e);
                }
            }

            SemverChecker semverChecker = new SemverChecker(projectPath, semanticVersion);
            if (showDiff) {
                outStream.println(semverChecker.getDiffSummary());
            } else {
                outStream.println(semverChecker.getVersionSuggestionSummary());
            }
        } catch (InvalidPathException e) {
            throw LauncherUtils.createLauncherException("invalid project path provided for the semver tool: ", e);
        } catch (SemverToolException e) {
            throw LauncherUtils.createLauncherException(e.getMessage());
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
