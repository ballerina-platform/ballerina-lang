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

package org.ballerinalang.launcher;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.config.cipher.AESCipherTool;
import org.ballerinalang.config.cipher.AESCipherToolException;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.util.VMOptions;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;

import static org.ballerinalang.runtime.Constants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.util.BLangConstants.COLON;
import static org.ballerinalang.util.BLangConstants.MAIN_FUNCTION_NAME;

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

    private static CommandLine addSubCommand(CommandLine parentCmd, String commandName, Object commandObject) {
        parentCmd.addSubcommand(commandName, commandObject);
        return parentCmd.getSubcommands().get(commandName);
    }

    private static Optional<BLauncherCmd> getInvokedCmd(String... args) {
        try {
            DefaultCmd defaultCmd = new DefaultCmd();
            CommandLine cmdParser = new CommandLine(defaultCmd);
            defaultCmd.setParentCmdParser(cmdParser);

            // Run command
            RunCmd runCmd = new RunCmd();
            CommandLine pcRunCmd = addSubCommand(cmdParser, BallerinaCliCommands.RUN, runCmd);
            runCmd.setParentCmdParser(cmdParser);
            runCmd.setSelfCmdParser(pcRunCmd);

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

            EncryptCmd encryptCmd = new EncryptCmd();
            cmdParser.addSubcommand(BallerinaCliCommands.ENCRYPT, encryptCmd);
            encryptCmd.setParentCmdParser(cmdParser);

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
        try (InputStream inputStream = Main.class.getResourceAsStream("/META-INF/launcher.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);

            String version = "Ballerina " + properties.getProperty("ballerina.version") + "\n";
            outStream.print(version);
        } catch (Throwable ignore) {
            // Exception is ignored
            throw LauncherUtils.createUsageExceptionWithHelp("version info not available");
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
     * This class represents the "run" command and it holds arguments and flags specified by the user.
     *
     * @since 0.8.0
     */
    @CommandLine.Command(name = "run", description = "compile and run Ballerina programs")
    private static class RunCmd implements BLauncherCmd {

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

        @CommandLine.Option(names = "--printreturn", description = "print return value to the out stream")
        private boolean printReturn;

        @CommandLine.Option(names = "-e", description = "Ballerina environment parameters")
        private Map<String, String> runtimeParams = new HashMap<>();

        @CommandLine.Option(names = "-B", description = "Ballerina VM options")
        private Map<String, String> vmOptions = new HashMap<>();

        public void execute() {
            if (helpFlag) {
                printUsageInfo(BallerinaCliCommands.RUN);
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
            String functionName = MAIN_FUNCTION_NAME;
            Path sourcePath = Paths.get(programArg);
            if (programArg.contains(COLON) && !Files.exists(sourceRootPath.resolve(programArg))) {
                int splitIndex = getSourceFunctionSplitIndex(sourceRootPath, programArg);
                if (splitIndex != -1) {
                    sourcePath = Paths.get(programArg.substring(0, splitIndex));
                    functionName = programArg.substring(splitIndex + 1);
                    if (functionName.isEmpty() || programArg.endsWith(COLON)) {
                        throw LauncherUtils.createUsageExceptionWithHelp("expected function name after final ':'");
                    }
                }
            }

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
            LauncherUtils.runProgram(sourceRootPath, sourcePath.normalize(), functionName, runtimeParams,
                                     configFilePath, programArgs, offline, observeFlag, printReturn);
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
            out.append("  ballerina run [flags] <balfile | module-name | balxfile> [args...] \n");
        }

        @Override
        public void setParentCmdParser(CommandLine parentCmdParser) {
        }

        @Override
        public void setSelfCmdParser(CommandLine selfCmdParser) {
        }

        /**
         * Retrieve the position of the colon to split at to separate source path and the name of the function to run if
         * specified.
         *
         * Returns the index of the colon, on which when split, the first part is a valid path and the second could
         * correspond to the function.
         *
         * @param sourceRootPath the path to the source root
         * @param programArg     the program argument specified
         * @return  the index of the colon to split at
         */
        private int getSourceFunctionSplitIndex(Path sourceRootPath, String programArg) {
            String[] programArgConstituents = programArg.split(COLON);
            int index = programArgConstituents.length - 1;

            String potentialFunction = programArgConstituents[index];
            String potentialPath = programArg.replace(COLON.concat(potentialFunction), "");
            if (Files.exists(sourceRootPath.resolve(potentialPath))) {
                return potentialPath.length();
            }
            index--;

            while (index != -1) {
                potentialFunction = programArgConstituents[index].concat(COLON).concat(potentialFunction);
                potentialPath = programArg.replace(COLON.concat(potentialFunction), "");

                if (Files.exists(sourceRootPath.resolve(potentialPath))) {
                    return potentialPath.length();
                }

                index--;
            }
            return index;
        }
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

        @Override
        public void setSelfCmdParser(CommandLine selfCmdParser) {
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

        @Override
        public void setSelfCmdParser(CommandLine selfCmdParser) {

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

        @Override
        public void setSelfCmdParser(CommandLine selfCmdParser) {

        }

        private String promptForInput(String msg) {
            errStream.println(msg);
            return new String(System.console().readPassword());
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

        @Override
        public void setSelfCmdParser(CommandLine selfCmdParser) {
        }
    }
}
