/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.nullable;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This class contains nullable types related test cases.
 */
public class NullableTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/nullable/nullable_type_basics.bal");
    }

    @Test(description = "Test basics of nullable types")
    public void testNullableTypeBasics1() {
        Object returns = BRunUtil.invoke(result, "testNullableTypeBasics1", new Object[]{});

        Assert.assertSame(returns.getClass(), Long.class);
        Assert.assertEquals(returns, 5L, "Invalid int value returned.");
    }

    @Test(description = "Test basics of nullable types")
    public void testNullableTypeBasics2() {
        Object returns = BRunUtil.invoke(result, "testNullableTypeBasics2", new Object[]{});

        Assert.assertNull(returns);
    }

    @Test(description = "Test basics of nullable types")
    public void testNullableArrayTypes1() {
        Object returns = BRunUtil.invoke(result, "testNullableArrayTypes1", new Object[]{});

        Assert.assertSame(returns.getClass(), Double.class);
        Assert.assertEquals(returns, 1.0, "Invalid float value returned.");
    }

    @Test(description = "Test referring to a user defined type in a type test")
    public void testNilableTypeInTypeTest() {
        Object returns = BRunUtil.invoke(result, "testNilableTypeInTypeTest");
        Assert.assertEquals(returns.toString(), "mixed");
    }

    @Test(description = "Test iterating over an array of optional typed values")
    public void testNilableTypeArrayIteration() {
        CompileResult result = BCompileUtil.compile("test-src/types/nullable/nilable_types_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        validateError(result, 0, "incompatible types: expected 'BType', found 'BType?'", 33, 19);
    }

    @Test(dataProvider = "dataToTestNullUsageWithDifferentTypes", description = "Test null in JSON related context")
    public void testNullUsageWithDifferentTypes(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestNullUsageWithDifferentTypes() {
        return new Object[]{
                "testNullWithBasicTypes",
                "testNullWithMap",
                "testNullWithMap2",
                "testNullWithArray",
                "testNullWithType"
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
