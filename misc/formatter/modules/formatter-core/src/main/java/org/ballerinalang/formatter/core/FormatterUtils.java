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
import io.ballerina.projects.Module;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.toml.api.Toml;
import io.ballerina.toml.validator.TomlValidator;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.ballerinalang.formatter.core.options.FormatSection;
import org.ballerinalang.formatter.core.options.FormattingOptions;
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
import java.util.Optional;
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

    /**
     * Build the formatting options given the build project.
     *
     * @param project the build project
     * @return the FormattingOptions
     */
    public static FormattingOptions buildFormattingOptions(BuildProject project) throws FormatterException {
        return FormattingOptions.builder()
                .build(project.sourceRoot(), loadFormatSection(project.currentPackage().manifest()));
    }

    /**
     * Checks whether the module is a build project.
     *
     * @param module Current module
     * @return whether module is a build project
     */
    public static boolean isBuildProject(Optional<Module> module) {
        return module.isPresent() && module.get().project().kind() != ProjectKind.SINGLE_FILE_PROJECT;
    }

    /**
     * Retrieves the default boolean value for the format option in the given format section.
     *
     * @param section the format section
     * @param key     the format option
     * @return the default boolean value
     */
    public static boolean getDefaultBoolean(FormatSection section, String key) {
        return Boolean.parseBoolean(getDefaultString(section, key));
    }

    /**
     * Retrieves the default integer value for the format option in the given format section.
     *
     * @param section the format section
     * @param key     the format option
     * @return the default integer value
     */
    public static int getDefaultInt(FormatSection section, String key) {
        return Integer.parseInt(getDefaultString(section, key));
    }

    /**
     * Retrieves the default string value for the format option in the given format section.
     *
     * @param section the format section
     * @param key     the format option
     * @return the default string value
     */
    public static String getDefaultString(FormatSection section, String key) {
        return DEFAULTS.getString(section.getStringValue() + "." + key);
    }

    /**
     * Loads the format section in the Ballerina.toml .
     *
     * @param manifest the package manifest
     * @return the format section
     */
    public static Object loadFormatSection(PackageManifest manifest) {
        return manifest.getValue(FORMAT);
    }

    /**
     * Retrieves the formatting file path.
     *
     * @param formatSection the format section loaded from Ballerina.toml
     * @param root          the root path of the project
     * @return the formatting file path
     */
    public static Optional<String> getFormattingFilePath(Object formatSection, String root) {
        if (formatSection != null) {
            Object path = ((Map<String, Object>) formatSection).get(FORMAT_FILE_FIELD);
            if (path != null) {
                String str = path.toString();
                if (str.endsWith(FORMAT_OPTION_FILE_EXT)) {
                    return Optional.of(str);
                }
                return Optional.of(Path.of(str, DEFAULT_FORMAT_OPTION_FILE).toString());
            }
        }

        Path defaultFile = Path.of(root, DEFAULT_FORMAT_OPTION_FILE);
        return Files.exists(defaultFile) ? Optional.of(defaultFile.toString()) : Optional.empty();
    }

    /**
     * Retrieves the formatting configurations.
     *
     * @param root the root path of the project
     * @param configurationFilePath the path of configuration file
     * @return the formatting configurations
     * @throws FormatterException if the file resolution fails
     */
    public static Map<String, Object> getFormattingConfigurations(Path root, String configurationFilePath)
            throws FormatterException {
        String content;
        Path path = Path.of(configurationFilePath);
        Path absPath = path.isAbsolute() || !root.isAbsolute() ? path : root.resolve(configurationFilePath);
        if (isLocalFile(absPath)) {
            try {
                content = Files.readString(absPath, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new FormatterException("Failed to retrieve local formatting configuration file");
            }
        } else {
            content = readRemoteFormatFile(root, configurationFilePath);
        }

        return parseConfigurationToml(TomlDocument.from(configurationFilePath, content));
    }

    private static boolean isLocalFile(Path path) {
        return new File(path.toString()).exists();
    }

    private static String readRemoteFormatFile(Path root, String fileUrl) throws FormatterException {
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

    /**
     * Retrieves the formatting configurations.
     *
     * @param document formatting configuration toml file
     * @return the formatting options
     * @throws FormatterException if the configuration file validation fails
     */
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
        for (Diagnostic diagnostic: diagnostics) {
            if (diagnostic.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR)) {
                errStream.println(diagnostic.message());
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
