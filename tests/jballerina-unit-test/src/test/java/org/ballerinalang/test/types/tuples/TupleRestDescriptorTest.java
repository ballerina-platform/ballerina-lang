/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.tuples;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for tuple rest descriptor.
 */
public class TupleRestDescriptorTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/tuples/tuple_rest_descriptor_test.bal");
        resultNegative = BCompileUtil.compile("test-src/types/tuples/tuple_rest_descriptor_negative.bal");
    }

    @Test(description = "Test tuple assignment with rest descriptor")
    public void testBasicTupleAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "basicTupleAssignment", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1 s true true 2 s true 3 s");
    }

    @Test(description = "Test tuple assignment with nil `()` rest descriptor")
    public void testTupleAssignmentWithNilRestDescriptor() {
        BValue[] returns = BRunUtil.invoke(result, "tupleAssignmentWithNilRestDescriptor", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1 s 1 s  1 s    ");
    }

    @Test(description = "Test tuple assignment with only rest descriptor")
    public void testTupleAssignmentWithOnlyRestDescriptor() {
        BValue[] returns = BRunUtil.invoke(result, "tupleAssignmentWithOnlyRestDescriptor", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1 2 s s  ");
    }

    @Test(description = "Test tuple covariance with rest descriptor")
    public void testTupleCovarianceWithRestDescriptor() {
        BValue[] returns = BRunUtil.invoke(result, "tupleCovarianceWithRestDescriptor", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "s {\"name\":\"John\",\"intern\":true} {\"name\":\"John\"," +
                "\"intern\":true} {\"name\":\"John\",\"intern\":true} ");
    }

    @Test(description = "Test tuple covariance with only rest descriptor")
    public void tupleCovarianceWithOnlyRestDescriptor() {
        BValue[] returns = BRunUtil.invoke(result, "tupleCovarianceWithOnlyRestDescriptor", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"John\",\"intern\":true} {\"name\":\"John\"," +
                "\"intern\":true} {\"name\":\"John\",\"intern\":true} ");
    }

    @Test(description = "Test function invocation with tuples with rest descriptor")
    public void testFunctionInvocation() {
        BValue[] returns = BRunUtil.invoke(result, "testFunctionInvocation", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "y 5.0 true false ");
    }

    @Test(description = "Test function return value with tuple assignment with rest descriptor")
    public void testFunctionReturnValue() {
        BValue[] returns = BRunUtil.invoke(result, "testFunctionReturnValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "x 5.0 true false ");
    }

    @Test(description = "Test indexed based access on tuples with rest descriptor")
    public void testIndexBasedAccess() {
        BValue[] returns = BRunUtil.invoke(result, "testIndexBasedAccess", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "false a1 b1 c1 ");
    }

    @Test(description = "Test out of bound indexed based access on tuples with rest descriptor",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}IndexOutOfRange \\{\"message\":\"tuple index out of range: " +
                            "index: 4, size: 4.*")
    public void testIndexBasedAccessNegative() {
        BRunUtil.invoke(result, "testIndexBasedAccessNegative");
    }

    @Test(description = "Test negative scenarios of assigning tuples with rest descriptors")
    public void testNegativeTupleLiteralAssignments() {
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'boolean', found 'int'",
                18, 50);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'boolean', found 'string'",
                18, 60);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'boolean', found 'int'",
                19, 24);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'boolean', found 'int'",
                19, 33);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'boolean', found 'string'",
                19, 36);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '([string,int,boolean]|string)'," +
                                          " found '[string,int,boolean,boolean]'", 24, 12);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '[int,int,string...]', found " +
                                          "'[int]'", 29, 31);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '[int,float,string,string...]', " +
                                          "found '[int,float,string...]'", 32, 41);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }

}
