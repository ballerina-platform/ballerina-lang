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

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import org.ballerinalang.config.cipher.AESCipherTool;
import org.ballerinalang.config.cipher.AESCipherToolException;
import org.ballerinalang.util.VMOptions;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.ParserException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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
            JCommander jcRunCmd = addSubCommand(cmdParser, BallerinaCliCommands.RUN, runCmd);
            runCmd.setParentCmdParser(cmdParser);
            runCmd.setSelfCmdParser(jcRunCmd);

            HelpCmd helpCmd = new HelpCmd();
            cmdParser.addCommand(BallerinaCliCommands.HELP, helpCmd);
            helpCmd.setParentCmdParser(cmdParser);

            // loading additional commands via SPI
            ServiceLoader<BLauncherCmd> bCmds = ServiceLoader.load(BLauncherCmd.class);
            for (BLauncherCmd bCmd : bCmds) {
                cmdParser.addCommand(bCmd.getName(), bCmd);
                bCmd.setParentCmdParser(cmdParser);
            }

            // Build Version Command
            VersionCmd versionCmd = new VersionCmd();
            cmdParser.addCommand(BallerinaCliCommands.VERSION, versionCmd);
            versionCmd.setParentCmdParser(cmdParser);

            EncryptCmd encryptCmd = new EncryptCmd();
            cmdParser.addCommand(BallerinaCliCommands.ENCRYPT, encryptCmd);
            encryptCmd.setParentCmdParser(cmdParser);

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

    private static void printUsageInfo(String commandName) {
        String usageInfo = BLauncherCmd.getCommandUsageInfo(commandName);
        outStream.println(usageInfo);
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

        @Parameter(names = {"--offline"})
        private boolean offline;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        @Parameter(names = "--java.debug", hidden = true, description = "remote java debugging port")
        private String javaDebugPort;

        @Parameter(names = {"--config", "-c"}, description = "path to the Ballerina configuration file")
        private String configFilePath;

        @Parameter(names = "--observe", description = "enable observability with default configs")
        private boolean observeFlag;

        @DynamicParameter(names = "-e", description = "Ballerina environment parameters")
        private Map<String, String> runtimeParams = new HashMap<>();

        @DynamicParameter(names = "-B", description = "Ballerina VM options")
        private Map<String, String> vmOptions = new HashMap<>();

        public void execute() {
            if (helpFlag) {
                printUsageInfo(BallerinaCliCommands.RUN);
                return;
            }

            if (argList == null || argList.size() == 0) {
                throw LauncherUtils.createUsageException("no ballerina program given");
            }

            // Enable remote debugging
            if (null != debugPort) {
                System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
            }

            Path sourceRootPath = LauncherUtils.getSourceRootPath(sourceRoot);
            System.setProperty("ballerina.source.root", sourceRootPath.toString());
            VMOptions.getInstance().addOptions(vmOptions);

            // Start all services, if the services flag is set.
            if (runServices) {
                if (argList.size() > 1) {
                    throw LauncherUtils.createUsageException("too many arguments");
                }

                LauncherUtils.runProgram(sourceRootPath, Paths.get(argList.get(0)), true, runtimeParams,
                        configFilePath, new String[0], offline, observeFlag);
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

            LauncherUtils.runProgram(sourceRootPath, sourcePath, false, runtimeParams, configFilePath,
                    programArgs, offline, observeFlag);
        }

        @Override
        public String getName() {
            return BallerinaCliCommands.RUN;
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

        @Parameter(names = "--java.debug", hidden = true)
        private String javaDebugPort;

        private JCommander parentCmdParser;

        public void execute() {
            if (helpCommands == null) {
                printUsageInfo(BallerinaCliCommands.HELP);
                return;

            } else if (helpCommands.size() > 1) {
                throw LauncherUtils.createUsageException("too many arguments given");
            }

            String userCommand = helpCommands.get(0);
            if (parentCmdParser.getCommands().get(userCommand) == null) {
                throw LauncherUtils.createUsageException("unknown help topic `" + userCommand + "`");
            }

            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(userCommand);
            outStream.println(commandUsageInfo);
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

        @Parameter(names = "--java.debug", hidden = true)
        private String javaDebugPort;

        @Parameter(names = {"--help", "-h"}, hidden = true)
        private boolean helpFlag;

        private JCommander parentCmdParser;

        public void execute() {
            if (helpFlag) {
                printUsageInfo(BallerinaCliCommands.VERSION);
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
        public void setParentCmdParser(JCommander parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }

        @Override
        public void setSelfCmdParser(JCommander selfCmdParser) {

        }
    }

    /**
     * Represents the encrypt command which can be used to make use of the AES cipher tool. This is for the users to be
     * able to encrypt sensitive values before adding them to config files.
     *
     * @since 0.966.0
     */
    @Parameters(commandNames = "encrypt", commandDescription = "encrypt sensitive data")
    public static class EncryptCmd implements BLauncherCmd {

        @Parameter(names = "--java.debug", hidden = true)
        private String javaDebugPort;

        @Parameter(names = {"--help", "-h"}, hidden = true)
        private boolean helpFlag;

        private JCommander parentCmdParser;

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

                outStream.println("Add the following to the runtime config:");
                outStream.println("@encrypted:{" + encryptedValue + "}\n");

                outStream.println("Or add to the runtime command line:");
                outStream.println("-e<param>=@encrypted:{" + encryptedValue + "}");
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
        public void setParentCmdParser(JCommander parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }

        @Override
        public void setSelfCmdParser(JCommander selfCmdParser) {

        }

        private String promptForInput(String msg) {
            outStream.println(msg);
            return new String(System.console().readPassword());
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

        @Parameter(names = "--debug <port>", description = "start Ballerina in remote debugging mode")
        private String debugPort;

        @Parameter(names = "--java.debug", hidden = true)
        private String javaDebugPort;

        private JCommander parentCmdParser;

        @Override
        public void execute() {
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
        public void setParentCmdParser(JCommander parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }

        @Override
        public void setSelfCmdParser(JCommander selfCmdParser) {
        }
    }
}
