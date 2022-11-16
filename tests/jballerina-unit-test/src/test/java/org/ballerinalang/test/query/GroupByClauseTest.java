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
package org.ballerinalang.test.query;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This contains methods to test group by clause in query expression.
 *
 * @since 2201.4.0
 */

public class GroupByClauseTest {

//    @Test(dataProvider = "groupby-positive-data-provider")
//    public void testGroupByWithVarRef(CompileResult result, String functionName) {
//        BRunUtil.invoke(result, functionName);
//    }
//
//    @DataProvider(name = "groupby-positive-data-provider")
//    public Object[][] groupByPositiveTestDataProvider() {
//        CompileResult result = BCompileUtil.compile("test-src/query/group-by-clause.bal");
//
//        return new Object[][]{
//                {result, "testGroupByWithVarRef"},
//                {result, "testGroupByWithVarDef"},
//                {result, "testGroupByWithVarDefAndVarRef"},
//                {result, "testNonGroupingKeyInFunctionContext"},
//                {result, "testNonGroupingKeyInListConstructorContext"}
//        };
//    }

    @Test(description = "Test negative scenarios for group by clause")
    public void testNegativeScenarios() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/query/group-by-clause-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 55);
        int index = 0;

        validateError(negativeResult, index++, "undefined symbol 'year'", 40, 18);
        validateError(negativeResult, index++, "'string' sequence binding used in " +
                "invalid context", 41, 18);
        //TODO:FIX AND REVISIT
        validateError(negativeResult, index++, "incompatible types: expected 'string', found 'int'",
                46, 36);
        //TODO:FIX AND REVISIT
        validateError(negativeResult, index++, "operator '+' not defined for 'int' and 'string'",
                54, 36);

        validateError(negativeResult, index++, "arguments not allowed after sequence binding argument",
                65, 36);

        validateError(negativeResult, index++, "undefined function 'sum'",
                69, 36);
        validateError(negativeResult, index++, "'int' sequence binding used in invalid context",
                69, 36);

        validateError(negativeResult, index++, "undefined function 'sum'",
                73, 36);

        validateError(negativeResult, index++, "arguments not allowed after sequence binding argument",
                77, 36);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'int[]'",
                77, 36);

        validateError(negativeResult, index++, "undefined function 'sum'",
                81, 36);
        validateError(negativeResult, index++, "'string' sequence binding used in invalid context",
                81, 36);

        validateError(negativeResult, index++, "undefined function 'sum'",
                85, 36);

        validateError(negativeResult, index++, "undefined function 'sum'",
                90, 36);

        validateError(negativeResult, index++, "undefined function 'sum'",
                95, 36);

        validateError(negativeResult, index++, "undefined function 'sum'",
                99, 36);
        validateError(negativeResult, index++, "'int' sequence binding used in invalid context",
                99, 36);

        validateError(negativeResult, index++, "undefined function 'startsWith'",
                103, 36);
        validateError(negativeResult, index++, "'string' sequence binding used in invalid context",
                103, 36);

        validateError(negativeResult, index++, "undefined function 'toHexString'",
                107, 36);
        validateError(negativeResult, index++, "'int' sequence binding used in invalid context",
                107, 36);

        validateError(negativeResult, index++, "'price2' that contains a sequence binding cannot " +
                "be used as an argument in user defined function 'userDefinedFunc1'", 115, 36);

        validateError(negativeResult, index++, "undefined function 'undefinedFunc'",
                120, 36);
        validateError(negativeResult, index++, "'int' sequence binding used in invalid context",
                120, 36);

        validateError(negativeResult, index++, "undefined function 'undefinedFunc'",
                125, 36);
        validateError(negativeResult, index++, "'int' sequence binding used in invalid context",
                125, 36);

        validateError(negativeResult, index++, "undefined function 'undefinedFunc'",
                130, 36);
        validateError(negativeResult, index++, "'int' sequence binding used in invalid context",
                130, 36);

        validateError(negativeResult, index++, "'price2' that contains a sequence binding cannot be " +
                "used as an argument in user defined function 'userDefinedFunc2'", 135, 36);

        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'int' sequence",
                152, 36);

        validateError(negativeResult, index++, "incompatible types: expected 'int[]', found 'int' sequence",
                157, 36);

        validateError(negativeResult, index++, "arguments not allowed after sequence binding argument",
                165, 36);

        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'int' sequence",
                169, 36);

        validateError(negativeResult, index++, "arguments not allowed after sequence binding argument",
                173, 36);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'int[]'",
                173, 36);

        validateError(negativeResult, index++, "incompatible types: expected 'int[]', found 'string[]'",
                177, 36);

        validateError(negativeResult, index++, "'int' sequence binding used in invalid context",
                181, 36);

        validateError(negativeResult, index++, "incompatible types: expected 'string', found 'boolean'",
                185, 36);
        validateError(negativeResult, index++, "incompatible types: expected 'string', " +
                "found 'string' sequence", 185, 36);

        validateError(negativeResult, index++, "incompatible types: expected 'int', " +
                "found 'int' sequence", 189, 36);

        validateError(negativeResult, index++, "incompatible types: expected " +
                "'(any|error)[]', found 'int' sequence", 193, 36);

        validateError(negativeResult, index++, "undefined function 'sum'", 197, 36);
        validateError(negativeResult, index++, "undefined module 'undefined'", 197, 36);
        validateError(negativeResult, index++, "'int' sequence binding used " +
                "in invalid context", 197, 36);

        validateError(negativeResult, index++, "'int' sequence binding used in invalid context",
                205, 36);

        validateError(negativeResult, index++, "'int' sequence binding used in invalid context",
                209, 36);

        validateError(negativeResult, index++, "'int' sequence binding used in invalid context",
                213, 36);

        validateError(negativeResult, index++, "invalid usage of spread operator: fixed " +
                "length list expected", 221, 36);

        //TODO:CHECK
        validateError(negativeResult, index++, "incompatible types: expected '[int[]]'," +
                " found '[int[]][]'", 223, 36);
        //TODO:CHECK
        validateError(negativeResult, index++, "sequence binding without an expected type or with" +
                " only a readonly type is not supported in this version", 229, 36);
        //TODO:CHECK
        validateError(negativeResult, index++, "sequence binding without an expected type or " +
                "with only a readonly type is not supported in this version", 233, 36);

        //TODO readonly,intersection,typerefdec,typedef?


























    }
}
