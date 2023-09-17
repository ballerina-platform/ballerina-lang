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

import io.ballerina.runtime.profiler.codegen.ProfilerClassLoader;
import io.ballerina.runtime.profiler.codegen.ProfilerMethodWrapper;
import io.ballerina.runtime.profiler.ui.HttpServer;
import io.ballerina.runtime.profiler.ui.JsonParser;
import io.ballerina.runtime.profiler.util.Constants;
import io.ballerina.runtime.profiler.util.ProfilerException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

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

import static io.ballerina.runtime.profiler.util.Constants.CPU_PRE_JSON;
import static io.ballerina.runtime.profiler.util.Constants.OUT_STREAM;

/**
 * This class is used to as the driver class of the Ballerina profiler.
 *
 * @since 2201.8.0
 */
public class Profiler {

    private final long profilerStartTime;
    private String skipFunctionString = null;
    private String balJarArgs = null;
    private String balJarName = null;
    private String targetDir = null;
    private String sourceRoot = null;
    private String profilerDebugArg = null;
    private final List<String> instrumentedPaths = new ArrayList<>();
    private final List<String> instrumentedFiles = new ArrayList<>();
    private final List<String> utilInitPaths = new ArrayList<>();
    private final List<String> utilPaths = new ArrayList<>();
    private int balFunctionCount = 0;
    private int moduleCount = 0;
    private final ProfilerMethodWrapper profilerMethodWrapper;

    public Profiler(long profilerStartTime) {
        this.profilerStartTime = profilerStartTime;
        this.profilerMethodWrapper = new ProfilerMethodWrapper();
    }

