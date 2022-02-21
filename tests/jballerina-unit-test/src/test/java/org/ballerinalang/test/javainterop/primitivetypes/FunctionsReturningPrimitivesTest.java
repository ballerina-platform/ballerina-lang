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

import io.ballerina.runtime.internal.values.HandleValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.BRunUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains test cases to test Java interop functions returning Java primitive types.
 *
 * @since 1.0.0
 */
public class FunctionsReturningPrimitivesTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/primitive_types/functions_returning_primitives_test.bal");
    }

    @Test(description = "Test a function that returns a Java boolean value")
    public void testFuncReturningBBooleanJBoolean() {
        String receiver = "Ballerina Language";
        String strValue = "Ballerina Language";
        Object[] args = new Object[2];
        args[0] = new HandleValue(receiver);
        args[1] = new HandleValue(strValue);
        Object returns = BRunUtil.invoke(result, "testReturningBBooleanJBoolean", args);
        
        Assert.assertEquals(returns, receiver.contentEquals(strValue));
    }

    @Test(description = "Test a function that returns a Java byte value")
    public void testReturningBByteJByte() {
        Long receiver = 4L;
        Object[] args = new Object[1];
        args[0] = new HandleValue(receiver);
        Object returns = BRunUtil.invoke(result, "testReturningBByteJByte", args);
        
        Assert.assertEquals(returns, receiver.intValue());
    }

    @Test(description = "Test a function that returns a Ballerina int value")
    public void testFuncReturningJavaInt() {
        Long receiver = 10L;
        Object[] args = new Object[1];
        args[0] = new HandleValue(receiver);
        Object returns = BRunUtil.invoke(result, "testReturningBIntJLong", args);
        
        Assert.assertEquals(returns, receiver.longValue());
    }

    @Test(description = "Test a function that returns a Ballerina float value")
    public void testFuncsReturningJavaFloatDouble() {
        Double receiver = 4d;
        Object[] args = new Object[1];
        args[0] = new HandleValue(receiver);
        Object returns = BRunUtil.invoke(result, "testReturningBFloatJDouble", args);
        
        Assert.assertEquals(returns, receiver);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
