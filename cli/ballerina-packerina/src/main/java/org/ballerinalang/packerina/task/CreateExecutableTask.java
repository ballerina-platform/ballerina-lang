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
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Task for creating the executable jar file.
 */
public class CreateExecutableTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {
        OUT.println("Generating executables");
        for (BLangPackage module : buildContext.getModules()) {
            if (module.symbol.entryPointExists) {
                assembleExecutable(buildContext, module);
            }
        }
    }
    
    private static void assembleExecutable(BuildContext buildContext, BLangPackage bLangPackage) {
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        try {
            // Copy the jar from cache to bin directory
            Path executablePath = buildContext.getExecutablePathFromTarget(bLangPackage.packageID);
            Path jarFromCachePath = buildContext.getJarPathFromTargetCache(bLangPackage.packageID);
            
            // Check if the package has an entry point.
            if (bLangPackage.symbol.entryPointExists) {
                Files.copy(jarFromCachePath, executablePath, StandardCopyOption.REPLACE_EXISTING);
                // Get the fs handle to the jar file
                
                // Iterate through the imports and copy dependencies.
                for (BPackageSymbol importz : bLangPackage.symbol.imports) {
                    Path importJar = findImportJarPath(buildContext, importz, sourceRootPath);
                    
                    if (importJar != null && Files.exists(importJar)) {
                        copyFromJarToJar(importJar, executablePath);
                    }
                }
    
                // Iterate through .balo and copy the platform libs
                Path baloAbsolutePath = buildContext.getBaloFromTarget(bLangPackage.packageID);
                String destination = extractJar(baloAbsolutePath.toString());
    
                if (Files.exists(Paths.get(destination).resolve(ProjectDirConstants.BALO_PLATFORM_LIB_DIR_NAME))) {
                    try (Stream<Path> walk = Files.walk(Paths.get(destination)
                            .resolve(ProjectDirConstants.BALO_PLATFORM_LIB_DIR_NAME))) {
            
                        List<String> result = walk.filter(Files::isRegularFile)
                                .map(Path::toString)
                                .collect(Collectors.toList());
            
                        result.forEach(lib -> {
                            try {
                                copyFromJarToJar(Paths.get(lib), executablePath);
                            } catch (Exception e) {
                                throw new BLangCompilerException("unable to create the executable :" + e.getMessage());
                            }
                        });
                    } catch (IOException e) {
                        throw new BLangCompilerException("unable to create the executable :" + e.getMessage());
                    }
                }
    
            }
            // Copy dependency jar
            // Copy dependency libraries
            // Executable is created at give location.
            OUT.println(sourceRootPath.relativize(executablePath).toString());
            // If no entry point is found we do nothing.
        } catch (IOException e) {
            throw new BLangCompilerException("Unable to create the executable :" + e.getMessage());
        }
    }
    
    private static Path findImportJarPath(BuildContext buildContext, BPackageSymbol importz, Path project) {
        // Get the jar paths
        PackageID id = importz.pkgID;
        
        // Skip ballerina and ballerinax
        if (id.orgName.value.equals("ballerina") || id.orgName.value.equals("ballerinax")) {
            return null;
        }
        // Look if it is a project module.
        if (ProjectDirs.isModuleExist(project, id.name.value)) {
            // If so fetch from project jar cache
            return buildContext.getJarPathFromTargetCache(id);
        } else {
            // If not fetch from home jar cache.
            return buildContext.getJarPathFromHomeCache(id);
        }
        // return the path
    }
    
    private static String extractJar(String jarFileName) throws NullPointerException {
        try (JarFile jar = new JarFile(jarFileName)) {
        
            java.util.Enumeration enumEntries = jar.entries();
            File destFile = File.createTempFile("temp-" + jarFileName, Long.toString(System.nanoTime()));
            if (!(destFile.delete())) {
                throw new BLangCompilerException("Could not delete temp file: " + destFile.getAbsolutePath());
            }
            if (!(destFile.mkdir())) {
                throw new BLangCompilerException("Could not create temp directory: " + destFile.getAbsolutePath());
            }
            while (enumEntries.hasMoreElements()) {
                JarEntry file = (JarEntry) enumEntries.nextElement();
                if (file.getName().contains(ProjectDirConstants.BALO_PLATFORM_LIB_DIR_NAME)) {
                    File f = new File(destFile.getPath() + File.separator + file.getName());
                    if (file.isDirectory()) { // if its a directory, create it
                        if (f.mkdir()) {
                            continue;
                        }
                    }
                    // get the input stream
                    try (InputStream is = jar.getInputStream(file); FileOutputStream fos = new FileOutputStream(f)) {
                        while (is.available() > 0) {  // write contents of 'is' to 'fos'
                            fos.write(is.read());
                        }
                    }
                }
            }
            return destFile.getPath();
        } catch (IOException e) {
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
