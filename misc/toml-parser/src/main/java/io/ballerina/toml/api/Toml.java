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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlTransformer;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.toml.validator.TomlValidator;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * API For Parsing Tom's Obvious, Minimal Language (TOML) file.
 *
 * @since 2.0.0
 */
public class Toml {

    private TomlTableNode rootNode;

    /**
     * Creates new Root TOML Node from AST.
     *
     * @param tomlTableNode AST representation of TOML Table.
     */
    public Toml(TomlTableNode tomlTableNode) {
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
        Path fileNamePath = path.getFileName();
        if (fileNamePath == null) {
            return null;
        }
        return read(Files.readString(path),
                fileNamePath.toString());
    }

    /**
     * Read TOML File using Path and validate it against a json schema.
     *
     * @param path Path of the TOML file
     * @param schema json schema to validate the toml against
     * @return TOML Object
     * @throws IOException if file is not accessible
     */
    public static Toml read(Path path, Schema schema) throws IOException {
        Path fileNamePath = path.getFileName();
        if (fileNamePath == null) {
            return null;
        }
        return read(Files.readString(path), fileNamePath.toString(), schema);
    }

    /**
     * Read TOML File using InputStream.
     *
     * @param inputStream InputStream of the TOML file
     * @return TOML Object
     * @throws IOException if file is not accessible
     */
    public static Toml read(InputStream inputStream) throws IOException {
        return read(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), null);
    }

    /**
     * Read TOML File using InputStream and validate against a json schema.
     *
     * @param inputStream InputStream of the TOML file
     * @param schema json schema to validate the toml against
     * @return TOML Object
     * @throws IOException if file is not accessible
     */
    public static Toml read(InputStream inputStream, Schema schema) throws IOException {
        return read(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), null, schema);
    }

    /**
     * Parse TOML file using TOML String.
     *
     * @param content String representation of the TOML file content.
     * @param fileName file name of the TOML file
     * @return TOML Object
     */
    public static Toml read(String content, String fileName) {
        TextDocument textDocument = TextDocuments.from(content);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument, fileName);
        TomlTransformer nodeTransformer = new TomlTransformer();
        TomlTableNode
                transformedTable = (TomlTableNode) nodeTransformer.transform((DocumentNode) syntaxTree.rootNode());
        return new Toml(transformedTable);
    }

    /**
     * Parse TOML file using TOML String and validate against a json schema.
     *
     * @param content String representation of the TOML file content.
     * @param schema json schema to validate the toml against
     * @param fileName file name of the TOML file
     * @return TOML Object
     */
    public static Toml read(String content, String fileName, Schema schema) {
        TextDocument textDocument = TextDocuments.from(content);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument, fileName);
        TomlTransformer nodeTransformer = new TomlTransformer();
        TomlTableNode
                transformedTable = (TomlTableNode) nodeTransformer.transform((DocumentNode) syntaxTree.rootNode());
        Toml toml = new Toml(transformedTable);
        TomlValidator tomlValidator = new TomlValidator(schema);
        tomlValidator.validate(toml);
        return toml;
    }

    /**
     * Get value from a key of a Key Value pair.
     *
     * @param dottedKey key name
     * @param <T> Type of the AST Value Node
     * @return AST Value Node
     */
    public <T extends TomlValueNode> Optional<T> get(String dottedKey) {
        String[] parentDottedKeys = splitDottedkeys(dottedKey);
        String lastKey = parentDottedKeys[parentDottedKeys.length - 1];
        parentDottedKeys = Arrays.copyOf(parentDottedKeys, parentDottedKeys.length - 1);
        Optional<TomlTableNode> parentNode = getParentNode(parentDottedKeys);
        if (parentNode.isEmpty()) {
            return Optional.empty();
        }
        TomlTableNode parentTableNode = parentNode.get();

        TopLevelNode valueEntry = parentTableNode.entries().get(lastKey);

        if (valueEntry == null || valueEntry.kind() != TomlType.KEY_VALUE) {
            return Optional.empty();
        }
        TomlKeyValueNode keyValueNode = (TomlKeyValueNode) valueEntry;
        if (keyValueNode.value() == null) {
            return Optional.empty();
        }
        TomlValueNode value = keyValueNode.value();
        return Optional.ofNullable((T) value);
    }

    private String[] splitDottedkeys(String dottedKey) {
        return dottedKey.split("\\.(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private Optional<TomlTableNode> getParentNode(String[] parentDottedKeys) {
        TomlTableNode parentTableNode = this.rootNode;
        for (String key : parentDottedKeys) {
            TopLevelNode topLevelNode = parentTableNode.entries().get(key);
            if (topLevelNode == null || topLevelNode.kind() != TomlType.TABLE) {
                return Optional.empty();
            }
            parentTableNode = (TomlTableNode) topLevelNode;
        }

        if (parentTableNode == null || parentTableNode.kind() != TomlType.TABLE) {
            return Optional.empty();
        }
        return Optional.of(parentTableNode);
    }

    /**
     * Get Child Table from TOML.
     *
     * @param dottedKey Identifier of the table.
     * @return Child Table
     */
    public Optional<Toml> getTable(String dottedKey) {
        String[] parentDottedKeys = splitDottedkeys(dottedKey);

        Optional<TomlTableNode> parentNode = getParentNode(parentDottedKeys);
        if (parentNode.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Toml(parentNode.get()));
    }

    /**
     * Get Array of Tables from TOML.
     *
     * @param dottedKey Identifier of the table.
     * @return List of Tables
     */
    public List<Toml> getTables(String dottedKey) {
        String[] parentDottedKeys = splitDottedkeys(dottedKey);
        String lastKey = parentDottedKeys[parentDottedKeys.length - 1];
        parentDottedKeys = Arrays.copyOf(parentDottedKeys, parentDottedKeys.length - 1);
        Optional<TomlTableNode> parentNode = getParentNode(parentDottedKeys);
        if (parentNode.isEmpty()) {
            return Collections.emptyList();
        }
        TomlTableNode parentTableNode = parentNode.get();

        TopLevelNode valueEntry = parentTableNode.entries().get(lastKey);

        if (valueEntry == null || valueEntry.kind() != TomlType.TABLE_ARRAY) {
            return Collections.emptyList();
        }

        TomlTableArrayNode tomlTableArrayNode = (TomlTableArrayNode) valueEntry;
        List<TomlTableNode> childs = tomlTableArrayNode.children();
        List<Toml> tomlList = new ArrayList<>();
        for (TomlTableNode child : childs) {
            tomlList.add(new Toml(child));
        }

        return tomlList;
    }

    public List<Diagnostic> diagnostics() {
        return new ArrayList<>(this.rootNode.diagnostics());
    }

    public TomlTableNode rootNode() {
        return rootNode;
    }
    
    public Map<String, Object> toMap() {
        return this.rootNode.toNativeObject();
    }

    public <T> T to(Class<T> targetClass) {
        Gson gson = new Gson();
        JsonElement json = gson.toJsonTree(toMap());

        if (targetClass == JsonElement.class) {
            return targetClass.cast(json);
        }

        return gson.fromJson(json, targetClass);
    }
}
