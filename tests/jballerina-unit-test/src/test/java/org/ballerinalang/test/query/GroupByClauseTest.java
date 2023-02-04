/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This contains methods to test nested query expressions.
 *
 * @since 2201.5.0
 */
public class GroupByClauseTest {
    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/group_by_clause.bal");
//        negativeResult = BCompileUtil.compile("test-src/query/inner-queries-negative.bal");
    }

    @Test(dataProvider = "dataToTestGroupByClause")
    public void testGroupByClause(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestGroupByClause() {
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
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause1",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause2",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause3",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause4",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause5",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause6",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause7",
                "testGroupByExpressionAndSelectWithGroupingKeysAndWhereClause8",
                "testGroupByExpressionAndSelectWithGroupingKeysFromClause1",
                "testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause1",
                "testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause2",
                "testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause3",
                "testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause4",
                "testGroupByExpressionAndSelectWithGroupingKeysWithJoinClause5",
                "testGroupByExpressionAndSelectWithGroupingKeysWithOrderbyClause1",
                "testGroupByExpressionAndSelectWithGroupingKeysWithOrderbyClause2",
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
                "testGroupByVarDefsAndSelectWithGroupingKeysWithJoinClause1",
                "testGroupByVarDefsAndSelectWithGroupingKeysWithJoinClause2",
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
                "testGroupByExpressionAndSelectWithNonGroupingKeys1",
                "testGroupByExpressionAndSelectWithNonGroupingKeys2",
                "testGroupByExpressionAndSelectWithNonGroupingKeys3",
                "testGroupByExpressionAndSelectWithNonGroupingKeys4",
                "testGroupByExpressionAndSelectWithNonGroupingKeys5",
                "testGroupByExpressionAndSelectWithNonGroupingKeys6",
                "testGroupByExpressionAndSelectWithNonGroupingKeys7",
                "testGroupByVarDefsAndSelectWithNonGroupingKeys1",
                "testGroupByVarDefsAndSelectWithNonGroupingKeys2",
                "testGroupByVarDefsAndSelectWithNonGroupingKeys3",
        };
    }
}
