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

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertList;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for class symbols.
 *
 * @since 2.0.0
 */
public class ClassSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/class_symbols_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test
    public void testSymbolAtCursor() {
        final List<String> fieldNames = List.of("fname", "lname");
        ClassSymbol symbol = (ClassSymbol) model.symbol(srcFile, LinePosition.from(16, 6)).get();

        assertList(symbol.fieldDescriptors(), fieldNames);
        assertList(symbol.methods(), List.of("getFullName"));

        MethodSymbol initMethod = symbol.initMethod().get();
        assertEquals(initMethod.name(), "init");
        assertEquals(
                initMethod.typeDescriptor().parameters().stream().map(p -> p.name().get()).collect(Collectors.toList()),
                fieldNames);
    }

    @Test
    public void testTypeReference() {
        Symbol symbol = model.symbol(srcFile, LinePosition.from(40, 6)).get();
        assertEquals(symbol.name(), "Person1");
        assertEquals(symbol.kind(), TYPE);

        TypeReferenceTypeSymbol tSymbol = (TypeReferenceTypeSymbol) symbol;
        assertEquals(tSymbol.typeKind(), TYPE_REFERENCE);

        ClassSymbol clazz = (ClassSymbol) tSymbol.typeDescriptor();
        assertEquals(clazz.name(), "Person1");
        assertEquals(clazz.kind(), CLASS);
        assertEquals(clazz.typeKind(), OBJECT);
        assertEquals(clazz.initMethod().get().name(), "init");
    }

    @Test
    public void testClassWithoutInit() {
        Symbol symbol = model.symbol(srcFile, LinePosition.from(41, 4)).get();
        assertEquals(symbol.name(), "Person2");
        assertEquals(symbol.kind(), TYPE);

        TypeReferenceTypeSymbol tSymbol = (TypeReferenceTypeSymbol) symbol;
        assertEquals(tSymbol.typeKind(), TYPE_REFERENCE);

        ClassSymbol clazz = (ClassSymbol) tSymbol.typeDescriptor();
        assertEquals(clazz.name(), "Person2");
        assertEquals(clazz.kind(), CLASS);
        assertEquals(clazz.typeKind(), OBJECT);
        assertTrue(clazz.initMethod().isEmpty());
    }

    @Test(dataProvider = "TypeInitPosProvider")
    public void testTypeInit(int line, int col, String name) {
        Symbol symbol = model.symbol(srcFile, LinePosition.from(line, col)).get();
        ClassSymbol clazz = (ClassSymbol) ((TypeReferenceTypeSymbol) symbol).typeDescriptor();
        assertEquals(clazz.name(), name);
    }

    @DataProvider(name = "TypeInitPosProvider")
    public Object[][] getTypeInit() {
        return new Object[][]{
                {40, 17, "Person1"},
                {40, 21, "Person1"},
                {40, 30, "Person1"},
                {41, 17, "Person2"},
                {42, 9, "Person2"},
                {42, 13, "Person2"},
                {42, 21, "Person2"},
        };
    }

    @Test
    public void testDistinctClasses() {
        Symbol symbol = model.symbol(srcFile, LinePosition.from(45, 15)).get();
        ClassSymbol clazz = (ClassSymbol) symbol;
        assertEquals(clazz.typeKind(), OBJECT);
        assertEquals(clazz.kind(), CLASS);
        assertTrue(clazz.qualifiers().contains(Qualifier.DISTINCT));
    }
}
