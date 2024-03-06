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
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.toml.api.Toml;
import io.ballerina.toml.validator.TomlValidator;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.ballerinalang.formatter.core.options.FormatSection;
import org.ballerinalang.formatter.core.options.WrappingFormattingOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Class that contains the util functions used by the formatting tree modifier.
 */
public class FormatterUtils {
    static final String NEWLINE_SYMBOL = System.getProperty("line.separator");
    static final String FORMAT_FILE_FIELD = "configPath";
    static final String FORMAT_OPTION_FILE_EXT = ".toml";
    static final String DEFAULT_FORMAT_OPTION_FILE = "Format.toml";
    static final String TARGET_DIR = "target";
    static final String FORMAT = "format";
    static final String FORMAT_TOML_SCHEMA = "format-toml-schema.json";
    private static final PrintStream errStream = System.err;

    public static final ResourceBundle DEFAULTS = ResourceBundle.getBundle("formatter", Locale.getDefault());

    private FormatterUtils() {
    }

    public static boolean getDefaultBoolean(FormatSection section, String key) {
        return Boolean.parseBoolean(getDefaultString(section, key));
    }

    public static int getDefaultInt(FormatSection section, String key) {
        return Integer.parseInt(getDefaultString(section, key));
    }

    public static String getDefaultString(FormatSection section, String key) {
        return DEFAULTS.getString(section.getStringValue() + "." + key);
    }

    public static Object loadFormatSection(PackageManifest manifest) {
        return manifest.getValue(FORMAT);
    }

    public static String getFormattingFilePath(Object formatSection, String root) {
        if (formatSection != null) {
            Object path = ((Map<String, Object>) formatSection).get(FORMAT_FILE_FIELD);
            if (path != null) {
                String str = path.toString();
                if (str.endsWith(FORMAT_OPTION_FILE_EXT)) {
                    return str;
                }
                return Path.of(str, DEFAULT_FORMAT_OPTION_FILE).toString();
            }
        }

        Path defaultFile = Path.of(root, DEFAULT_FORMAT_OPTION_FILE);
        return Files.exists(defaultFile) ? defaultFile.toString() : null;
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

    private static boolean isLocalFile(String path) {
        return new File(path).exists();
    }

    static String readRemoteFormatFile(Path root, String fileUrl) throws FormatterException {
        Path cachePath = root.resolve(TARGET_DIR).resolve(FORMAT).resolve(DEFAULT_FORMAT_OPTION_FILE);
        if (Files.exists(cachePath)) {
            try {
                return Files.readString(cachePath, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new FormatterException("Failed to read cached formatting configuration file");
            }
        }

        StringBuilder fileContent = new StringBuilder();
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect();
                throw new FormatterException("Failed to retrieve remote file. HTTP response code: " + responseCode);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.append(line).append(NEWLINE_SYMBOL);
                }
            }
            connection.disconnect();
        } catch (IOException e) {
            throw new FormatterException("Failed to retrieve formatting configuration file");
        }
        cacheRemoteConfigurationFile(root, fileContent.toString());

