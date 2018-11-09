/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.statements.arrays;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Array access expression test.
 *
 * @since 0.88
 */
public class ArrayAccessExprTest {

    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/array-access-expr.bal");
    }

    //TODO try to validate all the lines in the exception message
    @Test(description = "Test access an non-initialized arrays",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: array index out of range: index: 5, size: 0.*")
    public void testNonInitArrayAccess() {
//        CompileResult compileResult = BTestUtils.compile("test-src/statements/arrays/array-access-expr.bal");
        BRunUtil.invoke(compileResult, "testNonInitArrayAccess");
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Test arrays access expression")
    public void testArrayAccessExpr() {
        BValue[] args = { new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(compileResult, "arrayAccessTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 210;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test arrays return value")
    public void testArrayReturnValue() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(compileResult, "arrayReturnTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BIntArray.class);

        BIntArray arrayValue = (BIntArray) returns[0];
        Assert.assertEquals(arrayValue.size(), 106);

        Assert.assertEquals(arrayValue.get(0), 100);
        Assert.assertEquals(arrayValue.get(1), 5);
        Assert.assertEquals(arrayValue.get(105), 105);
    }

    @Test(description = "Test arrays arg value")
    public void testArrayArgValue() {
        BIntArray arrayValue = new BIntArray();
        arrayValue.add(0, 10);
        arrayValue.add(1, 1);

        BValue[] args = {arrayValue};
        BValue[] returns = BRunUtil.invoke(compileResult, "arrayArgTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 11;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test accessing an out of bound arrays-index",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*array index out of range: index: 5, size: 2.*")
    public void testArrayIndexOutOfBoundError() {
        BRunUtil.invoke(compileResult, "arrayIndexOutOfBoundTest");
    }

    @Test(description = "Test arrays access with a key")
    public void testArrayAccessWithKey() {
        CompileResult compileResult = BCompileUtil.compile("test-src/statements/arrays/incorrect-array-access.bal");
        BAssertUtil.validateError(compileResult, 0, "incompatible types: expected 'int', found 'string'", 4, 20);
    }

    @Test(description = "Test access a primitive as an arrays")
    public void testAccessPrimitiveAsArray() {
        CompileResult compileResult = BCompileUtil.compile("test-src/statements/arrays/access-primitive-as-array.bal");
        BAssertUtil.validateError(compileResult, 0, "invalid operation: type 'string' does not support indexing", 3, 5);
    }
}
