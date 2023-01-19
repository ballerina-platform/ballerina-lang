/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.ANNOTATION;
import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS;
import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.ENUM;
import static io.ballerina.compiler.api.symbols.SymbolKind.ENUM_MEMBER;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.RECORD_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.RESOURCE_METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE_DEFINITION;
import static io.ballerina.compiler.api.symbols.SymbolKind.WORKER;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.tools.text.LinePosition.from;
import static java.util.List.of;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for retrieving annotations from a symbol.
 *
 * @since 2.0.0
 */
public class AnnotationSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/annotation_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PosProvider")
    public void test(int line, int col, SymbolKind kind, List<String> annots) {
        Optional<Symbol> symbol = model.symbol(srcFile, from(line, col));
        assertEquals(symbol.get().kind(), kind);

        List<AnnotationSymbol> annotSymbols = ((Annotatable) symbol.get()).annotations();

        assertEquals(annotSymbols.size(), annots.size());
        for (int i = 0; i < annotSymbols.size(); i++) {
            assertEquals(annotSymbols.get(i).getName().get(), annots.get(i));
        }
    }

    @DataProvider(name = "PosProvider")
    public Object[][] getPos() {
        return new Object[][]{
                {30, 6, CONSTANT, of("v1")},
                {36, 12, TYPE_DEFINITION, of("v1")},
                {37, 15, RECORD_FIELD, of("v5")},
                {49, 6, CLASS, of("v1", "v2", "v2")},
                {50, 15, CLASS_FIELD, of("v5")},
                {55, 20, METHOD, of("v3")},
                {55, 69, PARAMETER, of("v4")},
                {65, 16, FUNCTION, of("v3")},
                {70, 11, ANNOTATION, of("v1")},
                {82, 18, CLASS_FIELD, of("v5")},
                {87, 22, RESOURCE_METHOD, of("v3")},
                {96, 11, WORKER, of("v1")},
                {105, 5, ENUM, of("v1", "v5")},
                {109, 4, ENUM_MEMBER, of("v1")},
        };
    }

    @Test
    public void testAnnotOnReturnType() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(57, 85));
        assertEquals(symbol.get().kind(), ANNOTATION);
        assertEquals(symbol.get().getName().get(), "v5");
    }

    @Test(dataProvider = "AnnotRefPosProvider")
    public void testAnnotTypes(int line, int col, String annotName, String typeName) {
        AnnotationSymbol symbol = (AnnotationSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col, annotName,
                                                                              ANNOTATION);

        Optional<TypeSymbol> typeSymbol = symbol.typeDescriptor();

        if (typeName != null) {
            assertTrue(typeSymbol.isPresent());
            assertEquals(typeSymbol.get().typeKind(), TYPE_REFERENCE);
            assertEquals(typeSymbol.get().getName().get(), typeName);
        } else {
            assertTrue(typeSymbol.isEmpty());
        }
    }

    @DataProvider(name = "AnnotRefPosProvider")
    public Object[][] getAnnotRefPos() {
        return new Object[][]{
                {27, 1, "v1", "Annot"},
                {37, 5, "v5", null},
                {50, 5, "v5",  null},
                {55, 29, "v4", "Annot"},
                {115, 5, "v5", null}
        };
    }

    @Test(dataProvider = "FunctionAnnotPosProvider")
    public void functionAnnotationTest(int line, int col, SymbolKind kind) {
        Optional<Symbol> symbol = model.symbol(srcFile, from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), kind);
    }

    @DataProvider(name = "FunctionAnnotPosProvider")
    public Object[][] getFunctionAnnotPos() {
        return new Object[][]{
                {84, 5, ANNOTATION},
                {85, 8, RECORD_FIELD}
        };
    }
}
