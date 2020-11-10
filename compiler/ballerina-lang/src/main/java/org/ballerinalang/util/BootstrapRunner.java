/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.util;

import org.ballerinalang.compiler.BLangCompilerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Creates jars in file system using bootstrap pack and create class loader hierarchy for them.
 */
public class BootstrapRunner {

    private static final PrintStream out = System.out;
    private static final PrintStream err = System.err;
    private static final String CLASSPATH = "CLASSPATH";
    private static final String TMP_OBJECT_FILE_NAME = "ballerina_native_objf.o";
    private static final String COMPILER_BACKEND_JVM = "ballerina.compiler_backend_jvm.$_init";
    private static final String COMPILER_BACKEND_LLVM = "ballerina.compiler_backend_llvm.$_init";
    private static String javaCommand = System.getProperty("java.command");

    public static void loadTargetAndGenerateJarBinary(String entryBir, String jarOutputPath, boolean dumpBir,
                                                      HashSet<Path> moduleDependencySet, String... birCachePaths) {
        //Load all Jars from module dependency set.
        List<String> jarFilePaths = new ArrayList<>(moduleDependencySet.size());
        for (Path path : moduleDependencySet) {
            jarFilePaths.add(path.toString());
        }
        List<String> commands =
                createArgsForJBalCompilerBackend(entryBir, jarOutputPath, dumpBir, jarFilePaths, birCachePaths);
        generateJarBinaryInProc(commands);
    }

    public static void genNativeCode(String entryBir, Path targetDir, boolean dumpLLVM, boolean noOptimizeLLVM) {
        Path nativeFolder = genNativeForlderInTarget(targetDir);
        Path objectFilePath = nativeFolder.resolve(TMP_OBJECT_FILE_NAME);
        genObjectFile(entryBir, objectFilePath.toString(), dumpLLVM, noOptimizeLLVM);
        genExecutable(objectFilePath, "llvm");

        Runtime.getRuntime().exit(0);
    }

    public static Path genNativeForlderInTarget(Path targetDir) {
        Path nativeFolder = targetDir.resolve("native");
        buildDirectoryFromPath(nativeFolder);
        return nativeFolder;
    }

