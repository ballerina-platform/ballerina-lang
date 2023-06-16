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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for tuple rest descriptor.
 */
public class TupleRestDescriptorTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/tuples/tuple_rest_descriptor_test.bal");
    }

    @Test(description = "Test tuple assignment with rest descriptor")
    public void testBasicTupleAssignment() {
        Object returns = BRunUtil.invoke(result, "basicTupleAssignment", new Object[]{});

        Assert.assertEquals(returns.toString(), "[1,\"s\",true,true] [2,\"s\",true] [3,\"s\"]");
    }

    @Test(description = "Test tuple assignment with nil `()` rest descriptor")
    public void testTupleAssignmentWithNilRestDescriptor() {
        Object returns = BRunUtil.invoke(result, "tupleAssignmentWithNilRestDescriptor", new Object[]{});

        Assert.assertEquals(returns.toString(), "[1,\"s\"] [1,\"s\",null] [1,\"s\",null,null] [null,null]");
    }

    @Test(description = "Test tuple assignment with only rest descriptor")
    public void testTupleAssignmentWithOnlyRestDescriptor() {
        Object returns = BRunUtil.invoke(result, "tupleAssignmentWithOnlyRestDescriptor", new Object[]{});

        Assert.assertEquals(returns.toString(), "[1,2] [\"s\",\"s\"] [null,null]");
    }

    @Test(description = "Test tuple covariance with rest descriptor")
    public void testTupleCovarianceWithRestDescriptor() {
        Object returns = BRunUtil.invoke(result, "tupleCovarianceWithRestDescriptor", new Object[]{});

        Assert.assertEquals(returns.toString(), "s {\"name\":\"John\",\"intern\":true} {\"name\":\"John\"," +
                "\"intern\":true} {\"name\":\"John\",\"intern\":true} ");
    }

    @Test(description = "Test tuple covariance with only rest descriptor")
    public void tupleCovarianceWithOnlyRestDescriptor() {
        Object returns = BRunUtil.invoke(result, "tupleCovarianceWithOnlyRestDescriptor", new Object[]{});

        Assert.assertEquals(returns.toString(), "{\"name\":\"John\",\"intern\":true} {\"name\":\"John\"," +
                "\"intern\":true} {\"name\":\"John\",\"intern\":true} ");
    }

    @Test(description = "Test function invocation with tuples with rest descriptor")
    public void testFunctionInvocation() {
        Object returns = BRunUtil.invoke(result, "testFunctionInvocation", new Object[]{});

        Assert.assertEquals(returns.toString(), "y 5.0 true false ");
    }

    @Test(description = "Test function return value with tuple assignment with rest descriptor")
    public void testFunctionReturnValue() {
        Object returns = BRunUtil.invoke(result, "testFunctionReturnValue", new Object[]{});

        Assert.assertEquals(returns.toString(), "x 5.0 true false ");
    }

    @Test(description = "Test indexed based access on tuples with rest descriptor")
    public void testIndexBasedAccess() {
        Object returns = BRunUtil.invoke(result, "testIndexBasedAccess", new Object[]{});

        Assert.assertEquals(returns.toString(), "false a1 b1 c1 ");
    }

    @Test
    public void testToStringRepresentation() {
        BRunUtil.invoke(result, "testToStringRepresentation");
    }

    @Test
    public void testSubTypingWithRestDescriptorPositive() {
        BRunUtil.invoke(result, "testSubTypingWithRestDescriptorPositive");
    }

    @Test
    public void testSubTypingWithRestDescriptorNegative() {
        BRunUtil.invoke(result, "testSubTypingWithRestDescriptorNegative");
    }

    @Test
    public void testRestVariablesWithArray() {
        BRunUtil.invoke(result, "testRestVariablesWithArray");
    }

    @Test(description = "Test out of bound indexed based access on tuples with rest descriptor",
            expectedExceptions = {BLangTestException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}IndexOutOfRange \\{\"message\":\"tuple index out of range: " +
                            "index: 4, size: 4.*")
    public void testIndexBasedAccessNegative() {
        BRunUtil.invoke(result, "testIndexBasedAccessNegative");
    }

    @Test(description = "Test negative scenarios of assigning tuples with rest descriptors")
    public void testNegativeTupleLiteralAssignments() {
        CompileResult resultNegative = BCompileUtil.compile("test-src/types/tuples/tuple_rest_descriptor_negative.bal");

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
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '[int,(string|int),(string|int)...]', " +
                        "found '[int,(string|int)...]'", 38, 42);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '[int,string...]', found '[int,(string|int)...]'",
                39, 26);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '[int]', found '[int,(string|int)...]'",
                40, 15);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '[int,int,int,int,int...]', found 'int[3]'",
                44, 38);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '[int,string...]', found 'int[3]'",
                45, 26);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '[string,string,string,string]', found 'int[3]'",
                46, 42);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
