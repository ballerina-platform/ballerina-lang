/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.statements.variabledef;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for tuple variable definition.
 * <p>
 * (string, int, float) (s, i, f) = ("Test", 45, 2.3);
 *
 * @since 0.982.0
 */
public class TupleVariableDefinitionTest {

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/variabledef/tuple-variable-definition.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/variabledef/tuple-variable-definition-negative.bal");
    }

    @Test(description = "Test tuple basic variable definition")
    public void testBasic() {
        BValue[] returns = BRunUtil.invoke(result, "testBasic");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "Fooo");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 4);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 6.7);
    }

    @Test(description = "Test tuple basic recursive definition")
    public void testBasicRecursive1() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicRecursive1");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "Fooo");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 4);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 6.7);
    }

    @Test(description = "Test tuple basic recursive definition")
    public void testBasicRecursive2() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicRecursive2");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "Fooo");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 34);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 6.7);
    }

    @Test(description = "Test tuple complex recursive definition")
    public void testComplexRecursive() {
        BValue[] returns = BRunUtil.invoke(result, "testComplexRecursive");
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(returns[0].stringValue(), "Bal");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertEquals(((BByte) returns[3]).byteValue(), 34);
        Assert.assertEquals(((BFloat) returns[4]).floatValue(), 5.6);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 45);
    }


    @Test(description = "Test tuple recursive definition with expression on rhs")
    public void testRecursiveWithExpression() {
        BValue[] returns = BRunUtil.invoke(result, "testRecursiveWithExpression");
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(returns[0].stringValue(), "Bal");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertEquals(((BByte) returns[3]).byteValue(), 34);
        Assert.assertEquals(((BFloat) returns[4]).floatValue(), 5.6);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 45);
    }

    @Test(description = "Test tuple tuple binding with records and objects")
    public void testTupleBindingWithRecordsAndObjects() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleBindingWithRecordsAndObjects");
        Assert.assertEquals(returns.length, 9);
        Assert.assertEquals(returns[0].stringValue(), "Test");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 23);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 34);
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
        Assert.assertEquals(returns[4].stringValue(), "Fooo");
        Assert.assertEquals(((BFloat) returns[5]).floatValue(), 3.7);
        Assert.assertEquals(((BByte) returns[6]).byteValue(), 23);
        Assert.assertTrue(((BBoolean) returns[7]).booleanValue());
        Assert.assertEquals(((BInteger) returns[8]).intValue(), 56);
    }

    @Test
    public void testNegativeRecordVariables() {
        Assert.assertEquals(resultNegative.getErrorCount(), 9);
        int i = -1;
        String errorMsg1 = "invalid tuple binding pattern; member variable count mismatch with member type count";
        String errorMsg2 = "invalid tuple binding pattern; incompatible type found for the member variable";
        String errorMsg3 = "tuple and expression size does not match";

        BAssertUtil.validateError(resultNegative, ++i, errorMsg1, 3, 26);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1, 7, 26);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1, 8, 26);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg2, 9, 34);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg3, 13, 41);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg3, 14, 41);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'string', found 'int'", 15, 42);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'int', found 'float'", 15, 45);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'float', found 'int'", 15, 50);
    }
}
