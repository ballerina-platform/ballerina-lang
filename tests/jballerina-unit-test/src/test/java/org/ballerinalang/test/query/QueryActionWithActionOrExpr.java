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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This contains methods to test query-action that allow action-or-expr in from clause and let clause.
 *
 * @since 2201.2.0
 */
public class QueryActionWithActionOrExpr {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/query/query_action_with_action_or_expr.bal");
    }

    @Test(dataProvider = "dataToTestQueryActionWithActionOrExpr")
    public void testQueryActionWithActionOrExpr(String functionName) {
        BRunUtil.invoke(compileResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestQueryActionWithActionOrExpr() {
        return new Object[]{
                "testQueryActionWithStartAction",
                "testQueryActionWithParenthesizedStartAction",
                "testQueryActionWithWaitAction",
                "testQueryActionWithParenthesizedWaitAction",
                "testQueryActionWithClientRemoteMethodCall",
                "testQueryActionWithParenthesizedClientRemoteMethodCall",
                "testQueryActionWithQueryAction",
                "testQueryActionWithParenthesizedQueryAction",
                "testQueryActionWithTypeCastActionOrExpr",
                "testQueryActionWithParenthesizedTypeCastActionOrExpr",
                "testQueryActionWithCheckingActionOrExpr",
                "testQueryActionWithParenthesizedCheckingActionOrExpr",
                "testQueryActionWithTrapActionOrExpr",
                "testQueryActionWithParenthesizedTrapActionOrExpr",
                "testQueryActionWithQueryActionOrExpr",
                "testQueryActionWithParenthesizedQueryActionOrExpr",
                "testPrecedenceOfActionsWhenUsingActionOrExprWithQueryAction",
                "testQueryActionWithAllQueryClauses",
                "testQueryActionWithNestedQueryActionOrExpr",
                "testQueryActionWithClientResourceAccessAction",
                "testQueryActionWithGroupedClientResourceAccessAction",
                "testNestedQueryActionWithClientResourceAccessAction"
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
