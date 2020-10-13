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

import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.compile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Test cases for the positions of symbols.
 *
 * @since 2.0.0
 */
public class SymbolPositionTest {

    private BallerinaSemanticModel model;

    @BeforeClass
    public void setup() {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/symbol_position_test.bal", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        model = new BallerinaSemanticModel(pkg, context);
    }

    @Test(dataProvider = "PositionProvider")
    public void testSymbolPositions(int sLine, int sCol, int eLine, int eCol, String expSymbolName) {
        Optional<Symbol> symbol = model.symbol("symbol_position_test.bal", LinePosition.from(sLine, sCol));

        if (!symbol.isPresent()) {
            assertNull(expSymbolName);
        }

        assertEquals(symbol.get().name(), expSymbolName);

        Location pos = symbol.get().location();
        assertEquals(pos.lineRange().filePath(), "symbol_position_test.bal");
        assertEquals(pos.lineRange().startLine().line(), sLine);
        assertEquals(pos.lineRange().startLine().offset(), sCol);
        assertEquals(pos.lineRange().endLine().line(), eLine);
        assertEquals(pos.lineRange().endLine().offset(), eCol);
    }

    @DataProvider(name = "PositionProvider")
    public Object[][] getPositions() {
        return new Object[][]{
                {16, 7, 16, 14, "aString"},
                {19, 9, 19, 13, "test"},
                {20, 11, 20, 16, "greet"},
                {25, 6, 25, 11, "HELLO"},
                {27, 5, 27, 11, "Person"},
                {31, 5, 31, 14, "PersonObj"},
                {32, 11, 32, 15, "name"},
                {34, 13, 34, 20, "PersonObj.getName"},
                {37, 6, 37, 17, "PersonClass"},
                {40, 13, 40, 17, "PersonClass.init"},
                {44, 13, 44, 20, "PersonClass.getName"},
                {51, 5, 51, 11, "Colour"},
                {54, 11, 54, 13, "w1"},
        };
    }

    @Test(dataProvider = "PositionProvider2")
    public void testTypeNarrowedSymbolPositions(int line, int col) {
        Optional<Symbol> symbol = model.symbol("symbol_position_test.bal", LinePosition.from(line, col));

        assertEquals(symbol.get().name(), "val");

        Location pos = symbol.get().location();
        assertEquals(pos.lineRange().filePath(), "symbol_position_test.bal");
        assertEquals(pos.lineRange().startLine().line(), 60);
        assertEquals(pos.lineRange().startLine().offset(), 21);
        assertEquals(pos.lineRange().endLine().line(), 60);
        assertEquals(pos.lineRange().endLine().offset(), 24);
    }

    @DataProvider(name = "PositionProvider2")
    public Object[][] getTypeNarrowedPositions() {
        return new Object[][]{
                {64, 13},
                {67, 21},
                {69, 23},
                {72, 20},
        };
    }
}
