/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
public class GroupByClauseTest {
    private CompileResult resultWithListCtr;
    private CompileResult resultWithInvocation;
    private CompileResult negativeResult;
    private CompileResult negativeSemanticResult;

    @BeforeClass
    public void setup() {
        resultWithListCtr = BCompileUtil.compile("test-src/query/group_by_clause_with_list_ctr.bal");
        resultWithInvocation = BCompileUtil.compile("test-src/query/group_by_clause_with_invocation.bal");
        negativeResult = BCompileUtil.compile("test-src/query/group_by_clause_negative.bal");
        negativeSemanticResult = BCompileUtil.compile("test-src/query/group_by_clause_negative_semantic.bal");
    }

    @Test(dataProvider = "dataToTestGroupByClauseWithListCtr")
    public void testGroupByClauseWithListCtr(String functionName) {
        BRunUtil.invoke(resultWithListCtr, functionName);
    }

    @DataProvider
    public Object[] dataToTestGroupByClauseWithListCtr() {
        return new Object[] {
                "testGroupByExpressionAndSelectWithGroupingKeys1",
                "testGroupByExpressionAndSelectWithGroupingKeys2",
                "testGroupByExpressionAndSelectWithGroupingKeys3",
                "testGroupByExpressionAndSelectWithGroupingKeys4",
                "testGroupByExpressionAndSelectWithGroupingKeys5",
                "testGroupByExpressionAndSelectWithGroupingKeys6",
                "testGroupByExpressionAndSelectWithGroupingKeys7",
                "testGroupByExpressionAndSelectWithGroupingKeys8",
                "testGroupByExpressionAndSelectWithGroupingKeys9",
                "testGroupByExpressionAndSelectWithGroupingKeys12",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause1",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause2",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause3",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause4",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause5",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause6",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause7",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause8",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause9",
                "testGroupByExpressionAndSelectWithGroupingKeysFromClause1",
                "testGroupByExpressionAndSelectWithGroupingKeysFromClause2",
                "testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause1",
                "testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause2",
                "testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause3",
                "testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause4",
                "testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause5",
                "testGroupByExpressionAndSelectWithGroupingKeysWithOrderbyClause1",
                "testGroupByExpressionAndSelectWithGroupingKeysWithOrderbyClause2",
                "testGroupByExpressionAndSelectWithGroupingKeysWithOrderbyClause3",
                "testGroupByExpressionAndSelectWithGroupingKeysWithLimitClause",
                "testGroupByExpressionAndSelectWithGroupingKeysWithTableResult",
                "testGroupByExpressionAndSelectWithGroupingKeysWithMapResult",
                "testGroupByExpressionAndSelectWithGroupingKeysWithFromClause",
                "testGroupByVarDefsAndSelectWithGroupingKeys1",
                "testGroupByVarDefsAndSelectWithGroupingKeys2",
                "testGroupByVarDefsAndSelectWithGroupingKeys3",
                "testGroupByVarDefsAndSelectWithGroupingKeys4",
                "testGroupByVarDefsAndSelectWithGroupingKeys5",
                "testGroupByVarDefsAndSelectWithGroupingKeys6",
                "testGroupByVarDefsAndSelectWithGroupingKeys7",
                "testGroupByVarDefsAndSelectWithGroupingKeys8",
                "testGroupByVarDefsAndSelectWithGroupingKeys9",
                "testGroupByVarDefsAndSelectWithGroupingKeys10",
                "testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause1",
                "testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause2",
                "testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause3",
                "testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause4",
                "testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause5",
                "testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause6",
                "testGroupByVarDefsAndSelectWithGroupingKeysAndWhereClause7",
                "testGroupByVarDefsAndSelectWithGroupingKeysWithJoinClause3",
                "testGroupByVarDefsAndSelectWithGroupingKeysWithJoinClause4",
                "testGroupByVarDefsAndSelectWithGroupingKeysWithJoinClause5",
                "testGroupByVarDefsAndSelectWithGroupingKeysWithOrderbyClause1",
                "testGroupByVarDefsAndSelectWithGroupingKeysWithOrderbyClause2",
                "testGroupByVarDefsAndSelectWithGroupingKeysWithOrderbyClause3",
                "testGroupByVarDefsAndSelectWithGroupingKeysWithLimitClause",
                "testGroupByVarDefsAndSelectWithGroupingKeysWithTableResult",
                "testGroupByVarDefsAndSelectWithGroupingKeysWithMapResult",
                "testGroupByVarDefsAndSelectWithGroupingKeysWithFromClause",

                "testGroupByExpressionAndSelectWithNonGroupingKeys2",
                "testGroupByExpressionAndSelectWithNonGroupingKeys3",
                "testGroupByExpressionAndSelectWithNonGroupingKeys4",
                "testGroupByExpressionAndSelectWithNonGroupingKeys5",
                "testGroupByExpressionAndSelectWithNonGroupingKeys6",
                "testGroupByExpressionAndSelectWithNonGroupingKeys7",
                "testGroupByVarDefsAndSelectWithNonGroupingKeys1",
                "testGroupByVarDefsAndSelectWithNonGroupingKeys2",
                "testGroupByVarDefsAndSelectWithNonGroupingKeys3",

                "testGroupByExpressionWithStreamOutput",
                "testGroupByExpressionWithStringOutput1",
                "testGroupByExpressionWithTableOutput",
                "testGroupByExpressionWithMapOutput",

                "testGroupByWithDoClause",

                "testGroupByExpressionAndSelectWithNonGroupingKeys8",
                "testGroupByExpressionAndSelectWithNonGroupingKeys9",
                "testGroupByExpressionAndSelectWithNonGroupingKeys10",
                "testGroupByExpressionAndSelectWithNonGroupingKeys11",
                "testGroupByExpressionAndSelectWithNonGroupingKeys12",
                "testGroupByExpressionAndSelectWithNonGroupingKeys13",
                "testGroupByExpressionAndSelectWithNonGroupingKeys14",
                "testGroupByExpressionAndSelectWithNonGroupingKeys15",
                "testGroupByExpressionAndSelectWithNonGroupingKeys16",
                "testGroupByExpressionAndSelectWithNonGroupingKeys17",

                "testGroupByVarDefsAndSelectWithNonGroupingKeys4",
                "testGroupByVarDefsAndSelectWithNonGroupingKeys5",
                "testGroupByVarDefsAndSelectWithNonGroupingKeys6",
                "testGroupByVarDefsAndSelectWithNonGroupingKeys7",
                "testGroupByVarDefsAndSelectWithNonGroupingKeys8",

                "testGroupByVarDefsAndSelectWithNonGroupingKeysWhereClause1",
                "testGroupByVarDefsAndSelectWithNonGroupingKeysWhereClause2",
                "testGroupByVarDefsAndSelectWithNonGroupingKeysWhereClause3",

                "testGroupByExpressionAndSelectWithNonGroupingKeys18",
                "testGroupByExpressionAndSelectWithNonGroupingKeys19",

                "testGroupByVarDefsAndSelectWithGroupingKeys11",
                "testGroupByVarDefsAndSelectWithNonGroupingKeys9",

                "testMultipleGroupBy",
                "testOptionalFieldsInInput",
                "testMultipleGroupByInSameQuery",
                "testMultipleFromClauses",
                "testOptionalFieldInput",
                "testEnumInInput",
                "testEmptyGroups",
                "testErrorSeq",
                "testGroupByExpressionAndSelectWithNonGroupingKeys1",
                "testGroupByExpressionAndSelectWithGroupingKeys10",
                "testGroupByExpressionAndSelectWithGroupingKeys11",
                "testGroupByExpressionAndSelectWithGroupingKeys12",
                "testGroupByExpressionAndSelectWithGroupingKeys13",
                "testGroupbyVarDefsAndSelectWithGroupingKeysFromClause1",
                "testGroupByVarDefsAndSelectWithGroupingKeysWithJoinClause1",
                "testGroupByVarDefsAndSelectWithGroupingKeysWithJoinClause2",
                "testGroupByVarAndSelectWithNonGroupingKeysWithJoinClause1"
        };
    }

