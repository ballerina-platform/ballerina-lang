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

package org.ballerinalang.test.expressions.elvis;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for elvis expressions.
 *
 * @since 0.964.1
 */
public class ElvisExpressionTest {

    private CompileResult compileResult;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/elvis/elvis-expr.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/elvis/elvis-expr-negative.bal");
    }

    @Test(description = "Test Elvis operator on expression evaluated to value type positive.")
    public void testElvisValueTypePositive() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisValueTypePositive");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 120);
    }

    @Test(description = "Test Elvis operator on expression evaluated to value type negative.")
    public void testElvisValueTypeNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisValueTypeNegative");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 111);
    }

    @Test(description = "Test Elvis operator on nested expressions evaluated to value type.")
    public void testElvisValueTypeNested() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisValueTypeNested");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 3000);
    }

    @Test(description = "Test Elvis operator on expression evaluated to ref type positive.")
    public void testElvisRefTypePositive() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisRefTypePositive");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 2300);
    }

    @Test(description = "Test Elvis operator on expression evaluated to ref type negative.")
    public void testElvisRefTypeNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisRefTypeNegative");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 111);
    }

    @Test(description = "Test Elvis operator on nested expressions evaluated to ref type.")
    public void testElvisRefTypeNested() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisRefTypeNested");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 4000);
    }

    @Test(description = "Test Elvis operator on nested expressions evaluated to ref type case two.")
    public void testElvisRefTypeNestedCaseTwo() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisRefTypeNestedCaseTwo");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BString);
        Assert.assertEquals(results[0].stringValue(), "kevin");
    }

    @Test(description = "Test Elvis operator on expression evaluated to record type positive.")
    public void testElvisRecordTypePositive() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisRecordTypePositive");
        Assert.assertEquals(results.length, 2);
        Assert.assertTrue(results[0] instanceof BString);
        Assert.assertEquals(results[0].stringValue(), "Jim");
        Assert.assertTrue(results[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 100);
    }

    @Test(description = "Test Elvis operator on expression evaluated to record type negative.")
    public void testElvisRecordTypeNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisRecordTypeNegative");
        Assert.assertEquals(results.length, 2);
        Assert.assertTrue(results[0] instanceof BString);
        Assert.assertEquals(results[0].stringValue(), "default");
        Assert.assertTrue(results[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 0);
    }

    @Test(description = "Test Elvis operator on expression evaluated to object type positive.")
    public void testElvisObjectTypePositive() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisObjectTypePositive");
        Assert.assertEquals(results.length, 3);
        Assert.assertTrue(results[0] instanceof BString);
        Assert.assertEquals(results[0].stringValue(), "Alice");
        Assert.assertTrue(results[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 15);
        Assert.assertTrue(results[2] instanceof BString);
        Assert.assertEquals(results[2].stringValue(), "Maths");
    }

    @Test(description = "Test Elvis operator on expression evaluated to object type negative.")
    public void testElvisObjectTypeNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisObjectTypeNegative");
        Assert.assertEquals(results.length, 3);
        Assert.assertTrue(results[0] instanceof BString);
        Assert.assertEquals(results[0].stringValue(), "Bob");
        Assert.assertTrue(results[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 18);
        Assert.assertTrue(results[2] instanceof BString);
        Assert.assertEquals(results[2].stringValue(), "Science");
    }

    @Test(description = "Test Elvis operator on expression evaluated to tuple type positive.")
    public void testElvisTupleTypePositive() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisTupleTypePositive");
        Assert.assertEquals(results.length, 2);
        Assert.assertTrue(results[0] instanceof BString);
        Assert.assertEquals(results[0].stringValue(), "Jack");
        Assert.assertTrue(results[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 23);
    }

    @Test(description = "Test Elvis operator on expression evaluated to tuple type negative.")
    public void testElvisTupleTypeNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisTupleTypeNegative");
        Assert.assertEquals(results.length, 2);
        Assert.assertTrue(results[0] instanceof BString);
        Assert.assertEquals(results[0].stringValue(), "default");
        Assert.assertTrue(results[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 0);
    }

    @Test(description = "Test Elvis operator on nested tuple case one.")
    public void testElvisNestedTupleTypeCaseOne() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisNestedTupleTypeCaseOne");
        Assert.assertEquals(results.length, 2);
        Assert.assertTrue(results[0] instanceof BString);
        Assert.assertEquals(results[0].stringValue(), "Jack");
        Assert.assertTrue(results[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 23);
    }

    @Test(description = "Test Elvis operator on nested tuple case two.")
    public void testElvisNestedTupleTypeCaseTwo() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisNestedTupleTypeCaseTwo");
        Assert.assertEquals(results.length, 2);
        Assert.assertTrue(results[0] instanceof BString);
        Assert.assertEquals(results[0].stringValue(), "Jill");
        Assert.assertTrue(results[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 77);
    }

    @Test(description = "Test Elvis operator on nested tuple case three.")
    public void testElvisNestedTupleTypeCaseThree() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisNestedTupleTypeCaseThree");
        Assert.assertEquals(results.length, 2);
        Assert.assertTrue(results[0] instanceof BString);
        Assert.assertEquals(results[0].stringValue(), "default");
        Assert.assertTrue(results[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 0);
    }

    @Test(description = "Test Elvis as an function argument.")
    public void testElvisAsArgumentPositive() {
        BRunUtil.invoke(compileResult, "testElvisAsArgumentPositive");
    }

    @Test(description = "Test Elvis with lang:value method calls in module level.")
    public void testElvisWithLangValueMethodCallsModuleLevel() {
        BRunUtil.invoke(compileResult, "testElvisWithLangValueMethodCallsModuleLevel");
    }

    @Test(description = "Test Elvis with lang:value method calls.")
    public void testElvisWithLangValueMethodCalls() {
        BRunUtil.invoke(compileResult, "testElvisWithLangValueMethodCalls");
    }

    @Test(description = "Test nested Elvis expressions without parenthesis in module level.")
    public void testNestedElvisWithoutParenthesisModuleLevel() {
        BRunUtil.invoke(compileResult, "testNestedElvisWithoutParenthesisModuleLevel");
    }

    @Test(description = "Test nested Elvis expressions without parenthesis.")
    public void testNestedElvisWithoutParenthesis() {
        BRunUtil.invoke(compileResult, "testNestedElvisWithoutParenthesis");
    }

    @Test(description = "Negative test cases.")
    public void testElvisOperatorNegative() {
        Assert.assertEquals(negativeResult.getErrorCount(), 17);
        BAssertUtil.validateError(negativeResult, 0, "incompatible types: expected 'int', found 'int?'", 5, 19);
        BAssertUtil.validateError(negativeResult, 1, "incompatible types: expected 'int', found 'string'", 12, 14);
        BAssertUtil.validateError(negativeResult, 2, "incompatible types: expected 'int', found 'string'", 19, 9);
        BAssertUtil.validateError(negativeResult, 3, "incompatible types: expected 'int', found 'string'", 26, 17);
        BAssertUtil.validateError(negativeResult, 4, "incompatible types: expected 'byte', found 'int'", 30, 17);
        BAssertUtil.validateError(negativeResult, 5, "incompatible types: expected 'int', found '(int|byte)?'", 37,
                10);
        BAssertUtil.validateError(negativeResult, 6,
                "incompatible types: expected 'int', found '(int|int:Unsigned8|int:Signed8)?'", 44, 10);
        BAssertUtil.validateError(negativeResult, 7,
                "incompatible types: expected 'int', found '(int|byte|int:Signed8)?'", 45, 10);
        BAssertUtil.validateError(negativeResult, 8,
                "incompatible types: expected 'int', found '(decimal|float|int:Signed16)?'", 52, 10);
        BAssertUtil.validateError(negativeResult, 9,
                "incompatible types: expected 'int', found '(decimal|float|int|int:Unsigned8|byte|int:Signed16)?'", 53,
                10);
        BAssertUtil.validateError(negativeResult, 10,
                "incompatible types: expected 'int', found '(int|string|string:Char|string[])'", 60, 10);
        BAssertUtil.validateError(negativeResult, 11, "incompatible types: expected 'int', found '(int|byte)?'", 67,
                14);
        BAssertUtil.validateError(negativeResult, 12,
                "incompatible types: expected 'int', found '(int|int:Unsigned8|int:Signed8)?'", 74, 14);
        BAssertUtil.validateError(negativeResult, 13, "incompatible types: expected 'int', found '(int|byte)?'", 75,
                14);
        BAssertUtil.validateError(negativeResult, 14,
                "incompatible types: expected 'int', found '(decimal|float|int:Signed8)?'", 82, 15);
        BAssertUtil.validateError(negativeResult, 15,
                "incompatible types: expected 'int', found '(decimal|float|int|int:Unsigned8|byte|int:Signed8)?'", 83, 15);
        BAssertUtil.validateError(negativeResult, 16,
                "incompatible types: expected 'int', found '(int|string|string:Char|string[])'", 90, 15);
    }
}
