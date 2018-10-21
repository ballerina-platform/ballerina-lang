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
package org.ballerinalang.launcher;

import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.connector.impl.ServerConnectorRegistry;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.util.LaunchListener;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.BLangUsageException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.observability.ObservabilityConstants;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;
import org.wso2.ballerinalang.programfile.ProgramFileWriter;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.util.BLangConstants.BLANG_EXEC_FILE_SUFFIX;
import static org.ballerinalang.util.BLangConstants.BLANG_SRC_FILE_SUFFIX;
import static org.ballerinalang.util.BLangConstants.MAIN_FUNCTION_NAME;

/**
 * Contains utility methods for executing a Ballerina program.
 *
 * @since 0.8.0
 */
public class LauncherUtils {

    private static PrintStream outStream = System.out;

    public static void runProgram(Path sourceRootPath, Path sourcePath, Map<String, String> runtimeParams,
                                  String configFilePath, String[] args, boolean offline, boolean observeFlag) {
        runProgram(sourceRootPath, sourcePath, MAIN_FUNCTION_NAME, runtimeParams, configFilePath, args, offline,
                   observeFlag, false);
    }

    public static void runProgram(Path sourceRootPath, Path sourcePath, String functionName,
                                  Map<String, String> runtimeParams, String configFilePath, String[] args,
                                  boolean offline, boolean observeFlag, boolean printReturn) {
        ProgramFile programFile;
        String srcPathStr = sourcePath.toString();
        Path fullPath = sourceRootPath.resolve(sourcePath);
        // Set the source root path relative to the source path i.e. set the parent directory of the source path
        System.setProperty(ProjectDirConstants.BALLERINA_SOURCE_ROOT, fullPath.getParent().toString());
        loadConfigurations(fullPath.getParent(), runtimeParams, configFilePath, observeFlag);

        if (srcPathStr.endsWith(BLANG_EXEC_FILE_SUFFIX)) {
            programFile = BLangProgramLoader.read(sourcePath);
        } else if (Files.isRegularFile(fullPath) && srcPathStr.endsWith(BLANG_SRC_FILE_SUFFIX) &&
                !RepoUtils.hasProjectRepo(sourceRootPath)) {
            programFile = compile(fullPath.getParent(), fullPath.getFileName(), offline);
        } else if (Files.isDirectory(sourceRootPath)) {
            if (Files.isDirectory(fullPath) && !RepoUtils.hasProjectRepo(sourceRootPath)) {
                throw createLauncherException("did you mean to run the module ? If so, either run from the project " +
                                              "folder or use --sourceroot to specify the project path and run the " +
                                              "module");
            }
            if (Files.isRegularFile(fullPath) && !srcPathStr.endsWith(BLANG_SRC_FILE_SUFFIX)) {
                throw createLauncherException("only modules, " + BLANG_SRC_FILE_SUFFIX + " and " +
                                                      BLANG_EXEC_FILE_SUFFIX + " files can be used with the " +
                                                      "'ballerina run' command.");
            }
            // If we are trying to run a bal file inside a module from inside a project directory an error is thrown.
            // To differentiate between top level bals and bals inside modules we need to check if the parent of the
            // sourcePath given is null. If it is null then its a top level bal else its a bal inside a module
            if (Files.isRegularFile(fullPath) && srcPathStr.endsWith(BLANG_SRC_FILE_SUFFIX) &&
                    sourcePath.getParent() != null) {
                throw createLauncherException("you are trying to run a ballerina file inside a module within a " +
                                                      "project. Try running 'ballerina run <module-name>'");
            }
            programFile = compile(sourceRootPath, sourcePath, offline);
        } else {
            throw createLauncherException("only modules, " + BLANG_SRC_FILE_SUFFIX + " and " + BLANG_EXEC_FILE_SUFFIX
                                                  + " files can be used with the 'ballerina run' command.");
        }

        // If a function named main is expected to be the entry point but such a function does not exist and there is
        // no service entry point either, throw an error
        if ((MAIN_FUNCTION_NAME.equals(functionName) && !programFile.isMainEPAvailable())
                && !programFile.isServiceEPAvailable()) {
            throw createLauncherException("'" + programFile.getProgramFilePath()
                                                  + "' does not contain a main function or a service");
        }

        boolean runServicesOnly = MAIN_FUNCTION_NAME.equals(functionName) && !programFile.isMainEPAvailable();

        // Load launcher listeners
        ServiceLoader<LaunchListener> listeners = ServiceLoader.load(LaunchListener.class);
        listeners.forEach(listener -> listener.beforeRunProgram(runServicesOnly));

        if (runServicesOnly) {
            if (args.length > 0) {
                throw LauncherUtils.createUsageExceptionWithHelp("arguments not allowed for services");
            }
            runServices(programFile);
        } else {
            runMain(programFile, functionName, args, printReturn);
        }
        BLangProgramRunner.resumeStates(programFile);
        listeners.forEach(listener -> listener.afterRunProgram(runServicesOnly));
    }

