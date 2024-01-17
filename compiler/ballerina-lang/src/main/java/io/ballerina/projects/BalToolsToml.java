/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects;

import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.toml.semantic.ast.TomlTableNode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static io.ballerina.projects.util.ProjectConstants.BAL_TOOLS_TOML;

/**
 * Represents the 'bal-tools.toml' file `.ballerina` repository.
 *
 * @since 2201.6.0
 */
public class BalToolsToml {
    private final Path balToolsTomlPath;
    private TomlDocumentContext balToolsTomlContext;

    private BalToolsToml(Path balToolsTomlPath) {
        String content = read(balToolsTomlPath);
        this.balToolsTomlContext = TomlDocumentContext.from(TomlDocument.from(BAL_TOOLS_TOML, content));
        this.balToolsTomlPath = balToolsTomlPath;
    }

    public static BalToolsToml from(Path balToolsTomlPath) {
        return new BalToolsToml(balToolsTomlPath);
    }

    private static String read(Path balToolsTomlPath) {
        StringBuilder content = new StringBuilder();
        if (!balToolsTomlPath.toFile().exists()) {
            try {
                Path parentDirectory = balToolsTomlPath.getParent();
                if (parentDirectory != null && !parentDirectory.toFile().exists()) {
                    Files.createDirectories(parentDirectory);
                }
                Files.createFile(balToolsTomlPath);
            } catch (IOException e) {
                throw new RuntimeException("Error while creating bal-tools.toml :" + e);
            }
        }
        try (BufferedReader reader = new BufferedReader(
                new FileReader(balToolsTomlPath.toString(), Charset.defaultCharset()))) {
            String line = reader.readLine();
            while (line != null) {
                content.append(line).append("\n");
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading bal-tools.toml :" + e);
        }
        return String.valueOf(content);
    }

    TomlDocumentContext ballerinaTomlContext() {
        return balToolsTomlContext;
    }

    public String name() {
        return ProjectConstants.BAL_TOOLS_TOML;
    }

    public TomlTableNode tomlAstNode() {
        return tomlDocument().toml().rootNode();
    }

    public TomlDocument tomlDocument() {
        return this.balToolsTomlContext.tomlDocument();
    }

    public void modify(BalToolsManifest balToolsManifest) {
        String updatedContent = generateContent(balToolsManifest);
        this.balToolsTomlContext = TomlDocumentContext.from(TomlDocument.from(BAL_TOOLS_TOML, updatedContent));
        write(updatedContent);
    }

    private String generateContent(BalToolsManifest balToolsManifest) {
        StringBuilder content = new StringBuilder();
        content.append(getAutoGenCode());
        for (Map.Entry<String, Map<String, Map<String, BalToolsManifest.Tool>>> toolEntry : balToolsManifest.tools()
                .entrySet()) {
            for (Map.Entry<String, Map<String, BalToolsManifest.Tool>> toolVersions : toolEntry.getValue().entrySet()) {
                for (Map.Entry<String, BalToolsManifest.Tool> tool : toolVersions.getValue().entrySet()) {
                    content.append("[[tool]]\n");
                    content.append("id = \"").append(tool.getValue().id()).append("\"\n");
                    content.append("org = \"").append(tool.getValue().org()).append("\"\n");
                    content.append("name = \"").append(tool.getValue().name()).append("\"\n");
                    content.append("version = \"").append(tool.getValue().version()).append("\"\n");
                    content.append("active = ").append(tool.getValue().active()).append("\n");
                    if (tool.getValue().repository() != null) {
                        content.append("repository = \"").append(tool.getValue().repository()).append("\"\n");
                    }
                    content.append("\n");
                }
            }
        }



        return String.valueOf(content);
    }

    private void write(String content) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(String.valueOf(balToolsTomlPath), Charset.defaultCharset()))) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Error while updating bal-tools.toml :" + e);
        }
    }

    private String getAutoGenCode() {
        return "# AUTO-GENERATED FILE. DO NOT MODIFY.\n" +
                "\n" +
                "# This file is auto-generated by Ballerina for managing tool commands.\n" +
                "# It should not be modified by hand.\n" +
                "\n";
    }
}
