/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package io.ballerina.cli.utils;

import io.ballerina.cli.cmd.RunCommand;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.internal.ProjectFiles;
import io.ballerina.projects.util.FileUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.ballerina.projects.util.ProjectConstants.BLANG_SOURCE_EXT;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.MODULES_ROOT;
import static io.ballerina.projects.util.ProjectConstants.RESOURCE_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.TOML_EXTENSION;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Represents a watcher that looks into project file changes.
 *
 * @since 2201.10.0
 */
public class ProjectWatcher {
    private final WatchService fileWatcher;
    private final Map<WatchKey, Path> watchKeys;
    private final RunCommand runCommand;
    private final PrintStream outStream;
    private final Path projectPath;
    private final ProjectKind projectKind;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Map<Path, Long> debounceMap = new ConcurrentHashMap<>();
    private static final long debounceTimeMillis = 250;
    private final RunCommandExecutor[] thread;
    private volatile boolean forceStop = false;

    public ProjectWatcher(RunCommand runCommand, Path projectPath, PrintStream outStream) throws IOException {
        this.fileWatcher = FileSystems.getDefault().newWatchService();
        this.runCommand = runCommand;
        thread = new RunCommandExecutor[]{new RunCommandExecutor(runCommand, outStream)};
        this.projectPath = projectPath.toAbsolutePath();
        this.outStream = outStream;
        this.watchKeys = new HashMap<>();
        this.projectKind = deriveProjectKind();
        validateProjectPath();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        registerFileTree(projectPath);
    }

    /**
     * Watches for any file changes in a Ballerina service project and restarts the service.
     * Changes on source files, resources and .toml files are considered valid file changes. Changes to
     * Dependencies.toml, tests, target directory and other files are ignored.
     *
     * @throws IOException if the watcher cannot register files for watching.
     */
    public void watch() throws IOException { // TODO: find out why panics and removing service doesn't exit the code
        thread[0].start();
        while (thread[0].shouldWatch() && !forceStop) {
            WatchKey key;
            key = fileWatcher.poll();
            Path dir = watchKeys.get(key);
            if (dir == null) {
                continue;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == OVERFLOW) {
                    continue;
                }
                WatchEvent<Path> pathWatchEvent = cast(event);
                Path changedFileName = pathWatchEvent.context();
                Path changedFilePath = dir.resolve(changedFileName).toAbsolutePath();
                if (isValidFileChange(changedFilePath)) {
                    long currentTime = System.currentTimeMillis();
                    debounceMap.put(changedFilePath, currentTime);
                    scheduledExecutorService.schedule(() -> {
                        Long lastModifiedTime = debounceMap.get(changedFilePath);
                        if (lastModifiedTime == null
                                || (System.currentTimeMillis() - lastModifiedTime < debounceTimeMillis)) {
                            return;
                        }
                        outStream.println("\nDetected file changes. Re-running the project...");
                        thread[0].terminate();
                        waitForRunCmdThreadToJoin();
                        thread[0] = new RunCommandExecutor(runCommand, outStream);
                        thread[0].start();
                        debounceMap.remove(changedFilePath);
                    }, debounceTimeMillis, TimeUnit.MILLISECONDS);
                }
                if (kind == ENTRY_CREATE && Files.isDirectory(changedFilePath)) {
                    registerFileTree(changedFilePath);
                }
            }
            boolean valid = key.reset();
            if (!valid) {
                watchKeys.remove(key);
                if (watchKeys.isEmpty()) {
                    break;
                }
            }
        }
        waitForRunCmdThreadToJoin();
    }

    public void stopWatching() {
        try {
            if (thread != null) {
                thread[0].terminate();
                thread[0].join();
            }
            forceStop = true;
            fileWatcher.close();
        } catch (IOException | InterruptedException e) {
            outStream.println("Error occurred while stopping the project watcher: " + e.getMessage());
        }
    }

    private ProjectKind deriveProjectKind() {
        return FileUtils.hasExtension(this.projectPath) ? ProjectKind.SINGLE_FILE_PROJECT : ProjectKind.BUILD_PROJECT;
    }

    private boolean isValidFileChange(Path path) {
        // If single file project, we only consider changes to the file itself
        if (projectKind.equals(ProjectKind.SINGLE_FILE_PROJECT)) {
            return projectPath.equals(path);
        }
        path = projectPath.relativize(path);
        if (Files.isDirectory(path)) { // We ignore the directory changes
            return false;
        }
        Path fileNamePath = path.getFileName();
        if (fileNamePath == null) {
            return false;
        }
        String fileName = fileNamePath.toString();
        if (path.getNameCount() == 1) {
            // Files (not directories) immediately in the root directory
             if (fileName.endsWith(BLANG_SOURCE_EXT)) {
                 return true;
             }
            return fileName.endsWith(TOML_EXTENSION) && !fileName.equals(DEPENDENCIES_TOML);
        }
        if (path.startsWith(RESOURCE_DIR_NAME) && path.getNameCount() > 1) {
            // name count > 1 to avoid files with resources prefix
            return true;
        }
        if (path.startsWith(MODULES_ROOT) && path.getNameCount() > 2) {
            // changes within submodules
            // name count > 2 means the path is in a submodule (modules/<submodule>)
            Path modulePath = path.subpath(2, path.getNameCount());
            if (modulePath.getNameCount() == 1 && fileName.endsWith(BLANG_SOURCE_EXT)) {
                    return true;
            }
            return modulePath.startsWith(RESOURCE_DIR_NAME);
        }
        return false;
    }

    private void registerFileTree(Path path) throws IOException {
        if (projectKind.equals(ProjectKind.SINGLE_FILE_PROJECT)) {
            Path parentPath = path.toAbsolutePath().getParent();
            if (parentPath != null) {
                register(parentPath);
            }
            return;
        }
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(fileWatcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        watchKeys.put(key, dir);
    }

    private void validateProjectPath() {
        if (projectKind.equals(ProjectKind.SINGLE_FILE_PROJECT)) {
            ProjectFiles.validateSingleFileProjectFilePath(projectPath);
        } else {
            ProjectFiles.validateBuildProjectDirPath(projectPath);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    private void waitForRunCmdThreadToJoin() {
        try {
            thread[0].join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
