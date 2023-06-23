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
import io.ballerina.runtime.profiler.util.CustomException;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.ballerina.runtime.profiler.ui.HTTPServer.initializeHTMLExport;
import static io.ballerina.runtime.profiler.ui.JSONParser.initializeCPUParser;

/**
 * Used to profile Ballerina programs.
 *
 * @since 2201.7.0
 */
public class Main {
    // Define ANSI escape codes for colored console output
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GRAY = "\033[37m";
    public static final String ANSI_CYAN = "\033[1;38;2;32;182;176m";

    // Define public static variables for the program
    public static long profilerStartTime;
    public static int exitCode = 0;
    public static String TEMP_JAR_FILE_NAME = "temp.jar";
    public static String balJarArgs = null;
    public static String balJarName = null;
    public static String skipFunctionString = null;
    public static int balFunctionCount = 0;
    public static int moduleCount = 0;

    public static List<String> instrumentedPaths = new ArrayList<>();
    public static List<String> instrumentedFiles = new ArrayList<>();
    public static List<String> utilInitPaths = new ArrayList<>();
    public static List<String> utilPaths = new ArrayList<>();

    public static void main(String[] args) throws CustomException {
        profilerStartTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
        tempFileCleanupShutdownHook(); // Register a shutdown hook to handle graceful shutdown of the application
        printHeader(); // Print the program header
        handleProfilerArguments(args); // Handle command line arguments
        extractTheProfiler(); // Extract the profiler used by the program
        createTempJar(balJarName); // Create a temporary JAR file inside target/bin
        initialize(balJarName); // Initialize profiling
    }

    private static void printHeader() {
        String header = "\n" +
                ANSI_GRAY + "================================================================================" + ANSI_RESET + "\n" +
                ANSI_CYAN + "Ballerina Profiler" + ANSI_RESET + ": Profiling..." + "\n" +
                ANSI_GRAY + "================================================================================" + ANSI_RESET + "\n" +
                "WARNING : Ballerina Profiler is an experimental feature.";
        System.out.println(header);
    }