    @Test(dataProvider = "dataToTestGroupByClauseWithInvocation")
    public void testGroupByClauseWithInvocation(String functionName) {
        BRunUtil.invoke(resultWithInvocation, functionName);
    }

    @DataProvider
    public Object[] dataToTestGroupByClauseWithInvocation() {
        return new Object[]{
                "testGroupByExpressionAndSelectWithNonGroupingKeys1",
                "testGroupByExpressionAndSelectWithNonGroupingKeys2",
                "testGroupByExpressionAndSelectWithNonGroupingKeys3",
                "testGroupByExpressionAndSelectWithNonGroupingKeys4",
                "testGroupByExpressionAndSelectWithNonGroupingKeys5",
                "testGroupByExpressionAndSelectWithGroupingKeys1",
                "testGroupByExpressionAndSelectWithGroupingKeys2",
                "testGroupByExpressionWithOrderBy",
                "testGroupByExpressionWithStreamOutput",
                "testGroupByExpressionWithTableOutput",
                "testGroupByExpressionWithMapOutput",
                "testGroupByWithDoClause",
                "testGroupByVarDefsAndSelectWithNonGroupingKeys1",

                "testGroupByVarDefsAndSelectWithNonGroupingKeys2",
                "testGroupByExpressionAndSelectWithNonGroupingKeys6",
                "testMultipleGroupBy",
                "testMultipleGroupByInSameQuery",
                "testOptionalFieldInput",
                "testGroupByExpressionAndSelectWithNonGroupingKeys6",
                "testGroupByExpressionAndSelectWithNonGroupingKeys7",
                "testEmptyGroups",
                "testGroupByExpressionAndSelectWithNonGroupingKeys8",
                "testEnumInInput",
                "testGroupByExpressionAndSelectWithNonGroupingKeys9",
                "testGroupByExpressionAndSelectWithNonGroupingKeys10",
                "testGroupByExpressionAndSelectWithNonGroupingKeys12",
                "testGroupByExpressionAndSelectWithNonGroupingKeys11"
        };
    }

