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
import io.ballerina.cli.task.CleanTargetDirTask;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.cli.task.CreateBalaTask;
import io.ballerina.cli.task.CreateExecutableTask;
import io.ballerina.cli.task.DumpBuildTimeTask;
import io.ballerina.cli.task.ResolveMavenDependenciesTask;
import io.ballerina.cli.task.RunTestsTask;
import io.ballerina.cli.utils.BuildTime;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.cli.cmd.Constants.BUILD_COMMAND;
import static io.ballerina.projects.internal.ManifestBuilder.getStringValueFromTomlTableNode;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.JACOCO_XML_FORMAT;

/**
 * This class represents the "bal build" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = BUILD_COMMAND, description = "bal build - Build Ballerina module(s) and generate " +
                                                         "executable output.")
public class BuildCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private boolean exitWhenFinish;
    private boolean skipCopyLibsFromDist;

    public BuildCommand() {
        this.projectPath = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
        this.skipCopyLibsFromDist = false;
    }

    public BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean dumpBuildTime) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = false;
        this.dumpBuildTime = dumpBuildTime;
    }

    public BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                        boolean skipCopyLibsFromDist) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
    }

    public BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                        boolean skipCopyLibsFromDist, boolean compile) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
        this.compile = compile;
    }

    public BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                        boolean skipCopyLibsFromDist, Boolean skipTests, Boolean testReport, Boolean coverage,
                        String coverageFormat) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
        this.skipTests = skipTests;
        this.testReport = testReport;
        this.coverage = coverage;
        this.coverageFormat = coverageFormat;
    }

    public BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                        boolean skipCopyLibsFromDist, String output) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
        this.output = output;
    }

    @CommandLine.Option(names = {"--compile", "-c"}, description = "Compile the source without generating " +
                                                                   "executable(s).")
    private boolean compile;

    @CommandLine.Option(names = {"--output", "-o"}, description = "Write the output to the given file. The provided " +
                                                                  "output file name may or may not contain the " +
                                                                  "'.jar' extension.")
    private String output;

    @CommandLine.Option(names = {"--offline"}, description = "Build/Compile offline without downloading " +
                                                              "dependencies.")
    private Boolean offline;

    @CommandLine.Option(names = {"--skip-tests"}, description = "Skip test compilation and execution.")
    private Boolean skipTests;

    @CommandLine.Parameters (arity = "0..1")
    private final Path projectPath;

    @CommandLine.Option(names = "--dump-bir", hidden = true)
    private boolean dumpBIR;

    @CommandLine.Option(names = "--dump-bir-file", hidden = true)
    private Boolean dumpBIRFile;

    @CommandLine.Option(names = "--dump-graph", hidden = true)
    private boolean dumpGraph;

    @CommandLine.Option(names = "--dump-raw-graphs", hidden = true)
    private boolean dumpRawGraphs;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--experimental", description = "Enable experimental language features.")
    private Boolean experimentalFlag;

    @CommandLine.Option(names = "--debug", description = "run tests in remote debugging mode")
    private String debugPort;

    private static final String buildCmd = "bal build [-o <output>] [--offline] [--skip-tests] [--taint-check]\n" +
            "                    [<ballerina-file | package-path>]";

    @CommandLine.Option(names = "--test-report", description = "enable test report generation")
    private Boolean testReport;

    @CommandLine.Option(names = "--code-coverage", description = "enable code coverage")
    private Boolean coverage;

    @CommandLine.Option(names = "--coverage-format", description = "list of supported coverage report formats")
    private String coverageFormat;

    @CommandLine.Option(names = "--observability-included", description = "package observability in the executable " +
            "JAR file(s).")
    private Boolean observabilityIncluded;

    @CommandLine.Option(names = "--cloud", description = "Enable cloud artifact generation")
    private String cloud;

    @CommandLine.Option(names = "--includes", hidden = true,
            description = "hidden option for code coverage to include all classes")
    private String includes;

    @CommandLine.Option(names = "--list-conflicted-classes",
            description = "list conflicted classes when generating executable")
    private Boolean listConflictedClasses;

    @CommandLine.Option(names = "--dump-build-time", description = "calculate and dump build time")
    private Boolean dumpBuildTime;

    @CommandLine.Option(names = "--sticky", description = "stick to exact versions locked (if exists)")
    private Boolean sticky;

    public void execute() {
        long start = 0;
        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(BUILD_COMMAND);
            this.errStream.println(commandUsageInfo);
            return;
        }

        // load project
        Project project;

        // Skip code coverage for single bal files if option is set
        if (FileUtils.hasExtension(this.projectPath)) {
            if (coverage != null && coverage) {
                this.outStream.println("Code coverage is not yet supported with single bal files. Ignoring the flag " +
                        "and continuing the test run...\n");
            }
            coverage = false;
        }

        if (sticky == null) {
            sticky = false;
        }

        BuildOptions buildOptions = constructBuildOptions();

        boolean isSingleFileBuild = false;
        if (FileUtils.hasExtension(this.projectPath)) {
            if (this.compile) {
                CommandUtil.printError(this.errStream,
                        "'-c' or '--compile' can only be used with a Ballerina package.", null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
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
                CommandUtil.printError(this.errStream, e.getMessage(), null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            isSingleFileBuild = true;
        } else {
            // Check if the output flag is set when building all the modules.
            if (null != this.output) {
                CommandUtil.printError(this.errStream,
                        "'-o' and '--output' are only supported when building a single Ballerina " +
                                "file.",
                        "bal build -o <output-file> <ballerina-file> ",
                        true);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
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
                CommandUtil.printError(this.errStream, e.getMessage(), null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        if (!(this.compile && project.currentPackage().compilerPluginToml().isPresent())) {
            if (isProjectEmpty(project)) {
                CommandUtil.printError(this.errStream, "package is empty. please add at least one .bal file.", null,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        // Check `[package]` section is available when compile
        if (this.compile && project.currentPackage().ballerinaToml().get().tomlDocument().toml()
                .getTable("package").isEmpty()) {
            CommandUtil.printError(this.errStream,
                    "'package' information not found in " + ProjectConstants.BALLERINA_TOML,
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check `[package]` org, name and version is available when compile
        if (this.compile) {
            TomlTableNode pkgNode = (TomlTableNode) project.currentPackage().ballerinaToml().get().tomlDocument().toml()
                    .rootNode().entries().get("package");

            if (pkgNode == null || pkgNode.kind() == TomlType.NONE) {
                CommandUtil.printError(this.errStream,
                                       "'package' information not found in " + ProjectConstants.BALLERINA_TOML,
                                       null,
                                       false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }

            List<String> pkgErrors = new ArrayList<>();
            if ("".equals(getStringValueFromTomlTableNode(pkgNode, "org", ""))) {
                pkgErrors.add("'org'");
            }
            if ("".equals(getStringValueFromTomlTableNode(pkgNode, "name", ""))) {
                pkgErrors.add("'name'");
            }
            if ("".equals(getStringValueFromTomlTableNode(pkgNode, "version", ""))) {
                pkgErrors.add("'version'");
            }

            if (!pkgErrors.isEmpty()) {
                String pkgErrorsString;
                if (pkgErrors.size() == 1) {
                    CommandUtil.printError(this.errStream,
                                           "to build a package " + pkgErrors.get(0) +
                                                   " field of the package is required in " +
                                                   ProjectConstants.BALLERINA_TOML,
                                           null,
                                           false);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return;
                } else if (pkgErrors.size() == 2) {
                    pkgErrorsString = pkgErrors.get(0) + " and " + pkgErrors.get(1);
                } else {
                    pkgErrorsString = pkgErrors.get(0) + ", " + pkgErrors.get(1) + " and " + pkgErrors.get(2);
                }
                CommandUtil.printError(this.errStream,
                                       "to build a package " + pkgErrorsString +
                                               " fields of the package are required in " +
                                               ProjectConstants.BALLERINA_TOML,
                                       null,
                                       false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        // Sets the debug port as a system property, which will be used when setting up debug args before running tests.
        if (!project.buildOptions().skipTests() && this.debugPort != null) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, this.debugPort);
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
        }
        // Validate Settings.toml file
        try {
            RepoUtils.readSettings();
        } catch (SettingsTomlException e) {
            this.outStream.println("warning: " + e.getMessage());
        }

        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                // clean the target directory(projects only)
                .addTask(new CleanTargetDirTask(), isSingleFileBuild)
                // resolve maven dependencies in Ballerina.toml
                .addTask(new ResolveMavenDependenciesTask(outStream))
                // compile the modules
                .addTask(new CompileTask(outStream, errStream))
//                .addTask(new CopyResourcesTask()) // merged with CreateJarTask
                // run tests (projects only)
                .addTask(new RunTestsTask(outStream, errStream, includes, coverageFormat),
                        project.buildOptions().skipTests() || isSingleFileBuild)
                // create the BALA if -c provided (build projects only)
                .addTask(new CreateBalaTask(outStream), isSingleFileBuild || !this.compile)
                // create the executable jar, skip if -c flag is provided
                .addTask(new CreateExecutableTask(outStream, this.output), this.compile)
                .addTask(new DumpBuildTimeTask(outStream), !project.buildOptions().dumpBuildTime())
                .build();

        taskExecutor.executeTasks(project);
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    private boolean isProjectEmpty(Project project) {
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            if (!module.documentIds().isEmpty() || !module.testDocumentIds().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private BuildOptions constructBuildOptions() {
        return new BuildOptionsBuilder()
                .codeCoverage(coverage)
                .experimental(experimentalFlag)
                .offline(offline)
                .skipTests(skipTests)
                .testReport(testReport)
                .observabilityIncluded(observabilityIncluded)
                .cloud(cloud)
                .dumpBir(dumpBIR)
                .dumpBirFile(dumpBIRFile)
                .dumpGraph(dumpGraph)
                .dumpRawGraphs(dumpRawGraphs)
                .listConflictedClasses(listConflictedClasses)
                .dumpBuildTime(dumpBuildTime)
                .sticky(sticky)
                .build();
    }

    @Override
    public String getName() {
        return BUILD_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Build a Ballerina project and produce an executable JAR file. The \n");
        out.append("executable \".jar\" file will be created in the <PROJECT-ROOT>/target/bin directory. \n");
        out.append("\n");
        out.append("Build a single Ballerina file. This creates an executable .jar file in the \n");
        out.append("current directory. The name of the executable file will be \n");
        out.append("<ballerina-file-name>.jar. \n");
        out.append("\n");
        out.append("If the output file is specified with the -o flag, the output \n");
        out.append("will be written to the given output file name. The -o flag will only \n");
        out.append("work for single files. \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal build [-o <output>] [--offline] [--skip-tests]\\n\" +\n" +
                "            \"                    [<ballerina-file | package-path>]");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
