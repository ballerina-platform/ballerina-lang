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
import io.ballerina.runtime.profiler.util.CustomException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
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
    static String skipFunctionString = null;
    private static int balFunctionCount = 0;
    static int moduleCount = 0;

    static final List<String> INSTRUMENTEDPATHS = new ArrayList<>();
    static final List<String> INSTRUMENTEDFILES = new ArrayList<>();
    static final List<String> UTILINITPATHS = new ArrayList<>();
    static final List<String> UTILPATHS = new ArrayList<>();

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
        String header = "%n" +
                Constants.ANSI_GRAY
                + "================================================================================" +
                Constants.ANSI_RESET
                + "%n" +
                Constants.ANSI_CYAN
                + "Ballerina Profiler" + Constants.ANSI_RESET + ": Profiling..." + "%n" +
                Constants.ANSI_GRAY
                + "================================================================================" +
                Constants.ANSI_RESET
                + "%n" +
                "WARNING : Ballerina Profiler is an experimental feature.";
        System.out.printf(header + "%n");
    }

    private static void handleProfilerArguments(String[] args) {
        String invalidArgument = "Invalid CLI Argument";
        if (args.length != 0) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--file":
                        balJarName = args[i + 1];
                        if (balJarName.startsWith("[") && balJarName.endsWith("]")) {
                            balJarName = balJarName.substring(1, balJarName.length() - 1);
                        } else {
                            System.out.printf(invalidArgument + "\n");
                        }
                        break;
                    case "--args":
                        balJarArgs = args[i + 1];
                        if (balJarArgs != null && balJarArgs.startsWith("[") && balJarArgs.endsWith("]")) {
                            balJarArgs = balJarArgs.substring(1, balJarArgs.length() - 1);
                        } else {
                            System.out.printf(invalidArgument + "\n");
                        }
                        break;
                    case "--skip":
                        skipFunctionString = args[i + 1];
                        if (skipFunctionString != null && skipFunctionString.matches("\\[.*\\]")) {
                            skipFunctionString = skipFunctionString.substring(1, skipFunctionString.length() - 1);
                        } else {
                            System.out.printf(invalidArgument + "\n");
                        }
                        break;
                    default:
                        // Handle unrecognized arguments here
                        System.out.printf(invalidArgument + "\n");
                        break;
                }
            }
        }
    }

    private static void extractTheProfiler() throws CustomException {
        System.out.printf(Constants.ANSI_CYAN + "[1/6] Initializing Profiler..." + Constants.ANSI_RESET + "%n");
        try {
            new ProcessBuilder("jar", "xvf", "Profiler.jar", "io/ballerina/runtime/profiler/runtime")
                    .start()
                    .waitFor();
        } catch (IOException | InterruptedException exception) {
            throw new CustomException(exception);
        }
    }

    public static void createTempJar(String balJarName) {
        try {
            System.out.printf(Constants.ANSI_CYAN + "[2/6] Copying Executable..." + Constants.ANSI_RESET + "%n");
            Path sourcePath = Paths.get(balJarName);
            Path destinationPath = Paths.get(Constants.TEMPJARFILENAME);
            Files.copy(sourcePath, destinationPath);
        } catch (IOException e) {
            exitCode = 2;
            System.out.printf("Error occurred while copying the file: %s%n", e.getMessage());
        }
    }

    private static void initialize(String balJarName) throws CustomException {
        System.out.printf(Constants.ANSI_CYAN + "[3/6] Performing Analysis..." + Constants.ANSI_RESET + "%n");
        ArrayList<String> classNames = new ArrayList<>();
        try {
            findAllClassNames(balJarName, classNames);
            findUtilityClasses(classNames);
        } catch (Exception e) {
            System.out.printf("(No such file or directory)" + "%n");
        }
        System.out.printf(Constants.ANSI_CYAN + "[4/6] Instrumenting Functions..." + Constants.ANSI_RESET + "%n");
        try (JarFile jarFile = new JarFile(balJarName)) {
            String mainClassPackage = MethodWrapper.mainClassFinder(
                    new URLClassLoader(new URL[]{new File(balJarName).toURI().toURL()}));
            CustomClassLoader customClassLoader = new CustomClassLoader(
                    new URLClassLoader(new URL[]{new File(balJarName).toURI().toURL()}));
            Set<String> usedPaths = new HashSet<>();
            for (String className : classNames) {
                if (mainClassPackage == null) {
                    continue;
                }
                if (className.startsWith(mainClassPackage.split("/")[0]) || UTILPATHS.contains(className)) {
                    try (InputStream inputStream = jarFile.getInputStream(jarFile.getJarEntry(className))) {
                        byte[] code = MethodWrapper.modifyMethods(inputStream);
                        customClassLoader.loadClass(code);
                        usedPaths.add(className.replace(".class", "").replace("/", "."));
                        MethodWrapper.printCode(className, code);
                    }
                }
                if (className.endsWith("/$_init.class")) {
                    moduleCount++;
                }
            }
            System.out.printf(" ○ Instrumented Module Count: " + moduleCount + "%n");
            try (PrintWriter printWriter = new PrintWriter("usedPathsList.txt", StandardCharsets.UTF_8)) {
                printWriter.println(String.join(", ", usedPaths));
            }
            System.out.printf(" ○ Instrumented Function Count: " + balFunctionCount + "%n");

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
            // Get a list of the directories containing instrumented files
            List<String> changedDirectories = INSTRUMENTEDFILES.stream().distinct().collect(Collectors.toList());
            loadDirectories(changedDirectories);
        } finally {
            for (String instrumentedFilePath : INSTRUMENTEDPATHS) {
                FileUtils.deleteDirectory(new File(instrumentedFilePath));
            }
            FileUtils.deleteDirectory(new File("io/ballerina/runtime/profiler/runtime"));
            MethodWrapper.invokeMethods();
        }
    }

    private static void loadDirectories(List<String> changedDirs) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("jar", "uf", Constants.TEMPJARFILENAME);
            processBuilder.command().addAll(changedDirs);
            processBuilder.start().waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void listAllFiles(final File userDirectory) {
        String absolutePath = Paths.get(Constants.TEMPJARFILENAME).toFile().getAbsolutePath();
        absolutePath = absolutePath.replaceAll(Constants.TEMPJARFILENAME, "");

        File[] files = userDirectory.listFiles();
        if (files != null) {
            for (final File fileEntry : files) {
                if (fileEntry.isDirectory()) {
                    listAllFiles(fileEntry);
                } else {
                    String fileEntryString = String.valueOf(fileEntry);
                    if (fileEntryString.endsWith(".class")) {
                        fileEntryString = fileEntryString.replaceAll(absolutePath, "");
                        int index = fileEntryString.lastIndexOf('/');
                        fileEntryString = fileEntryString.substring(0, index);
                        String[] fileEntryParts = fileEntryString.split("/");
                        INSTRUMENTEDPATHS.add(fileEntryParts[0]);
                        INSTRUMENTEDFILES.add(fileEntryString);
                    }
                }
            }
        }
    }

    private static void findAllClassNames(String jarPath, ArrayList<String> classNames) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(jarPath))) {
            for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    classNames.add(String.valueOf(entry));
                }
            }
        }
    }

    private static void findUtilityClasses(ArrayList<String> classNames) {
        for (String className : classNames) {
            if (className.endsWith("$_init.class")) {
                String path = className.substring(0, className.lastIndexOf('/') + 1);
                if (!UTILINITPATHS.contains(path)) {
                    UTILINITPATHS.add(path);
                }
            }
        }

        for (String name : classNames) {
            for (String path : UTILINITPATHS) {
                if (name.startsWith(path)) {
                    String subPath = name.substring(path.length());
                    if (subPath.indexOf('/') == -1) {
                        UTILPATHS.add(name);
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
                long profilerTotalTime = TimeUnit.MILLISECONDS.convert(
                        System.nanoTime(), TimeUnit.NANOSECONDS) - profilerStartTime;
                File tempJarFile = new File(Constants.TEMPJARFILENAME);
                if (tempJarFile.exists()) {
                    boolean deleted = tempJarFile.delete();
                    if (!deleted) {
                        System.err.printf("Failed to delete temp jar file: " + Constants.TEMPJARFILENAME + "%n");
                    }
                }
                System.out.printf("%n" + Constants.ANSI_CYAN
                        + "[6/6] Generating Output..." + Constants.ANSI_RESET + "%n");
                Thread.sleep(100);
                initializeCPUParser(skipFunctionString);
                deleteFileIfExists("usedPathsList.txt");
                deleteFileIfExists("CpuPre.json");
                System.out.printf(" ○ Execution Time: " + profilerTotalTime / 1000 + " Seconds" + "%n");
                deleteTempData();
                initializeHTMLExport();
                deleteFileIfExists("performance_report.json");
                System.out.printf("----------------------------------------");
                System.out.printf("----------------------------------------" + "%n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                String jarPath;
                try {
                    jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                File jarFile = new File(jarPath);
                if (jarFile.exists()) {
                    boolean deleted = jarFile.delete();
                    if (!deleted) {
                        System.err.printf("Failed to delete jar file: " + jarPath + "%n");
                    }
                }
            }
        }));
    }

    private static void deleteFileIfExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.err.printf("Failed to delete file: " + filePath + "%n");
            }
        }
    }

    public static void incrementBalFunctionCount() {
        balFunctionCount++;
    }

    public static String getBalJarArgs() {
        return balJarArgs;
    }
}
