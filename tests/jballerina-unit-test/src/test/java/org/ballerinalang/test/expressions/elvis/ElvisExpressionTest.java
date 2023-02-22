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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
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
        Object results = BRunUtil.invoke(compileResult, "testElvisValueTypePositive");
        Assert.assertTrue(results instanceof Long);
        Assert.assertEquals(results, 120L);
    }

    @Test(description = "Test Elvis operator on expression evaluated to value type negative.")
    public void testElvisValueTypeNegative() {
        Object results = BRunUtil.invoke(compileResult, "testElvisValueTypeNegative");
        Assert.assertTrue(results instanceof Long);
        Assert.assertEquals(results, 111L);
    }

    @Test(description = "Test Elvis operator on nested expressions evaluated to value type.")
    public void testElvisValueTypeNested() {
        Object results = BRunUtil.invoke(compileResult, "testElvisValueTypeNested");
        Assert.assertTrue(results instanceof Long);
        Assert.assertEquals(results, 3000L);
    }

    @Test(description = "Test Elvis operator on expression evaluated to ref type positive.")
    public void testElvisRefTypePositive() {
        Object results = BRunUtil.invoke(compileResult, "testElvisRefTypePositive");
        Assert.assertTrue(results instanceof Long);
        Assert.assertEquals(results, 2300L);
    }

    @Test(description = "Test Elvis operator on expression evaluated to ref type negative.")
    public void testElvisRefTypeNegative() {
        Object results = BRunUtil.invoke(compileResult, "testElvisRefTypeNegative");
        Assert.assertTrue(results instanceof Long);
        Assert.assertEquals(results, 111L);
    }

    @Test(description = "Test Elvis operator on nested expressions evaluated to ref type.")
    public void testElvisRefTypeNested() {
        Object results = BRunUtil.invoke(compileResult, "testElvisRefTypeNested");
        Assert.assertTrue(results instanceof Long);
        Assert.assertEquals(results, 4000L);
    }

    @Test(description = "Test Elvis operator on nested expressions evaluated to ref type case two.")
    public void testElvisRefTypeNestedCaseTwo() {
        Object results = BRunUtil.invoke(compileResult, "testElvisRefTypeNestedCaseTwo");
        Assert.assertTrue(results instanceof BString);
        Assert.assertEquals(results.toString(), "kevin");
    }

    @Test(description = "Test Elvis operator on expression evaluated to record type positive.")
    public void testElvisRecordTypePositive() {
        Object arr = BRunUtil.invoke(compileResult, "testElvisRecordTypePositive");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 2);
        Assert.assertTrue(results.get(0) instanceof BString);
        Assert.assertEquals(results.get(0).toString(), "Jim");
        Assert.assertTrue(results.get(1) instanceof Long);
        Assert.assertEquals(results.get(1), 100L);
    }

    @Test(description = "Test Elvis operator on expression evaluated to record type negative.")
    public void testElvisRecordTypeNegative() {
        Object arr = BRunUtil.invoke(compileResult, "testElvisRecordTypeNegative");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 2);
        Assert.assertTrue(results.get(0) instanceof BString);
        Assert.assertEquals(results.get(0).toString(), "default");
        Assert.assertTrue(results.get(1) instanceof Long);
        Assert.assertEquals(results.get(1), 0L);
    }

    @Test(description = "Test Elvis operator on expression evaluated to object type positive.")
    public void testElvisObjectTypePositive() {
        Object arr = BRunUtil.invoke(compileResult, "testElvisObjectTypePositive");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 3);
        Assert.assertTrue(results.get(0) instanceof BString);
        Assert.assertEquals(results.get(0).toString(), "Alice");
        Assert.assertTrue(results.get(1) instanceof Long);
        Assert.assertEquals(results.get(1), 15L);
        Assert.assertTrue(results.get(2) instanceof BString);
        Assert.assertEquals(results.get(2).toString(), "Maths");
    }

    @Test(description = "Test Elvis operator on expression evaluated to object type negative.")
    public void testElvisObjectTypeNegative() {
        Object arr = BRunUtil.invoke(compileResult, "testElvisObjectTypeNegative");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 3);
        Assert.assertTrue(results.get(0) instanceof BString);
        Assert.assertEquals(results.get(0).toString(), "Bob");
        Assert.assertTrue(results.get(1) instanceof Long);
        Assert.assertEquals(results.get(1), 18L);
        Assert.assertTrue(results.get(2) instanceof BString);
        Assert.assertEquals(results.get(2).toString(), "Science");
    }

    @Test(description = "Test Elvis operator on expression evaluated to tuple type positive.")
    public void testElvisTupleTypePositive() {
        Object arr = BRunUtil.invoke(compileResult, "testElvisTupleTypePositive");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 2);
        Assert.assertTrue(results.get(0) instanceof BString);
        Assert.assertEquals(results.get(0).toString(), "Jack");
        Assert.assertTrue(results.get(1) instanceof Long);
        Assert.assertEquals(results.get(1), 23L);
    }

    @Test(description = "Test Elvis operator on expression evaluated to tuple type negative.")
    public void testElvisTupleTypeNegative() {
        Object arr = BRunUtil.invoke(compileResult, "testElvisTupleTypeNegative");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 2);
        Assert.assertTrue(results.get(0) instanceof BString);
        Assert.assertEquals(results.get(0).toString(), "default");
        Assert.assertTrue(results.get(1) instanceof Long);
        Assert.assertEquals(results.get(1), 0L);
    }

    @Test(description = "Test Elvis operator on nested tuple case one.")
    public void testElvisNestedTupleTypeCaseOne() {
        Object arr = BRunUtil.invoke(compileResult, "testElvisNestedTupleTypeCaseOne");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 2);
        Assert.assertTrue(results.get(0) instanceof BString);
        Assert.assertEquals(results.get(0).toString(), "Jack");
        Assert.assertTrue(results.get(1) instanceof Long);
        Assert.assertEquals(results.get(1), 23L);
    }

    @Test(description = "Test Elvis operator on nested tuple case two.")
    public void testElvisNestedTupleTypeCaseTwo() {
        Object arr = BRunUtil.invoke(compileResult, "testElvisNestedTupleTypeCaseTwo");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 2);
        Assert.assertTrue(results.get(0) instanceof BString);
        Assert.assertEquals(results.get(0).toString(), "Jill");
        Assert.assertTrue(results.get(1) instanceof Long);
        Assert.assertEquals(results.get(1), 77L);
    }

    @Test(description = "Test Elvis operator on nested tuple case three.")
    public void testElvisNestedTupleTypeCaseThree() {
        Object arr = BRunUtil.invoke(compileResult, "testElvisNestedTupleTypeCaseThree");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 2);
        Assert.assertTrue(results.get(0) instanceof BString);
        Assert.assertEquals(results.get(0).toString(), "default");
        Assert.assertTrue(results.get(1) instanceof Long);
        Assert.assertEquals(results.get(1), 0L);
    }

    @Test(description = "Test Elvis as an function argument.")
    public void testElvisAsArgumentPositive() {
        BRunUtil.invoke(compileResult, "testElvisAsArgumentPositive");
    }

    @Test(description = "Test Elvis as a configurable variable.")
    public void testElvisWithConfigurableVar() {
        BRunUtil.invoke(compileResult, "testElvisWithConfigurableVar");
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

    @Test
    public void testElvisExprWithTypeRefType() {
        BRunUtil.invoke(compileResult, "testElvisExprWithTypeRefType");
    }

    @Test
    public void testElvisExprWithIntersectionTypes() {
        BRunUtil.invoke(compileResult, "testElvisExprWithIntersectionTypes");
    }

    @Test(description = "Negative test cases.")
    public void testElvisOperatorNegative() {
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found 'int?'", 5, 19);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found 'string'", 12, 14);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found 'string'", 19, 9);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found 'string'", 26, 17);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'byte', found 'int'", 30, 17);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found '(int|byte)?'", 37, 10);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found '(int|int:Unsigned8|int:Signed8)?'", 44, 10);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found '(int|byte|int:Signed8)?'", 45, 10);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found '(decimal|float|int:Signed16)?'", 52, 10);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found '(decimal|float|int|int:Unsigned8|byte|int:Signed16)?'",
                53, 10);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found '(int|string|string:Char|string[])'", 60, 10);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found '(int|byte)?'", 67, 14);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found '(int|int:Unsigned8|int:Signed8)?'", 74, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'int', found '(int|byte)?'",
                75, 14);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found '(decimal|float|int:Signed8)?'", 82, 15);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found '(decimal|float|int|int:Unsigned8|byte|int:Signed8)?'",
                83, 15);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected 'int', found '(int|string|string:Char|string[])'", 90, 15);
        BAssertUtil.validateError(negativeResult, index++,
                "operator '/' not defined for '(json|error)' and 'int'", 97, 13);
        BAssertUtil.validateError(negativeResult, index++,
                "operator '/' not defined for '(json|error)' and 'int'", 108, 13);
        BAssertUtil.validateError(negativeResult, index++,
                "operator '+' not defined for '(json|error)' and 'string'", 109, 16);
        BAssertUtil.validateError(negativeResult, index++,
                "operator '&&' not defined for '(json|error)' and 'boolean'", 110, 17);
        BAssertUtil.validateError(negativeResult, index++,
                "operator '/' not defined for '(json|error)' and 'int'", 115, 13);
        BAssertUtil.validateError(negativeResult, index++,
                "operator '/' not defined for '(json|error|any[])' and 'int'", 121, 13);
        BAssertUtil.validateError(negativeResult, index++,
                "operator '/' not defined for '(json|error|any[])' and 'int'", 127, 13);
        BAssertUtil.validateError(negativeResult, index++, "operator '?:' cannot be applied to type " +
                "'NonOptionalType'", 133, 15);
        BAssertUtil.validateError(negativeResult, index++, "operator '?:' cannot be applied to type " +
                "'(int[]|string)'", 135, 15);
        Assert.assertEquals(negativeResult.getErrorCount(), index);
    }
}
