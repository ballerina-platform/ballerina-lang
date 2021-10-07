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
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for use of symbol() within local variable declarations.
 *
 * @since 2.0.0
 */
public class SymbolsInLocalVarDeclStatementsTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/statements/symbols_in_local_var_decl_stmt.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "PosProvider")
    public void testSymbols(int line, int col, String expName, TypeDescKind expTypeKind, boolean isAnnotated,
                            boolean isFinal) {
        VariableSymbol symbol =
                (VariableSymbol) assertBasicsAndGetSymbol(model, srcFile, line, col, expName, VARIABLE);

        // check annotations (metadata)
        List<AnnotationSymbol> fieldAnnots = symbol.annotations();
        if (isAnnotated) {
            // TODO: Annotations in local-var-decl is not working (affects [22, 17] & [25, 14])
//            assertEquals(fieldAnnots.size(), 1);
//            assertEquals(fieldAnnots.get(0).getName().get(), "annot");
        } else {
            assertEquals(fieldAnnots.size(), 0);
        }

        // check qualifiers
        List<Qualifier> qualifiers = symbol.qualifiers();
        if (isFinal) {
            assertTrue(qualifiers.contains(Qualifier.FINAL));
        } else {
            assertTrue(qualifiers.isEmpty());
        }

        // check type descriptor
        assertEquals(symbol.typeDescriptor().typeKind(), expTypeKind);
    }

    @DataProvider(name = "PosProvider")
    public Object[][] getPos() {
        return new Object[][]{
                {17, 14, "integer", TypeDescKind.INT, false, true},
                {19, 11, "str", TypeDescKind.STRING, false, false},
                {22, 17, "person", TypeDescKind.TYPE_REFERENCE, true, true},
                {25, 14, "foo", TypeDescKind.INT, true, true},
                {27, 27, "intVar", TypeDescKind.INT, false, false},
                {27, 50, "otherVal", TypeDescKind.ARRAY, false, false},
                {29, 29, "a1", TypeDescKind.STRING, false, false},
                {31, 42, "valName", TypeDescKind.STRING, false, false},
                {33, 10, "dept", TypeDescKind.TYPE_REFERENCE, false, false},
                {35, 19, "jam", TypeDescKind.UNION, false, false},
        };
    }
}