    private void addShutdownHookAndCleanup() {
        // Add a shutdown hook to stop the profiler and parse the output when the program is closed.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                long profilerTotalTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS) -
                        profilerStartTime;
                deleteFileIfExists(Constants.TEMP_JAR_FILE_NAME);
                OUT_STREAM.printf("%s[6/6] Generating output...%s%n", Constants.ANSI_CYAN, Constants.ANSI_RESET);
                JsonParser jsonParser = new JsonParser();
                HttpServer httpServer = new HttpServer();
                jsonParser.initializeCPUParser(skipFunctionString);
                deleteFileIfExists("usedPathsList.txt");
                deleteFileIfExists(CPU_PRE_JSON);
                OUT_STREAM.printf(" ○ Execution time: %d seconds %n", profilerTotalTime / 1000);
                deleteTempData();
                httpServer.initializeHTMLExport(this.sourceRoot);
                deleteFileIfExists("performance_report.json");
                OUT_STREAM.println("--------------------------------------------------------------------------------");
            } catch (IOException e) {
                throw new ProfilerException("Error occurred while generating the output", e);
            } finally {
                deleteFileIfExists(targetDir);
            }
        }));
    }

    private void deleteFileIfExists(String filePath) {
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

    private void deleteTempData() {
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

    private void printHeader() {
        OUT_STREAM.printf("%n%s================================================================================%s",
                Constants.ANSI_GRAY, Constants.ANSI_RESET);
        OUT_STREAM.printf("%n%sBallerina Profiler%s", Constants.ANSI_CYAN, Constants.ANSI_RESET);
        OUT_STREAM.printf("%s: Profiling...%s", Constants.ANSI_GRAY, Constants.ANSI_RESET);
        OUT_STREAM.printf("%n%s================================================================================%s",
                Constants.ANSI_GRAY, Constants.ANSI_RESET);
        OUT_STREAM.printf("%n%sNote: This is an experimental feature, which supports only a limited set of " +
                "functionality.%s%n", Constants.ANSI_GRAY, Constants.ANSI_RESET);
    }

    private void handleProfilerArguments(String[] args) {
        if (args.length == 0) {
            return;
        }
        List<String> usedArgs = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--file" -> {
                    this.balJarName = extractFileArg(args[i + 1]);
                    addToUsedArgs(args, usedArgs, i);
                }
                case "--args" -> {
                    this.balJarArgs = extractBalJarArgs(args[i + 1]);
                    addToUsedArgs(args, usedArgs, i);
                }
                case "--skip" -> {
                    this.skipFunctionString = extractSkipFunctionString(args[i + 1]);
                    addToUsedArgs(args, usedArgs, i);
                }
                case "--target" -> {
                    this.targetDir = args[i + 1];
                    addToUsedArgs(args, usedArgs, i);
                }
                case "--sourceroot" -> {
                    this.sourceRoot = args[i + 1];
                    addToUsedArgs(args, usedArgs, i);
                }
                case "--profilerDebug" -> {
                    this.profilerDebugArg = args[i + 1];
                    addToUsedArgs(args, usedArgs, i);
                }
                default -> handleUnrecognizedArgument(args[i], usedArgs);
            }
        }
    }

    private String extractFileArg(String value) {
        if (!value.endsWith(".jar")) {
            throw new ProfilerException("Invalid file argument found: " + value);
        }
        return value;
    }

    private String extractBalJarArgs(String value) {
        if (value == null || !value.startsWith("[") || !value.endsWith("]")) {
            throw new ProfilerException("Invalid JAR arguments found: " + value);
        }
        return value.substring(1, value.length() - 1);
    }

    private String extractSkipFunctionString(String value) {
        if (value == null || !value.matches("\\[.*\\]")) {
            throw new ProfilerException("Invalid skip functions found: " + value);
        }
        return value.substring(1, value.length() - 1);
    }

    private void handleUnrecognizedArgument(String argument, List<String> usedArgs) {
        if (!usedArgs.contains(argument)) {
            throw new ProfilerException("Unrecognized argument found: " + argument);
        }
    }

    private void addToUsedArgs(String[] args, List<String> usedArgs, int i) {
        usedArgs.add(args[i]);
        usedArgs.add(args[i + 1]);
    }

    private void extractProfiler() throws ProfilerException {
        OUT_STREAM.printf("%s[1/6] Initializing...%s%n", Constants.ANSI_CYAN, Constants.ANSI_RESET);
        try {
            new ProcessBuilder("jar", "xvf", "Profiler.jar", "io/ballerina/runtime/profiler/runtime").start().waitFor();
        } catch (IOException | InterruptedException exception) {
            throw new ProfilerException(exception);
        }
    }

    private void createTempJar() {
        try {
            OUT_STREAM.printf("%s[2/6] Copying executable...%s%n", Constants.ANSI_CYAN, Constants.ANSI_RESET);
            Path sourcePath = Paths.get(balJarName);
            Path destinationPath = Paths.get(Constants.TEMP_JAR_FILE_NAME);
            Files.copy(sourcePath, destinationPath);
        } catch (IOException e) {
            throw new ProfilerException("Error occurred while copying the file: " + balJarName, e);
        }
    }

    private void initializeProfiling() throws ProfilerException {
        OUT_STREAM.printf("%s[3/6] Performing analysis...%s%n", Constants.ANSI_CYAN, Constants.ANSI_RESET);
        ArrayList<String> classNames = new ArrayList<>();
        try {
            findAllClassNames(balJarName, classNames);
            findUtilityClasses(classNames);
        } catch (Exception e) {
            throw new ProfilerException("error occurred while performing analysis", e);
        }
        OUT_STREAM.printf("%s[4/6] Instrumenting functions...%s%n", Constants.ANSI_CYAN, Constants.ANSI_RESET);
        try (JarFile jarFile = new JarFile(balJarName)) {
            String mainClassPackage = profilerMethodWrapper.mainClassFinder(new URLClassLoader(new URL[]{
                    new File(balJarName).toURI().toURL()}));
            ProfilerClassLoader profilerClassLoader = new ProfilerClassLoader(new URLClassLoader(new URL[]{
                    new File(balJarName).toURI().toURL()}));
            Set<String> usedPaths = new HashSet<>();
            for (String className : classNames) {
                if (mainClassPackage == null) {
                    continue;
                }
                if (className.startsWith(mainClassPackage.split("/")[0]) || utilPaths.contains(className)) {
                    try (InputStream inputStream = jarFile.getInputStream(jarFile.getJarEntry(className))) {
                        byte[] code = profilerMethodWrapper.modifyMethods(inputStream);
                        profilerClassLoader.loadClass(code);
                        usedPaths.add(className.replace(Constants.CLASS_SUFFIX, "").replace("/", "."));
                        profilerMethodWrapper.printCode(className, code, getFileNameWithoutExtension(balJarName));
                    }
                }
                if (className.endsWith("/$_init.class")) {
                    moduleCount++;
                }
            }
            OUT_STREAM.printf(" ○ Instrumented module count: %d%n", moduleCount);
            try (PrintWriter printWriter = new PrintWriter("usedPathsList.txt", StandardCharsets.UTF_8)) {
                printWriter.println(String.join(", ", usedPaths));
            }
            OUT_STREAM.printf(" ○ Instrumented function count: %d%n", balFunctionCount);
            modifyJar();
        } catch (Throwable throwable) {
            throw new ProfilerException(throwable);
        }
    }

    private void modifyJar() throws InterruptedException, IOException {
        try {
            final File userDirectory = new File(System.getProperty("user.dir")); // Get the user directory
            listAllFiles(userDirectory); // List all files in the user directory and its subdirectories
            // Get a list of the directories containing instrumented files
            List<String> changedDirectories = instrumentedFiles.stream().distinct().collect(Collectors.toList());
            loadDirectories(changedDirectories);
        } finally {
            for (String instrumentedFilePath : instrumentedPaths) {
                FileUtils.deleteDirectory(new File(instrumentedFilePath));
            }
            FileUtils.deleteDirectory(new File("io/ballerina/runtime/profiler/runtime"));
            profilerMethodWrapper.invokeMethods(profilerDebugArg);
        }
    }

    private void loadDirectories(List<String> changedDirs) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("jar", "uf", Constants.TEMP_JAR_FILE_NAME);
            processBuilder.command().addAll(changedDirs);
            processBuilder.start().waitFor();
        } catch (IOException | InterruptedException e) {
            throw new ProfilerException("Error occurred while loading the jar file", e);
        }
    }

    private void listAllFiles(final File userDirectory) {
        String absolutePath = Paths.get(Constants.TEMP_JAR_FILE_NAME).toFile().getAbsolutePath();
        analyseInstrumentedDirectories(userDirectory, absolutePath.replaceAll(Constants.TEMP_JAR_FILE_NAME, ""));
    }

    private void analyseInstrumentedDirectories(File directory, String absolutePath) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (final File fileEntry : files) {
                if (fileEntry.isDirectory()) {
                    analyseInstrumentedDirectories(fileEntry, absolutePath);
                } else {
                    updateInstrumentedFile(fileEntry, absolutePath);
                }
            }
        }
    }

    private void updateInstrumentedFile(File fileEntry, String absolutePath) {
        String fileEntryString = String.valueOf(fileEntry);
        if (fileEntryString.endsWith(Constants.CLASS_SUFFIX)) {
            fileEntryString = fileEntryString.replaceAll(absolutePath, "");
            int index = fileEntryString.lastIndexOf('/');
            fileEntryString = index == -1 ? "" : fileEntryString.substring(0, index);
            String[] fileEntryParts = fileEntryString.split("/");
            instrumentedPaths.add(fileEntryParts[0]);
            instrumentedFiles.add(fileEntryString);
        }
    }

    private void findAllClassNames(String jarPath, ArrayList<String> classNames) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(jarPath))) {
            for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                if (!entry.isDirectory() && entry.getName().endsWith(Constants.CLASS_SUFFIX)) {
                    classNames.add(String.valueOf(entry));
                }
            }
        }
    }

    private void findUtilityClasses(ArrayList<String> classNames) {
        populateInitPaths(classNames);
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

    private void populateInitPaths(ArrayList<String> classNames) {
        for (String className : classNames) {
            if (className.endsWith("$_init.class")) {
                String path = className.substring(0, className.lastIndexOf('/') + 1);
                if (!utilInitPaths.contains(path)) {
                    utilInitPaths.add(path);
                }
            }
        }
    }

    void incrementBalFunctionCount() {
        balFunctionCount++;
    }

    String getBalJarArgs() {
        return balJarArgs;
    }

    private String getFileNameWithoutExtension(String balJarName) {
        if (null != balJarName) {
            int index = FilenameUtils.indexOfExtension(balJarName);
            return index == -1 ? balJarName : balJarName.substring(0, index);
        } else {
            return null;
        }
    }

    void start(String[] args) {
        addShutdownHookAndCleanup();
        printHeader();
        handleProfilerArguments(args);
        extractProfiler();
        createTempJar();
        initializeProfiling();
    }
}
