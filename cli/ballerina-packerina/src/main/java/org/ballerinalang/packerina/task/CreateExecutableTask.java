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

import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.util.Lists;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Task for creating the executable jar file.
 */
public class CreateExecutableTask implements Task {
    
    private static HashSet<String> excludeExtensions =  new HashSet<>(Lists.of("DSA", "SF"));

    @Override
    public void execute(BuildContext buildContext) {
        Optional<BLangPackage> modulesWithEntryPoints = buildContext.getModules().stream()
                .filter(m -> m.symbol.entryPointExists)
                .findAny();
        
        if (modulesWithEntryPoints.isPresent()) {
            buildContext.out().println();
            buildContext.out().println("Generating executables");
            for (BLangPackage module : buildContext.getModules()) {
                if (module.symbol.entryPointExists) {
                    Path executablePath = buildContext.getExecutablePathFromTarget(module.packageID);
                    copyJarFromCachePath(buildContext, module, executablePath);
                    // Copy ballerina runtime all jar
                    URI uberJarUri = URI.create("jar:" + executablePath.toUri().toString());
                    // Load the to jar to a file system
                    try (FileSystem toFs = FileSystems.newFileSystem(uberJarUri, Collections.emptyMap())) {
                        assembleExecutable(module, buildContext.moduleDependencyPathMap.get(module.packageID), toFs);
                    } catch (IOException e) {
                        throw createLauncherException("unable to extract the uber jar :" + e.getMessage());
                    }
                }
            }
        } else {
            switch (buildContext.getSourceType()) {
                case SINGLE_BAL_FILE:
                    SingleFileContext singleFileContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    throw createLauncherException("no entry points found in '" + singleFileContext.getBalFile() + "'.");
                case SINGLE_MODULE:
                    SingleModuleContext singleModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    throw createLauncherException("no entry points found in '" + singleModuleContext.getModuleName() +
                                                  "'.\n" +
                            "Use `ballerina build -c` to compile the module without building executables.");
                case ALL_MODULES:
                    throw createLauncherException("no entry points found in any of the modules.\n" +
                            "Use `ballerina build -c` to compile the modules without building executables.");
                default:
                    throw createLauncherException("unknown source type found when creating executable.");
            }
        }
    }

    private void copyJarFromCachePath(BuildContext buildContext, BLangPackage bLangPackage, Path executablePath) {
        Path jarFromCachePath = buildContext.getJarPathFromTargetCache(bLangPackage.packageID);
        try {
            // Copy the jar from cache to bin directory
            Files.copy(jarFromCachePath, executablePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw createLauncherException("unable to copy the jar from cache path :" + e.getMessage());
        }
    }
    
    private void assembleExecutable(BLangPackage bLangPackage, HashSet<Path> dependencySet, FileSystem toFs) {
        try {
            // Check if the package has an entry point.
            if (bLangPackage.symbol.entryPointExists) {
                for (Path path : dependencySet) {
                    copyFromJarToJar(path, toFs);
                }
            }
            // Copy dependency jar
            // Copy dependency libraries
            // Executable is created at give location.
            // If no entry point is found we do nothing.
        } catch (IOException | NullPointerException e) {
            throw createLauncherException("unable to create the executable: " + e.getMessage());
        }
    }

    private static void copyFromJarToJar(Path fromJar, FileSystem toFs) throws IOException {
        Path to = toFs.getRootDirectories().iterator().next();
        URI moduleJarUri = URI.create("jar:" + fromJar.toUri().toString());
        // Load the from jar to a file system.
        try (FileSystem fromFs = FileSystems.newFileSystem(moduleJarUri, Collections.emptyMap())) {
            Path from = fromFs.getRootDirectories().iterator().next();
            // Walk and copy the files.
            Files.walkFileTree(from, new Copy(from, to));
        }
    }

    static class Copy extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;
        
        
        Copy(Path fromPath, Path toPath, StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }
        
        Copy(Path fromPath, Path toPath) {
            this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

         @Override
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
             Path toFile = toPath.resolve(fromPath.relativize(file).toString());
             String fileName = toFile.getFileName().toString();
             if ((!Files.exists(toFile) &&
                     !excludeExtensions.contains(fileName.substring(fileName.lastIndexOf(".") + 1)))) {
                 Files.copy(file, toFile, copyOption);
             } else if (toFile.toString().startsWith("/META-INF/services")) {
                 this.mergeSPIFiles(file, toFile);
             }
             return FileVisitResult.CONTINUE;
         }

         private void mergeSPIFiles(Path fromFilePath, Path toFilePath) throws IOException {
             // Merge the spi implementations for each service file.
             try (BufferedReader fromBr = new BufferedReader(new InputStreamReader(Files
                     .newInputStream(fromFilePath), StandardCharsets.UTF_8));
                  BufferedWriter toBw = new BufferedWriter(new OutputStreamWriter(Files
                          .newOutputStream(toFilePath, StandardOpenOption.APPEND), StandardCharsets.UTF_8))) {
                 String text;
                 while ((text = fromBr.readLine()) != null) {
                     toBw.newLine();
                     toBw.write(text);
                 }
             }
         }
     }
}
