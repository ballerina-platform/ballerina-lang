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

    private CompileResult unionCompileResult, unionNegativeResult, tupleCompileResult, tupleNegativeResult;
    private static final String INVALID_CYCLIC_MESSAGE = "invalid cyclic type reference in '[%s]'";

    @BeforeClass
    public void setup() {
        unionCompileResult = BCompileUtil.compile("test-src/typedefs/union-type-definitions-cyclic.bal");
        unionNegativeResult = BCompileUtil.compile("test-src/typedefs/type-definitions-cyclic-negative.bal");
        tupleCompileResult = BCompileUtil.compile("test-src/typedefs/tuple-type-definitions-cyclic.bal");
        tupleNegativeResult = BCompileUtil.compile("test-src/typedefs/tuple-type-definitions-cyclic-negative.bal");
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
                {"testCyclicTypeDefInTuple"},
                {"testComplexCyclicUnion"},
                {"testCyclicUserDefinedType"},
                {"testCyclicUnionAgainstSubSetNegative"},
                {"testImmutableImportedCyclicUnionVariable"},
                {"testCastingToImmutableCyclicUnion"}
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
                {"testCastingToImmutableCyclicUnion"}
        };
    }

    @Test(description = "Negative test cases for union cyclic type definitions")
    public void testUnionCyclicTypeDefNegative() {
        int i = 0;
        BAssertUtil.validateError(unionNegativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "A, A"), 1, 1);
        BAssertUtil.validateError(unionNegativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "B, B"), 3, 1);
        BAssertUtil.validateError(unionNegativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "C, D, C"), 5, 1);
        BAssertUtil.validateError(unionNegativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "D, C, D"), 6, 1);
        BAssertUtil.validateError(unionNegativeResult, i++, "incompatible types: expected 'E', found 'string'", 8, 25);
        BAssertUtil.validateError(unionNegativeResult, i++, "operator '==' not defined for 'CyclicDecimal' and 'float'", 14
                , 20);
        BAssertUtil.validateError(unionNegativeResult, i++, "operator '!=' not defined for 'CyclicDecimal' and 'float'", 15
                , 12);
        BAssertUtil.validateError(unionNegativeResult, i++, "operator '===' not defined for 'CyclicDecimal' and 'float'", 16
                , 12);
        Assert.assertEquals(i, unionNegativeResult.getErrorCount());
    }

    @AfterClass
    public void tearDown() {
        unionNegativeResult = null;
        unionCompileResult = null;
        tupleNegativeResult = null;
        tupleCompileResult = null;
    }
}
