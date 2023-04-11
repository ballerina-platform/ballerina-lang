package io.ballerina.cli.cmd;

// TODO:
//  1. Help text
//  2. How to persist once added ***
//  3. How to remove once added
//  4. How to list
//  5. Move OpenAPI to a separate gradle project and use

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.cmd.sub.ConcreteSubTool;
import io.ballerina.cli.cmd.sub.SubToolCommand;
import io.ballerina.cli.launcher.BallerinaCliCommands;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
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

    private final File subCommandsFile;

    private CommandLine parentCmdParser;

    public ToolCommand() {
        this.errStream = System.err;
        this.exitWhenFinish = true;
        this.subCommandsFile = new File(System.getProperty("user.home") + "/.ballerina/subCommands");
    }

    @Override
    public void execute() {
        System.out.println("Starting tool command logic...");
        this.loadSubCommands();

        if (helpFlag || toolCommands == null) {
            errStream.println("Ballerina Tool Command Help:");
            return;
        }

        if (toolCommands.size() == 2 && toolCommands.get(0).equals("pull")) {
            String toolName = toolCommands.get(1);
            System.out.println("Trying to pull tool: " + toolName);

            // Tool pulling logic identical to bal pull.
            // TODO: Should we have a way to identify if its a tool or a general package?
            // TODO: look into using SPI here. add to the spi list and load directly as a command
            // TODO: try using a creational design pattern here
            SubToolCommand subToolCommand = new ConcreteSubTool();
            subCommands.put(toolName, subToolCommand);
            saveSubCommands();
            System.out.println("Saved sub command: " + subToolCommand.getName());
            return;
        }

        if (toolCommands.size() >= 1 && subCommands.containsKey(toolCommands.get(0))) {
            SubToolCommand subToolCommand = subCommands.get(toolCommands.get(0));
            // TODO: need a mechanism to pass args to sub commands
            subToolCommand.execute();
            return;
        }

        System.out.println("Invalid tool command: " + toolCommands.get(0));
    }

    private void loadSubCommands() {
        try (BufferedReader reader = new BufferedReader(new FileReader(subCommandsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Loading sub command: " + line);
                String[] parts = line.split(":");
                String name = parts[0];
                String className = parts[1];
                Class<?> clazz = Class.forName(className);
                SubToolCommand command = (SubToolCommand) clazz.getDeclaredConstructor().newInstance();
                subCommands.put(name, command);
            }
            registerSubCommands();
        } catch (IOException | ReflectiveOperationException ignored) {}
    }

    private void saveSubCommands() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(subCommandsFile))) {
            for (Map.Entry<String, SubToolCommand> entry : subCommands.entrySet()) {
                String name = entry.getKey();
                String className = entry.getValue().getClass().getName();
                writer.printf("%s:%s%n", name, className);
            }
        } catch (IOException e) {
            System.err.printf("Failed to save sub commands to file '%s': %s.%n", subCommandsFile, e.getMessage());
        }
    }

    private void registerSubCommands() {
        for (Map.Entry<String, SubToolCommand> entry : this.subCommands.entrySet()) {
            parentCmdParser.addSubcommand(entry.getKey(), entry.getValue());
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
