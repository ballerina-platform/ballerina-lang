/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.statements.variabledef;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Cases for variable scopes.
 */
@Test(groups = { "brokenOnNewParser" })
public class VariableScopeTest {

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/variabledef/variable-scope.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/variabledef/variable-scope-negative.bal");
    }

    @Test
    public void testScopeValue() {

        scopeValue(result, "scopeIfValue", 5, 10, 20, 6);
        scopeValue(result, "scopeIfValue", 13, 8, 7, 7);
        scopeValue(result, "scopeIfValue", 25, 30, 8, 100000);

        scopeValue(result, "scopeWhileScope", 5, 10, 20, 5);
        scopeValue(result, "scopeWhileScope", 13, 8, 7, 105);
        scopeValue(result, "scopeWhileScope", 40, 30, 8, 205);
        scopeValue(result, "scopeWhileScope", 40, 30, 50, 305);
    }

    private void scopeValue(CompileResult result, String functionName, int a, int b, int c, int expected) {
        BValue[] args = {new BInteger(a), new BInteger(b), new BInteger(c)};
        BValue[] returns = BRunUtil.invoke(result, functionName, args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test variable scope with errors")
    public void testVariableScopeNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 5);
        //testVariableIfScope
        BAssertUtil.validateError(resultNegative, 0, "undefined symbol 'k'", 14, 12);
        //testVariableElseScope
        BAssertUtil.validateError(resultNegative, 1, "undefined symbol 'b'", 25, 12);
        //testVariableWhileScope
        BAssertUtil.validateError(resultNegative, 2, "undefined symbol 'b'", 34, 15);
        //testVariableResourceScope
        BAssertUtil.validateError(resultNegative, 3, "undefined symbol 'a'", 42, 17);
        BAssertUtil.validateError(resultNegative, 4, "undefined symbol 'b'", 47, 17);
    }
}
