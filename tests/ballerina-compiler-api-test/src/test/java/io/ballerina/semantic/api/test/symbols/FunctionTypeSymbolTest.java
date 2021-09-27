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

package io.ballerina.semantic.api.test.symbols;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for function type symbols.
 *
 * @since 2.0.0
 */
public class FunctionTypeSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/function_type_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test
    public void testFunctionType() {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(17, 100));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.VARIABLE);

        TypeSymbol varType = ((VariableSymbol) symbol.get()).typeDescriptor();
        assertEquals(varType.typeKind(), TypeDescKind.FUNCTION);

        FunctionTypeSymbol fnType = (FunctionTypeSymbol) varType;
        Optional<List<ParameterSymbol>> params = fnType.params();

        assertTrue(params.isPresent());
        List<ParameterSymbol> paramSymbols = params.get();
        ParameterSymbol rp = paramSymbols.get(0);
        assertTrue(rp.getName().isEmpty());
        assertEquals(rp.paramKind(), ParameterKind.REQUIRED);
        assertEquals(rp.typeDescriptor().typeKind(), TypeDescKind.INT);

        List<AnnotationSymbol> rpAnnots = rp.annotations();
//        assertEquals(rpAnnots.size(), 1);
//        assertEquals(rpAnnots.get(0).getName().get(), "v1");

        ParameterSymbol dp = paramSymbols.get(1);
        assertTrue(dp.getName().isPresent());
        assertEquals(dp.getName().get(), "dp");
        assertEquals(dp.paramKind(), ParameterKind.DEFAULTABLE);
        assertEquals(dp.typeDescriptor().typeKind(), TypeDescKind.STRING);
        assertTrue(dp.annotations().isEmpty());

        Optional<ParameterSymbol> rest = fnType.restParam();
        assertTrue(rest.isPresent());
        assertTrue(rest.get().getName().isPresent());
        assertEquals(rest.get().getName().get(), "rp");
        assertEquals(rest.get().paramKind(), ParameterKind.REST);
        assertEquals(rest.get().typeDescriptor().typeKind(), TypeDescKind.ARRAY);
        assertTrue(rest.get().annotations().isEmpty());

        assertTrue(fnType.returnTypeDescriptor().isPresent());
        assertEquals(fnType.returnTypeDescriptor().get().typeKind(), TypeDescKind.ANYDATA);
//        assertTrue(fnType.returnTypeAnnotations().isPresent());
//        assertEquals(fnType.returnTypeAnnotations().get().annotations().size(), 1);
//        assertEquals(fnType.returnTypeAnnotations().get().annotations().get(0).getName().get(), "v1");
    }

    @Test
    public void testAnyFunctionType() {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(18, 13));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.VARIABLE);

        TypeSymbol varType = ((VariableSymbol) symbol.get()).typeDescriptor();
        assertEquals(varType.typeKind(), TypeDescKind.FUNCTION);

        FunctionTypeSymbol fnType = (FunctionTypeSymbol) varType;
        assertTrue(fnType.params().isEmpty());
        assertTrue(fnType.restParam().isEmpty());
        assertTrue(fnType.returnTypeDescriptor().isEmpty());
        assertTrue(fnType.returnTypeAnnotations().isEmpty());
    }
}
