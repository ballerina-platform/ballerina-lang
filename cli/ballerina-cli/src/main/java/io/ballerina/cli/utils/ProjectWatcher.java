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
    private static final String BAL_EXTENSION = ".bal";
    private static final String TOML_EXTENSION = ".toml";
    private static final String DEPENDENCIES_TOML = "Dependencies.toml";
    private static final String RESOURCES_DIR = "resources";
    private static final String MODULES_DIR = "modules";

    private final WatchService fileWatcher;
    private final Map<WatchKey, Path> watchKeys;
    private final RunCommand runCommand;
    private final PrintStream outStream;
    private final Path projectPath;
    private final ProjectKind projectKind;

    public ProjectWatcher(RunCommand runCommand, Path projectPath, PrintStream outStream) throws IOException {
        this.fileWatcher = FileSystems.getDefault().newWatchService();
        this.runCommand = runCommand;
        this.projectPath = projectPath.toAbsolutePath();
        this.outStream = outStream;
        this.watchKeys = new HashMap<>();
        this.projectKind = deriveProjectKind();
        registerFileTree(projectPath);
    }

    public void watch() throws IOException, InterruptedException {
        RunCommandExecutor thread = new RunCommandExecutor(runCommand, outStream);
        thread.start();
        while (thread.shouldWatch()) {
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
                    outStream.println("\\nDetected file changes. Re-running the project...");
                    thread.terminate();
                    thread.join();
                    thread = new RunCommandExecutor(runCommand, outStream);
                    thread.start();
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
             if (fileName.endsWith(BAL_EXTENSION)) {
                 return true;
             }
            return fileName.endsWith(TOML_EXTENSION) && !fileName.equals(DEPENDENCIES_TOML);
        }
        if (path.startsWith(RESOURCES_DIR) && path.getNameCount() > 1) {
            // name count > 1 to avoid files with resources prefix
            return true;
        }
        if (path.startsWith(MODULES_DIR) && path.getNameCount() > 2) {
            // changes within submodules
            // name count > 2 means the path is in a submodule (modules/<submodule>)
            Path modulePath = path.subpath(2, path.getNameCount());
            if (modulePath.getNameCount() == 1 && fileName.endsWith(BAL_EXTENSION)) {
                    return true;
            }
            return modulePath.startsWith(RESOURCES_DIR);
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

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }
}
