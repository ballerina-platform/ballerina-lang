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
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for variable declaration symbols.
 *
 * @since 2.0.0
 */
public class VarDeclSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/var_decl_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "VariableDeclarationProvider")
    public void testVarDeclarations(int line, int col, String expName, TypeDescKind expTypeKind,
                                    String expDoc, String expAnnot, List<Qualifier> expQuals) {
        VariableSymbol symbol = (VariableSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col,
                                                                          expName, SymbolKind.VARIABLE);
        // check docs (metadata)
        assertTrue(symbol.documentation().isPresent());
        Documentation fieldDocs = symbol.documentation().get();
        if (expDoc != null) {
            assertTrue(fieldDocs.description().isPresent());
            assertEquals(fieldDocs.description().get(), expDoc);
        } else {
            assertTrue(fieldDocs.description().isEmpty());
        }

        // check annotations (metadata)
        List<AnnotationSymbol> fieldAnnots = symbol.annotations();
        if (expAnnot != null) {
            assertEquals(fieldAnnots.size(), 1);
            assertTrue(fieldAnnots.get(0).getName().isPresent());
            assertEquals(fieldAnnots.get(0).getName().get(), expAnnot);
        } else {
            assertEquals(fieldAnnots.size(), 0);
        }

        // check qualifiers
        List<Qualifier> qualifiers = symbol.qualifiers();
        if (expQuals.size() > 0) {
            expQuals.forEach(expQual -> assertTrue(qualifiers.contains(expQual)));
        } else {
            assertTrue(qualifiers.isEmpty());
        }

        // check type
        assertEquals(symbol.typeDescriptor().typeKind(), expTypeKind);
    }

    @DataProvider(name = "VariableDeclarationProvider")
    public Object[][] getVarDeclInfo() {
        return new Object[][]{
                // Simple Binding Pattern
                {18, 7, "s1", TypeDescKind.STRING, "String 1", "varDecl",
                        List.of(Qualifier.ISOLATED)},
                {22, 20, "s2", TypeDescKind.STRING, "String 2", "varDecl",
                        List.of(Qualifier.FINAL, Qualifier.CONFIGURABLE)},
                {26, 13, "s3", TypeDescKind.STRING, "String 3", "varDecl",
                        List.of(Qualifier.FINAL)},
                {30, 10, "s4", TypeDescKind.STRING, "String 4", "varDecl",
                        List.of(Qualifier.FINAL)},
                {34, 13, "s5", TypeDescKind.ANY, "String 5", "varDecl",
                        List.of(Qualifier.ISOLATED)},
                {38, 13, "i1", TypeDescKind.INT, "Int 1", "varDecl",
                        List.of(Qualifier.ISOLATED)},
                {42, 17, "i2", TypeDescKind.INT, "Int 2", "varDecl",
                        List.of(Qualifier.FINAL, Qualifier.CONFIGURABLE)},
                {46, 17, "myObj", TypeDescKind.OBJECT, "Object", "varDecl",
                        List.of(Qualifier.PUBLIC)},
                {50, 35, "func1", TypeDescKind.FUNCTION, "Function 1", "varDecl",
                        List.of(Qualifier.ISOLATED)},
                {54, 21, "func2", TypeDescKind.FUNCTION, "Function 2", "varDecl",
                        List.of(Qualifier.ISOLATED)},

                // Other Binding Patterns
                {58, 30, "intVar", TypeDescKind.INT, null, null,
                        List.of(Qualifier.PUBLIC)},
                {58, 38, "stringVar", TypeDescKind.STRING, null, null,
                        List.of(Qualifier.PUBLIC)},
                {58, 52, "otherValues", TypeDescKind.ARRAY, null, null,
                        List.of(Qualifier.PUBLIC)},
                {62, 25, "a1", TypeDescKind.STRING, null, null,
                        List.of()},
                {62, 34, "a3", TypeDescKind.FLOAT, null, null,
                        List.of()},
                {66, 34, "aValue", TypeDescKind.ANYDATA, null, null,
                        List.of(Qualifier.PUBLIC)},
                {66, 42, "bValue", TypeDescKind.ANYDATA, null, null,
                        List.of(Qualifier.PUBLIC)},
                {70, 8, "valueA", TypeDescKind.STRING, null, null,
                        List.of()},
                {70, 19, "valueB", TypeDescKind.INT, null, null,
                        List.of()},
                {74, 11, "str", TypeDescKind.STRING, null, "varDecl",
                        List.of()},
                {77, 14, "str2", TypeDescKind.STRING, null, "varDecl",
                        List.of(Qualifier.FINAL)},
        };
    }
}
