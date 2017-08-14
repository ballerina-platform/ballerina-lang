package org.ballerinalang.launcher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.ParserException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;

import static org.ballerinalang.runtime.Constants.SYSTEM_PROP_BAL_DEBUG;

/**
 * This class executes a Ballerina program.
 *
 * @since 0.8.0
 */
public class Main {
    private static final String JC_UNKNOWN_OPTION_PREFIX = "Unknown option:";
    private static final String JC_EXPECTED_A_VALUE_AFTER_PARAMETER_PREFIX = "Expected a value after parameter";

    private static PrintStream outStream = System.err;

    private static final Logger breLog = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {
        try {
            Optional<BLauncherCmd> optionalInvokedCmd = getInvokedCmd(args);
            optionalInvokedCmd.ifPresent(BLauncherCmd::execute);
        } catch (ParserException | SemanticException | BLangRuntimeException e) {
            outStream.println(e.getMessage());
            Runtime.getRuntime().exit(1);
        } catch (BLauncherException e) {
            LauncherUtils.printLauncherException(e, outStream);
            Runtime.getRuntime().exit(1);
        } catch (Throwable e) {
            String msg = e.getMessage();
            if (msg == null) {
                msg = "ballerina: internal error occurred";
            } else {
                msg = "ballerina: " + LauncherUtils.makeFirstLetterLowerCase(msg);
            }
            outStream.println(msg);
            breLog.error(msg, e);
            Runtime.getRuntime().exit(1);
        }
    }

    private static JCommander addSubCommand(JCommander parentCmd, String commandName, Object commandObject) {
        parentCmd.addCommand(commandName, commandObject);
        return parentCmd.getCommands().get(commandName);
    }

    private static Optional<BLauncherCmd> getInvokedCmd(String... args) {
        try {
            DefaultCmd defaultCmd = new DefaultCmd();
            JCommander cmdParser = new JCommander(defaultCmd);
            defaultCmd.setParentCmdParser(cmdParser);

            // Run command
            RunCmd runCmd = new RunCmd();
            JCommander jcRunCmd = addSubCommand(cmdParser, "run", runCmd);
            runCmd.setParentCmdParser(cmdParser);
            runCmd.setSelfCmdParser(jcRunCmd);

            HelpCmd helpCmd = new HelpCmd();
            cmdParser.addCommand("help", helpCmd);
            helpCmd.setParentCmdParser(cmdParser);

            // loading additional commands via SPI
            ServiceLoader<BLauncherCmd> bCmds = ServiceLoader.load(BLauncherCmd.class);
            for (BLauncherCmd bCmd : bCmds) {
                cmdParser.addCommand(bCmd.getName(), bCmd);
                bCmd.setParentCmdParser(cmdParser);
            }

            // Build Version Command
            VersionCmd versionCmd = new VersionCmd();
            cmdParser.addCommand("version", versionCmd);
            versionCmd.setParentCmdParser(cmdParser);

            cmdParser.setProgramName("ballerina");
            cmdParser.parse(args);
            String parsedCmdName = cmdParser.getParsedCommand();

            // User has not specified a command. Therefore returning the main command
            // which simply prints usage information.
            if (parsedCmdName == null) {
                return Optional.of(defaultCmd);
            }

            Map<String, JCommander> commanderMap = cmdParser.getCommands();
            return Optional.of((BLauncherCmd) commanderMap.get(parsedCmdName).getObjects().get(0));

        } catch (MissingCommandException e) {
            String errorMsg = "unknown command '" + e.getUnknownCommand() + "'";
            throw LauncherUtils.createUsageException(errorMsg);

        } catch (ParameterException e) {
            String msg = e.getMessage();
            if (msg == null) {
                throw LauncherUtils.createUsageException("internal error occurred");

            } else if (msg.startsWith(JC_UNKNOWN_OPTION_PREFIX)) {
                String flag = msg.substring(JC_UNKNOWN_OPTION_PREFIX.length());
                throw LauncherUtils.createUsageException("unknown flag '" + flag.trim() + "'");

            } else if (msg.startsWith(JC_EXPECTED_A_VALUE_AFTER_PARAMETER_PREFIX)) {
                String flag = msg.substring(JC_EXPECTED_A_VALUE_AFTER_PARAMETER_PREFIX.length());
                throw LauncherUtils.createUsageException("flag '" + flag.trim() + "' needs an argument");

            } else {
                // Make the first character of the error message lower case
                throw LauncherUtils.createUsageException(LauncherUtils.makeFirstLetterLowerCase(msg));
            }
        }
    }

    private static void printUsageInfo(JCommander cmdParser) {
        StringBuilder out = new StringBuilder();
        out.append("Ballerina is a general purpose, concurrent and strongly typed programming language \n");
        out.append("with both textual and graphical syntaxes, optimized for integration.\n");
        out.append("\n");
        out.append("* Find more information at http://ballerinalang.org\n");
        out.append("\n");
        out.append("Usage:\n");
        out.append("  ballerina [command] [options]\n");
        out.append("\n");

        out.append("Available Commands:\n");
        BLauncherCmd.printCommandList(cmdParser, out);

        out.append("\n");
        BLauncherCmd.printFlags(cmdParser.getParameters(), out);

        out.append("\n");
        out.append("Use \"ballerina help [command]\" for more information about a command.");
        outStream.println(out.toString());
    }

