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
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the positions of symbols.
 *
 * @since 2.0.0
 */
public class SymbolPositionTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbol_position_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PositionProvider")
    public void testSymbolPositions(int sLine, int sCol, int eLine, int eCol, String expSymbolName) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(sLine, sCol));

        if (symbol.isEmpty()) {
            assertNull(expSymbolName);
        }

        assertTrue(symbol.isPresent());
        assertTrue(symbol.get().getName().isPresent());
        assertEquals(symbol.get().getName().get(), expSymbolName);

        assertTrue(symbol.get().getLocation().isPresent());

        Location pos = symbol.get().getLocation().get();
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
                {34, 13, 34, 20, "getName"},
                {37, 6, 37, 17, "PersonClass"},
                {40, 13, 40, 17, "init"},
                {44, 13, 44, 20, "getName"},
                {51, 5, 51, 11, "Colour"},
                {54, 11, 54, 13, "w1"},
                {76, 30, 76, 35, "myStr"},
        };
    }

    @Test(dataProvider = "PositionProvider2")
    public void testTypeNarrowedSymbolPositions(int line, int col) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        assertEquals(symbol.get().getName().get(), "val");

        Location pos = symbol.get().getLocation().get();
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

    @Test(dataProvider = "TypeDefPositionProvider")
    public void testTypeDefPositions(int sLine, int sCol, String expSymbolName, int defSLine, int defSCol,
                                     int defELine, int defECol) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(sLine, sCol));

        if (symbol.isEmpty()) {
            assertNull(expSymbolName);
        }

        assertTrue(symbol.isPresent());
        assertTrue(symbol.get().getName().isPresent());
        assertEquals(symbol.get().getName().get(), expSymbolName);

        assertTrue(symbol.get().getLocation().isPresent());

        Location pos = symbol.get().getLocation().get();
        assertEquals(pos.lineRange().filePath(), "symbol_position_test.bal");
        assertEquals(pos.lineRange().startLine().line(), defSLine);
        assertEquals(pos.lineRange().startLine().offset(), defSCol);
        assertEquals(pos.lineRange().endLine().line(), defELine);
        assertEquals(pos.lineRange().endLine().offset(), defECol);
    }

    @DataProvider(name = "TypeDefPositionProvider")
    public Object[][] getTypeDefPositions() {
            return new Object[][] {
                    {88, 4, "Module", 78, 12, 78, 18},
                    {80, 5, "TypeDef", 80, 5, 80, 12},
            };
    }
}
