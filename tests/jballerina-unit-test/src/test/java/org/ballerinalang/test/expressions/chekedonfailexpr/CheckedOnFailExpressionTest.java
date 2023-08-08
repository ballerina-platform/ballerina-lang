/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
package org.ballerinalang.test.expressions.chekedonfailexpr;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for checked on fail expression.
 *
 * @since 2201.8.0
 */
public class CheckedOnFailExpressionTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/checkedonfailexpr/check_on_fail_expr.bal");
    }

    @Test
    public void testCheckedOnFailExprReturnsValue() {
        BRunUtil.invoke(result, "testCheckedOnFailExprReturnsValue");
    }

    @Test
    public void testCheckedOnFailExpressionWithCustomError() {
        BRunUtil.invoke(result, "testCheckedOnFailExpressionWithCustomError");
    }

    @Test
    public void testCheckedOnFailExpressionWithQueryExpression() {
        BRunUtil.invoke(result, "testCheckedOnFailExpressionWithQueryExpression");
    }

    @Test
    public void testCheckedOnFailExpressionInModuleLevel() {
        CompileResult initResult =
                BCompileUtil.compile("test-src/expressions/checkedonfailexpr/check_on_fail_in_module_level.bal");
        try {
            BRunUtil.runMain(initResult);
        } catch (RuntimeException e) {
            Assert.assertEquals(e.getMessage(), "error: Custom error");
        }
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