    public static void buildDirectoryFromPath(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new BLangCompilerException("could not create native folder inside target folder", e);
        }
    }

    private static void genObjectFile(String entryBir, String objFileOutputPath, boolean dumpLLVM,
            boolean noOptimizeLLVM) {
        List<String> commands = createArgsForNBalCompilerBackend(entryBir, objFileOutputPath, dumpLLVM, noOptimizeLLVM);
        generateJarBinaryInProc(commands);
    }

    private static void genExecutable(Path objectFilePath, String execFilename) {
        // Check whether gcc is installed
        checkGCCAvailability();

        // Create the os-specific gcc command
        ProcessBuilder gccProcessBuilder = createOSSpecificGCCCommand(objectFilePath, execFilename);

        try {
            // Execute gcc
            Process gccProcess = gccProcessBuilder.start();
            int exitCode = gccProcess.waitFor();
            if (exitCode != 0) {
                throw new BLangCompilerException("linker failed: " + getProcessErrorOutput(gccProcess));
            }
        } catch (IOException | InterruptedException e) {
            throw new BLangCompilerException("linker failed: " + e.getMessage(), e);
        }
    }

    private static void checkGCCAvailability() {
        Runtime rt = Runtime.getRuntime();
        try {
            Process gccCheckProc = rt.exec("gcc -v");
            int exitVal = gccCheckProc.waitFor();
            if (exitVal != 0) {
                throw new BLangCompilerException("'gcc' is not installed in your environment");
            }
        } catch (IOException | InterruptedException e) {
            throw new BLangCompilerException("probably, 'gcc' is not installed in your environment: " +
                    e.getMessage(), e);
        }
    }

    private static ProcessBuilder createOSSpecificGCCCommand(Path objectFilePath, String execFilename) {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        ProcessBuilder gccProcessBuilder = new ProcessBuilder();
        if (osName.startsWith("windows")) {
            // TODO Window environment
            gccProcessBuilder.command("cmd.exe", "/c", "dir");
        } else if (osName.startsWith("mac os x")) {
            // Mac OS X environment
            gccProcessBuilder.command("gcc", objectFilePath.toString(), "-o", execFilename);
        } else {
            // TODO Is this assumption correct?
            // Linux environment
            gccProcessBuilder.command("gcc", objectFilePath.toString(), "-static", "-o", execFilename);
        }
        return gccProcessBuilder;
    }

    private static String getProcessErrorOutput(Process process) throws IOException {
        //TODO: check win https://stackoverflow.com/questions/8398277/which-encoding-does-process-getinputstream-use
        InputStreamReader in = new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8);
        try (BufferedReader errReader = new BufferedReader(in)) {
            return errReader.lines().collect(Collectors.joining(", "));
        }
    }

    public static void generateJarBinaryInProc(List<String> commands) {
        try {
            // pass the classpath for the sub-process
            ProcessBuilder pb = new ProcessBuilder(commands);
            Map<String, String> env = pb.environment();
            env.put(CLASSPATH, System.getProperty("java.class.path"));

            Process process = pb.start();
            getConsoleOutput(process.getInputStream(), out);
            String consoleError = getConsoleOutput(process.getErrorStream(), err);
            boolean processEnded = process.waitFor(120, TimeUnit.SECONDS);
            if (!processEnded) {
                throw new BLangCompilerException("failed to generate jar file within 120s.");
            }
            if (process.exitValue() != 0) {
                throw new BLangCompilerException(consoleError);
            }
        } catch (InterruptedException | IOException e) {
            // these are un-handled errors at the jvm-codegen phase. Hence should not throw 
            // compiler-exceptions. These should cause a bad-sad error.
            throw new RuntimeException("failed running jvm code gen phase.", e);
        }
    }

    private static void setSystemProperty(List<String> commands, String propertName) {
        String value = System.getProperty(propertName);
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        commands.add("-D" + propertName + "=" + System.getProperty(propertName));
    }

    private static String getConsoleOutput(InputStream inputStream, PrintStream printStream) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));
        reader.lines().iterator().forEachRemaining(line -> {
            printStream.println(line);
            sj.add(line);
        });
        return sj.toString();
    }

    public static List<String> createArgsForJBalCompilerBackend(String entryBir, String jarOutputPath, boolean dumpBir,
            List<String> jarFilePaths, String... birCachePaths) {
        List<String> commands = new ArrayList<>();
        // Below code only runs for test cases, for all other scenarios, this value should be there when we start
        if (javaCommand == null) {
            javaCommand = System.getProperty("java.command");
        }
        commands.add(javaCommand);
        commands.add(COMPILER_BACKEND_JVM);
        commands.addAll(createArgsForCompilerBackend(entryBir, jarOutputPath, dumpBir, true,
                birCachePaths, jarFilePaths));
        return commands;
    }

    public static List<String> createArgsForCompilerBackend(String entryBir, String jarOutputPath, boolean dumpBir,
            boolean useSystemClassLoader, String[] birCachePaths, List<String> jarFilePaths) {
        List<String> commands = new ArrayList<>();
        commands.add(entryBir);
        commands.add(getMapPath());
        commands.add(jarOutputPath);
        commands.add(dumpBir ? "true" : "false"); // dump bir
        commands.add(useSystemClassLoader ? "true" : "false"); // useSystemClassLoader
        commands.add(String.valueOf(birCachePaths.length));
        commands.addAll(Arrays.asList(birCachePaths));
        commands.addAll(jarFilePaths);
        return commands;
    }

    private static List<String> createArgsForNBalCompilerBackend(String entryBir, String objFileOutputPath,
            boolean dumpLLVM, boolean noOptimizeLLVM) {
        List<String> commands = new ArrayList<>();
        // Below code only runs for test cases, for all other scenarios, this value should be there when we start
        if (javaCommand == null) {
            javaCommand = System.getProperty("java.command");
        }
        commands.add(javaCommand);
        commands.add(COMPILER_BACKEND_LLVM);
        commands.add(entryBir);
        commands.add(objFileOutputPath);
        commands.add(dumpLLVM ? "true" : "false"); // dump LLVM-IR
        commands.add(noOptimizeLLVM ? "true" : "false"); // Don't optimize LLVM-IR
        return commands;
    }


    private static String getMapPath() {
        String ballerinaNativeMap = System.getenv("BALLERINA_NATIVE_MAP");
        return ballerinaNativeMap == null ? " " : ballerinaNativeMap;
    }
}

