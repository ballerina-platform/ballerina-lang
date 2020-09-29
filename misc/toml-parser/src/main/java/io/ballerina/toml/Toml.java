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

package io.ballerina.toml;

import io.ballerina.toml.ast.TomlArray;
import io.ballerina.toml.ast.TomlBasicValue;
import io.ballerina.toml.ast.TomlDiagnostic;
import io.ballerina.toml.ast.TomlKey;
import io.ballerina.toml.ast.TomlKeyValue;
import io.ballerina.toml.ast.TomlNodeLocation;
import io.ballerina.toml.ast.TomlTable;
import io.ballerina.toml.ast.TomlTableArray;
import io.ballerina.toml.ast.TomlTransformer;
import io.ballerina.toml.ast.TomlValidator;
import io.ballerina.toml.ast.TomlValue;
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
import java.util.Arrays;
import java.util.List;

/**
 * API For Parsing Tom's Obvious, Minimal Language (TOML) file.
 *
 * @since 0.1.0
 */
public class Toml {

    private TomlTable rootNode;
    private List<TomlDiagnostic> diagnostics;

    /**
     * Creates empty TOML Node.
     */
    public Toml() {
        TomlKey tomlKey = new TomlKey("RootNode", SyntaxKind.MODULE_NAME);
        this.rootNode = new TomlTable(tomlKey);
        this.diagnostics = new ArrayList<>();
    }

    /**
     * Creates new Root TOML Node from AST.
     *
     * @param tomlTable AST representation of TOML Table.
     */
    public Toml(TomlTable tomlTable) {
        this.rootNode = tomlTable;
        this.diagnostics = this.rootNode.getDiagnostics();
    }

    /**
     * Read TOML File using Path.
     *
     * @param path Path of the TOML file
     * @return TOML Object
     * @throws IOException if file is not accessible
     */
    public Toml read(Path path) throws IOException {
        return this.read(Files.newBufferedReader(path));
    }

