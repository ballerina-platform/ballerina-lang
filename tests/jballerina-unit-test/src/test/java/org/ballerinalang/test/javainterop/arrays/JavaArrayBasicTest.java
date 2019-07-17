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
package org.ballerinalang.test.javainterop.arrays;

import org.ballerinalang.model.values.BHandleValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test cases for java interop with reference types arrays.
 *
 * @since 1.0.0
 */
public class JavaArrayBasicTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/arrays/java_array_passing_tests.bal");
    }

    @Test(description = "Test passing Java int array objects")
    public void testPassingJavaIntArray() {
        int[] values = {5, 6, 2, 9, 3, 7};
        int[] valuesCopy = {5, 6, 2, 9, 3, 7};
        Arrays.sort(valuesCopy);
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(values);
        BValue[] returns = BRunUtil.invoke(result, "testPassingJavaIntArray", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), valuesCopy);
    }

    @Test(description = "Test passing Java array objects")
    public void testPassingJavaStringArray() {
        String[] names = {"John", "Jane", "Peter", "Amber", "Autumn", "Harold"};
        String[] namesCopy = {"John", "Jane", "Peter", "Amber", "Autumn", "Harold"};
        Arrays.sort(namesCopy);
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(names);
        BValue[] returns = BRunUtil.invoke(result, "testPassingJavaStringArray", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), namesCopy);
    }

    @Test(description = "Test Returning Java string array")
    public void testReturningSortedJavaStringArray() {
        BValue[] returns = BRunUtil.invoke(result, "testReturningSortedJavaStringArray");
        Assert.assertEquals(returns.length, 1);

        String strValue = "Ballerina Programming Language Specification";
        String[] parts = strValue.split(" ");
        Arrays.sort(parts);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), parts);
    }
}