    private static void handleProfilerArguments(String[] args) {
        String invalidArgument = "Invalid CLI Argument";
        if ((args.length != 0)) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--file":
                        balJarName = args[i + 1];
                        if ((balJarName.startsWith("[") && balJarName.endsWith("]"))) {
                            balJarName = balJarName.substring(1, balJarName.length() - 1);
                        }else {
                            System.out.println(invalidArgument);
                            System.exit(0);
                        }
                        break;
                    case "--args":
                        balJarArgs = args[i + 1];
                        if (balJarArgs != null && balJarArgs.startsWith("[") && balJarArgs.endsWith("]")) {
                            balJarArgs = balJarArgs.substring(1, balJarArgs.length() - 1);
                        }else {
                            System.out.println(invalidArgument);
                            System.exit(0);
                        }
                        break;
                    case "--skip":
                        skipFunctionString = args[i + 1];
                        if (skipFunctionString != null && skipFunctionString.startsWith("[") && skipFunctionString.endsWith("]")) {
                            skipFunctionString = skipFunctionString.substring(1, skipFunctionString.length() - 1);
                        }else {
                            System.out.println(invalidArgument);
                            System.exit(0);
                        }
                        break;
                }
            }
        }
    }

    private static void extractTheProfiler() throws CustomException {
        System.out.println(ANSI_CYAN + "[1/6] Initializing Profiler..." + ANSI_RESET);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("jar", "xvf", "Profiler.jar", "io/ballerina/runtime/profiler/runtime");
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException exception) {
            throw new CustomException(exception);
        }
    }

    public static void createTempJar(String balJarName) {
        try {
            System.out.println(ANSI_CYAN + "[2/6] Copying Executable..." + ANSI_RESET);
            Path sourcePath = Paths.get(balJarName);
            Path destinationPath = Paths.get(TEMP_JAR_FILE_NAME);
            Files.copy(sourcePath.toFile().toPath(), destinationPath.toFile().toPath());
        } catch (Exception exception) {
            exitCode = 2;
            System.err.println("Invalid File Name");
            System.exit(0);
        }
    }

    private static void initialize(String balJarName) throws CustomException {
        System.out.println(ANSI_CYAN + "[3/6] Performing Analysis..." + ANSI_RESET);
        ArrayList<String> classNames = new ArrayList<>();
        try {
            findAllClassNames(balJarName, classNames);
            findUtilityClasses(classNames);
        } catch (Exception e) {
            System.out.println("(No such file or directory)");
        }
        System.out.println(ANSI_CYAN + "[4/6] Instrumenting Functions..." + ANSI_RESET);
        try (JarFile jarFile = new JarFile(balJarName)) {
            String mainClassPackage = MethodWrapper.mainClassFinder(new URLClassLoader(new URL[]{new File(balJarName).toURI().toURL()}));
            CustomClassLoader customClassLoader = new CustomClassLoader(new URLClassLoader(new URL[]{new File(balJarName).toURI().toURL()}));
            Set<String> usedPaths = new HashSet<>();
            for (String className : classNames) {
                if (mainClassPackage == null) continue;
                if (className.startsWith(mainClassPackage.split("/")[0]) || utilPaths.contains(className)) {
                    try (InputStream inputStream = jarFile.getInputStream(jarFile.getJarEntry(className))) {
                        byte[] code = MethodWrapper.modifyMethods(inputStream);
                        customClassLoader.loadClass(code);
                        usedPaths.add(className.replace(".class", "").replace("/", "."));
                        MethodWrapper.printCode(className, code);
                    }
                }
                if (className.endsWith("/$_init.class")){
                    moduleCount++;
                }
            }
            System.out.println(" ○ Instrumented Module Count: " + moduleCount);
            try (PrintWriter printWriter = new PrintWriter("usedPathsList.txt")) {
                printWriter.println(String.join(", ", usedPaths));
            }
            System.out.println(" ○ Instrumented Function Count: " + balFunctionCount);

        } catch (Throwable throwable) {
            throw new CustomException(throwable);
        }
        try {
            modifyTheJar();
        } catch (Throwable throwable) {
            throw new CustomException(throwable);
        }
    }

    private static void modifyTheJar() throws InterruptedException, IOException {
        try {
            final File userDirectory = new File(System.getProperty("user.dir")); // Get the user directory
            listAllFiles(userDirectory); // List all files in the user directory and its subdirectories
            List<String> changedDirectories = instrumentedFiles.stream().distinct().collect(Collectors.toList()); // Get a list of the directories containing instrumented files
            loadDirectories(changedDirectories);
        } finally {
            for (String instrumentedFilePath : instrumentedPaths) {
                FileUtils.deleteDirectory(new File(instrumentedFilePath));
            }
            FileUtils.deleteDirectory(new File("io/ballerina/runtime/profiler/runtime"));
            MethodWrapper.invokeMethods();
        }
    }

    private static void loadDirectories(List<String> changedDirs) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("jar", "uf", TEMP_JAR_FILE_NAME);
            processBuilder.command().addAll(changedDirs);
            processBuilder.start().waitFor();
        } catch (Exception e) {
            System.err.println("Error loading directories: " + e.getMessage());
        }
    }

    public static void listAllFiles(final File userDirectory) {
        String absolutePath = Paths.get(TEMP_JAR_FILE_NAME).toFile().getAbsolutePath();
        absolutePath = absolutePath.replaceAll(TEMP_JAR_FILE_NAME, "");
        for (final File fileEntry : userDirectory.listFiles()) {
            if (fileEntry.isDirectory()) {
                listAllFiles(fileEntry);
            }
            else {
                String fileEntryString = String.valueOf(fileEntry);
                if (fileEntryString.endsWith(".class")) {
                    fileEntryString = fileEntryString.replaceAll(absolutePath, "");
                    int index = fileEntryString.lastIndexOf('/');
                    fileEntryString = fileEntryString.substring(0, index);
                    String[] fileEntryParts = fileEntryString.split("/");
                    instrumentedPaths.add(fileEntryParts[0]);
                    instrumentedFiles.add(fileEntryString);
                }
            }
        }
    }

    private static void findAllClassNames(String jarPath, ArrayList<String> classNames) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(jarPath)); // Create a ZipInputStream to read the jar file
        for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                classNames.add(String.valueOf(entry));
            }
        }
    }

    private static void findUtilityClasses(ArrayList<String> classNames) {
        for (String className : classNames) {
            if (className.endsWith("$_init.class")) {
                String path = className.substring(0, className.lastIndexOf('/') + 1);
                if (!utilInitPaths.contains(path)) {
                    utilInitPaths.add(path);
                }
            }
        }

        for (String name : classNames) {
            for (String path : utilInitPaths) {
                if (name.startsWith(path)) {
                    String subPath = name.substring(path.length());
                    if (subPath.indexOf('/') == -1) {
                        utilPaths.add(name);
                    }
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

    private static void tempFileCleanupShutdownHook() {
        // Add a shutdown hook to stop the profiler and parse the output when the program is closed.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                long profilerTotalTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS) - profilerStartTime;
                FileUtils.delete(new File(TEMP_JAR_FILE_NAME));
                System.out.println("\n" + ANSI_CYAN + "[6/6] Generating Output..." + ANSI_RESET);
                Thread.sleep(100);
                initializeCPUParser(skipFunctionString);
                FileUtils.delete(new File("usedPathsList.txt"));
                FileUtils.delete(new File("CpuPre.json"));
                System.out.println(" ○ Execution Time: " + profilerTotalTime / 1000 + " Seconds");
                deleteTempData();
                initializeHTMLExport();
                FileUtils.delete(new File("performance_report.json"));
                System.out.println("--------------------------------------------------------------------------------");
            } catch (Exception ignore) {}
            finally {
                String jarPath;
                try {
                    jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                new File(jarPath).delete();
            }
        }));
    }
}