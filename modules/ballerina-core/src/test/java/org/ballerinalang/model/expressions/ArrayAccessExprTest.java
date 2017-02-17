/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Array access expression test.
 *
 * @since 0.8.0
 */
public class ArrayAccessExprTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/expressions/array-access-expr.bal");
    }

    @Test(description = "Test arrays access expression")
    public void testArrayAccessExpr() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayAccessTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 210;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test arrays return value")
    public void testArrayReturnValue() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayReturnTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BArray.class);

        BArray<BInteger> arrayValue = (BArray<BInteger>) returns[0];
        Assert.assertEquals(arrayValue.size(), 106);

        Assert.assertEquals(arrayValue.get(0).intValue(), 100);
        Assert.assertEquals(arrayValue.get(1).intValue(), 5);
        Assert.assertEquals(arrayValue.get(105).intValue(), 105);
    }

    @Test(description = "Test arrays arg value")
    public void testArrayArgValue() {
        BArray<BInteger> arrayValue = new BArray<>(BInteger.class);
        arrayValue.add(0, new BInteger(10));
        arrayValue.add(1, new BInteger(1));

        BValue[] args = {arrayValue};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayArgTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 11;
        Assert.assertEquals(actual, expected);
    }
    
    @Test(description = "Test accessing an out of bound arrays-index",
            expectedExceptions = { BallerinaException.class },
            expectedExceptionsMessageRegExp = "arrays index out of range: Index: 5, Size: 2")
    public void testArrayIndexOutOfBoundError() {
        BLangFunctions.invoke(bLangProgram, "arrayIndexOutOfBoundTest");
    }
    
    @Test(description = "Test arrays access with a key",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "incorrect-array-access.bal:4: non-integer array index type 'string'")
    public void testArrayAccessWithKey() {
        BTestUtils.parseBalFile("lang/expressions/incorrect-array-access.bal");
    }
    
    @Test(description = "Test access a primitive as an arrays",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "access-primitive-as-array.bal:3: invalid operation: " +
                    "type 'string' does not support indexing")
    public void testAccessPrimitiveAsArray() {
        BTestUtils.parseBalFile("lang/expressions/access-primitive-as-array.bal");
    }


    public static void main(String[] args) {
        ArrayAccessExprTest test = new ArrayAccessExprTest();
        test.testAccessPrimitiveAsArray();
    }
    
    @Test(description = "Test access an non-initialized arrays",
            expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "variable 'fruits' is null")
    public void testNonInitArrayAccess() {
        BLangFunctions.invoke(bLangProgram, "testNonInitArrayAccess");
        Assert.fail("Test should fail at this point.");
    }
}
