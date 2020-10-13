/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.stdlib.utils;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class providing cli tool to convert module to package
 *
 * @since 2.0.0
 */
public class ModuleToPackage {

    static final String BALLERINA_TOML = "Ballerina.toml";
    static final String SOURCE_DIR = "src";
    static final String MODULES_DIR = "modules";
    static Path projectDir;
    static Path oldProjectPath;

    public static void main(String[] args) throws IOException {
        oldProjectPath = Paths.get(args[0]);
        Path outputPath = Paths.get(args[1]);
        projectDir = outputPath;

        String defaultModule = null;
        if (args.length > 2) {
            defaultModule = args[2];
        }

        if (!Files.exists(oldProjectPath)) {
            throw new RuntimeException("Invalid project path");
        }
        // Create new directory if not exists
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }
        // Use the module name as project directory name
        List<Path> modules = Files.walk(oldProjectPath.resolve(SOURCE_DIR), 1)
                    .filter(file -> !SOURCE_DIR.equals(file.getFileName().toString()))
                    .filter(file -> Files.isDirectory(file))
                    .map(Path::getFileName)
                    .collect(Collectors.toList());

        // Identify default module
        if (modules.size() > 1) {
            if (defaultModule == null) {
                throw new RuntimeException("More than one module in the project. Please provide a default module.");
            }
        } else {
            defaultModule = modules.get(0).toString();
        }

        projectDir = projectDir.resolve(defaultModule);

        // Clear if directory exists
        if (Files.exists(projectDir)) {
            Files.walk(projectDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        Files.createDirectories(projectDir);
        createNewToml(defaultModule);
        copyModule(oldProjectPath.resolve(SOURCE_DIR).resolve(defaultModule), projectDir);
        //Move default module to project
    }

    private static void createNewToml(String moduleName) throws IOException {
        // Copy Ballerina.toml
        String oldToml = new String (Files.readAllBytes(oldProjectPath.resolve(BALLERINA_TOML)));
        Toml toml = new Toml().read(oldToml);

        Path newToml = projectDir.resolve(BALLERINA_TOML);
        // Create a new toml file
        Files.createFile(newToml);

        TomlWriter tomlWriter = new TomlWriter();

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> pkg = new HashMap<String, Object>();
        pkg.put("name", moduleName);
        pkg.put("org", toml.getTable("project").getString("org-name"));
        pkg.put("version", toml.getTable("project").getString("version"));
        map.put("package", pkg);
        String tomlString = tomlWriter.write(map);
        Files.write(newToml, tomlString.getBytes());
    }


    private static void copyModule(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
