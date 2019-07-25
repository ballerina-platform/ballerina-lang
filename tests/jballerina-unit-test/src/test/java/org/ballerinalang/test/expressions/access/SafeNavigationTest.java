/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.access;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for safe navigation.
 *
 * @since 0.970.0
 */
@Test(groups = { "brokenOnLangLibChange" })
public class SafeNavigationTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/access/safe_navigation.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/access/safe_navigation_negative.bal");
    }

    @Test
    public void testNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 19);
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string?', found '(string|error)'",
                25, 19);
        BAssertUtil.validateError(negativeResult, i++,
                "invalid operation: type '(Info|error)' does not support field access", 34, 25);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected '(string|error)?', found '(other|error)'", 34, 25);
        BAssertUtil.validateError(negativeResult, i++,
                "error lifting operator cannot be used in the target expression of an assignment", 40, 5);
        BAssertUtil.validateError(negativeResult, i++,
                "error lifting operator cannot be used in the target expression of an assignment", 40, 5);
        BAssertUtil.validateError(negativeResult, i++,
                "error lifting operator cannot be used in the target expression of an assignment", 40, 5);
        BAssertUtil.validateError(negativeResult, i++, "variable 'p' is not initialized", 40, 5);
        BAssertUtil.validateError(negativeResult, i++, "cannot infer type of the error from '(Person?[]|error)'",
                44, 25);
        BAssertUtil.validateError(negativeResult, i++,
                "invalid operation: type '(Person?[]|error)' does not support indexing", 45, 12);
        BAssertUtil.validateError(negativeResult, i++, "safe navigation operator not required for type 'error?'", 50,
                12);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found '(other|error)?'",
                50, 12);
        BAssertUtil.validateError(negativeResult, i++, "safe navigation operator not required for type 'error'", 55,
                12);
        BAssertUtil.validateError(negativeResult, i++, "invalid operation: type 'error' does not support field " +
                        "access", 55, 12);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'string?'", 64,
                16);
        BAssertUtil.validateError(negativeResult, i++, "safe navigation operator not required for type 'xml'", 68,
                9);
        BAssertUtil.validateError(negativeResult, i++, "safe navigation operator not required for type 'json'", 72,
                9);
        BAssertUtil.validateError(negativeResult, i++, "safe navigation operator not required for type '(json|string)'",
                80, 9);
        BAssertUtil.validateError(negativeResult, i++, "safe navigation operator not required for type 'json'",
                88, 12);
        BAssertUtil.validateError(negativeResult, i++, "safe navigation operator not required for type 'json'",
                93, 12);
    }

    @Test
    public void testNotNilPath() {
        BValue[] returns = BRunUtil.invoke(result, "testNotNilPath");
        Assert.assertEquals(returns[0].stringValue(), "Colombo");
    }

    @Test
    public void testNilInFirstVar() {
        BValue[] returns = BRunUtil.invoke(result, "testNilInFirstVar");
        Assert.assertEquals(returns[0], null);
    }

    @Test
    public void testNonExistingMapKeyWithIndexAccess() {
        BValue[] returns = BRunUtil.invoke(result, "testNonExistingMapKeyWithIndexAccess");
        Assert.assertNull(returns[0]);
    }
}
