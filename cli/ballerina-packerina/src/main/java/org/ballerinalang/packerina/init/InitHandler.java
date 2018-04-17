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
     *
     * @param projectPath The output path.
     * @param manifest    The manifest for Ballerina.toml.
     * @param srcFiles    The source files.
     */
    public static void initialize(Path projectPath, Manifest manifest, List<SrcFile> srcFiles) throws IOException {
        createBallerinaToml(projectPath, manifest);
        createBallerinaCacheFile(projectPath);
        createSrcFolder(projectPath, srcFiles);

        String ignoreFileContent = "target/\n";
        createIgnoreFiles(projectPath, ignoreFileContent);
    }

    /**
     * Create the Ballerina.toml manifest file.
     *
     * @param projectPath The output path.
     * @param manifest    The manifest for the file.
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
     *
     * @param projectPath The output path.
     * @throws IOException If file write exception occurs.
     */
    private static void createBallerinaCacheFile(Path projectPath) throws IOException {
        Path cacheFolder = Paths.get(projectPath.toString() + File.separator + ".ballerina");
        if (!Files.exists(cacheFolder)) {
            // Creating main function file.
            Files.createDirectory(cacheFolder);
        }
        String ignoreFileContent = "*\n!.gitignore\n!.svnignore";
        createIgnoreFiles(cacheFolder, ignoreFileContent);
    }

    /**
     * Create src/ folder.
     *
     * @param projectPath The output path.
     * @param srcFiles    The source files to be created.
     * @throws IOException If file write exception occurs.
     */
    private static void createSrcFolder(Path projectPath, List<SrcFile> srcFiles) throws IOException {
        final String testDirName = "tests";
        if (null != srcFiles && srcFiles.size() > 0) {
            for (SrcFile srcFile : srcFiles) {
                Path packagePath = projectPath.resolve(srcFile.getName());
                Path testDirPath = packagePath.resolve(testDirName);
                if (!Files.exists(packagePath)) {
                    Files.createDirectory(packagePath);
                }
                if (!Files.exists(testDirPath)) {
                    Files.createDirectory(testDirPath);
                }

                Path srcFilePath = packagePath.resolve(srcFile.getSrcFileType().getFileName());
                Path testFilePath = testDirPath.resolve(srcFile.getTestFileName());
                if (!Files.exists(srcFilePath)) {
                    Files.createFile(srcFilePath);
                    writeContent(srcFilePath, srcFile.getContent());
                }
                if (!Files.exists(testFilePath)) {
                    Files.createFile(testFilePath);
                    writeContent(testFilePath, srcFile.getTestContent());
                }
            }
        }
    }
    
    /**
     * Creates the .gitignore file.
     *
     * @param projectPath       The output path.
     * @param ignoreFileContent content to be ignored
     * @throws IOException If file write exception occurs.
     */
    private static void createIgnoreFiles(Path projectPath, String ignoreFileContent) throws IOException {
        Path gitIgnorePath = projectPath.resolve(".gitignore");
        Path svnIgnorePath = projectPath.resolve(".svnignore");
        if (Files.exists(gitIgnorePath)) {
            writeIgnoreFileContent(gitIgnorePath, ignoreFileContent);
        } else if (Files.exists(svnIgnorePath)) {
            writeIgnoreFileContent(svnIgnorePath, ignoreFileContent);
        } else {
            // Creating ignore files.
            Files.createFile(gitIgnorePath);
            Files.createFile(svnIgnorePath);
            writeIgnoreFileContent(gitIgnorePath, ignoreFileContent);
            writeIgnoreFileContent(svnIgnorePath, ignoreFileContent);
        }
    }
    
    /**
     * Add ignore content to file.
     *
     * @param ignoreFile        The ignore file path.
     * @param ignoreFileContent content to be ignored
     * @throws IOException If file write exception occurs.
     */
    private static void writeIgnoreFileContent(Path ignoreFile, String ignoreFileContent) throws IOException {
        String content = new String(Files.readAllBytes(ignoreFile), Charset.defaultCharset());
        if (!content.contains(ignoreFileContent)) {
            // Writing content.
            writeContent(ignoreFile, ignoreFileContent);
        }
    }
    
    /**
     * Write content to a file.
     *
     * @param file    The file.
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
     *
     * @param manifest The manifest model.
     * @return Manifest content.
     */
    private static String getManifestContent(Manifest manifest) {
        StringBuilder manifestContent = new StringBuilder("[project]");
        manifestContent.append("\n");
        if (null != manifest.getName() && !manifest.getName().isEmpty()) {
            manifestContent.append("org-name = \"");
            manifestContent.append(manifest.getName());
            manifestContent.append("\"\n");
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
