/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.expressions.lambda;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for Function pointers and lambda.
 *
 * @since 0.90
 */
public class FunctionPointersTest {

    CompileResult fpProgram, globalProgram, structProgram;

    @BeforeClass
    public void setup() {
        fpProgram = BTestUtils.compile("test-src/expressions/lambda/function-pointers.bal");
        globalProgram = BTestUtils.compile("test-src/expressions/lambda/global-function-pointers.bal");
        structProgram = BTestUtils.compile("test-src/expressions/lambda/struct-function-pointers.bal");
    }

    @Test
    public void testFunctionPointerAsVariable() {
        BValue[] returns = BTestUtils.invoke(fpProgram, "test1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }

    @Test
    public void testFunctionPointerAsLambda() {
        BValue[] returns = BTestUtils.invoke(fpProgram, "test2");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "sum is 3");
    }

    @Test
    public void testFunctionPointerAsParameter() {
        BValue[] returns = BTestUtils.invoke(fpProgram, "test3");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test
    public void testLambdaAsReturnParameter() {
        BValue[] returns = BTestUtils.invoke(fpProgram, "test4");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "hello world.");
    }

    @Test
    public void testFunctionPointerAsReturnParameter() {
        BValue[] args = new BValue[0];
        BValue[] returns = BTestUtils.invoke(fpProgram, "test5");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test5 string1.0");
    }


    @Test
    public void testNestedFunctionPointersAsParameters() {
        BValue[] returns = BTestUtils.invoke(fpProgram, "test6");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test6 test6 1.0");
    }

    @Test
    public void testGlobalFP() {
        BValue[] returns;
        // testing function pointer.
        returns = BTestUtils.invoke(globalProgram, "test1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test1");
    }

    @Test
    public void testGlobalFPAsLambda() {
        BValue[] returns;
        // lambda.
        returns = BTestUtils.invoke(globalProgram, "test2");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test2true");
    }

    @Test
    public void testGlobalFPAssignment() {
        BValue[] returns;
        // assign function pointer and invoke.
        returns = BTestUtils.invoke(globalProgram, "test3");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 3);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test3");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "test3");
        Assert.assertNotNull(returns[2]);
        Assert.assertEquals(returns[2].stringValue(), "3test");
    }

    @Test
    public void testGlobalFPNull() {
        // Invoking null function pointer.
        BLangRuntimeException exceptionThrown = null;
        try {
            BTestUtils.invoke(globalProgram, "test4");
        } catch (BLangRuntimeException e) {
            exceptionThrown = e;
        }
        if (exceptionThrown == null) {
            Assert.fail("Exception expected.");
        }
        Assert.assertTrue(exceptionThrown.getMessage().contains("NullReferenceException"));
    }

    @Test
    public void testGlobalFPWithLocalFP() {
        BValue[] returns;
        // Check global and local variable.
        returns = BTestUtils.invoke(globalProgram, "test5");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "falsetest5");
    }

    @Test
    public void testGlobalFPByAssigningLocalFP() {
        BValue[] returns;
        // assign local ref to global and invoke.
        returns = BTestUtils.invoke(globalProgram, "test6");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "truetest6");
    }

    @Test
    public void testStructFP() {
        BValue[] returns = BTestUtils.invoke(structProgram, "test1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 2);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "bob white");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "smith, tom");
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp = "" +
            ".*NullReferenceException.*")
    public void testStructFPNullReference() {
        BTestUtils.invoke(structProgram, "test2");
    }

    @Test
    public void testFPWithStruct() {
        BValue[] returns = BTestUtils.invoke(structProgram, "test3");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "white, bob");
    }

    @Test()
    public void testFunctionPointerNative() {
        CompileResult result = BTestUtils.compile("test-src/expressions/lambda/function-pointer-native.bal");
        BValue[] returns = BTestUtils.invoke(result, "test1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "1500526800000");
    }
}
