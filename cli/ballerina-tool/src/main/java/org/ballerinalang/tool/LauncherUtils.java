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
package org.ballerinalang.tool;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.tool.util.BFileUtil;
import org.ballerinalang.util.BootstrapRunner;
import org.ballerinalang.util.JBallerinaInMemoryClassLoader;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;
import org.wso2.ballerinalang.programfile.ProgramFileWriter;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.LogManager;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_TYPE;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.ballerinalang.util.BLangConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.JAVA_MAIN;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.MAIN_CLASS_MANIFEST_ENTRY;

/**
 * Contains utility methods for executing a Ballerina program.
 *
 * @since 0.8.0
 */
public class LauncherUtils {

    private static final String STATUS_CODE = "statusCode";
    private static final int EXIT_CODE_GENERAL_ERROR = 1;
    private static final int EXIT_CODE_SUCCESS = 0;

    private static PrintStream errStream = System.err;

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

    public static BLauncherException createLauncherException(String errorPrefix, Throwable cause) {
        String message;
        if (cause instanceof ErrorValue) {
            message = ((ErrorValue) cause).getPrintableStackTrace();
        } else {
            message = cause.toString();
        }
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage("error: " + errorPrefix + message);
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
     * @param enableExpFeatures Flag indicating to enable the experimental feature
     * @return Executable program
     */
    public static ProgramFile compile(Path sourceRootPath, Path sourcePath, boolean offline,
                                      boolean enableExpFeatures) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        options.put(OFFLINE, Boolean.toString(offline));
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(enableExpFeatures));

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
     * Compile and get the executable program file.
     *
     * @param sourceRootPath Path to the source root
     * @param sourcePath Path to the source from the source root
     * @param offline Should the build call remote repos
     * @param siddhiRuntimeFlag Flag to enable siddhi runtime based stream processing
     * @param enableExpFeatures Flag indicating to enable the experimental feature
     * @return Executable program
     */
    public static ProgramFile compile(Path sourceRootPath, Path sourcePath, boolean offline,
                                      boolean siddhiRuntimeFlag, boolean enableExpFeatures) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        options.put(OFFLINE, Boolean.toString(offline));
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(enableExpFeatures));

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
     * @param configFilePath config file path
     */
    public static void loadConfigurations(Path sourceRootPath, String configFilePath) {
        loadConfigurations(sourceRootPath, new HashMap<>(), configFilePath, false);
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
            throw createLauncherException("failed to read the specified configuration file: " +
                    ballerinaConfPath.toString());
        } catch (RuntimeException e) {
            throw createLauncherException(e.getMessage());
        }
    }

    private static void compileAndRunJar(Path sourceRootPath, boolean offline, boolean lockEnabled, boolean skiptests,
                                         boolean enableExperimentalFeatures, boolean siddhiRuntimeEnabled,
                                         String[] args, Path sourcePath, Path fullPath, String srcPathStr) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(OFFLINE, Boolean.toString(offline));
        options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());
        options.put(LOCK_ENABLED, Boolean.toString(lockEnabled));
        options.put(SKIP_TESTS, Boolean.toString(skiptests));
        options.put(TEST_ENABLED, "true");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(enableExperimentalFeatures));

        String source = validateAndGetSrcPath(sourceRootPath, sourcePath, fullPath, srcPathStr);

        if (RepoUtils.isBallerinaStandaloneFile(fullPath)) {
            options.put(PROJECT_DIR, fullPath.getParent().toString());
            options.put(SOURCE_TYPE, "SINGLE_BAL_FILE");
        } else {
            options.put(PROJECT_DIR, sourceRootPath.toString());
        }

        Compiler compiler = Compiler.getInstance(context);
        BLangPackage entryPkgNode = compiler.compile(source);
        if (entryPkgNode.diagCollector.hasErrors()) {
            throw new BLangCompilerException("compilation contains errors");
        }

        String balHome = Objects.requireNonNull(System.getProperty("ballerina.home"),
                "ballerina.home is not set");
        JBallerinaInMemoryClassLoader classLoader;
        try {
            Path targetDirectory = Files.createTempDirectory("ballerina-compile").toAbsolutePath();
            classLoader = BootstrapRunner.createClassLoaders(entryPkgNode,
                    Paths.get(balHome).resolve("bir-cache"),
                    targetDirectory, Optional.empty(), false, false);
        } catch (IOException e) {
            throw new BLangCompilerException("error invoking jballerina backend", e);
        }

        String initClassName = BFileUtil.getQualifiedClassName(entryPkgNode.packageID.orgName.value,
                entryPkgNode.packageID.name.value,
                MODULE_INIT_CLASS_NAME);
        runInMemoryJar(classLoader, args, initClassName);
        return;
    }

    private static void runInMemoryJar(JBallerinaInMemoryClassLoader classLoader, String[] args, String initClassName) {

        try {
            Class<?> initClazz = classLoader.loadClass(initClassName);
            Method mainMethod = initClazz.getDeclaredMethod(JAVA_MAIN, String[].class);
            mainMethod.invoke(null, (Object) args);
            if (!initClazz.getField("serviceEPAvailable").getBoolean(initClazz)) {
                Runtime.getRuntime().exit(0);
            }
        } catch (NoSuchMethodException e) {
            throw createLauncherException("main method cannot be found for init class " + initClassName);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw createLauncherException("invoking main method failed due to " + e.getMessage());
        } catch (InvocationTargetException | NoSuchFieldException e) {
            throw createLauncherException("invoking main method failed due to " + e.getCause());
        }

    }

    private static String validateAndGetSrcPath(Path sourceRootPath, Path sourcePath, Path fullPath,
                                                String srcPathStr) {
        if (RepoUtils.isBallerinaStandaloneFile(fullPath)) {
            // running a bal file, no other packages
            return fullPath.getFileName().toString();
        } else if (!ProjectDirs.isProject(sourceRootPath)) {
            throw createLauncherException("you are trying to run a module that is not inside " +
                    "a project. Run `ballerina init` from " + sourceRootPath + " to initialize it as a " +
                    "project and then run the module.");
        } else if (ProjectDirs.isModuleExist(sourceRootPath, srcPathStr)) {
            return sourcePath.toString();
        } else {
            throw createLauncherException("Module not found :" +
                    sourcePath);
        }
    }

    private static String getModuleInitClassName(Path sourcePath) {
        try (JarInputStream jarStream = new JarInputStream(new FileInputStream((sourcePath.toString())))) {
            Manifest mf = jarStream.getManifest();
            Attributes attributes = mf.getMainAttributes();
            String initClassName = attributes.getValue(MAIN_CLASS_MANIFEST_ENTRY);
            if (initClassName == null) {
                throw createLauncherException("Main-class manifest entry cannot be found in the jar.");
            }
            return initClassName.replaceAll("/", ".");
        } catch (IOException e) {
            throw createLauncherException("error while getting init class name from manifest due to " + e.getMessage());
        }
    }
}
