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
import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.cli.task.InstallDependenciesTask;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectLoadResult;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.ballerina.cli.cmd.Constants.INIT_COMMAND;
import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

/**
 * Init command for initializing the dependencies of a package or of the packages of a workspace: pulls them,
 * compiles them into the dependency cache and writes Dependencies.toml. A package that already has a
 * Dependencies.toml is not initialized again.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = INIT_COMMAND, description = "Initialize the dependencies of the package", hidden = true)
public class InitCommand implements BLauncherCmd {

    private final Path userDir;
    private final PrintStream outStream;
    private final PrintStream errStream;
    private final boolean exitWhenFinish;
    private final boolean offline;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Parameters(hidden = true)
    private List<String> argList;


    public InitCommand() {
        this.userDir = Path.of(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
        this.offline = false;
    }

    public InitCommand(Path userDir, PrintStream printStream, boolean exitWhenFinish) {
        this(userDir, printStream, exitWhenFinish, false);
    }

    /**
     * Creates the command with the dependencies resolved from the local repositories only, for tests.
     */
    InitCommand(Path userDir, PrintStream printStream, boolean exitWhenFinish, boolean offline) {
        this.userDir = userDir;
        this.outStream = printStream;
        this.errStream = printStream;
        this.exitWhenFinish = exitWhenFinish;
        this.offline = offline;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            errStream.println(BLauncherCmd.getCommandUsageInfo(INIT_COMMAND));
            return;
        }

        if (argList != null && !argList.isEmpty()) {
            if (argList.size() == 1 && argList.get(0).endsWith(ProjectConstants.BLANG_SOURCE_EXT)) {
                CommandUtil.printError(errStream, "initializing the dependencies of a standalone Ballerina file is "
                        + "not supported. Run 'bal init' inside a Ballerina package.", null, false);
            } else {
                CommandUtil.printError(errStream, "too many arguments", "bal init", false);
            }
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        Path projectRoot = ProjectUtils.findProjectRoot(this.userDir);
        if (projectRoot == null) {
            CommandUtil.printError(errStream, "not a Ballerina package: " + this.userDir.toAbsolutePath()
                    + ". Use 'bal new .' to create one.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // A package without Dependencies.toml is resolved as a new package: each dependency is the latest version
        // with the major version given in Ballerina.toml.
        BuildOptions buildOptions = BuildOptions.builder().setOffline(offline)
                .setLockingMode(PackageLockingMode.SOFT).build();
        Project project;
        try {
            ProjectLoadResult loadResult = ProjectLoader.load(projectRoot, buildOptions);
            if (loadResult.diagnostics().hasErrors()) {
                loadResult.diagnostics().errors().forEach(errStream::println);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            project = loadResult.project();
        } catch (ProjectException e) {
            CommandUtil.printError(errStream, e.getMessage(), null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        RepoUtils.readSettings();

        if (project instanceof WorkspaceProject workspaceProject) {
            initWorkspace(workspaceProject, projectRoot);
        } else if (!isInitialized((BuildProject) project)) {
            new InstallDependenciesTask(outStream, errStream).execute(project);
        }
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    /**
     * Initializes the packages of a workspace, as a build of the given path builds them: all packages from the
     * workspace root, else the package at the path and the workspace packages it depends on.
     */
    private void initWorkspace(WorkspaceProject workspaceProject, Path projectRoot) {
        DependencyGraph<BuildProject> graph = CommandUtil.resolveWorkspaceDependencies(workspaceProject);
        List<BuildProject> packages = new ArrayList<>(graph.toTopologicallySortedList());
        if (!workspaceProject.sourceRoot().equals(projectRoot)) {
            BuildProject target = packages.stream().filter(member -> member.sourceRoot().equals(projectRoot))
                    .findFirst().orElseThrow(() -> createLauncherException("package is not a member of the "
                            + "workspace: " + projectRoot));
            Collection<BuildProject> dependencies = graph.getAllDependencies(target);
            packages.removeIf(member -> member != target && !dependencies.contains(member));
        }

        // A package that fails does not stop the others from being initialized.
        List<String> failed = new ArrayList<>();
        for (BuildProject member : packages) {
            if (isInitialized(member)) {
                continue;
            }
            outStream.println("Initializing " + packageName(member));
            try {
                new InstallDependenciesTask(outStream, errStream).execute(member);
            } catch (BLauncherException e) {
                e.getDetailedMessages().forEach(message -> errStream.println("error: " + message));
                failed.add(packageName(member));
            }
        }
        if (!failed.isEmpty()) {
            throw createLauncherException("failed to initialize the dependencies of " + String.join(", ", failed));
        }
    }

    /**
     * The dependencies of a package are initialized once. Builds keep Dependencies.toml up to date afterwards, so an
     * initialized package is skipped, which is not a failure.
     */
    private boolean isInitialized(BuildProject project) {
        if (!Files.exists(project.sourceRoot().resolve(ProjectConstants.DEPENDENCIES_TOML))) {
            return false;
        }
        outStream.println("Package '" + packageName(project) + "' is already initialized: "
                + ProjectConstants.DEPENDENCIES_TOML + " exists. Use 'bal build' to update its dependencies.");
        return true;
    }

    private static String packageName(BuildProject project) {
        return project.currentPackage().packageOrg() + "/" + project.currentPackage().packageName();
    }

    @Override
    public String getName() {
        return INIT_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(INIT_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal init \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
