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
package org.ballerinalang.core.lang.error;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for finally block.
 */
public class TestFinallyBlock {

    ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/errors/test_finally.bal");

    }

    @Test
    public void testThrowErrorFromFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test1", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "finally block error");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "assigned");
    }

    @Test
    public void testReturnFromFinallyBlock() {
        BValue[] args = {new BInteger(11)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test2", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "finally block");
    }

    @Test(description = "check finally while returning.")
    public void testReturnWithFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test3", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "try");
    }

    @Test(description = "check finally while returning for reference type.")
    public void testReturnReferenceTypeWithFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test4", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "try innerFinally outerFinally");
    }

    @Test(description = "check while condition in a finally block")
    public void testWhileInaFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test5", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "start123end");
    }

    @Test(description = "check finally while returning for reference type.")
    public void testReturnReferenceTypeInFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test6", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "try innerFinally outerFinally");
    }

    @Test(description = "check try-finally block inside a finally block ")
    public void testTryFinallyInsideFinallyFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test7", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "try innerFinally innerInnerTry " +
                "innerInnerFinally outerFinally");
    }

    @Test(description = "check multiple return inside finally block ")
    public void testMultipleReturnInsideFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test8", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "OuterOk");
    }


    @Test(description = "check multiple return inside finally block ")
    public void testMultipleReturnInsideFinallyBlockWithRefType() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test9", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "try innerFinally innerInnerTry " +
                "innerInnerFinally outerFinally");
    }


    @Test(description = "check break with Finally ")
    public void testBreakWithFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testBreak1", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "s t-f1 t-f2 tf3");
    }

    @Test(description = "check continue with finally ")
    public void testContinueWithFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testContinue1", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "s t-f1 t-f2 tf3 t-f4 t-f5");
    }

    @Test(description = "check abort with finally ")
    public void testAbortWithFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testAbort1", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "s tf");
    }

    @Test(description = "check abort with finally ")
    public void testAbortWithFinallyBlock2() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testAbort2", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "s t-f1 tf2");
    }
}
