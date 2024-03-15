/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This contains methods to test query expressions and query actions which uses variables defined in binding patterns.
 *
 * @since 2201.9.0
 */
public class QueryWithBindingPatternVarTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query_with_binding_pattern.bal");
    }

    @Test(dataProvider = "dataToTestQueryWithBindingPatternVar")
    public void testQueryWithBindingPatternVars(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestQueryWithBindingPatternVar() {
        return new Object[]{
                "testSelectClauseWithBindingVar",
                "testCollectClauseWithBindingVar",
                "testFunctionPointerInvocationWithBindingVar",
                "testQueryActionWithBindingVar",
                "testClosureInQueryActionInDoWithBindingVar",
                "testNestedQueryInSelectWithBindingVar",
                "testLimitClauseAndQueryExprWithBindingVar"
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
