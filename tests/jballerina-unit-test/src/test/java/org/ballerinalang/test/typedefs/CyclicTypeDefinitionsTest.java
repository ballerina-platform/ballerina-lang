/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.typedefs;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Cyclic type definition test cases.
 */
public class CyclicTypeDefinitionsTest {

    private CompileResult unionCompileResult, negativeResult, tupleCompileResult, cyclicCompileResult;

    @BeforeClass
    public void setup() {
        unionCompileResult = BCompileUtil.compile("test-src/typedefs/union-type-definitions-cyclic.bal");
        negativeResult = BCompileUtil.compile("test-src/typedefs/type-definitions-cyclic-negative.bal");
        cyclicCompileResult = BCompileUtil.compile("test-src/typedefs/cyclic-type-definitions.bal");
        tupleCompileResult = BCompileUtil.compile("test-src/typedefs/tuple-type-definitions-cyclic.bal");
    }

    @Test(dataProvider = "FunctionListCyclicTypeDef")
    public void testCyclicTypeDefinition(String funcName) {
        BRunUtil.invoke(cyclicCompileResult, funcName);
    }

    @Test(description = "Positive tests for tuple cyclic type definitions", dataProvider = "FunctionListTuple")
    public void testTupleCyclicTypeDef(String funcName) {
        BRunUtil.invoke(tupleCompileResult, funcName);
    }

    @DataProvider(name = "FunctionListTuple")
    public Object[][] getTestTupleFunctions() {
        return new Object[][]{
                {"testCycleTypeArray"},
                {"testCycleTypeMap"},
                {"testCycleTypeTable"},
                {"testCyclicAsFunctionParams"},
                {"testCyclicTypeDefInRecord"},
                {"testCyclicTypeDefInUnion"},
                {"testComplexCyclicTuple"},
                {"testCyclicUserDefinedTypes"},
                {"testIndirectRecursion"},
                {"testCyclicRestType"},
                {"testCastingToImmutableCyclicTuple"},
                {"recursiveTupleArrayCloneTest"},
                {"testRecursiveTupleWithRestType"},
                {"testUnionWithCyclicTuplesHashCode"},
                {"testCloneOnRecursiveTuples"},
                {"testCyclicTuples"}
        };
    }

    @DataProvider(name = "FunctionListCyclicTypeDef")
    public Object[][] getTestCyclicTypeDefFunctions() {
        return new Object[][]{
                {"testCyclicRecordTypeDefinition"},
                {"testCyclicFunctionTypeDefinition"},
                {"testCyclicReadonlyFunctionTypeDefinition"},
                {"testCyclicStreamTypeDefinition"},
                {"testCyclicErrorTypeDefinition"},
                {"testCyclicReadonlyErrorTypeDefinition"},
                {"testComplexCyclicRecordTypeDefinition"},
                {"testComplexCyclicRecordTypeDefinition2"},
                {"testCyclicTableTypeDefinition"},
                {"testCyclicMapTypeDefinition"},
                {"testCyclicArrayTypeDefinition"},
                {"testCyclicTableTypeDefinition2"},
                {"testCyclicObjectTypeDefinition"},
                {"testCyclicReadonlyTupleTypeDefinition1"},
                {"testCyclicReadonlyTupleTypeDefinition2"}
        };
    }

    @Test(description = "Positive tests for union cyclic type definitions", dataProvider = "FunctionListUnion")
    public void testUnionCyclicTypeDef(String funcName) {
        BRunUtil.invoke(unionCompileResult, funcName);
    }

