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
import io.ballerina.compiler.api.symbols.Annotatable;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for function and function type symbols.
 *
 * @since 2.0.0
 */
public class FunctionSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/function_symbols_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PositionProvider")
    public void testFunctionSymbol(int line, int col, String expName, SymbolKind expKind, String expDoc,
                                   String expAnnot, List<Qualifier> expQuals, List<String> expParams,
                                   TypeDescKind expRetType) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), expKind);

        FunctionSymbol functionSymbol = (FunctionSymbol) symbol.get();

        // check name
        assertEquals(functionSymbol.getName().get(), expName);

        // check documentation (metadata)
        Optional<Documentation> methodDocs = functionSymbol.documentation();
        assertTrue(methodDocs.isPresent());
        assertTrue(methodDocs.get().description().isPresent());
        assertEquals(methodDocs.get().description().get(), expDoc);

        // check annotation (metadata)
        List<AnnotationSymbol> methodAnnots = functionSymbol.annotations();
        assertEquals(methodAnnots.size(), 1);
        assertEquals(methodAnnots.get(0).getName().get(), expAnnot);

        // check qualifiers
        if (expQuals.size() > 0) {
            List<Qualifier> qualifiers = functionSymbol.qualifiers();
            expQuals.forEach(expQual -> assertTrue(qualifiers.contains(expQual)));
        } else {
            assertTrue(functionSymbol.qualifiers().isEmpty());
        }

        FunctionTypeSymbol fnType = functionSymbol.typeDescriptor();

        Optional<List<ParameterSymbol>> params = fnType.params();

        assertTrue(params.isPresent());
        List<ParameterSymbol> paramSymbols = params.get();
        assertList(paramSymbols, expParams);

        assertTrue(fnType.returnTypeDescriptor().isPresent());
        assertEquals(fnType.returnTypeDescriptor().get().typeKind(), expRetType);
    }

    @DataProvider(name = "PositionProvider")
    public Object[][] getFunctionPosition() {
        return new Object[][]{
                {31, 9, "bar", SymbolKind.FUNCTION, "Function Bar", "a1",
                        List.of(), List.of("x", "y"), TypeDescKind.INT},
                {38, 39, "barIsolated", SymbolKind.FUNCTION, "Function isolated bar", "a2",
                        List.of(Qualifier.PUBLIC, Qualifier.ISOLATED, Qualifier.TRANSACTIONAL),
                        List.of("a", "b"), TypeDescKind.NIL},
                {49, 29, "getName", SymbolKind.METHOD, "Get name", "a2", List.of(Qualifier.REMOTE, Qualifier.ISOLATED),
                        List.of("a", "b"), TypeDescKind.STRING},
        };
    }

    @Test(dataProvider = "ReturnAnnotPosProvider")
    public void testAnnotsOnReturnType(int line, int col, boolean hasAnnot, int annotSize, List<String> expAnnots) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        FunctionTypeSymbol fnType = ((FunctionSymbol) symbol.get()).typeDescriptor();
        Optional<Annotatable> annots = fnType.returnTypeAnnotations();

        assertEquals(annots.isPresent(), hasAnnot);

        if (!hasAnnot) {
            return;
        }

        assertEquals(annots.get().annotations().size(), annotSize);

        List<AnnotationSymbol> actualAnnots = annots.get().annotations();
        for (int i = 0, expAnnotsSize = expAnnots.size(); i < expAnnotsSize; i++) {
            assertEquals(actualAnnots.get(i).getName().get(), expAnnots.get(i));
        }
    }

    @DataProvider(name = "ReturnAnnotPosProvider")
    public Object[][] getReturnAnnotPos() {
        return new Object[][]{
                {16, 9, true, 1, List.of("a1")},
                {18, 9, false, 0, null},
//                TODO: following due to: https://github.com/ballerina-platform/ballerina-lang/issues/30219
//                {21, 59, true, 1, List.of("a1")},
        };
    }
}
