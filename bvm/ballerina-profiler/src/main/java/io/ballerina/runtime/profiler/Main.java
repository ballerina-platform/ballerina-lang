/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.profiler;

import io.ballerina.runtime.profiler.codegen.CustomClassLoader;
import io.ballerina.runtime.profiler.codegen.MethodWrapper;
import io.ballerina.runtime.profiler.util.Constants;
import io.ballerina.runtime.profiler.util.ProfilerException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.ballerina.runtime.profiler.ui.HTTPServer.initializeHTMLExport;
import static io.ballerina.runtime.profiler.ui.JSONParser.initializeCPUParser;
import static io.ballerina.runtime.profiler.util.Constants.OUT;

/**
 * This class is used to as the driver class of the ballerina profiler.
 *
 * @since 2201.7.0
 */
public class Main {

    static long profilerStartTime;
    static int exitCode = 0;
    private static String balJarArgs = null;
    static String balJarName = null;
    static String targetDir = null;
    static String skipFunctionString = null;
    private static int balFunctionCount = 0;
    static int moduleCount = 0;

    static final List<String> INSTRUMENTED_PATHS = new ArrayList<>();
    static final List<String> INSTRUMENTED_FILES = new ArrayList<>();
    static final List<String> UTIL_INIT_PATHS = new ArrayList<>();
    static final List<String> UTIL_PATHS = new ArrayList<>();

    public static void main(String[] args) throws ProfilerException {
        profilerStartTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
        addShutdownHookAndCleanup(); // Register a shutdown hook to handle graceful shutdown of the application
        printHeader(); // Print the program header
        handleProfilerArguments(args); // Handle command line arguments
        extractProfiler(); // Extract the profiler used by the program
        createTempJar(balJarName); // Create a temporary JAR file inside target/bin
        initializeProfiling(balJarName); // Initialize profiling
    }

    private static void printHeader() {
        OUT.printf("%n%s================================================================================%s",
                Constants.ANSI_GRAY, Constants.ANSI_RESET);
        OUT.printf("%n%sBallerina Profiler%s", Constants.ANSI_CYAN, Constants.ANSI_RESET);
        OUT.printf("%s: Profiling...%s", Constants.ANSI_GRAY, Constants.ANSI_RESET);
        OUT.printf("%n%s================================================================================%s",
                Constants.ANSI_GRAY, Constants.ANSI_RESET);
        OUT.printf("%n%sWARNING : Ballerina Profiler is an experimental feature.%s%n", Constants.ANSI_GRAY,
                Constants.ANSI_RESET);
    }

