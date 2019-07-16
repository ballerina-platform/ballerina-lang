/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.test.jvm;

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to cover error related tests on JBallerina.
 *
 * @since 0.995.0
 */
public class ErrorTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/jvm/errors.bal");
    }

    @Test(description = "Test panic an error", expectedExceptions = RuntimeException.class, 
          expectedExceptionsMessageRegExp = "error: reason foo 2 message=int value\n\tat foo\\(errors"
                  + ".bal:47\\)\n\t   testPanic\\(errors.bal:19\\)")
    public void testPanic() {
            BRunUtil.invoke(compileResult, "testPanic", new BValue[] { new BInteger(0) });
    }

    @Test(description = "Test trap an error")
    public void testTrap() {
        BValue[] result = BRunUtil.invoke(compileResult, "testTrap", new BValue[] { new BInteger(0) });
        Assert.assertTrue(result[0] instanceof BError);
        BError bError = (BError) result[0];
        Assert.assertEquals(bError.getReason(), "reason foo 2");
        Assert.assertEquals(((BMap) bError.getDetails()).get("message").stringValue(), "int value");
    }

    @Test(description = "Test handle errors of nested function calls with single trap")
    public void testNestedCallsWithSingleTrap() {
        // Run with zero integer input
        BValue[] result = BRunUtil.invoke(compileResult, "testNestedCallsWithSingleTrap",
                                          new BValue[] { new BInteger(0) });
        BError bError = (BError) result[0];
        Assert.assertEquals(bError.getReason(), "reason bar 1");
        Assert.assertEquals(((BMap) bError.getDetails()).get("message").stringValue(), "bar");

        // Run with non zero integer input
        result = BRunUtil.invoke(compileResult, "testNestedCallsWithSingleTrap",
                                          new BValue[] { new BInteger(1) });
        bError = (BError) result[0];
        Assert.assertEquals(bError.getReason(), "reason foo 2");
        Assert.assertEquals(((BMap) bError.getDetails()).get("message").stringValue(), "int value");
    }

    @Test(description = "Test handle errors of nested function calls with trap expression for each function call")
    public void testNestedCallsWithAllTraps() {
        // Run with zero integer input
        BValue[] result = BRunUtil.invoke(compileResult, "testNestedCallsWithAllTraps",
                                          new BValue[] { new BInteger(0) });
        BError bError = (BError) result[0];
        Assert.assertEquals(bError.getReason(), "reason foo 1");
        Assert.assertEquals(((BMap) bError.getDetails()).get("message").stringValue(), "foo");

        // Run with non zero integer input
        result = BRunUtil.invoke(compileResult, "testNestedCallsWithSingleTrap",
                                 new BValue[] { new BInteger(1) });
        bError = (BError) result[0];
        Assert.assertEquals(bError.getReason(), "reason foo 2");
        Assert.assertEquals(((BMap) bError.getDetails()).get("message").stringValue(), "int value");
    }

    @Test(description = "Test handle errors of nested function calls with trap expression for some function calls")
    public void testNestedCallsWithSomeTraps() {
        // Run with zero integer input
        BValue[] result = BRunUtil.invoke(compileResult, "testNestedCallsWithSingleTrap",
                                          new BValue[] { new BInteger(0) });
        BError bError = (BError) result[0];
        Assert.assertEquals(bError.getReason(), "reason bar 1");
        Assert.assertEquals(((BMap) bError.getDetails()).get("message").stringValue(), "bar");

        // Run with non zero integer input
        result = BRunUtil.invoke(compileResult, "testNestedCallsWithSingleTrap",
                                 new BValue[] { new BInteger(1) });
        bError = (BError) result[0];
        Assert.assertEquals(bError.getReason(), "reason foo 2");
        Assert.assertEquals(((BMap) bError.getDetails()).get("message").stringValue(), "int value");
    }

    @Test
    public void testSelfReferencingObject() {
        BRunUtil.invoke(compileResult, "testSelfReferencingError");
    }
}
