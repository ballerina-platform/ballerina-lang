/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.cli.cmd;

import io.ballerina.cli.task.CleanTargetDirTask;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.cli.task.CreateBaloTask;
import io.ballerina.cli.task.CreateExecutableTask;
import io.ballerina.cli.task.CreateJarTask;
import io.ballerina.cli.task.CreateTargetDirTask;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.environment.ProjectEnvironmentContext;
import org.ballerinalang.compiler.CompilerPhase;
import io.ballerina.cli.TaskExecutor;
import io.ballerina.cli.task.CreateBirTask;
import org.ballerinalang.jvm.launch.LaunchUtils;
import org.ballerinalang.tool.BLauncherCmd;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static io.ballerina.cli.cmd.Constants.BUILD_COMMAND;

/**
 * This class represents the "ballerina build" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = BUILD_COMMAND, description = "Ballerina build - Build Ballerina module(s) and generate " +
                                                         "executable output.")
public class BuildCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private Path projectPath;
    private boolean exitWhenFinish;
    private boolean skipCopyLibsFromDist;

    public BuildCommand() {
        this.projectPath = Paths.get(System.getProperty("user.dir"));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
        this.skipCopyLibsFromDist = false;
    }

    public BuildCommand(Path userDir, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                        boolean skipCopyLibsFromDist) {
        this.projectPath = userDir;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
    }

    public BuildCommand(Path userDir, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                        boolean skipCopyLibsFromDist, Path executableOutputDir) {
        this.projectPath = userDir;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
        this.output = executableOutputDir.toString();
    }

//    @CommandLine.Option(names = {"--sourceroot"},
//                        description = "Path to the directory containing the source files and modules")
//    private String sourceRoot;

    @CommandLine.Option(names = {"--compile", "-c"}, description = "Compile the source without generating " +
                                                                   "executable(s).")
    private boolean compile;

//    @CommandLine.Option(names = {"--all", "-a"}, description = "Build or compile all the modules of the project.")
//    private boolean buildAll;

    @CommandLine.Option(names = {"--output", "-o"}, description = "Write the output to the given file. The provided " +
                                                                  "output file name may or may not contain the " +
                                                                  "'.jar' extension.")
    private String output;

//    @CommandLine.Option(names = {"--offline"}, description = "Build/Compile offline without downloading " +
//                                                              "dependencies.")
    private boolean offline;
//
//    @CommandLine.Option(names = {"--skip-lock"}, description = "Skip using the lock file to resolve dependencies.")
    private boolean skipLock;
//
//    @CommandLine.Option(names = {"--skip-tests"}, description = "Skip test compilation and execution.")
    private boolean skipTests;

    @CommandLine.Parameters
    private List<String> argList;

//    @CommandLine.Option(names = {"--native"}, hidden = true,
//                        description = "Compile a Ballerina program to a native binary.")
//    private boolean nativeBinary;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--experimental", description = "Enable experimental language features.")
    private boolean experimentalFlag;

    @CommandLine.Option(names = "--debug", description = "run tests in remote debugging mode")
    private String debugPort;

    private static final String buildCmd = "ballerina build [-o <output>] [--sourceroot] [--offline] [--skip-tests]\n" +
            "                    [--skip-lock] {<ballerina-file | module-name> | -a | --all} [--] [(--key=value)...]";

//    @CommandLine.Option(names = "--test-report", description = "enable test report generation")
//    private boolean testReport;
//
//    @CommandLine.Option(names = "--code-coverage", description = "enable code coverage")
//    private boolean coverage;

//    @CommandLine.Option(names = "--observability-included", description = "package observability in the executable " +
//            "JAR file(s).")
//    private boolean observabilityIncluded;

    public void execute() {
        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(BUILD_COMMAND);
            this.errStream.println(commandUsageInfo);
            return;
        }

//        String[] args;
//        if (this.argList == null) {
//            args = new String[0];
//        } else if (this.buildAll) {
//            args = this.argList.toArray(new String[0]);
//        } else {
//            args = argList.subList(1, argList.size()).toArray(new String[0]);
//        }
//
//        // Sets the debug port as a system property, which will be used when setting up debug args before running tests.
//        if (!this.skipTests && this.debugPort != null) {
//            System.setProperty(SYSTEM_PROP_BAL_DEBUG, this.debugPort);
//        }
//
//        String[] userArgs = LaunchUtils.getUserArgs(args, new HashMap<>());
//        // check if there are too many arguments.
//        if (userArgs.length > 0) {
//            CommandUtil.printError(this.errStream, "too many arguments.", buildCmd, false);
//            CommandUtil.exitError(this.exitWhenFinish);
//            return;
//        }
//
//        // If -a or --all is not given, then it is mandatory to give a module name or a Ballerina file as the arg.
//        if (!this.buildAll && (this.argList == null || this.argList.size() == 0)) {
//            CommandUtil.printError(this.errStream,
//                    "'build' command requires a module name or a Ballerina file to build/compile. Use '-a' or " +
//                    "'--all' to build/compile all the modules of the project.",
//                    "ballerina build {<ballerina-file> | <module-name> | -a | --all}",
//                    false);
//            CommandUtil.exitError(this.exitWhenFinish);
//            return;
//        }
//
//        // Validate and decide the source root and the full path of the source.
//        this.sourceRootPath = null != this.sourceRoot ?
//                Paths.get(this.sourceRoot).toAbsolutePath() : this.sourceRootPath;
//        Path sourcePath = null;
//        Path targetPath;
//
//        // When -a or --all is provided, check if the command is executed within a Ballerina project. Update source
//        // root path if the command is executed inside a project.
//        if (this.buildAll) {
//            //// Check if the output flag is set when building all the modules.
//            if (null != this.output) {
//                CommandUtil.printError(this.errStream,
//                                       "'-o' and '--output' are only supported when building a single Ballerina " +
//                                               "file.",
//                                       "ballerina build -o <output-file> <ballerina-file> ",
//                                       true);
//                CommandUtil.exitError(this.exitWhenFinish);
//                return;
//            }
//
//            //// Validate and set the path of the source root.
//            if (!ProjectDirs.isProject(this.sourceRootPath)) {
//                Path findRoot = ProjectDirs.findProjectRoot(this.sourceRootPath);
//                if (null == findRoot) {
//                    CommandUtil.printError(this.errStream,
//                                           "you are trying to build/compile a Ballerina project that does not have a " +
//                                                   "Ballerina.toml file.",
//                                           null,
//                                           false);
//                    CommandUtil.exitError(this.exitWhenFinish);
//                    return;
//                }
//
//                this.sourceRootPath = findRoot;
//            }
//
//            targetPath = this.sourceRootPath.resolve(ProjectConstants.TARGET_DIR_NAME);
//
//        } else if (this.argList.get(0).endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
//            // TODO: remove this once code cov is implemented to support single bal file
//            if (coverage) {
//                coverage = false;
//                this.outStream.println("Code coverage is not yet supported with single bal files. Ignoring the flag " +
//                        "and continuing the test run...");
//            }
//            // when a single bal file is provided.
//            if (this.compile) {
//                CommandUtil.printError(this.errStream,
//                                       "'-c' or '--compile' can only be used with modules.", null, false);
//                CommandUtil.exitError(this.exitWhenFinish);
//                return;
//            } else {
//                //// check if path given is an absolute path. update source root accordingly.
//                if (Paths.get(this.argList.get(0)).isAbsolute()) {
//                    sourcePath = Paths.get(this.argList.get(0));
//                } else {
//                    sourcePath = this.sourceRootPath.resolve(this.argList.get(0));
//                }
//                this.sourceRootPath = sourcePath.getParent();
//                //// check if the given file exists.
//                if (Files.notExists(sourcePath)) {
//                    CommandUtil.printError(this.errStream,
//                                           "'" + sourcePath + "' Ballerina file does not exist.",
//                                           null,
//                                           false);
//                    CommandUtil.exitError(this.exitWhenFinish);
//                    return;
//                }
//
//                //// check if the given file is a regular file and not a symlink.
//                if (!Files.isRegularFile(sourcePath)) {
//                    CommandUtil.printError(this.errStream,
//                                           "'" + sourcePath +
//                                                   "' is not a Ballerina file. Check if it is a symlink or a shortcut.",
//                                           null,
//                                           false);
//                    CommandUtil.exitError(this.exitWhenFinish);
//                    return;
//                }
//
//                try {
//                    targetPath = Files.createTempDirectory("ballerina-build-" + System.nanoTime());
//                } catch (IOException e) {
//                    throw LauncherUtils.createLauncherException("Error occurred when creating the executable.");
//                }
//            }
//        } else if (Files.exists(
//                this.sourceRootPath.resolve(ProjectConstants.SOURCE_DIR_NAME).resolve(this.argList.get(0))) &&
//                Files.isDirectory(
//                        this.sourceRootPath.resolve(ProjectConstants.SOURCE_DIR_NAME)
//                                .resolve(this.argList.get(0)))) {
//
//            // when building a ballerina module
//            //// the output flag cannot be set for projects
//            if (null != this.output) {
//                CommandUtil.printError(this.errStream,
//                                       "'-o' and '--output' are only supported for building a single Ballerina " +
//                                               "file.",
//                                       null,
//                                       false);
//                CommandUtil.exitError(this.exitWhenFinish);
//                return;
//            }
//
//            //// check if command executed from project root.
//            if (!RepoUtils.isBallerinaProject(this.sourceRootPath)) {
//                CommandUtil.printError(this.errStream,
//                        "you are trying to build/compile a module that is not inside a project.",
//                        null,
//                        false);
//                CommandUtil.exitError(this.exitWhenFinish);
//                return;
//            }
//
//            //// check if module name given is not absolute.
//            if (Paths.get(argList.get(0)).isAbsolute()) {
//                CommandUtil.printError(this.errStream,
//                                       "you are trying to build/compile a module giving the absolute path. You " +
//                                               "only need to give the name of the module.",
//                                       "ballerina build [-c] <module-name>",
//                                       true);
//                CommandUtil.exitError(this.exitWhenFinish);
//                return;
//            }
//
//            String moduleName = argList.get(0);
//
//            //// remove end forward slash
//            if (moduleName.endsWith("/")) {
//                moduleName = moduleName.substring(0, moduleName.length() - 1);
//            }
//
//            sourcePath = Paths.get(moduleName);
//
//            //// check if module exists.
//            if (Files.notExists(this.sourceRootPath.resolve(ProjectConstants.SOURCE_DIR_NAME).resolve(sourcePath))) {
//                CommandUtil.printError(this.errStream,
//                        "'" + sourcePath + "' module does not exist.",
//                        "ballerina build [-c] <module-name>",
//                        true);
//                CommandUtil.exitError(this.exitWhenFinish);
//                return;
//            }
//
//            targetPath = this.sourceRootPath.resolve(ProjectConstants.TARGET_DIR_NAME);
//        } else {
//            CommandUtil.printError(this.errStream,
//                                   "invalid Ballerina source path. It should either be a name of a module in a " +
//                                   "Ballerina project or a file with a \'" + BLangConstants.BLANG_SRC_FILE_SUFFIX +
//                                   "\' extension. Use -a or --all " +
//                                   "to build or compile all modules.",
//                                   "ballerina build {<ballerina-file> | <module-name> | -a | --all}",
//                                   true);
//            CommandUtil.exitError(this.exitWhenFinish);
//            return;
//        }
//
//        // normalize paths
//        this.sourceRootPath = this.sourceRootPath.normalize();

        // create builder context
//        BuildContext buildContext = new BuildContext(this.sourceRootPath, targetPath, sourcePath, compilerContext);
//        JarResolver jarResolver = JarResolverImpl.getInstance(buildContext, skipCopyLibsFromDist,
//                observabilityIncluded);
//        buildContext.put(BuildContextField.JAR_RESOLVER, jarResolver);
//        buildContext.setOut(outStream);
//        buildContext.setErr(errStream);

//        boolean isSingleFileBuild = buildContext.getSourceType().equals(SINGLE_BAL_FILE);
        // output path is the current directory if -o flag is not given.


        String[] args;
        if (this.argList == null) {
            args = new String[0];
        } else {
            args = argList.subList(1, argList.size()).toArray(new String[0]);
        }

        String[] userArgs = LaunchUtils.getUserArgs(args, new HashMap<>());
        // check if there are too many arguments.
        if (userArgs.length > 0) {
            CommandUtil.printError(this.errStream, "too many arguments.", buildCmd, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (argList == null) {
            this.projectPath = Paths.get(System.getProperty("user.dir"));
        } else {
            this.projectPath = Paths.get(argList.get(0));
        }

        Project project = ProjectLoader.loadProject(this.projectPath);
        boolean isSingleFileBuild = project instanceof SingleFileProject;

        if (isSingleFileBuild) {
            if (this.compile) {
                CommandUtil.printError(this.errStream,
                                       "'-c' or '--compile' can only be used with modules.", null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        } else {
            // Check if the output flag is set when building all the modules.
            if (null != this.output) {
                CommandUtil.printError(this.errStream,
                                       "'-o' and '--output' are only supported when building a single Ballerina " +
                                               "file.",
                                       "ballerina build -o <output-file> <ballerina-file> ",
                                       true);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        Path outputPath = null == this.output ? Paths.get(System.getProperty("user.dir")) : Paths.get(this.output);

        ProjectEnvironmentContext environmentContext = project.environmentContext();
        CompilerContext compilerContext = environmentContext.getService(CompilerContext.class);
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(OFFLINE, Boolean.toString(this.offline));
        options.put(LOCK_ENABLED, Boolean.toString(!this.skipLock));
        options.put(SKIP_TESTS, Boolean.toString(this.skipTests));
        options.put(TEST_ENABLED, Boolean.toString(!this.skipTests));
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(this.experimentalFlag));
        options.put(PRESERVE_WHITESPACE, "true");

        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetDirTask(), isSingleFileBuild)   // clean the target directory(projects only)
                .addTask(new CreateTargetDirTask()) // create target directory
//                .addTask(new ResolveMavenDependenciesTask()) // resolve maven dependencies in Ballerina.toml
                .addTask(new CompileTask(outStream, errStream)) // compile the modules
                .addTask(new CreateBirTask())   // create the bir
//                .addTask(new CreateLockFileTask(), this.skipLock || isSingleFileBuild)  // create a lock file if
                                                            // the given skipLock flag does not exist(projects only)
                .addTask(new CreateBaloTask(outStream))   // create the BALOs for modules (projects only)
                .addTask(new CreateJarTask())   // create the jar
//                .addTask(new CopyResourcesTask()) // merged with CreateJarTask
//                .addTask(new CopyObservabilitySymbolsTask(), isSingleFileBuild)
//                .addTask(new RunTestsTask(testReport, coverage, args), this.skipTests || isSingleFileBuild) // run tests
                                                                                                // (projects only)
                .addTask(new CreateExecutableTask(outStream, outputPath), this.compile)  // create the executable.jar file
//                .addTask(new CopyExecutableTask(outputPath), !isSingleFileBuild)    // copy executable
//                .addTask(new PrintExecutablePathTask(), this.compile)   // print the location of the executable
//                .addTask(new RunCompilerPluginTask(), this.compile) // run compiler plugins
                .addTask(new CleanTargetDirTask(), !isSingleFileBuild)  // clean the target dir(single bals only)
                .build();

        taskExecutor.executeTasks(project);
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    @Override
    public String getName() {
        return BUILD_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Build a Ballerina module(s)/file and produce an executable JAR file(s). \n");
        out.append("\n");
        out.append("Build a Ballerina project or a specific module in a project. The \n");
        out.append("executable \".jar\" files will be created in the <PROJECT-ROOT>/target/bin directory. \n");
        out.append("\n");
        out.append("Build a single Ballerina file. This creates an executable .jar file in the \n");
        out.append("current directory. The name of the executable file will be \n");
        out.append("<ballerina-file-name>.jar. \n");
        out.append("\n");
        out.append("If the output file is specified with the -o flag, the output \n");
        out.append("will be written to the given output file name. The -o flag will only \n");
        out.append("work for single files. \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina build [-o <output-file>] [--offline] [--skip-tests] [--skip-lock] " +
                   "{<ballerina-file | module-name> | -a | --all} [--] [(--key=value)...]\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
