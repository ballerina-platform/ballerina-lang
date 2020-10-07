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

package io.ballerina.toml.api;

import io.ballerina.toml.ast.TomlDiagnostic;
import io.ballerina.toml.ast.TomlKeyValueNode;
import io.ballerina.toml.ast.TomlNodeLocation;
import io.ballerina.toml.ast.TomlTableArrayNode;
import io.ballerina.toml.ast.TomlTableNode;
import io.ballerina.toml.ast.TomlTransformer;
import io.ballerina.toml.ast.TomlValidator;
import io.ballerina.toml.ast.TomlValueNode;
import io.ballerina.toml.ast.TopLevelNode;
import io.ballerina.toml.syntax.tree.ModulePartNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * API For Parsing Tom's Obvious, Minimal Language (TOML) file.
 *
 * @since 0.1.0
 */
public class Toml {

    private TomlTableNode rootNode;

    /**
     * Creates new Root TOML Node from AST.
     *
     * @param tomlTableNode AST representation of TOML Table.
     */
    private Toml(TomlTableNode tomlTableNode) {
        this.rootNode = tomlTableNode;
    }

    /**
     * Read TOML File using Path.
     *
     * @param path Path of the TOML file
     * @return TOML Object
     * @throws IOException if file is not accessible
     */
    public static Toml read(Path path) throws IOException {
        return read(Files.newBufferedReader(path));
    }

    /**
     * Read TOML File using InputStream.
     *
     * @param inputStream InputStream of the TOML file
     * @return TOML Object
     * @throws IOException if file is not accessible
     */
    public static Toml read(InputStream inputStream) throws IOException {
        return read(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    /**
     * Read TOML File using Reader.
     *
     * @param reader reader of the TOML file
     * @return TOML Object
     * @throws IOException if file is not accessible
     */
    public static Toml read(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder w = new StringBuilder();
        for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
            w.append(line).append('\n');
        }
        return read(w.toString());
    }

    /**
     * Parse TOML file using TOML String.
     *
     * @param content String representation of the TOML file content.
     * @return TOML Object
     */
    public static Toml read(String content) {
        TextDocument textDocument = TextDocuments.from(content);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        List<TomlDiagnostic> tomlDiagnostics = reportSyntaxDiagnostics(syntaxTree);
        TomlTransformer nodeTransformer = new TomlTransformer();
        TomlTableNode
                transformedTable = (TomlTableNode) nodeTransformer.transform((ModulePartNode) syntaxTree.rootNode());
        TomlValidator tomlValidator = new TomlValidator();
        tomlValidator.visit(transformedTable);
        transformedTable.setSyntacticalDiagnostics(tomlDiagnostics);
        tomlDiagnostics.addAll(transformedTable.collectSemanticDiagnostics());
        return new Toml(transformedTable);
    }

    private static List<TomlDiagnostic> reportSyntaxDiagnostics(SyntaxTree tree) {
        List<TomlDiagnostic> diagnostics = new ArrayList<>();
        for (Diagnostic syntaxDiagnostic : tree.diagnostics()) {
            TomlNodeLocation tomlNodeLocation = new TomlNodeLocation(syntaxDiagnostic.location().lineRange(),
                    syntaxDiagnostic.location().textRange());
            TomlDiagnostic tomlDiagnostic =
                    new TomlDiagnostic(tomlNodeLocation, syntaxDiagnostic.diagnosticInfo(), syntaxDiagnostic.message());
            diagnostics.add(tomlDiagnostic);
        }
        return diagnostics;
    }

    /**
     * Get value from a key of a Key Value pair.
     *
     * @param key key name
     * @param <T> Type of the AST Value Node
     * @return AST Value Node
     */
    public <T extends TomlValueNode> T get(String key) {
        TomlKeyValueNode tomlKeyValueNode = (TomlKeyValueNode) rootNode.children().get(key);
        if (tomlKeyValueNode != null) {
            TomlValueNode value = tomlKeyValueNode.value();
            if (value != null) {
                return (T) value;
            }
        }
        return null;
    }

    /**
     * Get Child Table from TOML.
     *
     * @param key Identifier of the table.
     * @return Child Table
     */
    public Toml getTable(String key) {
        TopLevelNode topLevelNode = rootNode.children().get(key);
        if (topLevelNode.kind() == SyntaxKind.TABLE) {
            return new Toml((TomlTableNode) topLevelNode);
        }
        return null;
    }

    /**
     * Get Array of Tables from TOML.
     *
     * @param key Identifier of the table.
     * @return List of Tables
     */
    public List<Toml> getTables(String key) {
        TopLevelNode tableNode = rootNode.children().get(key);
        if (tableNode.kind() == SyntaxKind.TABLE_ARRAY) {
            TomlTableArrayNode tomlTableArrayNode = (TomlTableArrayNode) tableNode;
            List<TomlTableNode> childs = tomlTableArrayNode.children();
            List<Toml> tomlList = new ArrayList<>();
            for (TomlTableNode child : childs) {
                tomlList.add(new Toml(child));
            }
            return tomlList;
        }
        return null;
    }

    public List<TomlDiagnostic> getDiagnostics() {
        return this.rootNode.diagnostics();
    }
}
