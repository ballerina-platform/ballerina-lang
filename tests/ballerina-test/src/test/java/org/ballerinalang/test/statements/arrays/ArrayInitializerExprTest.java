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
package org.ballerinalang.test.statements.arrays;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test arrays initializer expression.
 *
 * @since 0.8.0
 */
public class ArrayInitializerExprTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/array-initializer-expr.bal");
    }

    @Test(description = "Test arrays initializer expression")
    public void testArrayInitExpr() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "arrayInitTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 110;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test arrays return value")
    public void testArrayReturnValueTest() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "arrayReturnTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStringArray.class);

        BStringArray arrayValue = (BStringArray) returns[0];
        Assert.assertEquals(arrayValue.size(), 6);

        Assert.assertEquals(arrayValue.get(0), "Lion");
        Assert.assertEquals(arrayValue.get(1), "Cat");
        Assert.assertEquals(arrayValue.get(5), "Croc");
    }
    
//    @Test(description = "Test arrays initializing with different types",
//            expectedExceptions = {SemanticException.class },
//            expectedExceptionsMessageRegExp = "multi-type-array-initializer.bal:3: " +
//                    "incompatible types: 'string' cannot be assigned to 'int'")
//    public void testMultiTypeMapInit() {
//        BTestUtils.compile("test-src/statements/arrays/multi-type-array-initializer.bal");
//    }
    
    @Test(description = "Test nested array inline initializing")
    public void testNestedArrayInit() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testNestedArrayInit", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BRefValueArray.class);

        BRefValueArray arrayValue = (BRefValueArray) returns[0];
        Assert.assertEquals(arrayValue.size(), 2);

        BValue element = arrayValue.get(0);
        Assert.assertTrue(element instanceof BIntArray);
        BIntArray elementArray = (BIntArray) element;
        Assert.assertEquals(elementArray.size(), 3);
        Assert.assertEquals(elementArray.get(0), 1);
        Assert.assertEquals(elementArray.get(1), 2);
        Assert.assertEquals(elementArray.get(2), 3);
        
        element = arrayValue.get(1);
        Assert.assertTrue(element instanceof BIntArray);
        elementArray = (BIntArray) element;
        Assert.assertEquals(elementArray.size(), 4);
        Assert.assertEquals(elementArray.get(0), 6);
        Assert.assertEquals(elementArray.get(1), 7);
        Assert.assertEquals(elementArray.get(2), 8);
        Assert.assertEquals(elementArray.get(3), 9);
    }

    @Test(description = "Test array of maps inline initializing")
    public void testArrayOfMapsInit() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testArrayOfMapsInit", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BRefValueArray.class);

        BRefValueArray arrayValue = (BRefValueArray) returns[0];
        Assert.assertEquals(arrayValue.size(), 3);

        BValue adrs1 = arrayValue.get(0);
        Assert.assertTrue(adrs1 instanceof BMap<?, ?>);
        BValue address = ((BMap) adrs1).get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get("city").stringValue(), "Colombo");

        BValue adrs2 = arrayValue.get(1);
        Assert.assertTrue(adrs2 instanceof BMap<?, ?>);
        address = ((BMap) adrs2).get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get("city").stringValue(), "Kandy");

        BValue adrs3 = arrayValue.get(2);
        Assert.assertTrue(adrs3 instanceof BMap<?, ?>);
        address = ((BMap) adrs3).get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get("city").stringValue(), "Galle");
    }

    @Test(description = "Test float array initialization with integer values")
    public void testFloatArrayInitWithIntExpr() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "floatArrayInitWithInt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloatArray.class);

        BFloatArray arrayValue = (BFloatArray) returns[0];
        Assert.assertEquals(arrayValue.size(), 3);

        Assert.assertEquals(arrayValue.get(0), 2.0);
        Assert.assertEquals(arrayValue.get(1), 4.0);
        Assert.assertEquals(arrayValue.get(2), 5.0);
    }
}
