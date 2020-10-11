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
                {17, 8, 17, 15, "aString"},
                {20, 10, 20, 14, "test"},
                {21, 12, 21, 17, "greet"},
                {26, 7, 26, 12, "HELLO"},
                {28, 6, 28, 12, "Person"},
                {32, 6, 32, 15, "PersonObj"},
                {33, 12, 33, 16, "name"},
                {35, 14, 35, 21, "PersonObj.getName"},
                {38, 7, 38, 18, "PersonClass"},
                {41, 14, 41, 18, "PersonClass.init"},
                {45, 14, 45, 21, "PersonClass.getName"},
                {52, 6, 52, 12, "Colour"},
                {55, 12, 55, 14, "w1"},
        };
    }

    @Test(dataProvider = "PositionProvider2")
    public void testTypeNarrowedSymbolPositions(int line, int col) {
        Optional<Symbol> symbol = model.symbol("symbol_position_test.bal", LinePosition.from(line, col));

        assertEquals(symbol.get().name(), "val");

        Location pos = symbol.get().location();
        assertEquals(pos.lineRange().filePath(), "symbol_position_test.bal");
        assertEquals(pos.lineRange().startLine().line(), 61);
        assertEquals(pos.lineRange().startLine().offset(), 22);
        assertEquals(pos.lineRange().endLine().line(), 61);
        assertEquals(pos.lineRange().endLine().offset(), 25);
    }

    @DataProvider(name = "PositionProvider2")
    public Object[][] getTypeNarrowedPositions() {
        return new Object[][]{
                {65, 14},
                {68, 22},
                {70, 24},
                {73, 21},
        };
    }
}
