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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for testing TryCatchStmt and Throw Stmt.
 */
public class TryCatchThrowStmtTest {

    private CompileResult compileResult;
    private CompileResult compileResultNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/trycatch/try-catch-stmt.bal");
        compileResultNegative = BCompileUtil.compile("test-src/statements/trycatch/try-catch-finally-negative.bal");
    }

    @Test(description = "Test try block execution.")
    public void testTryCatchStmt() {
        BValue[] args = {new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testTryCatch", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(),
                "start insideTry insideInnerTry endInsideInnerTry innerFinally endInsideTry Finally End",
                "Try block didn't execute fully.");
    }

    @Test(description = "Test catch block execution.")
    public void testTryCatchWithThrow() {
        BValue[] args = {new BInteger(15)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testTryCatch", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(),
                "start insideTry insideInnerTry onError innerTestErrorCatch:test innerFinally TestErrorCatch" +
                        " Finally End", "Try block didn't execute fully.");
    }

    @Test(description = "Test catch block execution, where thrown exception is caught using equivalent catch block ")
    public void testTryCatchEquivalentCatch() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testTryCatch", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(),
                "start insideTry insideInnerTry onInputError innerFinally ErrorCatch Finally End",
                "Try block didn't execute fully.");
    }

    @Test(description = "Test throw statement in a function.")
    public void testTryCatch() {
        BValue[] args = {new BInteger(15)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testFunctionThrow", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true, "Catch block didn't execute.");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "013", "Unexpected execution order.");
    }

    @Test(description = "Test uncaught error in a function.", expectedExceptionsMessageRegExp = ".*error, " +
            "message: test message.*", expectedExceptions = BLangRuntimeException.class)
    public void testUncaughtException() {
        BValue[] args = {};
        BRunUtil.invoke(compileResult, "testUncaughtException", args);
    }

    @Test(description = "Test call stack frame of an error in a function.")
    public void testGetErrorCallStackFrame() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testErrorCallStackFrame", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct stackFrame1 = (BStruct) returns[0];
        Assert.assertEquals(stackFrame1.getStringField(0), "testErrorCallStackFrame");
        Assert.assertEquals(stackFrame1.getStringField(2), "try-catch-stmt.bal");
        Assert.assertEquals(stackFrame1.getIntField(0), 95);
        BStruct stackFrame2 = (BStruct) returns[1];
        Assert.assertEquals(stackFrame2.getStringField(0), "testUncaughtException");
        Assert.assertEquals(stackFrame2.getStringField(2), "try-catch-stmt.bal");
        Assert.assertEquals(stackFrame2.getIntField(0), 88);
    }

    @Test(description = "Test scope issue when using try catch inside while loop")
    public void testScopeIssueInTryCatch() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "scopeIssueTest", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        long value = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(value, 25);
    }

    @Test(description = "Test try statement within while block")
    public void testIssueWhenTryWithinWhile() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testTryWithinWhile", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        long value = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(value, 3);
    }

    @Test(description = "Test function call in finally block when error there is a error thrown.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: error, message: test.*")
    public void testMethodCallInFinally() {
        BValue[] args = {};
        BRunUtil.invoke(compileResult, "testMethodCallInFinally", args);
    }

    @Test()
    public void testDuplicateExceptionVariable() {
        BAssertUtil.validateError(compileResultNegative, 0, "redeclared symbol 'e'", 5, 9);
    }

    @Test()
    public void testInvalidThrow() {
        BAssertUtil.validateError(compileResultNegative, 1, "incompatible types: expected 'error', found 'int'",
                12, 11);
    }

    @Test()
    public void testInvalidFunctionThrow() {
        BAssertUtil.validateError(compileResultNegative, 2, "incompatible types: expected 'error', found 'int'",
                16, 11);
    }

    @Test()
    public void testDuplicateCatchBlock() {
        BAssertUtil.validateError(compileResultNegative, 3, "error 'TestError' already caught in catch block", 38, 14);
    }

}
