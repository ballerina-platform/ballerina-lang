/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.launcher;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.compiler.BLangCompilerException;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * This class executes a Ballerina program.
 *
 * @since 0.8.0
 */
public class Main {
    private static final String UNMATCHED_ARGUMENT_PREFIX = "Unmatched argument";
    private static final String MISSING_REQUIRED_PARAMETER_PREFIX = "Missing required parameter";
    private static final String COMPILATION_ERROR_MESSAGE = "compilation contains errors";

    private static PrintStream errStream = System.err;
    private static PrintStream outStream = System.out;

    public static void main(String... args) {
        try {
            Optional<BLauncherCmd> optionalInvokedCmd = getInvokedCmd(args);
            optionalInvokedCmd.ifPresent(BLauncherCmd::execute);
        } catch (BLangRuntimeException e) {
            errStream.println(e.getMessage());
            Runtime.getRuntime().exit(1);
        } catch (BLangCompilerException e) {
            if (!(e.getMessage().contains(COMPILATION_ERROR_MESSAGE))) {
                // print the error message only if the exception was not thrown due to compilation errors
                errStream.println(prepareCompilerErrorMessage(e.getMessage()));
            }
            // These are compiler errors, and are already logged. Hence simply exit.
            Runtime.getRuntime().exit(1);
        } catch (BLauncherException e) {
            LauncherUtils.printLauncherException(e, errStream);
            Runtime.getRuntime().exit(1);
        } catch (RuntimePanicException e) {
            Runtime.getRuntime().exit(e.getExitCode());
        } catch (Throwable e) {
            RuntimeUtils.logBadSad(e);
            Runtime.getRuntime().exit(1);
        }
    }

