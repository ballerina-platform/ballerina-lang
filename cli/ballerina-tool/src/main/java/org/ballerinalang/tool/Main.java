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

package org.ballerinalang.tool;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.config.cipher.AESCipherTool;
import org.ballerinalang.config.cipher.AESCipherToolException;
import org.ballerinalang.tool.util.BCompileUtil;
import org.ballerinalang.tool.util.ToolUtil;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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

    private static final Logger breLog = LoggerFactory.getLogger(Main.class);

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
            Runtime.getRuntime().exit(1);
        } catch (BLauncherException e) {
            LauncherUtils.printLauncherException(e, errStream);
            Runtime.getRuntime().exit(1);
        } catch (Throwable e) {
            errStream.println(getMessageForInternalErrors());
            breLog.error(e.getMessage(), e);
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

            HelpCmd helpCmd = new HelpCmd();
            cmdParser.addSubcommand(BallerinaCliCommands.HELP, helpCmd);
            helpCmd.setParentCmdParser(cmdParser);

            // loading additional commands via SPI
            ServiceLoader<BLauncherCmd> bCmds = ServiceLoader.load(BLauncherCmd.class);
            for (BLauncherCmd bCmd : bCmds) {
                cmdParser.addSubcommand(bCmd.getName(), bCmd);
                bCmd.setParentCmdParser(cmdParser);
            }

            // Build Version Command
            VersionCmd versionCmd = new VersionCmd();
            cmdParser.addSubcommand(BallerinaCliCommands.VERSION, versionCmd);
            versionCmd.setParentCmdParser(cmdParser);

            // Ballerina Home Command
            HomeCmd homeCmd = new HomeCmd();
            cmdParser.addSubcommand(BallerinaCliCommands.HOME, homeCmd);
            homeCmd.setParentCmdParser(cmdParser);

            EncryptCmd encryptCmd = new EncryptCmd();
            cmdParser.addSubcommand(BallerinaCliCommands.ENCRYPT, encryptCmd);
            encryptCmd.setParentCmdParser(cmdParser);

            //DistCmd Command
            DistCmd distCmd = new DistCmd();
            CommandLine distCmdParser = new CommandLine(distCmd);
            distCmd.setParentCmdParser(distCmdParser);

            ListCmd listCmd = new ListCmd();
            distCmdParser.addSubcommand(BallerinaCliCommands.LIST, listCmd);
            listCmd.setParentCmdParser(distCmdParser);

            PullCmd pullCmd = new PullCmd();
            distCmdParser.addSubcommand(BallerinaCliCommands.PULL, pullCmd);
            pullCmd.setParentCmdParser(distCmdParser);

            UseCmd useCmd = new UseCmd();
            distCmdParser.addSubcommand(BallerinaCliCommands.USE, useCmd);
            useCmd.setParentCmdParser(distCmdParser);

            UpdateCmd updateCmd = new UpdateCmd();
            distCmdParser.addSubcommand(BallerinaCliCommands.UPDATE, updateCmd);
            updateCmd.setParentCmdParser(distCmdParser);

            RemoveCmd removeCmd = new RemoveCmd();
            distCmdParser.addSubcommand(BallerinaCliCommands.REMOVE, removeCmd);
            removeCmd.setParentCmdParser(distCmdParser);

            distCmdParser.setCommandName("dist");
            distCmdParser.setPosixClusteredShortOptionsAllowed(false);

            cmdParser.addSubcommand(BallerinaCliCommands.DIST, distCmdParser);

            cmdParser.setCommandName("ballerina");
            cmdParser.setPosixClusteredShortOptionsAllowed(false);


            List<CommandLine> parsedCommands = cmdParser.parse(args);

            if (parsedCommands.size() < 1) {
                return Optional.of(defaultCmd);
            }

            return Optional.of(parsedCommands.get(parsedCommands.size() - 1).getCommand());
        } catch (CommandLine.UnmatchedArgumentException e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("internal error occurred");
            }
            if (errorMessage.contains(UNMATCHED_ARGUMENT_PREFIX)) {
                throw LauncherUtils.createUsageExceptionWithHelp("unknown command '" + getFirstUnknownArg(errorMessage)
                                                                 + "'");
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

            String output = "Ballerina " + properties.getProperty("ballerina.version") + "\n";
            output += "Language specification " + properties.getProperty("spec.version") + "\n";
            output += "Ballerina tool " + properties.getProperty("tool.version") + "\n";
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

    private static String getMessageForInternalErrors() {
        String errorMsg;
        try {
            errorMsg = BCompileUtil.readFileAsString("cli-help/internal-error-message.txt");
        } catch (IOException e) {
            errorMsg = "ballerina: internal error occurred";
        }
        return errorMsg;
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
                return;
            } else if (versionCommands.size() > 1) {
                throw LauncherUtils.createUsageExceptionWithHelp("too many arguments given");
            }

            String userCommand = versionCommands.get(0);
            if (parentCmdParser.getSubcommands().get(userCommand) == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("unknown command `" + userCommand + "`");
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
            out.append("  ballerina version\n");
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
                throw LauncherUtils.createUsageExceptionWithHelp("unknown command `" + userCommand + "`");
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
            out.append("  ballerina home\n");
        }

        @Override
        public void setParentCmdParser(CommandLine parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }
    }

    /**
     * Represents the encrypt command which can be used to make use of the AES cipher tool. This is for the users to be
     * able to encrypt sensitive values before adding them to config files.
     *
     * @since 0.966.0
     */
    @CommandLine.Command(name = "encrypt", description = "encrypt sensitive data")
    public static class EncryptCmd implements BLauncherCmd {

        @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
        private boolean helpFlag;

        @Override
        public void execute() {
            if (helpFlag) {
                printUsageInfo(BallerinaCliCommands.ENCRYPT);
                return;
            }

            String value;
            if ((value = promptForInput("Enter value: ")).trim().isEmpty()) {
                if (value.trim().isEmpty()) {
                    value = promptForInput("Value cannot be empty; enter value: ");
                    if (value.trim().isEmpty()) {
                        throw LauncherUtils.createLauncherException("encryption failed: empty value.");
                    }
                }
            }

            String secret;
            if ((secret = promptForInput("Enter secret: ")).trim().isEmpty()) {
                if (secret.trim().isEmpty()) {
                    secret = promptForInput("Secret cannot be empty; enter secret: ");
                    if (secret.trim().isEmpty()) {
                        throw LauncherUtils.createLauncherException("encryption failed: empty secret.");
                    }
                }
            }

            String secretVerifyVal = promptForInput("Re-enter secret to verify: ");

            if (!secret.equals(secretVerifyVal)) {
                throw LauncherUtils.createLauncherException("secrets did not match.");
            }

            try {
                AESCipherTool cipherTool = new AESCipherTool(secret);
                String encryptedValue = cipherTool.encrypt(value);

                errStream.println("Add the following to the runtime config:");
                errStream.println("@encrypted:{" + encryptedValue + "}\n");

                errStream.println("Or add to the runtime command line:");
                errStream.println("-e<param>=@encrypted:{" + encryptedValue + "}");
            } catch (AESCipherToolException e) {
                throw LauncherUtils.createLauncherException("failed to encrypt value: " + e.getMessage());
            }
        }

        @Override
        public String getName() {
            return BallerinaCliCommands.ENCRYPT;
        }

        @Override
        public void printLongDesc(StringBuilder out) {
            out.append("The encrypt command can be used to encrypt sensitive data.\n\n");
            out.append("When the command is executed, the user will be prompted to\n");
            out.append("enter the value to be encrypted and a secret. The secret will be used in \n");
            out.append("encrypting the value.\n\n");
            out.append("Once encrypted, the user can place the encrypted value in the config files,\n");
            out.append("similar to the following example:\n");
            out.append("\tuser.password=\"@encrypted:{UtD9d+o6eHpqFnBxtvhb+RWXey7qm7xLMt6+6mrt9w0=}\"\n\n");
            out.append("The Ballerina Config API will automatically decrypt the values on-demand.\n");
        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("  ballerina encrypt\n");
        }

        @Override
        public void setParentCmdParser(CommandLine parentCmdParser) {
        }

        private String promptForInput(String msg) {
            errStream.println(msg);
            return new String(System.console().readPassword());
        }
    }

    /**
     * This class represents the "Update" command and it holds arguments and flags specified by the user.
     *
     * @since 1.0
     */
    @CommandLine.Command(name = "dist", description = "Ballerina distribution commands")
    private static class DistCmd implements BLauncherCmd {

        @CommandLine.Option(names = { "--help", "-h", "?" }, hidden = true, description = "for more information")
        private boolean helpFlag;

        @Override
        public void execute() {
            if (helpFlag) {
                printUsageInfo(BallerinaCliCommands.HELP);
                return;
            }

            printUsageInfo(BallerinaCliCommands.DIST);
        }

        @Override
        public String getName() {
            return BallerinaCliCommands.DIST;
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

    /**
     * This class represents the "Update" command and it holds arguments and flags specified by the user.
     *
     * @since 1.0
     */
    @CommandLine.Command(name = "list", description = "List Ballerina Distributions")
    private static class ListCmd implements BLauncherCmd {

        @CommandLine.Parameters(description = "Command name")
        private List<String> listCommands;

        @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
        private boolean helpFlag;

        @CommandLine.Option(names = {"--remote"}, hidden = true)
        private boolean remoteFlag;

        private CommandLine parentCmdParser;

        public void execute() {
            if (helpFlag) {
                printUsageInfo(BallerinaCliCommands.LIST);
                return;
            }

            if (listCommands == null) {
                ToolUtil.listDistributions(outStream, remoteFlag);
                return;
            } else if (listCommands.size() > 1) {
                throw LauncherUtils.createUsageExceptionWithHelp("too many arguments given");
            }

            String userCommand = listCommands.get(0);
            if (parentCmdParser.getSubcommands().get(userCommand) == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("unknown command `" + userCommand + "`");
            }
        }

        @Override
        public String getName() {
            return BallerinaCliCommands.LIST;
        }

        @Override
        public void printLongDesc(StringBuilder out) {

        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("  ballerina list\n");
        }

        @Override
        public void setParentCmdParser(CommandLine parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }
    }

    /**
     * This class represents the "Update" command and it holds arguments and flags specified by the user.
     *
     * @since 1.0
     */
    @CommandLine.Command(name = "pull", description = "Pull Ballerina distribution")
    private static class PullCmd implements BLauncherCmd {

        @CommandLine.Parameters(description = "Command name")
        private List<String> pullCommands;

        @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
        private boolean helpFlag;

        private CommandLine parentCmdParser;

        public void execute() {
            if (helpFlag) {
                printUsageInfo(BallerinaCliCommands.PULL);
                return;
            }

            if (pullCommands == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("distribution is not provided");
            } else if (pullCommands.size() == 1) {
                ToolUtil.install(outStream, pullCommands.get(0));
                return;
            } else if (pullCommands.size() > 1) {
                throw LauncherUtils.createUsageExceptionWithHelp("too many arguments given");
            }

            String userCommand = pullCommands.get(0);
            if (parentCmdParser.getSubcommands().get(userCommand) == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("unknown command `" + userCommand + "`");
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
            out.append("  install update\n");
        }

        @Override
        public void setParentCmdParser(CommandLine parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }
    }

    /**
     * This class represents the "Update" command and it holds arguments and flags specified by the user.
     *
     * @since 1.0
     */
    @CommandLine.Command(name = "use", description = "Use Ballerina distribution")
    private static class UseCmd implements BLauncherCmd {

        @CommandLine.Parameters(description = "Command name")
        private List<String> useCommands;

        @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
        private boolean helpFlag;

        private CommandLine parentCmdParser;

        public void execute() {
            if (helpFlag) {
                printUsageInfo(BallerinaCliCommands.USE);
                return;
            }

            if (useCommands == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("distribution is not provided");
            } else if (useCommands.size() == 1) {
                ToolUtil.use(outStream, useCommands.get(0));
                return;
            } else if (useCommands.size() > 1) {
                throw LauncherUtils.createUsageExceptionWithHelp("too many arguments given");
            }

            String userCommand = useCommands.get(0);
            if (parentCmdParser.getSubcommands().get(userCommand) == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("unknown command `" + userCommand + "`");
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
            out.append("  install update\n");
        }

        @Override
        public void setParentCmdParser(CommandLine parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }
    }

    /**
     * This class represents the "Update" command and it holds arguments and flags specified by the user.
     *
     * @since 1.0
     */
    @CommandLine.Command(name = "update", description = "Update Ballerina current distribution")
    private static class UpdateCmd implements BLauncherCmd {

        @CommandLine.Parameters(description = "Command name")
        private List<String> updateCommands;

        @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
        private boolean helpFlag;

        private CommandLine parentCmdParser;

        public void execute() {
            if (helpFlag) {
                printUsageInfo(BallerinaCliCommands.UPDATE);
                return;
            }

            if (updateCommands == null) {
                ToolUtil.update(outStream, BallerinaCliCommands.VERSION);
                return;
            } else if (updateCommands.size() > 1) {
                throw LauncherUtils.createUsageExceptionWithHelp("too many arguments given");
            }

            String userCommand = updateCommands.get(0);
            if (parentCmdParser.getSubcommands().get(userCommand) == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("unknown command `" + userCommand + "`");
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
            out.append("  ballerina update\n");
        }

        @Override
        public void setParentCmdParser(CommandLine parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }
    }

    /**
     * This class represents the "Remove" command and it holds arguments and flags specified by the user.
     *
     * @since 1.0
     */
    @CommandLine.Command(name = "remove", description = "Remove Ballerina distribution")
    private static class RemoveCmd implements BLauncherCmd {

        @CommandLine.Parameters(description = "Command name")
        private List<String> removeCommands;

        @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
        private boolean helpFlag;

        private CommandLine parentCmdParser;

        public void execute() {
            if (helpFlag) {
                printUsageInfo(BallerinaCliCommands.REMOVE);
                return;
            }

            if (removeCommands == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("distribution is not provided");
            } else if (removeCommands.size() == 1) {
                ToolUtil.remove(outStream, removeCommands.get(0));
                return;
            } else if (removeCommands.size() > 1) {
                throw LauncherUtils.createUsageExceptionWithHelp("too many arguments given");
            }

            String userCommand = removeCommands.get(0);
            if (parentCmdParser.getSubcommands().get(userCommand) == null) {
                throw LauncherUtils.createUsageExceptionWithHelp("unknown command `" + userCommand + "`");
            }
        }

        @Override
        public String getName() {
            return BallerinaCliCommands.REMOVE;
        }

        @Override
        public void printLongDesc(StringBuilder out) {

        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("  ballerina remove\n");
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

        @CommandLine.Option(names = "--debug", description = "start Ballerina in remote debugging mode")
        private String debugPort;

        @CommandLine.Option(names = { "--version", "-v" }, hidden = true)
        private boolean versionFlag;

        @Override
        public void execute() {
            if (helpFlag) {
                printUsageInfo(BallerinaCliCommands.HELP);
                return;
            }

            if (versionFlag) {
                printVersionInfo();
                return;
            }

            printUsageInfo(BallerinaCliCommands.DEFAULT);
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
