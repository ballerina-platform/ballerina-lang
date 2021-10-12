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

package io.ballerina.semantic.api.test.typeof;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TABLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.UNION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests cases for testing typeOf() with query expressions.
 */
public class TypeOfQueryExprTest {

    private SemanticModel model;

    @BeforeClass
    public void setup() {
        model = SemanticAPITestUtils.getDefaultModulesSemanticModel(
                "test-src/symbols/symbols_in_query_exprs_test.bal");
    }

    @Test(dataProvider = "QueryExprPos")
    public void testQueryExprs(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        assertType(sLine, sCol, eLine, eCol, kind);
    }

    @DataProvider(name = "QueryExprPos")
    public Object[][] getPos() {
        return new Object[][]{
                // Basics
                {21, 18, 21, 89, ARRAY},
                {21, 29, 21, 31, null},
                {21, 33, 21, 41, ARRAY},
                {21, 18, 21, 41, null},
                {21, 54, 21, 59, INT},
                {21, 49, 21, 89, TYPE_REFERENCE},
                {22, 13, 22, 92, ARRAY},
                {22, 44, 22, 92, TYPE_REFERENCE},
                // Where clause
                {26, 22, 26, 30, STRING},
                // Let clause
                {30, 13, 32, 43, ARRAY},
                {31, 51, 31, 59, STRING},
                // Join clause
                {36, 30, 36, 34, null},
                {36, 39, 36, 45, ARRAY},
                {36, 49, 36, 54, INT},
                // Order by clause
                {41, 16, 41, 30, null},
                {41, 25, 41, 30, INT},
                // Limit clause
                {46, 22, 46, 27, INT},
                // On conflict clause
                {52, 43, 52, 80, ERROR},
        };
    }

    @Test(dataProvider = "ExprPos1")
    public void testQueryExprInDetail1(int sLine, int sCol, int eLine, int eCol) {
        ArrayTypeSymbol type = (ArrayTypeSymbol) assertType(sLine, sCol, eLine, eCol, ARRAY);
        assertEquals(type.memberTypeDescriptor().typeKind(), TYPE_REFERENCE);
        assertEquals(type.memberTypeDescriptor().getName().get(), "Person");
    }

    @DataProvider(name = "ExprPos1")
    public Object[][] getExprPos1() {
        return new Object[][]{
                {21, 18, 21, 89},
                {22, 13, 22, 92},
        };
    }

    @Test(dataProvider = "ExprPos2")
    public void testQueryExprInDetail2(int sLine, int sCol, int eLine, int eCol) {
        TypeReferenceTypeSymbol type = (TypeReferenceTypeSymbol) assertType(sLine, sCol, eLine, eCol, TYPE_REFERENCE);
        assertEquals(type.getName().get(), "Person");
        assertEquals(type.typeDescriptor().typeKind(), RECORD);
    }

    @DataProvider(name = "ExprPos2")
    public Object[][] getExprPos2() {
        return new Object[][]{
                {21, 49, 21, 89},
                {22, 44, 22, 92},
        };
    }

    @Test
    public void testTableKeySpecifier() {
        UnionTypeSymbol type = (UnionTypeSymbol) assertType(50, 37, 52, 80, UNION);
        assertEquals(type.userSpecifiedMemberTypes().size(), 2);
        assertEquals(type.userSpecifiedMemberTypes().get(0).typeKind(), TABLE);
        assertEquals(type.userSpecifiedMemberTypes().get(1).typeKind(), ERROR);
    }

    private TypeSymbol assertType(int sLine, int sCol, int eLine, int eCol, TypeDescKind kind) {
        Optional<TypeSymbol> type = model.typeOf(
                LineRange.from("symbols_in_query_exprs_test.bal", LinePosition.from(sLine, sCol),
                               LinePosition.from(eLine, eCol)));

        if (kind == null) {
            assertTrue(type.isEmpty());
            return null;
        }

        assertEquals(type.get().typeKind(), kind);
        return type.get();
    }
}
