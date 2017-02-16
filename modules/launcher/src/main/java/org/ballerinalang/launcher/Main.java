package org.ballerinalang.launcher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import static org.wso2.ballerina.core.runtime.Constants.SYSTEM_PROP_BAL_DEBUG;

/**
 * This class executes a Ballerina program.
 *
 * @since 0.8.0
 */
public class Main {
    private static final String JC_UNKNOWN_OPTION_PREFIX = "Unknown option:";
    private static final String JC_EXPECTED_A_VALUE_AFTER_PARAMETER_PREFIX = "Expected a value after parameter";

    private static PrintStream outStream = System.err;

    public static void main(String... args) {
        try {
            Optional<BLauncherCmd> optionalInvokedCmd = getInvokedCmd(args);

            LauncherUtils.writePID(System.getProperty("ballerina.home"));
            optionalInvokedCmd.ifPresent(BLauncherCmd::execute);
        } catch (BLauncherException e) {
            LauncherUtils.printLauncherException(e, outStream);
            Runtime.getRuntime().exit(1);
        } catch (Throwable e) {
            String msg = e.getMessage();
            if (msg == null) {
                outStream.println("ballerina: unexpected error occurred");
            } else {
                outStream.println("ballerina: unexpected error occurred: " +
                                    LauncherUtils.makeFirstLetterUpperCase(msg));
            }
            Runtime.getRuntime().exit(1);
        }
    }

    private static Optional<BLauncherCmd> getInvokedCmd(String... args) {
        try {
            MainCmd mainCmd = new MainCmd();
            JCommander cmdParser = new JCommander(mainCmd);
            mainCmd.setJCommander(cmdParser);

            RunCmd runCmd = new RunCmd();
            cmdParser.addCommand("run", runCmd);

            ServiceCmd serviceCmd = new ServiceCmd();
            cmdParser.addCommand("service", serviceCmd);

            AddCmd addCmd = new AddCmd();
            cmdParser.addCommand("add", addCmd);

            BuildCmd buildCmd = new BuildCmd();
            cmdParser.addCommand("build", buildCmd);

            HelpCmd helpCmd = new HelpCmd();
            cmdParser.addCommand("help", helpCmd);
            helpCmd.setJCommander(cmdParser);

            // loading additional commands via SPI
            ServiceLoader<BLauncherCmd> bCmds = ServiceLoader.load(BLauncherCmd.class);
            for (BLauncherCmd bCmd : bCmds) {
                cmdParser.addCommand(bCmd.getName(), bCmd);
            }

            cmdParser.setProgramName("ballerina");
            cmdParser.parse(args);
            String parsedCmdName = cmdParser.getParsedCommand();

            // User has not specified a command. Therefore returning the main command
            // which simply prints usage information.
            if (parsedCmdName == null) {
                return Optional.of(mainCmd);
            }

            Map<String, JCommander> commanderMap = cmdParser.getCommands();
            return Optional.of((BLauncherCmd) commanderMap.get(parsedCmdName).getObjects().get(0));

        } catch (MissingCommandException e) {
            String errorMsg = "unknown command '" + e.getUnknownCommand() + "'";
            throw LauncherUtils.createUsageException(errorMsg);

        } catch (ParameterException e) {
            String msg = e.getMessage();
            if (msg == null) {
                throw LauncherUtils.createUsageException("unexpected error occurred");

            } else if (msg.startsWith(JC_UNKNOWN_OPTION_PREFIX)) {
                String flag = msg.substring(JC_UNKNOWN_OPTION_PREFIX.length());
                throw LauncherUtils.createUsageException("unknown flag '" + flag.trim() + "'");

            } else if (msg.startsWith(JC_EXPECTED_A_VALUE_AFTER_PARAMETER_PREFIX)) {
                String flag = msg.substring(JC_EXPECTED_A_VALUE_AFTER_PARAMETER_PREFIX.length());
                throw LauncherUtils.createUsageException("flag '" + flag.trim() + "' needs an argument");

            } else {
                // Make the first character of the error message lower case
                throw LauncherUtils.createUsageException(LauncherUtils.makeFirstLetterUpperCase(msg));
            }
        }
    }

