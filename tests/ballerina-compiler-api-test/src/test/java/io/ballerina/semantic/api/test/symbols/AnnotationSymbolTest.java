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
import io.ballerina.compiler.api.impl.symbols.BallerinaFunctionSymbol;
import io.ballerina.compiler.api.symbols.Annotatable;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ExternalFunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
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
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleTestSource;
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

    private SemanticModel testProjectModel;
    private Document testFile;
    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/annotation_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);

        Project testProject = BCompileUtil.loadProject("test-src/annotations_in_tests_project/");
        testProjectModel = getDefaultModulesSemanticModel(testProject);
        testFile = getDocumentForSingleTestSource(testProject);
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
                {31, 6, CONSTANT, of("v1")},
                {37, 12, TYPE_DEFINITION, of("v1")},
                {38, 15, RECORD_FIELD, of("v5")},
                {50, 6, CLASS, of("v1", "v2", "v2")},
                {51, 15, CLASS_FIELD, of("v5")},
                {56, 20, METHOD, of("v3")},
                {56, 69, PARAMETER, of("v4")},
                {66, 16, FUNCTION, of("v3")},
                {71, 11, ANNOTATION, of("v1")},
                {83, 18, CLASS_FIELD, of("v5")},
                {88, 22, RESOURCE_METHOD, of("v3")},
                {97, 11, WORKER, of("v1")},
                {106, 5, ENUM, of("v1", "v5")},
                {110, 4, ENUM_MEMBER, of("v1")},
                {110, 4, ENUM_MEMBER, of("v1")},
        };
    }

    @Test(dataProvider = "ExternalPosProvider")
    public void testAnnotationsOnExternal(int line, int col, SymbolKind kind) {
        Optional<Symbol> symbol = model.symbol(srcFile, from(line, col));
        assertEquals(symbol.get().kind(), kind);
        List<AnnotationAttachmentSymbol> annotSymbols = ((ExternalFunctionSymbol) symbol.get()).annotAttachmentsOnExternal();
        Assert.assertFalse(annotSymbols.isEmpty());
        Assert.assertNotNull(annotSymbols.get(0));
    }

    @DataProvider(name = "ExternalPosProvider")
    public Object[][] getExternalPos() {
        return new Object[][]{
                {146, 9, FUNCTION},
                {139, 9, FUNCTION}
        };
    }

    @Test
    public void testAnnotOnReturnType() {
        Optional<Symbol> symbol = model.symbol(srcFile, from(58, 85));
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
                {28, 1, "v1", "Annot"},
                {38, 5, "v5", null},
                {51, 5, "v5",  null},
                {56, 29, "v4", "Annot"},
                {116, 5, "v5", null}
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
                {85, 5, ANNOTATION},
                {86, 8, RECORD_FIELD}
        };
    }

    @Test(dataProvider = "TestPosProvider")
    public void testInTestDir(int line, int col, SymbolKind kind, List<String> annots) {
        Optional<Symbol> symbol = testProjectModel.symbol(testFile, from(line, col));
        assertEquals(symbol.get().kind(), kind);

        List<AnnotationSymbol> annotSymbols = ((Annotatable) symbol.get()).annotations();

        assertEquals(annotSymbols.size(), annots.size());
        for (int i = 0; i < annotSymbols.size(); i++) {
            assertEquals(annotSymbols.get(i).getName().get(), annots.get(i));
        }
    }

    @DataProvider(name = "TestPosProvider")
    public Object[][] getPosTests() {
        return new Object[][]{
                {37, 11, RECORD_FIELD, of("Meta")},
                {44, 6, CONSTANT, of("v1")},
                {50, 12, TYPE_DEFINITION, of("v1")},
                {51, 15, RECORD_FIELD, of("v5")},
                {60, 6, CLASS, of("v2", "v2")},
                {61, 15, CLASS_FIELD, of("v5")},
                {66, 20, METHOD, of("v3")},
                {66, 69, PARAMETER, of("v4")},
                {76, 16, FUNCTION, of("v3")},
                {84, 11, WORKER, of("v1")},
        };
    }

    @Test(dataProvider = "AnnotRefTestsPosProvider")
    public void testAnnotInTestTypes(int line, int col, String annotName, String typeName) {
        AnnotationSymbol symbol =
                (AnnotationSymbol) assertBasicsAndGetSymbol(testProjectModel, testFile, line, col, annotName,
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

    @DataProvider(name = "AnnotRefTestsPosProvider")
    public Object[][] getTestsAnnotRefPos() {
        return new Object[][]{
                {46, 1, "v1", "Annot"},
                {66, 29, "v4", "Annot"},
        };
    }

}
