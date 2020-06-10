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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Cyclic type definition test cases.
 */
public class CyclicTypeDefinitionsTest {

    private static CompileResult compileResult, negativeResult;
    private static final String INVALID_CYCLIC_MESSAGE = "invalid cyclic type reference in '[%s]'";

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/typedefs/type-definitions-cyclic.bal");
        negativeResult = BCompileUtil.compile("test-src/typedefs/type-definitions-cyclic-negative.bal");
    }

    @Test(description = "Positive tests for cyclic type definitions", dataProvider = "FunctionList")
    public void testCyclicTypeDef(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @DataProvider(name = "FunctionList")
    public Object[][] getTestFunctions() {
        return new Object[][]{
                {"testCycleTypeArray"},
                {"testCycleTypeMap"},
                {"testCycleTypeTable"},
                {"testCyclicAsFunctionParams"},
                {"testCyclicTypeDefInRecord"},
                {"testCyclicTypeDefInTuple"},
                {"testComplexCyclicUnion"},
                {"testCyclicUserDefinedType"},
        };
    }

    @Test(description = "Negative test cases for cyclic type definitions")
    public void testCyclicTypeDefNegative() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "A, A"), 1, 1);
        BAssertUtil.validateError(negativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "B, B"), 3, 1);
        BAssertUtil.validateError(negativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "C, D, C"), 5, 1);
        BAssertUtil.validateError(negativeResult, i++, String.format(INVALID_CYCLIC_MESSAGE, "D, C, D"), 6, 1);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                "'(int|record {| E a; anydata...; |})', found 'string'", 8, 25);
    }
}
