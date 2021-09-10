/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.DiagnosticState;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.COMPILATION_ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the looking up a symbol of an identifier at a given position.
 *
 * @since 2.0.0
 */
public class SymbolAtCursorTest {

    @Test(dataProvider = "BasicsPosProvider")
    public void testBasics(int line, int column, String expSymbolName) {
        Project project = BCompileUtil.loadProject("test-src/symbol_at_cursor_basic_test.bal");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);

        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, column));
        symbol.ifPresent(value -> assertEquals(value.getName().get(), expSymbolName));

        if (symbol.isEmpty()) {
            assertNull(expSymbolName);
        }
    }

    @DataProvider(name = "BasicsPosProvider")
    public Object[][] getPositionsForExactLookup() {
        return new Object[][]{
                {16, 6, null},
                {16, 7, "aString"},
                {16, 9, "aString"},
                {16, 14, null},
                {19, 8, null},
                {19, 9, "test"},
                {19, 11, "test"},
                {19, 13, null},
                {22, 59, "HELLO"},
                {26, 11, null},
                {26, 12, "a"},
                {26, 13, null},
                {23, 7, "greet"},
                {40, 7, "Person"},
                {41, 7, "PersonObj"},
                {42, 20, "pObj"},
                {42, 29, "getFullName"},
                {43, 11, "p"},
                {43, 15, "name"},
                {44, 19, "p"},
                {44, 22, "name"},
                {49, 4, null},
                {49, 8, "Person"},
                {49, 14, null},
                {50, 14, "name"},
                {53, 10, "PersonObj"},
                {54, 14, "fname"},
                {55, 14, "lname"},
                {58, 15, "fname"},
                {59, 11, "self"},
                {63, 23, "fname"},
                {63, 41, "lname"},
                {72, 4, "test"},
                {72, 7, "test"},
                {76, 25, "RSA"},
                {82, 8, "rsa"},
                {83, 15, "RSA"},
                {86, 2, "v1"},
                {91, 23, "v2"},
                {92, 23, "v2"},
                {93, 23, "v2"},
                {93, 78, "v3"},
                {102, 2, "v4"},
                {112, 4, null},
                {115, 29, "CONST1"},
                {118, 74, "r"},
                {122, 56, "myStr"},
                {135, 5, "Atype"},
                {150, 12, "ParseError"},
        };
    }

    @Test(dataProvider = "EnumPosProvider")
    public void testEnum(int line, int column, String expSymbolName) {
        Project project = BCompileUtil.loadProject("test-src/symbol_at_cursor_enum_test.bal");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);

        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, column));
        symbol.ifPresent(value -> assertEquals(value.getName().get(), expSymbolName));

        if (symbol.isEmpty()) {
            assertNull(expSymbolName);
        }
    }

    @DataProvider(name = "EnumPosProvider")
    public Object[][] getEnumPos() {
        return new Object[][]{
                {16, 5, "Colour"},
                {17, 6, "RED"},
                {21, 18, "RED"},
                {22, 8, "Colour"},
                {26, 45, "Colour"},
                {30, 17, "GREEN"},
                {31, 28, "BLUE"},
        };
    }

    @Test(dataProvider = "WorkerSymbolPosProvider")
    public void testWorkers(int line, int column, String expSymbolName) {
        Project project = BCompileUtil.loadProject("test-src/symbol_lookup_with_workers_test.bal");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);

        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, column));
        symbol.ifPresent(value -> assertEquals(value.getName().get(), expSymbolName));

        if (symbol.isEmpty()) {
            assertNull(expSymbolName);
        }
    }

    @DataProvider(name = "WorkerSymbolPosProvider")
    public Object[][] getWorkerPos() {
        return new Object[][]{
                {21, 12, "w1"},
                {23, 12, "w2"},
                {26, 13, "w2"},
                {28, 23, "w2"},
                {34, 14, "w1"},
                {36, 20, "w1"},
                {39, 20, "w2"},
        };
    }

    @Test(dataProvider = "MissingConstructPosProvider")
    public void testMissingConstructs(int line, int column) {
        Project project = BCompileUtil.loadProject("test-src/symbol_at_cursor_undefined_constructs_test.bal");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);

        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, column));
        symbol.ifPresent(value -> assertTrue(true, "Unexpected symbol: " + value.getName().get()));
    }

    @DataProvider(name = "MissingConstructPosProvider")
    public Object[][] getMissingConstructsPos() {
        return new Object[][]{
                {20, 3},
                {21, 25},
                {23, 3},
        };
    }

    @Test(dataProvider = "QuotedIdentifierProvider")
    public void testQuotedIdentifiers(int line, int column, String expSymbolName, String expSymbolNameUnQuoted) {
        Project project = BCompileUtil.loadProject("test-src/symbol_at_cursor_quoted_identifiers_test.bal");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);

        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, column));
        symbol.ifPresent(value -> {
            assertTrue(value.nameEquals(expSymbolName));
            assertTrue(value.nameEquals(expSymbolNameUnQuoted));
        });

        if (symbol.isEmpty()) {
            assertNull(expSymbolName);
        }
    }

    @DataProvider(name = "QuotedIdentifierProvider")
    public Object[][]  getQuotedIdentifierPositions() {
        return new Object[][]{
                {18, 5, "'record", "record"},
                {19, 8, "'float", "float"},
                {22, 23, "'from", "from"},
                {23, 28, "'from", "from"},
                {26, 9, "'function", "function"},
                {30, 11, "'function", "function"},
                {31, 7, "'if", "if"},
                {32, 11, "'if", "if"},
                {33, 7, "'nonReservedVar", "nonReservedVar"},
                {39, 4, "'any", "any"},
                {40, 13, "'any", "any"},
                {43, 11, "'w1", "w1"},
                {45, 19, "'worker", "worker"},
                {52, 11, "'worker", "worker"},
                {58, 18, "'string", "string"},
                {60, 10, "'floatNum", "floatNum"},
                {62, 25, "'foreach", "foreach"},
                {63, 13, "'string", "string"},
                {64, 20, "'int", "int"},
                {68, 48, "'string", "string"},
                {70, 13, "'from", "from"},
                {78, 27, "'Example", "Example"},
                {80, 11, "'check", "check"},
                {85, 7, "'int", "int"},
                {86, 8, "'from", "from"},
                {86, 19, "'from", "from"},
                {88, 4, "'Example", "Example"},
                {88, 13, "'new", "new"},
                {89, 4, "'new", "new"},
                {89, 9, "'anydata", "anydata"},
                {90, 11, "'foo", "foo"},
                {91, 15, "'string", "string"},
                {92, 12, "'intVal", "\\u{69}ntVal"},
                {93, 18, "'intVal2", "intVal2"},
                {94, 16, "'intVal3", "intVal3"}
        };
    }

    @Test(dataProvider = "VarPosProvider")
    public void testVarsWithFunctionType(int line, int col, String name) {
        Project project = BCompileUtil.loadProject("test-src/regression-tests/field_with_function_type.bal");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);

        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.FUNCTION);
        assertEquals(symbol.get().getName().get(), name);
        assertEquals(((FunctionSymbol) symbol.get()).typeDescriptor().typeKind(), TypeDescKind.FUNCTION);
    }

    @DataProvider(name = "VarPosProvider")
    public Object[][] getVarPos() {
        return new Object[][]{
                {19, 10, "union"},
                {23, 10, "op"},
                {27, 10, "op"}
        };
    }

    @Test(dataProvider = "SymWithDiagnosticStatePosProvider")
    public void testVarSymbolsWithDiagnosticState(int line, int col, TypeDescKind typeKind, DiagnosticState state) {
        Project project = BCompileUtil.loadProject("test-src/var_symbols_with_error_type_test.bal");
        SemanticModel model = getDefaultModulesSemanticModel(project);
        Document srcFile = getDocumentForSingleSource(project);

        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), VARIABLE);
        assertEquals(((VariableSymbol) symbol.get()).typeDescriptor().typeKind(), typeKind);
        assertEquals(((VariableSymbol) symbol.get()).diagnosticState(), state);
    }

    @DataProvider(name = "SymWithDiagnosticStatePosProvider")
    public Object[][] getSymWithDiagnosticStatePos() {
        return new Object[][]{
                {17, 8, INT, DiagnosticState.VALID},
                {20, 10, COMPILATION_ERROR, DiagnosticState.REDECLARED},
                {23, 8, COMPILATION_ERROR, DiagnosticState.UNKNOWN_TYPE},
                {24, 8, COMPILATION_ERROR, DiagnosticState.UNKNOWN_TYPE},
                {25, 8, COMPILATION_ERROR, DiagnosticState.UNKNOWN_TYPE},
                {26, 11, TYPE_REFERENCE, DiagnosticState.VALID},
                {27, 8, COMPILATION_ERROR, DiagnosticState.UNKNOWN_TYPE},
                {30, 8, COMPILATION_ERROR, DiagnosticState.UNKNOWN_TYPE},
                {33, 8, COMPILATION_ERROR, DiagnosticState.UNKNOWN_TYPE},
                {35, 8, TYPE_REFERENCE, DiagnosticState.VALID},
        };
    }
}
