/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * This contains methods to test query expressions with anonymous function expressions.
 *
 * @since 2.0.0
 */
public class QueryExprWithAnonFunctionExprs {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query-expr-with-anon-func-exprs.bal");
    }

    @Test(dataProvider = "dataToTestQueryExprWithAnonFuncExprs", description = "Test query expressions with " +
            "anonymous function expressions")
    public void testQueryExprWithAnonFuncExprs(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestQueryExprWithAnonFuncExprs() {
        return new Object[]{
                "testQueryWithAnonFuncExprInLetClause",
                "testQueryWithAnonFuncExprInWhereClause",
                "testQueryWithAnonFuncExprInSelectClause",
                "testQueryWithAnonFuncExprInSelectClause2",
                "testNestedQueryWithAnonFuncExpr",
                "testComplexQueryWithAnonFuncExpr",
                "testComplexQueryWithAnonFuncExpr2",
                "testInnerQueryWithAnonFuncExpr",
                "testQueryWithNestedLambdaFunctions",
                "testGlobalQueryWithAnonFuncExpr"
        };
    }
}