    @DataProvider(name = "FunctionListUnion")
    public Object[][] getTestUnionFunctions() {
        return new Object[][]{
                {"testCycleTypeArray"},
                {"testCycleTypeMap"},
                {"testCycleTypeTable"},
                {"testCyclicAsFunctionParams"},
                {"testCyclicTypeDefInRecord"},
                {"testCyclicTypeDefInTuple"},
                {"testComplexCyclicUnion"},
                {"testCyclicUserDefinedType"},
                {"testCyclicUnionAgainstSubSetNegative"},
                {"testImmutableImportedCyclicUnionVariable"},
                {"testCastingToImmutableCyclicUnion"},
                {"testIndirectRecursion"},
                {"testRecursiveTupleTypeDefinitions"}
        };
    }

    @Test(description = "Negative test cases for cyclic type definitions")
    public void testCyclicTypeDefNegative() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[A, A]'", 1, 1);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference not yet supported for 'B'", 3, 8);
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[C, D, C]'", 5, 1);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'E', found 'string'", 8, 25);
        BAssertUtil.validateError(negativeResult, i++, "operator '==' not defined for 'CyclicDecimal' and 'float'", 15
                , 20);
        BAssertUtil.validateError(negativeResult, i++, "operator '!=' not defined for 'CyclicDecimal' and 'float'", 16
                , 12);
        BAssertUtil.validateError(negativeResult, i++, "operator '===' not defined for 'CyclicDecimal' and 'float'", 17
                , 12);
        BAssertUtil.validateError(negativeResult, i++, "operator '==' not defined for 'tupleCyclic' and '[int]'", 20,
                12);
        BAssertUtil.validateError(negativeResult, i++, "operator '!=' not defined for 'tupleCyclic' and '[int]'", 21,
                12);
        BAssertUtil.validateError(negativeResult, i++, "operator '===' not defined for 'tupleCyclic' and '[int]'", 22,
                12);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(int|F)', " +
                "found '[int,string,[int,string]]'", 32, 20);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'I'", 34, 12);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'v'", 37, 19);
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[P, XUnion1, P]'", 39, 1);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'XListRef'", 39, 26);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'XListRef'", 44, 18);
        BAssertUtil.validateError(negativeResult, i++, "redeclared symbol 'J'", 47, 6);
        BAssertUtil.validateError(negativeResult, i++, "redeclared symbol 'K'", 50, 6);
        BAssertUtil.validateError(negativeResult, i++, "redeclared symbol 'N'", 58, 6);
        BAssertUtil.validateError(negativeResult, i++,
                "invalid usage of list constructor: type 'L[2]' does not have a filler value", 62, 11);
        BAssertUtil.validateError(negativeResult, i++,
                "invalid usage of list constructor: type 'L[1]' does not have a filler value", 63, 11);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'ET', found 'int'", 75, 17);
        BAssertUtil.validateError(negativeResult, i++, "invalid intersection type 'B1 & readonly': no intersection",
                79, 9);
        BAssertUtil.validateError(negativeResult, i++, "invalid constraint type. expected subtype of " +
                "'map<any|error>' but found 'B2'", 82, 15);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference not yet supported for 'A3'", 85, 9);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference not yet supported for 'A4'", 89, 9);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference not yet supported for 'A5'", 91, 9);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference not yet supported for 'A6'", 94, 9);
        BAssertUtil.validateError(negativeResult, i++, "invalid constraint type. expected subtype of " +
                "'map<any|error>' but found 'table<A7>'", 97, 15);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference not yet supported for 'A8'", 100, 9);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference not yet supported for 'A9'", 102, 9);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference not yet supported for 'A10'", 104, 10);
        BAssertUtil.validateError(negativeResult, i++, "invalid constraint type. expected subtype of " +
                "'map<any|error>' but found 'A11'", 106, 16);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference not yet supported for 'A12'", 108, 10);
        BAssertUtil.validateError(negativeResult, i++, "cyclic type reference not yet supported for 'A13'", 110, 10);
        Assert.assertEquals(i, negativeResult.getErrorCount());
    }

    @AfterClass
    public void tearDown() {
        negativeResult = null;
        unionCompileResult = null;
        cyclicCompileResult = null;
        tupleCompileResult = null;
    }
}
