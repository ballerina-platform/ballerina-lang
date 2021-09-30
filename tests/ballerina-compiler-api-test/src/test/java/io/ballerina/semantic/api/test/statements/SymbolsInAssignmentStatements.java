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
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.OBJECT_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.RECORD_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for use of symbol() within assignment statements.
 *
 * @since 2.0.0
 */
public class SymbolsInAssignmentStatements {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/statements/symbols_in_assignment_stmt.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PosProvider")
    public void testSymbolsAtCursorPos(int line, int col, String expName, TypeDescKind expTypeKind) {
        VariableSymbol symbol = (VariableSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col, expName, VARIABLE);
        if (symbol.typeDescriptor().typeKind() == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeRefSymbol = (TypeReferenceTypeSymbol) symbol.typeDescriptor();
            assertEquals(typeRefSymbol.typeDescriptor().typeKind(), expTypeKind);
        } else {
            assertEquals(symbol.typeDescriptor().typeKind(), expTypeKind);
        }
    }

    @DataProvider(name = "PosProvider")
    public Object[][] getPosOfSymbols() {
        return new Object[][]{
                // Ordinary assignment
                {21, 4, "intVal", TypeDescKind.INT},
                {22, 4, "person", TypeDescKind.RECORD},
                {24, 4, "person", TypeDescKind.RECORD},
                {38, 8, "self", TypeDescKind.OBJECT},

                // Compound assignment
                {51, 4, "intVal", TypeDescKind.INT},
                {52, 4, "person", TypeDescKind.RECORD},
                {53, 4, "person", TypeDescKind.RECORD},

                // Destructuring assignment
                // {59, 4, "_", TypeDescKind.INT},
                {60, 27, "intVal", TypeDescKind.INT},
                {60, 49, "otherVal", TypeDescKind.ARRAY},
                {61, 18, "valName", TypeDescKind.STRING},
                {61, 32, "valAge", TypeDescKind.INT},
        };
    }

    @Test
    public void testFieldAccessInRecordSymbols() {
        RecordFieldSymbol symbol = (RecordFieldSymbol) assertBasicsAndGetSymbol(model, srcFile, 23, 11, "name", RECORD_FIELD);
        assertEquals(symbol.typeDescriptor().typeKind(), TypeDescKind.STRING);
    }

    @Test
    public void testFieldAccessInClassSymbols() {
        ClassFieldSymbol symbol = (ClassFieldSymbol) assertBasicsAndGetSymbol(model, srcFile, 27, 11, "books", CLASS_FIELD);
        assertEquals(symbol.typeDescriptor().typeKind(), TypeDescKind.INT);
    }

    @Test
    public void testFieldAccessInObjectSymbols() {
        ObjectFieldSymbol symbol = (ObjectFieldSymbol) assertBasicsAndGetSymbol(model, srcFile, 30, 10, "age", OBJECT_FIELD);
        assertEquals(symbol.typeDescriptor().typeKind(), TypeDescKind.INT);
    }

    // utils
    private <clz extends Symbol> void assertSymbol(int line, int col, SymbolKind expKind, String expName, Class clz) {
        Symbol symbol = assertBasicsAndGetSymbol(model, srcFile, line, col, expName, expKind);
        clz sym = (clz) symbol;
    }
}
