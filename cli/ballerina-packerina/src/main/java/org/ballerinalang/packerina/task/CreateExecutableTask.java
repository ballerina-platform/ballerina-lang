/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.task;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

/**
 * Task for creating the executable jar file.
 */
public class CreateExecutableTask implements Task {
    
    @Override
    public void execute(BuildContext buildContext) {
        
        buildContext.out().println("Generating executables");
        
        for (BLangPackage module : buildContext.getModules()) {
            if (module.symbol.entryPointExists) {
                assembleExecutable(buildContext, module);
            }
        }
    }
    
    private void assembleExecutable(BuildContext buildContext, BLangPackage bLangPackage) {
        try {
            // Copy the jar from cache to bin directory
            Path executablePath = buildContext.getExecutablePathFromTarget(bLangPackage.packageID);

            Path targetDir = buildContext.get(BuildContextField.TARGET_DIR);
            Path tmpDir = targetDir.resolve(ProjectDirConstants.TARGET_TMP_DIRECTORY);
            Path jarFromCachePath = buildContext.getJarPathFromTargetCache(bLangPackage.packageID);

            // Check if the package has an entry point.
            if (bLangPackage.symbol.entryPointExists) {
                Files.copy(jarFromCachePath, executablePath, StandardCopyOption.REPLACE_EXISTING);
                for (File file : tmpDir.toFile().listFiles()) {
                    if (!file.isDirectory()) {
                        copyFromJarToJar(file.toPath(), executablePath);
                    }
                }
            }
            // Copy dependency jar
            // Copy dependency libraries
            // Executable is created at give location.
            // If no entry point is found we do nothing.
        } catch (IOException | NullPointerException e) {
            throw new BLangCompilerException("Unable to create the executable :" + e.getMessage());
        }
    }

    private static void copyFromJarToJar(Path fromJar, Path toJar) throws IOException {
        URI uberJarUri = URI.create("jar:" + toJar.toUri().toString());
        // Load the to jar to a file system
        try (FileSystem toFs = FileSystems.newFileSystem(uberJarUri, Collections.emptyMap())) {
            Path to = toFs.getRootDirectories().iterator().next();
            URI moduleJarUri = URI.create("jar:" + fromJar.toUri().toString());
            // Load the from jar to a file system.
            try (FileSystem fromFs = FileSystems.newFileSystem(moduleJarUri, Collections.emptyMap())) {
                Path from = fromFs.getRootDirectories().iterator().next();
                // Walk and copy the files.
                Files.walkFileTree(from, new Copy(from, to));
            }
        }
    }
    
    static class Copy extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;
        
        
        public Copy(Path fromPath, Path toPath, StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }
        
        public Copy(Path fromPath, Path toPath) {
            this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
            
            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            Path toFile = toPath.resolve(fromPath.relativize(file).toString());
            if (!Files.exists(toFile)) {
                Files.copy(file, toFile, copyOption);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
