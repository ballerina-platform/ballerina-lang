package io.ballerina.cli.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.TaskExecutor;
import io.ballerina.cli.task.CleanTargetDirTask;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.cli.task.CreateBalaTask;
import io.ballerina.cli.task.DumpBuildTimeTask;
import io.ballerina.cli.task.ResolveMavenDependenciesTask;
import io.ballerina.cli.task.RunTestsTask;
import io.ballerina.cli.utils.BuildTime;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
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

import static io.ballerina.cli.cmd.Constants.PACK_COMMAND;
import static io.ballerina.projects.internal.ManifestBuilder.getStringValueFromTomlTableNode;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.JACOCO_XML_FORMAT;

/**
 * This class represents the "bal pack" command.
 *
 * @since 2.0.0
 */
public class PackCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private boolean exitWhenFinish;
    private boolean skipCopyLibsFromDist;
    private Boolean skipTests;

    @CommandLine.Option(names = {"--offline"}, description = "Build/Compile offline without downloading " +
            "dependencies.")
    private Boolean offline;

    @CommandLine.Option(names = {"--with-tests"}, description = "Run test compilation and execution.")
    private Boolean withTests;

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

    @CommandLine.Option(names = "--test-report", description = "enable test report generation")
    private Boolean testReport;

    @CommandLine.Option(names = "--code-coverage", description = "enable code coverage")
    private Boolean coverage;

    @CommandLine.Option(names = "--coverage-format", description = "list of supported coverage report formats")
    private String coverageFormat;

    @CommandLine.Option(names = "--includes", hidden = true,
            description = "hidden option for code coverage to include all classes")
    private String includes;

    @CommandLine.Option(names = "--dump-build-time", description = "calculate and dump build time")
    private Boolean dumpBuildTime;

    @CommandLine.Option(names = "--sticky", description = "stick to exact versions locked (if exists)")
    private Boolean sticky;

    @CommandLine.Option(names = "--target-dir", description = "path of custom target directory")
    private Path customTargetPath;

    public PackCommand() {
        this.projectPath = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
        this.skipCopyLibsFromDist = false;
    }

    public PackCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                       boolean skipCopyLibsFromDist) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
    }

    @Override
    public void execute() {
        long start = 0;
        boolean isSingleFileBuild = false; // Packing cannot be done for single files

        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(PACK_COMMAND);
            this.errStream.println(commandUsageInfo);
            return;
        }

        Project project;

        if (sticky == null) {
            sticky = false;
        }

        // If withTests flag is not provided, we change the skipTests flag accordingly
        if (withTests != null) {
            this.skipTests = !withTests;
        }

        BuildOptions buildOptions = constructBuildOptions();

        // Throw an error if its a single file
        if (FileUtils.hasExtension(this.projectPath)) {
            CommandUtil.printError(this.errStream, "bal pack can only be used with a Ballerina package.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // If a custom target path is set, we modify the tagetPath in the build options with the new custom one
        if (this.customTargetPath != null) {
            buildOptions.setTargetPath(this.customTargetPath);
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


        if (isProjectEmpty(project)) {
            if (project.currentPackage().compilerPluginToml().isPresent()) {
                CommandUtil.printError(this.errStream, "package is empty. please add at least one .bal file.", null,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        // Check `[package]` section is available when compile
        if (project.currentPackage().ballerinaToml().get().tomlDocument().toml().getTable("package")
                .isEmpty()) {
            CommandUtil.printError(this.errStream,
                    "'package' information not found in " + ProjectConstants.BALLERINA_TOML,
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        } else {
            // if not empty, validate the `[package]` section

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
                .addTask(new CleanTargetDirTask(), isSingleFileBuild)
                .addTask(new ResolveMavenDependenciesTask(outStream))
                .addTask(new CompileTask(outStream, errStream))
                .addTask(new RunTestsTask(outStream, errStream, includes, coverageFormat),
                        project.buildOptions().skipTests() || isSingleFileBuild)
                .addTask(new CreateBalaTask(outStream))
                .addTask(new DumpBuildTimeTask(outStream), !project.buildOptions().dumpBuildTime())
                .build();

        taskExecutor.executeTasks(project);
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    private BuildOptions constructBuildOptions() {
        return BuildOptions.builder()
                .setCodeCoverage(coverage)
                .setExperimental(experimentalFlag)
                .setOffline(offline)
                .setSkipTests(skipTests)
                .setTestReport(testReport)
                .setDumpBir(dumpBIR)
                .setDumpBirFile(dumpBIRFile)
                .setDumpGraph(dumpGraph)
                .setDumpRawGraphs(dumpRawGraphs)
                .setDumpBuildTime(dumpBuildTime)
                .setSticky(sticky)
                .build();
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

    @Override
    public String getName() {
        return PACK_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("packages the current package into a .bala file after verifying that it can build with \n");
        out.append("all its dependencies. Created .bala file contains the distribution format of the current package");

    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal pack [--offline] [--with-tests]\\n\" +\n" + "            \"       [<package-path>]");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
