/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.transform;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TransformStmtTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/transform/transform-stmt.bal");
    }

    @Test(description = "Test empty transformation")
    public void testEmptyTransform() {
        CompileResult result = BCompileUtil.compile("test-src/statements/transform/transform-stmt-empty.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
        BValue[] values = BRunUtil.invoke(result, "emptyTransform");
        Assert.assertNotEquals(values[0], null);
        Assert.assertEquals(values[0].stringValue(), "{name:\"\", age:0, address:\"\"}");
    }

    @Test(description = "Test simple unnamed transformer")
    public void unnamedTransform() {
        BValue[] returns = BRunUtil.invoke(result, "unnamedTransform");

        Assert.assertEquals(returns.length, 3);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "John");

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 30);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "London");
    }

    @Test(description = "Test simple named transformer")
    public void namedTransform() {
        BValue[] returns = BRunUtil.invoke(result, "namedTransform");

        Assert.assertEquals(returns.length, 3);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jane");

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Paris");
    }

    @Test(description = "Test simple named transformer with parameters")
    public void namedTransformWithParams() {
        BValue[] returns = BRunUtil.invoke(result, "namedTransformWithParams");

        Assert.assertEquals(returns.length, 3);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jack");

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 99);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "NY");
    }

    @Test(description = "Test one to one simple transformation")
    public void testFunctionsInTransform() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "functionsInTransform", args);

        Assert.assertEquals(returns.length, 3);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Mr.John");

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 30);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "London");
    }

    @Test(description = "Test one to one simple transformation with var for temporary variables")
    public void testVarInTransform() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "varInTransform", args);

        Assert.assertEquals(returns.length, 3);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Mr.John");

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 30);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "London");
    }

    @Test(description = "Test one to one simple transformation with new variable definitions")
    public void testVarDefInTransform() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "varDefInTransform", args);

        Assert.assertEquals(returns.length, 3);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Ms.Jane");

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 28);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "CA");
    }

    @Test(description = "Test one to one simple transformation with type cast and conversion")
    public void testCastAndConversionInTransform() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "castAndConversionInTransform", args);

        Assert.assertEquals(returns.length, 4);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Mr.John");

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 20);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "New York");

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(returns[3].stringValue(), "30");
    }

    @Test(description = "Test transform statement with errors")
    public void testTransformNegativeCases() {
        CompileResult resNegative = BCompileUtil.compile("test-src/statements/transform/transform-stmt-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resNegative, 0,
                "invalid usage of variable 'p': inputs cannot be updated inside transformer block", 22, 5);
        BAssertUtil.validateError(resNegative, 1,
                "invalid usage of variable 'e': outputs cannot be used in rhs expressions inside transformer block", 22,
                19);

        resNegative =
                BCompileUtil.compile("test-src/statements/transform/transform-stmt-cast-and-conversion-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 3);
        BAssertUtil.validateError(resNegative, 0, "incompatible types: expected 'string', found 'int'", 32, 11);
        BAssertUtil.validateError(resNegative, 1,
                "invalid usage of variable 'defaultAddress': inputs cannot be updated inside transformer block", 29, 5);
        BAssertUtil.validateError(resNegative, 2,
                "invalid usage of variable 'age': inputs cannot be updated inside transformer block", 32, 5);

        resNegative =
                BCompileUtil.compile("test-src/statements/transform/transform-stmt-function-invocations-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resNegative, 0,
                "invalid usage of variable 'e': outputs cannot be used in rhs expressions inside transformer block", 26,
                30);

        resNegative = BCompileUtil.compile("test-src/statements/transform/transform-stmt-with-var-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resNegative, 0,
                "invalid usage of variable 'temp': inputs cannot be updated inside transformer block", 28, 5);

        resNegative = BCompileUtil.compile("test-src/statements/transform/transform-stmt-with-var-def-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resNegative, 0,
                "invalid usage of variable 'prefix': inputs cannot be updated inside transformer block", 38, 5);
        BAssertUtil.validateError(resNegative, 1,
                "invalid usage of variable 'name': inputs cannot be updated inside transformer block", 46, 5);

        resNegative = BCompileUtil.compile("test-src/statements/transform/transform-stmt-literals-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 8);
        BAssertUtil.validateError(resNegative, 0,
                "invalid usage of variable 'str2': inputs cannot be updated inside transformer block", 34, 5);
        BAssertUtil.validateError(resNegative, 1,
                "invalid usage of variable 'flag2': inputs cannot be updated inside transformer block", 35, 5);
        BAssertUtil.validateError(resNegative, 2,
                "invalid usage of variable 'x2': inputs cannot be updated inside transformer block", 36, 5);
        BAssertUtil.validateError(resNegative, 3,
                "invalid usage of variable 'json2': inputs cannot be updated inside transformer block", 37, 5);
        BAssertUtil.validateError(resNegative, 4,
                "invalid usage of variable 'arr2': inputs cannot be updated inside transformer block", 38, 5);
        BAssertUtil.validateError(resNegative, 5,
                "invalid usage of variable 'map2': inputs cannot be updated inside transformer block", 39, 5);
        BAssertUtil.validateError(resNegative, 6,
                "invalid usage of variable 'p2': inputs cannot be updated inside transformer block", 40, 5);
        BAssertUtil.validateError(resNegative, 7,
                "invalid usage of variable 'jsonP2': inputs cannot be updated inside transformer block", 41, 5);

        resNegative = BCompileUtil.compile("test-src/statements/transform/transform-stmt-operators-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resNegative, 0,
                "invalid usage of variable 'p': inputs cannot be updated inside transformer block", 24, 5);
        BAssertUtil.validateError(resNegative, 1,
                "invalid usage of variable 'p': inputs cannot be updated inside transformer block", 26, 5);
    }
}
