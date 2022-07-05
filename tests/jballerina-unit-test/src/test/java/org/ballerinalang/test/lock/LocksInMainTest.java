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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.utils.ByteArrayUtils;
import org.testng.annotations.AfterClass;
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
        assertEquals(parallelCompileResult.getErrorCount(), 0);
    }

    @Test(description = "Tests lock within a lock")
    public void testLockWithinLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        Object val = BRunUtil.invoke(compileResult, "lockWithinLock");
        BArray returns = (BArray) val;
        assertEquals(returns.size(), 2);
        assertSame(returns.get(0).getClass(), Long.class);
        assertTrue(returns.get(1) instanceof BString);

        assertEquals(returns.get(0), 77L);
        assertEquals(returns.get(1).toString(), "second sample value");

    }

    // https://github.com/ballerina-platform/ballerina-lang/issues/22987
    @Test(description = "Tests lock within in workers", enabled = false)
    public void simpleLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/simple-lock.bal");
        Object returns = BRunUtil.invoke(compileResult, "simpleLock");
        assertTrue(returns instanceof BString);
        assertEquals(returns.toString(), "main in critical after w1 is out");
    }

    @Test(description = "Tests lock within lock in workers")
    public void testLockWithinLockInWorkers() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        Object val = BRunUtil.invoke(compileResult, "lockWithinLockInWorkers");
        BArray returns = (BArray) val;
        assertEquals(returns.size(), 2);
        assertSame(returns.get(0).getClass(), Long.class);
        assertTrue(returns.get(1) instanceof BString);

        assertEquals(returns.get(0), 66L);
        assertEquals(returns.get(1).toString(), "sample output");

    }

    @Test(description = "Tests lock inside while loop")
    public void testLockInsideWhileLoop() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        Object returns = BRunUtil.invoke(compileResult, "lockInsideWhileLoop");
        assertSame(returns.getClass(), Long.class);

        assertEquals(returns, 56L);

    }

    @Test(description = "Tests throwing and error inside lock")
    public void testThrowErrorInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        Object val = BRunUtil.invoke(compileResult, "throwErrorInsideLock");
        BArray returns = (BArray) val;
        assertEquals(returns.size(), 2);
        assertSame(returns.get(0).getClass(), Long.class);
        assertTrue(returns.get(1) instanceof BString);

        assertEquals(returns.get(0), 51L);
        assertEquals(returns.get(1).toString(), "second worker string");
    }

    @Test(description = "Tests throwing an error inside a lock inside try catch block")
    public void testThrowErrorInsideLockInsideTrap() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        Object val = BRunUtil.invoke(compileResult, "throwErrorInsideLockInsideTryFinally");
        BArray returns = (BArray) val;
        assertEquals(returns.size(), 2);
        assertSame(returns.get(0).getClass(), Long.class);
        assertTrue(returns.get(1) instanceof BString);

        assertEquals(returns.get(0), 53L);
        assertEquals(returns.get(1).toString(), "worker 2 sets the string value after try catch finally");
    }

    @Test(description = "Tests throwing an error inside try catch finally block inside a lock")
    public void testThrowErrorInsideTryCatchFinallyInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        Object val = BRunUtil.invoke(compileResult, "throwErrorInsideTryCatchFinallyInsideLock");
        BArray returns = (BArray) val;
        assertEquals(returns.size(), 2);
        assertSame(returns.get(0).getClass(), Long.class);
        assertTrue(returns.get(1) instanceof BString);

        assertEquals(returns.get(0), 53L);
        assertEquals(returns.get(1).toString(), "worker 2 sets the string after try catch finally inside lock");
    }

    // https://github.com/ballerina-platform/ballerina-lang/issues/22987
    @Test(description = "Tests throwing an error inside try finally block inside a lock", enabled = false)
    public void testThrowErrorInsideTryFinallyInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        Object val = BRunUtil.invoke(compileResult, "throwErrorInsideTryFinallyInsideLock");
        BArray returns = (BArray) val;
        assertEquals(returns.size(), 2);
        assertSame(returns.get(0).getClass(), Long.class);
        assertTrue(returns.get(1) instanceof BString);

        assertEquals(returns.get(0), 52L);
        assertEquals(returns.get(1).toString(), "worker 2 sets the string after try finally");
    }

    @Test(description = "Tests throwing an error inside a lock inside try catch block")
    public void testThrowErrorInsideLockInsideTryCatch() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        Object val = BRunUtil.invoke(compileResult, "throwErrorInsideLockInsideTryCatch");
        BArray returns = (BArray) val;
        assertEquals(returns.size(), 2);
        assertSame(returns.get(0).getClass(), Long.class);
        assertTrue(returns.get(1) instanceof BString);

        assertEquals(returns.get(0), 52L);
        assertEquals(returns.get(1).toString(), "worker 2 sets the string value after try catch");
    }

    @Test(description = "Tests throwing an error inside try catch block inside a lock")
    public void testThrowErrorInsideTryCatchInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        Object val = BRunUtil.invoke(compileResult, "throwErrorInsideTryCatchInsideLock");
        BArray returns = (BArray) val;
        assertEquals(returns.size(), 2);
        assertSame(returns.get(0).getClass(), Long.class);
        assertTrue(returns.get(1) instanceof BString);

        assertEquals(returns.get(0), 52L);
        assertEquals(returns.get(1).toString(), "worker 2 sets the string after try catch inside lock");
    }

    @Test(description = "Tests lock within lock in workers for boolean and blob")
    public void testLockWithinLockInWorkersForBlobAndBoolean() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        Object val = BRunUtil.invoke(compileResult, "lockWithinLockInWorkersForBlobAndBoolean");
        BArray returns = (BArray) val;
        assertEquals(returns.size(), 2);
        assertSame(returns.get(0).getClass(), Boolean.class);
        assertTrue(returns.get(1) instanceof BArray);

        assertTrue((Boolean) returns.get(0));
        ByteArrayUtils.assertJBytesWithBBytes(((BArray) returns.get(1)).getBytes(), "sample blob output".getBytes());

    }

    @Test(description = "Tests lock within functions down the function chain")
    public void testLockWithinFunctionsDownTheLine() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks_in_functions_down_the_chain.bal");

        Object returns = BRunUtil.invoke(compileResult, "lockWithinLock");
        assertTrue(returns instanceof BString);
        String result = returns.toString();
        assertTrue("w1w1w1vw2w2w2v".equals(result) || "w2w2w2v".equals(result));
    }

    // https://github.com/ballerina-platform/ballerina-lang/issues/22987
    @Test(description = "Tests returning inside lock statement", enabled = false)
    public void testReturnInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        Object val = BRunUtil.invoke(compileResult, "returnInsideLock");
        BArray returns = (BArray) val;
        assertEquals(returns.size(), 2);
        assertSame(returns.get(0).getClass(), Long.class);
        assertTrue(returns.get(1) instanceof BString);

        assertEquals(returns.get(0), 88L);
        assertEquals(returns.get(1).toString(), "changed value11");

    }

    @Test(description = "Tests break inside lock statement")
    public void testBreakInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        Object val = BRunUtil.invoke(compileResult, "breakInsideLock");
        BArray returns = (BArray) val;
        assertEquals(returns.size(), 2);
        assertSame(returns.get(0).getClass(), Long.class);
        assertTrue(returns.get(1) instanceof BString);

        assertEquals(returns.get(0), 657L);
        assertEquals(returns.get(1).toString(), "lock value inside second worker after while");

    }

    @Test(description = "Tests next inside lock statement")
    public void testNextInsideLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");

        Object val = BRunUtil.invoke(compileResult, "nextInsideLock");
        BArray returns = (BArray) val;
        assertEquals(returns.size(), 2);
        assertSame(returns.get(0).getClass(), Long.class);
        assertTrue(returns.get(1) instanceof BString);

        assertEquals(returns.get(0), 657L);
        assertEquals(returns.get(1).toString(), "lock value inside second worker after while");

    }

    @Test(description = "Test lock negative cases")
    public void testLockNegativeCases() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks-in-functions-negative.bal");
        assertEquals(compileResult.getErrorCount(), 2);
        BAssertUtil.validateError(compileResult, 0, "undefined symbol 'val'",
                                  8, 5);
        BAssertUtil.validateError(compileResult, 1, "undefined symbol 'val1'",
                                  18, 9);
    }

    // https://github.com/ballerina-platform/ballerina-lang/issues/22987
    @Test(description = "Test for parallel run using locks", enabled = false)
    public void testParallelRunUsingLocks() {
        Object returns = BRunUtil.invoke(parallelCompileResult, "runParallelUsingLocks");
    }

    @Test(description = "Test for parallel run when invocations have global variable dependencies")
    public void testParallelRunWithInvocationDependencies() {
        Object returns = BRunUtil.invoke(parallelCompileResult, "testLockWithInvokableAccessingGlobal");
    }

    @Test(description = "Test for parallel run when invocations have loops and global var dependencies")
    public void testParallelRunWithChainedInvocationDependencies() {
        Object returns = BRunUtil.invoke(parallelCompileResult, "testLockWIthInvokableChainsAccessingGlobal");
    }

    @Test(description = "Test for parallel run when invocation are recursive and have global var dependencies")
    public void testParallelRunWithRecursiveInvocationDependencies() {
        Object returns = BRunUtil.invoke(parallelCompileResult, "testLockWIthInvokableRecursiveAccessGlobal");
    }

    @Test(description = "Test for parallel run when invocations are imported and contains global var dependencies")
    public void testParallelRunWithImportInvocationDependencies() {
        CompileResult importInvocationDependencies = BCompileUtil.
                compile("test-src/lock/locks_in_imports_test_project");
        BRunUtil.invoke(importInvocationDependencies, "testLockWithInvokableChainsAccessingGlobal");
    }

    //TODO testcase disabled with https://github.com/ballerina-platform/ballerina-lang/issues/28930
    @Test(description = "Test for locks on global references", enabled = false)
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

    @AfterClass
    public void tearDown() {
        parallelCompileResult = null;
    }
}
