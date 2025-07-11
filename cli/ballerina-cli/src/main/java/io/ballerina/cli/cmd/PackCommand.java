package io.ballerina.cli.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.TaskExecutor;
import io.ballerina.cli.task.CleanTargetDirTask;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.cli.task.CreateBalaTask;
import io.ballerina.cli.task.DumpBuildTimeTask;
import io.ballerina.cli.task.ResolveMavenDependenciesTask;
import io.ballerina.cli.task.RunBuildToolsTask;
import io.ballerina.cli.utils.BuildTime;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.cli.cmd.Constants.PACK_COMMAND;
import static io.ballerina.projects.internal.ManifestBuilder.getStringValueFromTomlTableNode;
import static io.ballerina.projects.util.ProjectUtils.isProjectUpdated;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;

/**
 * This class represents the "bal pack" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = PACK_COMMAND, description = "Create distribution format of the current package")
public class PackCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private final boolean exitWhenFinish;
    private final boolean skipCopyLibsFromDist;

    @CommandLine.Option(names = {"--offline"}, description = "Build/Compile offline without downloading " +
            "dependencies.")
    private Boolean offline;

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

    @CommandLine.Option(names = "--dump-build-time", hidden = true, description = "calculate and dump build time")
    private Boolean dumpBuildTime;

    @CommandLine.Option(names = "--sticky", description = "stick to exact versions locked (if exists)")
    private Boolean sticky;

    @CommandLine.Option(names = "--target-dir", description = "target directory path")
    private Path targetDir;

    @CommandLine.Option(names = "--generate-config-schema", hidden = true)
    private Boolean configSchemaGen;

    @CommandLine.Option(names = "--enable-cache", description = "enable caches for the compilation", hidden = true)
    private Boolean enableCache;

    @CommandLine.Option(names = "--disable-syntax-tree-caching", hidden = true, description = "disable syntax tree " +
            "caching for source files", defaultValue = "false")
    private Boolean disableSyntaxTreeCaching;

    @CommandLine.Option(names = "--show-dependency-diagnostics", description = "Show the diagnostics " +
            "generated by the dependencies")
    private Boolean showDependencyDiagnostics;

    @CommandLine.Option(names = "--optimize-dependency-compilation", hidden = true,
            description = "experimental memory optimization for large projects")
    private Boolean optimizeDependencyCompilation;

    @CommandLine.Option(names = "--locking-mode", hidden = true,
            description = "allow passing the package locking mode.")
    private String lockingMode;

    public PackCommand() {
        this.projectPath = Path.of(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
        this.skipCopyLibsFromDist = false;
    }

    PackCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                       boolean skipCopyLibsFromDist) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
        this.offline = true;
    }

    PackCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                boolean skipCopyLibsFromDist, Boolean optimizeDependencyCompilation) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
        this.optimizeDependencyCompilation = optimizeDependencyCompilation;
        this.offline = true;
    }

    PackCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                       boolean skipCopyLibsFromDist, Path targetDir) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
        this.targetDir = targetDir;
        this.offline = true;
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

        BuildOptions buildOptions = constructBuildOptions();

        // Throw an error if its a single file
        if (FileUtils.hasExtension(this.projectPath)) {
            CommandUtil.printError(this.errStream, "bal pack can only be used with a Ballerina package.", null, false);
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

        // Validate Settings.toml file
        RepoUtils.readSettings();

        // Check package files are modified after last build
        boolean isPackageModified = isProjectUpdated(project);

        Optional<Diagnostic> deprecatedDocWarning = ProjectUtils.getProjectLoadingDiagnostic().stream().filter(
                diagnostic -> diagnostic.diagnosticInfo().code().equals(
                        ProjectDiagnosticErrorCode.DEPRECATED_DOC_FILE.diagnosticId())).findAny();

        deprecatedDocWarning.ifPresent(this.errStream::println);

        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetDirTask(isPackageModified, buildOptions.enableCache()), isSingleFileBuild)
                .addTask(new RunBuildToolsTask(outStream), isSingleFileBuild)
                .addTask(new ResolveMavenDependenciesTask(outStream))
                .addTask(new CompileTask(outStream, errStream, true, false,
                        isPackageModified, buildOptions.enableCache()))
                .addTask(new CreateBalaTask(outStream))
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
                .setExperimental(experimentalFlag)
                .setOffline(offline)
                .setDumpBir(dumpBIR)
                .setDumpBirFile(dumpBIRFile)
                .setDumpGraph(dumpGraph)
                .setDumpRawGraphs(dumpRawGraphs)
                .setDumpBuildTime(dumpBuildTime)
                .setSticky(sticky)
                .setConfigSchemaGen(configSchemaGen)
                .setEnableCache(enableCache)
                .disableSyntaxTreeCaching(disableSyntaxTreeCaching)
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
        return PACK_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(PACK_COMMAND));

    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal pack [--offline] \\n\" +\n" + "            \"       [<package-path>]");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
