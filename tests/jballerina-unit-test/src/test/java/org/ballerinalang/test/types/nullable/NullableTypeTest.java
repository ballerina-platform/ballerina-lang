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

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

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
        BValue[] returns = BRunUtil.invoke(result, "testNullableTypeBasics1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5, "Invalid int value returned.");
    }

    @Test(description = "Test basics of nullable types")
    public void testNullableTypeBasics2() {
        BValue[] returns = BRunUtil.invoke(result, "testNullableTypeBasics2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test basics of nullable types")
    public void testNullableArrayTypes1() {
        BValue[] returns = BRunUtil.invoke(result, "testNullableArrayTypes1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 1.0, "Invalid float value returned.");
    }

    @Test(description = "Test referring to a user defined type in a type test")
    public void testNilableTypeInTypeTest() {
        BValue[] returns = BRunUtil.invoke(result, "testNilableTypeInTypeTest");
        Assert.assertEquals(returns[0].stringValue(), "mixed");
    }

    @Test(description = "Test iterating over an array of optional typed values")
    public void testNilableTypeArrayIteration() {
        CompileResult result = BCompileUtil.compile("test-src/types/nullable/nilable_types_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        validateError(result, 0, "incompatible types: expected '(()|any)', found '(()|any)?'", 33, 19);
    }
}
