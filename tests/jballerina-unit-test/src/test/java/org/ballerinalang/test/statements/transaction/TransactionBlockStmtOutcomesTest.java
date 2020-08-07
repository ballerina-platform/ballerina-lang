/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.transaction;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to validate transaction block statement outcomes.
 *
 * @since Swan Lake
 */
public class TransactionBlockStmtOutcomesTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/transaction/transaction_block_stmt_outcomes.bal");
    }

    @Test(description = "Test commit success and success in block stmt outcome")
    public void testCommitSuccessWithSuccessOutcome() {
        BRunUtil.invoke(compileResult, "testCommitSuccessWithSuccessOutcome");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "Error while invoking function 'testCommitSuccessWithFailOutcome'.*",
            description = "Test commit success and failure in block stmt outcome")
    public void testCommitSuccessWithFailOutcome() {
        BRunUtil.invoke(compileResult, "testCommitSuccessWithFailOutcome");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Panic in increment count.*",
            description = "Test commit success and panic in block stmt outcome")
    public void testCommitSuccessWithPanicOutcome() {
        BRunUtil.invoke(compileResult, "testCommitSuccessWithPanicOutcome");
    }

    @Test(description = "Test commit fail and unusual success in block stmt outcome")
    public void testCommitFailWithUnusualSuccessOutcome() {
        BRunUtil.invoke(compileResult, "testCommitFailWithUnusualSuccessOutcome");
    }

    @Test(description = "Test commit fail and fail in block stmt outcome")
    public void testCommitFailWithFailOutcome() {
        BRunUtil.invoke(compileResult, "testCommitFailWithFailOutcome");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Panic in increment count.*",
            description = "Test commit fail and panic in block stmt outcome")
    public void testCommitFailWithPanicOutcome() {
        BRunUtil.invoke(compileResult, "testCommitFailWithPanicOutcome");
    }

    @Test(description = "Test rollback and success in block stmt outcome")
    public void testRollbackWithSuccessOutcome() {
        BRunUtil.invoke(compileResult, "testRollbackWithSuccessOutcome");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "Error while invoking function 'testRollbackWithFailOutcome'.*",
            description = "Test rollback and fail in block stmt outcome")
    public void testRollbackWithFailOutcome() {
        BRunUtil.invoke(compileResult, "testRollbackWithFailOutcome");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Panic in increment count.*",
            description = "Test rollback and panic in block stmt outcome")
    public void testRollbackWithPanicOutcome() {
        BRunUtil.invoke(compileResult, "testRollbackWithPanicOutcome");
    }

    @Test(description = "Test panic from rollback and unusual success in block stmt outcome")
    public void testPanicFromRollbackWithUnusualSuccessOutcome() {
        BRunUtil.invoke(compileResult, "testPanicFromRollbackWithUnusualSuccessOutcome");
    }

    @Test(description = "Test panic from commit and unusual success in block stmt outcome")
    public void testPanicFromCommitWithUnusualSuccessOutcome() {
        BRunUtil.invoke(compileResult, "testPanicFromCommitWithUnusualSuccessOutcome");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "Error while invoking function " +
                    "'testPanicFromRollbackWithUnusualFailOutcome'.*",
            description = "Test panic from rollback and unusual fail in block stmt outcome")
    public void testPanicFromRollbackWithUnusualFailOutcome() {
        BRunUtil.invoke(compileResult, "testPanicFromRollbackWithUnusualFailOutcome");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "Error while invoking function " +
                    "'testPanicFromCommitWithUnusualFailOutcome'.*",
            description = "Test panic from commit and unusual fail in block stmt outcome")
    public void testPanicFromCommitWithUnusualFailOutcome() {
        BRunUtil.invoke(compileResult, "testPanicFromCommitWithUnusualFailOutcome");
    }
}
