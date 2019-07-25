package org.ballerinalang.packerina.cmd;

import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.BallerinaCliCommands;
import org.ballerinalang.tool.LauncherUtils;
import org.ballerinalang.util.VMOptions;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.runtime.Constants.SYSTEM_PROP_BAL_DEBUG;

/**
 * This class represents the "run" command and it holds arguments and flags specified by the user.
 *
 * @since 1.0.0
 */
@CommandLine.Command(name = "run", description = "compile and run Ballerina programs")
public class RunCommand implements BLauncherCmd {

    private PrintStream errStream;

    @CommandLine.Parameters(description = "arguments")
    private List<String> argList;

    @CommandLine.Option(names = {"--sourceroot"},
            description = "path to the directory containing source files and modules")
    private String sourceRoot;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--offline"})
    private boolean offline;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    @CommandLine.Option(names = {"--config", "-c"}, description = "path to the Ballerina configuration file")
    private String configFilePath;

    @CommandLine.Option(names = "--observe", description = "enable observability with default configs")
    private boolean observeFlag;

    @CommandLine.Option(names = "-e", description = "Ballerina environment parameters")
    private Map<String, String> runtimeParams = new HashMap<>();

    @CommandLine.Option(names = "-B", description = "Ballerina VM options")
    private Map<String, String> vmOptions = new HashMap<>();

    @CommandLine.Option(names = "--experimental", description = "enable experimental language features")
    private boolean experimentalFlag;

    @CommandLine.Option(names = "--siddhiruntime", description = "enable siddhi runtime for stream processing")
    private boolean siddhiRuntimeFlag;

    public RunCommand() {
        errStream = System.err;
    }

    public RunCommand(PrintStream errStream) {
        this.errStream = errStream;
    }

    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(Constants.RUN_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.size() == 0) {
            throw LauncherUtils.createUsageExceptionWithHelp("no ballerina program given");
        }

        // Enable remote debugging
        if (null != debugPort) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }

        Path sourceRootPath = LauncherUtils.getSourceRootPath(sourceRoot);
        VMOptions.getInstance().addOptions(vmOptions);

        String programArg = argList.get(0);
        Path sourcePath = Paths.get(programArg);

        // Filter out the list of arguments given to the ballerina program.
        // TODO: 7/26/18 improve logic with positioned param
        String[] programArgs;
        if (argList.size() >= 2) {
            argList.remove(0);
            programArgs = argList.toArray(new String[0]);
        } else {
            programArgs = new String[0];
        }

        // Normalize the source path to remove './' or '.\' characters that can appear before the name
        LauncherUtils.runProgram(sourceRootPath, sourcePath.normalize(), runtimeParams, configFilePath, programArgs,
                offline, observeFlag, siddhiRuntimeFlag, experimentalFlag);
    }

    @Override
    public String getName() {
        return BallerinaCliCommands.RUN;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Run command runs a compiled Ballerina program. \n");
        out.append("\n");
        out.append("If a Ballerina source file or a module is given, \n");
        out.append("run command compiles and runs it. \n");
        out.append("\n");
        out.append("By default, 'ballerina run' executes the main function. \n");
        out.append("If the main function is not there, it executes services. \n");
        out.append("\n");
        out.append("If the -s flag is given, 'ballerina run' executes\n");
        out.append("services instead of the main function.\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina run [flags] <balfile | module-name > [args...] \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
