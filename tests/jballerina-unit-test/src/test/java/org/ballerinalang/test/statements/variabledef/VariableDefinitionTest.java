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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Cases for defining variable.
 */
public class VariableDefinitionTest {

    private static final double DELTA = 0.01;
    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/variabledef/variable-definition-stmt.bal");
    }

    @Test
    public void testVariableDefaultValue() {
        BValue[] returns = BRunUtil.invoke(result, "variableDefaultValue", new BValue[0]);
        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long i = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(i, 0);

        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        boolean b = ((BBoolean) returns[1]).booleanValue();
        Assert.assertFalse(b);

        Assert.assertSame(returns[2].getClass(), BString.class);
        String s = returns[2].stringValue();
        Assert.assertEquals(s, "");

        Assert.assertSame(returns[3].getClass(), BFloat.class);
        double f = ((BFloat) returns[3]).floatValue();
        Assert.assertEquals(f, 0.0f, DELTA);
    }

    @Test
    public void testInlineVarInit() {
        BValue[] returns = BRunUtil.invoke(result, "inlineVarInit", new BValue[0]);
        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long i = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(i, 10);

        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        boolean b = ((BBoolean) returns[1]).booleanValue();
        Assert.assertTrue(b);

        Assert.assertSame(returns[2].getClass(), BString.class);
        String s = returns[2].stringValue();
        Assert.assertEquals(s, "hello");

        Assert.assertSame(returns[3].getClass(), BFloat.class);
        double f = ((BFloat) returns[3]).floatValue();
        Assert.assertEquals(f, 2.6f, DELTA);
    }

    @Test
    public void testUpdateDefaultValue() {
        int v1 = 56;
        boolean v3 = false;
        String v4 = "newstr";
        double v5 = 68.3325f;

        BValue[] args = {
                new BInteger(v1), new BBoolean(v3), new BString(v4), new BFloat(v5)
        };

        BValue[] returns = BRunUtil.invoke(result, "updateVarValue", args);
        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long i = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(i, v1);

        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        boolean b = ((BBoolean) returns[1]).booleanValue();
        Assert.assertEquals(b, v3);

        Assert.assertSame(returns[2].getClass(), BString.class);
        String s = returns[2].stringValue();
        Assert.assertEquals(s, v4);

        Assert.assertSame(returns[3].getClass(), BFloat.class);
        double f = ((BFloat) returns[3]).floatValue();
        Assert.assertEquals(f, v5, DELTA);
    }

    @Test
    public void testUpdateVarValue() {
        int v1 = 56;
        boolean v3 = false;
        String v4 = "newstr";
        double v5 = 68.3325f;

        BValue[] args = {
                new BInteger(v1), new BBoolean(v3), new BString(v4), new BFloat(v5)
        };

        BValue[] returns = BRunUtil.invoke(result, "updateVarValue", args);
        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long i = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(i, v1);

        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        boolean b = ((BBoolean) returns[1]).booleanValue();
        Assert.assertEquals(b, v3);

        Assert.assertSame(returns[2].getClass(), BString.class);
        String s = returns[2].stringValue();
        Assert.assertEquals(s, v4);

        Assert.assertSame(returns[3].getClass(), BFloat.class);
        double f = ((BFloat) returns[3]).floatValue();
        Assert.assertEquals(f, v5, DELTA);
    }

    @Test(description = "Test variable definition negative test cases with errors")
    public void testUnsupportedTypeVariable() {
        resultNegative = BCompileUtil
                .compile("test-src/statements/variabledef/variable-def-unsupported-variables-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "unknown type 'Foo'", 2, 5);
    }

    @Test(description = "Test variable definition negative test cases with errors")
    public void testDuplicateConstVariables() {
        resultNegative = BCompileUtil
                .compile("test-src/statements/variabledef/variable-def-duplicate-constant-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "redeclared symbol 'b'", 2, 13);
    }

    @Test(description = "Test variable definition negative test cases with errors")
    public void testDuplicateVariables() {
        resultNegative = BCompileUtil
                .compile("test-src/statements/variabledef/variable-def-duplicate-variables-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "redeclared symbol 'b'", 5, 11);
    }

    @Test(description = "Test variable definition negative test cases with errors")
    public void testUndeclaredVariables() {
        resultNegative = BCompileUtil
                .compile("test-src/statements/variabledef/variable-def-undeclared-variables-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "undefined symbol 'a'", 2, 12);
    }

    @Test(description = "Test defining a constant from an arrays type")
    public void testArrayTypeConstant() {
        resultNegative = BCompileUtil
                .compile("test-src/statements/variabledef/variable-def-array-constants-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "incompatible types: expected 'int[]', found 'int'", 1, 17);
    }
}
