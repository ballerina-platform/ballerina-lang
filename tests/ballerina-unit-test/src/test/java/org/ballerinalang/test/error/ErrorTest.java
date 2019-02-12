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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.error;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for Error.
 *
 * @since 0.985.0
 */
public class ErrorTest {

    private CompileResult basicErrorTest;

    private static final String ERROR1 = "error1";
    private static final String ERROR2 = "error2";
    private static final String ERROR3 = "error3";
    private static final String EMPTY_CURLY_BRACE = "{}";

    @BeforeClass
    public void setup() {
        basicErrorTest = BCompileUtil.compile("test-src/error/basicErrorTest.bal");
    }

    @Test
    public void errorConstructReasonTest() {
        BValue[] returns = BRunUtil.invoke(basicErrorTest, "errorConstructReasonTest");

        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).reason, ERROR1);
        Assert.assertEquals(((BError) returns[0]).details.stringValue(), EMPTY_CURLY_BRACE);
        Assert.assertTrue(returns[1] instanceof BError);
        Assert.assertEquals(((BError) returns[1]).reason, ERROR2);
        Assert.assertEquals(((BError) returns[1]).details.stringValue(), EMPTY_CURLY_BRACE);
        Assert.assertTrue(returns[2] instanceof BError);
        Assert.assertEquals(((BError) returns[2]).reason, ERROR3);
        Assert.assertEquals(((BError) returns[2]).details.stringValue(), EMPTY_CURLY_BRACE);
        Assert.assertEquals(returns[3].stringValue(), ERROR1);
        Assert.assertEquals(returns[4].stringValue(), ERROR2);
        Assert.assertEquals(returns[5].stringValue(), ERROR3);
    }

    @Test
    public void errorConstructDetailTest() {
        BValue[] returns = BRunUtil.invoke(basicErrorTest, "errorConstructDetailTest");
        String detail1 = "{\"message\":\"msg1\"}";
        String detail2 = "{\"message\":\"msg2\"}";
        String detail3 = "{\"message\":\"msg3\"}";
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).reason, ERROR1);
        Assert.assertEquals(((BError) returns[0]).details.stringValue().trim(), detail1);
        Assert.assertTrue(returns[1] instanceof BError);
        Assert.assertEquals(((BError) returns[1]).reason, ERROR2);
        Assert.assertEquals(((BError) returns[1]).details.stringValue().trim(), detail2);
        Assert.assertTrue(returns[2] instanceof BError);
        Assert.assertEquals(((BError) returns[2]).reason, ERROR3);
        Assert.assertEquals(((BError) returns[2]).details.stringValue().trim(), detail3);
        Assert.assertEquals(returns[3].stringValue().trim(), detail1);
        Assert.assertEquals(returns[4].stringValue().trim(), detail2);
        Assert.assertEquals(returns[5].stringValue().trim(), detail3);
    }

    @Test
    public void errorPanicTest() {
        // Case without panic
        BValue[] args = new BValue[] { new BInteger(10) };
        BValue[] returns = BRunUtil.invoke(basicErrorTest, "errorPanicTest", args);
        Assert.assertEquals(returns[0].stringValue(), "done");

        // Now panic
        args = new BValue[] { new BInteger(15) };
        Exception expectedException = null;
        try {
            BRunUtil.invoke(basicErrorTest, "errorPanicTest", args);
        } catch (Exception e) {
            expectedException = e;
        }
        Assert.assertNotNull(expectedException);
        String result = "error: largeNumber {\"message\":\"large number\"}\n" +
                "\tat errorPanicCallee(basicErrorTest.bal:36)\n" +
                "\t   errorPanicTest(basicErrorTest.bal:30)";
        Assert.assertEquals(expectedException.getMessage().trim(), result.trim());
    }

    @Test
    public void errorTrapTest() {
        // Case without panic
        BValue[] args = new BValue[] { new BInteger(10) };
        BValue[] returns = BRunUtil.invoke(basicErrorTest, "errorTrapTest", args);
        Assert.assertEquals(returns[0].stringValue(), "done");

        // Now panic
        args = new BValue[] { new BInteger(15) };
        returns = BRunUtil.invoke(basicErrorTest, "errorTrapTest", args);
        String result = "largeNumber {\"message\":\"large number\"}";
        Assert.assertEquals(returns[0].stringValue(), result.trim());
    }

    @Test
    public void customErrorDetailsTest() {
        BValue[] returns = BRunUtil.invoke(basicErrorTest, "testCustomErrorDetails");
        Assert.assertEquals(returns[0].stringValue(), "trxErr {message:\"\", cause:(), data:\"test\"}");
        Assert.assertEquals(((BError) returns[0]).details.getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        Assert.assertEquals(((BError) returns[0]).details.getType().getName(), "TrxErrorData");
    }

    @Test
    public void testCustomErrorDetails2() {
        BValue[] returns = BRunUtil.invoke(basicErrorTest, "testCustomErrorDetails2");
        Assert.assertEquals(returns[0].stringValue(), "test");
    }

    @Test
    public void testErrorWithErrorConstructor() {
        BValue[] returns = BRunUtil.invoke(basicErrorTest, "testErrorWithErrorConstructor");
        Assert.assertEquals(returns[0].stringValue(), "test");
    }

    @Test
    public void testGetCallStack() {
        BValue[] returns = BRunUtil.invoke(basicErrorTest, "getCallStackTest");
        Assert.assertEquals(returns[0].stringValue(), "{callableName:\"getCallStack\", " +
                "moduleName:\"ballerina/runtime\", fileName:\"<native>\", lineNumber:0}");
    }

    @Test
    public void testConsecutiveTraps() {
        BValue[] returns = BRunUtil.invoke(basicErrorTest, "testConsecutiveTraps");
        Assert.assertEquals(returns[0].stringValue(), "Error");
        Assert.assertEquals(returns[1].stringValue(), "Error");
    }

    @Test
    public void testOneLinePanic() {
        BValue[] returns = BRunUtil.invoke(basicErrorTest, "testOneLinePanic");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray array = (BValueArray) returns[0];
        Assert.assertEquals(array.getString(0), "Error1");
        Assert.assertEquals(array.getString(1), "Error2");
        Assert.assertEquals(array.getString(2), "Something Went Wrong");
        Assert.assertEquals(array.getString(3), "Error3");
        Assert.assertEquals(array.getString(4), "Something Went Wrong");
        Assert.assertEquals(array.getString(5), "1");
    }
}
