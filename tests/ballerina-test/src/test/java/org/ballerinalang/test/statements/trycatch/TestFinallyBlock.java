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
package org.ballerinalang.test.statements.trycatch;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for finally block.
 */
public class TestFinallyBlock {

    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/trycatch/finally-stmt.bal");
    }

    @Test
    public void testThrowErrorFromFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "test1", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "finally block error");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "assigned");
    }

    @Test
    public void testReturnFromFinallyBlock() {
        BValue[] args = {new BInteger(11)};
        BValue[] returns = BRunUtil.invoke(compileResult, "test2", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "finally block");
    }

    @Test(description = "check finally while returning.")
    public void testReturnWithFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "test3", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "try");
    }

    @Test(description = "check finally while returning for reference type.")
    public void testReturnReferenceTypeWithFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "test4", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "try innerFinally outerFinally");
    }

    @Test(description = "check while condition in a finally block")
    public void testWhileInaFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "test5", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "start123end");
    }

    @Test(description = "check finally while returning for reference type.")
    public void testReturnReferenceTypeInFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "test6", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "try innerFinally outerFinally");
    }

    @Test(description = "check try-finally block inside a finally block ")
    public void testTryFinallyInsideFinallyFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "test7", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "try innerFinally innerInnerTry " +
                "innerInnerFinally outerFinally");
    }

    @Test(description = "check multiple return inside finally block ")
    public void testMultipleReturnInsideFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "test8", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "OuterOk");
    }


    @Test(description = "check multiple return inside finally block ")
    public void testMultipleReturnInsideFinallyBlockWithRefType() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "test9", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "try innerFinally innerInnerTry " +
                "innerInnerFinally outerFinally");
    }


    @Test(description = "check break with Finally ")
    public void testBreakWithFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testBreak1", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "s t-f1 t-f2 tf3");
    }

    @Test(description = "check next with finally ")
    public void testNextWithFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testNext1", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "s t-f1 t-f2 tf3 t-f4 t-f5");
    }

    @Test(description = "check abort with finally ")
    public void testAbortWithFinallyBlock() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAbort1", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "s tf");
    }

    @Test(description = "check abort with finally ")
    public void testAbortWithFinallyBlock2() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAbort2", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "s t-f1 tf2");
    }
}
