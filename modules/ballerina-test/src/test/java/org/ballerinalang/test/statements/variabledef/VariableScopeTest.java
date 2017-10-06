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

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Cases for variable scopes.
 */
public class VariableScopeTest {

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("test-src/statements/variabledef/variable-scope.bal");
        resultNegative = BTestUtils.compile("test-src/statements/variabledef/variable-scope-negative.bal");
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
        BValue[] returns = BTestUtils.invoke(result, functionName, args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test variable scope with errors")
    public void testVariableScopeNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 4);
        //testVariableIfScope
        BTestUtils.validateError(resultNegative, 0, "undefined symbol 'k'", 15, 12);
        //testVariableElseScope
        BTestUtils.validateError(resultNegative, 1, "undefined symbol 'b'", 26, 12);
        //testVariableWhileScope
        BTestUtils.validateError(resultNegative, 2, "undefined symbol 'b'", 35, 15);
        //testVariableResourceScope
        BTestUtils.validateError(resultNegative, 3, "undefined symbol 'b'", 47, 17);
    }
}
