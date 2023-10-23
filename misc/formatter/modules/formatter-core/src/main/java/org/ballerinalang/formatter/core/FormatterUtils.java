/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.formatter.core;

import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.TomlDocument;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.ballerinalang.formatter.core.options.WrappingFormattingOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class that contains the util functions used by the formatting tree modifier.
 */
public class FormatterUtils {

    private FormatterUtils() {

    }

    static final String NEWLINE_SYMBOL = System.getProperty("line.separator");

    public static String getFormattingFilePath(Object data, String root) {
        if (data instanceof Map) {
            Object path = ((Map<String, Object>) data).get("configPath");
            if (path != null) {
                String str = path.toString();
                if (str.endsWith(".toml")) {
                    return str;
                }
                return Path.of(str, "Format.toml").toString();
            }
        }
        File directory = new File(root);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().equals("Format.toml")) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }

    public static Map<String, Object> getFormattingConfigurations(Path root, String path) throws FormatterException {
        String content;
        if (isLocalFile(path)) {
            try {
                content = Files.readString(Path.of(path), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new FormatterException("Failed to retrieve local formatting configuration file");
            }
        } else {
            content = readRemoteFormatFile(root, path);
        }

        return parseConfigurationToml(TomlDocument.from(path, content));
    }

    public static boolean isLocalFile(String path) {
        return new File(path).exists();
    }

    static String readRemoteFormatFile(Path root, String fileUrl) throws FormatterException {
        StringBuilder fileContent = new StringBuilder();
        Path cachePath = root.resolve("target").resolve("format").resolve("Format.toml");

        if (Files.exists(cachePath)) {
            try {
                return Files.readString(cachePath, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new FormatterException("Failed to read cached formatting configuration file");
            }
        }

        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        fileContent.append(line).append(NEWLINE_SYMBOL);
                    }
                }
            } else {
                throw new FormatterException("Failed to retrieve remote file. HTTP response code: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            throw new FormatterException("Failed to retrieve remote formatting configuration file");
        }
        cacheRemoteConfigurationFile(root, fileContent.toString());

        return fileContent.toString();
    }

    private static void cacheRemoteConfigurationFile(Path root, String content) throws FormatterException {
        if (Files.exists(root.resolve("target"))) {
            if (!Files.exists(root.resolve("target").resolve("format"))) {
                try {
                    Files.createDirectories(root.resolve("target").resolve("format"));
                } catch (IOException e) {
                    throw new FormatterException("Failed to create format configuration cache directory");
                }
            }
            String filePath = root.resolve("target").resolve("format").resolve("Format.toml")
                    .toString();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
                writer.write(content);
            } catch (IOException e) {
                throw new FormatterException("Failed to write format configuration cache file");
            }
        }
    }

    public static Map<String, Object> parseConfigurationToml(TomlDocument document) {
        TomlTableNode tomlAstNode = document.toml().rootNode();
        Map<String, Object> formatConfigs = new HashMap<>();
        if (!tomlAstNode.entries().isEmpty()) {
            Map<String, Object> tomlMap = document.toml().toMap();
            for (Map.Entry<String, Object> entry : tomlMap.entrySet()) {
                formatConfigs.put(entry.getKey(), entry.getValue());
            }
        }
        return formatConfigs;
    }

    static boolean isInlineRange(Node node, LineRange lineRange) {
        if (lineRange == null) {
            return true;
        }

        int nodeStartLine = node.lineRange().startLine().line();
        int nodeStartColumn = node.lineRange().startLine().offset();
        int nodeEndLine = node.lineRange().endLine().line();
        int nodeEndColumn = node.lineRange().endLine().offset();

        int rangeStartLine = lineRange.startLine().line();
        int rangeStartColumn = lineRange.startLine().offset();
        int rangeEndLine = lineRange.endLine().line();
        int rangeEndColumn = lineRange.endLine().offset();

        // Node ends before the range
        if (nodeEndLine < rangeStartLine) {
            return false;
        } else if (nodeEndLine == rangeStartLine) {
            return nodeEndColumn > rangeStartColumn;
        }

        // Node starts after the range
        if (nodeStartLine > rangeEndLine) {
            return false;
        } else if (nodeStartLine == rangeEndLine) {
            return nodeStartColumn < rangeEndColumn;
        }

        return true;
    }

    /**
     * Sort ImportDeclaration nodes based on orgName and the moduleName in-place.
     *
     * @param importDeclarationNodes ImportDeclarations nodes
     */
    static void sortImportDeclarations(List<ImportDeclarationNode> importDeclarationNodes) {
        importDeclarationNodes.sort((node1, node2) -> new CompareToBuilder()
                .append(node1.orgName().isPresent() ? node1.orgName().get().orgName().text() : "",
                        node2.orgName().isPresent() ? node2.orgName().get().orgName().text() : "")
                .append(node1.moduleName().stream().map(node -> node.toString().trim()).collect(Collectors.joining()),
                        node2.moduleName().stream().map(node -> node.toString().trim()).collect(Collectors.joining()))
                .toComparison());
    }

    static int openBraceTrailingNLs(WrappingFormattingOptions options, Node node) {
        if (node.lineRange().startLine().line() != node.lineRange().endLine().line()) {
            return 1;
        }

        SyntaxKind parentKind = node.parent().kind();
        if (options.isSimpleBlocksInOneLine() && parentKind != SyntaxKind.METHOD_DECLARATION) {
            return 0;
        }
        return (options.isSimpleMethodsInOneLine() && parentKind == SyntaxKind.METHOD_DECLARATION) ? 0 : 1;
    }

    static int getConstDefWidth(ConstantDeclarationNode node) {
        int size = node.visibilityQualifier().isPresent() ? node.visibilityQualifier().get().text().length() : 0;
        size += node.constKeyword().text().length();
        size += node.typeDescriptor().isPresent() ? node.typeDescriptor().get().toSourceCode().length() : 0;
        size += node.variableName().text().length();
        return size;
    }
}
