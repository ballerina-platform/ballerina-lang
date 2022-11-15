/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TupleAndArrayCyclicTypeDefinitionTest {
    private CompileResult tupleCompileResult, arrayCompileResult;

    @BeforeClass
    public void setup() {
        //tupleCompileResult = BCompileUtil.compile("test-src/typedefs/tuple-type-definitions-cyclic.bal");
        arrayCompileResult = BCompileUtil.compile("test-src/typedefs/array_type_definitions_cyclic.bal");
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
                {"testUnionWithCyclicTuplesHashCode"}
        };
    }

    @Test(description = "Positive tests for array cyclic type definitions", dataProvider = "FunctionListArray")
    public void testArrayCyclicTypeDef(String funcName) {
        BRunUtil.invoke(arrayCompileResult, funcName);
    }

    @DataProvider(name = "FunctionListArray")
    public Object[][] getTestArrayFunctions() {
        return new Object[][]{
                {"testCyclicArrayTypeDefinitions"},
                {"testCyclicArrayTypeDefinitions2"},
//                {"testCyclicArrayTypeDefinitions3"},
                {"testCyclicMapDefinitions"}
        };
    }

    @AfterClass
    public void tearDown() {
        tupleCompileResult = null;
        arrayCompileResult = null;
    }
}