    private static Optional<BLauncherCmd> getInvokedCmd(String... args) {
        try {
            DefaultCmd defaultCmd = new DefaultCmd();
            CommandLine cmdParser = new CommandLine(defaultCmd);
            defaultCmd.setParentCmdParser(cmdParser);

            // Set stop at positional before the other commands are added as sub commands, to enforce ordering only
            // for the run command
            cmdParser.setStopAtPositional(true);

            // loading additional commands via SPI
            ServiceLoader<BLauncherCmd> bCmds = ServiceLoader.load(BLauncherCmd.class);
            for (BLauncherCmd bCmd : bCmds) {
                cmdParser.addSubcommand(bCmd.getName(), bCmd);
                bCmd.setParentCmdParser(cmdParser);
            }

            HelpCmd helpCmd = new HelpCmd();
            cmdParser.addSubcommand(BallerinaCliCommands.HELP, helpCmd);
            helpCmd.setParentCmdParser(cmdParser);

            // set stop at positional to run command
            cmdParser.getSubcommands().get("run").setStopAtUnmatched(true).setStopAtPositional(true)
                    .setUnmatchedOptionsArePositionalParams(true)
                    // this is a workaround to distinguish between the program args when the project path
                    // is not provided
                    .setEndOfOptionsDelimiter("");
            cmdParser.getSubcommands().get("build").setStopAtUnmatched(true).setStopAtPositional(true);
            cmdParser.getSubcommands().get("test").setStopAtUnmatched(true).setStopAtPositional(true);

            // Build Version Command
            VersionCmd versionCmd = new VersionCmd();
            cmdParser.addSubcommand(BallerinaCliCommands.VERSION, versionCmd);
            versionCmd.setParentCmdParser(cmdParser);

            // Ballerina Home Command
            HomeCmd homeCmd = new HomeCmd();
            cmdParser.addSubcommand(BallerinaCliCommands.HOME, homeCmd);
            homeCmd.setParentCmdParser(cmdParser);

            cmdParser.setCommandName("bal");
            cmdParser.setPosixClusteredShortOptionsAllowed(false);


            List<CommandLine> parsedCommands = cmdParser.parse(args);

            if (defaultCmd.argList.size() > 0 && cmdParser.getSubcommands().get(defaultCmd.argList.get(0)) == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("unknown command '"
                        + defaultCmd.argList.get(0) + "'");
            }

            if (parsedCommands.size() < 1 || defaultCmd.helpFlag) {
                if (parsedCommands.size() > 1) {
                    defaultCmd.argList.add(parsedCommands.get(1).getCommandName());
                }

                return Optional.of(defaultCmd);
            }

            return Optional.of(parsedCommands.get(parsedCommands.size() - 1).getCommand());
        } catch (CommandLine.UnmatchedArgumentException e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("internal error occurred");
            }
            if (errorMessage.contains(UNMATCHED_ARGUMENT_PREFIX)) {
                throw LauncherUtils.createUsageExceptionWithHelp("unknown command "
                                                                    + getFirstUnknownArg(errorMessage));
            }
            throw LauncherUtils.createUsageExceptionWithHelp(LauncherUtils.makeFirstLetterLowerCase(errorMessage));
        } catch (CommandLine.ParameterException e) {
            String msg = e.getMessage();
            if (msg == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("internal error occurred");
            } else if (msg.startsWith(MISSING_REQUIRED_PARAMETER_PREFIX)) {
                    throw LauncherUtils.createUsageExceptionWithHelp("flag " + msg.substring(msg.indexOf("'"))
                                                                     + " needs an argument");
            }
            throw LauncherUtils.createUsageExceptionWithHelp(LauncherUtils.makeFirstLetterLowerCase(msg));
        }
    }

    private static void printUsageInfo(String commandName) {
        String usageInfo = BLauncherCmd.getCommandUsageInfo(commandName);
        errStream.println(usageInfo);
    }

    private static void printVersionInfo() {
        try (InputStream inputStream = Main.class.getResourceAsStream("/META-INF/tool.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);

            String output = "Ballerina " + properties.getProperty("ballerina.channel") + " " +
                    properties.getProperty("ballerina.versionDisplayName") + "\n";
            output += "Language specification " + properties.getProperty("spec.version") + "\n";
            outStream.print(output);
        } catch (Throwable ignore) {
            // Exception is ignored
            throw LauncherUtils.createUsageExceptionWithHelp("version info not available");
        }
    }

    private static void printBallerinaDistPath() {
        String balHome = System.getProperty("ballerina.home");
        if (balHome != null) {
            outStream.print(balHome + "\n");
        } else {
            throw LauncherUtils.createUsageExceptionWithHelp("home info not available");
        }
    }

    private static String prepareCompilerErrorMessage(String message) {
        return "error: " + LauncherUtils.makeFirstLetterLowerCase(message);
    }

    private static String getFirstUnknownArg(String errorMessage) {
        String optionsString = errorMessage.split(":")[1];
        return (optionsString.split(","))[0].trim();
    }

    /**
     * This class represents the "help" command and it holds arguments and flags specified by the user.
     *
     * @since 0.8.0
     */
    @CommandLine.Command(name = "help", description = "print usage information")
    private static class HelpCmd implements BLauncherCmd {

        @CommandLine.Parameters(description = "Command name")
        private List<String> helpCommands;

        private CommandLine parentCmdParser;

        public void execute() {
            if (helpCommands == null) {
                printUsageInfo(BallerinaCliCommands.HELP);
                return;

            } else if (helpCommands.size() > 1) {
                throw LauncherUtils.createUsageExceptionWithHelp("too many arguments given");
            }

            String userCommand = helpCommands.get(0);
            if (parentCmdParser.getSubcommands().get(userCommand) == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("unknown help topic `" + userCommand + "`");
            }

            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(userCommand);
            errStream.println(commandUsageInfo);
        }

        @Override
        public String getName() {
            return BallerinaCliCommands.HELP;
        }

        @Override
        public void printLongDesc(StringBuilder out) {

        }

        @Override
        public void printUsage(StringBuilder out) {
        }

        @Override
        public void setParentCmdParser(CommandLine parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }
    }

    /**
     * This class represents the "version" command and it holds arguments and flags specified by the user.
     *
     * @since 0.8.1
     */
    @CommandLine.Command(name = "version", description = "Prints Ballerina version")
    private static class VersionCmd implements BLauncherCmd {

        @CommandLine.Parameters(description = "Command name")
        private List<String> versionCommands;

        @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
        private boolean helpFlag;

        private CommandLine parentCmdParser;

        public void execute() {
            if (helpFlag) {
                printUsageInfo(BallerinaCliCommands.VERSION);
                return;
            }

            if (versionCommands == null) {
                printVersionInfo();
            }
        }

        @Override
        public String getName() {
            return BallerinaCliCommands.VERSION;
        }

        @Override
        public void printLongDesc(StringBuilder out) {

        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("  bal version\n");
        }

        @Override
        public void setParentCmdParser(CommandLine parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }
    }

    /**
     * This class represents the "home" command and it holds arguments and flags specified by the user.
     *
     * @since 1.0.0
     */
    @CommandLine.Command(name = "home", description = "Prints the path of current Ballerina dist")
    private static class HomeCmd implements BLauncherCmd {

        @CommandLine.Parameters(description = "Command name")
        private List<String> homeCommands;

        @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
        private boolean helpFlag;

        private CommandLine parentCmdParser;

        public void execute() {
            if (helpFlag) {
                printUsageInfo(BallerinaCliCommands.HOME);
                return;
            }

            if (homeCommands == null) {
                printBallerinaDistPath();
                return;
            } else if (homeCommands.size() > 1) {
                throw LauncherUtils.createUsageExceptionWithHelp("too many arguments given");
            }

            String userCommand = homeCommands.get(0);
            if (parentCmdParser.getSubcommands().get(userCommand) == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("unknown command " + userCommand);
            }
        }

        @Override
        public String getName() {
            return BallerinaCliCommands.HOME;
        }

        @Override
        public void printLongDesc(StringBuilder out) {

        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("  bal home\n");
        }

        @Override
        public void setParentCmdParser(CommandLine parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }
    }

    /**
     * This class represents the "default" command required by picocli.
     *
     * @since 0.8.0
     */
    @CommandLine.Command(description = "Default Command.", name = "default")
    private static class DefaultCmd implements BLauncherCmd {

        @CommandLine.Option(names = { "--help", "-h", "?" }, hidden = true, description = "for more information")
        private boolean helpFlag;

        // --debug flag is handled by ballerina.sh/ballerina.bat. It will launch ballerina with java debug options.
        @CommandLine.Option(names = "--debug", description = "start Ballerina in remote debugging mode")
        private String debugPort;

        @CommandLine.Option(names = { "--version", "-v" }, hidden = true)
        private boolean versionFlag;

        @CommandLine.Parameters(arity = "0..1")
        private List<String> argList = new ArrayList<>();

        @Override
        public void execute() {
            if (versionFlag) {
                printVersionInfo();
                return;
            }

            if (!argList.isEmpty()) {
                printUsageInfo(argList.get(0));
                return;
            }

            printUsageInfo(BallerinaCliCommands.HELP);
        }

        @Override
        public String getName() {
            return BallerinaCliCommands.DEFAULT;
        }

        @Override
        public void printLongDesc(StringBuilder out) {

        }

        @Override
        public void printUsage(StringBuilder out) {
        }

        @Override
        public void setParentCmdParser(CommandLine parentCmdParser) {
        }
    }
}
