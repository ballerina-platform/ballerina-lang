/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.javainterop.primitivetypes;

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BHandleValue;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains test cases to test Java interop functions returning Java primitive types.
 *
 * @since 1.0.0
 */
public class PrimitiveConversionInFunctionReturnsTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(
                "test-src/javainterop/primitive_types/widening_primitive_conversion_in_function_returns.bal");
    }

    @Test(description = "Test functions that return a Ballerina int values")
    public void testFuncReturningJavaInt() {
        Long receiver = 10L;
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(receiver);
        BValue[] returns = BRunUtil.invoke(result, "testReturningBIntJByte", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((byte) (((BInteger) returns[0]).intValue()), receiver.byteValue());

        returns = BRunUtil.invoke(result, "testReturningBIntJShort", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((short) (((BInteger) returns[0]).intValue()), receiver.shortValue());

        returns = BRunUtil.invoke(result, "testReturningBIntJInt", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((int) (((BInteger) returns[0]).intValue()), receiver.intValue());

        Character charValue = (char) 68;
        args[0] = new BHandleValue(charValue);
        returns = BRunUtil.invoke(result, "testReturningBIntJChar", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((char) (((BInteger) returns[0]).intValue()), charValue + 3);
    }

    @Test(description = "Test functions that return Ballerina float values")
    public void testFuncsReturningJavaFloatDouble() {
        Double receiver = 4d;
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(receiver);
        BValue[] returns = BRunUtil.invoke(result, "testReturningBFloatJByte", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((byte) ((BFloat) returns[0]).floatValue()), receiver.byteValue());

        returns = BRunUtil.invoke(result, "testReturningBFloatJShort", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((short) ((BFloat) returns[0]).floatValue()), receiver.shortValue());

        returns = BRunUtil.invoke(result, "testReturningBFloatJInt", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((int) ((BFloat) returns[0]).floatValue()), receiver.intValue());

        returns = BRunUtil.invoke(result, "testReturningBFloatJFloat", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((float) ((BFloat) returns[0]).floatValue()), receiver.floatValue());

        Character charValue = (char) 68;
        args[0] = new BHandleValue(charValue);
        returns = BRunUtil.invoke(result, "testReturningBFloatJChar", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((char) (((BFloat) returns[0]).floatValue()), charValue.charValue());
    }
}
