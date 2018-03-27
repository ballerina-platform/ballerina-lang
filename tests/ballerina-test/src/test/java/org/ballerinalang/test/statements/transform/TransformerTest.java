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

/**
 * Class to test functionality of transformer.
 */
public class TransformerTest {

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

        Assert.assertEquals(returns.length, 1);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "John30London");
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
        BValue[] returns = BRunUtil.invoke(result, "functionsInTransform");

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
        BValue[] returns = BRunUtil.invoke(result, "varDefInTransform");

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
        BValue[] returns = BRunUtil.invoke(result, "castAndConversionInTransform");

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

    @Test(description = "Test invalid variable usages in transformer")
    public void testTransformerInvalidVarUsage() {
        CompileResult resNegative = BCompileUtil.compile("test-src/statements/transform/transform-stmt-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resNegative, 0, "invalid usage of variable 'p': source or input parameters cannot " +
                "be updated inside transformer block", 22, 5);
        BAssertUtil.validateError(resNegative, 1, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 22, 19);

        resNegative =
                BCompileUtil.compile("test-src/statements/transform/transform-stmt-cast-and-conversion-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 3);
        BAssertUtil.validateError(resNegative, 0, "incompatible types: expected 'string', found 'int'", 32, 11);
        BAssertUtil.validateError(resNegative, 1, "invalid usage of variable 'defaultAddress': source or input " +
                "parameters cannot be updated inside transformer block", 29, 5);
        BAssertUtil.validateError(resNegative, 2, "invalid usage of variable 'age': source or input parameters " +
                "cannot be updated inside transformer block", 32, 5);

        resNegative =
                BCompileUtil.compile("test-src/statements/transform/transform-stmt-function-invocations-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resNegative, 0, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 26, 30);

        resNegative = BCompileUtil.compile("test-src/statements/transform/transform-stmt-with-var-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resNegative, 0, "invalid usage of variable 'temp': source or input parameters " +
                "cannot be updated inside transformer block", 28, 5);

        resNegative = BCompileUtil.compile("test-src/statements/transform/transform-stmt-with-var-def-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resNegative, 0, "invalid usage of variable 'prefix': source or input parameters " +
                "cannot be updated inside transformer block", 38, 5);
        BAssertUtil.validateError(resNegative, 1, "invalid usage of variable 'name': source or input parameters " +
                "cannot be updated inside transformer block", 46, 5);

        resNegative = BCompileUtil.compile("test-src/statements/transform/transform-stmt-literals-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 8);
        BAssertUtil.validateError(resNegative, 0, "invalid usage of variable 'str2': source or input parameters " +
                "cannot be updated inside transformer block", 34, 5);
        BAssertUtil.validateError(resNegative, 1, "invalid usage of variable 'flag2': source or input parameters " +
                "cannot be updated inside transformer block", 35, 5);
        BAssertUtil.validateError(resNegative, 2, "invalid usage of variable 'x2': source or input parameters " +
                "cannot be updated inside transformer block", 36, 5);
        BAssertUtil.validateError(resNegative, 3, "invalid usage of variable 'json2': source or input parameters " +
                "cannot be updated inside transformer block", 37, 5);
        BAssertUtil.validateError(resNegative, 4, "invalid usage of variable 'arr2': source or input parameters " +
                "cannot be updated inside transformer block", 38, 5);
        BAssertUtil.validateError(resNegative, 5, "invalid usage of variable 'map2': source or input parameters " +
                "cannot be updated inside transformer block", 39, 5);
        BAssertUtil.validateError(resNegative, 6, "invalid usage of variable 'p2': source or input parameters " +
                "cannot be updated inside transformer block", 40, 5);
        BAssertUtil.validateError(resNegative, 7, "invalid usage of variable 'jsonP2': source or input parameters " +
                "cannot be updated inside transformer block", 41, 5);

        resNegative = BCompileUtil.compile("test-src/statements/transform/transform-stmt-operators-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resNegative, 0, "invalid usage of variable 'p': source or input parameters " +
                "cannot be updated inside transformer block", 24, 5);
        BAssertUtil.validateError(resNegative, 1, "invalid usage of variable 'p': source or input parameters " +
                "cannot be updated inside transformer block", 26, 5);
    }

    @Test(enabled = false)
    public void testAllExpressionsAsRHSInTransformer() {
        CompileResult resNegative =
                BCompileUtil.compile("test-src/statements/transform/transform-all-rhs-expressions-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 18);
        BAssertUtil.validateError(resNegative, 0, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 70, 19);
        BAssertUtil.validateError(resNegative, 1, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 73, 19);
        BAssertUtil.validateError(resNegative, 2, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 73, 39);
        BAssertUtil.validateError(resNegative, 3, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 76, 20);
        BAssertUtil.validateError(resNegative, 4, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 79, 35);
        BAssertUtil.validateError(resNegative, 5, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 79, 25);
        BAssertUtil.validateError(resNegative, 6, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 82, 22);
        BAssertUtil.validateError(resNegative, 7, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 85, 27);
        BAssertUtil.validateError(resNegative, 8, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 88, 23);
        BAssertUtil.validateError(resNegative, 9, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 95, 21);
        BAssertUtil.validateError(resNegative, 10, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 95, 33);
        BAssertUtil.validateError(resNegative, 11, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 95, 45);
        BAssertUtil.validateError(resNegative, 12, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 95, 71);
        BAssertUtil.validateError(resNegative, 13, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 95, 92);
        BAssertUtil.validateError(resNegative, 14, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 95, 109);
        BAssertUtil.validateError(resNegative, 15, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 95, 124);
        BAssertUtil.validateError(resNegative, 16, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 97, 20);
        BAssertUtil.validateError(resNegative, 17, "invalid usage of variable 'e': target cannot be used in rhs " +
                "expressions inside transformer block", 100, 33);
    }

    @Test(enabled = false)
    public void testTransformerNegative() {
        int i = 0;
        CompileResult resNegative =
                BCompileUtil.compile("test-src/statements/transform/transformer-define-negative.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 21);
        BAssertUtil.validateError(resNegative, i++, "redeclared symbol 'Foo_1'", 42, 1);
        BAssertUtil.validateError(resNegative, i++, "redeclared symbol 'Foo_1'", 46, 1);
        BAssertUtil.validateError(resNegative, i++, "redeclared symbol 'transformer<Person,Employee>'", 58, 1);
        
        // transformer conflicting with built-in conversions
        BAssertUtil.validateError(resNegative, i++,
                "invalid transformer: type conversion already exists from 'int' to 'string'", 96, 1);
        
        BAssertUtil.validateError(resNegative, i++, "incompatible types: expected 'Person', found 'Employee'", 17, 28);
        BAssertUtil.validateError(resNegative, i++, "incompatible types: expected 'Person', found 'Employee'", 19, 28);
        BAssertUtil.validateError(resNegative, i++, "incompatible types: expected 'Person', found 'Employee'", 21, 17);
        BAssertUtil.validateError(resNegative, i++, "incompatible types: expected 'Person', found 'Employee'", 23, 26);
        BAssertUtil.validateError(resNegative, i++, "incompatible types: expected 'string', found 'Employee'", 25, 28);
        BAssertUtil.validateError(resNegative, i++, "assignment count mismatch: expected 2 values, but found 1", 29,
                13);
        BAssertUtil.validateError(resNegative, i++, "assignment count mismatch: expected 3 values, but found 1", 31,
                22);
        BAssertUtil.validateError(resNegative, i++, "too many outputs for transformer: expected 1, found 3", 62, 1);

        BAssertUtil.validateError(resNegative, i++,
                "incompatible types: 'TestConnector' is not supported by the transformer", 100, 14);
        BAssertUtil.validateError(resNegative, i++,
                "incompatible types: 'TestConnector' is not supported by the transformer", 104, 25);

        // BAssertUtil.validateError(resNegative, 14, "'connector init' statement is not allowed inside a transformer",
        // 67, 35);
        BAssertUtil.validateError(resNegative, i++,
                "'action invocation' statement is not allowed inside a transformer", 69, 16);
        BAssertUtil.validateError(resNegative, i++, "'return' statement is not allowed inside a transformer", 71, 5);
        BAssertUtil.validateError(resNegative, i++, "'try' statement is not allowed inside a transformer", 73, 5);
        BAssertUtil.validateError(resNegative, i++, "'if' statement is not allowed inside a transformer", 77, 5);
        BAssertUtil.validateError(resNegative, i++, "'while' statement is not allowed inside a transformer", 81, 5);
        BAssertUtil.validateError(resNegative, i++, "'invocation' statement is not allowed inside a transformer", 84,
                5);
        BAssertUtil.validateError(resNegative, i++, "'next' statement is not allowed inside a transformer", 86, 5);
    }

    @Test(enabled = false)
    public void testTransformerPackage() {
        CompileResult result = BCompileUtil.compile(this, "test-src/statements/transform/", "a.b");
        BValue[] returns = BRunUtil.invoke(result, "a.b", "testTransformer");
        Assert.assertEquals(returns.length, 3);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jane");

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Paris");
    }

    @Test(description = "Test iterable operations inside transformer")
    public void testTransformIterableOperations() {
        BValue[] args = {};
        CompileResult result = BCompileUtil.compile("test-src/statements/transform/transform-iterable-operations.bal");
        BValue[] returns = BRunUtil.invoke(result, "testTransformerIterableOperations", args);
        Assert.assertEquals(returns.length, 3);

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 3);
    }
}
