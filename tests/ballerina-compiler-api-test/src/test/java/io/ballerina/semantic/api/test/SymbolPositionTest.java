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
//        CompileResult compileResult = BCompileUtil.compileAndCacheBala("test-src/testproject");
//        if (compileResult.getErrorCount() != 0) {
//            Arrays.stream(compileResult.getDiagnostics()).forEach(System.out::println);
//            Assert.fail("Compilation contains error");
//        }

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
                {18, 7, 18, 14, "aString"},
                {21, 9, 21, 13, "test"},
                {22, 11, 22, 16, "greet"},
                {27, 6, 27, 11, "HELLO"},
                {29, 5, 29, 11, "Person"},
                {33, 5, 33, 14, "PersonObj"},
                {34, 11, 34, 15, "name"},
                {36, 13, 36, 20, "getName"},
                {39, 6, 39, 17, "PersonClass"},
                {42, 13, 42, 17, "init"},
                {46, 13, 46, 20, "getName"},
                {53, 5, 53, 11, "Colour"},
                {56, 11, 56, 13, "w1"},
                {78, 30, 78, 35, "myStr"},
        };
    }

    @Test(dataProvider = "PositionProvider2")
    public void testTypeNarrowedSymbolPositions(int line, int col) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        assertEquals(symbol.get().getName().get(), "val");

        Location pos = symbol.get().getLocation().get();
        assertEquals(pos.lineRange().filePath(), "symbol_position_test.bal");
        assertEquals(pos.lineRange().startLine().line(), 62);
        assertEquals(pos.lineRange().startLine().offset(), 21);
        assertEquals(pos.lineRange().endLine().line(), 62);
        assertEquals(pos.lineRange().endLine().offset(), 24);
    }

    @DataProvider(name = "PositionProvider2")
    public Object[][] getTypeNarrowedPositions() {
        return new Object[][]{
                {66, 13},
                {69, 21},
                {71, 23},
                {74, 20},
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
                    {90, 4, "Module", 80, 12, 80, 18}
            };
    }
}
