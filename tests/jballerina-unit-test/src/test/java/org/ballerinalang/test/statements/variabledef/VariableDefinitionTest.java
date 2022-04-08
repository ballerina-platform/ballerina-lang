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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        BArray returns = (BArray) BRunUtil.invoke(result, "variableDefaultValue", new Object[0]);
        Assert.assertEquals(returns.size(), 4);

        Assert.assertSame(returns.get(0).getClass(), Long.class);
        long i = (long) returns.get(0);
        Assert.assertEquals(i, 0);

        Assert.assertSame(returns.get(1).getClass(), Boolean.class);
        boolean b = (boolean) returns.get(1);
        Assert.assertFalse(b);

        Assert.assertTrue(returns.get(2) instanceof BString);
        String s = returns.get(2).toString();
        Assert.assertEquals(s, "");

        Assert.assertSame(returns.get(3).getClass(), Double.class);
        double f = (double) returns.get(3);
        Assert.assertEquals(f, 0.0f, DELTA);
    }

    @Test
    public void testInlineVarInit() {
        BArray returns = (BArray) BRunUtil.invoke(result, "inlineVarInit", new Object[0]);
        Assert.assertEquals(returns.size(), 4);

        Assert.assertSame(returns.get(0).getClass(), Long.class);
        long i = (long) returns.get(0);
        Assert.assertEquals(i, 10);

        Assert.assertSame(returns.get(1).getClass(), Boolean.class);
        boolean b = (boolean) returns.get(1);
        Assert.assertTrue(b);

        Assert.assertTrue(returns.get(2) instanceof BString);
        String s = returns.get(2).toString();
        Assert.assertEquals(s, "hello");

        Assert.assertSame(returns.get(3).getClass(), Double.class);
        double f = (double) returns.get(3);
        Assert.assertEquals(f, 2.6f, DELTA);
    }

    @Test
    public void testUpdateDefaultValue() {
        int v1 = 56;
        boolean v3 = false;
        String v4 = "newstr";
        double v5 = 68.3325f;

        Object[] args = {
                (v1), (v3), StringUtils.fromString(v4), (v5)
        };

        BArray returns = (BArray) BRunUtil.invoke(result, "updateVarValue", args);
        Assert.assertEquals(returns.size(), 4);

        Assert.assertSame(returns.get(0).getClass(), Long.class);
        long i = (long) returns.get(0);
        Assert.assertEquals(i, v1);

        Assert.assertSame(returns.get(1).getClass(), Boolean.class);
        boolean b = (boolean) returns.get(1);
        Assert.assertEquals(b, v3);

        Assert.assertTrue(returns.get(2) instanceof BString);
        String s = returns.get(2).toString();
        Assert.assertEquals(s, v4);

        Assert.assertSame(returns.get(3).getClass(), Double.class);
        double f = (double) returns.get(3);
        Assert.assertEquals(f, v5, DELTA);
    }

    @Test
    public void testUpdateVarValue() {
        int v1 = 56;
        boolean v3 = false;
        String v4 = "newstr";
        double v5 = 68.3325f;

        Object[] args = {
                (v1), (v3), StringUtils.fromString(v4), (v5)
        };

        BArray returns = (BArray) BRunUtil.invoke(result, "updateVarValue", args);
        Assert.assertEquals(returns.size(), 4);

        Assert.assertSame(returns.get(0).getClass(), Long.class);
        long i = (long) returns.get(0);
        Assert.assertEquals(i, v1);

        Assert.assertSame(returns.get(1).getClass(), Boolean.class);
        boolean b = (boolean) returns.get(1);
        Assert.assertEquals(b, v3);

        Assert.assertTrue(returns.get(2) instanceof BString);
        String s = returns.get(2).toString();
        Assert.assertEquals(s, v4);

        Assert.assertSame(returns.get(3).getClass(), Double.class);
        double f = (double) returns.get(3);
        Assert.assertEquals(f, v5, DELTA);
    }

    @Test(description = "Test defining var with wild card")
    public void wildCardLocalVariables() {
        BRunUtil.invoke(result, "wildCardLocalVariables", new Object[0]);
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

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
    }
}
