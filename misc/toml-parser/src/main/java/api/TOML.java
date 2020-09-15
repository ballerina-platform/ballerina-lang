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

package api;

import diagnostics.Diagnostic;
import diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import sementic.TomlTransformer;
import sementic.nodes.DiagnosticLog;
import sementic.nodes.DiagnosticPos;
import sementic.nodes.DiagnosticSource;
import sementic.nodes.SemanticAnalyzer;
import sementic.nodes.TomlArray;
import sementic.nodes.TomlBasicValue;
import sementic.nodes.TomlKey;
import sementic.nodes.TomlKeyValue;
import sementic.nodes.TomlTable;
import sementic.nodes.TomlTableArray;
import sementic.nodes.TomlValue;
import sementic.nodes.TopLevelNode;
import sementic.tools.DiagnosticCode;
import syntax.tree.ModulePartNode;
import syntax.tree.NodeLocation;
import syntax.tree.SyntaxKind;
import syntax.tree.SyntaxTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TOML Library API.
 */
public class TOML {

    private TomlTable rootNode;
    private DiagnosticLog dlog;

    public TOML() {
        TomlKey tomlKey = new TomlKey("RootNode", SyntaxKind.MODULE_NAME);
        this.rootNode = new TomlTable(tomlKey);
        this.dlog = DiagnosticLog.getInstance();
    }

    public TOML(TomlTable tomlTable) {
        this.rootNode = tomlTable;
    }

    public TOML read(InputStream inputStream) {
        return this.read((Reader) (new InputStreamReader(inputStream)));
    }

    public TOML read(Reader reader) {

        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            StringBuilder w = new StringBuilder();
            for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                w.append(line).append('\n');
            }

            return this.read(w.toString());
        } catch (IOException var12) {
            throw new RuntimeException(var12);
        }
    }

    public TOML read(String content) {
        TextDocument textDocument = TextDocuments.from(content);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);

        DiagnosticSource diagnosticSource = new DiagnosticSource("basic-toml.toml"); //TODO instance
        reportSyntaxDiagnostics(diagnosticSource, syntaxTree);
        TomlTransformer nodeTransformer = new TomlTransformer(diagnosticSource);
        TomlTable transformedTable = (TomlTable) nodeTransformer.transform((ModulePartNode) syntaxTree.rootNode());
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.visit(transformedTable);
        return new TOML(transformedTable);
    }

    private void reportSyntaxDiagnostics(DiagnosticSource diagnosticSource, SyntaxTree tree) {
        for (Diagnostic syntaxDiagnostic : tree.diagnostics()) {
            DiagnosticPos pos = getPosition(syntaxDiagnostic.location(), diagnosticSource);

            DiagnosticCode code;
            DiagnosticSeverity severity = syntaxDiagnostic.diagnosticInfo().severity();
            if (severity == DiagnosticSeverity.WARNING) {
                code = DiagnosticCode.SYNTAX_WARNING;
                dlog.warning(pos, code, syntaxDiagnostic.message());
            } else {
                code = DiagnosticCode.SYNTAX_ERROR;
                dlog.error(pos, code, syntaxDiagnostic.message());
            }
        }
    }

    private DiagnosticPos getPosition(NodeLocation location, DiagnosticSource diagnosticSource) {
        if (location == null) {
            return null;
        }
        LineRange lineRange = location.lineRange();
        LinePosition startPos = lineRange.startLine();
        LinePosition endPos = lineRange.endLine();
        return new DiagnosticPos(diagnosticSource, startPos.line() + 1, endPos.line() + 1,
                startPos.offset() + 1, endPos.offset() + 1);
    }

    public String getString(String key) {
        Object value = getValueFromAST(key);
        if (value instanceof String) {
            return (String) value;
        } else {
            return null;
        }
    }

    public Double getDouble(String key) {
        Object value = getValueFromAST(key);
        if (value instanceof Double) {
            return (Double) value;
        } else {
            return null;
        }
    }

    public Long getLong(String key) {
        Object value = getValueFromAST(key);
        if (value instanceof Long) {
            return (Long) value;
        } else {
            return null;
        }
    }

    public <T> List<T> getList(String key) {
        Object value = getValueFromAST(key);
        if (value instanceof List) {
            return (List<T>) value;
        }
        return null;
    }

    public Boolean getBoolean(String key) {
        Object value = getValueFromAST(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return null;
        }
    }

    private Object getValueFromAST(String key) {
        String[] split = key.split("\\.");
        String[] parentTables = Arrays.copyOf(split, split.length - 1);
        String lastKey = split[split.length - 1];

        TomlTable parentTable = getParentTable(parentTables);
        if (parentTable != null) {
            TomlKeyValue tomlKeyValue = (TomlKeyValue) parentTable.getChilds().get(lastKey);
            if (tomlKeyValue != null) {

                TomlValue value = tomlKeyValue.value;
                if (value instanceof TomlBasicValue) {
                    return ((TomlBasicValue) value).getValue();
                } else if (value instanceof TomlArray) {
                    return extractValuesFromTomlArray((TomlArray) value);
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

    public TOML getTable(String key) {
        String[] parentTables = key.split("\\.");
        return new TOML(getParentTable(parentTables));
    }

    private TomlTable getParentTable(String[] parentTables) {
        TomlTable parentTable = rootNode;
        ArrayList<String> parentKeys = new ArrayList<>();
        for (String table : parentTables) {
            parentKeys.add(table);
            String parentKey = String.join(".", parentKeys);
            TopLevelNode tableNode = parentTable.getChilds().get(parentKey);
            if (tableNode instanceof TomlTable) {
                parentTable = (TomlTable) tableNode;
            } else {
                return null;
            }
        }
        return parentTable;
    }

    public List<TOML> getTables(String key) {
        String[] parentTables = key.split("\\.");
        TomlTable parentTable = rootNode;
        ArrayList<String> parentKeys = new ArrayList<>();
        for (String table : parentTables) {
            parentKeys.add(table);
            String parentKey = String.join(".", parentKeys);
            TopLevelNode tableNode = parentTable.getChilds().get(parentKey);
            if (tableNode instanceof TomlTable) {
                parentTable = (TomlTable) tableNode;
            } else if (tableNode instanceof TomlTableArray) {
                TomlTableArray tomlTableArray = (TomlTableArray) tableNode;
                List<TomlTable> childs = tomlTableArray.getChilds();
                List<TOML> tomlList = new ArrayList<>();
                for (TomlTable child : childs) {
                    tomlList.add(new TOML(child));
                }
                return tomlList;
            } else {
                return null;
            }
        }
        return null;
    }

    public DiagnosticLog getDlog() {
        return dlog;
    }
}
