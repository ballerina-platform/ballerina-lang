/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This contains methods to test nested query expressions.
 *
 * @since 2201.7.0
 */
public class CollectClauseTest {
    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/collect_clause.bal");
        negativeResult = BCompileUtil.compile("test-src/query/collect_clause_negative.bal");
    }

    @Test(dataProvider = "dataToTestCollectClause")
    public void testGroupByClauseWithListCtr(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestCollectClause() {
        return new Object[]{
                "testListConstructor",
                "testInvocation",
                "testEmptyGroups1",
                "testEmptyGroups2",
                "testGroupByAndCollectInSameQuery",
                "testMultipleCollect",
                "testDoClause",
                "testErrorSeq"
        };
    }

    @Test
    public void testNegativeCases() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found '[int...]'", 19, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string[]', found '[int...]'",
                21, 29);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 26, 29);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int[]', found 'int'", 28, 29);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int[]', found 'seq string'",
                33, 33);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 35, 45);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string[]', found 'seq int'",
                37, 39);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string[]', found 'seq int'",
                39, 43);
        BAssertUtil.validateError(negativeResult, i++, "undefined function 'join'", 41, 29);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'seq string'",
                46, 25);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 46, 25);
        BAssertUtil.validateError(negativeResult, i++, "operator '+' not defined for 'seq int' and 'int'", 48, 25);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 48, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| int x; |}', " +
                "found 'record {| [int...] x; |}'", 50, 41);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int[6]', found '[int...]'",
                55, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| int[6] intArr; |}', " +
                "found 'record {| [int...] intArr; |}'", 57, 49);
        BAssertUtil.validateError(negativeResult, i++, "undefined module 'foo'", 62, 29);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'seq int'", 67, 29);
        BAssertUtil.validateError(negativeResult, i++, "undefined function 'sumy'", 67, 29);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'seq int'", 69, 29);
        BAssertUtil.validateError(negativeResult, i++, "user defined functions are not allowed when arguments " +
                "contain aggregated variable", 69, 29);
        BAssertUtil.validateError(negativeResult, i++, "query construct types cannot be used with collect clause",
                79, 16);
        BAssertUtil.validateError(negativeResult, i++, "query construct types cannot be used with collect clause",
                81, 16);
        BAssertUtil.validateError(negativeResult, i++, "query construct types cannot be used with collect clause",
                83, 21);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'map<int>', found 'int'", 84, 29);
        BAssertUtil.validateError(negativeResult, i++, "query construct types cannot be used with collect clause",
                85, 16);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 91, 25);
        BAssertUtil.validateError(negativeResult, i++, "operator '+' not defined for 'seq int' and 'int'", 96, 26);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 96, 26);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', " +
                "found '[seq int,seq int]'", 98, 33);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 98, 34);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 98, 42);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'seq int'", 107, 25);
        BAssertUtil.validateError(negativeResult, i++, "user defined functions are not allowed when arguments" +
                " contain aggregated variable", 107, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int[]', found '[seq int]'",
                109, 25);
        BAssertUtil.validateError(negativeResult, i++, "user defined functions are not allowed when arguments" +
                " contain aggregated variable", 109, 26);
        BAssertUtil.validateError(negativeResult, i++, "'price1' is sequenced more than once", 123, 26);
        BAssertUtil.validateError(negativeResult, i++, "'name' is sequenced more than once", 128, 43);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string[]', " +
                "found 'seq seq string'", 128, 43);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int?'", 134, 25);
        BAssertUtil.validateError(negativeResult, i++, "arguments not allowed after seq argument", 139, 41);
        BAssertUtil.validateError(negativeResult, i++, "arguments not allowed after seq argument", 143, 41);
        BAssertUtil.validateError(negativeResult, i++, "arguments not allowed after seq argument", 147, 41);
        BAssertUtil.validateError(negativeResult, i++, "operator '+' not defined for 'seq int' and 'seq int'",
                150, 33);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 150, 33);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 150, 42);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                "'([int,int...]|record {| int n; |})', found '[int...]'", 154, 36);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }
}
