/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.symbols;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaModuleID;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.ParameterKind.DEFAULTABLE;
import static io.ballerina.compiler.api.symbols.ParameterKind.INCLUDED_RECORD;
import static io.ballerina.compiler.api.symbols.ParameterKind.REQUIRED;
import static io.ballerina.compiler.api.symbols.ParameterKind.REST;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for parameter symbols.
 *
 * @since 2.0.0
 */
public class ParameterSymbolTest {

    private Project project;
    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        project = BCompileUtil.loadProject("test-src/param_symbols_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "ParamPos")
    public void testSymbolAtCursor(int line, int col, String paramName, TypeDescKind typeKind, ParameterKind paramKind,
                                   String signature) {
        Symbol symbol = getSymbol(line, col);
        assertEquals(symbol.kind(), SymbolKind.PARAMETER);

        ParameterSymbol param = (ParameterSymbol) symbol;
        assertEquals(param.getName().get(), paramName);
        assertEquals(param.typeDescriptor().typeKind(), typeKind);
        assertEquals(param.paramKind(), paramKind);
        assertEquals(param.signature(), signature);
    }

    @DataProvider(name = "ParamPos")
    public Object[][] getParamPos() {
        return new Object[][]{
                {16, 31, "x", STRING, REQUIRED, "string x"},
                {16, 38, "y", INT, REQUIRED, "int y"},
                {16, 47, "f", FLOAT, DEFAULTABLE, "float f"},
                {16, 66, "grades", TYPE_REFERENCE, INCLUDED_RECORD, "*Grades grades"},
                {16, 84, "rest", ARRAY, REST, "string... rest"},
                {25, 31, "name", STRING, REQUIRED, "string name"},
                {25, 41, "age", INT, DEFAULTABLE, "int age"},
                {25, 61, "other", ARRAY, REST, "anydata... other"},
                {33, 32, "name", STRING, REQUIRED, "string name"},
                {33, 42, "age", INT, DEFAULTABLE, "int age"},
                {33, 62, "other", ARRAY, REST, "anydata... other"},
                {39, 35, "name", STRING, REQUIRED, "string name"},
                {40, 20, "name", STRING, REQUIRED, "string name"},
                {43, 31, "age", INT, DEFAULTABLE, "int age"},
                {44, 19, "age", INT, DEFAULTABLE, "int age"},
                {47, 39, "misc", ARRAY, REST, "anydata... misc"},
                {48, 20, "misc", ARRAY, REST, "anydata... misc"},
                {53, 30, "p", TYPE_REFERENCE, REQUIRED, "Person p"},
                {53, 44, "misc", ARRAY, REST, "anydata... misc"},
                {62, 55, "myStr", STRING, REQUIRED, "string myStr"},
                {67, 42, "abc", FUNCTION, REQUIRED, "function (int) returns () abc"},
                {67, 66, "pqr", FUNCTION, DEFAULTABLE, "function (boolean) returns () pqr"},
                {67, 102, "xyz", ARRAY, REST, "function (string) returns ()... xyz"},
                {71, 43, "aVal", INT, REQUIRED, "int aVal"},
                {74, 48, "value", STRING, REQUIRED, "string value"},
                {77, 90, "funcParam", FUNCTION, REQUIRED,
                        "function (function (int) returns string) returns string funcParam"},
        };
    }

    @Test
    public void testVisibleSymbols() {
        BallerinaModuleID moduleID =
                new BallerinaModuleID(project.currentPackage().getCompilation().defaultModuleBLangPackage().packageID);
        Map<String, Symbol> symbols = SemanticAPITestUtils.getSymbolsInFile(model, srcFile, 17, 20,
                                                                            moduleID);
        Map<String, Symbol> params = symbols.entrySet().stream()
                .filter(entry -> entry.getValue().kind() == SymbolKind.PARAMETER)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertParam((ParameterSymbol) params.get("x"), "x", REQUIRED, STRING);
        assertParam((ParameterSymbol) params.get("y"), "y", REQUIRED, INT);
        assertParam((ParameterSymbol) params.get("f"), "f", DEFAULTABLE, FLOAT);
        assertParam((ParameterSymbol) params.get("grades"), "grades", INCLUDED_RECORD, TYPE_REFERENCE);
        assertParam((ParameterSymbol) params.get("rest"), "rest", REST, ARRAY);
    }

    @Test
    public void testVisibleSymbolsInFuncWithFuncParams() {
        BallerinaModuleID moduleID =
                new BallerinaModuleID(project.currentPackage().getCompilation().defaultModuleBLangPackage().packageID);
        Map<String, Symbol> symbols = SemanticAPITestUtils.getSymbolsInFile(model, srcFile, 68, 4,
                moduleID);
        Map<String, Symbol> params = symbols.entrySet().stream()
                .filter(entry -> entry.getValue().kind() == SymbolKind.PARAMETER)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertParam((ParameterSymbol) params.get("abc"), "abc", REQUIRED, FUNCTION);
        assertParam((ParameterSymbol) params.get("pqr"), "pqr", DEFAULTABLE, FUNCTION);
        assertParam((ParameterSymbol) params.get("xyz"), "xyz", REST, ARRAY);
    }

    private Symbol getSymbol(int line, int col) {
        return model.symbol(srcFile, from(line, col))
                .orElseThrow(() -> new AssertionError("Expected a symbol at: (" + line + ", " + col + ")"));
    }

    private void assertParam(ParameterSymbol param, String name, ParameterKind kind, TypeDescKind typeKind) {
        assertEquals(param.getName().get(), name);
        assertEquals(param.paramKind(), kind);
        assertEquals(param.typeDescriptor().typeKind(), typeKind);
    }
}
