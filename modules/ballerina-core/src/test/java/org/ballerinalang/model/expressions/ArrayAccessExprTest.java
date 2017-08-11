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
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
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

    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/expressions/array-access-expr.bal");
    }

    @Test(description = "Test arrays access expression")
    public void testArrayAccessExpr() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "arrayAccessTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 210;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test arrays return value")
    public void testArrayReturnValue() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "arrayReturnTest", args);

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
        BValue[] returns = BLangFunctions.invokeNew(programFile, "arrayArgTest", args);

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
        BLangFunctions.invokeNew(programFile, "arrayIndexOutOfBoundTest");
    }
    
    @Test(description = "Test arrays access with a key",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "incorrect-array-access.bal:4: non-integer array index type 'string'")
    public void testArrayAccessWithKey() {
        BTestUtils.getProgramFile("lang/expressions/incorrect-array-access.bal");
    }
    
    @Test(description = "Test access a primitive as an arrays",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "access-primitive-as-array.bal:3: invalid operation: " +
                    "type 'string' does not support indexing")
    public void testAccessPrimitiveAsArray() {
        BTestUtils.getProgramFile("lang/expressions/access-primitive-as-array.bal");
    }


    public static void main(String[] args) {
        ArrayAccessExprTest test = new ArrayAccessExprTest();
        test.testAccessPrimitiveAsArray();
    }
}
