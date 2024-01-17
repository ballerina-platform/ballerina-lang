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
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains test cases to test Java interop functions accepting Java primitive values as parameters.
 *
 * @since 1.0.0
 */
public class PrimitiveTypeFunctionParamTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/primitive_types/primitive_type_function_params_test.bal");
    }

    @Test(description = "Test function that creates java.lang.Boolean instance")
    public void testCreateBoxedBoolean() {
        Object[] args = new Object[1];
        args[0] = (true);
        Object returns = BRunUtil.invoke(result, "testCreateBoxedBooleanFromBBoolean", args);
        
        Assert.assertEquals(((HandleValue) returns).getValue(), true);
    }

    @Test(description = "Test functions that create java.lang.Byte instances")
    public void testCreateBoxedByteFromBInt() {
        byte byteVal = (byte) 130;
        Object[] args = new Object[1];
        args[0] = (byteVal);
        Object returns = BRunUtil.invoke(result, "testCreateBoxedByteFromBByte", args);
        
        Assert.assertEquals(((HandleValue) returns).getValue(), byteVal);
    }

    @Test(description = "Test function that creates java.lang.Long instance")
    public void testCreateBoxedLongFromBInt() {
        Object[] args = new Object[1];
        args[0] = (100000000);
        Object returns = BRunUtil.invoke(result, "testCreateBoxedLongFromBInt", args);
        
        Assert.assertEquals(((HandleValue) returns).getValue(), (long) 100000000);
    }

    @Test(description = "Test function that creates java.lang.Double instance")
    public void testCreateBoxedDoubleFromBFloat() {
        Object[] args = new Object[1];
        args[0] = (30000000.00);
        Object returns = BRunUtil.invoke(result, "testCreateBoxedDoubleFromBFloat", args);
        
        Assert.assertEquals(((HandleValue) returns).getValue(), 30000000.00d);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
