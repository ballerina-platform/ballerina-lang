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
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.semantic.api.test.SymbolBIRTest;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for union type symbols.
 *
 * @since 2.0.0
 */
public class UnionTypeSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/union_type_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "TestData")
    public void testUnionTypeMemberTypes(int line, int col, String name, List<TypeDescKind> expTypeList) {
        TypeDefinitionSymbol symbol = (TypeDefinitionSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col, name,
                                                                                      SymbolKind.TYPE_DEFINITION);

        assertEquals(symbol.typeDescriptor().typeKind(), TypeDescKind.UNION);
        UnionTypeSymbol typeSymbol = (UnionTypeSymbol) symbol.typeDescriptor();

        List<TypeSymbol> memberTypeDescriptors = typeSymbol.memberTypeDescriptors();
        List<TypeDescKind> memberTypeDescKindList = memberTypeDescriptors
                                                            .stream()
                                                            .map(TypeSymbol::typeKind)
                                                            .collect(Collectors.toList());

        assertList(memberTypeDescKindList, expTypeList);
    }

    @DataProvider(name = "TestData")
    public Object[][] getPosition() {
        return new Object[][]{
                {16, 5, "T1",
                        List.of(TypeDescKind.INT, TypeDescKind.STRING, TypeDescKind.BOOLEAN)},
                {18, 5, "T2",
                        List.of(TypeDescKind.SINGLETON, TypeDescKind.SINGLETON, TypeDescKind.SINGLETON)},
                {20, 5, "T3",
                        List.of(TypeDescKind.SINGLETON, TypeDescKind.SINGLETON, TypeDescKind.SINGLETON)},
                {22, 5, "T4",
                        List.of(TypeDescKind.TYPE_REFERENCE, TypeDescKind.SINGLETON, TypeDescKind.SINGLETON)},
                {24, 5, "T5",
                        List.of(TypeDescKind.SINGLETON, TypeDescKind.SINGLETON, TypeDescKind.SINGLETON,
                                TypeDescKind.SINGLETON)},
                {26, 5, "T6",
                        List.of(TypeDescKind.INT, TypeDescKind.STRING, TypeDescKind.BOOLEAN,
                                TypeDescKind.TYPE_REFERENCE)},
                {28, 5, "T7",
                        List.of(TypeDescKind.TUPLE, TypeDescKind.OBJECT)},
                {35, 5, "T8",
                        List.of(TypeDescKind.MAP, TypeDescKind.TYPE_REFERENCE)},
                {37, 5, "T9",
                        List.of(TypeDescKind.ARRAY, TypeDescKind.MAP, TypeDescKind.TYPE_REFERENCE)},
        };
    }

    public static void assertList(List<TypeDescKind> actualValues, List<TypeDescKind> expectedValues) {
        assertEquals(actualValues.size(), expectedValues.size());

        for (TypeDescKind val : expectedValues) {
            assertTrue(actualValues.stream().anyMatch(val::equals));
        }
    }
}