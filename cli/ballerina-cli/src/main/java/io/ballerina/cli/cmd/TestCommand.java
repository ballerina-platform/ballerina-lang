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

import com.google.gson.Gson;
import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.TaskExecutor;
import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.cli.task.CleanTargetBinTestsDirTask;
import io.ballerina.cli.task.CleanTargetCacheDirTask;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.cli.task.CreateFingerprintTask;
import io.ballerina.cli.task.CreateTestExecutableTask;
import io.ballerina.cli.task.DumpBuildTimeTask;
import io.ballerina.cli.task.ResolveMavenDependenciesTask;
import io.ballerina.cli.task.ResolveWorkspaceDependenciesTask;
import io.ballerina.cli.task.RunBuildToolsTask;
import io.ballerina.cli.task.RunNativeImageTestTask;
import io.ballerina.cli.task.RunTestsTask;
import io.ballerina.cli.utils.BuildTime;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.projects.internal.model.BuildJson;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;
import org.ballerinalang.test.runtime.entity.TestReport;
import org.ballerinalang.test.runtime.util.CodeCoverageUtils;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.cli.cmd.CommandUtil.TEST_FAILURES_ERROR;
import static io.ballerina.cli.cmd.Constants.TEST_COMMAND;
import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.TestUtils.getReportToolsPath;
import static io.ballerina.projects.util.ProjectUtils.getBalHomePath;
import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectUtils.readBuildJson;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.COVERAGE_DIR;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.FILE_PROTOCOL;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.JACOCO_XML_FORMAT;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_DATA_PLACEHOLDER;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.REPORT_ZIP_NAME;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RESULTS_HTML_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.RESULTS_JSON_FILE;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.TOOLS_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;

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
        this.projectPath = Path.of(System.getProperty(ProjectConstants.USER_DIR));
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

    TestCommand(Path projectPath, boolean exitWhenFinish, Boolean optimizeDependencyCompilation) {
        this.projectPath = projectPath;
        this.optimizeDependencyCompilation = optimizeDependencyCompilation;
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
                Boolean testReport, Boolean coverage, String coverageFormat,
                Boolean optimizeDependencyCompilation) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.testReport = testReport;
        this.coverage = coverage;
        this.coverageFormat = coverageFormat;
        this.optimizeDependencyCompilation = optimizeDependencyCompilation;
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

    @CommandLine.Option(names = "--experimental", description = "Enable experimental language features.")
    private Boolean experimentalFlag;

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

    @CommandLine.Option(names = "--show-dependency-diagnostics", description = "Show the diagnostics " +
            "generated by the dependencies")
    private Boolean showDependencyDiagnostics;

    @CommandLine.Option(names = "--cloud", description = "Enable cloud artifact generation")
    private String cloud;

    @CommandLine.Option(names = "--optimize-dependency-compilation", hidden = true,
            description = "experimental memory optimization for large projects")
    private Boolean optimizeDependencyCompilation;

    @CommandLine.Option(names = "--locking-mode", hidden = true,
            description = "allow passing the package locking mode.")
    private String lockingMode;

    @CommandLine.Option(names = "--min-coverage", description = "minimum code coverage percentage to pass the test")
    private Float minCoverage;

    private static final String testCmd = "bal test [--OPTIONS]\n" +
            "                   [<ballerina-file> | <package-path>] [(-Ckey=value)...]";

    @Override
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
                this.projectPath = Path.of(argList.get(0));
                if (argList.size() > 1) {
                    cliArgs = argList.subList(1, argList.size()).toArray(new String[0]);
                }
            } else {
                cliArgs = argList.toArray(new String[0]);
            }
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
        Path absProjectPath = this.projectPath.toAbsolutePath().normalize();
        try {
            if (buildOptions.dumpBuildTime()) {
                start = System.currentTimeMillis();
                BuildTime.getInstance().timestamp = start;
            }
            if (!ProjectPaths.isBuildProjectRoot(projectPath)
                    && !ProjectPaths.isStandaloneBalFile(projectPath)
                    && !ProjectPaths.isWorkspaceProjectRoot(projectPath)) {
                throw new ProjectException("invalid package path: " + absProjectPath +
                        ". Please provide a valid Ballerina package, workspace or a standalone file.");
            }
            project = ProjectLoader.load(projectPath, buildOptions).project();

            if (buildOptions.dumpBuildTime()) {
                BuildTime.getInstance().projectLoadDuration = System.currentTimeMillis() - start;
            }
        } catch (ProjectException e) {
            CommandUtil.printError(this.errStream, e.getMessage(), testCmd, false);
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
            if (excludes != null && excludes.isEmpty()) {
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

        if (!project.buildOptions().cloud().isEmpty() && project.buildOptions().codeCoverage()) {
            this.outStream.println("WARNING: Code coverage generation is not supported with Ballerina cloud test");
        }

        if (!project.buildOptions().cloud().isEmpty() && this.rerunTests) {
            this.outStream.println("WARNING: Rerun failed tests is not supported with Ballerina cloud test");
        }

        if (!project.buildOptions().cloud().isEmpty() && project.buildOptions().testReport()) {
            this.outStream.println("WARNING: Test report generation is not supported with Ballerina cloud test");
        }

        boolean isTestingDelegated = project.buildOptions().cloud().equals("docker");
        AtomicInteger testResult = new AtomicInteger(0);

        TestReport testReport = null;
        if (project.buildOptions().testReport() || project.buildOptions().codeCoverage()) {
            testReport = new TestReport();
        }

        if (project.kind() == ProjectKind.WORKSPACE_PROJECT) {
            if (testReport != null) {
                testReport.setWorkspaceName(Optional.of(project.sourceRoot().getFileName()).get().toString());
            }
            WorkspaceProject workspaceProject = (WorkspaceProject) project;
            DependencyGraph<BuildProject> projectDependencyGraph = resolveWorkspaceDependencies(workspaceProject);
            if (!project.sourceRoot().equals(absProjectPath)) {
                // If the project path is not the workspace root, filter the topologically sorted list to include only
                // the projects that are dependencies of the project at the specified path.
                Optional<BuildProject> buildProjectOptional = projectDependencyGraph.getNodes().stream()
                        .filter(node -> node.sourceRoot().equals(absProjectPath)).findFirst();
                try {
                    executeTasks(isTestingDelegated, buildProjectOptional.orElseThrow(), testResult,
                            cliArgs, testReport);
                } catch (BLauncherException e) {
                    if (!e.getDetailedMessages().isEmpty()
                            && e.getDetailedMessages().get(0).equals(TEST_FAILURES_ERROR)) {
                        generateTestReport(project, testReport);
                    }
                    throw e;
                }
            } else {
                int execResult = 0;
                for (BuildProject buildProject : projectDependencyGraph.toTopologicallySortedList()) {
                    try {
                        executeTasks(isTestingDelegated, buildProject, testResult, cliArgs, testReport);
                    } catch (BLauncherException e) {
                        if (!e.getDetailedMessages().isEmpty()
                                && !e.getDetailedMessages().get(0).equals(TEST_FAILURES_ERROR)) {
                            throw e;
                        }
                        execResult = 1;
                    }
                }
                if (execResult == 1) {
                    generateTestReport(project, testReport);
                    throw createLauncherException(TEST_FAILURES_ERROR);
                }
            }
        } else {
            if (testReport != null) {
                String projectName = project.currentPackage().packageName().toString();
                if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                    projectName = Optional.of(project.sourceRoot().getFileName()).get().toString();
                }
                testReport.setWorkspaceName(projectName);
            }
            try {
                executeTasks(isTestingDelegated, project, testResult, cliArgs, testReport);
            } catch (BLauncherException e) {
                if (!e.getDetailedMessages().isEmpty()
                        && e.getDetailedMessages().get(0).equals(TEST_FAILURES_ERROR)) {
                    generateTestReport(project, testReport);
                }
                throw e;
            }
        }
        generateTestReport(project, testReport);

        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    private void generateTestReport(Project project, TestReport report) {
        if (!project.buildOptions().testReport() && !project.buildOptions().codeCoverage()) {
            return;
        }
        if (report == null || report.getPackages().isEmpty()) {
            // no tests found
            return;
        }
        outStream.println();
        outStream.println("Generating Test Report");
        report.finalizeTestResults(project.buildOptions().codeCoverage());
        Gson gson = new Gson();
        String json;
        if (project.kind() == ProjectKind.WORKSPACE_PROJECT) {
            json = gson.toJson(report);
        } else {
            json = gson.toJson(report.getPackages().get(0));
        }

        Path reportDir;
        try {
            Files.createDirectories(project.targetDir());
            Target target = new Target(project.targetDir());
            reportDir = target.getReportPath();
            File jsonFile = new File(reportDir.resolve(RESULTS_JSON_FILE).toString());
            try (FileOutputStream fileOutputStream = new FileOutputStream(jsonFile)) {
                try (Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                    writer.write(json);
                    outStream.println("\t" + jsonFile.toPath() + "\n");
                }
            }
        } catch (IOException e) {
            throw createLauncherException("error occurred while generating test report: " + e);
        }

        // Dump the Testerina html report only if '--test-report' flag is provided
        Path reportZipPath = getReportToolsPath();
        if (Files.exists(reportZipPath)) {
            String content;
            try {
                try (FileInputStream fileInputStream = new FileInputStream(reportZipPath.toFile())) {
                    CodeCoverageUtils.unzipReportResources(fileInputStream, reportDir.toFile());
                }
                content = Files.readString(reportDir.resolve(RESULTS_HTML_FILE));
                content = content.replace(REPORT_DATA_PLACEHOLDER, json);
            } catch (IOException e) {
                throw createLauncherException("error occurred while preparing test report: " + e);
            }

            try {
                File htmlFile = new File(reportDir.resolve(RESULTS_HTML_FILE).toString());
                try (FileOutputStream fileOutputStream = new FileOutputStream(htmlFile)) {
                    try (Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                        writer.write(content);
                        outStream.println("\tView the test report at: " +
                                FILE_PROTOCOL + Path.of(htmlFile.getPath()).toAbsolutePath().normalize());
                    }
                }
            } catch (IOException e) {
                throw createLauncherException("error occurred while writing test report to file: " + e);
            }
        } else {
            String reportToolsPath = getBalHomePath().resolve(BALLERINA_HOME_LIB).resolve(TOOLS_DIR_NAME)
                    .resolve(COVERAGE_DIR).resolve(REPORT_ZIP_NAME).toString();
            outStream.println("warning: Could not find the required HTML report tools for code coverage at "
                    + reportToolsPath);
        }
    }

    private void executeTasks(boolean isTestingDelegated, Project project, AtomicInteger testResult,
                              String[] cliArgs, TestReport testReport) {
        Path buildFilePath = project.targetDir().resolve(BUILD_FILE);
        boolean rebuildStatus = true;
        String prevTestClassPath  = "";
        BuildJson buildJson;
        try {
            buildJson = readBuildJson(buildFilePath);
            if (buildJson.getTestClassPath() != null) {
                prevTestClassPath = buildJson.getTestClassPath();
            }

            rebuildStatus = isRebuildNeeded(project, buildJson) || prevTestClassPath == null
                    || prevTestClassPath.isEmpty();
        } catch (IOException e) {
            // ignore exception
        }
        // Run pre-build tasks to have the project reloaded.
        // In code coverage generation, the module map is duplicated.
        // Therefore, the project needs to be reloaded beforehand to provide the latest project instance
        // which has the newly generated code for code coverage calculation.
        // Hence, below tasks are executed before extracting the module map from the project.
        boolean isSingleFile = project.kind().equals(ProjectKind.SINGLE_FILE_PROJECT);
        TaskExecutor preBuildTaskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetCacheDirTask(),
                        !rebuildStatus || isSingleFile) // clean the target cache dir(projects only)
                .addTask(new CleanTargetBinTestsDirTask(), !rebuildStatus || (isSingleFile || !isTestingDelegated))
                .addTask(new RunBuildToolsTask(outStream), !rebuildStatus || isSingleFile) // run build tools
                .build();
        preBuildTaskExecutor.executeTasks(project);

        Iterable<Module> originalModules = project.currentPackage().modules();
        Map<String, Module> moduleMap = new HashMap<>();

        for (Module originalModule : originalModules) {
            moduleMap.put(originalModule.moduleName().toString(), originalModule);
        }

        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                // resolve maven dependencies in Ballerina.toml
                .addTask(new ResolveMavenDependenciesTask(outStream, !rebuildStatus))
                // compile the modules
                .addTask(new CompileTask(outStream, errStream, false, false, !rebuildStatus))
                .addTask(new CreateTestExecutableTask(outStream, groupList, disableGroupList, testList, listGroups,
                        cliArgs, isParallelExecution), !isTestingDelegated)
                .addTask(new RunTestsTask(outStream, errStream, rerunTests, groupList, disableGroupList,
                                testList, includes, coverageFormat, moduleMap, listGroups, excludes, cliArgs,
                                isParallelExecution, rebuildStatus, prevTestClassPath, testResult, minCoverage,
                                testReport),
                        (project.buildOptions().nativeImage() || isTestingDelegated))
                .addTask(new RunNativeImageTestTask(outStream, rerunTests, groupList, disableGroupList,
                                testList, includes, coverageFormat, moduleMap, listGroups, isParallelExecution,
                                testReport),
                        (!project.buildOptions().nativeImage() || isTestingDelegated))
                .addTask(new DumpBuildTimeTask(outStream), !project.buildOptions().dumpBuildTime())
                .addTask(new CreateFingerprintTask(true, false),
                        !rebuildStatus || isSingleFile)
                .build();
        taskExecutor.executeTasks(project);
    }

    private DependencyGraph<BuildProject> resolveWorkspaceDependencies(WorkspaceProject workspaceProject) {
        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new ResolveWorkspaceDependenciesTask(outStream))
                .build();
        taskExecutor.executeTasks(workspaceProject);
        return workspaceProject.getResolution().dependencyGraph();
    }

    private boolean isRebuildNeeded(Project project, BuildJson buildJson) {
        try {
            if (!Objects.equals(buildJson.distributionVersion(), RepoUtils.getBallerinaVersion())) {
                return true;
            }
            if (buildJson.isExpiredLastUpdateTime()) {
                return true;
            }
            if (CommandUtil.isFilesModifiedSinceLastBuild(buildJson, project, true, true)) {
                return true;
            }
            if (isRebuildForCurrCmd()) {
                return true;
            }
            //TODO :  Need to investigate further
            if (!"".equals(project.buildOptions().cloud()) || project.buildOptions().nativeImage() ||
                    project.buildOptions().codeCoverage()) {
                return true;
            }
            return CommandUtil.isPrevCurrCmdCompatible(project.buildOptions(), buildJson.getBuildOptions());
        } catch (IOException e) {
            // ignore
        }
        return true;
    }

    private boolean isRebuildForCurrCmd() {
        return  this.dumpGraph
                || this.dumpRawGraphs
                || this.targetDir != null
                || Boolean.TRUE.equals(this.disableSyntaxTreeCaching)
                || Boolean.TRUE.equals(this.dumpBuildTime)
                || Boolean.TRUE.equals(this.showDependencyDiagnostics);
    }

    private BuildOptions constructBuildOptions() {
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();

        buildOptionsBuilder
                .setCodeCoverage(coverage)
                .setExperimental(experimentalFlag)
                .setOffline(offline)
                .setSkipTests(false)
                .setTestReport(testReport)
                .setObservabilityIncluded(observabilityIncluded)
                .setDumpBuildTime(dumpBuildTime)
                .setSticky(sticky)
                .setCloud(cloud)
                .setDumpGraph(dumpGraph)
                .setDumpRawGraphs(dumpRawGraphs)
                .setNativeImage(nativeImage)
                .disableSyntaxTreeCaching(disableSyntaxTreeCaching)
                .setGraalVMBuildOptions(graalVMBuildOptions)
                .setShowDependencyDiagnostics(showDependencyDiagnostics)
                .setOptimizeDependencyCompilation(optimizeDependencyCompilation)
                .setLockingMode(lockingMode);

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
