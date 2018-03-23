/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.packerina.init;

import org.ballerinalang.packerina.init.models.SrcFile;
import org.ballerinalang.toml.model.Manifest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Handler for creating ballerina project files.
 */
public class InitHandler {
    /**
     * Creates the project files.
     * @param projectPath The output path.
     * @param manifest The manifest for Ballerina.toml.
     * @param srcFiles The source files.
     */
    public static void initialize(Path projectPath, Manifest manifest, List<SrcFile> srcFiles) throws IOException {
        createBallerinaToml(projectPath, manifest);
        createBallerinaCacheFile(projectPath);
        createSrcFolder(projectPath, srcFiles);
        createIgnoreFiles(projectPath);
    }
    
    /**
     * Create the Ballerina.toml manifest file.
     * @param projectPath The output path.
     * @param manifest The manifest for the file.
     * @throws IOException If file write exception occurs.
     */
    private static void createBallerinaToml(Path projectPath, Manifest manifest) throws IOException {
        if (null != manifest) {
            Path tomlPath = Paths.get(projectPath.toString() + File.separator + "Ballerina.toml");
            if (!Files.exists(tomlPath)) {
                // Creating main function file.
                Files.createFile(tomlPath);
                // Writing content.
                writeContent(tomlPath, getManifestContent(manifest));
            }
        }
    }
    
    /**
     * Create the .ballerina/ cache folder.
     * @param projectPath The output path.
     * @throws IOException If file write exception occurs.
     */
    private static void createBallerinaCacheFile(Path projectPath) throws IOException {
        Path cacheFolder = Paths.get(projectPath.toString() + File.separator + ".ballerina");
        if (!Files.exists(cacheFolder)) {
            // Creating main function file.
            Files.createDirectory(cacheFolder);
        }
    }
    
    /**
     * Create src/ folder.
     * @param projectPath The output path.
     * @param srcFiles The source files to be created.
     * @throws IOException If file write exception occurs.
     */
    private static void createSrcFolder(Path projectPath, List<SrcFile> srcFiles) throws IOException {
        if (null != srcFiles && srcFiles.size() > 0) {
            for (SrcFile srcFile : srcFiles) {
                if (srcFile.getSrcFileType() == SrcFile.SrcFileType.MAIN) {
                    // Creating main function file.
                    Path srcFilePath = Paths.get(projectPath.toString() + File.separator + srcFile.getName());
                    if (!Files.exists(srcFilePath)) {
                        Files.createFile(srcFilePath);
                        // Writing content.
                        writeContent(srcFilePath, srcFile.getContent());
                    }
                } else if (srcFile.getSrcFileType() == SrcFile.SrcFileType.SERVICE) {
                    // Creating package directory.
                    Path packagePath = Paths.get(projectPath.toString() + File.separator + srcFile.getName());
                    Files.createDirectory(packagePath);
                    
                    // Creating service file.
                    Path servicesBalFile = Paths.get(packagePath.toString() + File.separator + "services.bal");
                    if (!Files.exists(servicesBalFile)) {
                        Files.createFile(servicesBalFile);
    
                        // Writing content.
                        writeContent(servicesBalFile, srcFile.getContent());
                    }
                }
            }
        }
    }
    
    /**
     * Creates the .gitignore file.
     * @param projectPath The output path.
     * @throws IOException If file write exception occurs.
     */
    private static void createIgnoreFiles(Path projectPath) throws IOException {
        Path gitIgnorePath = Paths.get(projectPath.toString() + File.separator + ".gitignore");
        Path svnIgnorePath = Paths.get(projectPath.toString() + File.separator + ".svnignore");
        if (Files.exists(gitIgnorePath)) {
            writeIgnoreFileContent(gitIgnorePath);
        } else if (Files.exists(svnIgnorePath)) {
            writeIgnoreFileContent(svnIgnorePath);
        } else {
            // Creating ignore files.
            Files.createFile(gitIgnorePath);
            Files.createFile(svnIgnorePath);
            writeIgnoreFileContent(gitIgnorePath);
            writeIgnoreFileContent(svnIgnorePath);
        }
    }
    
    /**
     * Add ignore content to file.
     * @param ignoreFile The ignore file path.
     * @throws IOException If file write exception occurs.
     */
    private static void writeIgnoreFileContent(Path ignoreFile) throws IOException {
        String ignoreFileContent = ".ballerina/\ntarget/\n";
        String content = new String(Files.readAllBytes(ignoreFile), Charset.defaultCharset());
        if (!content.contains(ignoreFileContent)) {
            // Writing content.
            writeContent(ignoreFile, ignoreFileContent);
        }
    }
    
    /**
     * Write content to a file.
     * @param file The file.
     * @param content The content.
     * @throws IOException If file write exception occurs.
     */
    private static void writeContent(Path file, String content) throws IOException {
        byte data[] = content.getBytes(Charset.defaultCharset());
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(file, CREATE, APPEND))) {
            out.write(data, 0, data.length);
        }
    }
    
    /**
     * Generate the manifest file content for Ballerina.toml.
     * @param manifest The manifest model.
     * @return Manifest content.
     */
    private static String getManifestContent(Manifest manifest) {
        StringBuilder manifestContent = new StringBuilder("[project]");
        manifestContent.append("\n");
        if (null != manifest.getName() && !manifest.getName().isEmpty()) {
            manifestContent.append("org-name = \"");
            manifestContent.append(manifest.getName());
            manifestContent.append("\"\n\n");
        }
        
        if (null != manifest.getVersion() && !manifest.getVersion().isEmpty()) {
            manifestContent.append("version = \"");
            manifestContent.append(manifest.getVersion());
            manifestContent.append("\"\n\n");
        }
    
        if (null != manifest.getAuthors() && !manifest.getAuthors().isEmpty()) {
            manifestContent.append("authors = [");
            manifestContent.append(String.join(", ", "\"" + manifest.getAuthors() + "\""));
            manifestContent.append("]\n\n");
        }
    
        if (null != manifest.getKeywords() && !manifest.getKeywords().isEmpty()) {
            manifestContent.append("keywords = [");
            manifestContent.append(String.join(", ", "\"" + manifest.getKeywords() + "\""));
            manifestContent.append("]\n\n");
        }
        
        // TODO: Implement the rest of the fields.
        
        return manifestContent.toString();
    }
}
