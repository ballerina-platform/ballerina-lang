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
        CompileResult resultNegative = BCompileUtil.compile("test-src/statements/transform/transform-stmt-empty.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 0);
    }

    @Test(description = "Test one to one simple transformation")
    public void testOneToOneTransform() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "oneToOneTransform", args);

        Assert.assertEquals(returns.length, 3);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "John");

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 30);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "London");
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
        Assert.assertEquals(returns[0].stringValue(), "Ms.John");

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 30);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "London");
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
                "input and output variables cannot be interchanged in transform statement", 19, 9);
        BAssertUtil.validateError(resNegative, 1,
                "input and output variables cannot be interchanged in transform statement", 19, 23);

        resNegative = BCompileUtil
                .compile("test-src/statements/transform/transform-stmt-cast-and-conversion-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 3);
        BAssertUtil.validateError(resNegative, 0, "incompatible types: expected 'string', found 'int'", 25, 15);
        BAssertUtil.validateError(resNegative, 1,
                "input and output variables cannot be interchanged in transform statement", 22, 9);
        BAssertUtil.validateError(resNegative, 2,
                "input and output variables cannot be interchanged in transform statement", 25, 9);

        resNegative = BCompileUtil
                .compile("test-src/statements/transform/transform-stmt-function-invocations-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resNegative, 0,
                "input and output variables cannot be interchanged in transform statement", 20, 9);

        resNegative = BCompileUtil
                .compile("test-src/statements/transform/transform-stmt-with-var-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resNegative, 0,
                "input and output variables cannot be interchanged in transform statement", 21, 9);

        resNegative = BCompileUtil
                .compile("test-src/statements/transform/transform-stmt-with-var-def-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resNegative, 0,
                "input and output variables cannot be interchanged in transform statement", 21, 9);
        BAssertUtil.validateError(resNegative, 1,
                                 "input and output variables cannot be interchanged in transform statement", 34, 9);

        resNegative = BCompileUtil
                .compile("test-src/statements/transform/transform-stmt-literals-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 8);
        BAssertUtil.validateError(resNegative, 0,
                                "input and output variables cannot be interchanged in transform statement", 25, 9);
        BAssertUtil.validateError(resNegative, 1,
                                 "input and output variables cannot be interchanged in transform statement", 26, 9);
        BAssertUtil.validateError(resNegative, 2,
                                 "input and output variables cannot be interchanged in transform statement", 27, 9);
        BAssertUtil.validateError(resNegative, 3,
                                 "input and output variables cannot be interchanged in transform statement", 28, 9);
        BAssertUtil.validateError(resNegative, 4,
                                 "input and output variables cannot be interchanged in transform statement", 29, 9);
        BAssertUtil.validateError(resNegative, 5,
                                 "input and output variables cannot be interchanged in transform statement", 30, 9);
        BAssertUtil.validateError(resNegative, 6,
                                 "input and output variables cannot be interchanged in transform statement", 31, 9);
        BAssertUtil.validateError(resNegative, 7,
                                 "input and output variables cannot be interchanged in transform statement", 32, 9);


        resNegative = BCompileUtil
                .compile("test-src/statements/transform/transform-stmt-operators-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resNegative, 0,
                                 "input and output variables cannot be interchanged in transform statement", 21, 9);
        BAssertUtil.validateError(resNegative, 1,
                                 "input and output variables cannot be interchanged in transform statement", 23, 9);
    }
}
