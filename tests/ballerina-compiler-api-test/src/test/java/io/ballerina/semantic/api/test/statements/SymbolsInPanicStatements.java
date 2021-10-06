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

package io.ballerina.semantic.api.test.statements;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
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
import static org.testng.Assert.assertTrue;

/**
 * Test cases for use of symbol() within transactional statements.
 *
 * @since 2.0.0
 */
public class SymbolsInPanicStatements {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/statements/symbols_in_panic_stmt.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test
    public void testSymbols1() {
        Symbol symbol = assertBasicsAndGetSymbol(model, srcFile, 20, 14, "call1", SymbolKind.FUNCTION);
        FunctionSymbol functionSymbol = (FunctionSymbol) symbol;
        assertTrue(functionSymbol.typeDescriptor().returnTypeDescriptor().isPresent());
        TypeSymbol typeSymbol = functionSymbol.typeDescriptor().returnTypeDescriptor().get();
        assertEquals(typeSymbol.typeKind(), TypeDescKind.ERROR);
    }

    @Test
    public void testSymbols2() {
        Symbol symbol = assertBasicsAndGetSymbol(model, srcFile, 22, 14, "val", SymbolKind.VARIABLE);
        VariableSymbol varSymbol = (VariableSymbol) symbol;
        TypeSymbol typeSymbol = varSymbol.typeDescriptor();
        assertEquals(typeSymbol.typeKind(), TypeDescKind.ERROR);
    }
}
