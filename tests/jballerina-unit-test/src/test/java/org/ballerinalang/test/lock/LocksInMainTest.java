/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.ballerinalang.test.lock;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.utils.ByteArrayUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Tests for Ballerina locks.
 *
 * @since 0.961.0
 */
public class LocksInMainTest {

    private CompileResult parallelCompileResult;

    @BeforeClass
    public void setup() {
        parallelCompileResult = BCompileUtil.compile("test-src/lock/parallel-run-lock.bal");
        Assert.assertEquals(parallelCompileResult.getErrorCount(), 0);
    }

    @Test(description = "Tests lock within a lock")
    public void testLockWithinLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "lockWithinLock");
        assertEquals(returns.length, 2);
        assertSame(returns[0].getClass(), BInteger.class);
        assertSame(returns[1].getClass(), BString.class);

        assertEquals(((BInteger) returns[0]).intValue(), 77);
        assertEquals(returns[1].stringValue(), "second sample value");

    }

    @Test(description = "Tests lock within in workers", enabled = false)
    public void simpleLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/simple-lock.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "simpleLock");
        assertEquals(returns.length, 1);
        assertSame(returns[0].getClass(), BString.class);
        assertEquals(returns[0].stringValue(), "main in critical after w1 is out");
    }

    @Test(description = "Tests lock within lock in workers")
    public void testLockWithinLockInWorkers() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "lockWithinLockInWorkers");
        assertEquals(returns.length, 2);
        assertSame(returns[0].getClass(), BInteger.class);
        assertSame(returns[1].getClass(), BString.class);

        assertEquals(((BInteger) returns[0]).intValue(), 66);
        assertEquals(returns[1].stringValue(), "sample output");

    }

    @Test(description = "Tests lock inside while loop")
    public void testLockInsideWhileLoop() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "lockInsideWhileLoop");
        assertEquals(returns.length, 1);
        assertSame(returns[0].getClass(), BInteger.class);

        assertEquals(((BInteger) returns[0]).intValue(), 56);

    }

    //    TODO:https://github.com/ballerina-platform/ballerina-lang/issues/11305
    @Test(description = "Tests throwing and error inside lock", enabled = false)
    public void testThrowErrorInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "throwErrorInsideLock");
        assertEquals(returns.length, 2);
        assertSame(returns[0].getClass(), BInteger.class);
        assertSame(returns[1].getClass(), BString.class);

        assertEquals(((BInteger) returns[0]).intValue(), 51);
        assertEquals(returns[1].stringValue(), "second worker string");
    }

    @Test(description = "Tests throwing an error inside a lock inside try catch block")
    public void testThrowErrorInsideLockInsideTrap() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "throwErrorInsideLockInsideTryFinally");
        assertEquals(returns.length, 2);
        assertSame(returns[0].getClass(), BInteger.class);
        assertSame(returns[1].getClass(), BString.class);

        assertEquals(((BInteger) returns[0]).intValue(), 53);
        assertEquals(returns[1].stringValue(), "worker 2 sets the string value after try catch finally");
    }

    @Test(description = "Tests throwing an error inside try catch finally block inside a lock")
    public void testThrowErrorInsideTryCatchFinallyInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "throwErrorInsideTryCatchFinallyInsideLock");
        assertEquals(returns.length, 2);
        assertSame(returns[0].getClass(), BInteger.class);
        assertSame(returns[1].getClass(), BString.class);

        assertEquals(((BInteger) returns[0]).intValue(), 53);
        assertEquals(returns[1].stringValue(), "worker 2 sets the string after try catch finally inside lock");
    }

    @Test(description = "Tests throwing an error inside try finally block inside a lock", enabled = false)
    public void testThrowErrorInsideTryFinallyInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "throwErrorInsideTryFinallyInsideLock");
        assertEquals(returns.length, 2);
        assertSame(returns[0].getClass(), BInteger.class);
        assertSame(returns[1].getClass(), BString.class);

        assertEquals(((BInteger) returns[0]).intValue(), 52);
        assertEquals(returns[1].stringValue(), "worker 2 sets the string after try finally");
    }

    @Test(description = "Tests throwing an error inside a lock inside try catch block")
    public void testThrowErrorInsideLockInsideTryCatch() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "throwErrorInsideLockInsideTryCatch");
        assertEquals(returns.length, 2);
        assertSame(returns[0].getClass(), BInteger.class);
        assertSame(returns[1].getClass(), BString.class);

        assertEquals(((BInteger) returns[0]).intValue(), 52);
        assertEquals(returns[1].stringValue(), "worker 2 sets the string value after try catch");
    }

    @Test(description = "Tests throwing an error inside try catch block inside a lock")
    public void testThrowErrorInsideTryCatchInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "throwErrorInsideTryCatchInsideLock");
        assertEquals(returns.length, 2);
        assertSame(returns[0].getClass(), BInteger.class);
        assertSame(returns[1].getClass(), BString.class);

        assertEquals(((BInteger) returns[0]).intValue(), 52);
        assertEquals(returns[1].stringValue(), "worker 2 sets the string after try catch inside lock");
    }

    @Test(description = "Tests lock within lock in workers for boolean and blob")
    public void testLockWithinLockInWorkersForBlobAndBoolean() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "lockWithinLockInWorkersForBlobAndBoolean");
        assertEquals(returns.length, 2);
        assertSame(returns[0].getClass(), BBoolean.class);
        assertSame(returns[1].getClass(), BValueArray.class);

        assertTrue(((BBoolean) returns[0]).booleanValue());
        ByteArrayUtils.assertJBytesWithBBytes(((BValueArray) returns[1]).getBytes(), "sample blob output".getBytes());

    }

    @Test(description = "Tests lock within functions down the function chain")
    public void testLockWithinFunctionsDownTheLine() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks_in_functions_down_the_chain.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "lockWithinLock");
        assertEquals(returns.length, 1);
        assertSame(returns[0].getClass(), BString.class);
        String result = returns[0].stringValue();
        assertTrue("w1w1w1vw2w2w2v".equals(result) || "w2w2w2v".equals(result));
    }

    @Test(description = "Tests returning inside lock statement", enabled = false)
    public void testReturnInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "returnInsideLock");
        assertEquals(returns.length, 2);
        assertSame(returns[0].getClass(), BInteger.class);
        assertSame(returns[1].getClass(), BString.class);

        assertEquals(((BInteger) returns[0]).intValue(), 88);
        assertEquals(returns[1].stringValue(), "changed value11");

    }

    @Test(description = "Tests break inside lock statement")
    public void testBreakInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "breakInsideLock");
        assertEquals(returns.length, 2);
        assertSame(returns[0].getClass(), BInteger.class);
        assertSame(returns[1].getClass(), BString.class);

        assertEquals(((BInteger) returns[0]).intValue(), 657);
        assertEquals(returns[1].stringValue(), "lock value inside second worker after while");

    }

    @Test(description = "Tests next inside lock statement")
    public void testNextInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        BValue[] returns = BRunUtil.invoke(compileResult, "nextInsideLock");
        assertEquals(returns.length, 2);
        assertSame(returns[0].getClass(), BInteger.class);
        assertSame(returns[1].getClass(), BString.class);

        assertEquals(((BInteger) returns[0]).intValue(), 657);
        assertEquals(returns[1].stringValue(), "lock value inside second worker after while");

    }

    @Test(description = "Test lock negative cases")
    public void testLockNegativeCases() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 2);
        BAssertUtil.validateError(compileResult, 0, "undefined symbol 'val'",
                                  8, 5);
        BAssertUtil.validateError(compileResult, 1, "undefined symbol 'val1'",
                                  18, 9);
    }

    @Test(description = "Test for parallel run using locks", enabled = false)
    public void testParallelRunUsingLocks() {
        BValue[] returns = BRunUtil.invoke(parallelCompileResult, "runParallelUsingLocks");
    }

    @Test(description = "Test for parallel run when invocations have global variable dependencies")
    public void testParallelRunWithInvocationDependencies() {
        BValue[] returns = BRunUtil.invoke(parallelCompileResult, "testLockWithInvokableAccessingGlobal");
    }

    @Test(description = "Test for parallel run when invocations have loops and global var dependencies")
    public void testParallelRunWithChainedInvocationDependencies() {
        BValue[] returns = BRunUtil.invoke(parallelCompileResult, "testLockWIthInvokableChainsAccessingGlobal");
    }

    @Test(description = "Test for parallel run when invocation are recursive and have global var dependencies")
    public void testParallelRunWithRecursiveInvocationDependencies() {
        BValue[] returns = BRunUtil.invoke(parallelCompileResult, "testLockWIthInvokableRecursiveAccessGlobal");
    }

    @Test(description = "Test for parallel run when invocations are imported and contains global var dependencies")
    public void testParallelRunWithImportInvocationDependencies() {
        CompileResult importInvocationDependencies = BCompileUtil.compile("test-src/lock/locks-in-imports-test",
                                                                          "mod1", true);

        BRunUtil.invoke(importInvocationDependencies, "testLockWIthInvokableChainsAccessingGlobal");
    }

    @Test(description = "Test for locks on global references")
    public void testLocksWhenGlobalVariablesReferToSameValue() {
        BRunUtil.invoke(parallelCompileResult, "testLocksWhenGlobalVariablesReferToSameValue");
    }

    @Test(description = "Test for global reference update inside a worker")
    public void testForGlobalRefUpdateInsideWorker() {
        BRunUtil.invoke(parallelCompileResult, "testForGlobalRefUpdateInsideWorker");
    }

    @Test(description = "Test for global reference updated inside conditional statment")
    public void testForGlobalRefUpdateInsideConditional() {
        BRunUtil.invoke(parallelCompileResult, "testForGlobalRefUpdateInsideConditional");
    }
}
