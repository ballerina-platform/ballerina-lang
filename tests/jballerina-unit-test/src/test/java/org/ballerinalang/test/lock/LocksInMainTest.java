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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests for Ballerina locks.
 *
 * @since 0.961.0
 */
public class LocksInMainTest {

    private CompileResult parallelCompileResult;
    private CompileResult locksCompileResult;

    @BeforeClass
    public void setup() {
        parallelCompileResult = BCompileUtil.compile("test-src/lock/parallel-run-lock.bal");
        assertEquals(parallelCompileResult.getErrorCount(), 0);

        locksCompileResult = BCompileUtil.compile("test-src/lock/locks-in-functions.bal");
        assertEquals(locksCompileResult.getErrorCount(), 0);
    }

    @Test(description = "Tests lock within a lock")
    public void testLockWithinLock() {
        BRunUtil.invoke(locksCompileResult, "testLockWithinLock");
    }

    @Test(description = "Tests lock within workers")
    public void simpleLock() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/simple-lock.bal");
        BRunUtil.invoke(compileResult, "testSimpleLock");
    }

    @Test(description = "Tests lock within lock in workers")
    public void testLockWithinLockInWorkers() {
        BRunUtil.invoke(locksCompileResult, "testLockWithinLockInWorkers");
    }

    @Test(description = "Tests lock inside while loop")
    public void testLockInsideWhileLoop() {
        BRunUtil.invoke(locksCompileResult, "testLockInsideWhileLoop");
    }

    @Test(description = "Tests throwing an error inside lock")
    public void testThrowErrorInsideLock() {
        BRunUtil.invoke(locksCompileResult, "testThrowErrorInsideLock");
    }

    @Test(description = "Tests throwing an error inside a lock inside try catch block")
    public void testThrowErrorInsideLockInsideTrap() {
        BRunUtil.invoke(locksCompileResult, "testThrowErrorInsideLockInsideTrap");
    }

    @Test(description = "Tests throwing an error inside try catch finally block inside a lock")
    public void testThrowErrorInsideTryCatchFinallyInsideLock() {
        BRunUtil.invoke(locksCompileResult, "testThrowErrorInsideTryCatchFinallyInsideLock");
    }

    @Test(description = "Tests throwing an error inside try finally block inside a lock")
    public void testThrowErrorInsideTryFinallyInsideLock() {
        BRunUtil.invoke(locksCompileResult, "testThrowErrorInsideTryFinallyInsideLock");
    }

    @Test(description = "Tests throwing an error inside a lock inside try catch block")
    public void testThrowErrorInsideLockInsideTryCatch() {
        BRunUtil.invoke(locksCompileResult, "testThrowErrorInsideLockInsideTryCatch");
    }

    @Test(description = "Tests throwing an error inside try catch block inside a lock")
    public void testThrowErrorInsideTryCatchInsideLock() {
        BRunUtil.invoke(locksCompileResult, "testThrowErrorInsideTryCatchInsideLock");
    }

    @Test(description = "Tests lock within lock in workers for boolean and blob")
    public void testLockWithinLockInWorkersForBlobAndBoolean() {
        BRunUtil.invoke(locksCompileResult, "testLockWithinLockInWorkersForBlobAndBoolean");
    }

    @Test(description = "Tests lock within functions down the function chain")
    public void testLockWithinFunctionsDownTheLine() {
        CompileResult compileResult = BCompileUtil.compile("test-src/lock/locks_in_functions_down_the_chain.bal");
        BRunUtil.invoke(compileResult, "testLockWithinFunctionsDownTheLine");
    }

    @Test(description = "Tests returning inside lock statement")
    public void testReturnInsideLock() {
        BRunUtil.invoke(locksCompileResult, "testReturnInsideLock");
    }

    @Test(description = "Tests break inside lock statement")
    public void testBreakInsideLock() {
        BRunUtil.invoke(locksCompileResult, "testBreakInsideLock");
    }

    @Test(description = "Tests next inside lock statement")
    public void testNextInsideLock() {
        BRunUtil.invoke(locksCompileResult, "testNextInsideLock");
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

    @Test()
    public void testPanicIfInLockConcurrently() {
        BRunUtil.invoke(parallelCompileResult, "testPanicIfInLockConcurrently");
    }

    @Test(description = "Test for parallel run using locks")
    public void testParallelRunUsingLocks() {
        BRunUtil.invoke(parallelCompileResult, "runParallelUsingLocks");
    }

    @Test(description = "Test for parallel run when invocations have global variable dependencies")
    public void testParallelRunWithInvocationDependencies() {
        BRunUtil.invoke(parallelCompileResult, "testLockWithInvokableAccessingGlobal");
    }

    @Test(description = "Test for parallel run when invocations have loops and global var dependencies")
    public void testParallelRunWithChainedInvocationDependencies() {
        BRunUtil.invoke(parallelCompileResult, "testLockWIthInvokableChainsAccessingGlobal");
    }

    @Test(description = "Test for parallel run when invocation are recursive and have global var dependencies")
    public void testParallelRunWithRecursiveInvocationDependencies() {
        BRunUtil.invoke(parallelCompileResult, "testLockWIthInvokableRecursiveAccessGlobal");
    }

    @Test(description = "Test for parallel run when invocations are imported and contains global var dependencies")
    public void testParallelRunWithImportInvocationDependencies() {
        CompileResult importInvocationDependencies = BCompileUtil.
                compile("test-src/lock/locks_in_imports_test_project");
        BRunUtil.invoke(importInvocationDependencies, "testLockWithInvokableChainsAccessingGlobal");
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

    @AfterClass
    public void tearDown() {
        parallelCompileResult = null;
        locksCompileResult = null;
    }
}