    private static void printUsageInfo(JCommander cmdParser) {
        StringBuilder out = new StringBuilder();
        out.append("Ballerina is a flexible, powerful and beautiful programming language designed for integration.\n");
        out.append("\n");
        out.append("* Find more information at http://ballerinalang.org\n");
        out.append("\n");
        out.append("Usage:\n");
        out.append("  ballerina [command] [options]\n");
        out.append("\n");

        out.append("Available Commands:\n");
        printCommandList(cmdParser, out);

        out.append("\n");
        printFlags(cmdParser.getParameters(), out);

        out.append("\n");
        out.append("Use \"ballerina help [command]\" for more information about a command.");
        outStream.println(out.toString());
    }

    private static void printFlags(List<ParameterDescription> paramDescs, StringBuilder out) {
        int longestNameLen = 0;
        int count = 0;
        for (ParameterDescription parameterDesc : paramDescs) {
            if (parameterDesc.getParameter().hidden()) {
                continue;
            }

            String names = parameterDesc.getNames();
            int length = names.length() + 2;
            if (length > longestNameLen) {
                longestNameLen = length;
            }
            count++;
        }

        if (count == 0) {
            return;
        }
        out.append("\n");
        out.append("Flags:\n");
        for (ParameterDescription parameterDesc : paramDescs) {
            if (parameterDesc.getParameter().hidden()) {
                continue;
            }
            String names = parameterDesc.getNames();
            String desc = parameterDesc.getDescription();
            int noOfSpaces = longestNameLen - (names.length() + 2);
            char[] charArray = new char[noOfSpaces + 4];
            Arrays.fill(charArray, ' ');
            out.append("  ").append(names).append(new String(charArray)).append(desc).append("\n");
        }
    }

    private static void printCommandList(JCommander cmdParser, StringBuilder out) {
        int longestNameLen = 0;
        for (JCommander commander : cmdParser.getCommands().values()) {
            BLauncherCmd cmd = (BLauncherCmd) commander.getObjects().get(0);
            if (cmd.getName().equals("main") || cmd.getName().equals("help")) {
                continue;
            }

            int length = cmd.getName().length() + 2;
            if (length > longestNameLen) {
                longestNameLen = length;
            }
        }

        for (JCommander commander : cmdParser.getCommands().values()) {
            BLauncherCmd cmd = (BLauncherCmd) commander.getObjects().get(0);
            if (cmd.getName().equals("main") || cmd.getName().equals("help")) {
                continue;
            }

            String cmdName = cmd.getName();
            String cmdDesc = cmdParser.getCommandDescription(cmdName);

            int noOfSpaces = longestNameLen - (cmd.getName().length() + 2);
            char[] charArray = new char[noOfSpaces + 4];
            Arrays.fill(charArray, ' ');
            out.append("  ").append(cmdName).append(new String(charArray)).append(cmdDesc).append("\n");
        }
    }

    private static void printCommandUsageInfo(JCommander cmdParser, String commandName) {
        StringBuilder out = new StringBuilder();
        JCommander jCommander = cmdParser.getCommands().get(commandName);
        BLauncherCmd bLauncherCmd = (BLauncherCmd) jCommander.getObjects().get(0);

        out.append(cmdParser.getCommandDescription(commandName)).append("\n");
        out.append("\n");
        out.append("Usage:\n");
        bLauncherCmd.printUsage(out);
        printFlags(jCommander.getParameters(), out);
        outStream.println(out.toString());
    }

