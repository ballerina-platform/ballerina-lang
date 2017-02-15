package org.ballerinalang.launcher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import org.ballerinalang.BLangProgramArchiveBuilder;
import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangPrograms;
import org.wso2.ballerina.core.model.BLangProgram;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

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
        } catch (BLangRuntimeException e) {
            outStream.println(e.getMessage());
            Runtime.getRuntime().exit(1);
        } catch (Throwable e) {
            String msg = e.getMessage();
            if (msg == null) {
                outStream.println("ballerina: unexpected error occurred");
            } else {
                outStream.println("ballerina: " + LauncherUtils.makeFirstLetterUpperCase(msg));
            }
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
            defaultCmd.setJCommander(cmdParser);


            // Run command
            RunCmd runCmd = new RunCmd();
            JCommander jcRunCmd = addSubCommand(cmdParser, "run", runCmd);
            runCmd.setJCommander(jcRunCmd);

            // Run main command
            RunMainCmd runMainCmd = new RunMainCmd();
            addSubCommand(jcRunCmd, "main", runMainCmd);

            // Run service command
            RunServiceCmd runServiceCmd = new RunServiceCmd();
            addSubCommand(jcRunCmd, "service", runServiceCmd);


            // Build command
            BuildCmd buildCmd = new BuildCmd();
            JCommander jcBuildCmd = addSubCommand(cmdParser, "build", buildCmd);
            buildCmd.setJCommander(jcBuildCmd);

            // Build main command
            BuildMainCmd buildMainCmd = new BuildMainCmd();
            addSubCommand(jcBuildCmd, "main", buildMainCmd);

            // Build service command
            BuildServiceCmd buildServiceCmd = new BuildServiceCmd();
            addSubCommand(jcBuildCmd, "service", buildServiceCmd);


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

        private JCommander cmdParser;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        public void execute() {
            String parsedCmdName = cmdParser.getParsedCommand();
            if (parsedCmdName == null || parsedCmdName.isEmpty()) {
                throw LauncherUtils.createUsageException("unknown command ''");
            }

            Map<String, JCommander> commanderMap = cmdParser.getCommands();
            BLauncherCmd bLauncherCmd = (BLauncherCmd) commanderMap.get(parsedCmdName).getObjects().get(0);
            bLauncherCmd.execute();
        }

        @Override
        public String getName() {
            return "run";
        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("ballerina run <filename>\n");
        }

        void setJCommander(JCommander cmdParser) {
            this.cmdParser = cmdParser;
        }
    }

    /**
     * This class represents the "run" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "main", commandDescription = "run main  program")
    private static class RunMainCmd implements BLauncherCmd {

        @Parameter(arity = 1, description = "arguments")
        private List<String> argList;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

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

            Path sourcePath = Paths.get(argList.get(0));
            BProgramRunner.runMain(sourcePath, programArgs);
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
    @Parameters(commandNames = "service", commandDescription = "run service command")
    private static class RunServiceCmd implements BLauncherCmd {

        @Parameter(description = "The list of files to commit")
        private List<String> sourceFileList;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        @Parameter(names = {"--service-root", "-sr"}, description = "directory which contains ballerina services")
        private String serviceRootPath;

        public void execute() {
            if (serviceRootPath != null && !serviceRootPath.isEmpty()) {
                File serviceRoot = new File(serviceRootPath);
                File[] services = serviceRoot.listFiles();
                if (!serviceRoot.exists() || !serviceRoot.isDirectory()) {
                    throw LauncherUtils.createUsageException("service root '" + serviceRootPath + 
                            "' is not a valid directory");
                }
                
                if (sourceFileList == null) {
                    sourceFileList = new ArrayList<String>();
                }
                
                for (File service : services) {
                    if (service.toString().endsWith(BLangProgram.Category.SERVICE_PROGRAM.getExtension())) {
                        sourceFileList.add(service.toString());
                    }
                }
            }

            if (sourceFileList == null || sourceFileList.size() == 0) {
                throw LauncherUtils.createUsageException("no ballerina programs given");
            }

            Path[] paths = new Path[sourceFileList.size()];
            for (int i = 0; i < sourceFileList.size(); i++) {
                paths[i] = Paths.get(sourceFileList.get(i));
            }

            BProgramRunner.runServices(paths);
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
        private List<String> argList;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        private JCommander cmdParser;

        public void execute() {
            String parsedCmdName = cmdParser.getParsedCommand();
            if (parsedCmdName == null || parsedCmdName.isEmpty()) {
                throw LauncherUtils.createUsageException("unknown command ''");
            }

            Map<String, JCommander> commanderMap = cmdParser.getCommands();
            BLauncherCmd bLauncherCmd = (BLauncherCmd) commanderMap.get(parsedCmdName).getObjects().get(0);
            bLauncherCmd.execute();
        }

        @Override
        public String getName() {
            return "build";
        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("ballerina build <filename>...\n");
        }

        void setJCommander(JCommander cmdParser) {
            this.cmdParser = cmdParser;
        }
    }

    /**
     * This class represents the "build main" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "main", commandDescription = "build main program ")
    private static class BuildMainCmd implements BLauncherCmd {

        @Parameter(arity = 1, description = "The package to be added to the Ballerina repository ")
        private List<String> argList;

        @Parameter(names = {"-o"}, description = "filename")
        private String outputFileName;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        public void execute() {
            if (argList == null || argList.size() == 0) {
                throw LauncherUtils.createUsageException("no ballerina program given");
            }

            if (argList.size() > 1) {
                throw LauncherUtils.createUsageException("too many arguments");
            }

            Path sourcePath = Paths.get(argList.get(0));
            try {
                Path realPath = sourcePath.toRealPath(LinkOption.NOFOLLOW_LINKS);
                if (!Files.isDirectory(realPath, LinkOption.NOFOLLOW_LINKS)
                        && !realPath.toString().endsWith(BLangPrograms.BSOURCE_FILE_EXT)) {
                    throw new IllegalArgumentException("invalid file or package '" + sourcePath + "'");

                }
            } catch (NoSuchFileException x) {
                throw new IllegalArgumentException("no such file or directory: " + sourcePath);
            } catch (IOException e) {
                throw new RuntimeException("error reading from file: " + sourcePath +
                        " reason: " + e.getMessage(), e);
            }

            Path programDirPath = Paths.get(System.getProperty("user.dir"));
            BLangProgram bLangProgram = new BLangProgramLoader()
                    .loadMain(programDirPath, sourcePath);

            // TODO Delete existing file  or WARNING
            if (outputFileName == null || outputFileName.isEmpty()) {
                new BLangProgramArchiveBuilder().build(bLangProgram);
            } else {
                new BLangProgramArchiveBuilder().build(bLangProgram, outputFileName.trim());
            }
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
     * This class represents the "build service" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "service", commandDescription = "build service program ")
    private static class BuildServiceCmd implements BLauncherCmd {

        @Parameter(arity = 1, description = "The package to be added to the Ballerina repository ")
        private List<String> argList;

        @Parameter(names = {"-o"}, description = "filename")
        private String outputFileName;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        public void execute() {
            if (argList == null || argList.size() == 0) {
                throw LauncherUtils.createUsageException("no ballerina program given");
            }

            if (argList.size() > 1) {
                throw LauncherUtils.createUsageException("too many arguments");
            }

            Path sourcePath = Paths.get(argList.get(0));
            try {
                Path realPath = sourcePath.toRealPath(LinkOption.NOFOLLOW_LINKS);
                if (!Files.isDirectory(realPath, LinkOption.NOFOLLOW_LINKS)
                        && !realPath.toString().endsWith(BLangPrograms.BSOURCE_FILE_EXT)) {
                    throw new IllegalArgumentException("invalid file or package '" + sourcePath + "'");

                }
            } catch (NoSuchFileException x) {
                throw new IllegalArgumentException("no such file or directory: " + sourcePath);
            } catch (IOException e) {
                throw new RuntimeException("error reading from file: " + sourcePath +
                        " reason: " + e.getMessage(), e);
            }

            Path programDirPath = Paths.get(System.getProperty("user.dir"));
            BLangProgram bLangProgram = new BLangProgramLoader()
                    .loadService(programDirPath, sourcePath);

            // TODO Delete existing file  or WARNING
            if (outputFileName == null || outputFileName.isEmpty()) {
                new BLangProgramArchiveBuilder().build(bLangProgram);
            } else {
                new BLangProgramArchiveBuilder().build(bLangProgram, outputFileName.trim());
            }
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
    private static class DefaultCmd implements BLauncherCmd {

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