    @Test
    public void testNegativeCases() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(any|error)[]', found 'int'",
                23, 37);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element list " +
                "constructor or function invocation", 29, 24);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'seq int'", 32, 28);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element list " +
                "constructor or function invocation", 32, 28);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element list " +
                "constructor or function invocation", 35, 25);
        BAssertUtil.validateError(negativeResult, i++, "operator '+' not defined for 'seq int' and 'int'", 39, 20);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element list " +
                "constructor or function invocation", 39, 20);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element list " +
                "constructor or function invocation", 42, 25);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element list " +
                "constructor or function invocation", 42, 33);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element list " +
                "constructor or function invocation", 45, 26);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 52, 39);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '[int,int...]', " +
                "found '[int...]'", 60, 39);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '[int[]]', found '[int...]'",
                65, 33);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '[int,int]', found '[int...]'",
                70, 36);
        BAssertUtil.validateError(negativeResult, i++, "invalid operation: type " +
                        "'seq record {| string name; int price1; |}' does not support field access", 78, 29);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 78, 29);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int[]', found 'seq int?'",
                99, 32);
        BAssertUtil.validateError(negativeResult, i++, "operator '+' not defined for 'seq int' and 'int'", 107, 40);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element" +
                " list constructor or function invocation", 107, 40);
        BAssertUtil.validateError(negativeResult, i++, "operator '+' not defined for 'seq int' and 'int'", 111, 33);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element" +
                " list constructor or function invocation", 111, 33);
        BAssertUtil.validateError(negativeResult, i++, "operator '+' not defined for 'seq int' and 'seq int'",
                124, 36);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 124, 36);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 124, 45);
        BAssertUtil.validateError(negativeResult, i++, "operator '+' not defined for 'seq int' and 'seq int'",
                127, 28);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 127, 28);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 127, 37);
        BAssertUtil.validateError(negativeResult, i++, "operator '+' not defined for 'seq int' and 'seq int'",
                130, 28);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 130, 28);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 130, 37);
        BAssertUtil.validateError(negativeResult, i++, "operator '+' not defined for 'seq int' and 'seq int'",
                133, 28);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 133, 28);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 133, 37);
        BAssertUtil.validateError(negativeResult, i++, "operator '+' not defined for 'seq int' and 'seq int'",
                136, 26);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 136, 26);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 136, 35);
        BAssertUtil.validateError(negativeResult, i++, "arguments not allowed after seq argument", 139, 36);
        BAssertUtil.validateError(negativeResult, i++, "arguments not allowed after rest argument", 142, 39);
        BAssertUtil.validateError(negativeResult, i++, "arguments not allowed after seq argument", 145, 37);
        BAssertUtil.validateError(negativeResult, i++, "arguments not allowed after seq argument", 145, 40);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(any|error)[]', found 'int'",
                148, 37);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                "'([int,int...]|record {| int n; |})', found '[int...]'", 158, 43);
        BAssertUtil.validateError(negativeResult, i++, "invalid grouping key type 'error', expected a subtype of " +
                "'anydata'", 166, 26);
        BAssertUtil.validateError(negativeResult, i++, "invalid grouping key type 'error', expected a subtype of " +
                "'anydata'", 169, 26);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'seq string'",
                191, 24);
        BAssertUtil.validateError(negativeResult, i++, "sequence variable can be used in a single element " +
                "list constructor or function invocation", 191, 24);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test
    public void testNegativeSemanticCases() {
        int i = 0;
        BAssertUtil.validateError(negativeSemanticResult, i++, "invalid usage of the 'check' expression operator: " +
                "no matching error return type(s) in the enclosing invokable", 24, 37);
        Assert.assertEquals(negativeSemanticResult.getErrorCount(), i);
    }
}