    /**
     * This class represents the "run" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "run", commandDescription = "run Ballerina program")
    private static class RunCmd implements BLauncherCmd {

        @Parameter(arity = 1, description = "arguments")
        private List<String> argList;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        @Parameter(names = "--ballerina.debug", hidden = true, description = "remote debugging port")
        private String ballerinaDebugPort;

        public void execute() {
            if (argList == null || argList.size() == 0) {
                throw LauncherUtils.createUsageException("no ballerina program given");
            }

            List<String> programArgs;
            if (argList.size() > 1) {
                programArgs = argList.subList(1, argList.size());
            } else {
                programArgs = new ArrayList<>(0);
            }

            if (null != ballerinaDebugPort) {
                System.setProperty(SYSTEM_PROP_BAL_DEBUG, ballerinaDebugPort);
            }

            Path p = Paths.get(argList.get(0));
            p = p.toAbsolutePath();
            BMainRunner.runMain(p, programArgs);
        }

        @Override
        public String getName() {
            return "run";
        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("ballerina run <filename>\n");
        }
    }

    /**
     * This class represents the "service" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "service", commandDescription = "start Ballerina services")
    private static class ServiceCmd implements BLauncherCmd {

        @Parameter(description = "The list of files to commit")
        private List<String> sourceFileList;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        @Parameter(names = {"--service-root", "-sr"}, description = "directory which contains ballerina services")
        private String serviceRootPath;

        @Parameter(names = "--ballerina.debug", hidden = true, description = "remote debugging port")
        private String ballerinaDebugPort;

        public void execute() {
            if (sourceFileList == null || sourceFileList.size() == 0) {
                throw LauncherUtils.createUsageException("no ballerina programs given");
            }

            Path[] paths = new Path[sourceFileList.size()];
            for (int i = 0; i < sourceFileList.size(); i++) {
                paths[i] = Paths.get(sourceFileList.get(i)).toAbsolutePath();
            }

            if (null != ballerinaDebugPort) {
                System.setProperty(SYSTEM_PROP_BAL_DEBUG, ballerinaDebugPort);
            }

            BServiceRunner.start(paths);
        }

        @Override
        public String getName() {
            return "service";
        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("ballerina service <filename>... [flags]\n");
        }
    }

    /**
     * This class represents the "build" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "build", commandDescription = "build Ballerina program with dependencies")
    private static class BuildCmd implements BLauncherCmd {

        @Parameter(arity = 1, description = "builds the given package with all the dependencies")
        private List<String> sourceFileList;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        public void execute() {
            outStream.println("ballerina: 'build' command is still being developed");
        }

        @Override
        public String getName() {
            return "build";
        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("ballerina build <filename>...\n");
        }
    }

    /**
     * This class represents the "add" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "add", commandDescription = "download and install Ballerina packages ")
    private static class AddCmd implements BLauncherCmd {

        @Parameter(arity = 1, description = "The package to be added to the Ballerina repository ")
        private List<String> sourceFileList;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        public void execute() {
            outStream.println("ballerina: 'add' command is still being developed");
        }

        @Override
        public String getName() {
            return "add";
        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("ballerina add <package>...\n");
        }
    }

    /**
     * This class represents the "help" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "help", commandDescription = "print usage information")
    private static class HelpCmd implements BLauncherCmd {

        @Parameter(description = "Command name")
        private List<String> helpCommands;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        private JCommander cmdParser;

        public void execute() {
            if (helpCommands == null) {
                printUsageInfo(cmdParser);
                return;

            } else if (helpCommands.size() > 1) {
                throw LauncherUtils.createUsageException("too many arguments given");
            }

            String userCommand = helpCommands.get(0);
            if (cmdParser.getCommands().get(userCommand) == null) {
                throw LauncherUtils.createUsageException("unknown help topic `" + userCommand + "`");
            }

            printCommandUsageInfo(cmdParser, userCommand);
        }

        @Override
        public String getName() {
            return "help";
        }

        @Override
        public void printUsage(StringBuilder out) {
        }

        void setJCommander(JCommander cmdParser) {
            this.cmdParser = cmdParser;
        }
    }

    /**
     * This class represents the "main" command required by the JCommander
     *
     * @since 0.8.0
     */
    private static class MainCmd implements BLauncherCmd {

        @Parameter(names = {"--help", "-h"}, description = "for more information")
        private boolean helpFlag;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        private JCommander cmdParser;

        @Override
        public void execute() {
            printUsageInfo(cmdParser);
        }

        @Override
        public String getName() {
            return "main";
        }

        @Override
        public void printUsage(StringBuilder out) {
        }

        void setJCommander(JCommander cmdParser) {
            this.cmdParser = cmdParser;
        }
    }
}

