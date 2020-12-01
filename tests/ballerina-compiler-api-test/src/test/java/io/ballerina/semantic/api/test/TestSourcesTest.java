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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import io.ballerina.tools.text.LineRange;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getSymbolNames;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Test cases for the positions of symbols.
 *
 * @since 2.0.0
 */
public class TestSourcesTest {

    private SemanticModel model;

    @BeforeClass
    public void setup() {
        model = SemanticAPITestUtils.getSemanticModelOf("test-src/test-project", "baz");
    }

    @Test(dataProvider = "SymbolPosProvider")
    public void testSymbolAtCursor(int sLine, int sCol, String expSymbolName) {
        Optional<Symbol> symbol = model.symbol("tests/test1.bal", from(sLine, sCol));

        if (symbol.isEmpty()) {
            assertNull(expSymbolName);
        }

        assertEquals(symbol.get().name(), expSymbolName);
    }

    @DataProvider(name = "SymbolPosProvider")
    public Object[][] getSymbolPos() {
        return new Object[][]{
                {18, 7, "Config"},
                {20, 9, "assertEquals"},
                {20, 22, "PI"},
        };
    }

    @Test(dataProvider = "VisibleSymbolPosProvider")
    public void testVisibleSymbols(int line, int col, List<String> expSymbols) {
        List<Symbol> symbols = model.visibleSymbols("tests/test1.bal", from(line, col)).stream()
                .filter(sym -> sym.moduleID().moduleName().equals("semapi.baz") ||
                        !sym.moduleID().moduleName().startsWith("lang."))
                .collect(Collectors.toList());

        assertEquals(symbols.size(), expSymbols.size());
        assertList(symbols, expSymbols);
    }

    @DataProvider(name = "VisibleSymbolPosProvider")
    public Object[][] getVisibleSymbolPos() {
        List<String> moduleSymbols = List.of("PI", "ZERO", "add", "concat", "newPerson", "Person", "Employee",
                                             "BasicType", "PersonObj", "Digit", "FileNotFoundError", "EofError",
                                             "Error", "Address");
        return new Object[][]{
                {17, 0, getSymbolNames(moduleSymbols, "testConstUse", "testAdd", "test")},
                {26, 31, getSymbolNames(moduleSymbols, "testConstUse", "testAdd", "test", "sum")},
        };
    }

    @Test(dataProvider = "ExprPosProvider")
    public void testType(int sLine, int sCol, int eLine, int eCol, TypeDescKind expKind) {
        LineRange exprRange = LineRange.from("tests/test1.bal", from(sLine, sCol), from(eLine, eCol));
        Optional<TypeSymbol> type = model.type("tests/test1.bal", exprRange);

        assertEquals(type.get().typeKind(), expKind);
    }

    @DataProvider(name = "ExprPosProvider")
    public Object[][] getExprPos() {
        return new Object[][]{
                {20, 22, 20, 24, FLOAT},
                {20, 26, 20, 30, FLOAT},
                {26, 22, 26, 25, INT},
        };
    }
}
