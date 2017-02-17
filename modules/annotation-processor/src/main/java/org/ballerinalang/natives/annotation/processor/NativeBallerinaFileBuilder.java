/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.natives.annotation.processor;

import org.ballerinalang.natives.annotation.processor.holders.PackageHolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builder class to generate ballerina files for the native APIs.
 */
public class NativeBallerinaFileBuilder {

    private static final PrintStream ERROR = System.err;
    private Map<String, PackageHolder> nativePackages;
    private String targetDirectory;
    private String balSourceDir;

    public NativeBallerinaFileBuilder(String srcDir, String targetDir) {
        this.balSourceDir = srcDir;
        this.targetDirectory = targetDir;
        this.nativePackages = new HashMap<String, PackageHolder>();
    }
    
    /**
     * Add the package map to the builder.
     * 
     * @param nativePackages Packages map
     */
    public void addNativePackages(Map<String, PackageHolder> nativePackages) {
        this.nativePackages = nativePackages;
    }
    

    /**
     * Create directories for native packages and write natives.bal files.
     */
    public void build() {
        // writes natives.bal files for all native Java constructs.
        nativePackages.forEach((k, v) -> {
            String directory = Arrays.asList(k.split("\\.")).stream().collect(Collectors.joining(File.separator));
            try {
                Files.createDirectories(Paths.get(targetDirectory, directory));
                try (BufferedWriter writer =
                        Files.newBufferedWriter(Paths.get(targetDirectory, directory, "natives.bal"))) {
                    writer.write(v.toString());
                }
            } catch (IOException e) {
                ERROR.println("couldn't create native files for [directory] " + directory + ". cause: " + e);
            }
        });

        // writes all native code defined in Ballerina also to the same targetDirectory
        try {
            Path source = Paths.get(balSourceDir);
            Path target = Paths.get(targetDirectory);
            Files.walkFileTree(source, new BallerinaFileVisitor(source, target));
        } catch (IOException e) {
            ERROR.println("failed to move native ballerina files. cause: " + e);
        }
    }

    /**
     * Visits ballerina package folders and files and copy them to target directory.
     */
    static class BallerinaFileVisitor extends SimpleFileVisitor<Path> {
        Path source;
        Path target;

        public BallerinaFileVisitor(Path aSource, Path aTarget) {
            this.source = aSource;
            this.target = aTarget;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetdir = target.resolve(source.relativize(dir));
            try {
                Files.copy(dir, targetdir);
            } catch (FileAlreadyExistsException e) {
                if (!Files.isDirectory(targetdir)) {
                    throw e;
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, target.resolve(source.relativize(file)));
            return FileVisitResult.CONTINUE;
        }
    }
}
