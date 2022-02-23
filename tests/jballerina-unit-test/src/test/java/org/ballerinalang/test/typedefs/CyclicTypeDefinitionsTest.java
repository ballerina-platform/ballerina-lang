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

    private CompileResult unionCompileResult, negativeResult, tupleCompileResult;
    private static final String INVALID_CYCLIC_MESSAGE = "invalid cyclic type reference in '[%s]'";

    @BeforeClass
    public void setup() {
        unionCompileResult = BCompileUtil.compile("test-src/typedefs/union-type-definitions-cyclic.bal");
        negativeResult = BCompileUtil.compile("test-src/typedefs/type-definitions-cyclic-negative.bal");
        tupleCompileResult = BCompileUtil.compile("test-src/typedefs/tuple-type-definitions-cyclic.bal");
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
                {"testIndirectRecursion"}
        };
    }

    @Test(description = "Negative test cases for cyclic type definitions")
    public void testCyclicTypeDefNegative() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "A, A"), 1, 1);
        BAssertUtil.validateError(negativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "B, B"), 3, 1);
        BAssertUtil.validateError(negativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "C, D, C"), 5, 1);
        BAssertUtil.validateError(negativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "D, C, D"), 6, 1);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'E', found 'string'", 8, 25);
        BAssertUtil.validateError(negativeResult, i++, "operator '==' not defined for 'CyclicDecimal' and 'float'", 15
                , 20);
        BAssertUtil.validateError(negativeResult, i++, "operator '!=' not defined for 'CyclicDecimal' and 'float'", 16
                , 12);
        BAssertUtil.validateError(negativeResult, i++, "operator '===' not defined for 'CyclicDecimal' and 'float'", 17
                , 12);
        BAssertUtil.validateError(negativeResult, i++, "operator '==' not defined for " +
                "'[int,tupleCyclic[]]' and '[int]'", 20, 12);
        BAssertUtil.validateError(negativeResult, i++, "operator '!=' not defined for " +
                "'[int,tupleCyclic[]]' and '[int]'", 21, 12);
        BAssertUtil.validateError(negativeResult, i++, "operator '===' not defined for " +
                "'[int,tupleCyclic[]]' and '[int]'", 22, 12);
        BAssertUtil.validateError(negativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "G, G"), 25, 1);
        BAssertUtil.validateError(negativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "H, H"), 26, 1);
        BAssertUtil.validateError(negativeResult, i++, "invalid cyclic type reference in '[Q, Q]'",
                29, 1);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                "'(int|[int,string,([int,string,...,map<F>]|int),map<F>])', found '[int,string,[int,string]]'", 32, 20);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', " +
                "found '[int,I[]]'", 34, 12);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'v'", 37, 19);
        BAssertUtil.validateError(negativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "P, XUnion1, P"),
                39, 1);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'XListRef'", 39, 26);
        BAssertUtil.validateError(negativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "XUnion1, P, XUnion1"),
                42, 1);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'XListRef'", 44, 18);
        BAssertUtil.validateError(negativeResult, i++, "redeclared symbol 'J'", 47, 6);
        BAssertUtil.validateError(negativeResult, i++, "redeclared symbol 'K'", 50, 6);
        BAssertUtil.validateError(negativeResult, i++, "redeclared symbol 'N'", 58, 6);
        BAssertUtil.validateError(negativeResult, i++, "tuple and expression size does not match", 62, 11);
        BAssertUtil.validateError(negativeResult, i++, "tuple and expression size does not match", 63, 11);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'ET', found 'int'", 75, 17);
        Assert.assertEquals(i, negativeResult.getErrorCount());
    }

    @AfterClass
    public void tearDown() {
        negativeResult = null;
        unionCompileResult = null;
        tupleCompileResult = null;
    }
}
