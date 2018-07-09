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
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
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
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertTrue(returns[1] instanceof BMap);
        BMap<String, BValue> stackFrame1 = (BMap<String, BValue>) returns[0];
        Assert.assertEquals(stackFrame1.get("callableName").stringValue(), "testErrorCallStackFrame");
        Assert.assertEquals(stackFrame1.get("fileName").stringValue(), "try-catch-stmt.bal");
        Assert.assertEquals(((BInteger) stackFrame1.get("lineNumber")).intValue(), 95);
        BMap<String, BValue> stackFrame2 = (BMap<String, BValue>) returns[1];
        Assert.assertEquals(stackFrame2.get("callableName").stringValue(), "testUncaughtException");
        Assert.assertEquals(stackFrame2.get("fileName").stringValue(), "try-catch-stmt.bal");
        Assert.assertEquals(((BInteger) stackFrame2.get("lineNumber")).intValue(), 88);
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

    @Test(description = "Test function call in finally block when an error is thrown.")
    public void testMethodCallInFinally() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMethodCallInFinally", args);

        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "startdone");
    }

    @Test(description = "Test throwing an error in the finally block when there is a return in the try block",
            expectedExceptionsMessageRegExp = ".*error, message: number of finally executions: 1.*",
            expectedExceptions = BLangRuntimeException.class)
    public void testThrowInFinallyWithReturnInTry() {
        BRunUtil.invoke(compileResult, "testThrowInFinallyWithReturnInTry", new BValue[]{});
    }

    @Test(description = "Test throwing an error in the finally block when there is a return in the catch block",
            expectedExceptionsMessageRegExp = ".*error, message: number of finally executions: 1.*",
            expectedExceptions = BLangRuntimeException.class)
    public void testThrowInFinallyWithReturnInCatch() {
        BRunUtil.invoke(compileResult, "testThrowInFinallyWithReturnInCatch", new BValue[]{});
    }

    @Test(description = "Test throwing an error in the finally block when there are returns in branches in the try "
            + "block, and try returns",
            expectedExceptionsMessageRegExp = ".*error, message: number of catch and finally executions: 1.*",
            expectedExceptions = BLangRuntimeException.class)
    public void testThrowInFinallyWithReturnInTryBranch() {
        BRunUtil.invoke(compileResult, "testThrowInFinallyWithReturnInTryBranches",
                        new BValue[]{new BInteger(5)});
    }

    @Test(description = "Test throwing an error in the finally block when there are returns in branches in the try "
            + "block, and try throws an error",
            expectedExceptionsMessageRegExp = ".*error, message: number of catch and finally executions: 2.*",
            expectedExceptions = BLangRuntimeException.class)
    public void testThrowInFinallyWithThrowInTryBranchWithReturns() {
        BRunUtil.invoke(compileResult, "testThrowInFinallyWithReturnInTryBranches",
                        new BValue[]{new BInteger(0)});
    }

    @Test(description = "Test throwing an error in the try block and the finally block when there is a return in "
            + "the finally block",
            expectedExceptionsMessageRegExp = ".*error, message: number of catch and finally executions: 2.*",
            expectedExceptions = BLangRuntimeException.class)
    public void testReturnInFinallyWithThrowInTryAndFinally() {
        BRunUtil.invoke(compileResult, "testReturnInFinallyWithThrowInTryAndFinally", new BValue[]{});
    }

    @Test(description = "Test nested try/catch with inner finally throwing an error where try/catch/finally return",
            expectedExceptionsMessageRegExp = ".*error, message: outer Catch Block Error 1180.*",
            expectedExceptions = BLangRuntimeException.class)
    public void nestedTryCatchFinallyWithReturnsOne() {
        BRunUtil.invoke(compileResult, "nestedTryCatchFinallyWithReturns", new BValue[]{new BInteger(1)});
    }

    @Test(description = "Test nested try/catch with inner finally throwing an error where try/catch/finally return",
            expectedExceptionsMessageRegExp = ".*error, message: outer Catch Block Error 930.*",
            expectedExceptions = BLangRuntimeException.class)
    public void nestedTryCatchFinallyWithReturnsTwo() {
        BRunUtil.invoke(compileResult, "nestedTryCatchFinallyWithReturns", new BValue[]{new BInteger(8)});
    }

    @Test(description = "Test nested try/catch with inner finally throwing an error where try/catch/finally return",
            expectedExceptionsMessageRegExp = ".*error, message: outer Catch Block Error 795.*",
            expectedExceptions = BLangRuntimeException.class)
    public void nestedTryCatchFinallyWithReturnsThree() {
        BRunUtil.invoke(compileResult, "nestedTryCatchFinallyWithReturns", new BValue[]{new BInteger(15)});
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

    @Test(description = "test not all catch blocks including a return statement when the finally block does not "
            + "contain a return statement")
    public void testIncorrectReturnsInCatchBlocks() {
        BAssertUtil.validateError(compileResultNegative, 4, "this function must return a result", 44, 1);
    }

    @Test()
    public void testUnreachableReturnWithTryCatch() {
        BAssertUtil.validateError(compileResultNegative, 5, "unreachable code", 64, 5);
    }

    @Test()
    public void testUnreachableReturnWithTryCatchFinally() {
        BAssertUtil.validateError(compileResultNegative, 6, "unreachable code", 77, 5);
    }
}