    /**
     * Read TOML File using InputStream.
     *
     * @param inputStream InputStream of the TOML file
     * @return TOML Object
     * @throws IOException if file is not accessible
     */
    public Toml read(InputStream inputStream) throws IOException {
        return this.read(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    /**
     * Read TOML File using Reader.
     *
     * @param reader reader of the TOML file
     * @return TOML Object
     * @throws IOException if file is not accessible
     */
    public Toml read(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder w = new StringBuilder();
        for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
            w.append(line).append('\n');
        }
        return this.read(w.toString());
    }

    /**
     * Parse TOML file using TOML String.
     *
     * @param content String representation of the TOML file content.
     * @return TOML Object
     */
    public Toml read(String content) {
        TextDocument textDocument = TextDocuments.from(content);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        diagnostics.addAll(reportSyntaxDiagnostics(syntaxTree));
        TomlTransformer nodeTransformer = new TomlTransformer();
        TomlTable transformedTable = (TomlTable) nodeTransformer.transform((ModulePartNode) syntaxTree.rootNode());
        TomlValidator tomlValidator = new TomlValidator();
        tomlValidator.visit(transformedTable);
        transformedTable.setSyntacticalDiagnostics(diagnostics);
        diagnostics.addAll(transformedTable.collectSemanticDiagnostics());
        return new Toml(transformedTable);
    }

    private List<TomlDiagnostic> reportSyntaxDiagnostics(SyntaxTree tree) {
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
     * Get String Value from TOML Key Value Pair.
     *
     * @param key Name of the Key or Dotted Key
     * @return String Value of the Key Value Pair
     */
    public String getString(String key) {
        Object value = getValueFromAST(key);
        return (String) value;
    }

    /**
     * Get Double Value from TOML Key Value Pair.
     *
     * @param key Name of the Key or Dotted Key
     * @return Double Value of the Key Value pair
     */
    public Double getDouble(String key) {
        Object value = getValueFromAST(key);
        return (Double) value;
    }

    /**
     * Get Long Value from TOML Key Value Pair.
     *
     * @param key Name of the Key or Dotted Key
     * @return Long Value of the Key Value pair
     */
    public Long getLong(String key) {
        Object value = getValueFromAST(key);
        return (Long) value;
    }

    /**
     * Get Boolean Value from TOML Key Value Pair.
     *
     * @param key Name of the Key or Dotted Key
     * @return Boolean Value of the Key Value pair
     */
    public Boolean getBoolean(String key) {
        Object value = getValueFromAST(key);
        return (Boolean) value;
    }

    /**
     * Get Array Value from TOML Key Value Pair.
     *
     * @param key Name of the Key or Dotted Key
     * @return Array Value of the Key Value pair
     */
    public <T> List<T> getList(String key) {
        Object value = getValueFromAST(key);
        return (List<T>) value;
    }

    private Object getValueFromAST(String key) {
        String[] split = key.split("\\.");
        String[] parentTables = Arrays.copyOf(split, split.length - 1);
        String lastKey = split[split.length - 1];

        TomlTable parentTable = getParentTable(parentTables);
        if (parentTable != null) {
            TomlKeyValue tomlKeyValue = (TomlKeyValue) parentTable.getChildren().get(lastKey);
            if (tomlKeyValue != null) {

                TomlValue value = tomlKeyValue.value;
                if (value.getKind() == SyntaxKind.ARRAY) {
                    return extractValuesFromTomlArray((TomlArray) value);
                } else {
                    return ((TomlBasicValue) value).getValue();
                }
            }
        }
        return null;
    }

    private List<Object> extractValuesFromTomlArray(TomlArray array) {
        List<TomlValue> elements = array.getElements();
        List<Object> output = new ArrayList<>();
        for (TomlValue value : elements) {
            if (value instanceof TomlBasicValue) {
                output.add(((TomlBasicValue) value).getValue());
            } else if (value instanceof TomlArray) {
                output.add(extractValuesFromTomlArray((TomlArray) value));
            }
        }
        return output;
    }

    /**
     * Get Child Table from TOML.
     *
     * @param key Identifier of the table.
     * @return Child Table
     */
    public Toml getTable(String key) {
        String[] parentTables = key.split("\\.");
        return new Toml(getParentTable(parentTables));
    }

    private TomlTable getParentTable(String[] parentTables) {
        TomlTable parentTable = rootNode;
        ArrayList<String> parentKeys = new ArrayList<>();
        for (String table : parentTables) {
            parentKeys.add(table);
            String parentKey = String.join(".", parentKeys);
            TopLevelNode tableNode = parentTable.getChildren().get(parentKey);
            if (tableNode instanceof TomlTable) {
                parentTable = (TomlTable) tableNode;
            } else {
                return null;
            }
        }
        return parentTable;
    }

    /**
     * Get Array of Tables from TOML.
     *
     * @param key Identifier of the table.
     * @return List of Tables
     */
    public List<Toml> getTables(String key) {
        String[] parentTables = key.split("\\.");
        TomlTable parentTable = rootNode;
        ArrayList<String> parentKeys = new ArrayList<>();
        for (String table : parentTables) {
            parentKeys.add(table);
            String parentKey = String.join(".", parentKeys);
            TopLevelNode tableNode = parentTable.getChildren().get(parentKey);
            if (tableNode instanceof TomlTable) {
                parentTable = (TomlTable) tableNode;
            } else if (tableNode instanceof TomlTableArray) {
                TomlTableArray tomlTableArray = (TomlTableArray) tableNode;
                List<TomlTable> childs = tomlTableArray.getChilds();
                List<Toml> tomlList = new ArrayList<>();
                for (TomlTable child : childs) {
                    tomlList.add(new Toml(child));
                }
                return tomlList;
            } else {
                return null;
            }
        }
        return null;
    }

    public List<TomlDiagnostic> getDiagnostics() {
        return this.diagnostics;
    }
}
