/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Test cases for combinations of field, optional field and member access.
 *
 * @since 1.0
 */
public class AccessTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/access/access.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/access/access_negative.bal");
    }

    @Test
    public void testNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        int i = 0;
        validateError(negativeResult, i++, "invalid operation: type 'AlphaTwo' does not support field access for " +
                "non-required field 'betas'", 40, 18);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found 'string?'", 53, 17);
        validateError(negativeResult, i, "invalid operation: type 'Delta?' does not support indexing", 70, 17);
    }

    @Test(dataProvider = "fieldAndOptionalFieldAccessFunctions", enabled = false)
    public void testFieldAndOptionalFieldAccess(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "fieldAndOptionalFieldAccessFunctions")
    public Object[][] fieldAndOptionalFieldAccessFunctions() {
        return new Object[][] {
                { "testFieldAccessWithOptionalFieldAccess1" },
                { "testFieldAccessWithOptionalFieldAccess2" },
                { "testFieldAccessWithOptionalFieldAccess3" }
        };
    }

    @Test(dataProvider = "fieldOptionalFieldAndMemberAccessFunctions")
    public void testFieldOptionalFieldAndMemberAccess(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "fieldOptionalFieldAndMemberAccessFunctions")
    public Object[][] fieldOptionalFieldAndMemberAccessFunctions() {
        return new Object[][] {
                { "testFieldOptionalFieldAndMemberAccess1" },
                { "testFieldOptionalFieldAndMemberAccess2" }
        };
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array index out of range: index: 0, size: 0.*")
    public void testFieldOptionalFieldAndMemberAccess2() {
        BRunUtil.invoke(result, "testFieldOptionalFieldAndMemberAccess3");
    }

    @Test
    public void testMemberAccessOnNillableObjectField() {
        BValue[] returns = BRunUtil.invoke(result, "testMemberAccessOnNillableObjectField");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testNilLiftingOnMemberAccessOnNillableObjectField() {
        BValue[] returns = BRunUtil.invoke(result, "testNilLiftingOnMemberAccessOnNillableObjectField");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleTypeAccessOnFunctionPointer() {
        BRunUtil.invoke(result, "testSimpleTypeAccessOnFunctionPointer");
    }
}