    private static void printVersionInfo() {
        try (InputStream inputStream = Main.class.getResourceAsStream("/META-INF/launcher.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);

            String version = "Ballerina " + properties.getProperty("ballerina.version") + "\n";
            outStream.print(version);
        } catch (Throwable ignore) {
            // Exception is ignored
            throw LauncherUtils.createUsageException("version info not available");
        }
    }

    /**
     * This class represents the "run" command and it holds arguments and flags specified by the user.
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "run", commandDescription = "compile and run Ballerina program")
    private static class RunCmd implements BLauncherCmd {

        private JCommander parentCmdParser;

        @Parameter(arity = 1, description = "arguments")
        private List<String> argList;

        @Parameter(names = {"--service", "-s"}, description = "run services instead of main")
        private boolean runServices;

        @Parameter(names = {"--sourceroot"}, description = "path to the directory containing source files and packages")
        private String sourceRoot;

        @Parameter(names = {"--help", "-h"}, hidden = true)
        private boolean helpFlag;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        @Parameter(names = "--ballerina.debug", hidden = true, description = "remote debugging port")
        private String ballerinaDebugPort;

        public void execute() {
            if (helpFlag) {
                String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "run");
                outStream.println(commandUsageInfo);
                return;
            }

            if (argList == null || argList.size() == 0) {
                throw LauncherUtils.createUsageException("no ballerina program given");
            }

            // Enable remote debugging
            if (null != ballerinaDebugPort) {
                System.setProperty(SYSTEM_PROP_BAL_DEBUG, ballerinaDebugPort);
            }

            Path sourceRootPath = LauncherUtils.getSourceRootPath(sourceRoot);

            // Start all services, if the services flag is set.
            if (runServices) {
                if (argList.size() > 1) {
                    throw LauncherUtils.createUsageException("too many arguments");
                }

                LauncherUtils.runProgram(sourceRootPath, Paths.get(argList.get(0)), true, new String[0]);
                return;
            }

            Path sourcePath = Paths.get(argList.get(0));
            // Filter out the list of arguments given to the ballerina program.
            String[] programArgs;
            if (argList.size() >= 2) {
                argList.remove(0);
                programArgs = argList.toArray(new String[0]);
            } else {
                programArgs = new String[0];
            }

            LauncherUtils.runProgram(sourceRootPath, sourcePath, false, programArgs);
        }

        @Override
        public String getName() {
            return "run";
        }

        @Override
        public void printLongDesc(StringBuilder out) {
            out.append("Run command runs a compiled Ballerina program. \n");
            out.append("\n");
            out.append("If a Ballerina source file or a source package is given, \n");
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
            out.append("  ballerina run [flags] <balfile | packagename | balxfile> [args...] \n");
        }

        @Override
        public void setParentCmdParser(JCommander parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }

        @Override
        public void setSelfCmdParser(JCommander selfCmdParser) {
        }
    }

    /**
     * This class represents the "help" command and it holds arguments and flags specified by the user.
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "help", commandDescription = "print usage information")
    private static class HelpCmd implements BLauncherCmd {

        @Parameter(description = "Command name")
        private List<String> helpCommands;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        private JCommander parentCmdParser;

        public void execute() {
            if (helpCommands == null) {
                printUsageInfo(parentCmdParser);
                return;

            } else if (helpCommands.size() > 1) {
                throw LauncherUtils.createUsageException("too many arguments given");
            }

            String userCommand = helpCommands.get(0);
            if (parentCmdParser.getCommands().get(userCommand) == null) {
                throw LauncherUtils.createUsageException("unknown help topic `" + userCommand + "`");
            }

            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, userCommand);
            outStream.println(commandUsageInfo);
        }

        @Override
        public String getName() {
            return "help";
        }

        @Override
        public void printLongDesc(StringBuilder out) {

        }

        @Override
        public void printUsage(StringBuilder out) {
        }

        @Override
        public void setParentCmdParser(JCommander parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }

        @Override
        public void setSelfCmdParser(JCommander selfCmdParser) {
        }
    }

    /**
     * This class represents the "version" command and it holds arguments and flags specified by the user.
     *
     * @since 0.8.1
     */
    @Parameters(commandNames = "version", commandDescription = "print Ballerina version")
    private static class VersionCmd implements BLauncherCmd {

        @Parameter(description = "Command name")
        private List<String> versionCommands;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        @Parameter(names = {"--help", "-h"}, hidden = true)
        private boolean helpFlag;

        private JCommander parentCmdParser;

        public void execute() {
            if (helpFlag) {
                String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "version");
                outStream.println(commandUsageInfo);
                return;
            }

            if (versionCommands == null) {
                printVersionInfo();
                return;
            } else if (versionCommands.size() > 1) {
                throw LauncherUtils.createUsageException("too many arguments given");
            }

            String userCommand = versionCommands.get(0);
            if (parentCmdParser.getCommands().get(userCommand) == null) {
                throw LauncherUtils.createUsageException("unknown command `" + userCommand + "`");
            }
        }

        @Override
        public String getName() {
            return "version";
        }

        @Override
        public void printLongDesc(StringBuilder out) {

        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("  ballerina version\n");
        }

        @Override
        public void setParentCmdParser(JCommander parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }

        @Override
        public void setSelfCmdParser(JCommander selfCmdParser) {

        }
    }

    /**
     * This class represents the "main" command required by the JCommander.
     *
     * @since 0.8.0
     */
    private static class DefaultCmd implements BLauncherCmd {

        @Parameter(names = {"--help", "-h"}, description = "for more information")
        private boolean helpFlag;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        private JCommander parentCmdParser;

        @Override
        public void execute() {
            printUsageInfo(parentCmdParser);
        }

        @Override
        public String getName() {
            return "default-cmd";
        }

        @Override
        public void printLongDesc(StringBuilder out) {

        }

        @Override
        public void printUsage(StringBuilder out) {
        }

        @Override
        public void setParentCmdParser(JCommander parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }

        @Override
        public void setSelfCmdParser(JCommander selfCmdParser) {
        }
    }
}

