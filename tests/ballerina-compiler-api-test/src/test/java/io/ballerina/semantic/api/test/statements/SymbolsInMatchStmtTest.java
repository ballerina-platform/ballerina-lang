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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.statements;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for match statement.
 *
 * @since 2.0.0
 */
public class SymbolsInMatchStmtTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/statements/match_stmt.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "SymbolPosProvider")
    public void testSymbolAtCursor(int line, int col, String name, TypeDescKind typeKind) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));

        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().getName().get(), name);

        switch (symbol.get().kind()) {
            case VARIABLE:
                assertEquals(((VariableSymbol) symbol.get()).typeDescriptor().typeKind(), typeKind);
                return;
            case PARAMETER:
                assertEquals(((ParameterSymbol) symbol.get()).typeDescriptor().typeKind(), typeKind);
                return;
            case CONSTANT:
                assertEquals(((ConstantSymbol) symbol.get()).typeDescriptor().typeKind(), typeKind);
                return;
            case TYPE:
                assertEquals(((TypeReferenceTypeSymbol) symbol.get()).typeDescriptor().typeKind(), typeKind);
                return;
        }

        throw new AssertionError("Unexpected symbol kind: " + symbol.get().kind());
    }

    @DataProvider(name = "SymbolPosProvider")
    public Object[][] getSymbolPos() {
        return new Object[][]{
                {20, 10, "val", TypeDescKind.ANY},
                {21, 12, "x", TypeDescKind.ANY},
                {25, 8, "PI", TypeDescKind.SINGLETON},
                {29, 30, "rest", TypeDescKind.ARRAY},
                {30, 24, "x", TypeDescKind.UNION},
                {34, 26, "b", TypeDescKind.UNION},
                {34, 36, "rest", TypeDescKind.MAP},
                {36, 24, "val", TypeDescKind.ANY},
                {38, 14, "Error", TypeDescKind.ERROR},

        };
    }
}
