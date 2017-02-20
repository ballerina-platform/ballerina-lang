package org.ballerinalang.launcher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import org.ballerinalang.BLangProgramArchiveBuilder;
import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.ParserException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangPrograms;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public static void main(String... args) {
        try {
            Optional<BLauncherCmd> optionalInvokedCmd = getInvokedCmd(args);

            LauncherUtils.writePID(System.getProperty("ballerina.home"));
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
                outStream.println("ballerina: unexpected error occurred");
            } else {
                outStream.println("ballerina: " + LauncherUtils.makeFirstLetterLowerCase(msg));
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
            defaultCmd.setParentCmdParser(cmdParser);


            // Run command
            RunCmd runCmd = new RunCmd();
            JCommander jcRunCmd = addSubCommand(cmdParser, "run", runCmd);
            runCmd.setParentCmdParser(cmdParser);
            runCmd.setSelfCmdParser(jcRunCmd);

            // Run main command
            RunMainCmd runMainCmd = new RunMainCmd();
            addSubCommand(jcRunCmd, "main", runMainCmd);
            runMainCmd.setParentCmdParser(jcRunCmd);

            // Run service command
            RunServiceCmd runServiceCmd = new RunServiceCmd();
            addSubCommand(jcRunCmd, "service", runServiceCmd);
            runServiceCmd.setParentCmdParser(jcRunCmd);


            // Build command
            BuildCmd buildCmd = new BuildCmd();
            JCommander jcBuildCmd = addSubCommand(cmdParser, "build", buildCmd);
            buildCmd.setParentCmdParser(cmdParser);
            buildCmd.setSelfCmdParser(jcBuildCmd);

            // Build main command
            BuildMainCmd buildMainCmd = new BuildMainCmd();
            addSubCommand(jcBuildCmd, "main", buildMainCmd);
            buildMainCmd.setParentCmdParser(jcBuildCmd);

            // Build service command
            BuildServiceCmd buildServiceCmd = new BuildServiceCmd();
            addSubCommand(jcBuildCmd, "service", buildServiceCmd);
            buildServiceCmd.setParentCmdParser(jcBuildCmd);


            HelpCmd helpCmd = new HelpCmd();
            cmdParser.addCommand("help", helpCmd);
            helpCmd.setParentCmdParser(cmdParser);

            // loading additional commands via SPI
            ServiceLoader<BLauncherCmd> bCmds = ServiceLoader.load(BLauncherCmd.class);
            for (BLauncherCmd bCmd : bCmds) {
                cmdParser.addCommand(bCmd.getName(), bCmd);
                bCmd.setParentCmdParser(cmdParser);
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
                throw LauncherUtils.createUsageException(LauncherUtils.makeFirstLetterLowerCase(msg));
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
        BLauncherCmd.printCommandList(cmdParser, out);

        out.append("\n");
        BLauncherCmd.printFlags(cmdParser.getParameters(), out);

        out.append("\n");
        out.append("Use \"ballerina help [command]\" for more information about a command.");
        outStream.println(out.toString());
    }

    /**
     * This class represents the "run" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "run", commandDescription = "run Ballerina main/service programs")
    private static class RunCmd implements BLauncherCmd {

        private JCommander parentCmdParser;
        private JCommander selfCmdParser;

        @Parameter(names = {"--help", "-h"}, hidden = true)
        private boolean helpFlag;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        public void execute() {
            if (helpFlag) {
                String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "run");
                outStream.println(commandUsageInfo);
                return;
            }

            String parsedCmdName = selfCmdParser.getParsedCommand();
            if (parsedCmdName == null || parsedCmdName.isEmpty()) {
                throw LauncherUtils.createUsageException("unknown command ''");
            }

            Map<String, JCommander> commanderMap = selfCmdParser.getCommands();
            BLauncherCmd bLauncherCmd = (BLauncherCmd) commanderMap.get(parsedCmdName).getObjects().get(0);
            bLauncherCmd.execute();
        }

        @Override
        public String getName() {
            return "run";
        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("  ballerina run main  <filename | packagename | archive>\n")
                    .append("  ballerina run service  <filename | packagename | archive>...\n");
        }

        @Override
        public void setParentCmdParser(JCommander parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }

        @Override
        public void setSelfCmdParser(JCommander selfCmdParser) {
            this.selfCmdParser = selfCmdParser;
        }
    }

    /**
     * This class represents the "run" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "main", commandDescription = "run Ballerina main program")
    private static class RunMainCmd implements BLauncherCmd {

        private JCommander parentCmdParser;

        @Parameter(arity = 1, description = "arguments")
        private List<String> argList;

        @Parameter(names = {"--help", "-h"}, hidden = true)
        private boolean helpFlag;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        @Parameter(names = "--ballerina.debug", hidden = true, description = "remote debugging port")
        private String ballerinaDebugPort;

        public void execute() {
            if (helpFlag) {
                String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "main");
                outStream.println(commandUsageInfo);
                return;
            }

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
            Path sourcePath = Paths.get(argList.get(0));
            BProgramRunner.runMain(sourcePath, programArgs);
        }

        @Override
        public String getName() {
            return "main";
        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("  ballerina run main  <filename | packagename | archive>\n");
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
     * This class represents the "service" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "service", commandDescription = "run Ballerina service programs")
    private static class RunServiceCmd implements BLauncherCmd {

        private JCommander parentCmdParser;

        @Parameter(description = "The list of files to commit")
        private List<String> sourceFileList;

        @Parameter(names = {"--help", "-h"}, hidden = true)
        private boolean helpFlag;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        @Parameter(names = {"--service-root", "-sr"}, description = "directory which contains ballerina services")
        private String serviceRootPath;

        @Parameter(names = "--ballerina.debug", hidden = true, description = "remote debugging port")
        private String ballerinaDebugPort;

        public void execute() {
            if (helpFlag) {
                String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "service");
                outStream.println(commandUsageInfo);
                return;
            }

            if (serviceRootPath != null && !serviceRootPath.isEmpty()) {
                if (sourceFileList != null && sourceFileList.size() != 0) {
                    throw LauncherUtils.createUsageException("too many arguments");
                }

                Path currentDir = Paths.get(System.getProperty("user.dir"));
                Path serviceRoot = Paths.get(serviceRootPath);
                try {
                    Path serviceRootRealPath = serviceRoot.toRealPath(LinkOption.NOFOLLOW_LINKS);
                    Path[] paths =
                            Files.list(serviceRootRealPath)
                                    .filter(path -> !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
                                    .filter(path -> path.getFileName().toString()
                                            .endsWith(BLangProgram.Category.SERVICE_PROGRAM.getExtension()))
                                    .map(currentDir::relativize).toArray(Path[]::new);
                    BProgramRunner.runServices(paths);
                    return;
                } catch (NoSuchFileException e) {
                    throw new IllegalArgumentException("no such file or directory: " + serviceRootPath);
                } catch (NotDirectoryException e) {
                    throw new IllegalArgumentException("given file is not a directory: " + serviceRootPath);
                } catch (IOException e) {
                    throw new RuntimeException("error reading from file: " + serviceRootPath + " reason: " +
                            e.getMessage(), e);
                }
            }

            if (sourceFileList == null || sourceFileList.size() == 0) {
                throw LauncherUtils.createUsageException("no ballerina programs given");
            }

            Path[] paths = new Path[sourceFileList.size()];
            for (int i = 0; i < sourceFileList.size(); i++) {
                paths[i] = Paths.get(sourceFileList.get(i));
            }

            if (null != ballerinaDebugPort) {
                System.setProperty(SYSTEM_PROP_BAL_DEBUG, ballerinaDebugPort);
            }
            BProgramRunner.runServices(paths);
        }

        @Override
        public String getName() {
            return "service";
        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("  ballerina run service  <filename | packagename | archive>...\n");
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
     * This class represents the "build" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "build", commandDescription = "create Ballerina program archives")
    private static class BuildCmd implements BLauncherCmd {

        private JCommander parentCmdParser;
        private JCommander selfCmdParser;

        @Parameter(arity = 1, description = "builds the given package with all the dependencies")
        private List<String> argList;

        @Parameter(names = {"--help", "-h"}, hidden = true)
        private boolean helpFlag;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        public void execute() {
            if (helpFlag) {
                String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "build");
                outStream.println(commandUsageInfo);
                return;
            }

            String parsedCmdName = selfCmdParser.getParsedCommand();
            if (parsedCmdName == null || parsedCmdName.isEmpty()) {
                throw LauncherUtils.createUsageException("unknown command ''");
            }

            Map<String, JCommander> commanderMap = selfCmdParser.getCommands();
            BLauncherCmd bLauncherCmd = (BLauncherCmd) commanderMap.get(parsedCmdName).getObjects().get(0);
            bLauncherCmd.execute();
        }

        @Override
        public String getName() {
            return "build";
        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("  ballerina build main  <packagename> [-o filename]\n")
                    .append("  ballerina build service <packagename>... [-o filename]\n");
        }

        @Override
        public void setParentCmdParser(JCommander parentCmdParser) {
            this.parentCmdParser = parentCmdParser;
        }

        @Override
        public void setSelfCmdParser(JCommander selfCmdParser) {
            this.selfCmdParser = selfCmdParser;
        }
    }

    /**
     * This class represents the "build main" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "main", commandDescription = "create main program archive")
    private static class BuildMainCmd implements BLauncherCmd {

        private JCommander parentCmdParser;

        @Parameter(arity = 1, description = "The package to be added to the Ballerina repository ")
        private List<String> argList;

        @Parameter(names = {"--help", "-h"}, hidden = true)
        private boolean helpFlag;

        @Parameter(names = {"-o"}, description = "output filename")
        private String outputFileName;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        public void execute() {
            if (helpFlag) {
                String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "main");
                outStream.println(commandUsageInfo);
                return;
            }

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
            return "main";
        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("  ballerina build main  <packagename> [-o filename]\n");
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
     * This class represents the "build service" command and it holds arguments and flags specified by the user
     *
     * @since 0.8.0
     */
    @Parameters(commandNames = "service", commandDescription = "create service program archive")
    private static class BuildServiceCmd implements BLauncherCmd {

        private JCommander parentCmdParser;

        @Parameter(arity = 1, description = "The package to be added to the Ballerina repository ")
        private List<String> argList;

        @Parameter(names = {"--help", "-h"}, hidden = true)
        private boolean helpFlag;

        @Parameter(names = {"-o"}, description = "output filename")
        private String outputFileName;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        public void execute() {
            if (helpFlag) {
                String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "service");
                outStream.println(commandUsageInfo);
                return;
            }

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
            return "service";
        }

        @Override
        public void printUsage(StringBuilder out) {
            out.append("  ballerina build service <packagename>... [-o filename]\n");
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
     * This class represents the "main" command required by the JCommander
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