        return fileContent.toString();
    }

    private static void cacheRemoteConfigurationFile(Path root, String content) throws FormatterException {
        Path targetDir = root.resolve(TARGET_DIR);
        if (!Files.exists(targetDir)) {
            return;
        }
        Path formatDir = targetDir.resolve(FORMAT);
        if (!Files.exists(formatDir)) {
            try {
                Files.createDirectories(formatDir);
            } catch (IOException e) {
                throw new FormatterException("Failed to create format configuration cache directory");
            }
        }
        String filePath = formatDir.resolve(DEFAULT_FORMAT_OPTION_FILE).toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            writer.write(content);
        } catch (IOException e) {
            throw new FormatterException("Failed to write format configuration cache file");
        }
    }

    public static Map<String, Object> parseConfigurationToml(TomlDocument document) throws FormatterException {
        Toml toml = document.toml();
        if (toml.rootNode().entries().isEmpty()) {
            return new HashMap<>();
        }
        TomlValidator formatTomlValidator;
        try {
            formatTomlValidator = new TomlValidator(Schema.from(FileUtils.readFileAsString(FORMAT_TOML_SCHEMA)));
        } catch (IOException e) {
            throw new ProjectException("Failed to read the Format.toml validator schema file.");
        }
        formatTomlValidator.validate(toml);

        List<Diagnostic> diagnostics = toml.diagnostics();
        boolean hasErrors = false;
        for (Diagnostic d: diagnostics) {
            if (d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR)) {
                errStream.println(d.message());
                hasErrors = true;
            }
        }
        if (hasErrors) {
            throw new FormatterException("Invalid Format.toml file");
        }
        return toml.toMap();
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

    /**
     * Swap leading minutiae of the first import in original code and the first import when sorted.
     *
     * @param firstImportNode First ImportDeclarationNode in original code
     * @param importNodes     Sorted formatted ImportDeclarationNode nodes
     */
    static void swapLeadingMinutiae(ImportDeclarationNode firstImportNode, List<ImportDeclarationNode> importNodes) {
        int prevFirstImportIndex = -1;
        String firstImportOrgName = firstImportNode.orgName().map(ImportOrgNameNode::toSourceCode).orElse("");
        String firstImportModuleName = getImportModuleName(firstImportNode);
        for (int i = 0; i < importNodes.size(); i++) {
            if (doesImportMatch(firstImportOrgName, firstImportModuleName, importNodes.get(i))) {
                prevFirstImportIndex = i;
                break;
            }
        }
        if (prevFirstImportIndex == 0) {
            return;
        }

        // remove comments from the previous first import
        ImportDeclarationNode prevFirstImportNode = importNodes.get(prevFirstImportIndex);
        MinutiaeList prevFirstLeadingMinutiae = prevFirstImportNode.leadingMinutiae();

        MinutiaeList prevFirstNewLeadingMinutiae = NodeFactory.createEmptyMinutiaeList();
        Minutiae prevFirstMinutiae = prevFirstLeadingMinutiae.get(0);
        if (prevFirstMinutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
            // if the prevFirstImport now is the first of a group of imports, handle the added leading newline
            prevFirstNewLeadingMinutiae = prevFirstNewLeadingMinutiae.add(prevFirstMinutiae);
            prevFirstLeadingMinutiae = prevFirstLeadingMinutiae.remove(0);
        }

        importNodes.set(prevFirstImportIndex,
                modifyImportDeclLeadingMinutiae(prevFirstImportNode, prevFirstNewLeadingMinutiae));

        if (!hasEmptyLineAtEnd(prevFirstLeadingMinutiae)) {
            // adds a newline after prevFirstImport's leading minutiae if not present
            prevFirstLeadingMinutiae =
                    prevFirstLeadingMinutiae.add(NodeFactory.createEndOfLineMinutiae(System.lineSeparator()));
        }

        ImportDeclarationNode newFirstImportNode = importNodes.get(0);
        MinutiaeList newFirstLeadingMinutiae = newFirstImportNode.importKeyword().leadingMinutiae();
        for (int i = 0; i < newFirstLeadingMinutiae.size(); i++) {
            Minutiae minutiae = newFirstLeadingMinutiae.get(i);
            if (i == 0 && minutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
                // since we added a new line after prevFirstImport's leading minutiae we can skip th newline here
                continue;
            }
            prevFirstLeadingMinutiae = prevFirstLeadingMinutiae.add(minutiae);
        }

        importNodes.set(0, modifyImportDeclLeadingMinutiae(newFirstImportNode, prevFirstLeadingMinutiae));
    }

    private static ImportDeclarationNode modifyImportDeclLeadingMinutiae(ImportDeclarationNode importDecl,
                                                                         MinutiaeList leadingMinutiae) {
        Token importToken = importDecl.importKeyword();
        Token modifiedImportToken = importToken.modify(leadingMinutiae, importToken.trailingMinutiae());
        return importDecl.modify().withImportKeyword(modifiedImportToken).apply();
    }

    private static boolean doesImportMatch(String orgName, String moduleName, ImportDeclarationNode importDeclNode) {
        String importDeclOrgName = importDeclNode.orgName().map(ImportOrgNameNode::toSourceCode).orElse("");
        return orgName.equals(importDeclOrgName) && moduleName.equals(getImportModuleName(importDeclNode));
    }

    private static String getImportModuleName(ImportDeclarationNode importDeclarationNode) {
        return importDeclarationNode.moduleName().stream().map(Node::toSourceCode).collect(Collectors.joining("."));
    }

    private static boolean hasEmptyLineAtEnd(MinutiaeList minutiaeList) {
        int size = minutiaeList.size();
        return minutiaeList.get(size - 1).kind() == SyntaxKind.END_OF_LINE_MINUTIAE &&
                minutiaeList.get(size - 2).kind() == SyntaxKind.END_OF_LINE_MINUTIAE;
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
