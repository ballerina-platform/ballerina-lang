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
import io.ballerina.cli.TaskExecutor;
import io.ballerina.cli.task.CleanTargetCacheDirTask;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.cli.task.DumpBuildTimeTask;
import io.ballerina.cli.task.ResolveMavenDependenciesTask;
import io.ballerina.cli.task.RunBuildToolsTask;
import io.ballerina.cli.task.RunNativeImageTestTask;
import io.ballerina.cli.task.RunTestsTask;
import io.ballerina.cli.utils.BuildTime;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.cli.cmd.Constants.TEST_COMMAND;
import static io.ballerina.projects.util.ProjectUtils.isProjectUpdated;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.JACOCO_XML_FORMAT;

/**
 * This class represents the "bal test" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = TEST_COMMAND, description = "Run package tests")
public class TestCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private Path projectPath;
    private final boolean exitWhenFinish;

    public TestCommand() {
        this.projectPath = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    TestCommand(Path projectPath, boolean exitWhenFinish) {
        this.projectPath = projectPath;
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = exitWhenFinish;
        this.offline = true;
    }

    TestCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.offline = true;
    }

    TestCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                       Boolean testReport, Boolean coverage, String coverageFormat) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.testReport = testReport;
        this.coverage = coverage;
        this.coverageFormat = coverageFormat;
        this.offline = true;
    }

    TestCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                       Boolean testReport, Path targetDir) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.testReport = testReport;
        this.targetDir = targetDir;
        this.offline = true;
    }

    TestCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                boolean nativeImage, String graalVMBuildOptions) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.nativeImage = nativeImage;
        this.offline = true;
        this.graalVMBuildOptions = graalVMBuildOptions;
    }

    @CommandLine.Option(names = {"--offline"}, description = "Run package tests")
    private Boolean offline;

    @CommandLine.Parameters(description = "Program arguments")
    private List<String> argList = new ArrayList<>();

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--debug", description = "start in remote debugging mode")
    private String debugPort;

    @CommandLine.Option(names = "--parallel", description = "enable parallel execution of tests",
            defaultValue = "false")
    private boolean isParallelExecution;

    @CommandLine.Option(names = "--list-groups", description = "list the groups available in the tests")
    private boolean listGroups;

    @CommandLine.Option(names = "--groups", description = "test groups to be executed")
    private String groupList;

    @CommandLine.Option(names = "--disable-groups", description = "test groups to be disabled")
    private String disableGroupList;

    @CommandLine.Option(names = "--test-report", description = "enable test report generation")
    private Boolean testReport;

    @CommandLine.Option(names = "--code-coverage", description = "enable code coverage")
    private Boolean coverage;

    @CommandLine.Option(names = "--coverage-format", description = "list of supported coverage report formats")
    private String coverageFormat;

    @CommandLine.Option(names = "--observability-included", description = "package observability in the executable.")
    private Boolean observabilityIncluded;

    @CommandLine.Option(names = "--tests", description = "Test functions to be executed")
    private String testList;

    @CommandLine.Option(names = "--rerun-failed", description = "Rerun failed tests.")
    private boolean rerunTests;

    @CommandLine.Option(names = "--includes", hidden = true,
            description = "hidden option for code coverage to include all classes")
    private String includes;

    @CommandLine.Option(names = "--dump-build-time", description = "calculate and dump build time", hidden = true)
    private Boolean dumpBuildTime;

    @CommandLine.Option(names = "--sticky", description = "stick to exact versions locked (if exists)")
    private Boolean sticky;

    @CommandLine.Option(names = "--target-dir", description = "target directory path")
    private Path targetDir;

    @CommandLine.Option(names = "--dump-graph", description = "Print the dependency graph.", hidden = true)
    private boolean dumpGraph;

    @CommandLine.Option(names = "--dump-raw-graphs", description = "Print all intermediate graphs created in the " +
            "dependency resolution process.", hidden = true)
    private boolean dumpRawGraphs;

    @CommandLine.Option(names = "--enable-cache", description = "enable caches for the compilation", hidden = true)
    private Boolean enableCache;

    @CommandLine.Option(names = "--graalvm", description = "enable running test suite against native image")
    private Boolean nativeImage;

    @CommandLine.Option(names = "--excludes", description = "option to exclude source files/folders from code coverage")
    private String excludes;

    @CommandLine.Option(names = "--disable-syntax-tree-caching", hidden = true, description = "disable syntax tree " +
            "caching for source files", defaultValue = "false")
    private Boolean disableSyntaxTreeCaching;

    @CommandLine.Option(names = "--graalvm-build-options", description = "additional build options for native image " +
            "generation")
    private String graalVMBuildOptions;


    private static final String testCmd = "bal test [--OPTIONS]\n" +
            "                   [<ballerina-file> | <package-path>] [(-Ckey=value)...]";

    public void execute() {
        long start = 0;
        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TEST_COMMAND);
            this.errStream.println(commandUsageInfo);
            return;
        }

        String[] cliArgs = new String[0];
        if (!argList.isEmpty()) {
            if (!argList.get(0).matches(ProjectConstants.CONFIG_ARGS_PATTERN)) {
                this.projectPath = Paths.get(argList.get(0));
                if (argList.size() > 1) {
                    cliArgs = argList.subList(1, argList.size()).toArray(new String[0]);
                }
            } else {
                cliArgs = argList.toArray(new String[0]);
            }
        }

        if (sticky == null) {
            sticky = false;
        }
        if (isParallelExecution) {
            this.outStream.println("WARNING: Running tests in parallel is an experimental feature");
        }

        // load project
        Project project;

        // Skip code coverage for single bal files if option is set
        if (FileUtils.hasExtension(this.projectPath)) {
            if (coverage != null && coverage) {
                this.outStream.println("Code coverage is not yet supported with single bal files. Ignoring the flag " +
                        "and continuing the test run...");
            }
            coverage = false;
            testReport = false;
        }
        BuildOptions buildOptions = constructBuildOptions();

        boolean isSingleFile = false;
        if (FileUtils.hasExtension(this.projectPath)) {
            try {
                if (buildOptions.dumpBuildTime()) {
                    start = System.currentTimeMillis();
                    BuildTime.getInstance().timestamp = start;
                }
                project = SingleFileProject.load(this.projectPath, buildOptions);
                if (buildOptions.dumpBuildTime()) {
                    BuildTime.getInstance().projectLoadDuration = System.currentTimeMillis() - start;
                }
            } catch (ProjectException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), testCmd, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            isSingleFile = true;
        } else {
            try {
                if (buildOptions.dumpBuildTime()) {
                    start = System.currentTimeMillis();
                    BuildTime.getInstance().timestamp = start;
                }
                project = BuildProject.load(this.projectPath, buildOptions);
                if (buildOptions.dumpBuildTime()) {
                    BuildTime.getInstance().projectLoadDuration = System.currentTimeMillis() - start;
                }
            } catch (ProjectException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), testCmd, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        // If project is empty
        if (ProjectUtils.isProjectEmpty(project)) {
            CommandUtil.printError(this.errStream, "package is empty. Please add at least one .bal file.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Sets the debug port as a system property, which will be used when setting up debug args before running tests.
        if (this.debugPort != null) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, this.debugPort);
        }
        //Display warning if any other options are provided with list-groups flag.
        if (listGroups && (rerunTests || coverage != null || testReport != null || groupList != null ||
                disableGroupList != null || testList != null)) {
            this.outStream.println("\nWarning: Other flags are skipped when list-groups flag is provided.\n");
        }

        if (project.buildOptions().codeCoverage()) {
            if (coverageFormat != null) {
                if (!coverageFormat.equals(JACOCO_XML_FORMAT)) {
                    String errMsg = "unsupported coverage report format '" + coverageFormat + "' found. Only '" +
                            JACOCO_XML_FORMAT + "' format is supported.";
                    CommandUtil.printError(this.errStream, errMsg, null, false);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return;
                }
            }
            if (excludes != null && excludes.equals("")) {
                this.outStream.println("warning: ignoring --excludes flag since given exclusion list is empty");
            }
        } else {
            // Skip --includes flag if it is set without code coverage
            if (includes != null) {
                this.outStream.println("warning: ignoring --includes flag since code coverage is not enabled");
            }
            // Skip --coverage-format flag if it is set without code coverage
            if (coverageFormat != null) {
                this.outStream.println("warning: ignoring --coverage-format flag since code coverage is not " +
                        "enabled");
            }
            if (excludes != null) {
                this.outStream.println("warning: ignoring --excludes flag since code coverage is not enabled");
            }
        }

        if (project.buildOptions().nativeImage() && project.buildOptions().codeCoverage()) {
            this.outStream.println("WARNING: Code coverage generation is not supported with Ballerina native test");
        }

        if (!project.buildOptions().nativeImage() && !project.buildOptions().graalVMBuildOptions().isEmpty()) {
            this.outStream.println("WARNING: Additional GraalVM build options are ignored since graalvm " +
                    "flag is not set");
        }

        // Run pre-build tasks to have the project reloaded.
        // In code coverage generation, the module map is duplicated.
        // Therefore, the project needs to be reloaded beforehand to provide the latest project instance
        // which has the newly generated code for code coverage calculation.
        // Hence, below tasks are executed before extracting the module map from the project.
        TaskExecutor preBuildTaskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetCacheDirTask(), isSingleFile) // clean the target cache dir(projects only)
                .addTask(new RunBuildToolsTask(outStream), isSingleFile) // run build tools
                .build();
        preBuildTaskExecutor.executeTasks(project);

        Iterable<Module> originalModules = project.currentPackage().modules();
        Map<String, Module> moduleMap = new HashMap<>();

        for (Module originalModule : originalModules) {
            moduleMap.put(originalModule.moduleName().toString(), originalModule);
        }

        // Check package files are modified after last build
        boolean isPackageModified = isProjectUpdated(project);

        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new ResolveMavenDependenciesTask(outStream)) // resolve maven dependencies in Ballerina.toml
                // compile the modules
                .addTask(new CompileTask(outStream, errStream, false, false,
                        isPackageModified, buildOptions.enableCache()))
//                .addTask(new CopyResourcesTask(), listGroups) // merged with CreateJarTask
                .addTask(new RunTestsTask(outStream, errStream, rerunTests, groupList, disableGroupList, testList,
                        includes, coverageFormat, moduleMap, listGroups, excludes, cliArgs, isParallelExecution),
                        project.buildOptions().nativeImage())
                .addTask(new RunNativeImageTestTask(outStream, rerunTests, groupList, disableGroupList,
                        testList, includes, coverageFormat, moduleMap, listGroups, isParallelExecution),
                        !project.buildOptions().nativeImage())
                .addTask(new DumpBuildTimeTask(outStream), !project.buildOptions().dumpBuildTime())
                .build();

        taskExecutor.executeTasks(project);
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    private BuildOptions constructBuildOptions() {
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();

        buildOptionsBuilder
                .setCodeCoverage(coverage)
                .setOffline(offline)
                .setSkipTests(false)
                .setTestReport(testReport)
                .setObservabilityIncluded(observabilityIncluded)
                .setDumpBuildTime(dumpBuildTime)
                .setSticky(sticky)
                .setDumpGraph(dumpGraph)
                .setDumpRawGraphs(dumpRawGraphs)
                .setNativeImage(nativeImage)
                .setEnableCache(enableCache)
                .disableSyntaxTreeCaching(disableSyntaxTreeCaching)
                .setGraalVMBuildOptions(graalVMBuildOptions);


        if (targetDir != null) {
            buildOptionsBuilder.targetDir(targetDir.toString());
        }

        return buildOptionsBuilder.build();
    }

    @Override
    public String getName() {
        return TEST_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(TEST_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append(testCmd + "\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

}
