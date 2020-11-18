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
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Test cases for field access.
 *
 * @since 1.0
 */
public class FieldAccessTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/access/field_access.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/access/field_access_negative.bal");
    }

    @Test
    public void testNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 17);
        int i = 0;
        validateError(negativeResult, i++, "invalid operation: type 'Employee' does not support field access " +
                              "for non-required field 'id'", 32, 9);
        validateError(negativeResult, i++, "invalid operation: type 'Employee' does not support field access " +
                              "for non-required field 'salary'", 33, 9);
        validateError(negativeResult, i++, "invalid operation: type '(Employee|Person)' does not support field access" +
                " for non-required field 'salary'", 39, 9);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found '(int|string)'", 56, 17);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found '(int|string)'", 57, 15);
        validateError(negativeResult, i++, "invalid operation: type 'map<string>' does not support field access",
                      62, 16);
        validateError(negativeResult, i++, "invalid operation: type '(map<string>|EmployeeTwo)' does not support " +
                              "field access", 68, 16);
        validateError(negativeResult, i++, "invalid operation: type 'EmployeeTwo?' does not support field access",
                      74, 17);
        validateError(negativeResult, i++, "invalid operation: type '(map<string>|map<int>)' does not support " +
                              "field access", 80, 20);
        validateError(negativeResult, i++, "incompatible types: expected 'json', found '(json|error)'", 85, 14);
        validateError(negativeResult, i++, "incompatible types: expected 'json', found '(json|error)'", 90, 14);
        validateError(negativeResult, i++, "invalid operation: type '(json|error)' does not support field access", 96,
                      22);
        validateError(negativeResult, i++, "incompatible types: expected '(map<json>|error)', " +
                              "found '(map<json>|json|error)'", 102, 26);
        validateError(negativeResult, i++, "incompatible types: expected 'map<json>', found '(json|map<json>|error)'",
                      106, 20);
        validateError(negativeResult, i++, "invalid operation: type 'Foo?' does not support field access", 131, 14);
        validateError(negativeResult, i++, "invalid operation: type 'Baz?' does not support field access", 134, 16);
        validateError(negativeResult, i, "invalid operation: type 'Foo[]' does not support field access", 138, 9);
    }

    @Test(dataProvider = "recordFieldAccessFunctions")
    public void testRecordFieldAccess(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "recordFieldAccessFunctions")
    public Object[][] recordFieldAccessFunctions() {
        return new Object[][] {
            { "testRecordFieldAccess1" },
            { "testRecordFieldAccess2" },
            { "testRecordFieldAccess3" }
        };
    }

    @Test
    public void testJsonFieldAccessPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonFieldAccessPositive");
        Assert.assertEquals(returns.length, 1);
        BValueArray array = ((BValueArray) returns[0]);
        Assert.assertEquals(array.size(), 2);
        for (int i = 0; i < 2; i++) {
            Assert.assertEquals(array.getBoolean(i), 1);
        }
    }

    @Test (enabled = false)
    public void testJsonFieldAccessNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonFieldAccessNegative");
        Assert.assertEquals(returns.length, 1);
        BValueArray array = ((BValueArray) returns[0]);
        Assert.assertEquals(array.size(), 5);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(array.getBoolean(i), 1);
        }
    }

    @Test
    public void testMapJsonFieldAccessPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testMapJsonFieldAccessPositive");
        Assert.assertEquals(returns.length, 1);
        BValueArray array = ((BValueArray) returns[0]);
        Assert.assertEquals(array.size(), 2);
        for (int i = 0; i < 2; i++) {
            Assert.assertEquals(array.getBoolean(i), 1);
        }
    }

    @Test (enabled = false)
    public void testMapJsonFieldAccessNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testMapJsonFieldAccessNegative");
        Assert.assertEquals(returns.length, 1);
        BValueArray array = ((BValueArray) returns[0]);
        Assert.assertEquals(array.size(), 5);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(array.getBoolean(i), 1);
        }
    }

    @Test(dataProvider = "nonNilLiftingJsonFieldAccessFunctions", enabled = false)
    public void testNonNilLiftingJsonFieldAccess(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "nonNilLiftingJsonFieldAccessFunctions")
    public Object[][] nonNilLiftingJsonFieldAccessFunctions() {
        return new Object[][] {
            { "testNonNilLiftingJsonAccess1" },
            { "testNonNilLiftingJsonAccess2" },
            { "testNonNilLiftingJsonAccess3" }
        };
    }

    @Test
    public void testLaxUnionFieldAccessPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testLaxUnionFieldAccessPositive");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(dataProvider = "laxUnionFieldAccessNegativeFunctions", enabled = false)
    public void testLaxUnionFieldAccessNegative(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "laxUnionFieldAccessNegativeFunctions")
    public Object[][] laxUnionFieldAccessNegativeFunctions() {
        return new Object[][] {
            { "testLaxUnionFieldAccessNegative1" },
            { "testLaxUnionFieldAccessNegative2" },
            { "testLaxUnionFieldAccessNegative3" }
        };
    }

    @Test(dataProvider = "mapJsonFieldAccessTypePositiveFunctions")
    public void testMapJsonFieldAccessTypePositive(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "mapJsonFieldAccessTypePositiveFunctions")
    public Object[][] mapJsonFieldAccessTypePositiveFunctions() {
        return new Object[][] {
                { "testMapJsonFieldAccessTypePositive1" },
                { "testMapJsonFieldAccessTypePositive2" }
        };
    }

    @Test
    public void testFieldAccessOnInvocation() {
        BValue[] returns = BRunUtil.invoke(result, "testFieldAccessOnInvocation");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testJsonFieldAccessOnInvocation() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonFieldAccessOnInvocation");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }
}