    public static void runMain(ProgramFile programFile, String functionName, String[] args, boolean printReturn) {
        try {
            BValue[] entryFuncResult = BLangProgramRunner.runEntryFunc(programFile, functionName, args);
            if (printReturn && entryFuncResult != null && entryFuncResult.length >= 1 && entryFuncResult[0] != null) {
                outStream.print(entryFuncResult[0].stringValue());
            }
        } catch (BLangUsageException | BallerinaException e) {
            throw createUsageException(makeFirstLetterLowerCase(e.getLocalizedMessage()));
        }

        if (programFile.isServiceEPAvailable()) {
            return;
        }
        try {
            ThreadPoolFactory.getInstance().getWorkerExecutor().shutdown();
            ThreadPoolFactory.getInstance().getWorkerExecutor().awaitTermination(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            // Ignore the error
        }
        Runtime.getRuntime().exit(0);
    }

    public static void runServices(ProgramFile programFile) {
        PrintStream outStream = System.out;

        ServerConnectorRegistry serverConnectorRegistry = new ServerConnectorRegistry();
        programFile.setServerConnectorRegistry(serverConnectorRegistry);
        serverConnectorRegistry.initServerConnectors();

        outStream.println("Initiating service(s) in '" + programFile.getProgramFilePath() + "'");
        BLangProgramRunner.runService(programFile);

        serverConnectorRegistry.deploymentComplete();
    }

    public static Path getSourceRootPath(String sourceRoot) {
        // Get source root path.
        Path sourceRootPath;
        if (sourceRoot == null || sourceRoot.isEmpty()) {
            sourceRootPath = Paths.get(System.getProperty("user.dir"));
        } else {
            try {
                sourceRootPath = Paths.get(sourceRoot).toRealPath(LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                throw new RuntimeException("error reading from directory: " + sourceRoot + " reason: " +
                        e.getMessage(), e);
            }

            if (!Files.isDirectory(sourceRootPath, LinkOption.NOFOLLOW_LINKS)) {
                throw new RuntimeException("source root must be a directory");
            }
        }
        return sourceRootPath;
    }

    private static BLauncherException createUsageException(String errorMsg) {
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage("ballerina: " + errorMsg);
        return launcherException;
    }

    public static BLauncherException createUsageExceptionWithHelp(String errorMsg) {
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage("ballerina: " + errorMsg);
        launcherException.addMessage("Run 'ballerina help' for usage.");
        return launcherException;
    }

    public static BLauncherException createLauncherException(String errorMsg) {
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage("error: " + errorMsg);
        return launcherException;
    }

    static void printLauncherException(BLauncherException e, PrintStream outStream) {
        List<String> errorMessages = e.getMessages();
        errorMessages.forEach(outStream::println);
    }

    static String makeFirstLetterLowerCase(String s) {
        if (s == null) {
            return null;
        }
        char c[] = s.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    /**
     * Write the process ID of this process to the file.
     *
     * @param ballerinaHome ballerina.home sys property value.
     */
    static void writePID(String ballerinaHome) {

        String[] cmd = {"bash", "-c", "echo $PPID"};
        Process p;
        String pid = "";
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            //Ignored. We might be invoking this on a Window platform. Therefore if an error occurs
            //we simply ignore the error.
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(),
                StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            pid = builder.toString();
        } catch (Throwable e) {
            throw createLauncherException("failed to write ballerina.pid file: "
                                                  + makeFirstLetterLowerCase(e.getMessage()));
        }

        if (pid.length() != 0) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(Paths.get(ballerinaHome, "ballerina.pid").toString()),
                    StandardCharsets.UTF_8))) {
                writer.write(pid);
            } catch (IOException e) {
                throw createLauncherException("failed to write ballerina.pid file: "
                                                      + makeFirstLetterLowerCase(e.getMessage()));
            }
        }
    }

    /**
     * Compile and get the executable program file.
     * 
     * @param sourceRootPath Path to the source root
     * @param sourcePath Path to the source from the source root
     * @param offline Should the build call remote repos
     * @return Executable program
     */
    public static ProgramFile compile(Path sourceRootPath, Path sourcePath, boolean offline) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        options.put(OFFLINE, Boolean.toString(offline));

        // compile
        Compiler compiler = Compiler.getInstance(context);
        BLangPackage entryPkgNode = compiler.compile(sourcePath.toString());
        CompiledBinaryFile.ProgramFile programFile = compiler.getExecutableProgram(entryPkgNode);
        if (programFile == null) {
            throw new BLangCompilerException("compilation contains errors");
        }

        ProgramFile progFile = getExecutableProgram(programFile);
        progFile.setProgramFilePath(sourcePath);
        return progFile;
    }

    /**
     * Get the executable program ({@link ProgramFile}) given the compiled program 
     * ({@link org.wso2.ballerinalang.programfile.CompiledBinaryFile.ProgramFile}).
     * 
     * @param programFile Compiled program
     * @return Executable program
     */
    public static ProgramFile getExecutableProgram(CompiledBinaryFile.ProgramFile programFile) {
        ByteArrayInputStream byteIS = null;
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        try {
            ProgramFileWriter.writeProgram(programFile, byteOutStream);

            ProgramFileReader reader = new ProgramFileReader();
            byteIS = new ByteArrayInputStream(byteOutStream.toByteArray());
            return reader.readProgram(byteIS);
        } catch (Throwable e) {
            throw createLauncherException("failed to compile file: " + makeFirstLetterLowerCase(e.getMessage()));
        } finally {
            if (byteIS != null) {
                try {
                    byteIS.close();
                } catch (IOException ignore) {
                }
            }

            try {
                byteOutStream.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Initializes the {@link ConfigRegistry} and loads {@link LogManager} configs.
     *
     * @param sourceRootPath source directory
     * @param runtimeParams  run time parameters
     * @param configFilePath config file path
     * @param observeFlag    to indicate whether observability is enabled
     */
    public static void loadConfigurations(Path sourceRootPath, Map<String, String> runtimeParams,
                                           String configFilePath, boolean observeFlag) {
        Path ballerinaConfPath = sourceRootPath.resolve("ballerina.conf");
        try {
            ConfigRegistry.getInstance().initRegistry(runtimeParams, configFilePath, ballerinaConfPath);
            ((BLogManager) LogManager.getLogManager()).loadUserProvidedLogConfiguration();

            if (observeFlag) {
                ConfigRegistry.getInstance()
                        .addConfiguration(ObservabilityConstants.CONFIG_METRICS_ENABLED, Boolean.TRUE);
                ConfigRegistry.getInstance()
                        .addConfiguration(ObservabilityConstants.CONFIG_TRACING_ENABLED, Boolean.TRUE);
            }

        } catch (IOException e) {
            throw new BLangRuntimeException(
                    "failed to read the specified configuration file: " + ballerinaConfPath.toString(), e);
        } catch (RuntimeException e) {
            throw new BLangRuntimeException(e.getMessage(), e);
        }
    }
}
