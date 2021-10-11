// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package io.ballerina.semantic.api.test.actions;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for use of symbol() with start action.
 *
 * @since 2.0.0
 */
public class SymbolsInStartActionsTest {
    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/actions/symbols_in_start_actions_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PosFunctionMethodRemoteMethod")
    public void testFuncMethodRemoteMethodCall(int line, int col, String name, SymbolKind symbolKind) {
        assertBasicsAndGetSymbol(model, srcFile, line, col, name, symbolKind);
    }

    @DataProvider(name = "PosFunctionMethodRemoteMethod")
    public Object[][] getPosFunctionMethodRemoteMethod() {
        return new Object[][]{
                {24, 30, "foo", SymbolKind.FUNCTION},
                {25, 14, "bar", SymbolKind.FUNCTION},
                {28, 21, "eat", SymbolKind.METHOD},
                {29, 21, "walk", SymbolKind.METHOD},
                {32, 23, "ride", SymbolKind.METHOD},
        };
    }

    @Test(dataProvider = "PosObj")
    public void testObject(int line, int col, String expName, TypeDescKind expTypeKind) {
        VariableSymbol symbol = (VariableSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col, expName,
                                                                          SymbolKind.VARIABLE);
        assertEquals(symbol.typeDescriptor().typeKind(), TypeDescKind.TYPE_REFERENCE);
        TypeReferenceTypeSymbol typeSymbol = (TypeReferenceTypeSymbol) symbol.typeDescriptor();
        assertEquals(typeSymbol.typeDescriptor().typeKind(), expTypeKind);
    }

    @DataProvider(name = "PosObj")
    public Object[][] getPosOfObjects() {
        return new Object[][]{
                {28, 14, "animal", TypeDescKind.OBJECT},
                {32, 14, "vehicle", TypeDescKind.OBJECT},
        };
    }

    @Test(dataProvider = "PosOfSymbolsInStartAction")
    public void testSymbolsInStartAction(int line, int col, String expName, SymbolKind expSymKind) {
        assertBasicsAndGetSymbol(model, srcFile, line, col, expName, expSymKind);
    }

    @DataProvider(name = "PosOfSymbolsInStartAction")
    public Object[][] getPosOfSymbolsInStartAction() {
        return new Object[][]{
                {25, 18, "nm", SymbolKind.VARIABLE},
//                {25, 22, "age", SymbolKind.PARAMETER},    // Named-arg symbol is not present
//                {26, 29, "ints", SymbolKind.VARIABLE},    // Rest variable ref is not present

                {29, 26, "ag", SymbolKind.VARIABLE},
//                {29, 30, "path", SymbolKind.PARAMETER},   // Named-arg symbol is not present
                {29, 37, "nm", SymbolKind.VARIABLE},
//                {30, 37, "ints", SymbolKind.VARIABLE},  // Rest variable ref is not present

                {32, 28, "nm", SymbolKind.VARIABLE},
//                {32, 32, "b", SymbolKind.PARAMETER},  // Named-arg symbol is not present
                {32, 36, "ag", SymbolKind.VARIABLE},
//                {33, 39, "ints", SymbolKind.VARIABLE},  // Rest variable ref is not present
        };
    }
}