    private static void handleProfilerArguments(String[] args) {
        if (args.length == 0) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--file":
                    balJarName = args[i + 1];
                    if (!balJarName.endsWith(".jar")) {
                        throw new ProfilerException("Invalid JAR file found : " + balJarName);
                    }
                    break;
                case "--args":
                    balJarArgs = args[i + 1];
                    if (balJarArgs == null || !balJarArgs.startsWith("[") || !balJarArgs.endsWith("]")) {
                        throw new ProfilerException("Invalid JAR arguments found : " + balJarArgs);
                    }
                    balJarArgs = balJarArgs.substring(1, balJarArgs.length() - 1);
                    break;
//                TODO: Implement skip function
//                case "--skip":
//                    skipFunctionString = args[i + 1];
//                    if (skipFunctionString != null && skipFunctionString.matches("\\[.*\\]")) {
//                        skipFunctionString = skipFunctionString.substring(1, skipFunctionString.length() - 1);
//                    } else {
//                        OUT.printf("%s%n", invalidArgument);
//                    }
//                    break;
                case "--target":
                    targetDir = args[i + 1];
                    break;
                default:
                    // Handle unrecognized arguments here
                    break;
            }
        }

    }

    private static void extractProfiler() throws ProfilerException {
        OUT.printf("%s[1/6] Initializing Profiler...%s%n", Constants.ANSI_CYAN, Constants.ANSI_RESET);
        try {
            new ProcessBuilder("jar", "xvf", "Profiler.jar", "io/ballerina/runtime/profiler/runtime").start().waitFor();
        } catch (IOException | InterruptedException exception) {
            throw new ProfilerException(exception);
        }
    }

    public static void createTempJar(String balJarName) {
        try {
            OUT.printf("%s[2/6] Copying Executable...%s%n", Constants.ANSI_CYAN, Constants.ANSI_RESET);
            Path sourcePath = Paths.get(balJarName);
            Path destinationPath = Paths.get(Constants.TEMP_JAR_FILE_NAME);
            Files.copy(sourcePath, destinationPath);
        } catch (IOException e) {
            exitCode = 2;
            OUT.printf("Error occurred while copying the file: %s%n", e.getMessage());
        }
    }

    private static void initializeProfiling(String balJarName) throws ProfilerException {
        OUT.printf("%s[3/6] Performing Analysis...%s%n", Constants.ANSI_CYAN, Constants.ANSI_RESET);
        ArrayList<String> classNames = new ArrayList<>();
        try {
            findAllClassNames(balJarName, classNames);
            findUtilityClasses(classNames);
        } catch (Exception e) {
            OUT.printf("(No such file or directory)%n");
        }
        OUT.printf("%s[4/6] Instrumenting Functions...%s%n", Constants.ANSI_CYAN, Constants.ANSI_RESET);
        try (JarFile jarFile = new JarFile(balJarName)) {
            String mainClassPackage = MethodWrapper.mainClassFinder(new URLClassLoader(new URL[]{new File(balJarName)
                    .toURI().toURL()}));
            CustomClassLoader customClassLoader = new CustomClassLoader(new URLClassLoader(
                    new URL[]{new File(balJarName).toURI().toURL()}));
            Set<String> usedPaths = new HashSet<>();
            for (String className : classNames) {
                if (mainClassPackage == null) {
                    continue;
                }
                if (className.startsWith(mainClassPackage.split("/")[0]) || UTIL_PATHS.contains(className)) {
                    try (InputStream inputStream = jarFile.getInputStream(jarFile.getJarEntry(className))) {
                        byte[] code = MethodWrapper.modifyMethods(inputStream);
                        customClassLoader.loadClass(code);
                        usedPaths.add(className.replace(Constants.CLASS_SUFFIX, "").replace("/", "."));
                        MethodWrapper.printCode(className, code);
                    }
                }
                if (className.endsWith("/$_init.class")) {
                    moduleCount++;
                }
            }
            OUT.printf(" ○ Instrumented Module Count: %d%n", moduleCount);
            try (PrintWriter printWriter = new PrintWriter("usedPathsList.txt", StandardCharsets.UTF_8)) {
                printWriter.println(String.join(", ", usedPaths));
            }
            OUT.printf(" ○ Instrumented Function Count: %d%n", balFunctionCount);
            modifyJar();
        } catch (Throwable throwable) {
            throw new ProfilerException(throwable);
        }
    }

    private static void modifyJar() throws InterruptedException, IOException {
        try {
            final File userDirectory = new File(System.getProperty("user.dir")); // Get the user directory
            listAllFiles(userDirectory); // List all files in the user directory and its subdirectories
            // Get a list of the directories containing instrumented files
            List<String> changedDirectories = INSTRUMENTED_FILES.stream().distinct().collect(Collectors.toList());
            loadDirectories(changedDirectories);
        } finally {
            for (String instrumentedFilePath : INSTRUMENTED_PATHS) {
                FileUtils.deleteDirectory(new File(instrumentedFilePath));
            }
            FileUtils.deleteDirectory(new File("io/ballerina/runtime/profiler/runtime"));
            MethodWrapper.invokeMethods();
        }
    }

    private static void loadDirectories(List<String> changedDirs) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("jar", "uf", Constants.TEMP_JAR_FILE_NAME);
            processBuilder.command().addAll(changedDirs);
            processBuilder.start().waitFor();
        } catch (IOException | InterruptedException e) {
            throw new ProfilerException("Error occurred while loading the jar file", e);
        }
    }

    public static void listAllFiles(final File userDirectory) {
        String absolutePath = Paths.get(Constants.TEMP_JAR_FILE_NAME).toFile().getAbsolutePath()
                .replaceAll(Constants.TEMP_JAR_FILE_NAME, "");

        File[] files = userDirectory.listFiles();
        if (files != null) {
            for (final File fileEntry : files) {
                if (fileEntry.isDirectory()) {
                    listAllFiles(fileEntry);
                } else {
                    String fileEntryString = String.valueOf(fileEntry);
                    if (fileEntryString.endsWith(Constants.CLASS_SUFFIX)) {
                        fileEntryString = fileEntryString.replaceAll(absolutePath, "");
                        int index = fileEntryString.lastIndexOf('/');
                        fileEntryString = fileEntryString.substring(0, index);
                        String[] fileEntryParts = fileEntryString.split("/");
                        INSTRUMENTED_PATHS.add(fileEntryParts[0]);
                        INSTRUMENTED_FILES.add(fileEntryString);
                    }
                }
            }
        }
    }

    private static void findAllClassNames(String jarPath, ArrayList<String> classNames) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(jarPath))) {
            for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                if (!entry.isDirectory() && entry.getName().endsWith(Constants.CLASS_SUFFIX)) {
                    classNames.add(String.valueOf(entry));
                }
            }
        }
    }

    private static void findUtilityClasses(ArrayList<String> classNames) {
        populateInitPaths(classNames);

        for (String name : classNames) {
            for (String path : UTIL_INIT_PATHS) {
                if (name.startsWith(path)) {
                    String subPath = name.substring(path.length());
                    if (subPath.indexOf('/') == -1) {
                        UTIL_PATHS.add(name);
                    }
                }
            }
        }
    }

    private static void populateInitPaths(ArrayList<String> classNames) {
        for (String className : classNames) {
            if (className.endsWith("$_init.class")) {
                String path = className.substring(0, className.lastIndexOf('/') + 1);
                if (!UTIL_INIT_PATHS.contains(path)) {
                    UTIL_INIT_PATHS.add(path);
                }
            }
        }
    }

    private static void deleteTempData() {
        String filePrefix = "jartmp";
        File[] files = new File(System.getProperty("user.dir")).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith(filePrefix)) {
                    FileUtils.deleteQuietly(file);
                }
            }
        }
    }

    private static void addShutdownHookAndCleanup() {
        // Add a shutdown hook to stop the profiler and parse the output when the program is closed.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                long profilerTotalTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS)
                        - profilerStartTime;
                deleteFileIfExists(Constants.TEMP_JAR_FILE_NAME);
                OUT.printf("%s[6/6] Generating Output...%s%n", Constants.ANSI_CYAN, Constants.ANSI_RESET);
                Thread.sleep(100);
                initializeCPUParser(skipFunctionString);
                deleteFileIfExists("usedPathsList.txt");
                deleteFileIfExists("CpuPre.json");
                OUT.printf(" ○ Execution Time: %d Seconds ", profilerTotalTime / 1000);
                deleteTempData();
                initializeHTMLExport();
                deleteFileIfExists("performance_report.json");
                OUT.println("--------------------------------------------------------------------------------");
            } catch (IOException | InterruptedException e) {
                throw new ProfilerException("Error occurred while generating the output", e);
            } finally {
                deleteFileIfExists(targetDir);
            }
        }));
    }

    private static void deleteFileIfExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        try {
            Files.delete(Paths.get(filePath));
        } catch (IOException e) {
            throw new ProfilerException("Failed to delete file: " + filePath + "%n", e);
        }
    }

    public static void incrementBalFunctionCount() {
        balFunctionCount++;
    }

    public static String getBalJarArgs() {
        return balJarArgs;
    }
}
