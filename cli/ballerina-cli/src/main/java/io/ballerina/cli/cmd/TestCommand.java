package io.ballerina.cli.cmd;

import io.ballerina.cli.TaskExecutor;
import io.ballerina.cli.task.CleanTargetDirTask;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.cli.task.CreateBaloTask;
import io.ballerina.cli.task.CreateTargetDirTask;
import io.ballerina.cli.task.ListTestGroupsTask;
import io.ballerina.cli.task.RunTestsTask;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.runtime.launch.LaunchUtils;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.tool.BLauncherCmd;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static io.ballerina.cli.cmd.Constants.BUILD_COMMAND;
import static io.ballerina.cli.cmd.Constants.TEST_COMMAND;
import static io.ballerina.runtime.util.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;

/**
 * This class represents the "ballerina test" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = TEST_COMMAND, description = "Test Ballerina modules")
public class TestCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private Path projectPath;
    private boolean exitWhenFinish;
    private boolean skipCopyLibsFromDist;

    public TestCommand() {
        this.projectPath = Paths.get(System.getProperty("user.dir"));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
        this.skipCopyLibsFromDist = false;
    }

    public TestCommand(Path userDir, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                       boolean skipCopyLibsFromDist) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
    }

    @CommandLine.Option(names = {"--offline"}, description = "Builds/Compiles offline without downloading " +
            "dependencies.")
    private boolean offline;

    @CommandLine.Option(names = {"--skip-lock"}, description = "Skip using the lock file to resolve dependencies.")
    private boolean skipLock;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--experimental", description = "Enable experimental language features.")
    private boolean experimentalFlag;

    @CommandLine.Option(names = "--debug", description = "start in remote debugging mode")
    private String debugPort;

    @CommandLine.Option(names = "--list-groups", description = "list the groups available in the tests")
    private boolean listGroups;

    @CommandLine.Option(names = "--groups", split = ",", description = "test groups to be executed")
    private List<String> groupList;

    @CommandLine.Option(names = "--disable-groups", split = ",", description = "test groups to be disabled")
    private List<String> disableGroupList;

    @CommandLine.Option(names = "--test-report", description = "enable test report generation")
    private boolean testReport;

    @CommandLine.Option(names = "--code-coverage", description = "enable code coverage")
    private boolean coverage;

    @CommandLine.Option(names = "--observability-included", description = "package observability in the executable.")
    private boolean observabilityIncluded;

    @CommandLine.Option(names = "--tests", split = ",", description = "Test functions to be executed")
    private List<String> testList;

    @CommandLine.Option(names = "--rerun-failed", description = "Rerun failed tests.")
    private boolean rerunTests;

    private static final String testCmd = "ballerina test [<ballerina-file] [--skip-lock] [--] [(--key=value)...]";

    public void execute() {
        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(BUILD_COMMAND);
            this.errStream.println(commandUsageInfo);
            return;
        }

        // Sets the debug port as a system property, which will be used when setting up debug args before running tests.
        if (this.debugPort != null) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, this.debugPort);
        }

        String[] args;
        if (this.argList == null) {
            args = new String[0];
        } else {
            args = argList.subList(1, argList.size()).toArray(new String[0]);
        }

        String[] userArgs = LaunchUtils.getUserArgs(args, new HashMap<>());
        // check if there are too many arguments.
        if (userArgs.length > 0) {
            CommandUtil.printError(this.errStream, "too many arguments.", testCmd, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (argList == null) {
            this.projectPath = Paths.get(System.getProperty("user.dir"));
        } else {
            this.projectPath = Paths.get(argList.get(0));
        }

        // load project
        Project project;
        boolean isSingleFileBuild = false;
        if (FileUtils.hasExtension(this.projectPath)) {
            try {
                project = SingleFileProject.load(this.projectPath);
            } catch (RuntimeException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            isSingleFileBuild = true;
        } else {
            try {
                project = BuildProject.load(this.projectPath);
            } catch (RuntimeException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        CompilerContext compilerContext = project.projectEnvironmentContext().getService(CompilerContext.class);
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(OFFLINE, Boolean.toString(this.offline));
        options.put(LOCK_ENABLED, Boolean.toString(!this.skipLock));
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(this.experimentalFlag));
        options.put(PRESERVE_WHITESPACE, "true");

        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetDirTask(), isSingleFileBuild)   // clean the target directory(projects only)
                .addTask(new CreateTargetDirTask()) // create target directory
//                .addTask(new ResolveMavenDependenciesTask()) // resolve maven dependencies in Ballerina.toml
                .addTask(new CompileTask(outStream)) // compile the modules
                .addTask(new CreateBaloTask(outStream), isSingleFileBuild || listGroups) // create the BALO ( build projects only)
//                .addTask(new CopyResourcesTask(), listGroups) // merged with CreateJarTask
                .addTask(new ListTestGroupsTask(outStream), !listGroups) // list the available test groups
                .addTask(new RunTestsTask(outStream, errStream, args, rerunTests, groupList, disableGroupList,
                        testList), listGroups)
                .build();

        taskExecutor.executeTasks(project);
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    @Override
    public String getName() {
        return TEST_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Test a Ballerina project or a standalone Ballerina file. \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append(testCmd + "\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

}
