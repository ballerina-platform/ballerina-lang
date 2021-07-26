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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test query expressions within type guards.
 *
 * @since 2.0.0
 */
public class QueryExpressionTypeNarrowingTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query-expr-type-narrowing.bal");
    }

    @Test(description = "Test query expression within a type guard")
    public void testQueryExprWithinTypeGuard() {
        BRunUtil.invoke(result, "testQueryExprWithinTypeGuard");
    }

    @Test(description = "Test query expression within a negated type guard")
    public void testQueryExprWithinNegatedTypeGuard() {
        BRunUtil.invoke(result, "testQueryExprWithinNegatedTypeGuard");
    }

    @Test(description = "Test query expression within a ternary operator")
    public void testTernaryWithinQueryExpression() {
        BRunUtil.invoke(result, "testTernaryWithinQueryExpression");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
