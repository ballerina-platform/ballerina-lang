package io.ballerina.cli.cmd;

// TODO:
//  1. Help text
//  2. How to persist once added ***
//  3. How to remove once added
//  4. How to list
//  5. Move OpenAPI to a separate gradle project and use

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.cmd.sub.SubToolCommand;
import io.ballerina.cli.launcher.BallerinaCliCommands;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;

/**
 * This class represents the "tool" command, and it holds arguments and flags specified by the user.
 *
 * @since 0.8.1
 */
@CommandLine.Command(name = TOOL_COMMAND, description = "Ballerina tool command")
public
class ToolCommand implements BLauncherCmd {
    private final boolean exitWhenFinish;
    private PrintStream errStream;

    @CommandLine.Parameters(description = "Command name")
    private List<String> toolCommands;

    @CommandLine.Option(names = {"--help", "-h", "?"}, usageHelp = true)
    private boolean helpFlag;

    private final Map<String, SubToolCommand> subCommands = new HashMap<>();


    private CommandLine parentCmdParser;

    public ToolCommand() {
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    @Override
    public void execute() {
        System.out.println("Starting tool command logic...");

        if (helpFlag || toolCommands == null) {
            errStream.println("Ballerina Tool Command Help:");
            return;
        }

        if (toolCommands.size() == 2 && toolCommands.get(0).equals("pull")) {
            String toolName = toolCommands.get(1);
            System.out.println("Trying to pull tool: " + toolName);

            // TODO: Implement the pulling logic similar to bal pull. May be we can call pull command from here.

            CommandUtil.addSubCommandsFromJarToCmdParser(toolName, this.parentCmdParser);

            // Save the path to the JAR file to a configuration file.
            try {
                CommandUtil.saveJarFilePathToConfigFile(toolName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Saved sub command: " + toolName);
        }
    }

    @Override
    public String getName() {
        return BallerinaCliCommands.TOOL;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("  bal tool\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal tool\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }
}
